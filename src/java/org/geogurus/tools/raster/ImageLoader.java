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

