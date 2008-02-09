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
 *
 * @author Gretti
 */
public class HostDescriptorBean implements Serializable {
    
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
    
    /** Path */
    protected String m_type = "";
    
    /**
     * Creates a new instance of HostDescriptorBean
     */
    public HostDescriptorBean() {
        super();
    }
    
    /**
     * Creates a new instance of HostDescriptorBean from String
     *
     * @param hostString_ String "name, path, port, uname, upwd, type"
     */
    public HostDescriptorBean(String host_) {
        super();
        StringTokenizer tok = new StringTokenizer(host_, ",");
        if(tok.countTokens() == 6) {
            m_name  = tok.nextToken();
            m_path  = tok.nextToken();
            m_port  = tok.nextToken();
            m_uname = tok.nextToken();
            m_upwd  = tok.nextToken();
            m_type  = tok.nextToken();
        }
    }
    
    /** Getter Method : Name of datasource*/
    public String getName() {return m_name;}
    /** Getter Method : Path of datasource*/
    public String getPath() {return m_path;}
    /** Getter Method : Port of datasource*/
    public String getPort() {return "null".equals(m_port) ? "" : m_port;}
    /** Getter Method : UserName of datasource*/
    public String getUname() {return "null".equals(m_uname) ? "" : m_uname;}
    /** Getter Method : UserPassword of datasource*/
    public String getUpwd() {return "null".equals(m_upwd) ? "" : m_upwd;}
    /** Getter Method : Type of datasource*/
    public String getType() {return m_type;}
    
    /** Setter Method : Name of datasource*/
    public void setName(String name_) {m_name = name_;}
    /** Setter Method : Path of datasource*/
    public void setPath(String path_) {m_path = path_;}
    /** Setter Method : Port of datasource*/
    public void setPort(String port_) {m_port = port_;}
    /** Setter Method : UserName of datasource*/
    public void setUname(String uname_) {m_uname = uname_;}
    /** Setter Method : UserPassword of datasource*/
    public void setUpwd(String upwd_) {m_upwd = upwd_;}
    /** Setter Method : Type of datasource*/
    public void setType(String type_) {m_type = type_;}
    
    public String toString() {
        String str = "Host :\n";
        
        str += this.getName()  + "\t";
        str += this.getPath()  + "\t";
        str += this.getPort()  + "\t";
        str += this.getUname() + "\t";
        str += this.getUpwd()  + "\t";
        str += this.getType()  + "\n";
        
        return str;
    }
}
