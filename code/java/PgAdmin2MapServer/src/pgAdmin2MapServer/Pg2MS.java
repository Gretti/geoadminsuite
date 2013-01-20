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
import pgAdmin2MapServer.mapserver.MapfileWriter;
import pgAdmin2MapServer.server.ElementalHttpServer;

/**
 *
 * @author nicolas
 */
public class Pg2MS {
    public static final String UPDATE_PG_PARAMS = "/updatePgParams";
    public static final String GENERATE_MAP = "/generateMap";
    //public static final int SERVER_PORT = 8080;
    
    /**
     * The socket to make our class a single instance: other invokations should
     * fail
     */
    private static ServerSocket SERVER_SOCKET;
    private static String msg = "";
    public static FileDroper fileDroper = null;
    private static int serverPort = 8081;

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
     * Loads the properties file
     */
    private static void loadProperties() throws IOException {
        Properties p = new Properties();
        p.load(Pg2MS.class.getClassLoader().getResourceAsStream("/pgAdmin2MapServer/resources/pg2ms.properties"));
        serverPort = Integer.valueOf(p.getProperty("INTERNAL_SERVER_PORT", "8081"));
        System.out.println("props read: " + serverPort);
    }

    public static void startServer(String[] args) throws IOException {
        //loadProperties();
        // TODO: find system tmp, not user tmp
        String docRoot = "/tmp/";
        String genUrl = "http://localhost:" + Pg2MS.serverPort + Pg2MS.GENERATE_MAP;
        
        //Thread t = new ElementalHttpServer.RequestListenerThread(8080, args[0]);
        System.out.println("calling: " + genUrl);
        Thread t = new ElementalHttpServer.RequestListenerThread(Pg2MS.serverPort, docRoot);
        t.setDaemon(false);
        t.start();

        // and launch GUI, should not be called if server already running
        MapfileWriter.params = args;
        Desktop.getDesktop().browse(URI.create(genUrl));
        WindowRunner r = new WindowRunner();
        java.awt.EventQueue.invokeLater(r);
        System.out.println("gui launched...");
    }

    public static void sendParamsToServer(String[] args) throws Exception {
        StringBuilder b = new StringBuilder("{");
        b.append("'dbname': ").append("'").append(args[0]).append("'");
        b.append("}");
        //HttpResponse response = ElementalHttpPost.post(Pg2MS.UPDATE_PG_PARAMS, b.toString());
        HttpResponse response = ElementalHttpPost.post(Pg2MS.UPDATE_PG_PARAMS, Arrays.toString(args));
        System.out.println("<< Response: " + response.getStatusLine());
        System.out.println(EntityUtils.toString(response.getEntity()));
        System.out.println("==============");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // starts or exit if started
        try {
            startServer(args);
        } catch (IOException x) {
            sendParamsToServer(args);
            System.out.println("Another internal server instance already running: " + Pg2MS.serverPort 
                    + " .Exit: " + x.getMessage());
            System.exit(1);
        }
        // TODO code application logic here
        String params = Arrays.toString(args);
        //File f = new File ("/tmp/test");
        //FileWriter fw = new FileWriter(f);
        //fw.write(params);
        //fw.close();
        if (args.length > 6) {
            String host = args[0];
            String port = args[1];
            String user = args[2];
            String passwd = args[3];
            String database = args[4];
            String schema = args[5];
            String table = args[6];
        }
        //System.exit(0);
    }
}
