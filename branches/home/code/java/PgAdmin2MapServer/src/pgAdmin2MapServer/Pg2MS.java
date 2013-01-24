/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import pgAdmin2MapServer.client.ElementalHttpPost;
import pgAdmin2MapServer.model.Mapfile;
import pgAdmin2MapServer.server.ElementalHttpServer;
import pgAdmin2MapServer.server.RequestManager;

/**
 *
 * @author nicolas
 */
public class Pg2MS {
    public static final String VERSION = "0.0.3";

    /**
     * The Swing frame displaying program logs and allowing geographic files
     * drag'n'drop (not yet available)
     */
    public static FileDroper fileDroper = null;
    /**
     * The port this server starts on
     */
    public static int serverPort = 8081;
    public static boolean debug = true;
    /**
     * The tmp file where MapServer can write image TODO: find correct folder
     * according to plateform
     */
    public static String docRoot = "/tmp/";

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
     * Starts the internal server on configured port.
     *
     * @throws Exception if the port is in use, or another unexpected error
     * occured
     */
    public static void startServer() throws Exception {
        //loadProperties();
        // TODO: find system tmp, not user tmp

        Thread t = new ElementalHttpServer.RequestListenerThread(Pg2MS.serverPort, docRoot);
        t.setDaemon(false);
        t.start();

        // register PG driver
        Class.forName("org.postgresql.Driver");

        //launch GUI
        //Desktop.getDesktop().browse(URI.create(genUrl));
        WindowRunner r = new WindowRunner();
        java.awt.EventQueue.invokeLater(r);
        // its windowOpened event then calls loadLayers
    }

    /**
     * Loads layers and launch browser. Should be called in the main gui onload
     * event
     *
     * @throws Exception
     */
    public static void loadLayers() throws Exception {
        String m = Mapfile.write();
        
        //TODO: implement this behaviour ?
        // opens the normal page: the client, fetching the config, should see its empty 
        //  => no geo to display
        if (m != null && !m.isEmpty()) {
            Pg2MS.log("mapfile written: " + m);
        } else {
            Pg2MS.log("No mapfile to write: no geo data from database");
        }
        //String action = RequestManager.REQUEST_FILE + "?fileName=" + URLEncoder.encode("/resources/ol.html", "UTF-8");
        String action = RequestManager.REQUEST_MAP;
        String serverUrl = "http://localhost:port/action".replace("port", String.valueOf(Pg2MS.serverPort))
                .replace("action", action);

        // Open GUI: a client browser ;)
        Pg2MS.log("Launching default browser with URL: " + serverUrl);
        Desktop.getDesktop().browse(URI.create(serverUrl));

        Pg2MS.log("GUI launched with config: " + Config.getInstance().toString());
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
                RequestManager.REQUEST_NEW_PARAMS,
                Arrays.toString(args).replace("[", "").replace("]", "").replace(" ", "").replace(",", "&"));
        Pg2MS.log("<< Response: " + response.getStatusLine());
        Pg2MS.log(EntityUtils.toString(response.getEntity()));
        Pg2MS.log("==============");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // init configuration
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
