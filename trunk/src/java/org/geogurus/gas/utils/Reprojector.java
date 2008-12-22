/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geogurus.data.Extent;
import org.geogurus.mapserver.objects.Projection;
import org.geogurus.tools.sql.ConPool2;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author gnguessan
 * Utils for reprojecting data
 */
public class Reprojector {

    /**
     * Reproject given extents from original projection to destination projection using postgis specified database
     * @param projRef
     * @param hExtents : Hashtable<Projection,Extent>
     * @param host
     * @param port
     * @param db
     * @param user
     * @param pwd
     * @return Extent calculated from given data
     */
    public static Extent returnBBox(Projection projRef, Hashtable hExtents, String host, String port, String db, String user, String pwd) {
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
            //Connects to GAS db
            ConPool2 conPool = ConPool2.getInstance();
            con = conPool.getConnection(
                    conPool.getConnectionURI(host, port, db, user, pwd, ConPool2.DBTYPE_POSTGRES));
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
            Logger.getLogger(Reprojector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
            return extent;
        }

    }

    /**
     * Reproject given extents from original projection to destination projection using geotools
     * @param projRef
     * @param hExtents : Hashtable<Projection,Extent>
     * @return Extent calculated from given data
     */
    public static Extent returnBBox(Projection projRef, Projection projOri, Extent extent) {
        Hashtable<Projection, Extent> hExtents = new Hashtable<Projection, Extent>(1);
        hExtents.put(projOri, extent);
        return returnBBox(projRef, hExtents);
    }

    /**
     * Reproject given extents from original projection to destination projection using geotools
     * @param projRef
     * @param hExtents : Hashtable<Projection,Extent>
     * @return Extent calculated from given data
     */
    public static Extent returnBBox(Projection projRef, Hashtable hExtents) {
        Extent extent = null;
        String refParam = (String) projRef.getAttributes().get(0);
        String refEpsg = refParam.substring(refParam.lastIndexOf(":") + 1, refParam.lastIndexOf("\""));
        CoordinateOperationFactory coFactory = ReferencingFactoryFinder.getCoordinateOperationFactory(null);
        CoordinateReferenceSystem crsSrc;
        CoordinateReferenceSystem crsDest = null;
        CoordinateOperation op;
        Extent curEx;
        try {
            if (!refEpsg.equals("900913")) {
                //System.setProperty("org.geotools.referencing.forceXY", "true");
                crsDest = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(refEpsg);
            } else {
                try {
                    crsDest = CRS.parseWKT("PROJCS[\"Google Mercator\",GEOGCS[\"WGS 84\",DATUM[\"World Geodetic System 1984\",SPHEROID[\"WGS 84\",6378137.0,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0.0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.017453292519943295],AXIS[\"Geodetic latitude\",NORTH],AXIS[\"Geodetic longitude\",EAST],AUTHORITY[\"EPSG\",\"4326\"]],PROJECTION[\"Mercator_1SP\"],PARAMETER[\"semi_minor\",6378137.0],PARAMETER[\"latitude_of_origin\",0.0],PARAMETER[\"central_meridian\",0.0],PARAMETER[\"scale_factor\",1.0],PARAMETER[\"false_easting\",0.0],PARAMETER[\"false_northing\",0.0],UNIT[\"m\",1.0],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"900913\"]],EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext  +no_defs\"]]");
                } catch (FactoryException ex) {
                    Logger.getLogger(Reprojector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            String param;
            String epsg;

            for (Enumeration e = hExtents.keys(); e.hasMoreElements();) {
                Projection p = (Projection) e.nextElement();
                param = (String) p.getAttributes().get(0);
                epsg = param.substring(param.lastIndexOf(":") + 1, param.lastIndexOf("\""));

                curEx = (Extent) hExtents.get(p);
                if (p == projRef) {
                    //Adds extent to map extent
                    if (extent == null) {
                        extent = (Extent) hExtents.get(p);
                    } else {
                        extent.add(curEx);
                    }
                } else {
                    //calculates reprojected extent before adding to map extent
                    crsSrc = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createCoordinateReferenceSystem(epsg);
                    op = coFactory.createOperation(crsSrc, crsDest);
                    MathTransform trans = op.getMathTransform();

                    // transform given coordinatespoints
                    DirectPosition ll;
                    DirectPosition ur;
                    //TODO: Must be fixed when geotools reprojection wont inverse axis
                    double xmin = curEx.ll.x;
                    double ymin = curEx.ll.y;
                    double xmax = curEx.ur.x;
                    double ymax = curEx.ur.y;
                    if (refEpsg.equals("900913") && epsg.equals("4326")) {
                        if (curEx.ll.x > -90 && curEx.ur.x < 90) {
                            xmin = curEx.ll.y;
                            ymin = curEx.ll.x;
                            xmax = curEx.ur.y;
                            ymax = curEx.ur.x;
                        } else {
                            xmin = curEx.ll.x == -180 ? -179.99999 : curEx.ll.x;
                            ymin = curEx.ll.y == -90 ? -89.99999 : curEx.ll.y;
                            xmax = curEx.ur.x == 180 ? 179.99999 : curEx.ur.x;
                            ymax = curEx.ur.y == 90 ? 89.99999 : curEx.ur.y;
                        }
                    }
                    ll = new GeneralDirectPosition(xmin, ymin);
                    ur = new GeneralDirectPosition(xmax, ymax);

                    ll = trans.transform(ll, null);
                    ur = trans.transform(ur, null);

                    if (extent == null) {
                        extent = new Extent(ll.getOrdinate(0), ll.getOrdinate(1), ur.getOrdinate(0), ur.getOrdinate(1));
                    } else {
                        extent.add(ll.getOrdinate(0), ll.getOrdinate(1), ur.getOrdinate(0), ur.getOrdinate(1));
                    }
                }
            }

        } catch (MismatchedDimensionException ex) {
            Logger.getLogger(Reprojector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformException ex) {
            Logger.getLogger(Reprojector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(Reprojector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return extent;
    }
}
