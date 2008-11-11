/*
 * CropImage.java
 *
 * Created on 19 mars 2002, 15:56
 */

package org.geogurus.tools.raster;


/**
 * Title:        ImageCropFactory
 * Description:  Get the extent (BOX3D) of a parcel from postgresql database.
 *               Crop an input tif image along this extent.
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNMEncodeParam;
import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFField;

/**
 * Cette classe correspond � la cha�ne de traitement n�cessaire � l'extraction d'une imagette.
 * au sein d'une image plus grande.
 */

public class CroppedImage extends BufferedImage {
    
    /** Graphical context */
    private Frame frame = new Frame();
    double[][][] imageInitiale;
    
    
    /**
     *
     * Constructor
     *
     * @param imageToCrop BufferedImage to crop
     * @param origin Origin of the crop
     * @param size Dimension of the new image
     *
     */
    public CroppedImage(BufferedImage imageToCrop, Point origin, Dimension size) {
        super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        ImageFilter filter = new CropImageFilter(origin.x, origin.y, size.width, size.height);
        ImageProducer ip = new FilteredImageSource(imageToCrop.getSource(), filter);
        this.frame.addNotify();
        Image tmpImage = this.frame.createImage(ip);
        
        this.getGraphics().drawImage(tmpImage, 0, 0, frame);
    }
    
    
    
    /**
     *
     * Cha�ne de traitement n�cessaire � l'extraction d'une imagette.
     *
     * @param tiffPath chemin de l'image� traiter
     * @param origin origine de la r�gion � cropper
     * @param size taille de la nouvelle image
     *
     */
    public CroppedImage(String tiffPath, Point origin, Dimension size) {
        super(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        int [] dim={0,0};
        int retour;
        long adress, pos, nbBytes;
        long n[];
        Graphics g;
        
        try {
            
            /* acces fichier */
            FileSeekableStream ss = new FileSeekableStream(tiffPath);
            
            /* recuperation de la taille de l'image de depart*/
            dim = getTiffDimension(ss);
            
            /* recuperation du nombre d'octets par bande de cette image */
            n = getByteByBande(ss);
            
            if ( n != null )
            {
                /* recuperation du nombre d'octet sur lequel est code un pixel */
                nbBytes = nbByte(dim[0], dim[1], n);

                /* recuperation du pointeur des donn�es images */
                adress = getImageData(ss);

                /* recuperation des donnees photometriques */
                int photo=getPhotogrammetric( ss);

                /* calcul du positionnement en (X,Y) */
                if ( origin.x != 0)
                    pos=adress+(dim[0]*origin.y + origin.x)*nbBytes;
                else pos = adress+ origin.y*nbBytes;

                /* recuperatoin de la boite */
                readColor(ss, tiffPath,  photo, pos, dim, size);
            }
            
        }
        catch (IOException ioe) {
            System.out.println("Erreur de positionnemnt!");
        }
        
    }
    
    
    /**
     * Returns a int array with 2 values: the width and height of the given tiff
     * file
     */
    protected static int[] getTiffDimension(FileSeekableStream ss) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            int[] res = {0,0};
            
            TIFFField tf = td.getField(256); 
            switch (tf.getType()) 
            {
                case TIFFField.TIFF_SLONG:
                case TIFFField.TIFF_BYTE:
                case TIFFField.TIFF_SBYTE:
                case TIFFField.TIFF_UNDEFINED:
                case TIFFField.TIFF_SHORT:
                case TIFFField.TIFF_SSHORT:
                    res[0] = tf.getAsInt(0);
                    break;
                case TIFFField.TIFF_LONG:
                    res[0] = (int)tf.getAsLong(0);
                    break;
            }

            tf = td.getField(257); 
            switch (tf.getType()) 
            {
                case TIFFField.TIFF_SLONG:
                case TIFFField.TIFF_BYTE:
                case TIFFField.TIFF_SBYTE:
                case TIFFField.TIFF_UNDEFINED:
                case TIFFField.TIFF_SHORT:
                case TIFFField.TIFF_SSHORT:
                    res[1] = tf.getAsInt(0);
                    break;
                case TIFFField.TIFF_LONG:
                    res[1] = (int)tf.getAsLong(0);
                    break;
            }
           
            return res;
        } catch (IOException ioe) {
            return null;
        }
    }
    
    /**
     * R�cup�re le param�tre contenant le nombre d'octet par bande.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     *
     * @return le nombre d'octets par bande.
     */
    protected static long[] getByteByBande(FileSeekableStream ss) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            TIFFField tf = td.getField(279);
            
            long res[] = new long[tf.getCount()];
            if ( tf.getType() == TIFFField.TIFF_LONG ) 
            {
                res=tf.getAsLongs();
            }
            else res = null;
            
            return res;            
        }
        catch (IOException ioe) {
            return null;
        }
    }
    
    
    /**
     * R�cup�re le param�tre contenant le mode d'interpr�tation photom�trique.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     *
     * @return le mode d'interpr�tation photom�trique.
     */
    protected static int getPhotogrammetric(FileSeekableStream ss) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            TIFFField tf = td.getField(262);
            
            int res = 0;
            switch (tf.getType()) 
            {
                case TIFFField.TIFF_SLONG:
                case TIFFField.TIFF_BYTE:
                case TIFFField.TIFF_SBYTE:
                case TIFFField.TIFF_UNDEFINED:
                case TIFFField.TIFF_SHORT:
                case TIFFField.TIFF_SSHORT:
                    res=tf.getAsInt(0);
                    break;
                case TIFFField.TIFF_LONG:
                    res = (int)tf.getAsLong(0);
                    break;
            }
            
            return res;
        }
        catch (IOException ioe) {
            return 0;
        }
    }
    
    /**
     * Calcule le nombre d'octets sur lequel est cod� un pixel.
     *
     * @param width largeur de l'image.
     * @param height hauteur de l'image.
     * @param nbByte ombre d'octets par bande de l'image
     *
     * @return le nombre d'octets sur lequel est cod� un pixel.
     */
    protected static long nbByte(int width, int height, long[] nbByte) {
        long nb = 0;
        long sum = 0;
        
        for ( int i = 0; i<nbByte.length; i++)
            sum=sum+nbByte[i];
        
        nb=sum/(width*height);
        return(nb);
        
    }
    
    /**
     * R�cup�re le param�tre contenant le type de configuration des plans.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     *
     * @return le type de configuration des plans.
     */
    protected static int getConfigType(FileSeekableStream ss) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            TIFFField tf = td.getField(284);
            
            int res = 0;
            switch (tf.getType()) 
            {
                case TIFFField.TIFF_SLONG:
                case TIFFField.TIFF_BYTE:
                case TIFFField.TIFF_SBYTE:
                case TIFFField.TIFF_UNDEFINED:
                case TIFFField.TIFF_SHORT:
                case TIFFField.TIFF_SSHORT:
                    res=tf.getAsInt(0);
                    break;
                case TIFFField.TIFF_LONG:
                    res = (int)tf.getAsLong(0);
                    break;
            }
            
            return res;
        }
        catch (IOException ioe) {
            return 0;
        }
    }
    
    /**
     * R�cup�re le pointeur vers les donn�es de l'image.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     *
     * @return le pointeur vers les donn�es de l'image.
     */
    protected long getImageData(FileSeekableStream ss) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            
            long res = 0;
            TIFFField tf = td.getField(273);
            res = tf.getAsLong(0);
            
            return res;
        }
        catch (IOException ioe) {
            return 0;
        }
    }
    
    /**
     * R�cup�re le nombre de grains de l'image par pixel.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     *
     * @return le nombre de grains de l'image par pixel.
     */
    protected int getGrainNumber( FileSeekableStream ss ) {
        try {
            TIFFDirectory td = new TIFFDirectory(ss, 0);
            int res = 0;
            
            TIFFField tf = td.getField(277);
            switch (tf.getType()) 
            {
                case TIFFField.TIFF_SLONG:
                case TIFFField.TIFF_BYTE:
                case TIFFField.TIFF_SBYTE:
                case TIFFField.TIFF_UNDEFINED:
                case TIFFField.TIFF_SHORT:
                case TIFFField.TIFF_SSHORT:
                    res=tf.getAsInt(0);
                    break;
                case TIFFField.TIFF_LONG:
                    res = (int)tf.getAsLong(0);
                    break;
            }
            
            return res;
        }
        catch (IOException ioe) {
            return 0;
        }
    }
    
    /**
     * Convertit un tableau de byte en int, suivant le type de l'image.
     *
     * @param tabByte tableau � convertir
     * @param type le type de l'image ( index�e, RVB, N&B ).
     *
     * @return le tableau d'int correspondant.
     */
    protected static int[] convert( byte[] tabByte, int type) {
        int tabInt[]=new int[type];
        
        for ( int i=0; i<type; i++) 
        {
            tabInt[i]=(int)tabByte[i];
            /* transformation en positif */
            if ( tabInt[i] < 0 )
                tabInt[i]=256+tabInt[i];
        }        
        return( tabInt );
    }
    
    
    /**
     * Cr�e l'imagette s�lectionn�e.
     *
     * @param ss le fichier dans lequel aller chercher les informations, au format FileSeekableStream.
     * @param chemin chemin du fichier image.
     * @param type type de l'image.
     * @param pos position du"curseur" dans le fichier.
     * @param dim dimension de l'image de d�part.
     * @param size dimension de l'imagette.
     *
     */
    protected  void readColor( FileSeekableStream ss, String chemin, int type, long pos, int [] dim, Dimension size) {
        try {
            byte[] tabByte ; // recupere le(s) octet(s) lu ds le fichier
            int[] tabInt ; // recuperera le(s) octet(s) non signe(s)
            Color clr;
            TIFFDirectory td;
            TIFFField tf;
    
            switch ( type ) 
            {
                case 3: // '\003' : palette, image indexee
                  //  System.out.println("Image indexee !!!");
                    td = new TIFFDirectory(ss, 0);
                    
                    //Test existence parametre definition de la palette
                    if((tf = td.getField(320)) == null)
                        System.out.println("Null field for param 320.");
                    
                    // recupere l'octet correspondant � l'index ( point d'entree dans la palette )
                    tabByte = new byte[1];
                    for(int j = 0; j < size.height; j++) 
                    {
                        // positionnement en tete de la i�me ligne de l'imagette 
                        for(int k = 0; k < size.width; k++) 
                        {
                            // positionnement ds partie image a extraire
                            ss.seek(pos + (long)k);
                            ss.read(tabByte, 0, 1);
                            // convertion entre 0 et 255
                            tabInt = convert(tabByte, 1);
                            // creation de l'image resultat en niveau de gris
                            //meme valeur sur les 3 canaux : la valeur de l'index pour chacun des pixels
                            clr = new Color(tabInt[0], tabInt[0], tabInt[0]);
                            this.setRGB(k, j, clr.getRGB());
                        }                        
                        pos += dim[0];
                    }                    
                    break;
                case 2: // image type RGB 
                    // Une image TIFF de type RVB est construite de la facon suivante : RVB RVB ... RVB
                    // Un pixel = (RVB) = 3 octets
                  //  System.out.println("Image RGB !!!");
                    
                    // tableau contenant les octets lus
                    tabByte = new byte[3]; 
                  
                    for(int j = 0; j < size.height; j++) 
                    {// positionnement en tete de la i�me ligne de l'imagette 
                        for(int k = 0; k < size.width; k++) 
                        {
                            // on se positionne sur le pixel a lire
                            ss.seek(pos + (long)k*3);
                            // on lit 3 octets : (RVB)
                            ss.read(tabByte, 0, 3);
                            // convertion entre 0 et 255
                            tabInt = convert(tabByte, 3);
                            //creation de l'image resultat au meme couleur que l'image initiale
                            clr = new Color(tabInt[0], tabInt[1], tabInt[2]);
                            setRGB(k, j, clr.getRGB());
                        }             
                        // on avance de 3 fois le nombre de pixels de la largeur de l'image 
                        // car un pixel = 3 octets
                        pos += dim[0]*3;
                    }  
                    
                    break;
                    
               case 0 : /* GrayScale  valeur maximale = noir et minimale = blanc*/
                 // System.out.println("GrayScale Type = 0 ");
                   
                    // recupere le(s) octet(s) lu ds le fichier
                    tabByte = new byte[1]; 
                    
                    for ( int j=0; j<size.height; j++) 
                    {
                        // positionnement en tete de la i�me ligne de l'imagette 
                        for ( int k=0; k<size.width; k++) 
                        {
                            // placement sur le pixel a lire 
                            ss.seek(pos+k);                            
                            // lecture du pixel = 1 octet car en ndg
                            ss.read(tabByte, 0, 1);
                            // convertion entre 0 et 255
                            tabInt=convert(tabByte, 1);
                            // niveau de gris donc meme valeur pour les 3 canaux.
                            clr= new Color(tabInt[0], tabInt[0], tabInt[0]);
                            this.setRGB(k, j, clr.getRGB());
                            // => setRGB(col ou width, lig ou  height, rgb )
                            
                        }
                        // calcul de la position de la ligne suivante 
                        pos=pos+dim[0];
                    }
                    break;
                case 1 : /* GrayScale valeur minimale = noir et maximale = blanc*/
                 //   System.out.println("GrayScale type = 1");
                    // recupere le(s) octet(s) lu ds le fichier
                    tabByte = new byte[1]; 
                    
                    for ( int j=0; j<size.height; j++) 
                    {
                        // positionnement en tete de la i�me ligne de l'imagette 
                        for ( int k=0; k<size.width; k++) 
                        {
                            // replacement sur le pixel a lire 
                            ss.seek(pos+k);                            
                            // lecture des octets 
                            ss.read(tabByte, 0, 1);
                            // convertion entre 0 et 255
                            tabInt=convert(tabByte, 1);
                            // niveau de gris donc meme valeur pour les 3 canaux.
                            clr= new Color(tabInt[0], tabInt[0], tabInt[0]);
                            this.setRGB(k, j, clr.getRGB());
                            // => setRGB(col ou width, lig ou  height, rgb )                            
                        }
                        // calcul de la position de la ligne suivante 
                        pos=pos+dim[0];
                    }
                    break;
                    
                default : System.out.println("Indexed image required!");break;
            }
            
        }
        catch (IOException ioe) {
            System.out.println("erreur ds readColor");
        }
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
            enc.encode(this.getData(), this.getColorModel());
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

