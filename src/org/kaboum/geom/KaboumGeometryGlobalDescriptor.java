/*
 *
 * Class KaboumGeometriesIndex from the Kaboum project.
 * This class define an indexed list of KaboumGeoObject.
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

package org.kaboum.geom;

import java.awt.*;
import java.io.Serializable;


/**
 *
 * This class describe a geometrical object within the applet, i.e.
 * a display and a properties descriptor.
 * Each geometrical object is defined by a unique identifiant
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGeometryGlobalDescriptor implements Serializable {
    
    /** Unique identifier, now a String (13/09/2005, NRI) */
    public String id;
    
    /** Geometry */
    public KaboumGeometry geometry;
    
    /** Display descriptor */
    public KaboumGeometryDisplayDescriptor dd;
    
    /** Properties descriptor */
    public KaboumGeometryPropertiesDescriptor pd;
    
    /** Overload the color defined by the display descriptor */
    public Color color = null;
    
    /** Overload isFilled() method from properties descriptor */
    public boolean isTranslucent = false;
    
    /** True if the geometry is in a hilite status */
    private boolean hiliteStatus = false;
    
    /** True if the geometry is in a super hilite status */
    private boolean superHiliteStatus = false;
    
    /**
     * True if the geometry is in a selected status
     * Selected status override hiliteStatus.
     *
     */
    private boolean selectedStatus = false;
    
    /** True if the geometry is in a modified status */
    private boolean modifiedStatus = false;
    
    /**
     * Set the edited status
     * If editedStatus is set to true, 
     * you can add point at the end of
     * the active coordinates list of this
     * GGD geometry (cf: KaboumGEOMETRYOpMode)
     * Default is set to false
     *
     */
    private boolean editedStatus = false;
    
    /** Title menu */
    private String titleMenu = null;

    /** Tooltip */
    private String tooltip = null;

    
    /**
     *
     * Default Constructor
     *
     * @param id        unique identifier
     * @param geometry  geometry
     * @param dd        diplay descriptor
     * @param pd        properties descriptor
     * @param color     own color for extra representation
     *
     */
    public KaboumGeometryGlobalDescriptor(
            KaboumGeometry geometry, 
            KaboumGeometryDisplayDescriptor dd, 
            KaboumGeometryPropertiesDescriptor pd, 
            Color color) {
        this.geometry = geometry;
        this.id = geometry.id;
        this.dd = dd;
        this.pd = pd;
        this.color = color;
    }
    
    /**
     *
     * Get the hilite status
     *
     */
    public boolean isHilite() {
        return this.hiliteStatus;
    }
    
    /**
     *
     * Get the super hilite status
     *
     */
    public boolean isSuperHilite() {
        return this.superHiliteStatus;
    }
    
    /**
     *
     * Set the hilite status
     *
     */
    public void setHilite(boolean b) {
        this.hiliteStatus = b;
    }
    
    /**
     *
     * Set the Super hilite status
     *
     */
    public void setSuperHilite(boolean b) {
        this.superHiliteStatus = b;
    }
    
    /**
     *
     * Set the selected status
     *
     */
    public boolean isSelected() {
        return this.selectedStatus;
    }
    
    
    /**
     *
     * Switch the selected status. This also
     * switch the hilite status...
     *
     *
     */
    public void setSelected(boolean b) {
        this.selectedStatus = b;
        this.hiliteStatus = b;
        if (!b) {
            this.superHiliteStatus = b;
        }
    }
    
    
    /**
     *
     * Set the modified status
     *
     */
    public boolean isModified() {
        return this.modifiedStatus;
    }
    
    
    /**
     *
     * Switch the modified status
     *
     */
    public void setModified(boolean b) {
        this.modifiedStatus = b;
    }
    
    
    /**
     *
     * Set the edited status
     * A list is set as "edited" until it's
     * validated by the KaboumPolygonOpMode.
     * Default is set to true.
     *
     */
    public boolean isEdited() {
        return this.editedStatus;
    }
    
    
    /**
     *
     * Switch the edited status
     *
     */
    public void setEdited(boolean b) {
        this.editedStatus = b;
    }
    
    
    /**
     *
     * Get current color (i.e. active color
     * which is one of normal, hilite or modified
     * color)
     *
     */
    public Color getCurrentColor() {
        if (this.dd == null) {
            return null;
        }
        if (this.isHilite()) {
            if (this.isSuperHilite()) {
                return this.dd.getSuperHiliteColor();
            }
            return this.dd.getHiliteColor();
        }
        if (this.isModified()) {
            return this.dd.getModifiedColor();
        }
        if (this.color != null) {
            return this.color;
        }
        return this.dd.getColor();
    }
    
    
    /**
     *
     * Set the menu title text
     *
     * @param str Title menu text
     *
     */
    public void setTitleMenu(String str) {
        this.titleMenu = str;
    }
    
    
    /**
     *
     * Get the menu title text
     *
     */
    public String getTitleMenuString() {
        return this.titleMenu;
    }

    
    /**
     *
     * Set the tooltip text
     *
     * @param str Tooltip text
     *
     */
    public void setTooltip(String str) {
        this.tooltip = str;
        if (geometry != null) {
            this.geometry.toolTip = str;
        }
    }
    
    
    /**
     *
     * Get the tooltip text
     *
     */
    public String getTooltip() {
        return this.geometry.getToolTip();
    }
    
    /**
     * Gets a string representation of this object (displays all fields)
     * @return a String representation of this object
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("GGD hash: ").append(hashCode()).append("\n");
        buf.append("  color: ").append(color).append("\n");
        buf.append("  editedStatus: ").append(editedStatus).append("\n");
        buf.append("  geometry: ").append(geometry == null ? "null" : geometry.getGeometryType()).append("\n");
        buf.append("  hiliteStatus: ").append(hiliteStatus).append("\n");
        buf.append("  id: ").append(id).append("\n");
        buf.append("  isTranslucent: ").append(isTranslucent).append("\n");
        buf.append("  modifiedStatus: ").append(modifiedStatus).append("\n");
        buf.append("  pd: ").append(pd).append("\n");
        buf.append("  dd: ").append(dd).append("\n");
        buf.append("  selectedStatus: ").append(selectedStatus).append("\n");
        buf.append("  superHiliteStatus: ").append(superHiliteStatus).append("\n");
        buf.append("  titleMenu: ").append(titleMenu).append("\n");
        buf.append("  tooltip: ").append(tooltip).append("\n");
        return buf.toString();
    }

}
