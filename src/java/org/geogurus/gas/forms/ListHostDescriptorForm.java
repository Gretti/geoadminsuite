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
 * ListHostDescriptorForm.java
 *
 * Created on 28 decembre 2006, 01:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geogurus.gas.forms;

import org.apache.struts.action.ActionForm;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.gas.objects.ListHostDescriptorBean;

/**
 *
 * @author Administrateur
 */
public class ListHostDescriptorForm  extends ActionForm {
    
    private ListHostDescriptorBean m_listHost;
    
    /** Creates a new instance of ListHostDescriptorForm */
    public ListHostDescriptorForm() {
        m_listHost = new ListHostDescriptorBean();
    }
    
    public ListHostDescriptorBean getListHost() {return m_listHost;}
    
    public void setListHost(ListHostDescriptorBean listHost_) {m_listHost = listHost_;}
    
    public HostDescriptorBean getHost(int index_) {
        while (index_ >= m_listHost.getNbHosts()) {
            m_listHost.addHost(new HostDescriptorBean());
        }
        return (HostDescriptorBean)m_listHost.getHost(index_);
    }
}
