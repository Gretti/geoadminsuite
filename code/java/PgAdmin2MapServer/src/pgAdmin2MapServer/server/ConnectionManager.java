/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pgAdmin2MapServer.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
    public static Connection getConnection(String[] params) throws SQLException {
        String host = params[0].split("=").length > 1 ? params[0].split("=")[1] : "" ;
        String port = params[1].split("=").length > 1 ? params[1].split("=")[1] : "" ;
        String dbname = params[2].split("=").length > 1 ? params[2].split("=")[1] : "" ;
        String user = params[3].split("=").length > 1 ? params[3].split("=")[1] : "" ;
        String pwd = params[4].split("=").length > 1 ? params[4].split("=")[1] : "" ;
        
        String url = "jdbc:postgresql://host:port/dbname";
        url = url.replace("host", host).replace("port", port).replace("dbname", dbname);
        Pg2MS.log("jdbc url: " + url + " user:" + user + " pwd:" + pwd);
        Connection conn = DriverManager.getConnection(url, user, pwd);
        
        return conn;
    } 
}
