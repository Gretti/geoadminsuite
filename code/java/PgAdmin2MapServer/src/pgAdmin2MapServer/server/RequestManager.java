/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
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
                } else {
                    throw new Exception("invalid endpoint: " + action);
                }
            } catch (Exception e) {
                Pg2MS.log("Server Error1: " + e.toString());
                // todo: custom http codes according to failures
                response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            }
        } else if (request instanceof BasicHttpRequest) {
            try {
                BasicHttpRequest req = (BasicHttpRequest) request;
                URLEncodedUtils.parse();
                String target = URLDecoder.decode(req.getRequestLine().getUri(), "UTF-8");
                if (REQUEST_MAP.equals(target)) {
                    Map(request, response, context);
                } else {
                    HttpParams p = request.getParams();
                    Pg2MS.log("RequestManager: unknown action: " + target);
                }
            } catch (Exception e) {
                // todo: custom http codes according to failures
                Pg2MS.log("Server Error2 : " + e.toString());
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
     */
    public static void Map(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {
        //String m = Mapfile.write();
        //Pg2MS.log("mapfile written: " + m);

        //URL u = ElementalHttpServer.class.getResource("/pgAdmin2Mapserver/resources/html/ol.html");
        InputStream is = ElementalHttpServer.class.getResourceAsStream("/resources/ol.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            //new OpenLayers.Bounds(1682667.23673968, 2182020.94070385, 1719513.08792259, 2242575.97358883)
            if (Mapfile.olBounds != null) {
                //line = line.replace("\"$$BOUNDS$$\"", MapfileWriter.olBounds);
            }
            sb.append(line).append("\n");
        }
        is.close();
        //URL u = Thread.currentThread().getContextClassLoader().getResource("/pgAdmin2Mapserver/resources/html/ol.html");
        //File f = new File(u.toURI());
        Pg2MS.log("sending file: /pgAdmin2Mapserver/resources/html/ol.html");
        response.setStatusCode(HttpStatus.SC_OK);
        StringEntity body = new StringEntity(sb.toString(), ContentType.create("text/html", Charset.forName("UTF-8")));
        //FileEntity body = new FileEntity(f, ContentType.create("text/html", (Charset) null));
        response.setEntity(body);
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
    public static void MapConfig() {
    }

    /**
     * Called when server receive a REQUEST_FILE request: returns the file whose
     * name is given in parameter. Parameters: name=<fileName> where <filename>
     * is the qualified name of the file to get.
     *
     */
    public static void File(
            final String fileName,
            final HttpResponse response,
            final HttpContext context) throws HttpException, IOException {

        InputStream is = ElementalHttpServer.class.getResourceAsStream(fileName);
        Pg2MS.log("Sending file: " + fileName);
        response.setStatusCode(HttpStatus.SC_OK);
        InputStreamEntity ise = new InputStreamEntity(is, -1, ContentType.create("text/html", Charset.forName("UTF-8")));
        response.setEntity(ise);
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
     */
    public static Map<String, String> getParameterMap(String queryString) {
        Map<String, String> mapOfLists = new HashMap<String, String>();
        if (queryString == null || queryString.length() == 0) {
            return mapOfLists;
        }
        List<NameValuePair> list = URLEncodedUtils.parse(URI.create("http://localhost/?" + queryString), "UTF-8");
        for (NameValuePair pair : list) {
            List<String> values = mapOfLists.get(pair.getName());
            if (values == null) {
                values = new ArrayList<String>();
                mapOfLists.put(pair.getName(), values);
            }
            if (pair.getValue() != null) {
                values.add(pair.getValue());
            }
        }

        return mapOfLists;
    }
}
