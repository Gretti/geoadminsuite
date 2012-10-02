package fr.sie.brique.dao.pg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.sie.brique.dao.DAOUtils;
import fr.sie.brique.dao.LineDAO;
import fr.sie.brique.model.Line;

public class PgLineDAO implements LineDAO {
	private final static Log log = LogFactory
	.getLog(PgLineDAO.class);
	
	private final String tableName = "geosie.brique_ligne"; // TODO : a changer
	private final String idName = "id_brique";

	public PgLineDAO() {
	}

	private Line getLineByReflection(ResultSet res) throws SQLException {
		if (res.next()) {
			final Line line = new Line();
			return (Line) DAOUtils.getObjectByReflection(line, res,
					line.getClass());
		}
		return null;
	}

	public Line getLineById(String id, Connection conn) throws SQLException {
		Line line = null;
		final String queryString = "SELECT * " + "FROM " + tableName + " "
				+ "WHERE " + idName + " = '" + id + "'; ";
		final Statement stat = conn.createStatement();
		final ResultSet res = stat.executeQuery(queryString);
		if (res != null) {
			// Recupere le point
			line = getLineByReflection(res);
		}
		return line;
	}

	public Integer insert(Line line, Connection conn) throws SQLException {
		String queryString = "";
		//Integer max = DAOUtils.generateUniqueId(idName, tableName, conn);
		//line.setId_brique(max);
		queryString = DAOUtils.getSQLInsertIntoByReflection(tableName, line,
				line.getClass());
		
		log.info(queryString);
		
		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			Integer max = DAOUtils.getId(idName, tableName, conn);
			return max;
		} else {
			return null;
		}
	}

	public Integer delete(Line line, Connection conn) throws SQLException {
		String queryString = "DELETE FROM " + tableName + " WHERE " + idName
				+ " = '" + line.getId_brique() + "';";
		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			return line.getId_brique();
		} else {
			return null;
		}
	}

	public Boolean update(Line line, Connection conn) throws SQLException {
		String queryString = "UPDATE "
				+ tableName
				+ " "
				+ "SET "
				+ DAOUtils
						.getSQLSetUpdateRequestString(line, line.getClass())
				+ " " + "WHERE " + idName + " = '" + line.getId_brique() + "';";

		Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
		if (rowCount == 1) {
			return true;
		} else {
			return false;
		}
	}
}
