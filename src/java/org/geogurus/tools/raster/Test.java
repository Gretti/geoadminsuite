package org.geogurus.tools.raster;

/**
 * Title:        Test
 * Description:  Test the ImageCropFactory and the MaskFactory
 * @author       Jerome Gasperi, aka jrom
 * @version      1.0
 */

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Test extends Component {

  public Test() {
/*
    // Input parameters
    Extent originalExtent = new Extent();
    Extent newExtent = new Extent();
    Connection con = this.getConnection();
    String result = null;
    String path = null;
    String wkt = null;

    // Original extent
    result = this.getSingleQuery(con, "select BOX3D(extent) from raster where id=1");
    if (result != null) {
       //originalExtent = Extent.getExtentFromBOX3D(result);
       originalExtent = new Extent(680000, 2150000, 690000, 2160000);
    }

    // Image path
    result = this.getSingleQuery(con, "select path from raster where id=1");
    if (result != null) {
       path = result;
    }

    // Polygon
    result = this.getSingleQuery(con, "select ASTEXT(the_geom) from theme3 where id=0");
    if (result != null) {
       wkt = result;
    }
    // New extent (is the extent of the selected polygon)
    result = this.getSingleQuery(con, "select BOX3D(the_geom) from theme3 where id=0");
    if (result != null) {
       newExtent = Extent.getExtentFromBOX3D(result);
    }

    // Close the connection
    try {
      con.close();
    } catch (SQLException sqle) { }

    // Read the tiff image
    PlanarImage image = (PlanarImage) JAI.create("fileload", path);

    long time1 = System.currentTimeMillis();
    ImageCropFactory imageCropFactory = new ImageCropFactory(image.getAsBufferedImage(), originalExtent, newExtent);
    long time2 = System.currentTimeMillis();

    System.out.println("IMAGECROPFACTORY :"+ (time2 - time1));

    // DEBUG
    time1 = System.currentTimeMillis();
    imageCropFactory.writeToPNM("/tmp/crop.pnm");
    time2 = System.currentTimeMillis();
    System.out.println("Ecriture crop sur disque : "+(time2 - time1));

    // CREATE the mask from the polygon WKT representation
    time1 = System.currentTimeMillis();
    MaskFactory maskFactory = new MaskFactory(wkt, newExtent, imageCropFactory.getSize());
    time2 = System.currentTimeMillis();
    System.out.println("MASKFACTORY : "+(time2 - time1));

    // DEBUG
    maskFactory.writeToPNM("/tmp/mask.pnm");

    //MaskFactory maskFactory = new MaskFactory(wkt, newExtent, new Dimension(image.getWidth(), image.getHeight()));

    time1 = System.currentTimeMillis();
    BufferedImage bi = this.merge(imageCropFactory, maskFactory);
    time2 = System.currentTimeMillis();
    System.out.println("Merge des deux images : "+(time2 - time1));

    time1 = System.currentTimeMillis();
    this.writeToPNM(bi, "/tmp/resultat.pnm");
    time2 = System.currentTimeMillis();
    System.out.println("Ecriture sur disque : "+(time2 - time1));
*/
  }


  /**
   *
   * Simulate the getConnection() from QUANTIX servlet
   *
   */
  public Connection getConnection() {

    // definir une connexion valide
    java.sql.Connection con = null;

try {
	String dbURL = "jdbc:postgresql://terence:5432/test_postgis";
    	Class driver = Class.forName("org.postgresql.Driver");
	con = DriverManager.getConnection(dbURL,"postgres", "postgres");
    } catch (ClassNotFoundException cnfe) {
      return null;
    } catch (SQLException sqle) {
      return null;
    }

    return con;

  }


  /**
   *
   * Return a result from a single query
   *
   */
  public String getSingleQuery(Connection con, String queryString) {

    String res = null;

    try {
      Statement stmt = con.createStatement();
      StringBuffer query = new StringBuffer(queryString);

      ResultSet rs = stmt.executeQuery(query.toString());

      while (rs.next()) {
        res = rs.getString(1);
      }

      System.out.println(query.toString());
      System.out.println("Resultat: "+res);

      stmt.close();
      //con.close();
    } catch (SQLException sqle) {
        System.out.println("getGeometries: SQL State: " + sqle.getSQLState() + " : " + sqle.getMessage());
        return null;
    }

    return res;

  }

/*
  public BufferedImage merge(ImageCropFactory image, MaskFactory mask) {

    // XOR-ing the two images
    BufferedImage bi = new BufferedImage(image.getSize().width, image.getSize().height, BufferedImage.TYPE_INT_RGB);

    Graphics g = bi.getGraphics();
    g.drawImage(image.getCroppedBufferedImage(), 0, 0, this);
    g.drawImage(mask.getTransparentBufferedImageMask(mask.getBufferedImageMask(), mask.transColor), 0, 0, this);


    return bi;

  }

*/
  /**
   *
   * Write the result into a PNM file
   *
   */
/*  public void writeToPNM(BufferedImage image, String fileName) {

    OutputStream os;
    try {
      os = new FileOutputStream(fileName);
      PNMEncodeParam param = new PNMEncodeParam();
      ImageEncoder enc = ImageCodec.createImageEncoder("PNM", os, param);
      enc.encode(image.getData(), image.getColorModel());
      os.close();
    } catch (FileNotFoundException fnf) {
      fnf.printStackTrace();
      return;
    }
    catch (IOException io) {
      io.printStackTrace();
    }

  }
*/

  public static void main(String[] args) {

    Test test = new Test();
    System.out.println("Fini");
    System.exit(0);
  }

}