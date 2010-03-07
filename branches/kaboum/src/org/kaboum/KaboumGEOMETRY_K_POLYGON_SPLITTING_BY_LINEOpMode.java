
/*
 *
 * Class KaboumGEOMETRYOpMode from the Kaboum project.
 * Class to split a polygon with a line.
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
 *     jerome dot gasperi at gmail dot com
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
import java.util.StringTokenizer;

import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.geom.KaboumGGDIndex;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumWKTReader;
import org.kaboum.geom.KaboumGeometryFactory;
import org.kaboum.geom.KaboumLineString;
import org.kaboum.util.KaboumFeatureModes;



/**
 *
 * Class to split a polygon with another polygon.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_K_POLYGON_SPLITTING_BY_LINEOpMode extends KaboumOpMode {
    
    /** CONSTANTS */
    //public static final String GEOMETRY_SELECTED_ID_LIST = "GEOMETRY_SELECTED_ID_LIST";
    
    /** CONSTANTS */
    public static final String PARAM_DRAGALLOWED = "GEOMETRY_ALLOW_TO_DRAG_POINT";
    public static final String PARAM_PRECISION = "GEOMETRY_COMPUTATION_PRECISION";
    public static final String PARAM_DCTIME = "GEOMETRY_DOUBLE_CLICK_TIME";
    public static final String PARAM_PIXELPRECISION = "GEOMETRY_PIXEL_PRECISION";
    public static final String PARAM_ERASING_ON_SERVER = "SERVER_DO_POLYGON_ERASING";
    public static final String PARAM_HOLES_COMPLETION_ON_SERVER = "SERVER_DO_HOLES_COMPLETION";
    public static final String PARAM_SPLITTING_ON_SERVER = "SERVER_DO_POLYGON_SPLITTING";
    public static final String PARAM_SPLITTING_BY_LINE_ON_SERVER = "SERVER_DO_POLYGON_SPLITTING_BY_LINE";
    public static final String PARAM_COMPLETION_ON_SERVER = "SERVER_DO_POLYGON_COMPLETION";
    public static final String PARAM_COMPLETION_FITTING_ON_SERVER = "SERVER_DO_POLYGON_COMPLETION_FITTING";
    
    public static String[] getParametersList() {
        String[] list = new String[10];
        list[0] = PARAM_DRAGALLOWED;
        list[1] = PARAM_PRECISION;
        list[2] = PARAM_DCTIME;
        list[3] = PARAM_ERASING_ON_SERVER;
        list[4] = PARAM_HOLES_COMPLETION_ON_SERVER;
        list[5] = PARAM_SPLITTING_ON_SERVER;
        list[6] = PARAM_COMPLETION_ON_SERVER;
        list[7] = PARAM_COMPLETION_FITTING_ON_SERVER;
        list[8] = PARAM_PIXELPRECISION;
        list[9] = PARAM_SPLITTING_BY_LINE_ON_SERVER;
        return list;
    }
    
    /** Private constant */
    protected final int SELECT_OBJECT = 0;
    protected final int ADD_POINT = 1;
    protected final int SHOW_MENU = 2;
    protected final int SHOW_VALIDATE_MENU = 5;
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
    
    // ADD JROM
    /** tells if curent opMode is in validaton mode or not */
    protected boolean isValidationMode;
    
    /** Current OPERATION */
    protected short OPERATION = KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE;
    
    /** Current selected reference polygon */
    protected static KaboumGeometryGlobalDescriptor selectedGGD = null;
    
    /** Selection list (list of all the selected objects) */
    protected Vector selectionList;
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRY_K_POLYGON_SPLITTING_BY_LINEOpMode(Kaboum parent) {
        this(parent, KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param idList Object return list send by KaboumServer
     *
     */
    public KaboumGEOMETRY_K_POLYGON_SPLITTING_BY_LINEOpMode(Kaboum parent, short OPERATION) {
        this.parent = parent;
        this.OPERATION = OPERATION;
        this.selectionList = new Vector();
        this.currentMousePosition = new Point(-100, -100);
        
        // ADD JROM
        this.mp = null;
        this.isValidationMode = false;
        this.unSelectAll();
        // END JROM
        
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
        
        KaboumOpModeStatus status = parent.opModeStatus;
        //checks the edition status
        if (status.isEditionPending && status.opModeName.equals(this.getOpModeName())) {
            // this OpMode is in edition mode, loads any previously selected objects
            if (status.getIdList().length() > 0) {
                StringTokenizer tok = new StringTokenizer(status.getIdList(), ";");
                KaboumGeometryGlobalDescriptor tmpGGD = null;

                while (tok.hasMoreTokens()) {
                    tmpGGD = parent.GGDIndex.getGGD(tok.nextToken());
                    //System.out.println("nico adding id: " + tmpGGD.id);
                    selectionList.addElement(tmpGGD);
                }
                isValidationMode = true;
            }
        } else if (selectedGGD != null) {
            this.selectGeometry(selectedGGD);
            KaboumUtil.debug("restoring existing drawer, selected object");
        }
        KaboumGGDIndex.onTopID = status.getOnTopID();
        // register this OpMode as the current edition OpMode
        status.opModeName = this.getOpModeName();
        
        if (this.parent.agp.activeGGD != null) {
            if (Kaboum.K_NEW_GEOMETRY.equals(this.parent.agp.activeGGD.id)) {
                this.setActiveGGD(this.parent.agp.activeGGD);
                //this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(0);
            } else {
                this.parent.agp.reset();
            }
            this.parent.repaint();
        }
        // END JROM
    }
    
    /**
     * Select a geometry.
     *
     * @param ggd Selected geometry
     */
    public void selectGeometry(KaboumGeometryGlobalDescriptor ggd) {
        ggd.setSelected(true);
        ggd.setSuperHilite(true);
        // send a SELECTION command to the client
        parent.kaboumResult("SELECTION|" + ggd.id);
        // Logs
        //KaboumUtil.debug("Select geometry " + ggd.id);
    }
    
    /**
     * Un-Select a geometry.
     *
     * @param ggd Geometry to be unselected
     */
    public void unSelectGeometry(KaboumGeometryGlobalDescriptor ggd) {
        ggd.setSelected(false);
    }
    
    /**
     * Un-Select all geometries.
     */
    public void unSelectAll() {
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        
        for (int i = 0; i < numGGD; i++) {
            this.unSelectGeometry(this.parent.GGDIndex.getVisibleGeometries()[i]);
        }
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
        
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
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
     * Reset the current active geometry
     *
     */
    protected void reset() {
        this.pop.removeAll();
        this.unSelectAll();
        if (this.selectionList != null) {
            this.selectionList.removeAllElements();
        }
        this.selectedGGD = null;
        if (this.parent.agp.activeGGD != null) {
            this.parent.GGDIndex.removeGeometry(this.parent.agp.activeGGD.id);
        }
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
        } else if (e.getActionCommand().equals("NEW_OBJECT")) {
            actionPerformedNewObject(e);
        } else if (e.getActionCommand().equals("PRE_VALIDATE_OBJECT")) {
            actionPerformedPreValidateObject(e);
        } else if (e.getActionCommand().equals("UNDO")) {
            actionPerformedUndo(e);
        } else if (e.getActionCommand().equals(KaboumFeatureModes.getMode(this.OPERATION))) {
            actionPerformedSendOperation(e);
        } else if (e.getActionCommand().equals("CLEAR_SELECTION")) {
            reset();
        } else if (e.getActionCommand().equals("VALIDATE")) {
            // sends the new object for validation and removes all objects
            // involved in the operation.
            this.isValidationMode = false;
            parent.opModeStatus.reset();
            selectedGGD.setModified(false);
            if (((String)parent.opModePropertiesHash.get("CLIENT_MANAGE_SPLIT")).equalsIgnoreCase("true")) {
                // kaboum should manage split object submission itself by adding and updating objects contained
                // in the resulting collection
                KaboumUtil.debug("Split mode: updating and adding individual objects");
                this.parent.featureServerTools.updateAndAdd(selectedGGD, "GEOMETRY_" + KaboumFeatureModes.getMode(this.OPERATION));
            } else {
                KaboumUtil.debug("Split mode: updating collection as one object");
                this.parent.featureServerTools.validateAndRemove(selectedGGD, null, "GEOMETRY_" + KaboumFeatureModes.getMode(this.OPERATION));
            }
            reset();
        } else if (e.getActionCommand().equals("CANCEL")) {
            KaboumGEOMETRY_K_POLYGON_SPLITTING_BY_LINEOpMode.selectedGGD.setModified(false);
            parent.featureServerTools.restoreGeometry();
            parent.opModeStatus.reset();
            isValidationMode = false;
            reset();
        }
        this.pop.removeAll();
        this.parent.repaint();
    }
    
    /**
     *
     * Send result to KaboumResult method or to KaboumFeature
     * depending on the input options
     *
     * @param e ActionEvent
     *
     */
    protected void actionPerformedSendOperation(ActionEvent e) {
        if ((this.selectedGGD != null) && (this.parent.agp.activeGGD != null)) {
            
            this.pop.removeAll();
            this.parent.standbyOn();
            
            this.parent.agp.activeGGD.geometry.normalize();
            this.parent.agp.activeGGD.setEdited(false);
            
            String idString = this.selectedGGD.id + ";" + this.parent.agp.activeGGD.id;
            parent.opModeStatus.setIdList(idString);
            parent.opModeStatus.opModeName = this.getOpModeName();
            parent.opModeStatus.isEditionPending = true;
            parent.opModeStatus.setOnTopID(this.selectedGGD.id);
            //KaboumUtil.debug("SEND : " + idString);
            
            if (parent.featureServerTools != null &&
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SPLITTING_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_SPLITTING) ||
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SPLITTING_BY_LINE_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_SPLITTING_BY_LINE) ||
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_COMPLETION_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_COMPLETION) ||
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_ERASING_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_ERASING) ||
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_COMPLETION_FITTING_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING) ||
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_HOLES_COMPLETION_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_HOLES_COMPLETION)) {
                // operation is sent on the server
                Vector geoms = new Vector(2);
                geoms.addElement(this.selectedGGD.geometry);
                geoms.addElement(this.parent.agp.activeGGD.geometry);
                selectedGGD.setSelected(false);
                this.parent.featureServerTools.processSpatialAnalysis(
                    this.parent.agp.activeGGD.dd.name,
                    geoms,
                    OPERATION,
                    true);
                this.selectedGGD.setModified(true);
                this.parent.agp.reset();
                destroyEvent();
            } else {
                // Operation is processed by JS code
                // Result form:
                // SELECTION|<object1.id>;<object2.id>; ...
                //
                parent.kaboumResult(KaboumFeatureModes.getMode(this.OPERATION) + "|" + idString);
                
            }
            this.parent.setCursor("HAND");
            this.parent.repaint();
            return;
        }
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
        
        // CASE FIVE : RIGHT BUTTON (show validate menu)
        else if (action == this.SHOW_VALIDATE_MENU) {
            resolveMouseShowValidateMenu(mouse, action);
        }
        // CASE SIX : RIGHT BUTTON (Validate object)
        else if (action == this.VALIDATE_OBJECT) {
            resolveMouseValidateObject(mouse, action);
        }
        
        return;
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
            }
        }
    }
    
    
    /**
     *
     * Add the generic menu
     *
     */
    protected void addGenericMenu() {
        
        // ADD JROM
        MenuItem operationSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_"+ KaboumFeatureModes.getMode(this.OPERATION)));
        operationSelection.setActionCommand(KaboumFeatureModes.getMode(this.OPERATION));
        this.pop.add(operationSelection);
        
        MenuItem clearSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_CLEAR_SELECTION"));
        clearSelection.setActionCommand("CLEAR_SELECTION");
        this.pop.add(clearSelection);
        
    }
    
    /**
     * Displays the validate/Cancel menu at the given mouse coordinate.<br>
     * Sets the action events accordingly.
     *@param x, y the mouse coordinate where the menu will be displayed
     */
    protected void addValidateMenu() {
        
        MenuItem validateSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_VALIDATE"));
        validateSelection.setActionCommand("VALIDATE");
        pop.add(validateSelection);
        
        MenuItem cancelSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_CANCEL"));
        cancelSelection.setActionCommand("CANCEL");
        pop.add(cancelSelection);
    }
    
    
    public void mouseDragged(MouseEvent evt) {
        
        //this.mp = new Point(-10, -10);
        
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
            if (((Math.abs(this.currentMousePosition.x - this.pmc.getX()) > 60) || 
                    (Math.abs(this.currentMousePosition.y - this.pmc.getY()) > 60)) && ((Math.abs(evt.getWhen() - pmc.getWhen()) < this.dctime))) {
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
        
        if (isValidationMode) {
            if ( (evt.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
                //other than click
                this.resolveMouseEvent(this.currentMousePosition, this.SHOW_VALIDATE_MENU);
            }
            return;
        }
        if ((evt.getModifiers() & InputEvent.CTRL_MASK) != 0) {
            this.resolveMouseEvent(this.currentMousePosition, this.SELECT_OBJECT);
        } else if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            // First mouse button (left button) pressed : Add a point
            if (this.selectedGGD == null) {
                return;
            }
            
            if (this.parent.agp.activeGGD != null) {
                if (this.parent.agp.activeGGD.isEdited()) {
                    this.resolveMouseEvent(this.currentMousePosition, this.ADD_POINT);
                }
            }
            return;
        } else {
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
        
        if (this.dragStatus) {
            this.dragStatus = false;
        }
        
        parent.repaint();
        this.firstPointDragged = true;
    }
    
    
    public void mouseMoved(MouseEvent evt) {
        
        //this.mp = evt.getPoint();
        
        this.currentMousePosition = evt.getPoint();
        
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(this.currentMousePosition.x, this.currentMousePosition.y), true);
        
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
     * Save the current geometry coordinates
     *
     */
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
            
            // ADD JROM
            if (numPoints > this.parent.agp.activeGGD.geometry.getDimension()) {
                
                MenuItem operationSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_"+ KaboumFeatureModes.getMode(this.OPERATION)));
                operationSelection.setActionCommand(KaboumFeatureModes.getMode(this.OPERATION));
                this.pop.add(operationSelection);
                
                MenuItem preValidateObject = new MenuItem(parent.defaultLang.getString("GEOMETRYOPERATION_MODIFY_POINTS"));
                preValidateObject.setActionCommand("PRE_VALIDATE_OBJECT");
                this.pop.add(preValidateObject);
                
                MenuItem clearSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_CLEAR_SELECTION"));
                clearSelection.setActionCommand("CLEAR_SELECTION");
                this.pop.add(clearSelection);
                
            }
            
        }
        this.parent.GGDIndex.resetOnTopID();
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
        //nri no exist on new object, to allow to change selection
//        if (this.parent.GGDIndex.getGGDIndex(Kaboum.K_NEW_GEOMETRY) != -1) {
//            return;
//        }
        
        if (selectedGGD != null) {
            reset();
        }
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        
        for (int i = 0; i < numGGD; i++) {
            
            tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
            
            // Locked objects cannot be selectionned
            if (tmpGGD.pd.isLocked()) {
                continue;
            }
            
            if (this.contains(tmpGGD, mouse.x, mouse.y)) {
                this.unSelectAll();
                if (this.selectedGGD != null) {
                    if (this.selectedGGD.id.equals(tmpGGD.id)) {
                        this.selectedGGD = null;
                        break;
                    }
                }
                this.parent.GGDIndex.shiftOnTop(tmpGGD.id);
                this.selectedGGD = tmpGGD;
                this.selectGeometry(this.selectedGGD);
                this.actionPerformedNewObject(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "NEW"));
                break;
            }
            
        }
        
        this.parent.repaint();
        
    }
    
    
    /**
     *
     * Resolve MOUSE RIGHT OR MIDDLE CLICK  action : Show menu
     *
     * @param mouse mouse position
     * @param action action to resolve
     *
     */
    protected void resolveMouseShowValidateMenu(Point mouse, int action) {
        this.pop.removeAll();
        this.addValidateMenu();
        this.pop.show(parent, mouse.x, mouse.y);
        
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
                
                return;
                
            }
            
            else {
                
                this.parent.agp.activePointClickedPosition = this.selectCoordinatePosition(mouse);
                
                if (this.parent.agp.activePointClickedPosition != -1) {
                    
                    this.pop.removeAll();
                    
                    int modifier = 1;
                    
                    if (this.parent.agp.activeSimpleGeometry.getNumPoints() >
                    this.parent.agp.activeSimpleGeometry.getDimension() + modifier) {
                        MenuItem removeCtrlPoint = new MenuItem(parent.defaultLang.getString("GEOMETRY_REMOVE_POINT"));
                        removeCtrlPoint.setActionCommand("REMOVE_POINT");
                        pop.add(removeCtrlPoint);
                    }
                    
                    this.addGenericMenu();
                    
                    this.pop.show(parent, mouse.x, mouse.y);
                } else if (this.parent.agp.activePointClickedPosition == -1) {
                    
                    int numGeoms = this.parent.agp.activeGGD.geometry.getNumGeometries();
                    
                    for (int i = 0; i < numGeoms; i++) {
                        
                        if (KaboumAlgorithms.doesPointBorderGeometry(
                        this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y),
                        this.parent.agp.activeGGD.geometry.getGeometryN(i), precision)) {
                            
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
                            
                            this.addGenericMenu();
                            
                            this.pop.show(parent, mouse.x, mouse.y);
                            
                            parent.repaint();
                            
                            return;
                        }
                        
                        else if (KaboumAlgorithms.doesPointLieInGeometry(
                        this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y),
                        this.parent.agp.activeGGD.geometry.getGeometryN(i), precision)) {
                            
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
            this.dragPointPosition = this.selectAbsoluteCoordinatePosition(
            mouse,
            this.parent.agp.activeGGD.geometry.getCoordinates());
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
            
            // Move the current point to its new position
            this.parent.agp.activeGGD.geometry.getCoordinates()[this.dragPointPosition].moveTo(this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y));
            
            // list was modified
            this.parent.agp.activeGGD.setModified(true);
            
            parent.repaint();
            
        }
        
        return;
        
    }
    
    
    
    ///////////////////////////     PAINT METHODS /////////////////////////////////////
    
    
    public boolean removeLastCoordinate() {
        
        int size = this.parent.agp.activeSimpleGeometry.getNumPoints();
        
        if (size < 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size - 1];
        System.arraycopy(this.parent.agp.activeSimpleGeometry.getCoordinates(), 0, tmpInternals, 0, size - 1);
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    
    public boolean removeCoordinateAt(int position) {
        
        int size = this.parent.agp.activeSimpleGeometry.getExteriorNumPoints();
        
        if (position > size - 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size - 1];
        KaboumCoordinate[] sgInternals = this.parent.agp.activeSimpleGeometry.getCoordinates();
        System.arraycopy(sgInternals, 0, tmpInternals, 0, position);
        
        if (position < size - 1) {
            System.arraycopy(sgInternals, position + 1, tmpInternals, position, size - position - 1);
        }
        
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    public boolean insertCoordinate(KaboumCoordinate internal, int position) {
        
        int size = this.parent.agp.activeSimpleGeometry.getNumPoints();
        
        if (position > size - 1) {
            return false;
        }
        
        KaboumCoordinate[] tmpInternals = new KaboumCoordinate[size + 1];
        KaboumCoordinate[] sgInternals = this.parent.agp.activeSimpleGeometry.getCoordinates();
        System.arraycopy(sgInternals, 0, tmpInternals, 0, position);
        tmpInternals[position] = internal;
        System.arraycopy(sgInternals, position, tmpInternals, position + 1, size - position);
        
        this.parent.agp.activeSimpleGeometry.setCoordinates(tmpInternals);
        
        return true;
    }
    
    
    /**
     *
     * Return an alert box that indicate that the current Geometry
     * is cancelled
     *
     */
    protected void actionPerformedCancelGeometry(ActionEvent e) {
        parent.kaboumResult("GEOMETRY|" + this.parent.agp.activeGGD.id + "|CANCEL");
    }
    
    
    public boolean isValid(KaboumGeometryGlobalDescriptor ggd) {
        //if (ggd.geometry != null && ggd.geometry.getGeometryType().indexOf("Polygon") != -1) {
        if (ggd.geometry.getGeometryType().indexOf("LineString") != -1) {
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
        KaboumCoordinate internal = this.parent.mapServerTools.mouseXYToInternal(mouse.x, mouse.y);
        
        // Add the new point
        this.parent.agp.activeSimpleGeometry.addCoordinate(internal);
        
        // Save the last point ID, i.e. the current point ID
        this.parent.agp.activePointClickedPosition = 0;
        this.parent.agp.activeGGD.setModified(true);
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
        KaboumLineString[] lineStrings = new KaboumLineString[1];
        if (position == -1) {
            try {
                KaboumWKTReader wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
                lineStrings[0] = (KaboumLineString) wktReader.read("LINESTRING EMPTY");
                geometry = KaboumGeometryFactory.createMultiLineString(lineStrings);
                geometry.id = Kaboum.K_NEW_GEOMETRY;
            }
            catch (Exception jse) {
                KaboumUtil.debug(" WARNING ! :  error creating geometry ");
                return;
            }
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
     * ActionPerformed event e : PreValidateObject
     *
     * @param e action to perform
     *
     */
    public void actionPerformedPreValidateObject(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        if (this.parent.agp.activeSimpleGeometry.getNumPoints() > this.parent.agp.activeGGD.geometry.getDimension()) {
            this.parent.agp.activeSimpleGeometry.addCoordinate(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates()[0]);
            this.parent.agp.activeGGD.geometry.normalize();
            this.parent.agp.activeGGD.setEdited(false);
            this.isMenuOn = false;
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
            
            int numPoints = this.parent.agp.activeSimpleGeometry.getCoordinates().length;
            
            this.removeCoordinateAt(this.parent.agp.activePointClickedPosition);
            this.parent.agp.activePointClickedPosition = -1;
            this.parent.agp.activeGGD.setModified(true);
        }
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
        KaboumLineString tmpLinestring;
        
        for (int i = 0; i < numGeoms; i++) {
            
            tmpLinestring = (KaboumLineString) this.parent.agp.activeGGD.geometry.getGeometryN(i);
            
            internals = tmpLinestring.getCoordinates();
            idControlPoint = this.selectAbsoluteCoordinatePosition(mouse, internals);
            
            if (idControlPoint != -1) {
                this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(i);
                break;
            }
        }
        return idControlPoint;
    }
    
    
    public void paint(Graphics g) {
        if (this.parent.agp.activeGGD != null) {
            if (this.parent.agp.activeGGD.isEdited()) {
                if (this.parent.agp.activeSimpleGeometry != null) {
                    int numPoints = this.parent.agp.activeSimpleGeometry.getNumPoints();
                    if (numPoints < 1) {
                        return;
                    }
                    // Set the paint color
                    g.setColor(this.parent.agp.activeGGD.getCurrentColor());
                    
                    Point start = this.parent.mapServerTools.internalToMouseXY(this.parent.agp.activeSimpleGeometry.getCoordinates()[numPoints - 1]);
                    //Point end = this.parent.mapServerTools.internalToMouseXY(this.parent.agp.activeSimpleGeometry.getCoordinates()[0]);
                    
                    g.drawLine(start.x, start.y, this.currentMousePosition.x, this.currentMousePosition.y);
                  //g.drawLine(this.currentMousePosition.x, this.currentMousePosition.y, end.x, end.y);
                }
            }
        }
    }

    /**
     *
     * Check if an input coordinates pair (x, y) is inside input geometry
     *
     * @param ggd Input KaboumGeometryGlobalDescriptor
     * @param x Input x coordinate
     * @param x Input x coordinate
     *
     */
    private boolean contains(KaboumGeometryGlobalDescriptor ggd, int x, int y) {
        
        boolean result = false;
        double precision = (this.parent.mapServerTools.getRealExtent().dx() / this.parent.screenSize.width ) * KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_PIXELPRECISION), 1);
        
        try {
            result = KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(x, y), ggd.geometry, precision);
        } catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  KaboumAlgorithms class not found in jar file. SELECTION OpMode cannot work");
            return false;
        }
        return result;
    }
}
