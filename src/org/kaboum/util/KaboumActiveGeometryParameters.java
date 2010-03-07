/*
 * KaboumActiveGeometryParameters.java
 *
 * Created on October 3, 2005, 10:05 PM
 */

package org.kaboum.util;

import org.kaboum.Kaboum;
import org.kaboum.geom.KaboumGGDIndex;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;

/**
 *
 * @author jrom
 */
public class KaboumActiveGeometryParameters {
    /** Active GGD */
    public KaboumGeometryGlobalDescriptor activeGGD = null;
    
    /** Active geometry (MUST NOT BE A GEOMETRY COLLECTION!) */
    public KaboumGeometry activeSimpleGeometry = null;
    
    /** Savec Coordinates array */
    public KaboumCoordinate[] savedCoordinates = null;
    
    /** Active Point clicked position within the current coordinate vector */
    public int activePointClickedPosition = -1;
    
    private Kaboum parent;
    
    
    /** Creates a new instance of KaboumActiveGeometryParameters */
    public KaboumActiveGeometryParameters(Kaboum parent) {
        this.parent = parent;
    }
    
    /**
     *
     * Reset the current active geometry
     *
     */
    public void reset() {
        if (this.activeGGD != null) {
            if (Kaboum.K_NEW_GEOMETRY.equals(this.activeGGD.id)) {
                this.parent.GGDIndex.removeGeometry(this.activeGGD.id);
            }
        }
        this.activePointClickedPosition = -1;
        this.activeGGD = null;
        this.parent.activeGGD = null;
        this.activeSimpleGeometry = null;
        this.savedCoordinates = null;
        this.parent.GGDIndex.onTopID = KaboumGGDIndex.RESERVED_ID;
    }
    
}
