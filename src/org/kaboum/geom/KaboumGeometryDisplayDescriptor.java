package org.kaboum.geom;

/*
 *
 * Class KaboumGeoObjectDisplayDescriptor from the Kaboum project.
 * This class define a geometry class. A geometry class
 * id defined by a geometry type, a color...
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

import java.awt.*;
import java.io.Serializable;
import org.kaboum.util.KaboumCoordinate;

/**
 *
 * This class is a display descriptor for GeoObject. This descriptor
 * define the way in which an object must be drawn on screen (i.e. line
 * and point color, hilite color, etc...)
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGeometryDisplayDescriptor implements Serializable {
    
    /** DisplayDescriptor name */
    public String name;
    
    /** DisplayDescriptor color */
    private Color color = Color.red;
    
    /** DisplayDescriptor fill color */
    private Color fillColor = Color.red;
    
    /** DisplayDescriptor hilite color */
    private Color hiliteColor = Color.yellow;
    
    /** DisplayDescriptor super hilite color */
    private Color superHiliteColor = Color.yellow;
    
    /** DisplayDescriptor modified color */
    private Color modifiedColor = Color.green;
    
    /** Point type */
    private int pointType = KaboumCoordinate.K_TYPE_BOX;
    
    /** Point height */
    private int pointHeight = 5;
    
    /** Point width */
    private int pointWidth = 5;
    
    /** Line width */
    private int lineWidth = 1;
    
    /** Point color */
    private Color pointColor = Color.black;
    
    /** Point hilite color */
    private Color pointHiliteColor = Color.yellow;
    
    /** Image drawn at point position (null if not defined) */
    private Image pointImage = null;
    
    /** Fill status */
    private boolean filling = false;
    
    
    /**
     *
     * Default constructor
     *
     */
    public KaboumGeometryDisplayDescriptor(String _name) {
        this(_name, null, null, null, null, null, -1, -1, -1, -1, null, null, null, false);
    }
    
    
    /**
     *
     * Constructor
     *
     *  @param name DisplayDescriptor name (unique identifier)
     *  @param color Geometry color
     *  @param fillColor Geometry fill color
     *  @param hiliteColor Geometry hilited color
     *  @param superHiliteColor Geometry hilited color
     *  @param modifiedColor Geometry modified color
     *  @param pointType Point type (cf: KaboumControlPoint)
     *  @param pointHeight Point height
     *  @param pointWidth Point width
     *  @param lineWidth Line width
     *  @param pointColor Point Color
     *  @param pointHiliteColor Point Hilite Color
     *  @param pointImage Image drawn at point position (null if not define)
     *  @param filling True: object is filled (only valid for polygon)
     *
     */
    public KaboumGeometryDisplayDescriptor(String _name, Color _color, Color _fillColor, Color _hiliteColor, Color _superHiliteColor, Color _modifiedColor, int _pointType, int _pointHeight, int _pointWidth, int _lineWidth, Color _pointColor, Color _pointHiliteColor, Image _pointImage, boolean _filling) {
        
        this.name = _name;
        if (_color != null) { this.color = _color; }
        if (_fillColor != null) { this.fillColor = _fillColor; }
        if (_hiliteColor != null) { this.hiliteColor = _hiliteColor; }
        if (_superHiliteColor != null) { this.superHiliteColor = _superHiliteColor; }
        if (_modifiedColor != null) { this.modifiedColor = _modifiedColor; }
        if (_pointType != -1) { this.pointType = _pointType; }
        if (_pointHeight != -1) { this.pointHeight = _pointHeight; }
        if (_pointWidth != -1) { this.pointWidth = _pointWidth; }
        if (_lineWidth != -1) { this.lineWidth = _lineWidth; }
        if (_pointColor != null) { this.pointColor = _pointColor; }
        if (_pointHiliteColor != null) { this.pointHiliteColor = _pointHiliteColor; }
        if (_pointImage != null) { this.pointImage = _pointImage; }
        this.filling = _filling;
    }
    
    
    /**
     *
     * Get name
     *

     */
    public String getName() {
        return this.name;
    }
    
    
    /**
     *
     * Get color
     *
     */
    public Color getColor() {
        return this.color;
    }
    
    /**
     *
     * Get fill color
     *
     */
    public Color getFillColor() {
        return this.fillColor;
    }
    
    
    /**
     *
     * Get hilite color
     *
     */
    public Color getHiliteColor() {
        if (this.color == null) {
            return null;
        }
        return this.hiliteColor;
    }
    
    /**
     *
     * Get super hilite color
     *
     */
    public Color getSuperHiliteColor() {
        if (this.color == null) {
            return null;
        }
        return this.superHiliteColor;
    }
    
    
    /**
     *
     * Get modified color
     *
     */
    public Color getModifiedColor() {
        if (this.color == null) {
            return null;
        }
        return this.modifiedColor;
    }
    
    
    /**
     *
     * Get point type
     *
     */
    public int getPointType() {
        return this.pointType;
    }
    
    
    /**
     *
     * Get point height
     *
     */
    public int getPointHeight() {
        return this.pointHeight;
    }
    
    
    /**
     *
     * Get point width
     *
     */
    public int getPointWidth() {
        return this.pointWidth;
    }
    

    /**
     *
     * Get line width
     *
     */
    public int getLineWidth() {
        return this.lineWidth;
    }

    
    /**
     *
     * Get point color
     *
     */
    public Color getPointColor() {
        if (this.color == null) {
            return null;
        }
        return this.pointColor;
    }
    
    
    /**
     *
     * Get point hilite color
     *
     */
    public Color getPointHiliteColor() {
        if (this.color == null) {
            return null;
        }
        return this.pointHiliteColor;
    }
    
        
    /**
     *
     * Get point image
     *
     */
    public Image getPointImage() {
        if (this.color == null) {
            return null;
        }
        return this.pointImage;
    }
    
    
    /**
     *
     * Get Filling
     *
     */
    public boolean getFilling() {
        return this.filling;
    }
    
    
    /**
     *
     * Set color
     *
     * @param color New color
     *
     */
    public void setColor(Color _color) {
        if (_color != null) {
            this.color = _color;
        }
    }
    
    
    /**
     *
     * Set fill color
     *
     * @param color New color
     *
     */
    public void setFillColor(Color _color) {
        if (_color != null) {
            this.fillColor = _color;
        }
    }
    
    
    /**
     *
     * Set hilite color
     *
     * @param color New hilite color
     *
     */
    public void setHiliteColor(Color _color) {
        if (_color != null) {
            this.hiliteColor = _color;
        }
    }
    
    
    /**
     *
     * Set super hilite color
     *
     * @param color New super hilite color
     *
     */
    public void setSuperHiliteColor(Color _color) {
        if (_color != null) {
            this.superHiliteColor = _color;
        }
    }
    
    
    /**
     *
     * Set modified  color
     *
     * @param color New modified color
     *
     */
    public void setModifiedColor(Color _color) {
        if (_color != null) {
            this.modifiedColor = _color;
        }
    }
    
    
    /**
     *
     * Set point type
     *
     * @param type New point type
     *
     */
    public void setPointType(int type) {
        this.pointType = type;
    }
    
    
    /**
     *
     * Set point color
     *
     * @param color New point color
     *
     */
    public void setPointColor(Color _color) {
        if (_color != null) {
            this.pointColor = _color;
        }
    }
    
    
    /**
     *
     * Set point hilite color
     *
     * @param color New point hilite color
     *
     */
    public void setPointHiliteColor(Color _color) {
        if (_color != null) {
            this.pointHiliteColor = _color;
        }
    }
    
        
    /**
     *
     * Set point image
     *
     * @param image New point image
     *
     */
    public void setPointImage(Image image) {
        this.pointImage = image;
    }
    
    
    /**
     *
     * Set fill status
     *
     * @param fill True: object is filled (only for polygon)
     *
     */
    public void setFilling(boolean fill) {
        this.filling = fill;
    }
    
    
    /**
     * Returns the string representation of this object
     *<p>
     * Really usefull for debugging
     *</p>
     *@return the String representation
     */
    public String toString() {
        
        StringBuffer str = new StringBuffer();
        
        str.append("DISPLAY DESCRIPTOR : ").append(this.name).append("\n");
        str.append("  COLOR : ").append(this.color ).append("\n");
        str.append("  FILL_COLOR : " ).append( this.fillColor ).append( "\n");
        str.append( "  HILITE COLOR : " ).append( this.hiliteColor ).append( "\n");
        str.append( "  SUPER HILITE COLOR : " ).append( this.superHiliteColor ).append( "\n");
        str.append( "  MODIFIED COLOR : " ).append( this.modifiedColor ).append( "\n");
        str.append( "  POINT IMAGE : " ).append( this.pointImage ).append( "\n");
        str.append( "  POINT TYPE : " ).append( this.pointType ).append( "\n");
        str.append( "  POINT HEIGHT : " ).append( this.pointHeight ).append( "\n");
        str.append( "  POINT WIDTH : " ).append( this.pointWidth ).append( "\n");
        str.append( "  LINE WIDTH : " ).append( this.lineWidth ).append( "\n");
        str.append( "  POINT COLOR : " ).append( this.pointColor ).append( "\n");
        str.append( "  POINT HILITE COLOR : " ).append( this.pointHiliteColor ).append( "\n");
        str.append( "  FILLING : " ).append( this.filling);
        
        return str.toString();
    }
}

