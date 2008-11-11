/*
 * HostDescriptorBean.java
 *
 * Created on 27 december 2006, 10:46
 *
 */

package org.geogurus.gas.objects;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A bean connection parameters, ie a folder path or a Postgresql database
 * cluster, or an Oracle instance. A postgresql cluster may contain several
 * databases. The pgTemplateDB attribute is the name of the template database on
 * the cluster that will be queried to get the list of databases.
 * 
 * @author Gretti
 */
public class HostDescriptorBean implements Serializable {

    private static final long serialVersionUID = -5446515886614067714L;

    /**
     * Name of the HostDescriptorBean
     */
    protected String m_name = "";

    /** Path */
    protected String m_path = "";

    /** Port */
    protected String m_port = "";

    /** User Name */
    protected String m_uname = "";

    /** User Password */
    protected String m_upwd = "";

    /** type of */
    protected String m_type = "";

    /** Name of a table in a schema */
    protected String m_tablename = "";

    /** Name of a schema */
    protected String m_schema = "";

    /**
     * database template/instance to connect to when querying the list of
     * available databases, or when listing geo tables for oracle
     */
    protected String m_instance = "template1";

    /**
     * in case of folder host, "on" if folder should be recursively scanned
     * Note: Should be a boolean value but can't load extJs value into the bean
     * with boolean type
     */
    protected String m_recurse;

    /**
     * Creates a new instance of HostDescriptorBean
     */
    public HostDescriptorBean() {
        super();
    }

    /**
     * Creates a new instance of HostDescriptorBean from String, ex:
     * localhost,null,5432,postgres,postgres,template1,null,pg
     * localhost,/Users/nicolas/tmp,null,null,null,null,on,folder
     * localhost,/Applications/GIS/data,null,null,null,null,off,folder thales
     * peru,/home/gnguessan/projects/thales_perou/data,null,null,null,null,on,
     * folder laptox,null,1521,geo,geo,orcl,null,Oracle
     * 
     * @param hostString_
     *            String
     *            "name, path, port, uname, upwd, instance, recurse, type"
     */
    public HostDescriptorBean(String host_) {
        super();
        StringTokenizer tok = new StringTokenizer(host_, ",");
        if (tok.countTokens() == 8) {
            m_name = tok.nextToken();
            m_path = tok.nextToken();
            m_port = tok.nextToken();
            m_uname = tok.nextToken();
            m_upwd = tok.nextToken();
            m_instance = tok.nextToken();
            m_recurse = tok.nextToken();
            m_type = tok.nextToken();
        }
    }

    /** Getter Method : Name of datasource */
    public String getName() {
        return m_name;
    }

    /** Getter Method : Path of datasource */
    public String getPath() {
        return m_path;
    }

    /** Getter Method : Port of datasource */
    public String getPort() {
        return "null".equals(m_port) ? "" : m_port;
    }

    /** Getter Method : UserName of datasource */
    public String getUname() {
        return "null".equals(m_uname) ? "" : m_uname;
    }

    /** Getter Method : UserPassword of datasource */
    public String getUpwd() {
        return "null".equals(m_upwd) ? "" : m_upwd;
    }

    /** Getter Method : Type of datasource */
    public String getType() {
        return m_type;
    }

    /** Getter Method : PG template database of datasource */
    public String getInstance() {
        return m_instance;
    }

    /** Getter Method : recurse into folder */
    public String getRecurse() {
        return m_recurse;
    }

    public String getTablename() {
        return m_tablename;
    }

    public String getSchema() {
        return m_schema;
    }

    /** Setter Method : Name of datasource */
    public void setName(String name_) {
        m_name = name_;
    }

    /** Setter Method : Path of datasource */
    public void setPath(String path_) {
        m_path = path_;
    }

    /** Setter Method : Port of datasource */
    public void setPort(String port_) {
        m_port = port_;
    }

    /** Setter Method : UserName of datasource */
    public void setUname(String uname_) {
        m_uname = uname_;
    }

    /** Setter Method : UserPassword of datasource */
    public void setUpwd(String upwd_) {
        m_upwd = upwd_;
    }

    /** Setter Method : Type of datasource */
    public void setType(String type_) {
        m_type = type_;
    }

    /** Setter Method : instance database */
    public void setInstance(String instance_) {
        m_instance = instance_;
    }

    /**
     * Setter Method : recurse into folder, string param must be "on" for the
     * value to be true (Ext parameter)
     */
    public void setRecurse(String recurse_) {
        m_recurse = recurse_;
    }

    public void setTablename(String tablename_) {
        m_tablename = tablename_;
    }

    public void setSchema(String schema_) {
        m_schema = schema_;
    }

    @Override
    public String toString() {
        String str = "Host :\n";

        str += this.getName() + "\t";
        str += this.getPath() + "\t";
        str += this.getPort() + "\t";
        str += this.getUname() + "\t";
        str += this.getUpwd() + "\t";
        str += this.getInstance() + "\n";
        str += this.getRecurse() + "\n";
        str += this.getType() + "\n";

        return str;
    }

    public boolean parseRecurse() {
        return ("on".equalsIgnoreCase(getRecurse()) || "true"
                .equalsIgnoreCase(getRecurse()));
    }
}
