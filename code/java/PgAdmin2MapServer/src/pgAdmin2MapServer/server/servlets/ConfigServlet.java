/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import pgAdmin2MapServer.Pg2MS;
import static pgAdmin2MapServer.server.RequestManager.CONFIG_LAYERS;
import static pgAdmin2MapServer.server.RequestManager.CONFIG_TREE_MODEL;

/**
 * Returns the configuration as a JSON object to the caller.
 *
 * @author nicolas
 */
public class ConfigServlet extends HttpServlet {

    public ConfigServlet() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String item = request.getParameter("item");
            String mapConfig = "";
            if (CONFIG_LAYERS.equals(item)) {
                // sends full layer definition as JSON object
                mapConfig = Pg2MS.map.getMapConfigJson(false);
                Pg2MS.log("sending mapConfig allLayers to client: " + mapConfig);
            } else if (CONFIG_TREE_MODEL.equals(item)) {
                // sends only layer tree model as JSON object
                mapConfig = Pg2MS.map.getMapConfigJson(true);
                Pg2MS.log("sending mapConfig treeModel to client: " + mapConfig);
            }
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(mapConfig);
        } catch (JSONException jse) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Pg2MS.log("mapConfig response error: " + jse.toString());
        }
    }
}