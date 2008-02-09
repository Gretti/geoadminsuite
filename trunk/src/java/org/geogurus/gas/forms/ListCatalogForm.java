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

/**
 *
 * @author Administrateur
 */
public class ListCatalogForm  extends ActionForm {
    
    private String m_selectedIds;
    
    private Integer m_screenWidth;
    
    private Integer m_screenHeight;
    
    /** Creates a new instance of ListHostDescriptorForm */
    public ListCatalogForm() {
    }
    
    public String getSelectedIds() {return m_selectedIds;}
    
    public void setSelectedIds(String ids_) {m_selectedIds = ids_;}
    
    public Integer getScreenWidth() {return this.m_screenWidth;}
    
    public void setScreenWidth(Integer screenWidth_) {this.m_screenWidth = screenWidth_;}
    
    public Integer getScreenHeight() {return this.m_screenHeight;}
    
    public void setScreenHeight(Integer screenHeight_) {this.m_screenHeight = screenHeight_;}
}
