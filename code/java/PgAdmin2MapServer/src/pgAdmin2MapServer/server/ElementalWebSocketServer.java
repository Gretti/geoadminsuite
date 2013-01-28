/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import pgAdmin2MapServer.Pg2MS;

/**
 * A simple WebSocket Server to communicate with client, sending it new layer list from PgAdmin.
 * This server manages WebSocket client requests. It maintain the socket opened until
 * either the client closes it (closing browser windows) or the program is stopped.
 * 
 * The valid endpoints for this socket are:
 *  - /configDispatcher to open a socket to receive new mapConfig from server
 * @author nicolas
 */
public class ElementalWebSocketServer extends WebSocketServer {
    public static final String CONFIG_DISPATCHER = "/configDispatcher";

	public ElementalWebSocketServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public ElementalWebSocketServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        if (ElementalWebSocketServer.CONFIG_DISPATCHER.equals(handshake.getResourceDescriptor())) {
            conn.send("configDispatcher started. Will send mapConfig updates when PgAdmin call this plugin...");
        } else {
            conn.send("Invalid endpoint called: " + handshake.getResourceDescriptor());
        }
		//this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        
		Pg2MS.log("New WebSocket connection from: " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		//this.sendToAll( conn + " has left the room!" );
		Pg2MS.log("Client: " + conn + " closed the connection. Reason: " + reason);
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		//conn.send("Sending message to conn: " + conn);
		Pg2MS.log("WebSocket Server received client message: " + message );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ElementalWebSocketServer s = new ElementalWebSocketServer( port );
		s.start();
		System.out.println( "ElementalWebSocketServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
		}
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		Pg2MS.log("WebSocket Server error: " + ex.toString());
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
}
