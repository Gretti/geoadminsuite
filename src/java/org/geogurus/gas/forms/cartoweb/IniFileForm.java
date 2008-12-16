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
