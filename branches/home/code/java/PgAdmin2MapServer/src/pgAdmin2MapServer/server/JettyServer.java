/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import pgAdmin2MapServer.Pg2MS;
import pgAdmin2MapServer.server.servlets.ConfigServlet;
import pgAdmin2MapServer.server.servlets.GeoJSONServlet;
import pgAdmin2MapServer.server.servlets.NewParamsServlet;

public class JettyServer {
    public static final String REQUEST_MAP_CONFIG = "/mapConfig";
    public static final String REQUEST_NEW_PARAMS = "/newParams";
    public static final String REQUEST_GEOJSON = "/geoJSON";

	private Server server;
	
	public JettyServer() {
		this(Pg2MS.serverPort);
	}
	public JettyServer(Integer runningPort) {
		server = new Server(runningPort);
        ResourceHandler resource_handler = new ResourceHandler();

        String webDir = JettyServer.class.getClassLoader().getResource("html").toExternalForm();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index_ol2.html"});
        resource_handler.setResourceBase(webDir);
        //resource_handler.setResourceBase(".");
        //resource_handler.setContextPath("/");

        ServletHandler configHandler = new ServletHandler();
        server.setHandler(configHandler);
        configHandler.addServletWithMapping(ConfigServlet.class, JettyServer.REQUEST_MAP_CONFIG);

        ServletHandler geoJsonHandler = new ServletHandler();
        server.setHandler(geoJsonHandler);
        configHandler.addServletWithMapping(GeoJSONServlet.class, JettyServer.REQUEST_GEOJSON);

        ServletHandler newParamsHandler = new ServletHandler();
        server.setHandler(newParamsHandler);
        configHandler.addServletWithMapping(NewParamsServlet.class, JettyServer.REQUEST_NEW_PARAMS);


        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
            configHandler, 
            geoJsonHandler, 
            newParamsHandler, 
            resource_handler, 
            new DefaultHandler()});
        
        server.setHandler(handlers);
	}
	
	public void setHandler(ContextHandlerCollection contexts) {
		server.setHandler(contexts);
	}
	
	public void start() throws Exception {
		server.start();
	}
	
	public void stop() throws Exception {
		server.stop();
		server.join();
	}
	
	public boolean isStarted() {
		return server.isStarted();
	}
	
	public boolean isStopped() {
		return server.isStopped();
	}
}