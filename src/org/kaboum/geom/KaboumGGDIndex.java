/*
 *
 * Class KaboumGGDIndex from the Kaboum project.
 * This class define an indexed KaboumList of KaboumGeoObject.
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
 * License alfong with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.kaboum.geom;

import java.awt.*;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Vector;
import org.kaboum.Kaboum;
import org.kaboum.util.KaboumList;
import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumExtent;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.util.KaboumFeatureModes;


/**
 *
 * This class define an indexed KaboumList of KaboumGeometryGlobalDescriptor.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGGDIndex extends KaboumList implements Serializable {
    
    /** CONSTANT : RESERVED ID (try to make it unique) */
    public static final String RESERVED_ID = "$$$KABOUM_RESERVED_ID$$$";
    
    /** Reference to parent class */
    private Kaboum parent;
    
    /** Valid extent of the current view. This is used to only paint visible geometries */
    private KaboumExtent extent;
    
    /** Array of visible object */
    private KaboumGeometryGlobalDescriptor[] visibleGeometries;
    
    /** The geometry with identificator unpaintID is not paint */
    public static String onTopID = RESERVED_ID;
    
    /** tells if visible geometries reconstruction should be made in the add method,
     * or temporary disabled to speed up performance
     */
    public boolean shouldReconstruct;
    
    /** true to prevent geometries from being painted */
    public boolean hideGeometries;
    
    /** the Array of KaboumCoordinates that participates to the snapping process, if any
     * defined.
     * this list is maintained in // with the visibleGeoemtries array: each time the map view is updated
     * and geometries are refreshed, this list should be rebuilt
     */
    KaboumCoordinate[] snapVertices = null;
    
    /**
     *
     * Default Constructor
     *
     * @param extent   Current extent view
     *
     */
    public KaboumGGDIndex(Kaboum parent, KaboumExtent extent) {
        this.parent = parent;
        this.extent = extent;
        shouldReconstruct = true;
        hideGeometries = false;
    }
    
    
    /**
     *
     * Default Constructor
     *
     */
    public KaboumGGDIndex(Kaboum parent) {
        this(parent, new KaboumExtent());
    }
    
    /**
     * Set the new extent.
     *<p>
     * If a KaboumFeatureServerTools object exist, the call to reconstructVisibleGeometries
     * will be skipped as the object will explictly call it each time features
     *</p>
     *@param _extent the extent to set
     */
    public void setExtent(KaboumExtent _extent) {
        this.extent = _extent;
        
        //if (parent.featureServerTools == null) {
        // no server configured to retrieve geometries, reconstruction is done when
        // setting extent.
        this.reconstructVisibleGeometries();
        //}
    }
    
    
    /**
     *
     * Reconstruct the visible KaboumList according to the
     * current extent
     *<p>
     * skip this reconstruction if featureServerTools.liveMode is set to 0 (= live)
     * because in this case, all geometries lie in the current extent
     *</p>
     *
     */
    public void reconstructVisibleGeometries() {
        if (this.extent == null) {
            return;
        }
        int size = this.size();
        int visibleSize = 0;
        ArrayList snapVerticesAl = new ArrayList();
        KaboumGeometryGlobalDescriptor tmpGGD;
        KaboumGeometryGlobalDescriptor[] tmpGGDArray = new KaboumGeometryGlobalDescriptor[size];
        for (int i = 0; i < size; i++) {
            
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            
            //
            // Special case :
            //
            // K_NEW_GEOMETRY is always in the visibleGeometries list
            // even if its geometry is null
            //
            if (tmpGGD.id.equals(Kaboum.K_NEW_GEOMETRY)) {
                tmpGGDArray[visibleSize] = tmpGGD;
                visibleSize++;
                continue;
            }
            
            if (tmpGGD.geometry == null) {
                continue;
            }
            if (this.extent.overlap(tmpGGD.geometry.getExtent())) {
                tmpGGDArray[visibleSize] = tmpGGD;
                visibleSize++;
            }
            
            if (tmpGGD.pd.isVertexSnappable()) {
                // this GGD coordinates participate to snapping
                for (int j = 0; j < tmpGGD.geometry.getCoordinates().length; j++) {
                    snapVerticesAl.add(tmpGGD.geometry.getCoordinates()[j]);
                }
            }
        }
        
        if (visibleSize > 0) {
            this.visibleGeometries = new KaboumGeometryGlobalDescriptor[visibleSize];
            System.arraycopy(tmpGGDArray, 0, this.visibleGeometries, 0, visibleSize);
            this.snapVertices = (KaboumCoordinate[])snapVerticesAl.toArray(new KaboumCoordinate[snapVerticesAl.size()]);
        } else {
            this.visibleGeometries = new KaboumGeometryGlobalDescriptor[0];
        }
    }
    
    
    /**
     *
     * Return the array of visible objects within
     * the current extent
     *
     */
    public KaboumGeometryGlobalDescriptor[] getVisibleGeometries() {
        return this.visibleGeometries;
    }
    
    /**
     *
     * Return the array of geometries vertices that can be snapped to (class the geometry
     * belongs to should be snappable).
     * The list of snappable vertices is built on a reconstrucVisibleGeometries() call
     *
     */
    public KaboumCoordinate[] getSnapVertices() {
        return snapVertices;
    }
    
    /**
     * Gets a vector of KaboumGeometryGlobalDescriptor for the given class name (PD or DD name)
     * @param className
     * @return
     */
    public Vector getGeometries(String className) {
        if (className == null) {
            return null;
        }
        Vector geoms = new Vector();
        for (int i = 0; i < this.size(); i++) {
            if (className.equals(((KaboumGeometryGlobalDescriptor) this.elementAt(i)).pd.getName())) {
                geoms.add(this.elementAt(i));
            }
        }
        return geoms;
    }
    /**
     *
     * Remove an element from this Array. In fact, element is
     * not strictly removed (except for K_NEW_GEOMETRY), but
     * it's geometry is set to null
     *
     * @param id Id of the element to remove
     *
     */
    public boolean removeGeometry(String id) {
        
        int size = this.size();
        KaboumGeometryGlobalDescriptor tmpGGD;
        
        for (int i = 0; i < size; i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if ( tmpGGD.id.equals(id) ) {
                
                // Special case
                if (Kaboum.K_NEW_GEOMETRY.equals(id)) {
                    this.removeElementAt(i);
                    this.reconstructVisibleGeometries();
                    return true;
                }
                
                tmpGGD.geometry = null;
                this.reconstructVisibleGeometries();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Remove all the elements from this geoObject
     */
    public void removeAllGeometries() {
        
        int size = this.size();
        KaboumGeometryGlobalDescriptor tmpGGD;
        
        for (int i = 0; i < size; i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            tmpGGD.geometry = null;
        }
        this.visibleGeometries = new KaboumGeometryGlobalDescriptor[0];
    }
    
    /**
     * Delete all the geometries except K_NEW_OBJECT and modified geometries
     *@return the number of deleted geometries
     */
    public int deleteAllGeometries() {
        int res = 0;
        KaboumGeometryGlobalDescriptor tmpGGD;
        
        for (int i = 0; i < this.size(); i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if (!tmpGGD.isModified() && !tmpGGD.id.equals(Kaboum.K_NEW_GEOMETRY)) {
                res++;
                this.removeElementAt(i--);
            }
        }
        this.visibleGeometries = new KaboumGeometryGlobalDescriptor[0];
        return res;
    }
    
    /**
     *
     * Remove all the elements from this geoObject
     * linked with input Properties Descriptor
     *
     */
    public void removeAllGeometries(KaboumGeometryPropertiesDescriptor tmpPD) {
        KaboumGeometryGlobalDescriptor tmpGGD;
        boolean atLeastOneRemoved = false;
        
        for (int i = 0; i < this.size(); i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if (tmpGGD.pd == tmpPD) {
                tmpGGD.geometry = null;
                atLeastOneRemoved = true;
            }
        }
        if (atLeastOneRemoved) {
            this.visibleGeometries = new KaboumGeometryGlobalDescriptor[0];
        }
    }
    
    
    /**
     * Add a geometry object: either update an existing object or create a new one if an object
     * cannot be found.
     *
     * @param geometry - the geometry to add
     * @param dd - the geometry's KaboumGeometryDisplayDescriptor
     * @param pd - the geometry's KaboumGeometryPropertiesDescriptor
     * @param color - the color of a new DD if given one is null.
     * @param overwrite - true to overwrite an existing modified geometry by the new one, false
     *        to preserve existing modified geometry
     */
    public KaboumGeometryGlobalDescriptor addGeometry(
    KaboumGeometry geometry,
    KaboumGeometryDisplayDescriptor dd,
    KaboumGeometryPropertiesDescriptor pd,
    Color color,
    boolean overwrite) {
        
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        KaboumGeometryGlobalDescriptor ggd = null;
        boolean isNew = true;
        
        for (int i = 0; i < size(); i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if (geometry.id.equals(tmpGGD.id)) {
                isNew = false;
                if (tmpGGD.isModified()) {
                    if (overwrite || geometry.id.equals(Kaboum.K_NEW_GEOMETRY)) {
                        tmpGGD.geometry = geometry;
                    }
                    ggd = tmpGGD;
                    break;
                } else {
                    tmpGGD.geometry = geometry;
                    ggd = tmpGGD;
                    break;
                }
            }
        }
        
        if (ggd == null && isNew) {
            ggd = new KaboumGeometryGlobalDescriptor(geometry, dd, pd, color);
            this.addElement(ggd);
        }
        if (shouldReconstruct) {
            this.reconstructVisibleGeometries();
        }
        
        return ggd;
    }
    
    
    /**
     *
     * Return the position of geometry with identifier id
     *
     * @param id   Identifier
     *
     */
    public KaboumGeometryGlobalDescriptor getGGD(String id) {
        
        if (id == null) {
            return null;
        }
        
        int size = this.size();
        
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        KaboumGeometryGlobalDescriptor ggd = null;
        
        
        for (int i = 0; i < size; i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if (tmpGGD.id.equals(id)) {
                ggd = tmpGGD;
                break;
            }
        }
        
        return ggd;
        
    }
    
    
    /**
     *
     * Return the position of geometry with identifier id
     *
     * @param id   Identifier
     *
     */
    public int getGGDIndex(String id) {
        
        if (id == null) {
            return -1;
        }
        
        int position = -1;
        int size = this.size();
        
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
        for (int i = 0; i < size; i++) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.elementAt(i);
            if (tmpGGD.id.equals(id)) {
                position = i;
                break;
            }
        }
        
        return position;
        
    }
    
    /**
     *
     * Shift one unit up geometry with identifier id
     *
     * @param id   identifier of the geometry
     *
     */
    public void shiftUp(String id) {
        
        int size = this.size();
        
        if (size == 1) {
            return;
        }
        
        int position = this.getGGDIndex(id);
        
        if ((position == -1) || (position == size)) {
            return;
        }
        
        // Insert new object at position + 2
        if (position + 2 > size) {
            this.addElement(this.elementAt(position));
        } else {
            this.insertElementAt(this.elementAt(position), position + 2);
        }
        
        this.remove(position);
        this.reconstructVisibleGeometries();
        
        return;
    }
    
    
    /**
     *
     * Shift one unit down geometry with identifier id
     *
     * @param id   identifier of the geometry
     *
     */
    public void shiftDown(String id) {
        
        int size = this.size();
        
        if (size == 1) {
            return;
        }
        
        int position = this.getGGDIndex(id);
        
        if (position - 1 < 0) {
            return;
        }
        
        this.insertElementAt(this.elementAt(position), position - 1);
        
        this.remove(position + 1);
        this.reconstructVisibleGeometries();
        
        return;
        
    }
    
    
    /**
     *
     * Move geometry with identifier id on top of the KaboumList.
     *
     * @param id   identifier of the geometry
     *
     */
    public void shiftOnTop(String id) {
        
        int size = this.size();
        
        if (size == 1) {
            return;
        }
        
        int position = this.getGGDIndex(id);
        
        if (position == -1) {
            return;
        }
        
        this.addElement(this.elementAt(position));
        
        this.remove(position);
        this.reconstructVisibleGeometries();
        
        return;
        
    }
    
    
    /**
     *
     * Move geometry with identifier id on bottom of the KaboumList.
     *
     * @param id   identifier of the geometry
     *
     */
    public void shiftOnBottom(String id) {
        
        int size = this.size();
        
        if (size == 1) {
            return;
        }
        
        int position = this.getGGDIndex(id);
        
        if (position == -1) {
            return;
        }
        
        this.addElement(this.elementAt(position));
        
        this.remove(position + 1);
        this.reconstructVisibleGeometries();
        
        return;
        
    }
    
    
    /**
     *
     * This method paint tooltip box
     * corresponding to geometry lying under
     * the mouse cursor.
     * Does nothing if no geometries are under
     * the mouse cursor
     *
     * @param g     Graphical context
     * @param mp    Mouse position
     *
     */
    public void paintTooltip(Graphics g, Point mp) {
        
        if (mp == null) {
            return;
        }
        
        double precision = (this.parent.mapServerTools.getRealExtent().dx() / this.parent.screenSize.width ) * KaboumUtil.stoi(this.parent.getOpModeProperty("KABOUM_TOOLTIP_PIXEL_PRECISION"), 1);
        int numGGD = this.getVisibleGeometries().length;
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        String tooltip = null;
        
        try {
            
            for (int i = 0; i < numGGD; i++) {
                
                tmpGGD = this.getVisibleGeometries()[i];
                tooltip = tmpGGD.getTooltip();
                // If tooltip contains one of the special keywords denoting area or perimeter geometry value
                // ($$AREA$$ or $$PERIMETER$$), replaces this keyword by its runtime value, after unit conversion
                if (tooltip != null && (tooltip.contains(KaboumFeatureModes.K_TOOLTIP_AREA_KEY) 
                        || tooltip.contains(KaboumFeatureModes.K_TOOLTIP_PERIMETER_KEY))) {
                    String regex = null;
                    regex = "\\[" + KaboumFeatureModes.K_TOOLTIP_AREA_KEY.replace("$", "\\$") + "\\]";
                    String tmpStr = "" + this.parent.mapServerTools.pm.areaToString(Math.abs(tmpGGD.geometry.getArea())) + 
                    this.parent.mapServerTools.pm.sUnit;
                    tooltip = tooltip.replaceAll(regex, tmpStr);
                    regex = "\\[" + KaboumFeatureModes.K_TOOLTIP_PERIMETER_KEY.replace("$", "\\$") + "\\]";
                    tmpStr = "" + this.parent.mapServerTools.pm.getNumberFormated(tmpGGD.geometry.getPerimeter()) + 
                    this.parent.mapServerTools.pm.strUnit;
                    tooltip = tooltip.replaceAll(regex, String.valueOf(tmpStr));
                }
                //tooltip += " (id: " + tmpGGD.geometry.id + ")";
                if (KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(mp.x, mp.y), tmpGGD.geometry, precision)) {
                    if (tooltip != null) {
                        (new KaboumTextBox(this.parent, mp, tooltip)).paint(g);
                        break;
                    }
                }
            }
        } catch (Exception jse) {
            return;
        }
    }
    
    
    /**
     *
     * This method only paint objects that lie into the current
     * extent.
     *
     */
    public void paint(Graphics g) {
        
        // nri: a new command is available to hide vector objects
        if (this.hideGeometries) return;
        
        if (this.visibleGeometries == null) {
            KaboumUtil.debug("No visible geom to paint");
            return;
        }
        KaboumGeometryGlobalDescriptor tmpGGD;
        KaboumGeometryGlobalDescriptor onTopGeometry = null;
        boolean translucentState = true;

        // NRI: 22 june 2009: manage onTopId coming from an existing drawer that is
        // not yet validated, to allow painting ontopid even if the drawer is lost (for a zoom or a pan for instance)
        if (parent.opModeStatus != null && parent.opModeStatus.getOnTopID().length() > 0) {
            onTopID = parent.opModeStatus.getOnTopID();
        }
        for (int i = 0; i < this.visibleGeometries.length; i++) {
            
            tmpGGD = this.visibleGeometries[i];
            
            // Avoid null geometries to be paint
            if (tmpGGD.geometry == null) {
                continue;
            }
            
            // OnTop geometry is not painted now
            if ( onTopID.equals(tmpGGD.id) ) {
                onTopGeometry = tmpGGD;
                translucentState = tmpGGD.id.equals(Kaboum.K_NEW_GEOMETRY);
                continue;
            }
            
            // Invisible geometry is not painted
            if (!tmpGGD.pd.isVisible()) {
                continue;
            }
            
            if (tmpGGD.geometry.getGeometryType().indexOf("Multi") != -1) {
                for (int j = 0; j < tmpGGD.geometry.getNumGeometries(); j++) {
                    this.paintGeometry(tmpGGD, tmpGGD.geometry.getGeometryN(j), false, g);
                }
            } else {
                this.paintGeometry(tmpGGD, tmpGGD.geometry, false, g);
            }
        }
        if (onTopGeometry != null) {
            if (onTopGeometry.geometry.getGeometryType().indexOf("Multi") != -1) {
                for (int j = 0; j < onTopGeometry.geometry.getNumGeometries(); j++) {
                    //this.paintGeometry(onTopGeometry, onTopGeometry.geometry.getGeometryN(j), true, g);
                    this.paintGeometry(onTopGeometry, onTopGeometry.geometry.getGeometryN(j), translucentState, g);
                }
            } else {
                //this.paintGeometry(onTopGeometry, onTopGeometry.geometry, true, g);
                this.paintGeometry(onTopGeometry, onTopGeometry.geometry, translucentState, g);
            }
        }
        
    }
    
    
    /**
     *
     * Paint a geometry
     *
     */
    private void paintGeometry(KaboumGeometryGlobalDescriptor tmpGGD, KaboumGeometry geometry, boolean isTranslucent, Graphics g) {
        
        // Count the number of points
        int numPoints = geometry.getNumPoints();
        
        if (numPoints == 0) {
            return;
        }
        
        // Set the paint color
        if (tmpGGD.getCurrentColor() == null) {
            return;
        }
        
        g.setColor(tmpGGD.getCurrentColor());
        
        
        //
        // CASE 1: Polygons
        //
        if (geometry.getGeometryType().equals("Polygon")) {
            if (tmpGGD.dd.getFilling() && !isTranslucent && !tmpGGD.isTranslucent) {
                this.paintPolygon(tmpGGD, (KaboumPolygon) geometry, g, true);
            } else {
                this.paintPolygon(tmpGGD, (KaboumPolygon) geometry, g, false);
            }
            return;
        }
        
        //
        // CASE 2: Non-polygonal geometries
        //
        KaboumCoordinate[] internals = geometry.getCoordinates();
        KaboumCoordinate internal;
        int roughness = KaboumUtil.stoi(this.parent.getOpModeProperty("GEOMETRY_ROUGHNESS"), 1);
        
        int x1 = this.parent.mapServerTools.internalToMouseX(internals[0].x);
        int y1 = this.parent.mapServerTools.internalToMouseY(internals[0].y);
        int x2;
        int y2;
        
        for (int i = 1; i < numPoints; i = i + roughness) {
            
            internal = internals[i];
            
            x2 = this.parent.mapServerTools.internalToMouseX(internal.x);
            y2 = this.parent.mapServerTools.internalToMouseY(internal.y);
            
            this.drawLine(g, x1, y1, x2, y2, tmpGGD.dd.getLineWidth());
            
            x1 = this.parent.mapServerTools.internalToMouseX(internal.x);
            y1 = this.parent.mapServerTools.internalToMouseY(internal.y);
            
        }
        
        // Paint the vertices
        this.paintVertices(tmpGGD, internals, g);
        
        return;
    }
    
    
    /**
     *
     * Paint a line with different width
     *
     * @param g    Graphical context
     * @param x1   X1 coordinate
     * @param y1   Y1 coordinate
     * @param x2   X2 coordinate
     * @param y2   Y2 coordinate
     * @param w    Line width
     *
     */
    private void drawLine(Graphics g, int x1, int y1, int x2, int y2, int w) {
        
        int semiw = (int) Math.floor(w / 2);
        int offset = 0;
        if (Math.abs(x1 - x2) < Math.abs(y1 - y2)) {
            for (int i = 0; i < w; i++) {
                offset = i - semiw;
                g.drawLine(x1 + offset, y1, x2 + offset, y2);
            }
        } else {
            for (int i = 0; i < w; i++) {
                offset = i - semiw;
                g.drawLine(x1, y1 + offset, x2, y2 + offset);
            }
        }
        
    }
    
    
    /**
     *
     * Paint the vertices
     *
     */
    private void paintVertices(KaboumGeometryGlobalDescriptor tmpGGD, KaboumCoordinate[] internals, Graphics g) {
        
        
        //
        // Vertices are paint only if their geometry is hilited.
        // Except for Point and MultiPoint geometries which are always
        // paint even if they are not hilited.
        //
        if (!tmpGGD.isHilite()  && !(tmpGGD.geometry.getDimension() == 0)) {
            return;
        }
        
        //
        // Vertices are not paint if their geometry is selected.
        // Except for Point and MultiPoint geometries which are always
        // paint even if they are not hilited.
        //
        if (tmpGGD.isSelected() && !(tmpGGD.geometry.getDimension() == 0)) {
            return;
        }
        
        int numPoints = internals.length;
        
        int roughness = KaboumUtil.stoi(this.parent.getOpModeProperty("GEOMETRY_ROUGHNESS"), 1);
        
        for (int i = 0; i < numPoints; i = i + roughness) {
            this.paintVertices(tmpGGD, internals[i], g);
        }
        
    }
    
    
    /**
     *
     * Paint one vertice
     *
     */
    private void paintVertices(KaboumGeometryGlobalDescriptor tmpGGD, KaboumCoordinate internal, Graphics g) {
        
        
        int tmpX = this.parent.mapServerTools.internalToMouseX(internal.x);
        int tmpY = this.parent.mapServerTools.internalToMouseY(internal.y);
        int pointHeight = tmpGGD.dd.getPointHeight();
        int pointWidth = tmpGGD.dd.getPointWidth();
        
        g.setColor(tmpGGD.dd.getPointColor());
        
        // Selected point
        if (tmpGGD.isSelected()) {
            g.setColor(tmpGGD.dd.getPointHiliteColor());
        }
        
        switch (tmpGGD.dd.getPointType()) {
            case KaboumCoordinate.K_TYPE_CIRCLE:
                g.drawOval((int)(tmpX-pointWidth/2), (int)(tmpY-pointHeight/2), pointWidth, pointHeight);
                break;
            case KaboumCoordinate.K_TYPE_BOX:
                g.fillRect((int)(tmpX-pointWidth/2), (int)(tmpY-pointHeight/2), pointWidth, pointHeight);
                break;
            case KaboumCoordinate.K_TYPE_POINT:
                g.drawOval(tmpX, tmpY, 1, 1);
                break;
            case KaboumCoordinate.K_TYPE_IMAGE:
                g.drawImage(tmpGGD.dd.getPointImage(),
                (int) (tmpX - tmpGGD.dd.getPointImage().getWidth(parent) / 2.0),
                (int) (tmpY - tmpGGD.dd.getPointImage().getHeight(parent) / 2.0), parent);
                if (tmpGGD.isHilite()) {
                    g.setColor(tmpGGD.dd.getPointHiliteColor());
                    g.drawRect((int) (tmpX-pointWidth/2), (int) (tmpY-pointHeight/2), pointWidth, pointHeight);
                }
                break;
                
                // Default is K_TYPE_POINT
            default:
                g.drawOval(tmpX, tmpY, 1, 1);
        }
        
    }
    
    
    /**
     *
     * Paint a polygon (filled or not)
     *
     */
    private void paintPolygon(KaboumGeometryGlobalDescriptor tmpGGD, KaboumPolygon geometry, Graphics g, boolean isFilled) {
        // Number of holes for this geometry
        int numHoles = geometry.getNumInteriorRing();
        
        // Filled case
        if (isFilled) {
            
            // Fill the shell
            if (! tmpGGD.isHilite()) {
                // Change NRI: fill with modified color
                if (tmpGGD.isModified()) {
                    g.setColor(tmpGGD.dd.getModifiedColor());
                } else {
                    g.setColor(tmpGGD.dd.getFillColor());
                }
            }
            // gets arranged coordinates to display holes in the right manner
            g.fillPolygon(this.parent.mapServerTools.internalToPolygon(geometry.getFilledCoordinates()));
            g.setColor(tmpGGD.getCurrentColor());
        }
        
        // Paint the shell
        this.paintGeometry(tmpGGD, geometry.getExteriorRing(), false, g);
        
        // Paint the holes
        if (!isFilled) {
            for (int i = 0; i <numHoles; i++) {
                this.paintGeometry(tmpGGD, geometry.getInteriorRingN(i), false, g);
            }
        }
        
        // Paint the shell vertices
        this.paintVertices(tmpGGD, geometry.getExteriorRing().getCoordinates(), g);
        
        // Paint the holes vertices
        for (int i = 0; i < numHoles; i++) {
            this.paintVertices(tmpGGD, geometry.getInteriorRingN(i).getCoordinates(), g);
        }
        
    }
    
    
    /**
     *
     * Reset the onTopID to default value
     *
     */
    public void resetOnTopID() {
        this.onTopID = KaboumGGDIndex.RESERVED_ID;
    }
    
    
}
