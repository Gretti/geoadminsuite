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
 * ListHostDescriptorBean.java
 *
 * Created on 27 december 2006, 11:45
 */

package org.geogurus.gas.objects;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author Gretti
 */
public class ListHostDescriptorBean implements Serializable {
    
    protected Vector m_listHost;
    
    /** Creates a new instance of ListHostDescriptorBean */
    public ListHostDescriptorBean() {
        m_listHost = new Vector();
    }
    
    /**
     * Gets an iterator on ListHost's Vector.
     * @return java.util.Iterator
     */
    public Iterator getListHostDescriptor() {
        return m_listHost.iterator();
    }
    
    /**
     * Find a Host using index.
     * @param i_
     * @return HostDescriptorBean
     */
    public HostDescriptorBean getHost(int i_) {
        return (HostDescriptorBean)m_listHost.get(i_);
    }
    
    /**
     * Return number of Hosts in list.
     * @return int
     */
    public int getNbHosts() {
        return m_listHost.size();
    }
    
    /**
     * Add a Host in list.
     * @param host_
     */
    public void addHost(HostDescriptorBean host_) {
        m_listHost.add(host_);
    }
    
    /**
     * @param hosts_
     */
    public void setHosts(Vector hosts_) {
        m_listHost = hosts_;
    }
    
    /**
     * Replace native toString method
     * @return String
     */
    @Override
    public String toString() {
        String s = "ListHostDescriptor " + m_listHost.size() + " element(s)\n";
        
        for(Iterator ite = m_listHost.iterator(); ite.hasNext();) {
            HostDescriptorBean h = (HostDescriptorBean)ite.next();
            s += h.toString();
            s += "\n";
        }
        
        return s;
    }
    
}
