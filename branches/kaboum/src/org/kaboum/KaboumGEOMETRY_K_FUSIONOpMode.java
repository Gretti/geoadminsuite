/*
 * KaboumGEOMETRY_K_UNION_INSIDEOpMode.java
 *
 * Created on 5 octobre 2005, 12:03
 */

package org.kaboum;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumUtil;

/**
 * Realize the fusion of first selected polygon inside the second selected polygon.
 * To select polygons, an arrow is drawn. Its start point is used to find any polygon containing
 * this point, same with end point for the second polygon.
 *<p>
 * if first and second points lie on separated polygons, the fusion will be made between
 * first polygon and second one, preserving second one.
 *</p>
 * @author Nicolas Ribot
 */
public class KaboumGEOMETRY_K_FUSIONOpMode extends KaboumGEOMETRY_SYMETRIC_OPERATIONOpMode {
    /**
     * inner class representing an arrow object in the screen coordinates,
     * Used to select source and target polygon
     */
    private class Arrow {
        public Point startPoint;
        private Point endPoint;
        public boolean freezed;
        private Polygon tip;
        private Color color;
        private int tipLength;
        
        public Arrow() {
            freezed = false;
        }
        
        /**
         * Builds an arrow with given start point, color and tip length
         */
        public Arrow(Point sp, Color color, int tipLenth ) {
            super();
            startPoint = sp;
            this.color = color;
            this.tipLength = tipLenth;
        }
        
        public void setEndPoint(Point p) {
            endPoint = p;
            
            computeTip();
        }
        
        private void computeTip() {
            if (startPoint == null || endPoint == null) {
                return;
            }
            tip = new Polygon();
            // first tip point
            Point M1 = new Point();
            // second tip point
            Point M2 = new Point();
            //arrow length
            double d = Math.sqrt( Math.pow((endPoint.x - startPoint.x), 2) + Math.pow((endPoint.y - startPoint.y), 2) );
            // tip angle
            double a = Math.PI / 6;
            // coef to multiply the arrow to get a distance of tipLength from the arrow vertex
            double coef = tipLength / d;
            // tip base
            Point M = new Point();
            M.x = (int) (coef * startPoint.x + endPoint.x * (1-coef));
            M.y = (int) (coef * startPoint.y + endPoint.y * (1-coef));
            Point OM = new Point(M.x - endPoint.x, M.y - endPoint.y);

            Point OM1 = new Point( (int) (Math.cos(a) * OM.x + Math.sin(a) * OM.y), (int) (Math.cos(a) * OM.y - Math.sin(a) * OM.x));
            M1.x = OM1.x + endPoint.x;
            M1.y = OM1.y + endPoint.y;
            
            a = 0.0 - (a);
            Point OM2 = new Point( (int) (Math.cos(a) * OM.x + Math.sin(a) * OM.y), (int) (Math.cos(a) * OM.y - Math.sin(a) * OM.x));
            M2.x = OM2.x + endPoint.x;
            M2.y = OM2.y + endPoint.y;
            
            tip.addPoint(M1.x, M1.y);
            tip.addPoint(M2.x, M2.y);
            tip.addPoint(endPoint.x, endPoint.y);
        }
        
        /**
         * Paints this arrrow in the given Graphics
         */
        public void paint(Graphics g) {
            Color previousColor = g.getColor();
            g.setColor(color);
            
            if (startPoint != null) {
                g.fillOval(startPoint.x - 3, startPoint.y - 3, 6, 6);
                
                if (endPoint != null) {
                    g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
                    g.fillPolygon(tip);
                }
            }
            // restore previous color
            g.setColor(previousColor);
            
        }
    }
    /** the arrow created by user when clicking in the applet
     */
    protected Arrow arrow;
    
    protected Color arrowColor;
    
    /** parameter telling if FUSION spatial analyis should be made on the server (default is true) */
    public static final String PARAM_FUSION_ON_SERVER = "SERVER_DO_FUSION";
    /** color of arrow */
    public static final String PARAM_FUSION_ARROW_COLOR = "GEOMETRY_FUSION_ARROW_COLOR";
    /** arrow tip length */
    public static final String PARAM_FUSION_ARROW_LENGTH = "GEOMETRY_FUSION_ARROW_LENGTH";
    
    public static String[] getParametersList() {
        String[] list = new String[4];
        list[0]  = PARAM_PIXELPRECISION;
        list[1]  = PARAM_FUSION_ON_SERVER;
        list[2]  = PARAM_FUSION_ARROW_COLOR;
        list[3]  = PARAM_FUSION_ARROW_LENGTH;
        return list;
    }
    
    /** Creates a new instance of KaboumGEOMETRY_K_UNION_INSIDEOpMode */
    public KaboumGEOMETRY_K_FUSIONOpMode(Kaboum parent) {
        super(parent, KaboumFeatureModes.K_FUSION);
        // in this mode, only 2 objects can be selected
        arrowColor = Color.red;
        KaboumUtil.debug("building a FUSION tool: " + this.hashCode());
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
        
        boolean leftClick = (mods & InputEvent.BUTTON1_MASK) != 0;
        boolean menuDisplayed = pop.getItemCount() != 0;
        boolean selectionValid = selectionList.size() > 1;
        
        if (leftClick) {
            if (isValidationMode) {
            } else if (selectionList == null || selectionList.size() == 0 || selectionList.size() == 2) {
                // first click: initiate arrow
                unSelectAll();
                selectionList = new Vector(2);
                arrow = new Arrow(
                        new Point(x, y),
                        KaboumUtil.getColorParameter(parent.getOpModeProperty(PARAM_FUSION_ARROW_COLOR), Color.red),
                        KaboumUtil.stoi(parent.getOpModeProperty(PARAM_FUSION_ARROW_LENGTH), 10));
                
                tmpGGD = getGGDAt(x, y);
                
                if (tmpGGD != null) {
                    this.selectionList.addElement(tmpGGD);
                    this.selectGeometry(tmpGGD);
                } else {
                    // bad arrow: removes it:
                }
            } else if (selectionList != null && selectionList.size() == 1) {
                // second click: finalize the arrow
                arrow.setEndPoint(new Point(x, y));
                this.repaint();
                
                tmpGGD = getGGDAt(x, y);
                
                if (tmpGGD != null && ! tmpGGD.id.equals(((KaboumGeometryGlobalDescriptor)selectionList.elementAt(0)).id)) {
                    // a valid second polygon
                    this.selectionList.addElement(tmpGGD);
                    this.selectGeometry(tmpGGD);
                    parent.GGDIndex.onTopID = tmpGGD.id;
                    arrow.freezed = true;
                }
            }
            evt.consume();
        } else {
            // a right click
            if (!menuDisplayed) {
                // a right click: either display a validate menu, or a operation/clear menu
                if (isValidationMode && parent.featureServerTools != null &&
                        ((KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_FUSION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_FUSION))) {
                    
                    // now SpatialAnalysis is performed, can display a validate menu
                    addValidateMenu(x, y);
                } else if (selectionValid) {
                    // can display an operation/clear menu
                    addMenu(x, y);
                } else {
                    // a way to cancel arrow, if there is only one polygon displayed.
                    KaboumUtil.debug("canceling FUSION tool by right click");
                    //todo: put all reset code into one method, to factorize
                    parent.opModeStatus.reset();
                    unSelectAll();
                    this.selectionList.removeAllElements();
                    arrow = null;
                    isValidationMode = false;
                    pop.removeAll();
                }
            }
            evt.consume();
        }
        parent.repaint();
    }
    
    public void mouseMoved(MouseEvent evt) {
        if (arrow != null) {
            if (arrow.freezed) {
                return;
            }
            arrow.setEndPoint(new Point(evt.getX(), evt.getY()));
        }
        parent.repaint();
        evt.consume();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(KaboumFeatureModes.getMode(this.OPERATION))) {
            if (this.selectionList.size() > 0) {
                
                pop.removeAll();
                parent.standbyOn();
                
                // build the list of selected geometries
                KaboumGeometryGlobalDescriptor tmpGGD;
                String selectedObjectId = ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(0)).id;
                String idString = selectedObjectId;
                idString += ";";
                idString += ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).id;

                // stores this list globally, to allow this opMode to rebuild
                // the selected list after it has been destroyed
                parent.opModeStatus.setIdList(idString);
                parent.opModeStatus.opModeName = this.getOpModeName();
                parent.opModeStatus.isEditionPending = true;
                if (parent.featureServerTools != null &&
                        (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_FUSION_ON_SERVER)) &&
                        this.OPERATION == KaboumFeatureModes.K_FUSION)) {
                    
                    // operation is sent on the server, sending the target object first
                    Vector geoms = new Vector(2);
                    geoms.addElement(((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).geometry);
                    geoms.addElement(((KaboumGeometryGlobalDescriptor)selectionList.elementAt(0)).geometry);
                    ddName = ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).dd.name;
                    // set onTopID to display the merged object on top:
                    parent.opModeStatus.setOnTopID(((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).id);
                    parent.featureServerTools.processSpatialAnalysis(ddName, geoms, OPERATION, true);
                    ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).setModified(true);
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
            arrow = null;
            return;
        } else if ("VALIDATE".equals(e.getActionCommand())) {
            // sends the new object for validation and removes all objects
            // involved in the operation.
            isValidationMode = false;
            parent.opModeStatus.reset();
            Vector vec = new Vector(1);
            vec.addElement(selectionList.elementAt(0));
            parent.featureServerTools.validateAndRemove(
                    (KaboumGeometryGlobalDescriptor)selectionList.elementAt(1), 
                    vec, 
                    "GEOMETRY_" + KaboumFeatureModes.getMode(this.OPERATION));
            ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).setModified(false);
        } else if ("CANCEL".equals(e.getActionCommand())) {
            ((KaboumGeometryGlobalDescriptor)selectionList.elementAt(1)).setModified(false);
            parent.featureServerTools.restoreGeometry();
            parent.opModeStatus.reset();
            unSelectAll();
            this.selectionList.removeAllElements();
            arrow = null;
            isValidationMode = false;
            pop.removeAll();
        } else {
            // In other case just send the command to the navigator
            parent.kaboumResult(e.getActionCommand());
            this.parent.setCursor("HAND");
        }
        
    }
    /**
     * paints the arrow onto the given Graphics
     */
    public void paint(Graphics g) {
        super.paint(g);
        
        if (arrow != null) {
            arrow.paint(g);
        }
    }
    
    /** returns the KaboumGeometryGlobalDescriptor object containing the given point, composed
     * of x and y coordinates, or null if no object is found
     *
     *@param x the x-coordinate of the point
     *@param y the y-coordinate of the point
     *
     *@return a KaboumGeometryGlobalDescriptor object containing the given coordinates, or null
     * if there are no objects under these coordinates
     */
    public KaboumGeometryGlobalDescriptor getGGDAt(int x, int y) {
        KaboumGeometryGlobalDescriptor ggd = null;
        
        if (this.parent.GGDIndex == null) {
            return null;
        }
        
        for (int i = 0; i < parent.GGDIndex.getVisibleGeometries().length; i++) {
            
            ggd = this.parent.GGDIndex.getVisibleGeometries()[i];
            
            // Locked objects cannot be selectionned
            if (ggd.pd.isLocked()) {
                continue;
            }
            
            if (this.contains(ggd, x, y)) {
                // found !
                return ggd;
            }
        }
        return null;
    }
}
