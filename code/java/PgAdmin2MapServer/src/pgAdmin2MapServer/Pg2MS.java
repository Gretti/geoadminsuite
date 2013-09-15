/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import org.java_websocket.WebSocket;
import static pgAdmin2MapServer.Pg2MS.createServer;
import static pgAdmin2MapServer.Pg2MS.debug;
import static pgAdmin2MapServer.Pg2MS.init;
import static pgAdmin2MapServer.Pg2MS.map;
import static pgAdmin2MapServer.Pg2MS.mapfileName;
import static pgAdmin2MapServer.Pg2MS.sendParamsToServer;
import static pgAdmin2MapServer.Pg2MS.tmpDir;
import static pgAdmin2MapServer.Pg2MS.webSocketServerPort;
import pgAdmin2MapServer.model.Map;
import pgAdmin2MapServer.server.ElementalWebSocketServer;
import pgAdmin2MapServer.server.JettyServer;

/**
 * Main class: starts the JETTY server on configured port if needed or send new
 * params to running server to refresh client map TODO: real logging..., paths
 * config
 *
 * @author nicolas
 */
public class Pg2MS {

    public static final String VERSION = "0.0.7";
    /**
     * The Relative path to zip file containing HTML resources (Map, JS et al.)
     */
    // for easier debug
    public static final boolean READ_FROM_ZIP = false;
    /**
     * True to use GeoJSON format for the vector layers, false to use MapServer
     * layers
     */
    public static final boolean USE_FORMAT_GEOJSON = false;
    /**
     * The Swing frame displaying program logs and allowing geographic files
     * drag'n'drop (not yet available)
     */
    public static FileDroper fileDroper = null;
    /**
     * The web server port this server listen to TODO: get value from config
     */
    public static int serverPort = 9472;
    /**
     * The WebSocket server port this server listen to * TODO: get value from
     * config
     */
    public static int webSocketServerPort = 8887;
    public static boolean debug = true;
    public static boolean debugNetwork = true;
    public static String mapfileName = "pgadmin_viewer.map";
    // TODO: get it from config or smart guess
    public static File tmpDir = null;
    public static String mapserverExe = null;
    public static String mapfileUrl = null;
    public static ElementalWebSocketServer wsServer = null;
    /**
     * The JETTY servlet server
     */
    public static JettyServer server = null;
    // The full path to the HTML resources this program uses to display the map
    // will be computed live when the program is run
    public static String htmlResources = null;
    private static String todel = "";
    /**
     * The map containing the layers
     */
    public static Map map;

    /**
     * Thread for test WebSocket ticker, sending client a message each 30 sec
     */
    private static class WSTestRunner implements Runnable {

        public void run() {
            Pg2MS.log("Ticker thread started...");

            while (true) {
                if (Pg2MS.wsServer != null) {
                    Date d = new Date();
                    for (WebSocket ws : Pg2MS.wsServer.connections()) {
                        if (ws.isOpen()) {
                            Pg2MS.log("sending ticker to client: " + ws.getRemoteSocketAddress().toString());
                            ws.send("server time: " + d.toString());
                        }
                    }
                } else {
                    Pg2MS.log("Ticker: no webSocket to send to.");
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ie) {
                    Pg2MS.log("Ticker thread error" + ie.toString());
                }
            }
        }
    }

    /**
     * Thread for the DnD frame
     */
    private static class WindowRunner implements Runnable {

        public void run() {
            Pg2MS.fileDroper = new FileDroper();
            Pg2MS.fileDroper.setVisible(true);
            Pg2MS.log("Running version: " + Pg2MS.VERSION);
            Pg2MS.log("path: " + Pg2MS.htmlResources);
            try {
                //Pg2MS.server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * TODO: real logging... log4j ?
     *
     * @param msg
     */
    public static void log(String msg) {
        if (debug) {
            if (Pg2MS.fileDroper != null && Pg2MS.fileDroper.jTextArea1 != null) {
                Pg2MS.fileDroper.jTextArea1.setText(Pg2MS.fileDroper.jTextArea1.getText() + "\n" + msg);
            } else {
                System.out.println("null UI !!");
            }

            System.out.println("log:" + msg);
        }
    }

    /**
     * Performs some internal initialization Config object is available if init
     * called from createServer().
     */
    public static void init() {

        System.out.println(System.getProperty("java.io.tmpdir"));
        // init the full path to the HTML resources according to target plateform:
        // on windows, html.zip resource is built from bindir
        // on *nix plateform, uses the user.dir variable to get the working directory
        // tmp dir must be mapserver readable, so force it.
        if (System.getProperty("os.name").contains("Windows")) {
            Pg2MS.htmlResources = Config.getInstance().binDir + File.separator
                    + "plugins.d" + File.separator + "lib" + File.separator + "html.zip";
            Pg2MS.tmpDir = new File("c:\\windows\\temp");

        } else {
            Pg2MS.htmlResources = Pg2MS.class.getClassLoader().getResource("html").toExternalForm();
            Pg2MS.tmpDir = new File("/tmp");
        }
        Pg2MS.log("HTML resources in: " + Pg2MS.htmlResources);
        Pg2MS.mapserverExe = System.getProperty("os.name").contains("Windows") ? "mapserv.exe" : "mapserv";
        Pg2MS.mapfileUrl = "http://localhost/cgi-bin/mapserv?map=" + tmpDir + File.separator + mapfileName;
    }

    /**
     * Starts the internal servers on configured port: 9472 for the internal
     * Jetty web server, 8887 for the internal Web Socket server
     *
     *
     * @throws Exception if the port is in use, or another unexpected error
     * occured
     */
    public static void createServer() throws Exception {
        // internal initialisation
        init();
        // Starts Web server
        Pg2MS.server = new JettyServer(Pg2MS.serverPort);
        Pg2MS.server.start();

        // starts Web Socket server
        // TODO: replace by a jetty socket server
        Pg2MS.wsServer = new ElementalWebSocketServer(webSocketServerPort);
        Pg2MS.wsServer.start();
        Pg2MS.log("WebSocket Server started on port: " + Pg2MS.wsServer.getPort());

        // starts test ticker 
        //Thread th = new Thread(new WSTestRunner());
        //th.start();

        // register PG driver
        Class.forName("org.postgresql.Driver");

        //launch GUI
        //Desktop.getDesktop().browse(URI.create(genUrl));
        Pg2MS.WindowRunner r = new Pg2MS.WindowRunner();
        java.awt.EventQueue.invokeLater(r);
        // its windowOpened event then calls loadLayers(true)
    }

    /**
     * Returns true if server is running on configured port. TODO: handle port
     * that can change between program calls
     */
    public static boolean serverIsRunning() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Pg2MS.serverPort);
        } catch (IOException e) {
            Pg2MS.log("Port in use, server already started: " + e.toString());
            return true;
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    /**
     * Loads layers and launch browser. It is called in the main gui onload
     * event.
     *
     * @param openBrowser true to open a browser with a MAP url (client
     * initialization) false to send new MapConfig JSON through webSOcket
     * (PgAdmin update)
     * @throws Exception if an error occured
     */
    public static void loadLayers(boolean openBrowser) throws Exception {
        map = new Map();
        map.mapfile.write();
        //String m = Mapfile.write();

        // opens the normal page: the client, fetching the config, should see its empty 
        //  => no geo to display
//        if (m != null && !m.isEmpty()) {
//            Pg2MS.log("mapfile written: " + m);
//        } else {
//            Pg2MS.log("No mapfile to write: no geo data from database");
//        }

        if (openBrowser) {
            String serverUrl = "http://localhost:port/".replace("port", String.valueOf(Pg2MS.serverPort));

            // Open GUI: a client browser ;)
            Pg2MS.log("Launching default browser with URL: " + serverUrl);
            Desktop.getDesktop().browse(URI.create(serverUrl));

            Pg2MS.log("GUI launched with config: " + Config.getInstance().toString());
            Pg2MS.log(todel);
        } else {
            // send new layer config to client webSocket:
            // TODO: replace with a simple server order mechanism to tell client to fetch resources
            // instead of sending resources through socket from server
            for (WebSocket ws : Pg2MS.wsServer.connections()) {
                if (ws.isOpen()) {
                    Pg2MS.log("sending new MapConfig JSON to client: " + ws.getRemoteSocketAddress().toString());
                    ws.send(Pg2MS.map.getMapConfigJson(false));
                }
            }
        }
    }

    /**
     * Method to call the server, giving it new arguments
     *
     * @param args
     * @throws Exception
     */
    public static void sendParamsToServer(String[] args) throws Exception {
        String params = Arrays.toString(args).replace("[", "").replace("]", "").replace(" ", "").replace(",", "&");
        String url = "http://host:port/newParams?params"
                .replace("host", "localhost")
                .replace("port", "" + Pg2MS.serverPort)
                .replace("params", params);
        try {

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            con.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param url
     * @return the text returned by the url, or null if this URL points to a non
     * textual resource (img for instance)
     */
    public static String urlIsText(String url) {
        System.out.println("testing url: " + url);
        String res = null;
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =
                    (HttpURLConnection) new URL(url).openConnection();
            //con.setRequestMethod("HEAD");
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String ct = con.getContentType().toLowerCase();
                System.out.println("ct: " + ct);
                if (!ct.contains("image")) {
                    System.out.println("\tnot an image...");
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                            con.getInputStream()));

                    StringBuilder response = new StringBuilder();
                    String inputLine;

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    in.close();

                    res = response.toString();
                    Pg2MS.log("invalid url, server response: " + res);
                } else {
                    System.out.println("\tan image");
                    FileOutputStream fout = new FileOutputStream(new File("/tmp/out.png"));
                    InputStream is = null;
                    try {
                        is = con.getInputStream();
                        byte[] bytebuff = new byte[4096];
                        int n;

                        while ((n = is.read(bytebuff)) > 0) {
                            fout.write(bytebuff, 0, n);
                        }
                        fout.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * @param args the command line arguments. Client refresh can be triggered
     * with URL of the form:
     * http://localhost:9472/newParams?bindir=/Applications/Dev/pgAdmin3.app/Contents/MacOS&host=&port=5432&database=nicolas&user=nicolas&passwd=&schema=public&table=
     */
    public static void main(String[] args) throws Exception {
        // init configuration
        // TODO: supprimer cela
        todel = "prog args: " + Arrays.toString(args);
        Config.getInstance().parseArgs(args);
        // starts or exit if started
        if (Pg2MS.serverIsRunning()) {
            Pg2MS.log("Another internal server instance already running on " + Pg2MS.serverPort);
            Pg2MS.log("Sending new params to running server:  " + Arrays.toString(args));
            sendParamsToServer(args);
            System.exit(1);

        } else {
            createServer();
        }
    }
}
