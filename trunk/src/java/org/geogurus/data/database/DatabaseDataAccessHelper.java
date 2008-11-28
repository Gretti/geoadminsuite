package org.geogurus.data.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.geogurus.data.Pair;
import org.geogurus.data.ParameterizedCallable;
import org.geogurus.data.operations.UniqueValueFeatureClassification;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.tools.sql.ConPool2;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Utility methods that contains the code used by different database DataAccess
 * implementations
 * 
 * @author jesse
 */
public final class DatabaseDataAccessHelper {
	
	private DatabaseDataAccessHelper() {	}
	
	public static <T> T runOperation(String connectionURI, ParameterizedCallable<T, Connection> operation, Logger logger) {
        Connection con = null;
        T foundMetadata = null;
        try {
            con = ConPool2.getInstance().getConnection(connectionURI);
            if (con != null) {
                foundMetadata = operation.run(con);
            } else {
                logger.warning("Cannot get a connection (null) for: " + connectionURI);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
            }
        }
        return foundMetadata;
	}

    public static void performUniqueValueFeatureClassification(
            String connectionURI, String schemaName, String tableName,
            String columnName, UniqueValueFeatureClassification op) {
        ListClassesBean list = op.getList();
    
        ConPool2 conPool = ConPool2.getInstance();
        Connection con = conPool.getConnection(connectionURI);
        if (con == null) {
            list.setMessage("null connection");
            return;
        }
        try {
    
            Statement stmt = con.createStatement();
            // this query gives all classitem attribute distinct values where
            // geographic field is not null,
            // and construct new classes for each one
            // caution when db is not Postgis enabled
            String schema;
    
            if (schemaName == null || schemaName.length() == 0) {
                schema = "";
            } else {
                schema = schemaName + ".";
            }
    
            String query = "select distinct " + 
                    ConversionUtilities.quotes(op.getAttributeName()) +
                    " from " + 
                    ConversionUtilities.quotes(schemaName) + "." +
                    ConversionUtilities.quotes(tableName) + 
                    " where " + 
                    ConversionUtilities.quotes(columnName) + 
                    " is not null";
            ResultSet rs = stmt.executeQuery(query);
            Class cl = null;
            int count = 0;
    
            while (rs.next()) {
                if (count++ < op.getClassLimit()) {
                    cl = new Class();
                    cl.setColor(op.getColorGenerator().getNextColor());
                    cl.setOutlineColor(new RGB(0, 0, 0));
    
                    // Uses first class of defaultMsLayer of gc to assign to all
                    // other classes
                    cl.setSymbol(op.getSymName());
                    cl.setSize(op.getSymSize());
    
                    // getString is null if attribute is null in the table
                    String s = rs.getString(1);
                    s = (s == null) ? null : s.trim();
                    // trim expression and name: expression with leading or
                    // trailing spaces are not
                    // handled correctly by MapServer
                    cl.setName(s);
                    cl.setExpression(s);
                    list.addClass(cl);
                } else {
                    list.setMessage("classlimitation," + op.getClassLimit());
                    break;
                }
            }
            stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception sqle2) {
                sqle2.printStackTrace();
            }
        }
    }
    // ---------------------------------------------------------------//
    // I'm keeping the stuff below because it was in the GeometryClass
    // but was not used so commenting it out but keeping it around
    // ---------------------------------------------------------------//

    /**
     * looks for the min and max value of the given attribute in a gc of type
     * PGCLASS or ORACLASS
     */
    @SuppressWarnings("unchecked")
    public static Pair<Comparable<Object>, Comparable<Object>> getMinMaxFromDB(
            String connectionURI, String schemaName, String tableName, String columnName,
            String classitem) {
        Connection con = null;
        String query = null;
        Comparable<Object>[] res = new Comparable[2];
    
        // fetch min and max
        try {
            ConPool2 conPool = ConPool2.getInstance();
            con = conPool.getConnection(connectionURI);
            if (con == null) {
                return Pair.read(res[0], res[1]);
            }
            Statement stmt = con.createStatement();
            // this query gives all classitem attribute distinct values where
            // geographic field is not null,
            // and construct new classes for each one
            // caution when db is not Postgis enabled
            query = "select min(" + 
                    ConversionUtilities.quotes(classitem) +
                    "), max(" + 
                    ConversionUtilities.quotes(classitem) + 
                    ") from " +
                    ConversionUtilities.quotes(schemaName) + 
                    "." + 
                    ConversionUtilities.quotes(tableName) + 
                    " where " + 
                    ConversionUtilities.quotes(columnName) + 
                    " is not null";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                res[0] = (Comparable<Object>) rs.getObject(1);
                res[1] = (Comparable<Object>) rs.getObject(2);
            }
            stmt.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception sqle2) {
                sqle2.printStackTrace();
            }
        }
        return Pair.read(res[0], res[1]);
    }
}
