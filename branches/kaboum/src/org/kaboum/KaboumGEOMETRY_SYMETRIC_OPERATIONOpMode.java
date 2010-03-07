/*
 *
 * Class KaboumOPERATIONOpMode from the Kaboum project.
 * Multi-selection opMode.
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
 *     jerome.gasperi at gmail.com
 *
 */
package org.kaboum;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.StringTokenizer;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.util.KaboumUtil;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumFeatureModes;


/**
 *
 * This opMode allows to select one or more object from
 * parent.geoObjectList and send a TOPOLOGICAL operation request
 * to KaboumServer
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode extends KaboumOpMode {
    
    /** CONSTANTS */
    public static final String PARAM_PIXELPRECISION = "SELECTION_PIXEL_PRECISION";
    public static final String PARAM_UNION_ON_SERVER = "SERVER_DO_UNION";
    public static final String PARAM_INTERSECTION_ON_SERVER = "SERVER_DO_INTERSECTION";
    
    public static String[] getParametersList() {
        String[] list = new String[3];
        list[0]  = PARAM_PIXELPRECISION;
        list[1]  = PARAM_UNION_ON_SERVER;
        list[2]  = PARAM_INTERSECTION_ON_SERVER;
        return list;
    }
    
    
    /** Parent reference */
    protected Kaboum parent;
    
    /** Popup menu (activated by right click mouse) */
    protected PopupMenu pop = null;
    
    /** Selection list (list of all the selected objects) */
    protected Vector selectionList;
    
    /** OPERATION type : one of K_UNION or K_INTERSECTION modes
     * @see KaboumFeatureModes for list of spatial analysis operations
     */
    protected short OPERATION;
    
    /** the name of the current display descriptor */
    protected String ddName = null;
    
    /** tells if curent opMode is in validaton mode or not */
    protected boolean isValidationMode;
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    protected KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode(Kaboum parent, short OPERATION) {
        
        this.parent = parent;
        selectionList = new Vector();
        isValidationMode = false;
        this.mp = null;
        
        // Un-hilite all geometrical objects...
        this.unSelectAll();
        // Remove current edited object if any
        this.parent.agp.reset();
        
        // Add parent listeners
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        
        // Popup menu stuff
        this.pop = new PopupMenu();
        this.pop.setFont(this.parent.getFont());
        this.pop.addActionListener(this);
        this.parent.add(this.pop);
        
        this.OPERATION = OPERATION;
        this.parent.setCursor("HAND");
        
        // Looks the edition status of this OpMode:
        // if another OpMode is in edition mode, returns an error message to the client,
        // else, if edition is pending, load the list of hilighted objects, if any
        KaboumOpModeStatus status = parent.opModeStatus;
        if (status.isEditionPending && status.opModeName.equals(this.getOpModeName())) {
            if (status.getIdList().length() > 0) {
                StringTokenizer tok = new StringTokenizer(status.getIdList(), ";");
                KaboumGeometryGlobalDescriptor tmpGGD = null;

                while (tok.hasMoreTokens()) {
                    tmpGGD = parent.GGDIndex.getGGD(tok.nextToken());
                    selectionList.addElement(tmpGGD);
                }
                isValidationMode = true;
            }
        }
        // register this object as the current edition tool
        status.opModeName = this.getOpModeName();
    }
    
    /**
     *
     * PAINT
     *
     */
    public void paint(Graphics g) {
        this.parent.GGDIndex.paint(g);
    }
    
    public void destroyEvent() {
        parent.removeMouseListener(this);
        parent.removeMouseMotionListener(this);
        /*
        if (this.pop != null) {
            this.pop.removeActionListener(this);
        }
         **/
    }
    
    
    /**
     *
     * Select a geometry.
     *
     * @param ggd Selected geometry
     *
     */
    public void selectGeometry(KaboumGeometryGlobalDescriptor ggd) {
        ggd.setSelected(true);
        // send a SELECTION command to the client
        parent.kaboumResult("SELECTION|" + ggd.id);
        //KaboumUtil.debug("Select geometry " + ggd.id);
    }
    
    
    /**
     *
     * Select a geometry.
     *
     * @param ggd Geometry to be unselected
     *
     */
    public void unSelectGeometry(KaboumGeometryGlobalDescriptor ggd) {
        ggd.setSelected(false);
    }
    
    /**
     *
     * Un-Select all geometries.
     *
     */
    public void unSelectAll() {
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        
        for (int i = 0; i < numGGD; i++) {
            this.unSelectGeometry(this.parent.GGDIndex.getVisibleGeometries()[i]);
        }
    }
    
    
    public void mouseClicked(MouseEvent evt) {
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        int numGGD = this.parent.GGDIndex.getVisibleGeometries().length;
        
        // Test is user clicked outside the popup menu
        if (pop.getItemCount() != 0) {
            pop.removeAll();
            return;
        }
        
        // Clear the contextual menu
        pop.removeAll();
        
        int mods = evt.getModifiers();
        int x = evt.getX();
        int y = evt.getY();
        
        boolean leftClicked = (mods & InputEvent.BUTTON1_MASK) != 0;
        boolean menuDisplayed = pop.getItemCount() != 0;
        boolean selectionValid = selectionList.size() > 1;
        
        if (leftClicked) {
            if (! isValidationMode) {
                for (int i = 0; i < numGGD; i++) {
                    
                    tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
                    
                    // Locked objects cannot be selectionned
                    if (tmpGGD.pd.isLocked()) {
                        continue;
                    }
                    
                    if (this.contains(tmpGGD, x, y)) {
                        // Add the new list (or remove it from the list if already present)
                        int indexOfObject = this.selectionList.indexOf(tmpGGD);
                        
                        if (indexOfObject != -1) {
                            this.unSelectGeometry(tmpGGD);
                            this.selectionList.removeElementAt(indexOfObject);
                        } else {
                            this.selectionList.addElement(tmpGGD);
                            this.selectGeometry(tmpGGD);
                        }
                    }
                }
            }
        } else {
            if (!menuDisplayed) {
                // a right click: either display a validate menu, or a operation/clear menu
                if (isValidationMode && parent.featureServerTools != null &&
                        ((KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_INTERSECTION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_INTERSECTION) ||
                        (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_UNION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_UNION))) {
                    
                    // now SpatialAnalysis is performed, can display a validate menu
                    addValidateMenu(x, y);
                } else if (selectionValid) {
                    // can display an operation/clear menu
                    addMenu(x, y);
                }
            }
        }
        parent.repaint();
        return;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(KaboumFeatureModes.getMode(this.OPERATION))) {
            if (this.selectionList.size() > 0) {
                
                pop.removeAll();
                parent.standbyOn();
                
                // build the list of selected geometries
                KaboumGeometryGlobalDescriptor tmpGGD;
                boolean first = true;
                String idString = "";
                String onTopId = "";
                for (Enumeration en = this.selectionList.elements(); en.hasMoreElements();) {
                    tmpGGD = (KaboumGeometryGlobalDescriptor) en.nextElement();
                    if (first) {
                        idString = tmpGGD.id;
                        onTopId = tmpGGD.id;
                        first = false;
                    } else {
                        idString += ";" + tmpGGD.id;
                    }
                }
                // stores this list globally, to allow this opMode to rebuild
                // the selected list after it has been destroyed
                parent.opModeStatus.setIdList(idString);
                parent.opModeStatus.opModeName = this.getOpModeName();
                parent.opModeStatus.isEditionPending = true;
                parent.opModeStatus.setOnTopID(onTopId);
                
                if (parent.featureServerTools != null &&
                        (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_INTERSECTION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_INTERSECTION) ||
                        (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_UNION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_UNION)) {
                    
                    // operation is sent on the server
                    Vector geoms = new Vector();
                    for (Enumeration en = selectionList.elements(); en.hasMoreElements();) {
                        tmpGGD = (KaboumGeometryGlobalDescriptor) en.nextElement();
                        geoms.addElement(tmpGGD.geometry);
                        ddName = tmpGGD.dd.name;
                    }
                    parent.featureServerTools.processSpatialAnalysis(ddName, geoms, OPERATION, false);
                    // JTS has generated a new geometry in this case. Sets its status to modified, 
                    // to display it with its modified color
                    KaboumGeometryGlobalDescriptor ggd = parent.GGDIndex.getGGD(Kaboum.K_NEW_GEOMETRY);
                    ggd.setModified(true);
                    parent.GGDIndex.onTopID = ggd.id;
                    destroyEvent();
                } else {
                    // Operation is processed by JS code
                    // Result form:
                    // SELECTION|<object1.id>;<object2.id>; ...
                    //
                    parent.kaboumResult(KaboumFeatureModes.getMode(this.OPERATION) + "|" + idString);
                }
                this.parent.setCursor("HAND");
                parent.repaint();
                return;
            }
        } else if ("CLEAR_SELECTION".equals(e.getActionCommand())) {
            this.unSelectAll();
            this.selectionList.removeAllElements();
            pop.removeAll();
            parent.repaint();
            return;
        } else if ("VALIDATE".equals(e.getActionCommand())) {
            // sends the new object for validation and removes all objects
            // involved in the operation.
            isValidationMode = false;
            KaboumGeometryGlobalDescriptor newGGD = parent.GGDIndex.getGGD(Kaboum.K_NEW_GEOMETRY);
            // complete the selectionList to add the K_NEW_OBJECT that must be removed from kaboum
            selectionList.addElement(newGGD);
            parent.opModeStatus.reset();
            parent.featureServerTools.validateAndRemove(newGGD, selectionList, "GEOMETRY_" + KaboumFeatureModes.getMode(this.OPERATION));
        } else if ("CANCEL".equals(e.getActionCommand() )) {
            parent.GGDIndex.removeGeometry(Kaboum.K_NEW_GEOMETRY);
            parent.opModeStatus.reset();
            unSelectAll();
            this.selectionList.removeAllElements();
            isValidationMode = false;
            pop.removeAll();
        } else {
            // In other case just send the command to the navigator
            parent.kaboumResult(e.getActionCommand());
            this.parent.setCursor("HAND");
        }
    }
    
    
    protected boolean contains(KaboumGeometryGlobalDescriptor ggd, int x, int y) {
        
        boolean result = false;
        double precision = (this.parent.mapServerTools.getRealExtent().dx() /
                this.parent.screenSize.width ) *
                KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_PIXELPRECISION), 1);
        
        try {
            result = KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(x, y), ggd.geometry, precision);
        } catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  KaboumAlgorithms class not found in jar file. SELECTION OpMode cannot work");
            return false;
        }
        
        return result;
    }
    
    /**
     * Displays the operation/clear menu at the given mouse coordinate.<br>
     * Sets the action events accordingly
     *@param x, y the mouse coordinate where the menu will be displayed
     */
    protected void addMenu(int x, int y) {
        MenuItem operationSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_"+ KaboumFeatureModes.getMode(this.OPERATION)));
        operationSelection.setActionCommand(KaboumFeatureModes.getMode(this.OPERATION));
        pop.add(operationSelection);
        
        //MenuItem tmpMenuItem = new MenuItem(parent.defaultLang.getString(this.parent.defaultLang.getString("GENERIC_SEPARATOR")));
        //pop.add(tmpMenuItem);
        
        MenuItem clearSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_CLEAR_SELECTION"));
        clearSelection.setActionCommand("CLEAR_SELECTION");
        pop.add(clearSelection);
        
        pop.show(parent, x, y);
    }
    
    /**
     * Displays the validate/Cancel menu at the given mouse coordinate.<br>
     * Sets the action events accordingly.
     *@param x, y the mouse coordinate where the menu will be displayed
     */
    protected void addValidateMenu(int x, int y) {
        
        MenuItem validateSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_VALIDATE"));
        validateSelection.setActionCommand("VALIDATE");
        pop.add(validateSelection);
        
        MenuItem cancelSelection = new MenuItem(this.parent.defaultLang.getString("GEOMETRYOPERATION_CANCEL"));
        cancelSelection.setActionCommand("CANCEL");
        pop.add(cancelSelection);
        
        pop.show(parent, x, y);
    }
    
    public void mouseDragged(MouseEvent e) {
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
    
    
    public void mouseMoved(MouseEvent e) {
        //this.mp = e.getPoint();
        //this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
}
