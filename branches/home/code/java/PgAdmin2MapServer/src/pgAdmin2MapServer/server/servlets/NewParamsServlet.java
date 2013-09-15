/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.java_websocket.WebSocket;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;

/**
 *
 * @author nicolas
 */
public class NewParamsServlet extends HttpServlet {

    public NewParamsServlet() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, String[]> params = request.getParameterMap();

            Map<String, String> p = new HashMap<String, String>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                p.put(entry.getKey(), entry.getValue()[0]);
            }

            Config.getInstance().parseArgs(p);
            Pg2MS.log("Receiving new parameters from client or second program: " + Config.getInstance().toString());
            Pg2MS.map.loadLayers();
            Pg2MS.map.mapfile.write();

            // send client a refresh message to ask it reload mapConfig
            if (Pg2MS.wsServer.connections().isEmpty()) {
                Pg2MS.log("No connected client found. Either map window was closed or a socket error occured: ");
            } else {
                for (WebSocket ws : Pg2MS.wsServer.connections()) {
                    if (ws.isOpen()) {
                        Pg2MS.log("sending refresh config command to client: " + ws.getRemoteSocketAddress().toString());
                        ws.send(Pg2MS.wsServer.REFRESH_CONFIG);
                    }
                }
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
