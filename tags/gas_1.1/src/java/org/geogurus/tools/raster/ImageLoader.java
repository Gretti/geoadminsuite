/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geogurus.tools.raster;

/**
 * Title:        ImageLoader
 * Description:  Get the extent (BOX3D) of a parcel from postgresql database.
 *               Crop an input tif image along this extent.
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */

import java.awt.image.BufferedImage;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

public class ImageLoader {
    
    
    /**
     *
     * Read an image from disk and return a BufferedImage
     *
     * @param fileName Chemin vers le fichier image
     *
     */
    public static BufferedImage getBufferedImage(String fileName) {

        // Read the image from disk
        PlanarImage image = (PlanarImage) JAI.create("fileload", fileName);
        
        if (image != null) {
            return image.getAsBufferedImage();
        }
        
        return null;
        
    }
    
}

