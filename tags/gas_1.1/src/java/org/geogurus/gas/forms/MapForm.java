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
 * LayerForm.java
 *
 * Created on 5 fevrier 2007, 22:51
 */

package org.geogurus.gas.forms;

/**
 *
 * @author Administrateur
 * @version
 */

public class MapForm extends org.apache.struts.action.ActionForm {
    
    private String extent;
    private Byte imageType;
    private Integer imageQuality;
    private String imageColor;
    private Byte interlace;
    private String name;
    private Double width;
    private Double height;
    private Integer resolution;
    private Double scale;
    private Byte status;
    private String fontSet;
    private String shapePath;
    private String canonicalPath;
    private Byte transparent;
    private Byte units;
    
    public String getExtent() {return extent;}
    public Byte getImageType() {return imageType;}
    public Integer getImageQuality() {return imageQuality;}
    public String getImageColor() {return imageColor;}
    public Byte getInterlace() {return interlace;}
    public String getName() {return name;}
    public Double getWidth() {return width;}
    public Double getHeight() {return height;}
    public Integer getResolution() {return resolution;}
    public Double getScale() {return scale;}
    public Byte getStatus() {return status;}
    public String getFontSet() {return fontSet;}
    public String getShapePath() {return shapePath;}
    public String getCanonicalPath() {return canonicalPath;}
    public Byte getTransparent() {return transparent;}
    public Byte getUnits() {return units;}
    
    public void setExtent(String e) {extent = e;}
    public void setImageType(Byte it) {imageType = it;}
    public void setImageQuality(Integer iq) {imageQuality = iq;}
    public void setImageColor(String ic) {imageColor = ic;}
    public void setInterlace(Byte i) {interlace = i;}
    public void setName(String n) {name = n;}
    public void setWidth(Double w) {width = w;}
    public void setHeight(Double h) {height = h;}
    public void setResolution(Integer r) {resolution = r;}
    public void setScale(Double s) {scale = s;}
    public void setStatus(Byte s) {status = s;}
    public void setFontSet(String fs) {fontSet = fs;}
    public void setShapePath(String sp) {shapePath = sp;}
    public void setCanonicalPath(String ss) {canonicalPath = ss;}
    public void setTransparent(Byte t) {transparent = t;}
    public void setUnits(Byte u) {units = u;}
    
    /**
     *
     */
    public MapForm() {
        super();
    }
}
