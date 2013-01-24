/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import pgAdmin2MapServer.Config;
import pgAdmin2MapServer.Pg2MS;

/**
 * Very simple Manager to get a Postgresql connection from a given set of properties
 * @author nicolas
 */
public class ConnectionManager {
    /**
     * Gets a JDBS connection from params, in the form:
     * host=localhost port=5432 dbname=nicolas user=nicolas pwd= schema=public table=mytable 
     * @param params the 
     * @return the connection: the caller must close it
     */
    public static Connection getConnection() throws SQLException {
        
        String url = "jdbc:postgresql://host:port/dbname";
        
        url = url.replace("host", Config.getInstance().host).replace("port", Config.getInstance().port)
                .replace("dbname", Config.getInstance().database);
        Pg2MS.log("jdbc url: " + url + " user:" + Config.getInstance().user);
        Connection conn = DriverManager.getConnection(url, Config.getInstance().user, Config.getInstance().pwd);
        
        return conn;
    } 
}
