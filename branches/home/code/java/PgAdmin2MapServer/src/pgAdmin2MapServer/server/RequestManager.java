/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.model.Mapfile;

/**
 * Static class to manage server Service API: handle client (MapFish Map)
 * requests
 *
 * @author nicolas
 */
public class RequestManager {

    public static final String REQUEST_MAP = "map";
    public static final String REQUEST_MAP_CONFIG = "mapConfig";
    public static final String REQUEST_FILE = "file";
    public static final String REQUEST_NEW_PARAMS = "newParams";
    public static final String VERSION = "0.0.3";
    // Configuration item values
    /**
     * sent by the client to get the MapConfig object with all layers in a JSON
     * object
     */
    public static final String CONFIG_LAYERS = "allLayers";

    /**
     * processes incoming request and calls suitable method. TODO: understant HC
     * API :D
     */
    public static void processRequest(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        if (request instanceof HttpEntityEnclosingRequest) {
            try {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                String entityContent = EntityUtils.toString(entity);
                String action = URLDecoder.decode(request.getRequestLine().getUri(), "UTF-8");
                Pg2MS.log("action: " + action + " content: " + entityContent);

                if (REQUEST_NEW_PARAMS.equals(action)) {
                    Pg2MS.log("updating pgadmin parameters: " + entityContent);
                    Config.getInstance().updateParams(entityContent);
                    //generateMap(request, response, context);

                    //TODO: websocket with client ? 
                    /*String genUrl = "http://localhost:" + Pg2MS.serverPort + Pg2MS.GENERATE_MAP;
                     Pg2MS.log("calling: " + genUrl);

                     Desktop.getDesktop().browse(URI.create(genUrl));*/
                } else if (REQUEST_FILE.equals(action)) {
                    Pg2MS.log("File requested: " + entityContent);
                    RequestManager.File(entityContent, response, context);
                } else if (REQUEST_MAP_CONFIG.equals(action)) {
                    Map<String, String> params = getParameterMap(entityContent);
                    MapConfig(params, response, context);
                } else {
                    throw new Exception("invalid endpoint: " + action);
                }
            } catch (Exception e) {
                Pg2MS.log("Server Error1: " + e.toString());
                // todo: custom http codes according to failures
                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else if (request instanceof BasicHttpRequest) {
            String target = "";
            try {
                BasicHttpRequest req = (BasicHttpRequest) request;
                target = URLDecoder.decode(req.getRequestLine().getUri(), "UTF-8");
                Map<String, String> params = RequestManager.getParameterMap(target);
                String serverAction = params.get("serverAction");

                if (REQUEST_MAP.equals(serverAction)) {
                    Map(request, response, context);
                } else if (REQUEST_FILE.equals(serverAction)) {
                    File(params.get("fileName"), response, context);
                } else if (REQUEST_MAP_CONFIG.equals(serverAction)) {
                    MapConfig(params, response, context);
                } else {
                    if (Pg2MS.debugNetwork) {
                        Pg2MS.log("RequestManager: unknown server action: " + serverAction
                                + ". Requesting resource as a file");
                    }
                    File(serverAction, response, context);
                }
            } catch (Exception e) {
                // todo: custom http codes according to failures
                Pg2MS.log("Server Error2 : " + e.toString() + ": " + target);
                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    /**
     * Called when server receive a REQUEST_MAP request: returns the HTML
     * MapFish map (with its resources) to the client.
     *
     * No parameters are expected from the client TODO: replace by a
     * file?name=map.html ? 
     * TODO: factorize response
     */
    public static void Map(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws Exception {
        //String m = Mapfile.write();
        //Pg2MS.log("mapfile written: " + m);
        String mapHtml = "index.html";
        response.setStatusCode(HttpStatus.SC_OK);
        ContentType contentType = getContentType(mapHtml);
        InputStreamEntity ise = new InputStreamEntity(
                getStreamFromZip(mapHtml), 
                -1, 
                contentType);
        response.setEntity(ise);
    }

    /**
     * Called when server receive a REQUEST_MAP_CONFIG request: returns the JSON
     * MapConfig object allowing client to enable the mapFile and its layers.
     * Layers are returned by schemas and databases, in alphabetic order (same
     * as PgAdmin left tree).
     *
     * No parameters are expected from the client
     *
     */
    public static void MapConfig(
            final Map<String, String> params,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException, JSONException {

        if (params != null) {
            String item = params.get("item");

            if (CONFIG_LAYERS.equals(item)) {
                // sends full layer definition as JSON object
                response.setStatusCode(HttpStatus.SC_OK);
                String mapConfig = Mapfile.getMapConfigJson();
                Pg2MS.log("sending mapConfig to client: " + mapConfig);
                StringEntity body = new StringEntity(mapConfig, ContentType.create("application/json", Charset.forName("UTF-8")));
                response.setEntity(body);
            }
        }
    }

    /**
     * Called when server receive a REQUEST_FILE request: returns the file whose
     * name is given in parameter. Parameters: name=<fileName> where <filename>
     * is the qualified name of the file to get.
     * Files are read from external zip lying in the lib folder.
     * It allows client modifications.
     * TODO: mutualize ZipFile
     */
    public static void File(
            final String fileName,
            final HttpResponse response,
            final HttpContext context) throws Exception {

        if (Pg2MS.debugNetwork) {
            Pg2MS.log("Sending file: " + fileName);
        }
        response.setStatusCode(HttpStatus.SC_OK);
        ContentType contentType = getContentType(fileName);
        InputStreamEntity ise = new InputStreamEntity(
                getStreamFromZip(fileName), 
                -1, 
                contentType);
        response.setEntity(ise);
    }

    /**
     * Try to guess the content type from filename extension
     *
     * @param fileName
     * @return
     */
    private static ContentType getContentType(String fileName) {
        ContentType res = ContentType.create("text/html", Charset.forName("UTF-8"));

        if (fileName != null) {
            if (fileName.endsWith(".js")) {
                res = ContentType.create("text/javascript", Charset.forName("UTF-8"));
            } else if (fileName.endsWith(".css")) {
                res = ContentType.create("text/css", Charset.forName("UTF-8"));
            } else if (fileName.endsWith(".gif")) {
                res = ContentType.create("image/gif");
            } else if (fileName.endsWith(".png")) {
                res = ContentType.create("image/png");
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                res = ContentType.create("image/jpeg");
            } else {
                // defaut CT is html
            }
        }

        return res;
    }

    /**
     * Called when server receive a REQUEST_NEW_PARAMS request: Rebuild the list
     * of layers based on new parameters rewrite the mapfile and tells client to
     * refresh itself, sending it a new MapConfig JSON object Parameters:
     * &host=h&port=p&database=d&user=u&passwd=p[&schema=s&table=t]
     *
     * returns the new MapConfig JSON object
     */
    public static void newParams() {
    }

    /**
     * Own query parser, time to find how to receive a HC request with
     * parameters...
     *
     * @param queryString the received query string
     * @return a map with query parameters as key/value + a "serverAction" key
     * containing the server action called by the client
     */
    public static Map<String, String> getParameterMap(String queryString) {
        Map<String, String> params = new HashMap<String, String>();
        if (queryString == null || queryString.length() == 0) {
            return params;
        }
        int argSep = queryString.indexOf("?");
        String serverAction = "";
        if (argSep < 0) {
            serverAction = queryString.substring(1);
        } else {
            // some params
            serverAction = queryString.substring(1, argSep);
            String[] vals = queryString.substring(argSep + 1).split("&");
            for (String s : vals) {
                String[] p = s.split("=");
                if (p.length > 1) {
                    params.put(p[0], p[1]);
                }
            }
        }
        params.put("serverAction", serverAction);

        return params;
    }
    
    /**
     * Gets the filename from the HTML zip resource
     * @param fileName
     * @return 
     */
    private static InputStream getStreamFromZip(String fileName) throws Exception {
        File f = new File(Config.getInstance().binDir, Pg2MS.HTML_RESOURCES_PATH);
        ZipFile zip = new ZipFile(f);
        ZipEntry entry = new ZipEntry(fileName);
        return zip.getInputStream(new ZipEntry(fileName));
    }
    
}
