package org.geogurus.tools.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Logger;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * A static class that gives a Datasource object for a given connection URI, maintaining a Pool
 * of connections by using Jakarat DBCP and Pool common tools.
 * For each distinct URI, a BasicDataSource object will be created and maintained into a hashtable.
 * JDBC drivers are loaded once.
 * @author nicolas Ribot
 */
public class ConPool2 implements Serializable {
    // Class constants for DB type that other classes can use to precise their actual
    // database type
    public  static final byte DBTYPE_ORACLE = 1;
    public  static final byte DBTYPE_POSTGRES = 2;
    public  static final byte DBTYPE_DB2 = 3;
    public  static final byte DBTYPE_MYSQL = 4;
    
    /** the internal hashtable storing BasicDataSource objects under a key which
     * is the database connection string to pass to the driver.
     */
    private Hashtable<String, BasicDataSource> dataSources = null;
    private transient Logger logger = null;
    /** the singleton instance */
    private final static ConPool2 instance = new ConPool2();
    private BasicDataSource dataSource = null;
    /**
     * private ctor to make it singleton
     */
    private ConPool2() {
        logger = Logger.getLogger(ConPool2.class.getName());
        loadDriver("oracle.jdbc.driver.OracleDriver");
        loadDriver("org.postgresql.Driver");
    }

	private void loadDriver(String driverName) {
		try {
            Class.forName(driverName);
        } catch (ClassNotFoundException cnfe) {
            logger.severe(cnfe.getMessage());
        }
	}
    
    /**
     * gets an instance of this class (singleton)
     * @return
     */
    public static ConPool2 getInstance() {
            
        return instance;
    }
    
    /**
     * Registers the given DB connection URI into the internal hashtable, associating
     * it with a PoolingDataSource object created for this connection URI.
     * @param connectURI the database connection URI to register as a key in the internal
     * database. The connection URI must contain username and password, as for example:
     * <ul>
     * <li>Oracle:     jdbc:oracle:thin:geo/geo@laptox:1521:orcl or</li>
     * <li>PostgreSQL: jdbc:postgresql://localhost:5432/test?user=postgres&password=postgres</li>
     * </ul>
     */
    private void registerConnectURI(String connectURI) {
        if (dataSources == null) {
            dataSources = new Hashtable<String, BasicDataSource>();
        }
        if (! dataSources.containsKey(connectURI)) {
            logger.fine("registering URI: " + connectURI);
            BasicDataSource ds = new BasicDataSource();
            if (connectURI.indexOf("jdbc:oracle") >= 0) {
                ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            } else if (connectURI.indexOf("jdbc:postgresql") >= 0) {
                ds.setDriverClassName("org.postgresql.Driver");
            } else {
                // default to postgres
                ds.setDriverClassName("org.postgresql.Driver");
            }
            ds.setUrl(connectURI);            
            //stores into the hash:
            dataSources.put(connectURI, ds);
        } else {
            logger.fine("using already configurd uri: " + connectURI);
        }
    }
    /**
     * Returns a Pooled datasource for the given URI, using the pool mechanism
     * @param connectURI
     * @return
    public DataSource getDataSource(String connectURI) {
        // then register this connectURI into the hash
        registerConnectURI(connectURI);
        return dataSources.get(connectURI);
    }
     */
    
    /**
     * Returns a direct connection to the given URI
     * @param connectURI
     * @return a database connection for the given URI
    public DataSource getDataSource(String connectURI) {
        // then register this connectURI into the hash
        //registerConnectURI(connectURI);
        if (dataSource == null) {
            dataSource = new BasicDataSource();
        }
        if (connectURI.indexOf("jdbc:oracle") >= 0) {
            dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        } else if (connectURI.indexOf("jdbc:postgresql") >= 0) {
            dataSource.setDriverClassName("org.postgresql.Driver");
        } else {
            // default to postgres
            dataSource.setDriverClassName("org.postgresql.Driver");
        }
        dataSource.setUrl(connectURI);
        return dataSource;
    }
     */
    
    /** 
     * returns a java connection for the given URI
     * @param connectURI
     * @return a jdbc connection to the specified URI, or null
     */
    public Connection getConnection(String connectURI) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(connectURI);
        } catch (SQLException sqle) {
            logger.severe(sqle.getMessage());
        }
        return con;
    }
    /**
     * prints connection statistics
     * @param connectURI the database connection URI for which the number of active conn
     * is to retrieved 
     * @return
     */
    public String getStatistic(String connectURI) {
        if (connectURI == null || !dataSources.containsKey(connectURI)) {
            return null;
        }
        BasicDataSource ds = dataSources.get(connectURI);
        StringBuilder stats = new StringBuilder("active: ");
        stats.append(ds.getNumActive());
        stats.append(" (max: ");
        stats.append(ds.getMaxActive());
        stats.append(") idle: ");
        stats.append(ds.getNumIdle());
        stats.append(" (max: ");
        stats.append(ds.getMaxIdle());
        stats.append(")");
        
        return stats.toString();
        //return (()ConPool2.pooledDataSources.get(connectURI).getConnection()).get
    }
    
    /** returns
     * 
     * Number of stored datasources
     * 
     * @return the number of elements in the DataSources hashtable
     */
    public int getNumDataSources() {
        return (dataSources != null ? dataSources.size() : 0);
    }
    
    /**
     * Returns a String representing a database connection URL according to given connection parameters
     * and db Type. See class constants for available dbTypes
     * @param host the database host
     * @param port the database port
     * @param dbName the database name/instance
     * @param user the database username
     * @param pwd the database userPwd
     * @param the database type as specified by this class constants
     * @return a string representing a database connection, for example:
     * <ul>
     * <li>Oracle:     jdbc:oracle:thin:geo/geo@laptox:1521:orcl or</li>
     * <li>PostgreSQL: jdbc:postgresql://localhost:5432/test?user=postgres&password=postgres</li>
     * </ul>
     * 
     */
    public String getConnectionURI(String host, String port, String dbName, String user, String pwd, byte dbType) {
        String connectionURI = null;
        switch (dbType) {
            case (ConPool2.DBTYPE_POSTGRES):
                connectionURI = "jdbc:postgresql://" + host + ":" +
                        port + "/" +
                        dbName + "?user=" +
                        user + "&password=" + pwd;
                break;
            case (ConPool2.DBTYPE_ORACLE):
                connectionURI = "jdbc:oracle:thin:" + user + "/" +
                        pwd + "@" + host + ":" +
                        port + ":" + dbName;
                break;
            default:
                // not a supported type or not a db type ?
                logger.severe("not a supported database: " + dbType);
                break;
        }
        return connectionURI;
    }
}
