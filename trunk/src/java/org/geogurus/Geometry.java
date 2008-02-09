package org.geogurus;

import java.util.Vector;
import java.io.*;
import uk.ac.leeds.ccg.shapefile.*;

import org.geogurus.tools.LogEngine;
/**
 * <p> Simple geometry object used to ease the storage and retrieval of geographic
 * object to and from a relationnal Database</p>
 * Fields are public for faster access (DB retrieval, shapefile writing)<br>
 * Polygon geometries are not geometrically closed: the last point is NOT equal
 * to the first. It is up to the shapeFileWriter to close the polygon, if needed.
 *
 * <br>Copyright (c) 2001 SCOT SA.
 * @author Nicolas Ribot
 * @version 1.0
 */

public class Geometry  implements java.io.Serializable {
    // Follow ESRI shapefile geometry types
    public final static int NULL     = -1;
    public final static int GEOMETRY  = 0;
    public final static int POINT    = 1;
    public final static int LINESTRING = 3;
    public final static int POLYGON  = 5;
    public final static int RASTER  = 6;
    public final static int MULTIPOLYGON  = 55;
    public final static int MULTIPOINT  = 11;
    public final static int MULTILINESTRING  = 33;
    public final static int GEOMETRYCOLLECTION  = 22;
    
    /** the unique ID for this geometry. No unicity control is performed:
     *  this ID may comes from a database, for instance
     */
    public String id;
    
    /** the geometry type. (See class constant values) */
    public int type;
    
    /** This <code>Geometry's Extent<code>*/
    public Extent extent;
    
    /** The GeometryClass name this geometry belongs to. See kaboum documentation */
    public String className;
    
    /** the wkt representation of this Geometry. Used both for database and kaboum*/
    protected String wkt;
    
    /** the db SRID for this Geometry */
    protected int srid;
    
    /** tells if this geometry is valid (coordinates and type to display or store it */
    protected boolean isValid;
    
    /** the computed area by kaboum. Only relevant if type is polygon */
    protected String area;
    
    /** the <code>Vector</code> of <code>Coord</code> composing this geometry */
    public Vector points;
    
    /** true if this geometry should be hilited in the client application */
    public boolean hilited;
    
    /** true if this geometry can be modified by the user */
    public boolean modifiable;
    
    /** true if this geometry is selected by default */
    public boolean selected;
    
    /** The ESRI shape representing this geometry, as defined by GeoTools library<br>
     * Ugly code to avoid cast operations: the <code>geometry</code> object keeps 3 Shapes, only one is
     * valid according to geometry type
     */
    protected ShapePoint shapept;
    /** The ESRI shape representing this geometry, as defined by GeoTools library<br>
     * Ugly code to avoid cast operations: the <code>geometry</code> object keeps 3 Shapes, only one is
     * valid according to geometry type
     */
    protected ShapePolygon shapepg;
    /** The ESRI shape representing this geometry, as defined by GeoTools library<br>
     * Ugly code to avoid cast operations: the <code>geometry</code> object keeps 3 Shapes, only one is
     * valid according to geometry type
     */
    protected ShapeArc shapepl;
    
    /** /ESRI shapefile specific).The parts for a geometry: only one for the moment
     *  <br>See GeoTools package for information about geometry parts*/
    int[] parts;
    
    /**
     *  Constructs a geometry of the specified type.
     */
    public Geometry(int type) {
        this.type = type;
        points = new Vector();
        parts = new int[1];
        parts[0]= 0;
        hilited = false;
        modifiable = false;
        selected = false;
        isValid = false;
        srid = -1;
    }
    /**
     *  Constructs a geometry with the specified id, type, extent, and GeometryClass name
     */
    public Geometry(String id, int type, Extent extent, String className) {
        this(type);
        this.id = id;
        this.extent = extent;
        this.className = className;
    }
    /**
     *  Constructs a geometry with the specified id, type, eng_id, extent, theme alias name,
     *  and Vector of coordinates
     */
    public Geometry(String id, int type, Extent extent, String className, Vector points) {
        this(id, type, extent, className);
        this.points = points;
    }
    
    /**
     *  Constructs a geometry with the specified id, WKT and class name
     */
    public Geometry(String id, int type, String wkt, String className) {
        this(type);
        this.id = id;
        this.wkt = wkt;
        this.className = className;
    }
    
    /**
     *  Constructs a geometry with the specified id, WKT and class name and extent
     */
    public Geometry(String id, int type, String wkt, String className, Extent ext) {
        this(id, type, wkt, className);
        this.extent = ext;
    }
    /**
     *  Constructs a geometry with the specified id, WKT and class name
     */
    public Geometry(String id, String wkt, String className) {
        this(0);
        this.id = id;
        this.wkt = wkt;
        this.className = className;
        this.type = guessType(wkt);
    }
    
    /**
     * overloaded to allow Vector.contains to work with this object
     * NOTE: this methods returns true if the 2 geometries belongs to the same
     * DB table AND their ids are the same: Geometry.id must be the unique record identifier
     * in the DB.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Geometry) {
            Geometry t = (Geometry)obj;
            
            if (this.className.equals(t.className) &&
                    this.id.equals(t.id)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Methods to test the geometry validity. Should be used before any insert
     * into the DB, or manipulation
     * Validity means:
     * id not null, tableName not null, WKT corresponding to the type
     */
    public boolean isValid() {
        if (id == null) { return false;}
        if (className == null) { return false; }
        
        switch (type) {
            case Geometry.NULL:
                return false;
            case Geometry.POINT:
                return this.wkt.indexOf("POINT") != -1;
            case Geometry.LINESTRING:
                return this.wkt.indexOf("LINESTRING") != -1;
            case Geometry.POLYGON:
                return this.wkt.indexOf("POLYGON") != -1;
            default:
                return false;
        }
    }
    
    // return the type based on the given WKT
    protected int guessType(String wkt) {
        if (wkt == null) return Geometry.NULL;
        
        if (wkt.indexOf("POINT") != -1) {
            return Geometry.POINT;
        }
        if (wkt.indexOf("LINESTRING") != -1) {
            return Geometry.LINESTRING;
        }
        if (wkt.indexOf("POLYGON") != -1) {
            return Geometry.POLYGON;
        }
        return Geometry.NULL;
    }
    
    /**
     * Returns the postgis canonical representation of this geometry.
     * Form is:SRID=<value>;<wkt>
     * where <value> is the SRID value (integer)
     * <wkt> is the wkt representation of the geometry
     *
     * Should test this Geometry's validity before using this method, to avoid
     * a "SIRD=-1;NULL string"
     */
    public String toPostgisString() {
        return "SRID=" + this.srid + ";" + this.wkt;
    }
    
    public void add(double x, double y) {
        add(new Coord(x, y));
    }
    
    /**
     * Adds the given Coord to this geometry. If type is POINT, replace the existing one
     * <br>Expands this <code>Extent</code>
     * <br>Maintain the ShapefileShape object valid: allocate a new array of points and copy the
     * previous values in it.
     * @param co the Coord to add to this geometry
     */
    public void add(Coord co) {
        if (type == Geometry.POINT) {
            points.insertElementAt(co, 0);
            shapept = new ShapePoint(co.x, co.y);
        } else {
            points.add(co);
            extent.add(co);
            
            if (type == Geometry.LINESTRING) {
                if (shapepl == null) {
                    ShapePoint[] pts = new ShapePoint[1];
                    pts[0] = new ShapePoint(co.x, co.y);
                    shapepl = new ShapeArc(extent.getExtentAsDouble(), parts, pts);
                } else {
                    ShapePoint[] pts = shapepl.getPoints();
                    ShapePoint[] pts2 = new ShapePoint[pts.length + 1];
                    System.arraycopy(pts, 0, pts2, 0, pts.length);
                    pts2[pts2.length-1] = new ShapePoint(co.x, co.y);
                    shapepl = new ShapeArc(extent.getExtentAsDouble(), parts, pts2);
                }
            } else if (type == Geometry.POLYGON) {
                if (shapepg == null) {
                    ShapePoint[] pts = new ShapePoint[1];
                    pts[0] = new ShapePoint(co.x, co.y);
                    shapepg = new ShapePolygon(extent.getExtentAsDouble(), parts, pts);
                } else {
                    ShapePoint[] pts = shapepg.getPoints();
                    ShapePoint[] pts2 = new ShapePoint[pts.length + 1];
                    System.arraycopy(pts, 0, pts2, 0, pts.length);
                    pts2[pts2.length-1] = new ShapePoint(co.x, co.y);
                    shapepg = new ShapePolygon(extent.getExtentAsDouble(), parts, pts2);
                }
            }
        }
    }
    
    /**
     * Returns the string representation of this geometry, as excepted by Kaboum
     * Java interface: this representation is:
     * <br>
     * OBJECT|<class>|<obj_id>|<coords>
     * where OBJECT is a reserved keyword
     *       <class> is the alias of the class whose object belongs to
     *       <obj_id> is the unique object identifier from the database
     *       <coord> is a list of coordinates separated by semi-column,
     *               each coordinate is separated by comma 1,2;3,4;5,6
     *
     * @return the string representation of this geometry
     */
    public String toKaboumString() {
        return toKaboumString("");
    }
    
    /**
     * Returns the string representation of this geometry to remove it from the applet, as excepted by Kaboum
     * Java interface: this representation is:
     * <br>
     * OBJECT|<class>|<obj_id>|<coords>
     * where OBJECT is a reserved keyword
     *       <class> is the alias of the class whose object belongs to
     *       <obj_id> is the unique object identifier from the database
     *       <coord> is a list of coordinates separated by semi-column,
     *               each coordinate is separated by comma 1,2;3,4;5,6
     *
     * @return the string representation of this geometry
     */
    public String toKaboumRemoveString() {
        return toKaboumRemoveString("");
    }
    
    /**
     * Returns the string representation of this geometry, as excepted by Kaboum
     * Java interface: this representation is:
     * <br>
     * OBJECT|<theme_alias>|<obj_id>|<coords>
     * where OBJECT is a reserved keyword
     *       <theme_alias> is the alias of the theme whose object belongs to
     *       <obj_id> is the unique object identifier from the database
     *       <coord> is a list of coordinates separated by semi-column,
     *               each coordinate is separated by comma 1,2;3,4;5,6
     *@param nameSuffix the suffix to append to the geometry's name
     *
     * @return the string representation of this geometry
     */
    public String toKaboumString(String nameSuffix) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("OBJECT|").append(className).append(nameSuffix).append("|").append(id).append("|");
        buf.append(this.wkt);
        
        return buf.toString();
    }
    
    /**
     * Returns the string representation of this geometry, as excepted by Kaboum
     * Java interface: this representation is:
     * <br>
     * OBJECT|<theme_alias>|<obj_id>|<coords>
     * where OBJECT is a reserved keyword
     *       <theme_alias> is the alias of the theme whose object belongs to
     *       <obj_id> is the unique object identifier from the database
     *       <coord> is a list of coordinates separated by semi-column,
     *               each coordinate is separated by comma 1,2;3,4;5,6
     *@param nameSuffix the suffix to append to the geometry's name
     *
     * @return the string representation of this geometry
     */
    public String toKaboum4String(String nameSuffix) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("GEOMETRY|").append(id).append("|");
        buf.append(this.wkt);
        
        return buf.toString();
    }
    
    /**
     * Returns the string representation of this geometry, as excepted by Kaboum, to REMOVE it from the applet
     * Java interface: this representation is:
     * <br>
     * OBJECT|<theme_alias>|<obj_id>|REMOVE
     * where OBJECT is a reserved keyword
     *       <theme_alias> is the alias of the theme whose object belongs to
     *       <obj_id> is the unique object identifier from the database
     *       REMOVE is a reserved keyword
     *@param nameSuffix the suffix to append to the geometry's name
     *
     * @return the string representation of this geometry
     */
    public String toKaboumRemoveString(String nameSuffix) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("OBJECT|").append(className).append(nameSuffix).append("|").append(id).append("|").append("REMOVE");
        
        return buf.toString();
    }
    
    /**
     * Return a <code>ShapeFileShape</code> object representing this <code>Geometry</code>
     * If this type is POINT, returns a ShapePoint
     * If this type is POLYLINE, returns a ShapeArc
     * If this type is POLYGON, returns a ShapePolygon
     * Returns null otherwise.
     */
    public ShapefileShape getShape() {
        switch (type) {
            case Geometry.POINT:
                return shapept;
            case Geometry.LINESTRING:
                return shapepl;
            case Geometry.POLYGON:
                // closes the polygon to make it a valid shape
                ShapePoint[] pts = shapepg.getPoints();
                ShapePoint[] pts2 = new ShapePoint[pts.length + 1];
                System.arraycopy(pts, 0, pts2, 0, pts.length);
                pts2[pts2.length-1] = pts[0];
                shapepg = new ShapePolygon(extent.getExtentAsDouble(), parts, pts2);
                
                return shapepg;
            default:
                return null;
        }
    }
    
    /**
     * Returns the WKT representation of a COllection if the type is collection and current WKT
     * is not: this may happen when editing objects in kaboum: a multipolygon is loaded as a polygon in
     * kaboum, then returned as a polygon
     */
    public String rebuildWkt() {
//System.out.println("rebuildWKT: original WKT: " + wkt);
        if (type != MULTILINESTRING && type != MULTIPOINT && type != MULTIPOLYGON) {
            return wkt;
        }
        if (type == MULTIPOLYGON && wkt.indexOf("MULTIPOLYGON") == -1) {
            // a polygon to rebuild
            int idx = wkt.indexOf("POLYGON") + "POLYGON".length();
            return "MULTIPOLYGON(" + wkt.substring(idx) + ")";
        }
        return wkt;
    }
    
    /////////////////////////////////////////////////
    // get set methods
    ////////////////////////////////////////////////
    /**
     *
     */
    public String getID() { return this.id; }
    public int getType() { return this.type;}
    public String getWKT() { return this.wkt; }
    
    /**
     *
     */
    public void setWKT(String wkt) {this.wkt = wkt;}
    public void setExtent(Extent e) { this.extent = e;}
}