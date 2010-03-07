/*
 * KaboumOpModeStatus.java
 *
 * Created on November 18, 2005, 3:44 PM
 *
 */

package org.kaboum;

import org.kaboum.geom.KaboumGeometryGlobalDescriptor;


/**
 * A class responsible for storing edition OpMode status, allowing
 * Edition opMode tools to change their current mode.
 * <p>
 * It is up to each opMode to deal with this object to guarantee a normal behaviour:
 * setting the opMode name and object id list when performing a spatial analysis, 
 * clearing the object when validating or canceling operation.<b>
 * This object is attached to Kaboum, to allow other objects to access it.
 *(the initial singleton pattern was removed, due to problems when several Kaboum instances
 * exist in a single VM)
 * </p>
 * @author Nicolas
 */
public class KaboumOpModeStatus {
    /** the name of the current opMode drawer storing its status in this object */
    public String opModeName;
    
    /** true to indicate that opModeName is currently in edition mode, waiting for the
     * user to validate or cancel the operation
     */
    public boolean isEditionPending;
    
    /** the | separated list of KaboumGeometryGlobalDescriptor objects ids handled by the current OpMode */
    private String idList;

    /** the object to draw ontop (edited object for instance) */
    private String onTopID;

    /** The eventual user-drawn object used for the OpMode, like for instance
     * the linestring drawn in the POLYGON_SPLITTING_BY_LINE opMode
     */
    private KaboumGeometryGlobalDescriptor editedGGD;
    /** Creates a new instance of KaboumOpModeStatus.
     */
    public KaboumOpModeStatus() {
        opModeName = "";
        isEditionPending = false;
        idList = "";
        onTopID = "";
    }
    
    /**
     * Resets the status of this object (empties vectors and sets opModeName to ""
     * This method should be called after an tool OpMode has finished its cycle (validation or cancel)
     */
    public void reset() {
        opModeName = "";
        isEditionPending = false;
        idList = "";
        onTopID = "";
        editedGGD = null;
    }
    
    /** sets this idList by copying the given String */
    public void setIdList(String il) {
        idList = new String(il);
    }
    
    /** gets this idList */
    public String getIdList() {
        return idList;
    }

    public KaboumGeometryGlobalDescriptor getEditedGGD() {
        return editedGGD;
    }

    public void setEditedGGD(KaboumGeometryGlobalDescriptor editedGGD) {
        this.editedGGD = editedGGD;
    }


    public String getOnTopID() {
        return onTopID;
    }

    public void setOnTopID(String onTopID) {
        this.onTopID = onTopID;
    }

    
    
    /** returns a string representation of this object
     */
    public String toString() {
        String res = "hashcode: " + this.hashCode() + "\n";
        res += "opModeName: " + opModeName + "\n";
        res += "isEditionPending: " + isEditionPending + "\n";
        res += "idList: " + idList + "\n";
        res += "onTopID: " + onTopID + "\n";
        res += "editedGGD: " + (editedGGD == null ? "null" : editedGGD.toString()) + "\n";
        return res;
    }
    
    /**
     * Tests if given drawer name is an edition drawer or not, by comparing it to the list of 
     * existing edition drawer.
     * Fixme: move this logic at the drawer level (isEdition), because user-defined drawers may exist
     * 
     *@param drawerName the name of the drawer to test
     *@return true if given name is an edition drawer name, false otherwise
     */
    public boolean isEditionDrawer(String drawerName) {
        return ("GEOMETRY_ASYMETRIC_OPERATION".equals(drawerName) ||
                "GEOMETRY_SYMETRIC_OPERATION".equals(drawerName) ||
                "GEOMETRY_K_DIFFERENCE".equals(drawerName) ||
                "GEOMETRY_K_FUSION".equals(drawerName) ||
                "GEOMETRY_K_HOLES_COMPLETION".equals(drawerName) ||
                "GEOMETRY_K_INTERSECTION".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_COMPLETION".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_COMPLETION_FITTING".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_ERASING".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_FITTING".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_SPLITTING".equals(drawerName) ||
                "GEOMETRY_K_POLYGON_SPLITTING_BY_LINE".equals(drawerName) ||
                "GEOMETRY_K_UNION".equals(drawerName));
    }
}











