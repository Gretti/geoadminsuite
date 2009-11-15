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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.forms;

import java.awt.Dimension;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.ScaleBar;

/**
 *
 * Represents the GUI ScaleBar form. the validate method will built a full scaleBar object
 * with given values
 * @author gnguessan
 */
public class ScaleBarForm extends org.apache.struts.action.ActionForm {
    

    private ScaleBar scale;
    private Integer width;
    private Integer height;
    private String fgcolor;
    private String bgcolor;
    private String olcolor;

    private Boolean interlace;
    private Integer intervals;
    private Integer position;

    private Boolean postLabelCache;
    private Integer status;
    private Integer style;
    private Integer transparent;
    private Integer units;

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
       // validation is done on the client part.
       // can build a scale object representing this form values:
       scale.setSize(new Dimension(getWidth(), getHeight()));
       scale.setBackgroundColor(new RGB(getBgcolor()));
       scale.setColor(new RGB(getFgcolor()));
       scale.setOutlineColor(new RGB(getOlcolor()));
       scale.setInterlace(getInterlace());
       scale.setIntervals(getIntervals());
       scale.setPosition(getPosition().byteValue());
       scale.setPostLabelCache(getPostLabelCache());
       scale.setStatus(getStatus().byteValue());
       scale.setStyle(getStyle());
       scale.setTransparent(getTransparent().byteValue());
       scale.setUnits(getUnits().byteValue());
       return errors;
   }

public Boolean getInterlace() {
        return interlace;
    }

    public void setInterlace(Boolean interlace) {
        this.interlace = interlace;
    }

    public Integer getIntervals() {
        return intervals;
    }

    public void setIntervals(Integer intervals) {
        this.intervals = intervals;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getPostLabelCache() {
        return postLabelCache;
    }

    public void setPostLabelCache(Boolean postLabelCache) {
        this.postLabelCache = postLabelCache;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStyle() {
        return style;
    }

    public void setStyle(Integer style) {
        this.style = style;
    }

    public Integer getTransparent() {
        return transparent;
    }

    public void setTransparent(Integer transparent) {
        this.transparent = transparent;
    }

    public Integer getUnits() {
        return units;
    }

    public void setUnits(Integer units) {
        this.units = units;
    }    
    
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
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
