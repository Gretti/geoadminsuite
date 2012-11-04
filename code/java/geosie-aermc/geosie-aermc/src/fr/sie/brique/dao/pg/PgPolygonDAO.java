package fr.sie.brique.dao.pg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.sie.brique.dao.DAOUtils;
import fr.sie.brique.dao.PolygonDAO;
import fr.sie.brique.model.Polygon;

public class PgPolygonDAO implements PolygonDAO {
	private final static Log log = LogFactory
	.getLog(PgPolygonDAO.class);
	
	private final String tableName = "geosie.brique_polygone"; // TODO : a changer
	private final String idName = "id_brique";

	public PgPolygonDAO() {
	}

	private Polygon getPolygonByReflection(ResultSet res) throws SQLException {
		if (res.next()) {
			final Polygon polygon = new Polygon();
			return (Polygon) DAOUtils.getObjectByReflection(polygon, res,
					polygon.getClass());
		}
		return null;
	}

	public Polygon getPolygonById(String id, Connection conn) throws SQLException {
		Polygon poly = null;
		final String queryString = "SELECT * " + "FROM " + tableName + " "
				+ "WHERE " + idName + " = '" + id + "'; ";
		final Statement stat = conn.createStatement();
		final ResultSet res = stat.executeQuery(queryString);
		if (res != null) {
			// Recupere le point
			poly = getPolygonByReflection(res);
		}
		return poly;
	}

	public Integer insert(Polygon poly, Connection conn) throws SQLException {
		String queryString = "";
		//Integer max = DAOUtils.generateUniqueId(idName, tableName, conn);
		//poly.setId_brique(max);
		queryString = DAOUtils.getSQLInsertIntoByReflection(tableName, poly,
				poly.getClass());
		
		log.info(queryString);
		
		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			Integer max = DAOUtils.getId(idName, tableName, conn);

			return max;
		} else {
			return null;
		}
	}

	public Integer delete(Polygon poly, Connection conn) throws SQLException {
		String queryString = "DELETE FROM " + tableName + " WHERE " + idName
				+ " = '" + poly.getId_brique() + "';";
		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			return poly.getId_brique();
		} else {
			return null;
		}
	}

	public Boolean update(Polygon poly, Connection conn) throws SQLException {
		String queryString = "UPDATE "
				+ tableName
				+ " "
				+ "SET "
				+ DAOUtils
						.getSQLSetUpdateRequestString(poly, poly.getClass())
				+ " " + "WHERE " + idName + " = '" + poly.getId_brique() + "';";
		log.info(queryString);

		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			return true;
		} else {
			return false;
		}
	}
}
