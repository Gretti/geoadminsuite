/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.java_websocket.WebSocket;
import pgAdmin2MapServer.client.ElementalHttpPost;
import pgAdmin2MapServer.model.Mapfile;
import pgAdmin2MapServer.server.ElementalHttpServer;
import pgAdmin2MapServer.server.ElementalWebSocketServer;
import pgAdmin2MapServer.server.RequestManager;

/**
 * TODO: real logging...
 *
 * @author nicolas
 */
public class Pg2MS {

    public static final String VERSION = "0.0.3";
    /**
     * The Relative path to zip file containing HTML resources (Map, JS et al.)
     */
    // for easier debug
    public static final String HTML_RESOURCES_PATH = "/Users/nicolas/code/java/PgAdmin2MapServer/html";
    public static final boolean READ_FROM_ZIP = true;
    /**
     * The Swing frame displaying program logs and allowing geographic files
     * drag'n'drop (not yet available)
     */
    public static FileDroper fileDroper = null;
    /**
     * The web server port this server listen to
     */
    public static int serverPort = 9472;
    /**
     * The WebSocket server port this server listent to
     */
    public static int webSocketServerPort = 8887;
    public static boolean debug = true;
    public static boolean debugNetwork = false;
    public static String mapfileName = "pgadmin_viewer.map";
    // TODO: get it from config or smart guess
    public static String tmpDir = "/tmp";
    public static String mapserverExe = System.getProperty("os.name").contains("Windows") ? "mapserv.exe" : "mapserv";
    public static String mapfileUrl = "http://localhost/cgi-bin/mapserv?map=" + tmpDir + File.separator + mapfileName;
    public static ElementalWebSocketServer wsServer = null;
    // The full path to the HTML resources this program uses to display the map
    // will be computed live when the program is run
    public static File resourceFile = null;
    /**
     * The tmp file where MapServer can write image TODO: find correct folder
     * according to plateform
     */
    public static String docRoot = tmpDir + File.separator;
    private static String todel = "";

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
            }

            System.out.println(msg);
        }
    }

    /**
     * Performs some internal initialization
     */
    public static void init() {
        // init the full path to the HTML resources
        // System.getProperty("user.dir") is not robust enough on plateforms to use it:
        System.out.println(Pg2MS.class.getClass().getProtectionDomain().getCodeSource().getLocation());
        try {
            //s = new File(".").getAbsolutePath();
        } catch (Exception e) {
            
        }
        String sub = "lib" + File.separator + "html.zip";
        Pg2MS.resourceFile = new File(System.getProperty("user.dir"), sub);
        Pg2MS.log("HTML resources in: " + Pg2MS.resourceFile.getAbsolutePath());
    }

    /**
     * Starts the internal servers on configured port: 9472 for the internal web
     * server 8887 for the internal Web Socket server
     *
     *
     * @throws Exception if the port is in use, or another unexpected error
     * occured
     */
    public static void startServer() throws Exception {
        // internal initialisation
        init();
        // Starts Web server
        Thread t = new ElementalHttpServer.RequestListenerThread(Pg2MS.serverPort, docRoot);
        t.setDaemon(false);
        t.start();

        // starts Web Socket server
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
        WindowRunner r = new WindowRunner();
        java.awt.EventQueue.invokeLater(r);
        // its windowOpened event then calls loadLayers(true)
    }

    /**
     * Loads layers and launch browser. Should be called in the main gui onload
     * event
     *
     * @param openBrowser true to open a browser with a MAP url (client
     * initialization) false to send new MapConfig JSON through webSOcket
     * (PgAdmin update)
     * @throws Exception if an error occured
     */
    public static void loadLayers(boolean openBrowser) throws Exception {
        String m = Mapfile.write();

        // opens the normal page: the client, fetching the config, should see its empty 
        //  => no geo to display
        if (m != null && !m.isEmpty()) {
            Pg2MS.log("mapfile written: " + m);
        } else {
            Pg2MS.log("No mapfile to write: no geo data from database");
        }

        if (openBrowser) {
            //String action = RequestManager.REQUEST_FILE + "?fileName=" + URLEncoder.encode("/resources/ol.html", "UTF-8");
            String action = RequestManager.REQUEST_MAP;
            String serverUrl = "http://localhost:port/action".replace("port", String.valueOf(Pg2MS.serverPort))
                    .replace("action", action);

            // Open GUI: a client browser ;)
            Pg2MS.log("Launching default browser with URL: " + serverUrl);
            Desktop.getDesktop().browse(URI.create(serverUrl));

            Pg2MS.log("GUI launched with config: " + Config.getInstance().toString());
            Pg2MS.log(todel);
        } else {
            // send new layer config to client webSocket
            for (WebSocket ws : Pg2MS.wsServer.connections()) {
                if (ws.isOpen()) {
                    Pg2MS.log("sending new MapConfig JSON to client: " + ws.getRemoteSocketAddress().toString());
                    ws.send(Mapfile.getMapConfigJson(false));
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
        //HttpResponse response = ElementalHttpPost.post(Pg2MS.UPDATE_PG_PARAMS, b.toString());
        HttpResponse response = ElementalHttpPost.post(
                "/" + RequestManager.REQUEST_NEW_PARAMS,
                Arrays.toString(args).replace("[", "").replace("]", "").replace(" ", "").replace(",", "&"));
        Pg2MS.log("<< Response: " + response.getStatusLine());
        Pg2MS.log(EntityUtils.toString(response.getEntity()));
        Pg2MS.log("==============");
    }

    /**
     * @param args the command line arguments Client refresh can be triggered
     * with URL:
     * http://localhost:9472/newParams?bindir=/Applications/Dev/pgAdmin3.app/Contents/MacOS&host=&port=5432&database=nicolas&user=nicolas&passwd=&schema=public&table=
     */
    public static void main(String[] args) throws Exception {
        // init configuration
        // TODO: supprimer cela
        todel = "prog args: " + Arrays.toString(args);
        Config.getInstance().parseArgs(args);
        // starts or exit if started
        try {
            startServer();
        } catch (IOException x) {
            Pg2MS.log("Another internal server instance already running on " + Pg2MS.serverPort
                    + ".\n\tException: " + x.toString());
            Pg2MS.log("Sending new params to running server:  " + Arrays.toString(args));
            sendParamsToServer(args);
            System.exit(1);
        }
    }
}
