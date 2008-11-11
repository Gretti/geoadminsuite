package org.geogurus.tools.raster;

/**
 * Title:        ImageConverter
 * Description:  Convert Java Image type to RenderedImage description
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */

import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageConverter  {
   
  public static BufferedImage getBufferedImage(Image image) {

      BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
      bi.getGraphics().drawImage(image, 0, 0, null);
      
      return bi;
  }
  
  public static BufferedImage getIndexedBufferedImage(Image image) {

      BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_BYTE_INDEXED);
      bi.getGraphics().drawImage(image, 0, 0, null);
      
      return bi;
  }
}
  