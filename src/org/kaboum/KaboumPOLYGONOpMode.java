/*
 *
 * Class KaboumPOINTOpMode from the Kaboum project.
 * Class to create or modify a KaboumPoint or KaboumMultiPoint within the applet.
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
 * For more information, contact:
 *
 *     Jerome Gasperi @
 *
 *     SCOT
 *     Parc Technologique du canal
 *     8, rue Hermes
 *     31526 Ramonville Cedex
 *     France
 *
 *     www.scot.fr
 *
 */
package org.kaboum;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumWKTReader;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.geom.KaboumGeometryFactory;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPolygon;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.geom.KaboumLinearRing;
import org.kaboum.util.KaboumFeatureModes;


/**
 *
 * Create or modify KaboumPoint object within the applet.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumPOLYGONOpMode extends KaboumGEOMETRYOpMode {
    
    /* CONSTANTS */
    private final int FIRST_POINT_OK = 1;
    private final int FIRST_POINT_NOT_IN_SURROUNDING = -1;
    private final int FIRST_POINT_IN_GEOMETRY = -10;
    
    /** Current geometry where a hole is drawn */
    private static KaboumGeometry holeFatherGeometry = null;
    
    /** if true current geometry drawn is a hole */
    private static boolean holeDrawing = false;
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumPOLYGONOpMode(Kaboum parent) {
        super(parent);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param id Pre-selected object
     *
     */
    public KaboumPOLYGONOpMode(Kaboum parent, String id) {
        super(parent, id);
    }
    
    
    public boolean isValid(KaboumGeometryGlobalDescriptor ggd) {
        if (ggd.geometry != null && ggd.geometry.getGeometryType().indexOf("Polygon") != -1) {
        //if (ggd.geometry.getGeometryType().indexOf("Polygon") != -1) {
            return true;
        }
        return false;
    }
    
    /**
     * Resolve MOUSE LEFT CLICK  action : Add a point
     *
     * @param mouse mouse position
     * @param action action to resolve
     *
     */
    protected void resolveMouseAddPoint(Point mouse, int action) {
        
        if (this.parent.agp.activeGGD == null) {
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        //
        // Conversion (x,y) image ----> map
        // This is a bit tricky.
        // To avoid aproximation problem, we must
        // check if the new calculated coordinate does
        // not lie inside an existing closed object.
        // If this is the case, we use a LR (lower rigth)
        // calculation instead of a UL (upper left) calculation
        // for the image ---> map transfom
        //
        //KaboumCoordinate internal = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
        KaboumCoordinate internal = null;
        if (this.parent.agp.activeGGD.pd.isVertexSnappable() && 
                this.currentSnapCoord != null && 
                ! parent.agp.activeSimpleGeometry.isEmpty()) {
            // use a reference to build a topologic edition tool...
            //internal = new KaboumCoordinate(this.currentSnapCoord);
            internal = (this.currentSnapCoord);
        } else {
            internal = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
        }
        
        
        // A hole MUST BE inside is father geometry
        if (this.holeDrawing) {
            if (!KaboumAlgorithms.isPointInPolygon(internal, this.holeFatherGeometry)) {
                return;
            }
        } else {
            
            //
            // The first point must lie inside DRAWN_INSIDE geometries
            // and outside other geometries
            //
            if (this.parent.agp.activeSimpleGeometry.getCoordinates().length == 0) {
                int result = this.firstPointIsValid(internal);
                if (result == this.FIRST_POINT_IN_GEOMETRY) {
                    this.parent.kaboumResult("ALERT|GEOMETRY_FIRST_POINT_IS_INSIDE_GEOMETRY");
                    return;
                } else if (result == this.FIRST_POINT_NOT_IN_SURROUNDING) {
                    this.parent.kaboumResult("ALERT|GEOMETRY_FIRST_POINT_IS_NOT_INSIDE_SURROUNDING_GEOMETRY");
                    return;
                }
                
            }
            
            
        }
        
        // Add the new point
        this.parent.agp.activeSimpleGeometry.addCoordinate(internal);
        
        // Save the last point ID, i.e. the current point ID
        this.parent.agp.activePointClickedPosition = 0;
        
        // La liste a ete modifiee
        this.parent.agp.activeGGD.setModified(true);
        
        // Repaint
        this.parent.repaint();
        
        return;
        
    }
    
    
    /**
     *
     * actionPerformed event e : NewObject
     *
     * @param e action to perform
     */
    protected void actionPerformedNewObject(ActionEvent e) {
        
        int position = this.parent.GGDIndex.getGGDIndex(Kaboum.K_NEW_GEOMETRY);
        KaboumGeometry geometry = null;
        //KaboumPolygon[] polygons = new KaboumPolygon[1];
        
        if (position == -1) {
            /*
            try {
                KaboumWKTReader wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
                polygons[0] = (KaboumPolygon) wktReader.read("POLYGON EMPTY");
                geometry = KaboumGeometryFactory.createMultiPolygon(polygons);
                geometry.id = Kaboum.K_NEW_GEOMETRY;
            } catch (Exception jse) {
                KaboumUtil.debug(" WARNING ! :  error creating geometry ");
                return;
            }
             */
            geometry = getEmptyGeometry();
            
            if (geometry != null) {
                KaboumGeometryGlobalDescriptor ggd = this.parent.GGDIndex.addGeometry(
                        geometry,
                        this.parent.currentDD,
                        this.parent.currentPD,
                        null,
                        true);
                
                this.setActiveGGD(ggd);
                this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(0);
            }
        }
        
        return;
        
    }
    
    
    /**
     *
     * actionPerformed event e : RemoveSimpleGeometry
     *
     * @param e action to perform
     *
     */
    public void actionPerformedRemoveGeometryWithinCollection(ActionEvent e) {
        KaboumUtil.debug("removing object in the collection");
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        this.parent.agp.activeGGD.geometry.removeGeometry(this.parent.agp.activeSimpleGeometry);
        this.parent.agp.activeGGD.setModified(true);
        
        if (this.parent.agp.activeGGD.geometry.getNumGeometries() == 0) {
            /*
            KaboumPolygon[] polygons = new KaboumPolygon[1];
            try {
                KaboumWKTReader wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
                polygons[0] = (KaboumPolygon) wktReader.read("POLYGON EMPTY");
                this.parent.agp.activeGGD.geometry = KaboumGeometryFactory.createMultiPolygon(polygons);
            } catch (Exception jse) {
                KaboumUtil.debug(" WARNING ! :  error creating geometry ");
                return;
            }
             */
            this.parent.agp.activeGGD.geometry = getEmptyGeometry();
            this.parent.agp.activeGGD.setEdited(true);
            this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(0);
        }
        
        this.repaint();
        
        return;
        
    }
    
    
    /**
     *
     * actionPerformed event e : Add a geometry within this collection
     *
     * @param e action to perform
     *
     */
    protected void actionPerformedAddGeometryWithinCollection(ActionEvent e) {
        
        if (this.parent.agp.activeGGD == null) {
            return;
        }
        
        if (this.parent.agp.activeGGD.geometry == null) {
            return;
        }
        
        int numGeoms = this.parent.agp.activeGGD.geometry.getNumGeometries();
        
        
        KaboumPolygon[] polygons = new KaboumPolygon[numGeoms + 1];
        
        for (int i = 0; i < numGeoms; i++) {
            polygons[i] = (KaboumPolygon) this.parent.agp.activeGGD.geometry.getGeometryN(i);
        }
        
        polygons[numGeoms] = new KaboumPolygon(null);
        this.parent.agp.activeGGD.geometry = KaboumGeometryFactory.createMultiPolygon(polygons);
        //NRI: added geometry id setting to avoid null id
        this.parent.agp.activeGGD.geometry.id = Kaboum.K_NEW_GEOMETRY;
        this.parent.agp.activeGGD.setEdited(true);
        this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(numGeoms);
        
        this.parent.repaint();
        
        return;
        
    }
    
    
    /**
     *
     * Add a hole within this geometry.
     * This has no sense for other geometries than Polygon
     *
     */
    protected void actionPerformedAddHoleWithinGeometry(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        int numHoles = ((KaboumPolygon) this.parent.agp.activeSimpleGeometry).getNumInteriorRing();
        
        KaboumLinearRing[] holes = new KaboumLinearRing[numHoles + 1];
        
        for (int i = 0; i < numHoles; i++) {
            holes[i] = (KaboumLinearRing) ((KaboumPolygon) this.parent.agp.activeSimpleGeometry).getInteriorRingN(i);
        }
        
        holes[numHoles] = new KaboumLinearRing(null);
        this.parent.agp.activeGGD.setEdited(true);
        ((KaboumPolygon) this.parent.agp.activeSimpleGeometry).setHoles(holes);
        this.holeFatherGeometry = this.parent.agp.activeSimpleGeometry;
        this.holeDrawing = true;
        this.parent.agp.activeSimpleGeometry = ((KaboumPolygon) this.parent.agp.activeSimpleGeometry).getInteriorRingN(numHoles);
        
        this.parent.repaint();
        
        
        return;
    }
    
    
    /**
     *
     * ActionPerformed event e : PreValidateObject
     *
     * @param e action to perform
     *
     */
    public void actionPerformedPreValidateObject(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry.getExteriorNumPoints() > this.parent.agp.activeGGD.geometry.getDimension()) {
            
            // Check auto-intersection
            if (!this.ringIsvalid(this.parent.agp.activeSimpleGeometry.getCoordinates())) {
                this.parent.kaboumResult("ALERT|GEOMETRY_AUTO_INTERSECT");
                return;
            }
            this.parent.agp.activeSimpleGeometry.addCoordinate(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates()[0]);
            this.parent.agp.activeGGD.geometry.normalize();
            this.parent.agp.activeGGD.setEdited(false);
            this.isMenuOn = false;
            this.holeDrawing = false;
            this.holeFatherGeometry = null;
            this.parent.repaint();
        }
    }
    
    
    /**
     *
     * actionPerformed event e : RemovePoint
     *
     * @param e action to perform
     */
    public void actionPerformedRemovePoint(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.parent.agp.activePointClickedPosition != -1) {
            
            this.saveCurrentCoordinates();
            
            int numPoints = this.parent.agp.activeSimpleGeometry.getExteriorCoordinates().length;
            
            // Special case : first or last point
            if ((this.parent.agp.activePointClickedPosition == 0) || (this.parent.agp.activePointClickedPosition == numPoints - 1)) {
                this.removeCoordinateAt(numPoints - 1);
                this.removeCoordinateAt(0);
                this.parent.agp.activeSimpleGeometry.addCoordinate(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates()[0]);
            } else {
                this.removeCoordinateAt(this.parent.agp.activePointClickedPosition);
            }
            this.parent.agp.activePointClickedPosition = -1;
            this.parent.agp.activeGGD.setModified(true);
            
            if (this.parent.agp.activePointClickedPosition < numPoints) {
                this.intersect(this.parent.agp.activePointClickedPosition);
            } else {
                this.intersect(0);
            }
        }
    }
    
    
    /**
     *
     * Return the position of a point in a coordinate list corresponding
     * to a (x,y) mouse click within a box of w * h.
     * Return -1 if no points are found
     *
     * @param mouse           mouse position
     *
     */
    protected int selectCoordinatePosition(Point mouse) {
        
        int idControlPoint = -1;
        
        int numGeoms = this.parent.agp.activeGGD.geometry.getNumGeometries();
        int numHoles = 0;
        boolean holeFind = false;
        
        KaboumCoordinate[] internals;
        KaboumPolygon tmpPolygon;
        
        for (int i = 0; i < numGeoms; i++) {
            
            tmpPolygon = (KaboumPolygon) this.parent.agp.activeGGD.geometry.getGeometryN(i);
            
            internals = tmpPolygon.getExteriorCoordinates();
            idControlPoint = this.selectAbsoluteCoordinatePosition(mouse, internals);
            
            if (idControlPoint != -1) {
                this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(i);
                break;
            }
            
            // Check the holes
            numHoles = tmpPolygon.getNumInteriorRing();
            
            for (int j = 0; j < numHoles; j++) {
                
                internals = tmpPolygon.getInteriorRingN(j).getExteriorCoordinates();
                idControlPoint = this.selectAbsoluteCoordinatePosition(mouse, internals);
                
                if (idControlPoint != -1) {
                    this.parent.agp.activeSimpleGeometry = tmpPolygon.getInteriorRingN(j);
                    holeFind = true;
                    break;
                }
                
            }
            
            if (holeFind) {
                break;
            }
            
        }
        
        return idControlPoint;
        
    }
    
    
    public void paint(Graphics g) {
        if (this.parent.agp.activeGGD != null) {
            
            if (this.parent.agp.activeGGD.isEdited()) {
                
                if (this.parent.agp.activeSimpleGeometry != null) {
                    
                    int numPoints = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
                    
                    if (numPoints < 1) {
                        return;
                    }
                    
                    // Set the paint color
                    g.setColor(this.parent.agp.activeGGD.getCurrentColor());
                    
                    Point start = this.parent.mapServerTools.internalToMouseXY(this.parent.agp.activeSimpleGeometry.getCoordinates()[numPoints - 1]);
                    Point end = this.parent.mapServerTools.internalToMouseXY(this.parent.agp.activeSimpleGeometry.getCoordinates()[0]);
                    
                    // the current cursor position: in case of snapping, this is not the mouse position
                    // but the vertex snapped to
                    Point currentPosition = this.parent.agp.activeGGD.pd.isVertexSnappable() && this.currentSnapCoord != null ?
                        this.parent.mapServerTools.internalToMouseXY(this.currentSnapCoord) : 
                        this.currentMousePosition;
                    
                    g.drawLine(start.x, start.y, currentPosition.x, currentPosition.y);
                    g.drawLine(currentPosition.x, currentPosition.y, end.x, end.y);
                }
            }
        }
    }
    
    
    public int firstPointIsValid(KaboumCoordinate internal) {
        
        if (this.parent.agp.activeGGD == null) {
            return this.FIRST_POINT_OK;
        }
        
        if (!this.parent.agp.activeGGD.pd.isComputed()) {
            return this.FIRST_POINT_OK;
        }
        
        int numGeoms = this.parent.GGDIndex.getVisibleGeometries().length;
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        boolean isInside = false;
        boolean thereIsSurrounding = false;
        
        for (int i = 0; i < numGeoms; i++) {
            
            tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
            
            if (tmpGGD.id.equals(this.parent.agp.activeGGD.id)) {
                continue;
            }
            
            if (!tmpGGD.pd.isComputed()) {
                continue;
            }
            
            if (tmpGGD.pd.isSurrounding()) {
                thereIsSurrounding = true;
            }
            
            if (KaboumAlgorithms.isPointInPolygon(internal, tmpGGD.geometry)) {
                
                if (!tmpGGD.pd.isSurrounding()) {
                    return this.FIRST_POINT_IN_GEOMETRY;
                } else {
                    isInside = true;
                }
                
            }
            
        }
        
        if (!thereIsSurrounding) {
            return this.FIRST_POINT_OK;
        }
        
        if (isInside) {
            return this.FIRST_POINT_OK;
        }
        
        return this.FIRST_POINT_NOT_IN_SURROUNDING;
        
    }
    
    
    /**
     *
     * Check if a ring is valid.
     * A ring is valid if it does not intersect itself
     *
     */
    public boolean ringIsvalid(KaboumCoordinate[] internals) {
        return !KaboumAlgorithms.autoIntersect(internals);
    }
    
    /**
     * Gets a new, empty polygon or multipolygon geometry, according to the PARAM_MULTIALLOWED parameter
     * @return an empty polygon if is set to false, an empty multipolygon otherwise
     */
    public KaboumGeometry getEmptyGeometry() {
        KaboumGeometry geometry = null;
        KaboumPolygon[] polygons = new KaboumPolygon[1];
        KaboumWKTReader wktReader = null;
        try {
            wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
            polygons[0] = (KaboumPolygon) wktReader.read("POLYGON EMPTY");
            if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_MULTIALLOWED), true)) {
                geometry = KaboumGeometryFactory.createMultiPolygon(polygons);
            } else {
                geometry = polygons[0];
            }
            geometry.id = Kaboum.K_NEW_GEOMETRY;
            // sets tooltip according to global parameter, to display surface and area while digitalization.
            if (KaboumUtil.stob((String)this.parent.opModePropertiesHash.get("TOOLTIP_DISPLAY_AREA"), false)) {
                geometry.setToolTip(parent.defaultLang.getString("STATUS_SURFACE")
                        + "[" + KaboumFeatureModes.K_TOOLTIP_AREA_KEY + "]");
            }
            if (KaboumUtil.stob((String)this.parent.opModePropertiesHash.get("TOOLTIP_DISPLAY_PERIMETER"), false)) {
                geometry.setToolTip(geometry.getToolTip() + " " + parent.defaultLang.getString("STATUS_PERIMETER")
                        + "[" +  KaboumFeatureModes.K_TOOLTIP_PERIMETER_KEY  + "]");
            }
            
        } catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  error creating geometry ");
            return null;
        }
        return geometry;
    }
}

