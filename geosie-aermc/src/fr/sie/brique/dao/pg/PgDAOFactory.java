package fr.sie.brique.dao.pg;


import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;

import fr.sie.brique.dao.DAOFactory;
import fr.sie.brique.dao.LineDAO;
import fr.sie.brique.dao.PointDAO;
import fr.sie.brique.dao.PolygonDAO;


/**
 * Implémentation d'une Factory de Data Access Object (DAO) liée à PostgreSQL
 * 
 * @author mauclerc
 */
public class PgDAOFactory extends DAOFactory{

	static {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Datasource (managed by c3p0)
	 */
	private volatile static DataSource ds;
	
	/**
	 * Initialize the datasource
	 * @throws SQLException 
	 */
	private static synchronized DataSource initDataSource() throws SQLException {
		if (ds != null) {
			return ds;
		} else {
			try {
				Context env = (Context) new InitialContext().lookup("java:comp/env");
		    	DataSource temp = (DataSource)env.lookup("jdbc/PostGreDS");
		    	temp = DataSources.pooledDataSource(temp);
				return ds = temp;
			} catch (NamingException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * Get the datasource
	 * @throws SQLException 
	 */
	private static DataSource getDataSource() throws SQLException {
		if (ds != null) {
			return ds;
		} else {
			return initDataSource();
		}
	}
	
	/**
	 * Méthode d'accès à une connexion base de données à partir de la ressource
	 * JNDI.
	 * 
	 * @return Connexion à la base de données
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection conn = getDataSource().getConnection();
		return conn;
	}

	@Override
	public PointDAO getPointDAO() {
		return new PgPointDAO();
	}

	@Override
	public LineDAO getLineDAO() {
		return new PgLineDAO();
	}

	@Override
	public PolygonDAO getPolygonDAO() {
		return new PgPolygonDAO();
	}

}
