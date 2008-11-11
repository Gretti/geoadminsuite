package org.geogurus.data;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * A class for containing connection parameters for creating to a DataAccess
 * object from a Factory. There is redundancy in the field name so that
 * different DataAccess implementation can have parameters that make sense for
 * them. For example a FileDataAccess might have path but a WMS would have host.
 * Etc...
 * 
 * 
 * @author jesse
 */
// Perhaps we should make subclasses of this class?
public class ConnectionParams implements Serializable {
    private static final long serialVersionUID = -1798457654727210583L;
    public String type;
    public String host;
    public String dbname;
    public String schema;
    public String table;
    public String port;
    public String username;
    public String password;
    public String name;
    public String path;
    public String layer;
    public final Datasource owner;
    public String typename;

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    /**
     * Creates a new instance of type ConnectionParams
     * 
     */
    public ConnectionParams(Datasource owner) {
        this.owner = owner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Datasource getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("serialVersionUID")) {
                continue;
            }
            try {
                Object val = field.get(this);
                if (val != null) {
                    if (string.length() > 0) {
                        string.append(", \n\t");
                    }
                    string.append(field.getName()).append("=").append(
                            val.toString());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return string.toString();
    }
}
