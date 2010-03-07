/*
 *
 * Class KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode from the Kaboum project.
 * Class to complete (union) a selected polygon with another drawn polygon.
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

import java.awt.event.ActionEvent;
import java.util.Vector;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumFeatureModes;
import org.kaboum.util.KaboumUtil;

/**
 *
 * Class to complete (union) a selected polygon with another drawn polygon.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode extends KaboumGEOMETRY_K_POLYGON_SPLITTINGOpMode {
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode(Kaboum parent) {
        super(parent, KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING);
    }
    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     * @param idList Object return list send by KaboumServer
     *
     */
    public KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode(Kaboum parent, short OPERATION) {
        super(parent, KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING);
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
        if ((KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode.selectedGGD != null) && (this.parent.agp.activeGGD != null)) {
            
            this.pop.removeAll();
            this.parent.standbyOn();
            
            // closes the drawn polygon
            if (! isRingClosed) {
                KaboumCoordinate currentCoordinateClicked = this.parent.mapServerTools.mouseXYToInternal(this.freezedMousePosition.x, this.freezedMousePosition.y);
                this.parent.agp.activeSimpleGeometry.addCoordinate(currentCoordinateClicked);
                isRingClosed = true;
            }
            
            this.parent.agp.activeSimpleGeometry.addCoordinate(this.parent.agp.activeSimpleGeometry.getExteriorCoordinates()[0]);
            this.parent.agp.activeGGD.geometry.normalize();
            this.parent.agp.activeGGD.setEdited(false);
            
            String idString = KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode.selectedGGD.id +
                    ";" +
                    this.parent.agp.activeGGD.id;
            parent.opModeStatus.setIdList(idString);
            parent.opModeStatus.opModeName = this.getOpModeName();
            parent.opModeStatus.isEditionPending = true;
            parent.opModeStatus.setOnTopID(KaboumGEOMETRY_K_POLYGON_COMPLETION_FITTINGOpMode.selectedGGD.id);
            
            if (parent.featureServerTools != null &&
            (KaboumUtil.stob(this.parent.getOpModeProperty(PARAM_COMPLETION_FITTING_ON_SERVER)) &&
            this.OPERATION == KaboumFeatureModes.K_POLYGON_COMPLETION_FITTING)) {
                // operation is sent on the server
                Vector geoms = new Vector(2);
                geoms.addElement(this.selectedGGD.geometry);
                geoms.addElement(this.parent.agp.activeGGD.geometry);
                selectedGGD.setSelected(false);
                for (int i = 0; i < this.parent.GGDIndex.getVisibleGeometries().length; i++) {
                    // do not add K_NEW or selectedGeom.
                    if (! this.parent.GGDIndex.getVisibleGeometries()[i].id.equals(Kaboum.K_NEW_GEOMETRY) &&
                    ! this.parent.GGDIndex.getVisibleGeometries()[i].id.equals(selectedGGD.id)) {
                        geoms.addElement(this.parent.GGDIndex.getVisibleGeometries()[i].geometry);
                    }
                }
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
}
