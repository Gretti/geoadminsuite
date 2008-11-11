/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.geogurus.mapserver.objects.ScaleBar;

/**
 *
 * @author gnguessan
 */
public class ScaleBarForm extends org.apache.struts.action.ActionForm {
    

    private ScaleBar scale;
    private Double width;
    private Double height;
    private String fgcolor;
    private String bgcolor;
    private String olcolor;

   /**
    *
    */
   public ScaleBarForm() {
       super();
       this.scale = new ScaleBar();
   }

    @Override
   public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
       ActionErrors errors = new ActionErrors();
       return errors;
   }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public ScaleBar getScale() {
        return scale;
    }

    public void setScale(ScaleBar scale) {
        this.scale = scale;
    }

    public String getFgcolor() {
        return fgcolor;
    }

    public void setFgcolor(String fgcolor) {
        this.fgcolor = fgcolor;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public String getOlcolor() {
        return olcolor;
    }

    public void setOlcolor(String olcolor) {
        this.olcolor = olcolor;
    }
}
