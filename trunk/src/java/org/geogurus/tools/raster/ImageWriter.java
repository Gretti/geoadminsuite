package org.geogurus.tools.raster;
/**
 * Title:        ImageWriter
 * Description:  Write image to disk
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.media.jai.JAI;
import javax.media.jai.RenderedImageAdapter;

import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codec.PNGEncodeParam;
import com.sun.media.jai.codec.PNMEncodeParam;
import com.sun.media.jai.codec.TIFFEncodeParam;
public class ImageWriter {
    
    
    /**
     *
     * Write an image to disk
     *
     * @param image Image to write
     * @param fileName File name created
     *
     */
    public static void writeTIFF(RenderedImage image, String fileName) throws IOException
    /* Written by amar on january 2003 */
    /**
     * Passe via ecriture dans un fichier PNG
     */
    {
        // mise au format JAI
        RenderedImageAdapter src = new RenderedImageAdapter(image);
        
        // Chargement de l'image dans d'un fichier temporaire de meme nom que fichier souhaite
        StringTokenizer strTemp = new StringTokenizer(fileName);
        String fileTemp = strTemp.nextToken(".");
        fileTemp = fileTemp.concat(".png");
        File imageFile = new File(fileTemp);
        
        JAI.create("FILESTORE", image, fileTemp, "PNG", null);
        
        // chargement du contenu du fichier dans un RenderedImage
        RenderedImage ri = (RenderedImage)JAI.create("fileload", fileTemp);
        TIFFEncodeParam param = new TIFFEncodeParam();
        
        RenderedImage ri2 = (RenderedImage)JAI.create("FILESTORE", ri, fileName, "TIFF", param);
        
        imageFile.delete();
    }
    /* end amar's part*/
    
/*    public static void writeTiff(RenderedImage image, String fileName)
        throws IOException {
 
        // write it to file:
        //TIFFEncodeParam tiffParam = new TIFFEncodeParam();
        JAI.create("FILESTORE", image, fileName, "TIFF", null);
 
    }
 */
    /**
     *
     * Write an image to disk
     *
     * @param image Image to write
     * @param fileName File name created
     *
     */
    public static void writeJPEG(RenderedImage image, String fileName) throws IOException
    /* Written by amar on january 2003 */
    /**
     * Passe via ecriture dans un fichier PNG
     */
    {
        // mise au format JAI
        RenderedImageAdapter src = new RenderedImageAdapter(image);
        
        // Chargement de l'image dans d'un fichier temporaire de meme nom que fichier souhaite
        StringTokenizer strTemp = new StringTokenizer(fileName);
        String fileTemp = strTemp.nextToken(".");
        fileTemp = fileTemp.concat(".png");
        
        JAI.create("FILESTORE", image, fileTemp, "PNG", null);
        
        // chargement du contenu du fichier dans un RenderedImage
        File imageFile = new File(fileTemp);
        RenderedImage ri = (RenderedImage)JAI.create("fileload", fileTemp);
        
        // mise a jour des parametres de l'image JPEG
        JPEGEncodeParam param = new JPEGEncodeParam();
        param.setHorizontalSubsampling(0, 1);
        param.setHorizontalSubsampling(1, 1);
        param.setHorizontalSubsampling(2, 1);
        param.setVerticalSubsampling(0, 1);
        param.setVerticalSubsampling(1, 1);
        param.setVerticalSubsampling(2, 1);
        param.setQuality(1);
        // chargement du RenderedImage dans un fichier JPEG
        RenderedImage ri2 = (RenderedImage)JAI.create("FILESTORE", ri, fileName, "JPEG", param);
        imageFile.delete();
    }
    /* end amar's part*/
    
/*    public static void writeJpeg(RenderedImage image, String fileName)
        throws IOException {
 
        OutputStream os;
        os = new FileOutputStream(fileName);
        JPEGEncodeParam param = new JPEGEncodeParam();
        param.setQuality(1);
        param.setVerticalSubsampling(0, 1);
        param.setVerticalSubsampling(1, 1);
        param.setVerticalSubsampling(2, 1);
        param.setHorizontalSubsampling(0, 1);
        param.setHorizontalSubsampling(1, 1);
        param.setHorizontalSubsampling(2, 1);
        ImageEncoder enc = ImageCodec.createImageEncoder("JPEG", os, param);
        enc.encode(image);
        os.close();
 
        //JAI.create("FILESTORE", image, fileName, "JPEG", null);
 
    }
 */
    /**
     *
     * Write a tfw descriptor to disk
     *
     * @param fileName Output file name (full path)
     * @param ulx Upper left x coordinate
     * @param uly Uper left y coordinate
     * @param dx Size of an x pixel
     * @param dy Size of an y pixel
     *
     */
    public static void writeTfw(String fileName, double ulx, double uly, double dx, double dy)
    throws IOException {
        
        BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(fileName));
        StringBuffer sb = new StringBuffer(dx + System.getProperty("line.separator"));
        sb.append("0" + System.getProperty("line.separator"));
        sb.append("0" + System.getProperty("line.separator"));
        sb.append("-" + dy + System.getProperty("line.separator"));
        sb.append(ulx + System.getProperty("line.separator"));
        sb.append(uly + System.getProperty("line.separator"));
        bw.write(sb.toString());
        bw.close();
        
        return;
    }
    
    
    /**
     *
     * Write an image to disk
     *
     * @param image Image to write
     * @param fileName File name created
     *
     */
    public static void writePNM(RenderedImage image, String fileName)
    /* Written by amar on january 2003 */
    {
        // mise au format JAI
        RenderedImageAdapter src = new RenderedImageAdapter(image);
        
        // mise a jour des parametres de l'image PNG
        PNMEncodeParam param = new PNMEncodeParam();
        
        JAI.create("FILESTORE", image, fileName, "PNM", param);
    }
    /* end amar's part*/
    
    /**
     *
     * Write an image to disk
     *
     * @param image Image to write
     * @param fileName File name created
     *
     */
    public static void writePNG(RenderedImage image, String fileName)
    /* Written by amar on january 2003 */
    {
        // mise au format JAI
        RenderedImageAdapter src = new RenderedImageAdapter(image);
        
        // mise a jour des parametres de l'image PNG
        PNGEncodeParam.RGB param = new PNGEncodeParam.RGB();
        
        JAI.create("FILESTORE", src, fileName, "PNG", param);
    }
    /* end amar's part*/
    
 /*   public static void writePNM(RenderedImage image, String fileName)
        throws IOException {
  
        OutputStream os;
        os = new FileOutputStream(fileName);
        PNMEncodeParam param = new PNMEncodeParam();
        ImageEncoder enc = ImageCodec.createImageEncoder("PNM", os, param);
        enc.encode(image);
        os.close();
  
    }
  **/
}
