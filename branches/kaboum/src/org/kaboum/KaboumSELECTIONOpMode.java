/*
 *
 * Class KaboumSELECTIONOpMode from the Kaboum project.
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
 */
package org.kaboum;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import org.kaboum.algorithm.KaboumAlgorithms;
import org.kaboum.util.KaboumUtil;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumCoordinate;

/**
 *
 * This opMode allows to select one or more object from
 * parent.geoObjectList
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumSELECTIONOpMode extends KaboumOpMode {
    
    /** CONSTANTS */
    /** validation is done when a feature is selected (no right click to access a validation contextual menu */
    public static final String PARAM_AUTOSELECT = "SELECTION_AUTO_COMMIT";
    /** the precision, in pixels, when clicking on an object */
    public static final String PARAM_PIXELPRECISION = "SELECTION_PIXEL_PRECISION";
    /** should the clicked point coordinates be returned upon selection */
    public static final String PARAM_RETURNXY = "SELECTION_RETURN_XY";
    /** true to send the selected object(s) to the server with a addGeometry command
     * Great caution should be exercised when using this mode, as each selected objects will be added on the server
     * thus copying it on the underlying layer.
     * This mode is useful to implement a "copy features" feature in conjunction with a userMetadata object:
     * first set a userMetadata object with some parameters that your server code understand,
     * (for instance, the name of the source layer to copy from, the target layer to copy to)
     * Set the userMD object to kaboum before selecting a feature
     * upon selection and validation, overload the server's GeometryAccess class for the addGeometries method
     * and perform the custom copy, using the passed objects and UserMetadata object.
     */
    public static final String PARAM_SENDSELECTIONTOSERVER = "SELECTION_SEND_SELECTION_TO_SERVER";
    
    public static String[] getParametersList() {
        String[] list = new String[4];
        list[0]  = PARAM_AUTOSELECT;
        list[1]  = PARAM_PIXELPRECISION;
        list[2]  = PARAM_RETURNXY;
        list[3]  = PARAM_SENDSELECTIONTOSERVER;
        return list;
    }
    
    
    /** Parent reference */
    protected Kaboum parent;
    
    /** Popup menu (activated by right click mouse) */
    protected PopupMenu pop = null;
    
    /** Selection list (list of all the selected objects) */
    protected Hashtable selectionList = new Hashtable();
    
    /** True: multiselection mode */
    protected boolean multiSelect;
    
    /** the last clicked point, in geographic coordinates */
    protected KaboumCoordinate clickedPoint = null;
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumSELECTIONOpMode(Kaboum parent) {
        this(parent, false);
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumSELECTIONOpMode(Kaboum parent, boolean multiSelect) {
        
        this.parent = parent;
       
        this.multiSelect = multiSelect;
        
        // Un-hilite all geometrical objects...
        this.unSelectAll();
        
        // Remove current edited object if any
        this.parent.agp.reset();
        
        // Ajoute les listeners au parent
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        
        // Popup menu stuff
        this.pop = new PopupMenu();
        this.pop.setFont(this.parent.getFont());
        this.pop.addActionListener(this);
        this.parent.add(this.pop);
        
        // Definit la forme du curseur de la souris
        this.parent.setCursor("HAND");
        
        //this.parent.repaint();
    }
    
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param list List of pre-selectionned objects
     *
     */
    public KaboumSELECTIONOpMode(Kaboum parent, String list) {
        
        this(parent);
        
        //
        // Hilite pre-selectionned objects (list)
        // List form is : idA;idB;...
        //
        StringTokenizer st = new StringTokenizer(list, ";");
        KaboumGeometryGlobalDescriptor tmpGGD = null;
        
        int position = this.parent.GGDIndex.getGGDIndex(st.nextToken());
        
        if (position != -1) {
            tmpGGD = (KaboumGeometryGlobalDescriptor) this.parent.GGDIndex.elementAt(position);
            // Add the new list
            this.parent.GGDIndex.shiftOnTop(tmpGGD.id);
            this.selectionList.put(new Integer(position), tmpGGD);
            this.selectGeometry(tmpGGD);
        }
        
        this.parent.repaint();
    }
    
    
    /**
     *
     * PAINT
     *
     */
    public void paint(Graphics g) {
        this.parent.GGDIndex.paint(g);
    }
    
    public void  destroyEvent() {
        this.parent.removeMouseListener(this);
        this.parent.removeMouseMotionListener(this);
        this.unSelectAll();
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
        //Logs
        KaboumUtil.debug("Select geometry " + ggd.id);
        
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
        boolean returnXY = KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_RETURNXY), false);        
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
        // stores clicked coordinates, converted into geographic coordinates
        clickedPoint = this.parent.mapServerTools.mouseXYToInternal(x, y);
        //
        // All buttons except button1 open the
        // popup menu.
        //
        boolean menuButton = true;
        boolean test = false;
        
        if ((mods & InputEvent.BUTTON1_MASK) != 0) {
            menuButton = false;
        }
        if (((mods & InputEvent.CTRL_MASK) != 0) && this.multiSelect) {
            for (int i = 0; i < numGGD; i++) {
                tmpGGD = this.parent.GGDIndex.getVisibleGeometries()[i];
                // Locked objects cannot be selectionned
                if (tmpGGD.pd.isLocked()) {
                    continue;
                }
                if (this.contains(tmpGGD, x, y)) {
                    test = true;
                    
                    // Add the new list (or remove it from the list if already present)
                    if (this.selectionList.get(tmpGGD.id) != null) {
                        this.unSelectGeometry(tmpGGD);
                        this.selectionList.remove(tmpGGD.id);
                    }
                    else {
                        this.selectionList.put(tmpGGD.id, tmpGGD);
                        this.selectGeometry(tmpGGD);
                    }
                }
            }
            if (!test) {
                this.unSelectAll();
                this.selectionList.clear();
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
                        // Un-selected all elements
                        // and clear the list
                        this.unSelectAll();
                        this.selectionList.clear();
                        test = true;
                        
                    }
                    // puts selected geometry on top
                    this.parent.GGDIndex.shiftOnTop(tmpGGD.id);
                    this.selectionList.put(tmpGGD.id, tmpGGD);
                    this.selectGeometry(tmpGGD);
                    
                    // AutoSelect activated ---> send the result
                    if ((KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_AUTOSELECT), false)) && (!menuButton)) {
                        this.actionPerformed(new ActionEvent(this, 0, "VALIDATE"));
                        // reset top id
                        this.parent.GGDIndex.resetOnTopID();
                        return;
                    }
                } else {
                // This is a bug: in case of superposed polygons
                    MenuItem tmpMenuItem;
                    String tmpKey;
                    
                    // Does the object get a title?
                    if (tmpGGD.getTitleMenuString() != null) {
                        tmpMenuItem = new MenuItem(tmpGGD.getTitleMenuString());
                        pop.add(tmpMenuItem);
                        tmpMenuItem = new MenuItem(parent.defaultLang.getString(this.parent.defaultLang.getString("GENERIC_SEPARATOR")));
                        pop.add(tmpMenuItem);
                    }
                    // Add an item menu for each action
                    for (Enumeration f = tmpGGD.pd.getMenuHash().keys(); f.hasMoreElements();) {
                        tmpKey = (String) f.nextElement();
                        tmpMenuItem = new MenuItem((String) tmpGGD.pd.getMenuHash().get(tmpKey));
                        
                        //
                        // Result form:
                        // FUNCTION|pd|id|keyword
                        //
                        tmpMenuItem.setActionCommand("FUNCTION|"+tmpGGD.pd.getName()+"|"+tmpGGD.id+"|"+tmpKey);
                        pop.add(tmpMenuItem);
                    }
                }
                // In single selection mode, you can't select more than one object
                if (!this.multiSelect) {
                    break;
                }
            } // end if contains
        } // end for each visible geometry
        if (menuButton) {
            if (this.selectionList.size() > 0 || returnXY) {
                MenuItem validateSelection = new MenuItem(this.parent.defaultLang.getString("SELECTION_VALIDATE"));
                validateSelection.setActionCommand("VALIDATE");
                pop.add(validateSelection);
            }
            if (pop.getItemCount() > 0) {
                pop.show(parent, x, y);
            }
        }
        // If nothing was selected, empty the list
        if (!test && !menuButton) {
            this.unSelectAll();
            this.selectionList.clear();
            
            // in return XY mode, send clicked coordinate each time
            if (returnXY && KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_AUTOSELECT), false)) {
                this.actionPerformed(new ActionEvent(this, 0, "VALIDATE"));
                // reset top id
                this.parent.GGDIndex.resetOnTopID();
            }
        }
        
        parent.repaint();
        return;
    }
    
    
    //@Override
    public void actionPerformed(ActionEvent e) {
        boolean returnXY = KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_RETURNXY), false);
        if ("VALIDATE".equals(e.getActionCommand())) {
            // also validate click if mode is return XY and no object is found
            if (this.selectionList.size() > 0 || returnXY) {
                
                pop.removeAll();
                
                // Construct the idString from the selection
                KaboumGeometryGlobalDescriptor tmpGGD;
                boolean first = true;
                String idString = "";
                // vector of geometries to  send to server if sendselectiontoserver is true
                Vector geoms = new Vector();
                KaboumUtil.debug("send ? : " + KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SENDSELECTIONTOSERVER), false));
                boolean sendOnServer = KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_SENDSELECTIONTOSERVER), false);
                String className = "";

                for (Enumeration en = this.selectionList.elements(); en.hasMoreElements();) {
                    tmpGGD = (KaboumGeometryGlobalDescriptor) en.nextElement();
                    if (first) {
                        idString = tmpGGD.id;
                        first = false;
                    }
                    else {
                        idString += ";" + tmpGGD.id;
                    }
                    if (sendOnServer) {
                        geoms.add(tmpGGD.geometry);
                        // fixme: only one classname allowed for selection or not ?
                        className = tmpGGD.dd.getName();
                    }
                }
                // reset top id
                this.parent.GGDIndex.resetOnTopID();
                //
                // Result form:
                // SELECTION|<object1.id>;<object2.id>; ...
                // if RETURN_XY parameter is set, form is:
                // SELECTION|<object1.id>;<object2.id>;...|X Y
                // where X Y are the mouse coordinates in geographic units
                //
                String res = "SELECTION|"+idString;
                if (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_RETURNXY), false) &&
                        this.clickedPoint != null) {
                    res += "|" + clickedPoint.x + " " + clickedPoint.y;
                }
                parent.kaboumResult(res);

                if (sendOnServer) {
                    KaboumUtil.debug("sending selected object(s) to server: " + idString);
                    this.parent.featureServerTools.AddGeometries(geoms, className);
                }
                this.parent.setCursor("HAND");
                
                parent.repaint();
                
                return;
            }
        }
        
        // In other case just send the command to the navigator
        else {
            parent.kaboumResult(e.getActionCommand());
            
            this.parent.setCursor("HAND");
            
        }
    }
    
    
    private boolean contains(KaboumGeometryGlobalDescriptor ggd, int x, int y) {
        
        boolean result = false;
        double precision = (this.parent.mapServerTools.getRealExtent().dx() / this.parent.screenSize.width ) * KaboumUtil.stoi(this.parent.getOpModeProperty(PARAM_PIXELPRECISION), 1);
        
        try {
            result = KaboumAlgorithms.doesPointLieInGeometry(this.parent.mapServerTools.mouseXYToInternal(x, y), ggd.geometry, precision);
        }
        catch (Exception jse) {
            KaboumUtil.debug(" WARNING ! :  KaboumAlgorithms class not found in jar file. SELECTION OpMode cannot work");
            return false;
        }
        
        return result;
    }
    
    public void mouseDragged(MouseEvent e) {
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
    
    
    public void mouseMoved(MouseEvent e) {
        this.mp = e.getPoint();
        this.parent.refreshTooltip();
        this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(e.getX(), e.getY()));
    }
    
}
