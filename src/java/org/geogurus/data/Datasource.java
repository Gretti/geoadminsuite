/*
 * Datasource.java
 *
 * Created on 31 juillet 2002, 17:41
 */
package org.geogurus.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;



/**
 * A class mapping a datasource = place where to find geo data. It can be a
 * postgresql database, a folder, a mapfile, an Oracle Spatial database
 * Retrieving list of data (geo tables, geo files or mapfile layers)
 * 
 * @author nicolas Ribot
 */
public abstract class Datasource implements Serializable,
        Comparable<Datasource> {
    private static final long serialVersionUID = 1L;

    /**
     * the name of the datasource: either a folder path or a DB table name,
     * according to the Datasource type (see class constants)
     */
    protected String name = null;
    protected String host = null;
    private DatasourceType type;
    /**
     * the list of geometryClasses contained in this datasource (indexed by
     * their ID)
     */
    protected final Hashtable<String, DataAccess> dataList = new Hashtable<String, DataAccess>();
    /** the sorted list of GeometryClass, based on their names */
    protected DataAccess[] sortedDataList;
    /** this error message, for callers */
    public String errorMessage;
    protected final transient Logger logger;
    protected transient Map<String, Object> cache = new HashMap<String, Object>();
    /**
     * the unique identifier for this datasource, can be used to identify a
     * datasource, for instance in UI part
     */
    private String id;

    /** Creates a new instance of Datasource */
    public Datasource() {
        this.id = "" + System.identityHashCode(this);
        logger = Logger.getLogger(this.getClass().getName());
    }

    /** Creates a new instance of Datasource */
    public Datasource(String name, String host, DatasourceType type) {
        this();
        this.name = name;
        this.host = host;
        this.type = type;
    }

    /**
     * Returns a sorted list of data (GeometryClass), usefull for presentation
     */
    public DataAccess[] getSortedDataList() {
        try {
            sortedDataList = (DataAccess[]) getDataList().values().toArray(
                    new DataAccess[dataList.size()]);
            Arrays.sort(sortedDataList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sortedDataList;
    }

    public Hashtable<String, DataAccess> getDataList() {
        return dataList;
    }

    /**
     * Creates all the DataAccess objects and puts then in the {@link #dataList}
     * field.
     * 
     * @return true if the DataAccess objects were able to be loaded
     */
    public abstract boolean load();

    /**
     * 
     * Returns this object's identifier (based on hashcode for this class
     * 
     * @return the identifier string
     */
    public String getId() {
        return id;
    }

    /**
     *Implements the Comparable::compareTo() method. the comparison is based on
     * datasource id
     * 
     */
    public int compareTo(Datasource o) {
        return getId().compareTo(o.getId());
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
     * @return null Option if the object cannot be created otherwise an option
     *         with the object
     * @throws IOException
     */
    public abstract <T> Option<T> resource(java.lang.Class<T> request)
            throws IOException;

    public String getName() {
        return name;
    }

    /*Needs to be escaped for Windows path*/
    public String getEscapedName() {
        return this.name.replace('\\', '/');
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public DatasourceType getType() {
        return type;
    }

}
