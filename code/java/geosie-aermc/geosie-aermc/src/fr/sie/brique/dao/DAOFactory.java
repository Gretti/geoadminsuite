package fr.sie.brique.dao;

import java.sql.Connection;
import java.sql.SQLException;

import fr.sie.brique.dao.pg.PgDAOFactory;


/**
 * Classe abstraite de Factory de Data Access Object (DAO)
 * 
 * @author mauclerc
 *
 */
public abstract class DAOFactory {

	// List of DAO types supported by the factory
	public static final int POSTGRES = 1;
	
	public abstract Connection getConnection() throws SQLException;
	
	public abstract PointDAO getPointDAO();
	public abstract LineDAO getLineDAO();
	public abstract PolygonDAO getPolygonDAO();
	
	public static DAOFactory getDAOFactory(int whichFactory) {
		switch (whichFactory) {
		case POSTGRES: 
			return new PgDAOFactory();
		default           : 
			return null;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
