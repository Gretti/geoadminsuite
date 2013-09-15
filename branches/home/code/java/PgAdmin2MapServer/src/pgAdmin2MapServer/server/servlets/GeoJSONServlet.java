/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server.servlets;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.model.Map;

/**
 *
 * @author nicolas
 */
public class GeoJSONServlet extends HttpServlet {

    public GeoJSONServlet() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String dbName = request.getParameter("dbName");
            String layerName = request.getParameter("layerName");
            int maxFeature = Integer.parseInt(request.getParameter("maxFeatures"));
            
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            
            OutputStream out = response.getOutputStream();
            Pg2MS.map.layerToGeoJson(dbName, layerName, maxFeature, out);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Pg2MS.log("geoJSON response error: " + e.toString());
            e.printStackTrace();
        }
    }
}