package org.geogurus.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geotools.data.Query;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Title:        geOnline Server classes
 * Description:  Set of Java classes to make the link between kaboum client and shape files / postgis DB containing geodata.
 * @author Nicolas Ribot
 * @version 1.0
 */
/**
 * Represents a Kaboum class of geometric objects: a logical set of objets
 * sharing some properties ( representation, for instance).
 * 
 * TODO: Change int static variables to byte.
 */
public abstract class DataAccess implements Serializable,
        Comparable<DataAccess> {

    private static final long serialVersionUID = 1L;
    /** General variables from System */
    public static final String ls = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    /** The default transparency level for the MS representation of this class */
    public static final int MS_LAYER_TRANSPARENCY = 50;
    /**
     * The unique identifier for this geometryClass. Used to store gc in
     * hashtables
     */
    protected String id;
    /**
     * The generic name of this class (for instance used as display name in a
     * web application
     */
    protected String name;
    protected String geometryAttributeName;
    /**
     * The name of the server = host hosting the geographic information
     * represented by this object
     */
    protected String host;
    /**
     * The spatial reference text the OpenGIS Spatial reference system string
     * for this postgis table, or an epsg string in case of shapefile. Format
     * for this string is: <name / subname> | <list of parameters> This string
     * is built from the EPSG proj4 reference file (provided with mapserver)
     */
    protected String SRText;
    /** The spatial reference identifier */
    protected int SRID;
    /**
     * The Vector of Geometry objects composing this class. Vector is used to
     * guarantee the order of geometries when retrieved from DB
     */
    protected Vector<Geometry> geometries;
    /**
     * The number of geometries not null in the table. Can be different from
     * geometries.size(); or might be available when geometries is null (no
     * geometries got from db)
     */
    protected int numGeometries;
    /** the vector of index of selected geometries after client play */
    protected Vector<Geometry> selectedGeometries;
    /** the extent for all geometries */
    protected Extent extent;
    /** the geometric type of this GeometryClass */
    protected int geomTypeCode;
    /**
     * the datasource name A database name if datasourceType is db, a folder
     * path otherwise
     */
    protected String datasourceName;
    /** the datasource type */
    protected DataAccessType datasourceType;
    /** the vector of fields attribute information */
    protected Vector<GeometryClassFieldBean> metadata = null;
    /** The Layer representing this GeometryClass */
    protected Layer msLayer;
    /**
     * tells if this GeometryClass should be displayed by kaboum: NOTE: a valid
     * Layer object must be created for this object
     */
    protected String errorMessage;
    /**
     * the Geometry identifier for the geometry to be created or modified in
     * Kaboum: Allows geometric creation/modification for an existing object in
     * DB
     */
    protected String editedGeometryID;
    protected transient Logger logger = null;
    protected final Datasource owner;

    /**
     * Creates a new instance of type DataAccess
     * 
     * @param name
     *            a human readable name describing the resource. Does not have
     *            to be technical in anyway
     * @param host
     * @param owner
     * @param type
     */
    public DataAccess(String name, Datasource owner, DataAccessType type) {
        this.id = "" + System.identityHashCode(this);
        this.name = name;
        // default type set to polygon
        this.geomTypeCode = Geometry.POLYGON;
        this.extent = new Extent();
        // default new object to Postgis class, as we want to support this type
        // mainly.
        this.datasourceType = type;
        this.owner = owner;

        logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Return the Kaboum keyword for a geometry type based on the DB values for
     * geometry types
     */
    public String getKaboumType() {
        switch (geomTypeCode) {
            case Geometry.POINT:
            case Geometry.MULTIPOINT:
                return "MS_LAYER_POINT";
            case Geometry.LINESTRING:
            case Geometry.MULTILINESTRING:
                return "MS_LAYER_POLYLINE";
            case Geometry.POLYGON:
            case Geometry.MULTIPOLYGON:
                return "MS_LAYER_POLYGON";
        }
        // the default value for majority of themes
        return "MS_LAYER_POLYLINE";
    }

    /**
     * Return the geometry type of this geometryClass
     */
    public int getGeoType() {
        geomTypeCode = msLayer.getType();
        if (geomTypeCode == 0 && geomTypeCode != Layer.NONE) {
            // type is not set by DB values, try to guess it from the
            // MapServerLayer object
            if (geomTypeCode == Layer.POINT) {
                return Geometry.POINT;
            }
            if ((geomTypeCode == Layer.POLYGON)
                    || (geomTypeCode == Layer.POLYLINE_POLYGON)) {
                return Geometry.POLYGON;
            }
            if ((geomTypeCode == Layer.POLYLINE)
                    || (geomTypeCode == Layer.LINE)) {
                return Geometry.LINESTRING;
            }
            return Geometry.NULL;
        }
        return geomTypeCode;
    }

    /**
     * public interface to make this object load the into metadata this object.
     * The following fields must be loaded if they apply to this type of
     * DataAccess: Extent, SRS, geomTypeCode,
     * metadata,numGeometries,geometryAttributeName
     */
    public abstract boolean loadMetadata();

    public Vector<Vector<Object>> getSampleData(int limit) {
        int from = 0;
        int to = from + limit;
        return getSampleData(from, to);
    }

    public abstract Vector<Vector<Object>> getSampleData(int from, int to);

    /**
     * Null param Layer constructor in case no info is given (easier for Struts)
     */
    public Layer getNullMsLayer() {
        return getMSLayer(null, false);
    }

    /**
     * Default param Layer constructor in case no info is given (easier for
     * Struts)
     */
    public Layer getDefaultMsLayer() {
        RGB rgb = new RGB(255, 255, 0);
        if(this.geomTypeCode == Geometry.POINT || this.geomTypeCode == Geometry.POINT){
            rgb = new RGB(255, 0, 0);
        }
        return getMSLayer(rgb, false);
    }

    public abstract ConnectionParams getConnectionParams();

    /**
     * build the MapServerLayer representation of this GeometryClass
     * 
     * @param: color: the RGB object for MS object color force true to indicate
     *         that the MSLayer should be reconstructed from scratch with the
     *         given color. Useful to build quickview when a Layer already have
     *         some display properties
     */
    public Layer getMSLayer(RGB color, boolean force) {

        if (msLayer != null && !force) {
            return msLayer;
        } else {
            Layer tmp = createMSLayerInner(color);
            if (msLayer == null) {
                msLayer = tmp;
            }
            return tmp;
        }
    }

    protected abstract Layer createMSLayerInner(RGB color);

    /**
     * Returns the MS layer object without reconstructing it
     * 
     * @return
     */
    public Layer getMSLayer() {
        if (msLayer == null) {
            getDefaultMsLayer();
        }
        return msLayer;
    }

    /**
     * Returns the geometry whose id equals the given one.
     * 
     * @param id
     *            the geometry's id to get
     * @return the geometry whose id is equals to the given one, null otherwise.
     */
    public Geometry getGeometry(String gid) {
        for (Geometry g : geometries) {
            if (g.id.equals(gid)) {
                return g;
            }
        }
        return null;
    }

    /**
     * gets this geo type as an OpenGIS String type, Ogis types are Geometry
     * class constants
     * 
     * @deprecated see {@link Geometry#toOgisType(int)}
     */
    public String getOgisType() {
        return Geometry.toOgisType(geomTypeCode);
    }

    /**
     * Sets this geo type according to the given OpenGIS String type, Ogis types
     * are Geometry class constants
     */
    public void setOgisType(String ogisType) {
        this.geomTypeCode = Geometry.fromOgisType(ogisType);
    }

    /**
     * Returns this datasource's type as a string (see class constants)
     * 
     * @return
     */
    public String getDatasourceTypeAsString() {

        return datasourceType.displayname();
    }

    /**
     * Returns true if this GeometryClass is eligible to be edited by Kaboum:
     * For the moment, only PGCLASS GC can be editable. the isEdited field tells
     * if an editable GC (in absolute) was specifically set editable by the user
     * (defautl is false)
     */
    public abstract boolean isEditable();

    /** MISSING JAVADOC ! */
    public void setName(String name) {
        this.name = name;
    }

    /** MISSING JAVADOC ! */
    public void setColumnName(String colname) {
        this.geometryAttributeName = colname;
    }

    /** MISSING JAVADOC ! */
    public void setDatasourceName(String dsname) {
        this.datasourceName = dsname;
    }

    /** MISSING JAVADOC ! */
    public void setType(int type) {
        this.geomTypeCode = type;
    }

    /** MISSING JAVADOC ! */
    public void setMSLayer(Layer l) {
        this.msLayer = l;
    }

    // /**
    // * Sets a new projection string for this GC, also updates the MSLayer
    // object
    // * to reflect the change.
    // */
    // public void setSRText(String srtext) {
    // this.SRText = srtext;
    // if (msLayer == null) {
    // getMSLayer(new RGB(255, 0, 0), true);
    // } else {
    // getMSLayer(((Class) (msLayer.getMapClass().getFirstClass()))
    // .getColor(), true);
    // }
    // }

    // --------------------------------------//
    // ----------- GETTER METHODS -----------//
    // --------------------------------------//
    /** MISSING JAVADOC ! */
    public String getName() {
        return this.name;
    }
    
    /*Needs to be escaped for Windows path*/
    public String getEscapedName() {
        return this.name.replace('\\', '/');
    }

    /** MISSING JAVADOC ! */
    public String getHost() {
        return this.host;
    }

    /** MISSING JAVADOC ! */
    public String getDatasourceName() {
        return owner.getName();
    }

    /**
     * @return the owner
     */
    public Datasource getOwner() {
        return owner;
    }

    /** MISSING JAVADOC ! */
    public String getSRText() {
        return this.SRText;
    }

    /** MISSING JAVADOC ! */
    public int getSrid() {
        return this.SRID;
    }

    /** MISSING JAVADOC ! */
    public Vector<Geometry> getSelectedGeometries() {
        return this.selectedGeometries;
    }

    /** MISSING JAVADOC ! */
    public int getType() {
        return this.geomTypeCode;
    }

    /** MISSING JAVADOC ! */
    public DataAccessType getDatasourceType() {
        return this.datasourceType;
    }

    /** MISSING JAVADOC ! */
    public Vector<Geometry> getGeometries() {
        return this.geometries;
    }

    /** MISSING JAVADOC ! */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /** MISSING JAVADOC ! */
    public String getEditedGeometryID() {
        return this.editedGeometryID;
    }

    /** MISSING JAVADOC ! */
    public Extent getExtent() {
        return this.extent;
    }

    /** MISSING JAVADOC ! */
    public String getID() {
        return this.id;
    }

    /** MISSING JAVADOC ! */
    public int getNumGeometries() {
        return numGeometries;
    }

    /** @deprecated see {@link #getAttributeData()} */
    public Vector<GeometryClassFieldBean> getColumnInfo() {
        return getAttributeData();
    }

    /** Returns the metdata describing this object's data */
    public Vector<GeometryClassFieldBean> getAttributeData() {
        if (metadata == null) {
            loadMetadata();
        }
        return metadata;
    }

    /**
     *Implements the Comparable::compareTo() method. the comparison is based on
     * tableName
     * 
     */
    public int compareTo(DataAccess other) {
        String currentValue = this.name;
        if (currentValue == null || "null".equalsIgnoreCase(currentValue)) {
            return -1;
        }
        String valueToCompareTo = other.name;
        if (valueToCompareTo == null
                || "null".equalsIgnoreCase(valueToCompareTo)) {
            return -1;
        }
        return currentValue.toLowerCase().compareTo(
                valueToCompareTo.toLowerCase());
    }

    /**
     * Returns the Database connection URI as a string, included user and
     * password ! Example:
     * <ul>
     * <li>Oracle: jdbc:oracle:thin:geo/geo@laptox:1521:orcl or</li>
     * <li>PostgreSQL:
     * jdbc:postgresql://localhost:5432/test?user=postgres&password=postgres</li>
     * </ul>
     * 
     * @return the connection URL
     */
    public abstract String getConnectionURI();

    /**
     * Returns a Some option with the FeatureType that describes the features
     * this DataAccess offer or a None option if this DataAccess does not
     * provide Features.
     * 
     * @return A Some option with the FeatureType that describes the features
     *         this DataAccess offer or a None option if this DataAccess does
     *         not provide Features.
     */
    public abstract Option<SimpleFeatureType> featureType();

    /**
     * This follows the GOF visitor pattern. All features that match the Query
     * will be passed to the FeatureOperation. For performance the
     * implementation can be optimized for certain classes. For example Database
     * DataAccess implementations can recognize the
     * DiscreteClassificationOperation class and can use a specialized query to
     * return distinct elements in the database rather than all features in the
     * table.
     * 
     * The Query is the Geotools query object which provides more functionality
     * than we will use perhaps. Right now all parameters are ignored except
     * Filter and Properties. Filter restricts the features that are passed to
     * the FeatureOperation and Properties restricts what attribute data is
     * passed to the Operation.
     * 
     * @param op
     *            the operation to run
     * @param context
     *            the object to pass to the operate method along with the first
     *            feature
     * @param query
     *            the query for filtering features to visit.
     * 
     * @return If this operation is not supported then the option will be a null
     *         option. Otherwise it will contain a true if the operation visited
     *         all features (Wasn't terminated prematurely by the operation
     *         returning false)
     * 
     * @throws IOException
     *             thrown if there is an error accessing the data
     */
    public final <T> Option<Boolean> run(Operation<SimpleFeature, T> op, T context,
            Query query) throws IOException {
        op.start(context);
        Option<Boolean> result = performOperateStep(op, context, query);
        if (result.isSome()) {
            op.end(context, result.get());
        }
        return result;
    }

    /**
     * Called by run to get the subclass to pass all the features to the
     * operation
     */
    protected abstract <T> Option<Boolean> performOperateStep(
            Operation<SimpleFeature, T> op, T context, Query query)
            throws IOException;

    /**
     * This will pass a java.awt.image.RenderedImage to the operation.
     * 
     * Do we need this? Probably will at some point. But maybe not right now.
     * 
     * @param op
     *            the operation to run
     * @param context
     *            the object to pass to the operate method along with the first
     *            feature
     * 
     * @return If this operation is not supported then the option will be a null
     *         option. Otherwise it will contain a true if the operation visited
     *         all features (Wasn't terminated prematurely by the operation
     *         returning false)
     * 
     * @throws IOException
     *             thrown if there is an error accessing the data
     */
    public final <T> Option<Boolean> run(
            Operation<java.awt.image.RenderedImage, T> op, T context) {
        op.start(context);
        Option<Boolean> result = peformOperateStep(op, context);
        if (result.isSome()) {
            op.end(context, result.get());
        }
        return result;

    }

    /**
     * Called by run to get the subclass to pass the operate method on the
     * operation
     */
    public abstract <T> Option<Boolean> peformOperateStep(
            Operation<java.awt.image.RenderedImage, T> op, T context);

    /**
     * Gets the list of columns for this GeometryClass
     * 
     * Note this is currently being called in the JSPs
     */
    public Vector<String> getAttributeDataNames() {
        Vector<String> columnNames = new Vector<String>();
        for (Iterator<GeometryClassFieldBean> iteInfo = getAttributeData()
                .iterator(); iteInfo.hasNext();) {
            columnNames.add(iteInfo.next().getName());
        }
        return columnNames;
    }

    /**
     * Gets the list of numeric-type columns for this GeometryClass Note this is
     * currently being called in the JSPs
     */
    public Vector<String> getNumericAttributeData() {
        Vector<String> columnNames = new Vector<String>();
        for (Iterator<GeometryClassFieldBean> iteInfo = getAttributeData()
                .iterator(); iteInfo.hasNext();) {
            GeometryClassFieldBean gcFb = iteInfo.next();

            if (gcFb.isNumeric()) {
                columnNames.add(gcFb.getName());
            }
        }
        return columnNames;
    }

    /**
     * This is the extendible interface pattern. Call this method with a Class
     * and if the class can create this object it will return it if not the Null
     * option will be returned.
     * 
     * @param <T>
     *            the type of object to attempt to create
     * @param request
     *            the type of object to attempt to create
     * @return Null Option if the object cannot be created otherwise an option
     *         with the object
     * @throws IOException
     */
    public abstract <T> Option<T> resource(java.lang.Class<T> request);
}
