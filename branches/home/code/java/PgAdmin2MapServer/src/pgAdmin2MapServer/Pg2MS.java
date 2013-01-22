/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import pgAdmin2MapServer.client.ElementalHttpPost;
import pgAdmin2MapServer.model.Mapfile;
import pgAdmin2MapServer.server.ElementalHttpServer;

/**
 *
 * @author nicolas
 */
public class Pg2MS {
    public static final String UPDATE_PG_PARAMS = "/updatePgParams";
    public static final String GENERATE_MAP = "/generateMap";
    public static final String VERSION = "0.0.3";
    
    //public static final int SERVER_PORT = 8080;
    
    /**
     * The socket to make our class a single instance: other invokations should
     * fail
     */
    private static ServerSocket SERVER_SOCKET;
    public static FileDroper fileDroper = null;
    public static int serverPort = 8081;

    public Pg2MS() {
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
     * TODO: real logging...
     * @param msg 
     */
    public static void log(String msg) {
        if (Pg2MS.fileDroper != null && Pg2MS.fileDroper.jTextArea1 != null) {
            Pg2MS.fileDroper.jTextArea1.setText(Pg2MS.fileDroper.jTextArea1.getText() + "\n" + msg);
        }

        System.out.println(msg);
    }
    
    /**
     * Loads the properties file
     */
    private static void loadProperties() throws IOException {
        Properties p = new Properties();
        p.load(Pg2MS.class.getClassLoader().getResourceAsStream("/pgAdmin2MapServer/resources/pg2ms.properties"));
        serverPort = Integer.valueOf(p.getProperty("INTERNAL_SERVER_PORT", "8081"));
        Pg2MS.log("props read: " + serverPort);
    }

    public static void startServer() throws Exception {
        //loadProperties();
        // TODO: find system tmp, not user tmp
        String docRoot = "/tmp/";
        
        //Thread t = new ElementalHttpServer.RequestListenerThread(8080, args[0]);
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
     * Loads layers and launch browser. Should be called in the main gui onload event
     * @throws Exception 
     */
    public static void loadLayers() throws Exception {
        // and loads layers from program arguments
        Mapfile.params = args;
        // directly write mapfile and call the openlayers mapserver template, time
        // to set-up a nice HTML interface
        String m = Mapfile.write();
        Pg2MS.log("mapfile written: " + m);
        
        //String genUrl = "http://localhost/cgi-bin/mapserv?mode=browse&template=OpenLayers&map=/tmp/pgadmin_viewer.map&params=" 
        //        + Arrays.toString(args).replace("[", "").replace("]", "").replace(" ", "").replace(",", "&");
        String genUrl = "http://localhost:" + Pg2MS.serverPort + Pg2MS.GENERATE_MAP;
        Pg2MS.log("calling: " + genUrl);
        
        Desktop.getDesktop().browse(URI.create(genUrl));
        
        Pg2MS.log("gui launched with args: " + Arrays.toString(args));
    }

    public static void sendParamsToServer(String[] args) throws Exception {
        StringBuilder b = new StringBuilder("{");
        b.append("'dbname': ").append("'").append(args[0]).append("'");
        b.append("}");
        //HttpResponse response = ElementalHttpPost.post(Pg2MS.UPDATE_PG_PARAMS, b.toString());
        HttpResponse response = ElementalHttpPost.post(Pg2MS.UPDATE_PG_PARAMS, Arrays.toString(args));
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
            Pg2MS.log("Another internal server instance already running: " + Pg2MS.serverPort 
                    + " .Exit: " + x.toString());
            sendParamsToServer();
            System.exit(1);
        }
    }
}
