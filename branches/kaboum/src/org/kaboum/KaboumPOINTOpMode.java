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
import java.awt.event.ActionEvent;

import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumWKTReader;
import org.kaboum.geom.KaboumGeometryFactory;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumPoint;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;

/**
 *
 * Create or modify KaboumPoint object within the applet.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumPOINTOpMode extends KaboumGEOMETRYOpMode {

    
    /**
     *
     * Constructor
     *
     * @param parent Parent reference
     *
     */
    public KaboumPOINTOpMode(Kaboum parent) {
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
    public KaboumPOINTOpMode(Kaboum parent, String id) {
        super(parent, id);
    }
    
    
    public boolean isValid(KaboumGeometryGlobalDescriptor ggd) {
        if (ggd.geometry.getGeometryType().indexOf("Point") != -1) {
            return true;
        }
        return false;
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
        KaboumPoint[] points = new KaboumPoint[1];
        
        if (position == -1) {
            
            try {
                KaboumWKTReader wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
                points[0] = (KaboumPoint) wktReader.read("POINT EMPTY");
                geometry = KaboumGeometryFactory.createMultiPoint(points);
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
     * actionPerformed event e : RemoveSimpleGeometry
     *
     * @param e action to perform
     *
     */
    public void actionPerformedRemoveGeometryWithinCollection(ActionEvent e) {
        
        if (this.parent.agp.activeSimpleGeometry == null) {
            return;
        }
        
        this.parent.agp.activeGGD.geometry.removeGeometry(this.parent.agp.activeSimpleGeometry);
        this.parent.agp.activeGGD.setModified(true);
        
        if (this.parent.agp.activeGGD.geometry.getNumGeometries() == 0) {
            KaboumPoint[] points = new KaboumPoint[1];
            try {
                KaboumWKTReader wktReader = new KaboumWKTReader(this.parent.mapServerTools.pm);
                points[0] = (KaboumPoint) wktReader.read("POINT EMPTY");
                this.parent.agp.activeGGD.geometry = KaboumGeometryFactory.createMultiPoint(points);
            }
            catch (Exception jse) {
                KaboumUtil.debug(" WARNING ! :  error creating geometry ");
                return;
            }
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
        
        KaboumPoint[] points = new KaboumPoint[numGeoms + 1];
        
        for (int i = 0; i < numGeoms; i++) {
            points[i] = (KaboumPoint) this.parent.agp.activeGGD.geometry.getGeometryN(i);
        }
        points[numGeoms] = new KaboumPoint(null);
        this.parent.agp.activeGGD.geometry = KaboumGeometryFactory.createMultiPoint(points);
        this.parent.agp.activeGGD.setEdited(true);
        this.parent.agp.activeSimpleGeometry = this.parent.agp.activeGGD.geometry.getGeometryN(numGeoms);
        
        this.parent.repaint();
        
        return;
        
    }

    public void paint(Graphics g) {}
    
}

