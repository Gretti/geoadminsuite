/*
 *
 * Class KaboumGEOMETRYOpMode from the Kaboum project.
 * Abstract class to create or modify KaboumGeometry within the applet.
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

 *
 */
package org.kaboum;

import java.awt.PopupMenu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.Point;
import java.awt.Graphics;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumWKTWriter;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.geom.KaboumGGDIndex;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumWKTReader;


/**
 *
 * Abstract class to create or modify KaboumGeometry within the applet.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public abstract class KaboumGEOMETRYOpMode extends KaboumOpMode {
    
    /** CONSTANTS */
    public static final String PARAM_DRAGALLOWED = "GEOMETRY_ALLOW_TO_DRAG_POINT";
    public static final String PARAM_SUPPRESSIONALLOWED = "GEOMETRY_ALLOW_TO_SUPPRESS_OBJECT";
    public static final String PARAM_PRECISION = "GEOMETRY_COMPUTATION_PRECISION";
    public static final String PARAM_DCTIME = "GEOMETRY_DOUBLE_CLICK_TIME";
    public static final String PARAM_PIXELPRECISION = "GEOMETRY_PIXEL_PRECISION";
    public static final String PARAM_MULTIALLOWED = "GEOMETRY_ALLOW_GEOMETRY_COLLECTION";
    public static final String PARAM_HOLEALLOWED = "GEOMETRY_ALLOW_HOLE_IN_GEOMETRY";
    public static final String PARAM_SUPPRESSIONWITHINCOLLECTIONALLOWED = "GEOMETRY_ALLOW_TO_SUPPRESS_GEOMETRY_WITHIN_COLLECTION";
    public static final String PARAM_CANCELALLOWED = "GEOMETRY_ALLOW_TO_CANCEL_GEOMETRY";
    public static final String PARAM_VALIDATETODATASTORE = "SERVER_VALIDATE_TO_DATASTORE";
    public static final String PARAM_DELETEFROMDATASTORE = "SERVER_DELETE_FROM_DATASTORE";
    public static final String PARAM_SNAPPINGTOLERANCE = "GEOMETRY_SNAPPING_TOLERANCE";
    
    
    public static String[] getParametersList() {
        String[] list = new String[12];
        list[0] = PARAM_DRAGALLOWED;
        list[1] = PARAM_SUPPRESSIONALLOWED;
        list[2] = PARAM_PRECISION;
        list[3] = PARAM_DCTIME;
        list[4] = PARAM_PIXELPRECISION;
        list[5] = PARAM_MULTIALLOWED;
        list[6] = PARAM_HOLEALLOWED;
        list[7] = PARAM_SUPPRESSIONWITHINCOLLECTIONALLOWED;
        list[8] = PARAM_CANCELALLOWED;
        list[9] = PARAM_VALIDATETODATASTORE;
        list[10] = PARAM_DELETEFROMDATASTORE;
        list[13] = PARAM_SNAPPINGTOLERANCE;
        return list;
    }
    
    /** Private constant */
    protected final int SELECT_OBJECT = 0;
    protected final int ADD_POINT = 1;
    protected final int SHOW_MENU = 2;
    protected final int DRAG_POINT = 3;
    protected final int VALIDATE_OBJECT = 4;
    
    
    /** Reference to the parent applet */
    protected Kaboum parent;
    
    /** Popup menu (activated by right click mouse) */
    protected PopupMenu pop = null;
    
    /** Current drag point position */
    protected int dragPointPosition = -1;
    
    /** Last mouse position */
    protected Point freezedMousePosition;
    
    /** Mouse position */
    protected Point currentMousePosition;
    
    /** Current mouse event */
    protected MouseEvent pmc = null;
    
    /** Double click time */
    protected int dctime = 500;
    
    /** True if the validate/undo menu is on */
    protected boolean isMenuOn = false;
    
    /** True if mouse is dragged */
    protected boolean dragStatus = false;
    
    /** True if the mouse dragged a point for the first time */
    protected boolean firstPointDragged = true;
    
    /** the vertex coordinate the closest to the mouse position in case of snapping mode for
     * the current PD class. 
     * Event handlers can use this variable to add/update coordinates of the edited geometry with
     * the snapped vertex
     */
    protected KaboumCoordinate currentSnapCoord = null;

    protected KaboumWKTWriter wktWriter = null;
    protected KaboumWKTReader wktReader = null;


    /** the string representing the geom's WKT to store in case of cancel
     * fixme: find a smarter way to do that: copy geom object directly for instance
     */
    protected String undoGeomWkt;
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRYOpMode(Kaboum parent) {
        this(parent, null);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param id Pre-selected object
     *
     */
    public KaboumGEOMETRYOpMode(Kaboum parent, String id) {
        
        this.parent = parent;
        
        // Add MouseListener
        this.parent.addMouseListener(this);
        this.parent.addMouseMotionListener(this);
        
        // Popup menu stuff
        this.pop = new PopupMenu();
        this.pop.setFont(this.parent.getFont());
        this.pop.addActionListener(this);
        this.parent.add(this.pop);
        
        // Double click time
        this.dctime = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_DCTIME), this.dctime);
        
        // Mouse appearence
        this.parent.setCursor("CROSSHAIR");
        
        if (id != null) {
            // Pre-selected geometry referenced by identifier id.
            // This geometry is selected ONLY if its type is valid
            // within the current opMode. For example, a Polygon Geometry
            //
            KaboumGeometryGlobalDescriptor tmpGGD = this.parent.GGDIndex.getGGD(id);
            
            if (tmpGGD != null) {
                if (this.isValid(tmpGGD)) {
                    this.setActiveGGD(tmpGGD);
                    this.parent.repaint();
                }
            }
        }
        if (this.parent.agp.activeGGD != null) {
            if (!this.isValid(this.parent.agp.activeGGD)) {
                
                if (this.parent.agp.activeGGD.id.equals(Kaboum.K_NEW_GEOMETRY)) {
                    this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
                    this.reset();
                } else {
                    KaboumGeometryGlobalDescriptor tmpGGD = this.parent.GGDIndex.getGGD(Kaboum.K_NEW_GEOMETRY);
                    if (tmpGGD != null) {
                        this.setActiveGGD(tmpGGD);
                        this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
                    } else {
                        this.parent.agp.activeGGD.setEdited(false);
                        this.parent.agp.activeGGD.setHilite(false);
                    }
                    this.reset();
                }
                this.parent.repaint();
            }
        }
        
        // If input geometry is a new one, pre-create it
        if (id != null) {
            if (Kaboum.K_NEW_GEOMETRY.equals(id)) {
                this.actionPerformedNewObject(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "NEW"));
            }
        }

        // build the WKT writer.
        this.wktWriter = new KaboumWKTWriter(this.parent.getPrecisionModel());
        this.wktReader = new KaboumWKTReader(this.parent.getPrecisionModel());
    }
    
    
    /**
     *
     * Set the input geometry as the activeGGD
     *
     */
    public void setActiveGGD(KaboumGeometryGlobalDescriptor ggd) {
        
        if (ggd == null) {
            return;
        }
        
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
        // Un-hilite all geometrical objects except the input one
        for (int i = 0; i < numGGD; i++) {
            tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
            //tmpGGD.setHilite(false);
            tmpGGD.setSelected(false);
        }
        
        this.parent.agp.activeGGD = ggd;
        if (this.parent.agp.activeGGD.geometry != null) {
            if (this.parent.agp.activeGGD.geometry.getNumPoints() == 0) {
                this.parent.agp.activeGGD.setEdited(true);
            }
        }
        this.parent.agp.activeGGD.setHilite(true);
        this.parent.activeGGD = ggd;
        KaboumGGDIndex.onTopID = this.parent.agp.activeGGD.id;
        this.parent.agp.activePointClickedPosition = -1;
    }
    
    
    /**
     *
     * Unselect all geometries
     *
     */
    private void unselectAll() {
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
        // Un-hilite all geometrical objects except the input one
        for (int i = 0; i < numGGD; i++) {
            tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
            tmpGGD.setSelected(false);
        }
        
        this.parent.agp.activePointClickedPosition = -1;
        this.parent.agp.activeGGD = null;
        this.parent.activeGGD = null;
        this.parent.agp.activeSimpleGeometry = null;
        this.parent.agp.savedCoordinates = null;
        this.parent.GGDIndex.onTopID = KaboumGGDIndex.RESERVED_ID;        
    }
    
    /**
     *
     * Reset the current active geometry
     *
     */
    protected void reset() {
        this.parent.agp.reset();
    }
    
    public void  destroyEvent() {
        parent.removeMouseListener(this);
        parent.removeMouseMotionListener(this);
    }
    
    
    /**
     *
     * actionPerformed event e
     *
     * @param e action to perform
     */
    public void actionPerformed(ActionEvent e) {
        
        
        if (e.getActionCommand().equals("ADD_POINT")) {
            actionPerformedAddPoint(e);
        } else if (e.getActionCommand().equals("REMOVE_POINT")) {
            actionPerformedRemovePoint(e);
        } else if (e.getActionCommand().equals("VALIDATE_OBJECT")) {
            actionPerformedValidateObject(e);
        } else if (e.getActionCommand().equals("NEW_OBJECT")) {
            actionPerformedNewObject(e);
        } else if (e.getActionCommand().equals("REMOVE_OBJECT")) {
            actionPerformedRemoveObject(e);
        } else if (e.getActionCommand().indexOf("TOPOLOGY") != -1) {
            actionPerformedTopology(e);
        } else if (e.getActionCommand().equals("PRE_VALIDATE_OBJECT")) {
            actionPerformedPreValidateObject(e);
        } else if (e.getActionCommand().equals("UNDO")) {
            actionPerformedUndo(e);
        } else if (e.getActionCommand().equals("ADD_GEOMETRY_WITHIN_COLLECTION")) {
            actionPerformedAddGeometryWithinCollection(e);
        } else if (e.getActionCommand().equals("ADD_HOLE_WITHIN_GEOMETRY")) {
            actionPerformedAddHoleWithinGeometry(e);
        } else if (e.getActionCommand().equals("REMOVE_GEOMETRY_WITHIN_COLLECTION")) {
            actionPerformedRemoveGeometryWithinCollection(e);
        } else if (e.getActionCommand().equals("CANCEL_GEOMETRY")) {
            actionPerformedCancelGeometry(e);
        }
        this.pop.removeAll();
        this.parent.repaint();
    }
    
    
    /**
     *
     * Resolve Mouse Event action
     *
     * @param mouse  mouse point
     * @param action action to resolve
     *
     */
    protected void resolveMouseEvent(Point mouse, int action) {
        
        this.freezedMousePosition = new Point(mouse);
        
        // CASE ONE : CONTROL CLICK (Select an object)
        if (action == this.SELECT_OBJECT) {
            resolveMouseSelectObject(mouse, action);
        }
        
        // CASE TWO : LEFT CLICK (Add a point)
        else if (action == this.ADD_POINT) {
            resolveMouseAddPoint(mouse, action);
        }
        
        // CASE THREE : RIGHT OR MIDDLE CLICK (Show menu)
        else if (action == this.SHOW_MENU) {
            resolveMouseShowMenu(mouse, action);
        }
        
        // CASE FOUR : DRAG BUTTON (Drag a point)
        else if (action == this.DRAG_POINT) {
            resolveMouseDragPoint(mouse, action);
        }
        
        // CASE FIVE : RIGHT BUTTON (Validate object)
        else if (action == this.VALIDATE_OBJECT) {
            resolveMouseValidateObject(mouse, action);
        }
        
        return;
    }
    
    
    /**
     *
     * ActionPerformed event e : PreValidateObject
     *
     * @param e action to perform
     */
    public void actionPerformedPreValidateObject(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry.getExteriorNumPoints() > this.parent.agp.activeGGD.geometry.getDimension()) {
            this.parent.agp.activeGGD.geometry.normalize();
            this.parent.agp.activeGGD.setEdited(false);
            this.isMenuOn = false;
            this.parent.repaint();
        }
    }
    
    
    /**
     *
     * actionPerformed event e : Undo
     *
     * @param e action to perform
     */
    public void actionPerformedUndo(ActionEvent e) {
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.removeLastCoordinate()) {
            this.isMenuOn = false;
            parent.repaint();
        }
        
    }
    
    
    /**
     *
     * actionPerformed event e : AddPoint
     *
     * @param e action to perform
     */
    public void actionPerformedAddPoint(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry.getExteriorNumPoints() > 1) {
            
            KaboumCoordinate currentCoordinateClicked = this.parent.mapServerTools.mouseXYToInternal(this.freezedMousePosition.x, this.freezedMousePosition.y);
            
            // Get the closest point id
            int currentId = KaboumAlgorithms.getClosestPointPosition(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates(), currentCoordinateClicked);
            
            this.saveCurrentCoordinates();
            
            if (currentId != -1) {
                this.insertCoordinate(currentCoordinateClicked, currentId);
                this.intersect(currentId);
            }
            
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
            this.removeCoordinateAt(this.parent.agp.activePointClickedPosition);
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
     * actionPerformed event e : ValidateObject
     *
     * @param e action to perform
     */
    public void actionPerformedValidateObject(ActionEvent e) {
        if (this.parent.agp.activeGGD != null) {
            
            if (this.parent.agp.activeGGD.geometry.getNumPoints() > this.parent.agp.activeGGD.geometry.getDimension()) {
                // Check the validity of geometry (non intersection for closed geometry)
                if (!KaboumAlgorithms.geometryIsValid(this.parent.agp.activeGGD.geometry)) {
                    this.parent.kaboumResult("ALERT|GEOMETRY_AUTO_INTERSECT");
                    return;
                }
                
                // Check intersection with other geometries
                if (this.parent.agp.activeGGD.pd.isComputed()) {
                    
                    // Check the validity of geometry (surrounding or not surrounding)
                    if (KaboumAlgorithms.geometrySurroundsOther(this.parent.agp.activeGGD, this.parent.GGDIndex.getVisibleGeometries())) {
                        this.parent.kaboumResult("ALERT|GEOMETRY_SURROUNDS_OTHER_GEOMETRY");
                        return;
                    }
                    
                    // Check the intersection of geometry with other geometry
                    if (KaboumAlgorithms.geometryIntersectsOther(this.parent.agp.activeGGD, this.parent.GGDIndex.getVisibleGeometries())) {
                        this.parent.kaboumResult("ALERT|GEOMETRY_INTERSECTS_OTHER_GEOMETRY");
                        return;
                    }
                    
                    // Check the validity of geometry (geometry not inside other geometries)
                    if (KaboumAlgorithms.geometryIsInsideOther(this.parent.agp.activeGGD, this.parent.GGDIndex.getVisibleGeometries())) {
                        this.parent.kaboumResult("ALERT|GEOMETRY_IS_INSIDE_OTHER_GEOMETRY");
                        return;
                    }
                }
                String curOpModeName = this.parent.currentOpMode.getOpModeName();
                parent.standbyOn();
                pop.removeAll();

                // validates to the server only if a feature server is available and if corresponding
                // parameter is set to true
                if (parent.featureServerTools != null &&
                        KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_VALIDATETODATASTORE)) ) {
                    
                    // and set its modified status to false, now it is validated
                    this.parent.agp.activeGGD.setModified(false);
                    Vector vec = new Vector(1);
                    vec.addElement(this.parent.agp.activeGGD);
                    this.parent.featureServerTools.validateAndRemove(this.parent.agp.activeGGD, vec, curOpModeName);
                    reset();
                    destroyEvent();
                } else {
                    // classic JS mechanism to validate geometry
                    //
                    // Result form:
                    // GEOMETRY|<Geometry Identifier>|<Geometry perimeter>|<Geometry surface>|<Geometry WKT>
                    //
                    String wkt = null;
                    try {
                        KaboumWKTWriter wktWriter = new KaboumWKTWriter(this.parent.mapServerTools.pm);
                        wkt =  wktWriter.write(this.parent.agp.activeGGD.geometry);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        KaboumUtil.debug(" ERROR ! Cannot write WKT string");
                        wkt = "EMPTY";
                    }
                    
                    String tmpStr = "GEOMETRY|" +
                            this.parent.agp.activeGGD.id +
                            "|" +
                            this.parent.mapServerTools.pm.perimeterToString(this.parent.agp.activeGGD.geometry.getPerimeter()) +
                            "|" +
                            this.parent.mapServerTools.pm.areaToString(this.parent.agp.activeGGD.geometry.getArea()) +
                            "|" +
                            wkt;
                    // In all cases remove the currentGeoObject, and wait for javascript
                    // to re-send it
                    //
                    this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
                    this.reset();
                    parent.kaboumResult(tmpStr);
                    this.destroyEvent();
                }
                return;
            }
        }
    }
    
    
    /**
     *
     * actionPerformed event e : RemoveObject
     *
     * @param e action to perform
     */
    public void actionPerformedRemoveObject(ActionEvent e) {
        if (this.parent.agp.activeGGD != null) {
            parent.standbyOn();
            pop.removeAll();

            if (parent.featureServerTools != null &&
                    KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_DELETEFROMDATASTORE))) {
                // ask server to do the job
                parent.featureServerTools.removeGeometry(this.parent.agp.activeGGD.id, this.parent.agp.activeGGD.dd.name, "POLYGON");
                this.reset();
                this.destroyEvent();
            } else if (this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id)) {
                // process with JS validation
                String tmpStr = "GEOMETRY|" + this.parent.agp.activeGGD.id + "|REMOVE";
                this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
                this.reset();
                // Send the result to javascript
                this.parent.kaboumResult(tmpStr);
                this.destroyEvent();
            }
        }
    }
    
    
    /**
     *
     * actionPerformed event e : Topology
     *
     * @param e action to perform
     */
    public void actionPerformedTopology(ActionEvent e) {
        
        if (this.parent.agp.activeGGD != null) {
            
            if (this.parent.agp.activeGGD.geometry.getNumPoints() > this.parent.agp.activeGGD.geometry.getDimension()) {
                
                this.parent.standbyOn();
                pop.removeAll();
                
                //
                // Result form:
                // ACTION|<Geometry identifier>|<Geometry perimeter>|<Geometry area>|<Geometyr WKT>
                //
                String wkt = null;
                try {
                    KaboumWKTWriter wktWriter = new KaboumWKTWriter(this.parent.mapServerTools.pm);
                    wkt =  wktWriter.write(this.parent.agp.activeGGD.geometry);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    KaboumUtil.debug(" ERROR ! Cannot write WKT string");
                    wkt = "EMPTY";
                }
                
                String tmpStr = e.getActionCommand() +
                        "|" +
                        this.parent.agp.activeGGD.id +
                        "|" +
                        this.parent.mapServerTools.pm.perimeterToString(this.parent.agp.activeGGD.geometry.getPerimeter()) +
                        "|" +
                        this.parent.mapServerTools.pm.areaToString(this.parent.agp.activeGGD.geometry.getArea()) +
                        "|" +
                        wkt;
                
                this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
                this.reset();
                
                parent.kaboumResult(tmpStr);
                
                return;
            }
        }
    }
    
    
    /**
     *
     * Add the generic menu
     *
     */
    protected void addGenericMenu() {
        
        //
        // Allow Geometry Collection ?
        //
        if (!this.parent.agp.activeGGD.isEdited()) {
            if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_MULTIALLOWED), true)) {
                MenuItem addNGWC = new MenuItem(parent.defaultLang.getString("GEOMETRY_ADD_GEOMETRY_WITHIN_COLLECTION"));
                addNGWC.setActionCommand("ADD_GEOMETRY_WITHIN_COLLECTION");
                this.pop.add(addNGWC);
            }
            if (this.parent.agp.activeSimpleGeometry != null) {
                if (this.parent.agp.activeSimpleGeometry.getGeometryType().equals("Polygon")) {
                    if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_HOLEALLOWED), true)) {
                        MenuItem addNHWG = new MenuItem(parent.defaultLang.getString("GEOMETRY_ADD_HOLE_WITHIN_GEOMETRY"));
                        addNHWG.setActionCommand("ADD_HOLE_WITHIN_GEOMETRY");
                        this.pop.add(addNHWG);
                    }
                }
            }
            if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SUPPRESSIONWITHINCOLLECTIONALLOWED), true)) {
                MenuItem addRSG = new MenuItem(parent.defaultLang.getString("GEOMETRY_REMOVE_GEOMETRY_WITHIN_COLLECTION"));
                addRSG.setActionCommand("REMOVE_GEOMETRY_WITHIN_COLLECTION");
                this.pop.add(addRSG);
            }
        }
        
        if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_CANCELALLOWED), true)) {
            MenuItem cancelObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_CANCEL_GEOMETRY"));
            cancelObject.setActionCommand("CANCEL_GEOMETRY");
            this.pop.add(cancelObject);
        }
        
        MenuItem validateObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_VALIDATE_GEOMETRY"));
        validateObject.setActionCommand("VALIDATE_OBJECT");
        this.pop.add(validateObject);
        
        if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SUPPRESSIONALLOWED), true)) {
            MenuItem removeObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_REMOVE_GEOMETRY"));
            removeObject.setActionCommand("REMOVE_OBJECT");
            this.pop.add(removeObject);
        }
        
    }
    
    
    /**
     *
     * Add the topological menu
     *
     */
    protected void addTopologicalMenu() {
        
        String tmpKey;
        MenuItem tmpMenuItem;
        
        //
        // Add an item menu for each action
        //
        tmpMenuItem = new MenuItem("Union");
        tmpMenuItem.setActionCommand("TOPOLOGY|UNION");
        this.pop.add(tmpMenuItem);
        
        tmpMenuItem = new MenuItem("Difference");
        tmpMenuItem.setActionCommand("TOPOLOGY|DIFFERENCE");
        this.pop.add(tmpMenuItem);
        
        tmpMenuItem = new MenuItem("Symetrique difference");
        tmpMenuItem.setActionCommand("TOPOLOGY|SYMDIFFERENCE");
        this.pop.add(tmpMenuItem);
        
    }
    
    
    /**
     *
     * Add a separator menu
     *
     */
    protected void addSeparatorMenu() {
        MenuItem separator = new MenuItem("-");
        this.pop.add(separator);
    }
    
    
    public void mouseDragged(MouseEvent evt) {
        
        this.mp = new Point(-10, -10);
        
        this.currentMousePosition = evt.getPoint();
        
        if (this.parent.agp.activeGGD == null) {
            return;
        }
        
        
        if (this.parent.agp.activeGGD.isEdited()) {
            return;
        }
        
        //
        // Avoid a very nasty bug: when two consecutive clicks
        // occur, java thinks there is a drag instead of a click
        // And that's bad
        //
        if (this.pmc == null) {
            this.pmc = evt;
            this.resolveMouseEvent(this.currentMousePosition, this.DRAG_POINT);
        } else {
            if (((Math.abs(this.currentMousePosition.x - this.pmc.getX()) > 60) || (Math.abs(this.currentMousePosition.y - this.pmc.getY()) > 60)) && ((Math.abs(evt.getWhen() - pmc.getWhen()) < this.dctime))) {
                this.pmc = evt;
                if (this.parent.agp.activeGGD.isEdited()) {
                    this.resolveMouseEvent(this.currentMousePosition, this.ADD_POINT);
                }
            } else {
                this.resolveMouseEvent(this.currentMousePosition, this.DRAG_POINT);
            }
        }
        
        return;
    }
    
    
    public void mouseClicked(MouseEvent evt) {
        
        this.currentMousePosition = evt.getPoint();
        
        if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0) {
            this.resolveMouseEvent(this.currentMousePosition, this.SELECT_OBJECT);
        }
        
        // First mouse button (left button) pressed : Add a point
        else if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            
            if (this.parent.agp.activeGGD != null) {
                if (this.parent.agp.activeGGD.isEdited()) {
                    this.resolveMouseEvent(this.currentMousePosition, this.ADD_POINT);
                }
            }
            
            return;
            
        }
        
        else  {
            
            if (this.parent.agp.activeGGD != null) {
                if (this.parent.agp.activeGGD.isEdited()) {
                    this.resolveMouseEvent(this.currentMousePosition, this.VALIDATE_OBJECT);
                    return;
                }
            }
            
            this.resolveMouseEvent(this.currentMousePosition, this.SHOW_MENU);
            
            return;
            
        }
        
    }
    
    
    public void mousePressed(MouseEvent evt) {
        
        this.currentMousePosition = evt.getPoint();
        
        if (this.pmc == null) {
            this.pmc = evt;
        }
        
    }
    
    
    public void mouseReleased(MouseEvent evt) {
        
        this.currentMousePosition = evt.getPoint();
        
        // Un-intersect current object
        if (this.dragStatus) {
            this.intersect(this.parent.agp.activePointClickedPosition);
            this.dragStatus = false;
        }
        
        parent.repaint();
        this.firstPointDragged = true;
    }
    
    
    public void mouseMoved(MouseEvent evt) {
        
        if (this.parent.activeGGD != null && this.parent.activeGGD.pd.isVertexSnappable()) {
            snapToVertex(evt.getPoint(), null);
        }
        this.mp = evt.getPoint();
        this.currentMousePosition = evt.getPoint();
       
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(this.currentMousePosition.x, this.currentMousePosition.y), true);
        //this.parent.showMessage("could snap here" + this.parent.mapServerTools.getMapCoordString(this.currentMousePosition.x, this.currentMousePosition.y), true);
        
        this.dragStatus = false;
        
        // no more tooltip in this tool
        if (this.parent.agp.activeGGD == null) {
            //this.parent.refreshTooltip();
            return;
        }
        
        if (this.parent.agp.activeGGD.isEdited()) {
            this.mp = new Point(-10, -10);
        }
        
        this.parent.repaint();
    }
    
    
    
    /**
     *
     * Intersection procedure. If tmpPointId is != -1, only the two segments linked
     * to tmpPointId are treated. This avoid recomputation of others segments when only
     * one point had changed
     *
     * @param tmpPointId Point to use
     * @param type Type of intersection
     *
     *
     */
    protected boolean intersect(int tmpPointId) {
        
        return false;
    }
    
    
    protected void saveCurrentCoordinates() {
        
        if (this.parent.agp.activeSimpleGeometry != null) {
            
            int numPoints = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
            this.parent.agp.savedCoordinates = new KaboumCoordinate[numPoints];
            System.arraycopy(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates(), 0, this.parent.agp.savedCoordinates, 0, numPoints);
            
        }
        
    }
    
    
    
    /**
     * Resolve MOUSE RIGHT BUTTON action : Validate object
     *
     * @param mouse mouse position
     * @param action action to resolve
     *
     */
    protected void resolveMouseValidateObject(Point mouse, int action) {
        
        if (this.parent.agp.activeGGD == null) {
            return;
        }
        
        if (this.parent.agp.activeGGD.geometry == null) {
            return;
        }
        
        this.pop.removeAll();
        
        // Popup menu is on. Hide it
        if (this.isMenuOn) {
            this.isMenuOn = false;
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry != null) {
            
            int numPoints = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
            
            // If the object get a valid number of points, user can validate it
            if (numPoints > this.parent.agp.activeGGD.geometry.getDimension()) {
                MenuItem preValidateObject = new MenuItem(parent.defaultLang.getString("GEOMETRY_PRE_VALIDATE_GEOMETRY"));
                preValidateObject.setActionCommand("PRE_VALIDATE_OBJECT");
                this.pop.add(preValidateObject);
            }
            
            // If the object get more than one point, user can cancel previous action
            if (numPoints > 0) {
                MenuItem undo = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_UNDO"));
                undo.setActionCommand("UNDO");
                this.pop.add(undo);
            }
            
            if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_CANCELALLOWED), false)) {
                MenuItem cancelObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_CANCEL_GEOMETRY"));
                cancelObject.setActionCommand("CANCEL_GEOMETRY");
                this.pop.add(cancelObject);
            }
            
        }
        
        if (this.pop.getItemCount() > 0) {
            this.pop.show(parent, mouse.x, mouse.y);
            this.isMenuOn = true;
        }
        
    }
    
    
    /**
     * Resolve MOUSE CONTROL CLICK action : Select an object
     *
     * @param mouse mouse position
     * @param action action  to resolve
     *
     */
    protected void resolveMouseSelectObject(Point mouse, int action) {
        
        boolean result = false;
        double precision = (this.parent.mapServerTools.getRealExtent().dx() / this.parent.screenSize.width ) * KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_PIXELPRECISION), 1);
        
        if (this.parent.agp.activeGGD != null) {
            if (this.parent.agp.activeGGD.isEdited()) {
                return;
            }
        }
        
        try {
            
            int numGeometries = this.parent.GGDIndex.getVisibleGeometries().length;
            KaboumGeometryGlobalDescriptor tmpGGD;
            
            for (int i = 0; i < numGeometries; i++) {
                
                tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
                
                // Only valid geometry can be selected
                if (!this.isValid(tmpGGD)) {
                    continue;
                }
                
                // Locked objects cannot be selectionned
                if (tmpGGD.pd.isLocked()) {
                    continue;
                }
                
                result = KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y), tmpGGD.geometry, precision);
                
                if (result) {
                    // stores the active geom to be able to restore it if edition
                    // is canceled:
                    this.undoGeomWkt = wktWriter.write(tmpGGD.geometry);
                    this.setActiveGGD(tmpGGD);
                    this.parent.repaint();
                    return;
                }
            }
            // CTRL+CLICK outside existing objects ---> unselect all geometry
            this.unselectAll();
            
        } catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  KaboumAlgorithms class not found in jar file. SELECTION OpMode cannot work (" + jse.getMessage() + ")");
            return;
        }
        this.parent.repaint();
        return;
    }
    
    /**
     *
     * Resolve MOUSE RIGHT OR MIDDLE CLICK  action : Show menu
     *
     * @param mouse mouse position
     * @param action action to resolve
     *
     */
    protected void resolveMouseShowMenu(Point mouse, int action) {
        
        double precision = (this.parent.mapServerTools.getRealExtent().dx() / this.parent.screenSize.width ) * KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_PIXELPRECISION), 1);
        boolean isDragAllowed = KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_DRAGALLOWED), true);
        
        try {
            
            if (this.parent.agp.activeGGD == null) {
                
                MenuItem newObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_NEW_GEOMETRY"));
                newObject.setActionCommand("NEW_OBJECT");
                
                pop.removeAll();
                pop.add(newObject);
                
                if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_CANCELALLOWED), false)) {
                    MenuItem cancelObject = new MenuItem(this.parent.defaultLang.getString("GEOMETRY_CANCEL_GEOMETRY"));
                    cancelObject.setActionCommand("CANCEL_OBJECT");
                    this.pop.add(cancelObject);
                }
                
                pop.show(parent, mouse.x, mouse.y);
                parent.repaint();
                
                return;
            } else {
                
                this.parent.agp.activePointClickedPosition = this.selectCoordinatePosition(mouse);
                
                if (this.parent.agp.activePointClickedPosition != -1) {
                    
                    this.pop.removeAll();
                    
                    int modifier = 1;
                    
                    // Avoid unvalidated object by removing to much points...
                    if (this.parent.agp.activeSimpleGeometry.getGeometryType().indexOf("Polygon") != -1) {
                        modifier = 2;
                    }
                    
                    if (this.parent.agp.activeSimpleGeometry.getExteriorNumPoints() > this.parent.agp.activeSimpleGeometry.getDimension() + modifier) {
                        MenuItem removeCtrlPoint = new MenuItem(parent.defaultLang.getString("GEOMETRY_REMOVE_POINT"));
                        removeCtrlPoint.setActionCommand("REMOVE_POINT");
                        pop.add(removeCtrlPoint);
                    }
                    
                    this.addSeparatorMenu();
                    this.addGenericMenu();
                    
                    this.pop.show(parent, mouse.x, mouse.y);
                } else if (this.parent.agp.activePointClickedPosition == -1) {
                    
                    int numGeoms = this.parent.agp.activeGGD.geometry.getNumGeometries();
                    
                    for (int i = 0; i < numGeoms; i++) {
                        
                        if (KaboumAlgorithms.doesPointBorderGeometry(this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y), this.parent.agp.activeGGD.geometry.getGeometryN(i), precision)) {
                            
                            this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(i);
                            this.pop.removeAll();
                            
                            //
                            // Add point is a non sense if point drag is not allowed
                            //
                            if (isDragAllowed) {
                                MenuItem addCtrlPoint = new MenuItem(parent.defaultLang.getString("GEOMETRY_ADD_POINT"));
                                addCtrlPoint.setActionCommand("ADD_POINT");
                                this.pop.add(addCtrlPoint);
                            }
                            
                            this.addSeparatorMenu();
                            this.addGenericMenu();
                            
                            this.pop.show(parent, mouse.x, mouse.y);
                            
                            parent.repaint();
                            
                            return;
                        }
                        
                        else if (KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y), this.parent.agp.activeGGD.geometry.getGeometryN(i), precision)) {
                            
                            this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(i);
                            this.pop.removeAll();
                            this.addGenericMenu();
                            this.pop.show(parent, mouse.x, mouse.y);
                            this.parent.repaint();
                            return;
                        }
                    }
                    
                }
                
            }
            
        } catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  KaboumAlgorithms class not found in jar file. SELECTION OpMode cannot work");
            return;
        }
        
        parent.repaint();
        
        return;
        
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
        
        KaboumCoordinate[] internals;
        
        for (int i = 0; i < numGeoms; i++) {
            internals = this.parent.agp.activeGGD.geometry.getGeometryN(i).getExteriorCoordinates();
            idControlPoint = this.selectAbsoluteCoordinatePosition(mouse, internals);
            if (idControlPoint != -1) {
                this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(i);
                break;
            }
        }
        
        return idControlPoint;
        
    }
    
    
    /**
     *
     * Select absolute coordinate position
     *
     */
    protected int selectAbsoluteCoordinatePosition(Point mouse, KaboumCoordinate[] internals) {
        int count = 0;
        int idControlPoint = -1;
        int numPoints = internals.length;
        Point tmpMouse;
        int w = this.parent.agp.activeGGD.dd.getPointWidth();
        int h = this.parent.agp.activeGGD.dd.getPointHeight();
        
        for (int j = 0; j < numPoints; j++) {
            
            tmpMouse = this.parent.mapServerTools.internalToMouseXY(internals[j]);
            if (((mouse.x >= tmpMouse.x - w) && (mouse.x <= tmpMouse.x + w)) &&
                    ((mouse.y >= tmpMouse.y - h) && (mouse.y <= tmpMouse.y + h))) {
                idControlPoint = count;
                break;
            }
            count++;
        }
        
        return idControlPoint;
        
    }
    
    
    /**
     *
     * Select absolute coordinate position
     *
     */
    protected int selectAbsoluteCoordinatePosition(Point mouse) {
        return this.selectAbsoluteCoordinatePosition(mouse, this.parent.agp.activeGGD.geometry.getCoordinates());
    }
    
    
    /**
     * Resolve MOUSE DRAG BUTTON  action : Drag a point
     *
     * @param mouse mouse position
     * @param action action to resolve
     *
     */
    protected void resolveMouseDragPoint(Point mouse, int action) {
        
        boolean isDragAllowed = KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_DRAGALLOWED), true);
        
        // Drag a point is allowed by applet ?
        if (!isDragAllowed) {
            return;
        }
        
        if (this.parent.agp.activeGGD == null) {
            return;
        }
        
        // Show the mouse coordinate
        this.parent.showMessage(parent.mapServerTools.getMapCoordString(mouse.x, mouse.y), true);
        
        if (this.firstPointDragged) {
            this.dragPointPosition = this.selectAbsoluteCoordinatePosition(mouse);
        }
        
        if (this.dragPointPosition != -1) {
            
            this.dragStatus = true; 
            
            // The first time save the current object
            if (this.firstPointDragged) {
                this.saveCurrentCoordinates();
                this.firstPointDragged = false;
            }
            
            // Save the last point ID, i.e. the current point ID
            this.parent.agp.activePointClickedPosition = this.dragPointPosition;
            
            // Move the current point to its new position if eligible (not the current dragged vertex)
            KaboumCoordinate currentDraggedVertex = this.parent.agp.activeGGD.geometry.getCoordinates()[this.dragPointPosition];

            // finds closest vertex to snap to now, now currentSnapVertex is known
            if (this.parent.agp.activeGGD.pd != null && this.parent.activeGGD.pd.isVertexSnappable()) {
                snapToVertex(mouse, currentDraggedVertex);
            }
            
            KaboumCoordinate coord = this.parent.agp.activeGGD.pd.isVertexSnappable() && 
                    this.currentSnapCoord != null ?
                this.currentSnapCoord :
                this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
            
            currentDraggedVertex.moveTo(coord);
            
            // list was modified
            this.parent.agp.activeGGD.setModified(true);
            
            parent.repaint();
        }
        return;
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
        KaboumCoordinate internal = null;
        if (this.parent.agp.activeGGD.pd.isVertexSnappable() && 
                this.currentSnapCoord != null && 
                ! parent.agp.activeSimpleGeometry.isEmpty()) {
            internal = this.currentSnapCoord;
        } else {
            internal = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
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
    
    
///////////////////////////     PAINT METHODS /////////////////////////////////////
    
    
    public boolean removeLastCoordinate() {
        
        int size = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
        
        if (size < 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size - 1];
        System.arraycopy(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates(), 0, tmpInternals, 0, size - 1);
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    
    public boolean removeCoordinateAt(int position) {
        
        int size = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
        
        if (position > size - 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size - 1];
        KaboumCoordinate[] sgInternals = this.parent.agp.activeSimpleGeometry.getExteriorCoordinates();
        System.arraycopy(sgInternals, 0, tmpInternals, 0, position);
        
        if (position < size - 1) {
            System.arraycopy(sgInternals, position + 1, tmpInternals, position, size - position - 1);
        }
        
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    public boolean insertCoordinate(KaboumCoordinate internal, int position) {
        
        int size = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
        
        if (position > size - 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size + 1];
        KaboumCoordinate[] sgInternals = this.parent.agp.activeSimpleGeometry.getExteriorCoordinates();
        System.arraycopy(sgInternals, 0, tmpInternals, 0, position);
        tmpInternals[position] = internal;
        System.arraycopy(sgInternals, position, tmpInternals, position + 1, size - position);
        
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    
    /**
     *
     * Add a hole within this geometry.
     * This has no sense for other geometries than Polygon
     *
     */
    protected void actionPerformedAddHoleWithinGeometry(ActionEvent e) {
        return;
    }
    
    
    /**
     *
     * Return an alert box that indicate that the current Geometry
     * is cancelled
     *
     */
    protected void actionPerformedCancelGeometry(ActionEvent e) {
        parent.kaboumResult("GEOMETRY|" + this.parent.agp.activeGGD.id + "|CANCEL");
        // reset the current modified object: removes it from ggd if new, restore old object
        // if updated existing object.
        this.parent.agp.activeSimpleGeometry = null;
        if (! this.parent.agp.activeGGD.id.equals(Kaboum.K_NEW_GEOMETRY)) {
            // an existing object is edited, must restore it
            KaboumUtil.debug("should cancel edition for object: " + this.parent.agp.activeGGD.id + " by restoring old object");
            try {
                this.parent.agp.activeGGD.geometry = wktReader.read(undoGeomWkt);
                this.parent.agp.activeGGD.setEdited(false);
                this.parent.agp.activeGGD.setHilite(false);
                this.parent.agp.activeGGD.setModified(false);
            } catch (Exception ex) {
                KaboumUtil.debug(ex.getMessage());
                ex.printStackTrace();
            }
        }
        this.reset();
    }
    
////////////////////////// ABSTRACT METHODS  //////////////////////////////
    
    
    /**
     *
     * Add a geometry within this collection
     *
     */
    protected abstract void actionPerformedAddGeometryWithinCollection(ActionEvent e);
    
    
    /**
     *
     * Remove a geometry within this collection
     *
     */
    protected abstract void actionPerformedRemoveGeometryWithinCollection(ActionEvent e);
    
    
    /**
     *
     * actionPerformed event e : NewObject
     *
     * @param e action to perform
     *
     */
    protected abstract void actionPerformedNewObject(ActionEvent e);
    
    
    public abstract boolean isValid(KaboumGeometryGlobalDescriptor ggd);
    
    
    public abstract void paint(Graphics g);
    
    
    // snapping methods
    /** 
     * Finds and Stores the closest vertex coordinate to the given point, at the specified tolerance (see SNAPPING_TOLERANCE parameter)
     * into the currentSnapCoord variable, or stores null if no vertex can be found
     * @param mouse the current mouse position
     * @param currentDraggedVertex the current dragged vertex in case of vertex drag mode, or null.
     * this can be used to avoid storing current dragged vertex as the vertex to snap to.
     */
    protected void snapToVertex(Point mouse, KaboumCoordinate currentDraggedCoord) {
        if (mouse == null) {
            return;
        }
        // converts pixel snapping tolerance to map units distance
        double snapTolerance = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_SNAPPINGTOLERANCE));
        snapTolerance *= this.parent.mapServerTools.getPixelScale();
        double minDist = snapTolerance;
        int coordIndex = -1;
        // current mouse position in map coords
        KaboumCoordinate coord = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
        // array of valid
        KaboumCoordinate[] coords = this.parent.GGDIndex.getSnapVertices();
        for (int i = 0; i < coords.length; i++) {
            if (currentDraggedCoord != null && coords[i].hashCode() == currentDraggedCoord.hashCode()) {
                //KaboumUtil.debug("using currentsnapCoord: " + this.parent.mapServerTools.internalToMouseXY(currentDraggedCoord));
                continue;
            }
            double d = coords[i].distance(coord);
            if (d < minDist) {
                coordIndex = i;
                minDist = d;
            }
        }
        //KaboumUtil.debug("mindistance (" + coordIndex + "): " + minDist);
        if (coordIndex >= 0) {
            this.currentSnapCoord = coords[coordIndex];
        } else {
            this.currentSnapCoord = null;
        }
        //KaboumUtil.debug("using currentsnapCoord: " + this.currentSnapCoord);
    }
            
    
    // snapping methods
    /** 
     * Finds and Stores the closest edge coordinate to the given point, at the specified tolerance (see SNAPPING_TOLERANCE parameter)
     * into the currentSnapCoord variable, or stores null if no vertex can be found
     * @param mouse the current mouse position
     * @param currentDraggedVertex the current dragged vertex in case of vertex drag mode, or null.
     * this can be used to avoid storing current dragged vertex as the vertex to snap to.
     */
    protected void snapToEdge(Point mouse, KaboumCoordinate currentDraggedCoord) {
        if (mouse == null) {
            return;
        }
        // converts pixel snapping tolerance to map units distance
        double snapTolerance = KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_SNAPPINGTOLERANCE));
        snapTolerance *= this.parent.mapServerTools.getPixelScale();
        double minDist = snapTolerance;
        int coordIndex = -1;
        // current mouse position in map coords
        KaboumCoordinate coord = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
        // array of valid
        KaboumCoordinate[] coords = this.parent.GGDIndex.getSnapVertices();
        for (int i = 0; i < coords.length; i++) {
            if (currentDraggedCoord != null && coords[i].hashCode() == currentDraggedCoord.hashCode()) {
                //KaboumUtil.debug("using currentsnapCoord: " + this.parent.mapServerTools.internalToMouseXY(currentDraggedCoord));
                continue;
            }
            double d = coords[i].distance(coord);
            if (d < minDist) {
                coordIndex = i;
                minDist = d;
            }
        }
        //KaboumUtil.debug("mindistance (" + coordIndex + "): " + minDist);
        if (coordIndex >= 0) {
            this.currentSnapCoord = coords[coordIndex];
        } else {
            this.currentSnapCoord = null;
        }
        //KaboumUtil.debug("using currentsnapCoord: " + this.currentSnapCoord);
    }
            
    
}
