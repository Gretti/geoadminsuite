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
  