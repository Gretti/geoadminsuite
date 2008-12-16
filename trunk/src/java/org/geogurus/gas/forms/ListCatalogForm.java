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
