/*
 *
 * Class KaboumGEOMETRY_K_DIFFERENCEOpMode from the Kaboum project.
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
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumUtil;


/**
 *
 * This opMode allows to select one or more object from
 * parent.geoObjectList and send an operation request
 * to KaboumServer
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_ASYMETRIC_OPERATIONOpMode extends KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode {
    
    /** Key whose value tells if DIFFERENCE spatial analyis should be made on the server (default is true) */
    public static final String PARAM_DIFFERENCE_ON_SERVER = "SERVER_DO_DIFFERENCE";
    /** Key whose value tells if SYMMETRIC DIFFERENCE spatial analyis should be made on the server (default is true) */
    public static final String PARAM_SYM_DIFFERENCE_ON_SERVER = "SERVER_DO_SYM_DIFFERENCE";
    
    public static String[] getParametersList() {
        String[] list = new String[3];
        list[0]  = PARAM_PIXELPRECISION;
        list[1]  = PARAM_DIFFERENCE_ON_SERVER;
        list[2]  = PARAM_SYM_DIFFERENCE_ON_SERVER;
        return list;
    }
    
    /**
     * Constructor
     *
     * @param parent Kaboum applet reference
     * @param OPERATION the type of operation for this asymetric operation
     * (@see KaboumFeatureModes for the list of available operations)
     *
     */
    public KaboumGEOMETRY_ASYMETRIC_OPERATIONOpMode(Kaboum parent, short OPERATION) {
        super(parent, OPERATION);
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
        
        //
        // All buttons except button1 open the
        // popup menu.
        //
        boolean menuButton = true;
        boolean test = false;
        
        if ((mods & InputEvent.CTRL_MASK) != 0) {
            
            menuButton = false;
            
            for (int i = 0; i < this.selectionList.size(); i++) {
                
                tmpGGD = (KaboumGeometryGlobalDescriptor) this.selectionList.elementAt(i);
                
                if (this.contains(tmpGGD, x, y)) {
                    break;
                }
            }
            
            if (tmpGGD != null) {
                
                for (int i = 0; i < this.selectionList.size(); i++) {
                    ((KaboumGeometryGlobalDescriptor)this.selectionList.elementAt(i)).setSuperHilite(false);
                }
                
                tmpGGD.setSuperHilite(true);
                
                parent.repaint();
                
            }
            
            return;
            
        }
        
        if ((mods & InputEvent.BUTTON1_MASK) != 0) {
            
            menuButton = false;
            
            for (int i = 0; i < numGGD; i++) {
                
                tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
                
                // Locked objects cannot be selectionned
                if (tmpGGD.pd.isLocked()) {
                    continue;
                }
                
                if (this.contains(tmpGGD, x, y)) {
                    test = true;
                    
                    // Add the new list (or remove it from the list if already present)
                    int indexOfObject = this.selectionList.indexOf(tmpGGD);
                    if (indexOfObject != -1) {
                        this.selectionList.removeElementAt(indexOfObject);
                        if (tmpGGD.isSuperHilite()) {
                            if (this.selectionList.size() != 0) {
                                ((KaboumGeometryGlobalDescriptor) this.selectionList.elementAt(0)).setSuperHilite(true);
                            }
                        }
                        this.unSelectGeometry(tmpGGD);
                    } else {
                        this.selectionList.addElement(tmpGGD);
                        this.selectGeometry(tmpGGD);
                        if (this.selectionList.size() == 1) {
                            tmpGGD.setSuperHilite(true);
                        }
                    }
                }
                
            }
            
            parent.repaint();
            
            return;
        }
        
        for (int i = 0; i < numGGD; i++) {
            
            tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
            
            // Locked objects cannot be selectionned
            if (tmpGGD.pd.isLocked()) {
                continue;
            }
            
            if (this.contains(tmpGGD, x, y)) {
                
                // Add the new list
                if (!menuButton) {
                    
                    if (!test) {
                        
                        //
                        // Un-selected all elements
                        // and clear the list
                        //
                        this.unSelectAll();
                        this.selectionList.removeAllElements();
                        test = true;
                        
                    }
                    
                    this.selectionList.addElement(tmpGGD);
                    this.selectGeometry(tmpGGD);
                    
                }
                
                // This is a bug: in case of superposed polygons
                else {
                    
                    MenuItem tmpMenuItem;
                    String tmpKey;
                    
                    // Does the object get a title?
                    if (tmpGGD.getTitleMenuString() != null) {
                        tmpMenuItem = new MenuItem(tmpGGD.getTitleMenuString());
                        pop.add(tmpMenuItem);
                        tmpMenuItem = new MenuItem(parent.defaultLang.getString(this.parent.defaultLang.getString("GENERIC_SEPARATOR")));
                        pop.add(tmpMenuItem);
                    }
                    
                }
                
            }
            
        }
        
        if (menuButton) {
            if (isValidationMode) {
                addValidateMenu(x, y);
            } else {
                addMenu(x, y);
            }
        }
        
        // If nothing was selected, empty the list
        if (!test && !menuButton) {
            this.unSelectAll();
            this.selectionList.removeAllElements();
        }
        
        parent.repaint();
        
        return;
    }
    
    
    public void actionPerformed(ActionEvent e) {
        
        if (e.getActionCommand().equals(KaboumFeatureModes.getMode(this.OPERATION))) {
            
            if (this.selectionList.size() > 0) {
                
                pop.removeAll();
                parent.standbyOn();
                
                // Construct the idString and vector of geometries from the selection
                KaboumGeometryGlobalDescriptor tmpGGD = null;
                KaboumGeometryGlobalDescriptor slGGD = null;
                Vector geoms = new Vector();
                String className = null;
                boolean first = true;
                String idString = "";
                int numGGD = this.selectionList.size();
                
                
                // Re-order the list to get the first element as the superHilited one
                for (Enumeration en = this.selectionList.elements(); en.hasMoreElements();) {
                    slGGD = (KaboumGeometryGlobalDescriptor) en.nextElement();
                    if (slGGD.isSuperHilite()) {
                        idString = slGGD.id;
                        geoms.addElement(slGGD.geometry);
                        className = slGGD.dd.name;
                        break;
                    }
                }
                
                for (Enumeration en = this.selectionList.elements(); en.hasMoreElements();) {
                    tmpGGD = (KaboumGeometryGlobalDescriptor) en.nextElement();
                    if (! slGGD.id.equals(tmpGGD.id)) {
                        idString += ";" + tmpGGD.id;
                        geoms.addElement(tmpGGD.geometry);
                    }
                }
                // stores this list globally, to allow this opMode to rebuild
                // the selected list after it has been destroyed
                parent.opModeStatus.setIdList(idString);
                parent.opModeStatus.opModeName = this.getOpModeName();
                parent.opModeStatus.isEditionPending = true;
                        
                if (parent.featureServerTools != null &&
                (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_DIFFERENCE_ON_SERVER)) &&
                this.OPERATION == KaboumFeatureModes.K_DIFFERENCE) ||
                (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SYM_DIFFERENCE_ON_SERVER)) &&
                this.OPERATION == KaboumFeatureModes.K_SYM_DIFFERENCE)) {
                    parent.featureServerTools.processSpatialAnalysis(className, geoms, OPERATION, false);
                    parent.GGDIndex.getGGD(Kaboum.K_NEW_GEOMETRY).setModified(true);
                } else {
                    //
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
        } else if ("CANCEL".equals(e.getActionCommand())) {
            parent.GGDIndex.removeGeometry(Kaboum.K_NEW_GEOMETRY);
            unSelectAll();
            this.selectionList.removeAllElements();
            parent.opModeStatus.reset();
            isValidationMode = false;
            pop.removeAll();
        } else {
            // In other case just send the command to the navigator
            parent.kaboumResult(e.getActionCommand());
            
            this.parent.setCursor("HAND");
            
        }
    }
}
