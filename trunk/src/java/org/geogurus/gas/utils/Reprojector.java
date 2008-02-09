/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import org.geogurus.Extent;
import org.geogurus.mapserver.objects.Projection;
import org.geogurus.tools.sql.ConPool;

/**
 *
 * @author gnguessan
 */
public class Reprojector {

    public static Extent returnBBox(Projection projRef, Hashtable hExtents) throws SQLException {
        Extent extent = null;
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String param, refParam, epsg, refEpsg;
        String strExtent;
        StringBuilder sb = new StringBuilder();
        Extent curEx;
        refParam = (String) projRef.getAttributes().get(0);
        refEpsg = refParam.substring(refParam.lastIndexOf(":") + 1, refParam.lastIndexOf("\""));

        for (Enumeration e = hExtents.keys(); e.hasMoreElements();) {
            Projection p = (Projection) e.nextElement();
            param = (String) p.getAttributes().get(0);
            epsg = param.substring(param.lastIndexOf(":") + 1, param.lastIndexOf("\""));

            curEx = (Extent) hExtents.get(p);
            strExtent = curEx.ll.x + " " + curEx.ll.y + "," + curEx.ur.x + " " + curEx.ur.y;
            if (p == projRef) {
                //Adds extent to map extent
                if (extent == null) {
                    extent = (Extent) hExtents.get(p);
                } else {
                    extent.add(curEx);
                }
            } else {
                //Adds query to sb
                if (sb.length() > 0) {
                    sb.append(" union ");
                }
                sb.append("select ");
                sb.append(" xmin(transform(setSRID('BOX(" + strExtent + ")'::box2d, " + epsg + ")," + refEpsg + ")) as xmin,");
                sb.append(" ymin(transform(setSRID('BOX(" + strExtent + ")'::box2d, " + epsg + ")," + refEpsg + ")) as ymin,");
                sb.append(" xmax(transform(setSRID('BOX(" + strExtent + ")'::box2d, " + epsg + ")," + refEpsg + ")) as xmax,");
                sb.append(" ymax(transform(setSRID('BOX(" + strExtent + ")'::box2d, " + epsg + ")," + refEpsg + ")) as ymax");

            }


        }

        try {
            //Builds query
            System.out.println(sb.toString());
            //Connects to GAS db
            con = ConPool.getConnection("127.0.0.1", "5432", "gas", "postgres", "c2c", ConPool.DBTYPE_POSTGRES);
            stmt = con.createStatement();



            rs = stmt.executeQuery(sb.toString());

            while (rs.next()) {
                extent.add(
                        rs.getDouble("xmin"),
                        rs.getDouble("ymin"),
                        rs.getDouble("xmax"),
                        rs.getDouble("ymax"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                con.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (rs != null) {
                rs.close();
            }
        }

        return extent;
    }
}
