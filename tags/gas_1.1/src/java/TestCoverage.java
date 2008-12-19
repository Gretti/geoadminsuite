
import java.io.File;
import java.io.IOException;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicolas
 */
public class TestCoverage {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        //File file = new File("/Users/nicolas/projets/CG31/data/raster/fra100k_009_003_l_l_ta_v25.tif");
        File file = new File("/Users/nicolas/projets/CG31/data/raster/tfra100k_009_003_l_l_ta_v25.tif");
        //File file = new File("/Users/nicolas/public_html/ifremer_sextant/extracteur/local/mosaic/fr_004_005.tif");

        GeoTiffReader reader = null;
        try {
            reader = new GeoTiffReader(file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
        } catch (DataSourceException ex) {
            ex.printStackTrace();
            //return;
        }

        GridCoverage2D coverage = null;
        try {
            coverage = (GridCoverage2D) reader.read(null);
        } catch (IOException ex) {
            ex.printStackTrace();
            //return;
        }

        // Using a GridCoverage2D
        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
        System.out.println("crs: " + crs);
        int SRID = CRS.lookupEpsgCode(crs, true).intValue();
        System.out.println("srid: " + SRID);
        Envelope env = coverage.getEnvelope();
        System.out.println("evn: " + env.toString());
    }
}
