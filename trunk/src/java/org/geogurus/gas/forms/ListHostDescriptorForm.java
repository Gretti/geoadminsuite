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
