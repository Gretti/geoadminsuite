package fr.sie.brique.dao.pg;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.sie.brique.dao.DAOUtils;
import fr.sie.brique.dao.PointDAO;
import fr.sie.brique.model.Point;

public class PgPointDAO implements PointDAO {

    private final static Log log = LogFactory
            .getLog(PgPointDAO.class);
    private final String tableName = "ouvrage"; // TODO : a changer
    private final String idName = "gid";

    public PgPointDAO() {
    }

    private Point getPointByReflection(ResultSet res) throws SQLException {
        if (res.next()) {
            final Point pt = new Point();
            return (Point) DAOUtils.getObjectByReflection(pt, res,
                    pt.getClass());
        }
        return null;
    }

    public Point getPointById(String id, Connection conn) throws SQLException {
        Point point = null;
        final String queryString = "SELECT * " + "FROM " + tableName + " "
                + "WHERE " + idName + " = '" + id + "'; ";
        final Statement stat = conn.createStatement();
        final ResultSet res = stat.executeQuery(queryString);
        if (res != null) {
            // Recupere le point
            point = getPointByReflection(res);
        }
        return point;
    }

    public Integer insert(Point point, Connection conn) throws SQLException {
        String queryString = "";
        //Integer max = DAOUtils.generateUniqueId(idName, tableName, conn);
        //point.setId_brique(max);
        queryString = DAOUtils.getSQLInsertIntoByReflection(tableName, point,
                point.getClass());

        log.info(queryString);

        Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
        if (rowCount == 1) {
            Integer max = DAOUtils.getId(idName, tableName, conn);
            return max;
        } else {
            return null;
        }
    }

    public Integer delete(Point point, Connection conn) throws SQLException {
        String queryString = "DELETE FROM " + tableName + " WHERE " + idName
                + " = '" + point.getGid() + "';";
        Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
        if (rowCount == 1) {
            return point.getGid();
        } else {
            return null;
        }
    }

    public Boolean update(Point point, Connection conn) throws SQLException {
        String queryString = "UPDATE "
                + tableName
                + " "
                + "SET "
                + DAOUtils.getSQLSetUpdateRequestString(point, point.getClass())
                + " " + "WHERE " + idName + " = " + point.getGid() + ";";

        Integer rowCount = DAOUtils.executeSQLUpdate(queryString, conn);
        if (rowCount == 1) {
            return true;
        } else {
            return false;
        }
    }
}
