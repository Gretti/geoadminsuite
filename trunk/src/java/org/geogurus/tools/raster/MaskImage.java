package org.geogurus.tools.raster;
/**
 * Title:        MaskFactory
 * Description:  Create an image mask from an input polygon.
 * Copyright:    Copyright (c) 2001
 * Company:      SCOT
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.media.jai.codec.*;
public class MaskImage extends BufferedImage {
  /** Transparent color */
  public Color transColor = Color.red;
  /** Dummy frame */
  private Frame frame = new Frame();
  
  private boolean doBorder = true;

  /**
   *
   * Constructor
   *
   * @param polygon Polygon object
   * @param dimension Dimension of the output image
   * @param doBorder Polygon border is drawn
   *
   */
  public MaskImage(Polygon polygon, Dimension size, boolean doBorder) {
      //super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      super(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED);
      this.doBorder = doBorder;
      
      // Create the imageMask
      this.draw(polygon, size);
  }
  
  /**
   *
   * Constructor, done for tallage with multipolygon
   *
   * @param polygon Polygon object
   * @param dimension Dimension of the output image
   * @param doBorder Polygon border is drawn
   *
   */
  public MaskImage(Vector polygons, Dimension size, boolean doBorder) {
      //super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      super(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED);
      this.doBorder = doBorder;
      
      // Create the imageMask
      this.draw(polygons, size);
  }
  
  /**
   *
   * Constructor
   *
   * @param polygon Polygon object
   * @param dimension Dimension of the output image
   *
   */
  public MaskImage(Polygon polygon, Dimension size) {
      //super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      super(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED);
      // Create the imageMask
      this.draw(polygon, size);
  }
  /**
   *
   * Constructor, done for tallage with multipolygon
   *
   * @param polygon Polygon object
   * @param dimension Dimension of the output image
   *
   */
  public MaskImage(Vector polygons, Dimension size) {
      //super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
      super(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED);
      // Create the imageMask
      this.draw(polygons, size);
  }
  /**
   *
   * Return the transparent version
   * of the Mask image
   *
   */
  public Image getTransparentMask(Image imageMask, final Color transColor) {
    ImageFilter filter = new RGBImageFilter() {
       public int markerRGB = transColor.getRGB() | 0xFF000000;
       public final int filterRGB(int x, int y, int rgb) {
         if ( ( rgb | 0xFF000000 ) == markerRGB ) {
	    return 0x00FFFFFF & rgb;
         }
	 else {
	    return rgb;
	 }
       }
    };
    ImageProducer ip = new FilteredImageSource(imageMask.getSource(), filter);
    this.frame.addNotify();
    
    return this.frame.createImage(ip);
  }
  
  /**
   *
   * Draw the result
   *
   */
  private void draw(Polygon polygon, Dimension size) {
    // Get the graphical context
    Graphics g = this.getGraphics();
    // Clear the buffer
    g.clearRect(0, 0, size.width, size.height);
    g.setColor(this.transColor);
    
    g.fillPolygon(polygon);
    
    if (doBorder) {
        g.drawPolygon(polygon);
    }
  }
  /**
   *
   * Draw the result from a list of polygon
   *
   */
  private void draw(Vector polygons, Dimension size) {
      for (Iterator iter = polygons.iterator(); iter.hasNext();) {
          draw((Polygon)iter.next(), size);
      }
  }

  /**
   *
   * Merge this mask with an input image
   *
   */
  public BufferedImage merge(Image image) {
  
      Frame tmpFrame = new Frame();
      tmpFrame.addNotify();
      BufferedImage bi = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics g = bi.getGraphics();
      g.drawImage(image, 0, 0, tmpFrame);
      g.drawImage(this.getTransparentMask(this, this.transColor), 0, 0, tmpFrame);
      
      return bi;
  }
  
  
  /**
   *
   * Write the result into a PNM file
   *
   */
  public void writeToPNM(String fileName) {
    OutputStream os;
    try {
      os = new FileOutputStream(fileName);
      PNMEncodeParam param = new PNMEncodeParam();
      ImageEncoder enc = ImageCodec.createImageEncoder("PNM", os, param);
      enc.encode(this);
      os.close();
    } catch (FileNotFoundException fnf) {
      fnf.printStackTrace();
      return;
    }
    catch (IOException io) {
      io.printStackTrace();
    }
  }

}
