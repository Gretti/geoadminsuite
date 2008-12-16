/*
 * Copyright (C) 2007-2008  Camptocamp
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.forms.cartoweb;

import org.apache.struts.upload.FormFile;

/**
 *
 * @author nicolas
 */
public class IniFileForm extends org.apache.struts.action.ActionForm {
   /**
    * The formFile to upload to set this 
    */
   private FormFile iniFile;
   /**
    * The type of ini file to upload, as set in the client's select component
    */
   private String fileType;
   
   
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public FormFile getIniFile() {
        return iniFile;
    }

    public void setIniFile(FormFile iniFile) {
        this.iniFile = iniFile;
    }
   
   
}
