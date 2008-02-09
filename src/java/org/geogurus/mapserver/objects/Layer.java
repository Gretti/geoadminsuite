/*
 * Layer.java
 *
 * Created on 20 mars 2002, 10:38
 */

package org.geogurus.mapserver.objects;
import java.io.File;
import java.io.BufferedReader;
import org.geogurus.gas.objects.ListClassesBean;
import org.geogurus.tools.string.ConversionUtilities;
import java.util.ArrayList;

/**
 * The most used object in MapFile, this one describes layers used to make up a map.
 * Layers are drawn in their order of appearence in the MapFile (First layer is
 * at the bottom, last is on the top).
 *
 * @author  Bastien VIALADE
 */

public class Layer extends MapServerObject implements java.io.Serializable {
    // Constants for connections type
    public static final byte LOCAL      = 0;
    public static final byte SDE= 1;
    public static final byte OGR= 2;
    public static final byte POSTGIS    = 3;
    public static final byte ORACLESPATIAL    = 4;
    // Constants for labelCache state and status
    public static final byte ON = 0;
    public static final byte OFF= 1;
    public static final byte DEFAULT    = 2;
    // Constants defines size and tolerance units
    public static final byte PIXELS      = 0;
    public static final byte FEET       = 1;
    public static final byte INCHES     = 2;
    public static final byte KILOMETERS = 3;
    public static final byte METERS     = 4;
    public static final byte MILES      = 5;
    // only for tolerance unit
    public static final byte DD = 6;
    // Constants for type
    public static final byte NONE       = -1;
    public static final byte POINT      = 0;
    public static final byte LINE       = 1;
    public static final byte POLYLINE   = 2;
    public static final byte POLYGON    = 3;
    public static final byte ANNOTATION = 4;
    public static final byte RASTER     = 5;
    public static final byte QUERYONLY  = 6;
    public static final byte POLYLINE_POLYGON = 7;
    public static final byte CIRCLE = 8;
    public static final byte QUERY = 9;
    
    /** Signal the start of a class object */
    private ListClassesBean classes;
    
    /** Item name in attribute table to use for class lookups */
    private String classItem;
    /** Database connecting string to retrieve remote data
     * An SDE connection string consists of a hostname, instance name, database name, username and password separated by commas.
     * A PostGIS connection string is basically a regular PostgreSQL connection string,
     * it takes the form of "user=nobody password=****** dbname=dbname host=localhost port=5432"
     * An Oracle connection string: user/pass[@db] */
    private String connection;
    /** Type of connection */
    private byte connectionType;
    /** Full filename of the spatial data to process.
     * No file extension is necessary for shapefiles.
     * Can be specified relative to the SHAPEPATH option from the Map Object.
     * If this is an SDE layer, the parameter should include the name of the layer
     * as well as the geometry column, i.e. "mylayer,shape".
     * If this is a PostGIS layer, the parameter should be in the form
     * of "<columnname> from <tablename>", where "columnname" is the name of the column
     * containing the geometry objects and "tablename" is the name of the table from which the geometry data will be read.
     * For Oracle, use "shape FROM table" or "shape FROM (SELECT statement)"
     * or even more complex Oracle compliant queries!
     * Note that there are important performance impacts when using spatial subqueries however.
     * Try using MapServer's FILTER whenever possible instead.
     * You can also see the SQL submitted by forcing an error, for instance
     * by submitting a DATA parameter you know won't work, using for example a bad column name.
     */
    private String data;
    /** Signals the start of a FEATURE object. */
    private Feature feature;
    /** This parameter allows for data specific attribute filtering
     * that is done at the same time spatial filtering is done,
     * but before any CLASS expressions are evaluated.
     * For OGR and shapefiles the string is simply a mapserver regular expression.
     * For spatial databases the string is a SQL WHERE clause
     * that is valid with respect to the underlying database.
     * For example: FILTER "type='road' and size <2" */
    private String filter;
    /** Item to use with simple FILTER expressions. OGR and shapefiles only. */
    private String filterItem;
    /** Template to use after a layer's set of results have been sent.
     * Multiresult query modes only. */
    private File footer;
    /** Name of a group that this layer belongs to.
     * The group name can then be reference as a regular layer name in the template files,
     * allowing to do things like turning on and off a group of layers at once. */
    private String group;
    /** Template to use before a layer's set of results have been sent.
     * Multiresult query modes only. */
    private File header;
    /** Item name in attribute table to use for class annotation angles.
     * Values should be in degrees. */
    private String labelAngleItem;
    /** Specifies whether labels should be drawn as the features
     * for this layer are drawn, or whether they should be cached and drawn
     * after all layers have been drawn.
     * Default is on.
     * Label overlap removal, auto placement etc... are only available when the label cache is active. */
    private byte labelCache;
    /** Item name in attribute table to use for class annotation (i.e. labeling).*/
    private String labelItem;
    /** Maximum scale at which the layer is labeled. */
    private double labelMaxScale;
    /** Minimum scale at which the layer is labeled. */
    private double labelMinScale;
    /** Sets context for labeling this layer, for example:
     * LABELREQUIRES ([orthoquads] != 1)
     * means that this layer would NOT be labeled if a layer named "orthoquads" is on.
     * The expression consists of a boolean expression based on the status
     * of other layers, each [layer name] substring is replaced by a 0 or a 1
     * depending on that layers STATUS and then evaluated as normal.
     * Logical operators AND and OR can be used. */
    private String labelRequires;
    /** Item name in attribute table to use for class annotation sizes.
     * Values should be in pixels. */
    private String labelSizeItem;
    /** Specifies the number of features that should be drawn for this layer
     * in the CURRENT window.
     * Has some interesting uses with annotation and with sorted data
     *(i.e. lakes by area). */
    private int maxFeatures;
    /** Maximum scale at which this layer is drawn.*/
    private double maxScale;
    /** This keyword allows for arbitrary data to be stored as name value pairs.
     * This is used with OGC WMS to define things such as layer title.
     * It can also allow more flexibility in creating templates,
     * as anything you put in here will be accessible via template tags.
     * Example:
     * METADATA
     * 	title "My layer title"
     * author "Me!"
     * END */
    private MetaData metaData;
    /** Minimum scale at which this layer is drawn */
    private double minScale;
    /** Short name for this layer.
     * Limit is 20 characters.
     * This name is the link between the mapfile and web interfaces
     * that refer to this name. They must be identical.
     * The name should be unique, unless one layer replaces another at different scales.
     * Use the GROUP option to associate layers with each other. */
    private String name;
    /** Sets the color index to treat as transparent for raster layers. */
    private int offSite;
    /** Tells MapServer to render this layer after all labels in the cache have been drawn.
     * Useful for adding neatlines and similar elements. */
    private boolean postLabelCache;
    /** Signals the start of a PROJECTION object.*/
    private Projection projection;
    /** Sets context for displaying this layer */
    private String requires;
    /** Sets the unit of CLASS object SIZE values (default is pixels).
     * Usefull for simulating buffering. */
    private byte sizeUnits;
    /** Sets the current status of the layer.
     * Often modified by MapServer itself.
     * Default turns the layer on permanently. */
    private byte status;
    /** Item to use for feature specific styling.
     * This is *very* experimental and OGR only at the moment. */
    private String styleItem;
    /** The scale at which symbols and/or text appear full size.
     * This allows for dynamic scaling of objects based on the scale of the map.
     * If not set then this layer will always appear at the same size.
     * Scaling only takes place within the limits of MINSIZE and MAXSIZE as described above. */
    private double symbolScale;
    /** Used as a global alternative to CLASS TEMPLATE */
    private String template;
    /** Full filename for the index or tile definition for this layer.
     * Similar to an ArcInfo library index, this shapefile contains polygon
     * features for each tile.
     * The item that contains the location of the tiled data is given
     * using the TILEITEM parameter.
     * If the DATA parameter contains a value then it is added to the end of the location.
     * If DATA is empty then the location should contain the entire filename. */
    private File tileIndex;
    /** Item that contains the location of an individual tile, default is "location". */
    private String tileItem;
    /** Sensitivity for point based queries (i.e. via mouse and/or map coordinates).
     * Given in TOLERANCEUNITS with a default of 3 pixels.
     * To restrict polygon searches so that the point must occur in the polygon
     * set the tolerance to zero. */
    private double tolerance;
    /** Units of the TOLERANCE value. Default is pixels. */
    private byte toleranceUnits;
    /** Tells MapServer whether or not a particular layer needs to be transformed
     * from some coordinate system to image coordinates.
     * Default is true.
     * This allows you to create shapefiles in image/graphics coordinates
     * and therefore have features that will always be displayed in the same location
     * on every map.
     * Ideal for placing logos or text in maps.
     * Remember that the graphics coordinate system has an origin
     * in the upper left hand corner of the image, contrary to most map coordinate systems. */
    private boolean transform;
    /**
     * The non-documented transparency feature in MapServer 3.6. 0 = tranparent,
     * 100 = opaque
     */
    private int transparency;
    /** Specifies how the data should be drawn.
     * Need not be the same as the shapefile type.
     * For example, a polygon shapefile may be drawn as a point layer,
     * but a point shapefile may not be drawn as a polygon layer.
     * Common sense rules.
     * Annotation means that a label point will be calculated for the features,
     * but the feature itself will not be drawn
     * although a marker symbol can be optionally drawn.
     * This allows for advanced labeling like numbered highway shields.
     * Points are labeled at that point.
     * Polygons are labeled first using a centroid, and if that doesn't fall in the polygon
     * a scanline approach is used to guarantee the label falls within the feature.
     * Lines are labeled at the middle of the longest arc in the visible portion of the line.
     * Query only means the layer can be queried but not drawn.
     *
     * In order to differentiate between POLYGONs and POLYLINEs (which do not exist as a type),
     * simply respectively use or ommit the COLOR keyword when classifying.
     * If you use it, it's a polygon with a fill color,
     * otherwise it's a polyline with only an OUTLINECOLOR.
     *
     * A circle must be defined by a a minimum bounding rectangle.
     * That is, 2 points that define the smallest square that can contain it.
     * These 2 points are the two opposite corners of said box. */
    private byte type;

    
    /** Empty constructor */
    public Layer() {
        this(null, (byte)0, null, null, 0, 0, 0, 0, 0, null, null, 0, null,100);
    }
    
    
    
    
    /** Creates a new instance of Layer */
    public Layer(String connection_, byte connectionType_,
    String data_, Feature feature_, double labelMaxScale_, double labelMinScale_,
    int maxFeatures_, double maxScale_, double minScale_, String name_,
    Projection projection_, double symbolScale_, File tileIndex_, int transparency_) {
        classes = null;
        feature = null;
        classItem = null;
        connection = connection_;
        connectionType = connectionType_;
        data = data_;
        feature = feature_;
        filter = null;
        filterItem = null;
        footer = null;
        group = null;
        header = null;
        labelAngleItem = null;
        labelCache = this.ON;
        labelItem = null;
        labelMaxScale = labelMaxScale_;
        labelMinScale = labelMinScale_;
        labelRequires = null;
        labelSizeItem = null;
        maxFeatures = maxFeatures_;
        maxScale = maxScale_;
        metaData = null;
        minScale = minScale_;
        name = name_;
        offSite = -1;
        postLabelCache = false;
        projection = projection_;
        requires = null;
        sizeUnits = this.PIXELS;
        status = this.DEFAULT;
        styleItem = null;
        symbolScale = symbolScale_;
        template = null;
        tileIndex = tileIndex_;
        tileItem = null;
        tolerance = 3;
        toleranceUnits = this.PIXELS;
        transform = true;
        type = NONE;
        transparency = transparency_;
    }
    
    // Get and set methods
    public String getClassItem()            {return classItem ;}
    public String getConnection()           {return connection;}
    public byte getConnectionType()         {return connectionType;}
    public String getData()                 {return data;}
    public Feature getFeature()             {return feature;}
    public String getFilter()               {return filter;}
    public String getFilterItem()           {return filterItem;}
    public File getFooter()                 {return footer;}
    public String getGroup()                {return group;}
    public File getHeader()                 {return header;}
    public String getLabelAngleItem()       {return labelAngleItem;}
    public byte getLabelCache()             {return labelCache;}
    public String getLabelItem()            {return labelItem;}
    public double getLabelMaxScale()        {return labelMaxScale ;}
    public double getLabelMinScale()        {return labelMinScale ;}
    public String getLabelRequires()        {return labelRequires;}
    public String getLabelSizeItem()        {return labelSizeItem;}
    public ListClassesBean getMapClass()    {return classes ;}
    public int getMaxFeatures()             {return maxFeatures ;}
    public double getMaxScale()             {return maxScale ;}
    public MetaData getMetaData()           {return metaData;}
    public double getMinScale()             {return minScale;}
    public String getName()                 {return name;}
    public int getOffSite()                 {return offSite;}
    public boolean isPostLabelCache()       {return postLabelCache;}
    public Projection getProjection()       {return projection;}
    public String getRequires()             {return requires;}
    public byte getSizeUnits()              {return sizeUnits;}
    public byte getStatus()                 {return status;}
    public String getStyleItem()            {return styleItem;}
    public double getSymbolScale()          {return symbolScale;}
    public String getTemplate()             {return template;}
    public File getTileIndex()              {return tileIndex;}
    public String getTileItem()             {return tileItem;}
    public double getTolerance()            {return tolerance;}
    public byte getToleranceUnit()          {return toleranceUnits;}
    public boolean isTransform()            {return transform;}
    public byte getType()                   {return type;}
    public int getTransparency()            {return transparency;}
    
    public void setClassItem(String classItem_)             {classItem = classItem_ ;}
    public void setConnection(String connection_)           {connection = connection_;}
    public void setConnectionType(byte connectionType_)     {connectionType = connectionType_;}
    public void setData(String data_)                       {data = data_;}
    public void setFeature(Feature feature_)                {feature = feature_;}
    public void setFilter(String filter_)                   {filter = filter_;}
    public void setFilterItem(String filterItem_)           {filterItem = filterItem_;}
    public void setFooter(File footer_)                     {footer = footer_;}
    public void setGroup(String group_)                     {group = group_;}
    public void setHeader(File header_)                     {header = header_;}
    public void setLabelAngleItem(String labelAngleItem_)   {labelAngleItem = labelAngleItem_;}
    public void setLabelCache(byte labelCache_)             {labelCache = labelCache_;}
    public void setLabelItem(String labelItem_)             {labelItem = labelItem_;}
    public void setLabelMaxScale(double labelMaxScale_)     {labelMaxScale = labelMaxScale_ ;}
    public void setLabelMinScale(double labelMinScale_)     {labelMinScale = labelMinScale_ ;}
    public void setLabelRequires(String labelRequires_)     {labelRequires = labelRequires_;}
    public void setLabelSizeItem(String labelSizeItem_)     {labelSizeItem = labelSizeItem_;}
    public void setMapClass(ListClassesBean classes_)       {classes = classes_ ;}
    public void setMaxFeatures(int maxFeatures_)            {maxFeatures = maxFeatures_ ;}
    public void setMaxScale(double maxScale_)               {maxScale = maxScale_ ;}
    public void setMetaData(MetaData metaData_)             {metaData = metaData_;}
    public void setMinScale(double minScale_)               {minScale = minScale_;}
    public void setName(String name_)                       {name = name_;}
    public void setOffSite(int offSite_)                    {offSite = offSite_;}
    public void setPostLabelCache(boolean postLabelCache_)  {postLabelCache = postLabelCache_;}
    public void setProjection(Projection projection_)       {projection = projection_;}
    public void setRequires(String requires_)               {requires = requires_;}
    public void setSizeUnits(byte sizeUnits_)               {sizeUnits = sizeUnits_;}
    public void setStatus(byte status_)                     {status = status_;}
    public void setStyleItem(String styleItem_)             {styleItem = styleItem_;}
    public void setSymbolScale(double symbolScale_)         {symbolScale = symbolScale_;}
    public void setTemplate(String template_)               {template = template_;}
    public void setTileIndex(File tileIndex_)               {tileIndex = tileIndex_;}
    public void setTileItem(String tileItem_)               {tileItem = tileItem_;}
    public void setTolerance(double tolerance_)             {tolerance = tolerance_;}
    public void setToleranceUnit(byte toleranceUnits_)      {toleranceUnits = toleranceUnits_;}
    public void setTransform(boolean transform_)            {transform = transform_;}
    public void setType(byte type_)                         {type = type_;}
    public void setTransparency(int transparency_)          {transparency = transparency_;}
    public void addClass(MapClass mapClass)                 {
        if (classes==null) classes = new ListClassesBean();
        classes.addClass(mapClass);
    }
    
    
    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public synchronized boolean load(BufferedReader br) {
        boolean result = true;
        
        boolean isName = false;
        boolean isData = false;
        
        try {
            String[] tokens;
            String line;
            
            while ((line = br.readLine()) != null) {
                
                // Looking for the first util line
                while ((line.trim().length()==0)||(line.trim().startsWith("#"))||(line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("CLASS")) {
                    MapClass mapClass = new MapClass();
                    result = mapClass.load(br);
                    addClass(mapClass);
                }
                else if (tokens[0].equalsIgnoreCase("CLASSITEM")) {
                    if (tokens.length<2) return false;
                    classItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("CONNECTION")) {
                    if (tokens.length<2) return false;
                    connection = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("CONNECTIONTYPE")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("LOCAL")) connectionType = this.LOCAL;
                    else if (tokens[1].equalsIgnoreCase("SDE")) connectionType = this.SDE;
                    else if (tokens[1].equalsIgnoreCase("OGR")) connectionType = this.OGR;
                    else if (tokens[1].equalsIgnoreCase("POSTGIS")) connectionType = this.POSTGIS;
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("DATA")) {
                    if (tokens.length < 2) return false;
                    data = ConversionUtilities.getValueFromMapfileLine(line);
                    isData = true;
                }
                else if (tokens[0].equalsIgnoreCase("FEATURE")) {
                    feature = new Feature();
                    result = feature.load(br);
                }
                else if (tokens[0].equalsIgnoreCase("FILTER")) {
                    if (tokens.length<2) return false;
                    filter = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("FILTERITEM")) {
                    if (tokens.length<2) return false;
                    filterItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("FOOTER")) {
                    if (tokens.length<2) return false;
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    footer = new File(imagePathString);
                }
                else if (tokens[0].equalsIgnoreCase("GROUP")) {
                    if (tokens.length<2) return false;
                    group = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("HEADER")) {
                    if (tokens.length<2) return false;
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    header = new File(imagePathString);
                }
                else if (tokens[0].equalsIgnoreCase("LABELANGLEITEM")) {
                    if (tokens.length<2) return false;
                    labelAngleItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("LABELCACHE")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON") || tokens[1].equalsIgnoreCase("TRUE")) labelCache = this.ON;
                    else if (tokens[1].equalsIgnoreCase("OFF") || tokens[1].equalsIgnoreCase("FALSE")) labelCache = this.OFF;
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("LABELITEM")) {
                    if (tokens.length<2) return false;
                    labelItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("LABELMAXSCALE")) {
                    if (tokens.length<2) return false;
                    labelMaxScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("LABELMINSCALE")) {
                    if (tokens.length<2) return false;
                    labelMinScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("LABELREQUIRES")) {
                    if (tokens.length<2) return false;
                    labelRequires = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("LABELSIZEITEM")) {
                    if (tokens.length<2) return false;
                    labelSizeItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("MAXFEATURES")) {
                    if (tokens.length<2) return false;
                    maxFeatures = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("MAXSCALE")) {
                    if (tokens.length<2) return false;
                    maxScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("METADATA")) {
                    metaData = new MetaData();
                    result = metaData.load(br);
                }
                else if (tokens[0].equalsIgnoreCase("MINSCALE")) {
                    if (tokens.length<2) return false;
                    minScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if(tokens[0].equalsIgnoreCase("NAME")) {
                    if (tokens.length<2) return false;
                    name = ConversionUtilities.getValueFromMapfileLine(line);
                    isName = true;
                }
                else if(tokens[0].equalsIgnoreCase("OFFSITE")) {
                    if (tokens.length<2) return false;
                    offSite = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if(tokens[0].equalsIgnoreCase("POSTLABELCACHE")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        postLabelCache = true;
                    }
                    else {
                        postLabelCache = false;
                    }
                }
                else if (tokens[0].equalsIgnoreCase("PROJECTION")) {
                    projection = new Projection();
                    projection.load(br);
                }
                else if (tokens[0].equalsIgnoreCase("REQUIRES")) {
                    if (tokens.length<2) return false;
                    requires = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("SIZEUNITS")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("PIXELS")) {
                        sizeUnits = PIXELS;
                    }
                    else if (tokens[1].equalsIgnoreCase("FEET")) {
                        sizeUnits = FEET;
                    }
                    else if (tokens[1].equalsIgnoreCase("INCHES")) {
                        sizeUnits = INCHES;
                    }
                    else if (tokens[1].equalsIgnoreCase("KILOMETERS")) {
                        sizeUnits = KILOMETERS;
                    }
                    else if (tokens[1].equalsIgnoreCase("METERS")) {
                        sizeUnits = METERS;
                    }
                    else if (tokens[1].equalsIgnoreCase("MILES")) {
                        sizeUnits = MILES;
                    }
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) status = this.ON;
                    else if (tokens[1].equalsIgnoreCase("OFF")) status = this.OFF;
                    else if (tokens[1].equalsIgnoreCase("DEFAULT")) status = this.DEFAULT;
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("STYLEITEM")) {
                    if (tokens.length<2) return false;
                    styleItem = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("SYMBOLSCALE")) {
                    if (tokens.length<2) return false;
                    symbolScale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("TEMPLATE")) {
                    if (tokens.length<2) return false;
                    template = ConversionUtilities.getValueFromMapfileLine(line);
                }
                else if (tokens[0].equalsIgnoreCase("TILEINDEX")) {
                    if (tokens.length<2) return false;
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    tileIndex = new File(imagePathString);
                }
                else if (tokens[0].equalsIgnoreCase("TOLERANCE")) {
                    if (tokens.length<2) return false;
                    tolerance = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("TOLERANCEUNITS")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("PIXELS")) {
                        toleranceUnits = PIXELS;
                    }
                    else if (tokens[1].equalsIgnoreCase("FEET")) {
                        toleranceUnits = FEET;
                    }
                    else if (tokens[1].equalsIgnoreCase("INCHES")) {
                        toleranceUnits = INCHES;
                    }
                    else if (tokens[1].equalsIgnoreCase("KILOMETERS")) {
                        toleranceUnits = KILOMETERS;
                    }
                    else if (tokens[1].equalsIgnoreCase("METERS")) {
                        toleranceUnits = METERS;
                    }
                    else if (tokens[1].equalsIgnoreCase("MILES")) {
                        toleranceUnits = MILES;
                    }
                    else if (tokens[1].equalsIgnoreCase("DD")) {
                        toleranceUnits = DD;
                    }
                    else return false;
                }
                else if(tokens[0].equalsIgnoreCase("TRANSFORM")) {
                    if (ConversionUtilities.getValueFromMapfileLine(line).equalsIgnoreCase("TRUE")) {
                        transform = true;
                    }
                    else {
                        transform = false;
                    }
                }
                else if (tokens[0].equalsIgnoreCase("TRANSPARENCY")) {
                    if (tokens.length<2) return false;
                    transparency = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                }
                else if (tokens[0].equalsIgnoreCase("TYPE")) {
                    if (tokens.length<2) return false;
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("POINT"))                type = this.POINT;
                    else if (tokens[1].equalsIgnoreCase("LINE"))            type = this.LINE;
                    else if (tokens[1].equalsIgnoreCase("POLYLINE"))        type = this.POLYLINE;
                    else if (tokens[1].equalsIgnoreCase("POLYGON"))         type = this.POLYGON;
                    else if (tokens[1].equalsIgnoreCase("ANNOTATION"))      type = this.ANNOTATION;
                    else if (tokens[1].equalsIgnoreCase("RASTER"))          type = this.RASTER;
                    else if (tokens[1].equalsIgnoreCase("QUERYONLY"))       type = this.QUERYONLY;
                    else if (tokens[1].equalsIgnoreCase("QUERY"))           type = this.QUERY;
                    else if (tokens[1].equalsIgnoreCase("CIRCLE"))          type = this.CIRCLE;
                    else if (tokens[1].equalsIgnoreCase("POLYLINE_POLYGON"))type = this.POLYLINE_POLYGON;
                    else return false;
                }
                else if (tokens[0].equalsIgnoreCase("END")) {
                    return true ;
                }
                else return false;
                
                // Stop parse file if error detected
                if (!result) return false;
            }
        } catch (Exception e) {
            System.out.println("Layer.load(). Exception: " +  e.getMessage());
            e.printStackTrace();
            e.printStackTrace();
            return false;
        }
        
        if (!isName && !isData) {
            return false;
        }
        
        return result;
    }
    
    /**  Saves LAYER object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t layer\n");
            if (name!=null)             bw.write("\t\t name "+ConversionUtilities.quotes(name)+"\n");
            if (classItem != null)  bw.write("\t\t classitem " + ConversionUtilities.quotes(classItem) + "\n");
            if (connection!=null)       bw.write("\t\t connection " + ConversionUtilities.quotes(connection) + "\n");
            switch (connectionType) {
                case LOCAL:             /* writes nothing to avoid MS bug with "local"*/ break;
                case SDE:               bw.write("\t\t connectionType SDE\n"); break;
                case OGR:               bw.write("\t\t connectionType OGR\n"); break;
                case POSTGIS:           bw.write("\t\t connectionType POSTGIS\n"); break;
                case ORACLESPATIAL:     bw.write("\t\t connectionType ORACLESPATIAL\n"); break;
            }
            if (data!=null)             bw.write("\t\t data "+ConversionUtilities.quotes(data)+"\n");
            if (feature!=null)          feature.saveAsMapFile(bw);    
            if (filter!=null)           bw.write("\t\t filter "+ConversionUtilities.quotes(filter)+"\n");
            if (filterItem!=null)       bw.write("\t\t filteritem "+ConversionUtilities.quotes(filterItem)+"\n");
            if (footer!=null)           bw.write("\t\t footer "+ConversionUtilities.quotes(footer.getPath().replace('\\','/'))+"\n");
            if (group !=null)           bw.write("\t\t group "+ConversionUtilities.quotes(group)+"\n");
            if (header!=null)           bw.write("\t\t header "+ConversionUtilities.quotes(header.getPath().replace('\\','/'))+"\n");
            if (labelAngleItem !=null)  bw.write("\t\t labelangleitem "+ConversionUtilities.quotes(labelAngleItem)+"\n");
            switch(labelCache) {
                case ON:                bw.write("\t\t labelcache ON\n"); break;
                case OFF:               bw.write("\t\t labelcache OFF\n"); break;
            }
            if (labelItem !=null)  bw.write("\t\t labelitem "+ConversionUtilities.quotes(labelItem)+"\n");
            if (labelMaxScale != 0) bw.write("\t\t labelmaxscale "+labelMaxScale+"\n");
            if (labelMinScale != 0) bw.write("\t\t labelminscale "+labelMinScale+"\n");
            if (labelRequires != null) bw.write("\t\t labelrequires "+labelRequires+"\n");
            if (labelSizeItem !=null)  bw.write("\t\t labelsizeitem "+ConversionUtilities.quotes(labelSizeItem)+"\n");
            if (maxFeatures != -1) bw.write("\t\t maxfeatures "+maxFeatures+"\n");
            if (maxScale != 0) bw.write("\t\t maxscale "+maxScale+"\n");
            if (minScale != 0) bw.write("\t\t minscale "+minScale+"\n");
            if (metaData != null) metaData.saveAsMapFile(bw);  
            if (offSite != -1) bw.write("\t\t offsite "+offSite+"\n");
            if (postLabelCache == true) {
                bw.write("\t\t postlabelcache TRUE\n");
            }
            else {
                bw.write("\t\t postlabelcache FALSE\n");
            }
            if (projection != null) projection.saveAsMapFile(bw);
            if (requires != null)   bw.write("\t\t requires "+ConversionUtilities.quotes(requires)+"\n");
            switch (sizeUnits) {
                case PIXELS:
                    bw.write("\t\t sizeunits PIXELS\n");
                    break;
                case FEET:
                    bw.write("\t\t sizeunits FEET\n");
                    break;
                case INCHES:
                    bw.write("\t\t sizeunits INCHES\n");
                    break;
                case KILOMETERS:
                    bw.write("\t\t sizeunits KILOMETERS\n");
                    break;
                case METERS:
                    bw.write("\t\t sizeunits METERS\n");
                    break;
                case MILES:
                    bw.write("\t\t sizeunits MILES\n");
                    break;
            }
            switch (status) {
                case ON:                bw.write("\t\t status ON\n"); break;
                case OFF:               bw.write("\t\t status OFF\n"); break;
                case DEFAULT:           bw.write("\t\t status DEFAULT\n"); break;
                default :               bw.write("\t\t status DEFAULT\n"); break;
            }
            if (styleItem !=null)  bw.write("\t\t styleitem "+styleItem+"\n");
            if (symbolScale != 0) bw.write("\t\t symbolscale "+symbolScale+"\n");
            if (template != null) bw.write("\t\t template "+ConversionUtilities.quotes(template)+"\n");
            if (tileIndex!=null)    bw.write("\t\t tileindex "+ConversionUtilities.quotes(tileIndex.getPath().replace('\\','/'))+"\n");
            if (tileItem !=null)  bw.write("\t\t tileitem "+tileItem+"\n");
            if (tolerance != -1) bw.write("\t\t tolerance "+tolerance+"\n");
            switch (toleranceUnits) {
                case PIXELS:
                    bw.write("\t\t toleranceunits PIXELS\n");
                    break;
                case FEET:
                    bw.write("\t\t toleranceunits FEET\n");
                    break;
                case INCHES:
                    bw.write("\t\t toleranceunits INCHES\n");
                    break;
                case KILOMETERS:
                    bw.write("\t\t toleranceunits KILOMETERS\n");
                    break;
                case METERS:
                    bw.write("\t\t toleranceunits METERS\n");
                    break;
                case MILES:
                    bw.write("\t\t toleranceunits MILES\n");
                    break;
                case DD:
                    bw.write("\t\t toleranceunits DD\n");
                    break;
            }
            if (transform == true) {
                bw.write("\t\t transform TRUE\n");
            }
            else {
                bw.write("\t\t transform FALSE\n");
            }
            if (transparency != 100) bw.write("\t\t transparency "+transparency+"\n");
            switch (type) {
                case POINT:             bw.write("\t\t type POINT\n"); break;
                case LINE:              bw.write("\t\t type LINE\n"); break;
                // modified NRI to avoid crash in MS 3.6.3
                case POLYLINE:          bw.write("\t\t type LINE\n"); break;
                case POLYGON:           bw.write("\t\t type POLYGON\n"); break;
                case ANNOTATION:        bw.write("\t\t type ANNOTATION\n"); break;
                case RASTER:            bw.write("\t\t type RASTER\n"); break;
                case QUERYONLY:         bw.write("\t\t type QUERYONLY\n"); break;
                case POLYLINE_POLYGON:  bw.write("\t\t type POLYLINE_POLYGON\n"); break;
                case QUERY:             bw.write("\t\t type QUERY\n"); break;
                case CIRCLE:            bw.write("\t\t type CIRCLE\n"); break;
            }
            
            if (classes!=null) {
                for (int i=0; i<classes.getNbClasses(); i++) {
                    ((MapClass)classes.getClass(i)).saveAsMapFile(bw);
                }
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }
    
    
    /** Returns a string representation of the LAYER Object
     * @return a string representation of the LAYER Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("LAYER OBJECT ");
            if (name!=null)
                buffer.append("\n* LAYER name              = ").append(name);
            buffer.append("\n* LAYER status            = ").append(status);
            buffer.append("\n* LAYER type              = ").append(type);
            buffer.append("\n* LAYER maxScale          = ").append(maxScale);
            if (tileIndex!=null)
                buffer.append("\n* LAYER tileIndex         = ").append(tileIndex.getAbsolutePath());
            if (tileItem!=null)
                buffer.append("\n* LAYER tileItem          = ").append(tileItem);
            if (data!=null)
                buffer.append("\n* LAYER data              = ").append(data);
            if (classItem!=null)
                buffer.append("\n* LAYER classItem         = ").append(classItem);
            if (labelItem!=null)
                buffer.append("\n* LAYER labelItem         = ").append(labelItem);
            if (classes!=null) {
                for (int i=0; i<classes.getNbClasses(); i++) {
                    buffer.append("\n* LAYER class ").append(i).append("     = ").append(((MapClass)classes.getClass(i)).toString());
                }
            }
            buffer.append("\n* LAYER connectionType     = ").append(connectionType);
            if (connection!=null)
                buffer.append("\n* LAYER connection         = ").append(connection);
            if (filter!=null)
                buffer.append("\n* LAYER filter             = ").append(filter);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY LAYER OBJECT\n\n"+ex;
        }
        return buffer.toString();
    }
    
    
    
}

