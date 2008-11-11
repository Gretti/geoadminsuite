package org.geogurus.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Logger;

import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;

/**
 * Helper methods for code that is shared by the various subclasses of
 * {@link DataAccess}
 * 
 * @author jesse
 */
public final class DataAccessHelper {

    /**
     * Creates a new instance of type DataAccessHelper
     */
    private DataAccessHelper() {
    }

    /**
     * Set the color, symbol and transparency properties on the Class based on
     * the geomType. Also sets the layer type (Point, Raster, etc...)
     */
    public static void setMSLayerColorProperties(Layer msLayer, int geomType,
            RGB color, org.geogurus.mapserver.objects.Class c) {
        msLayer.setStatus(Layer.ON);

        if (geomType == Geometry.POINT || geomType == Geometry.MULTIPOINT) {
            msLayer.setType(Layer.POINT);
            c.setColor(color);
            c.setSymbol(ObjectKeys.DEFAULT_POINT_SYMBOL);
            c.setSize(5);
        } else if (geomType == Geometry.POLYGON
                || geomType == Geometry.MULTIPOLYGON) {
            msLayer.setType(Layer.POLYGON);
            c.setColor(color);
            c.setOutlineColor(new RGB(0, 0, 0));
        } else if (geomType == Geometry.RASTER) {
            msLayer.setType(Layer.RASTER);
        } else {
            // default value for all other types, including linestring
            msLayer.setType(Layer.LINE);
            c.setColor(color);
        }
        if (geomType != Geometry.RASTER) {
            // no classes for rasters
            msLayer.addClass(c);
        }
        // default all layers to be transparent (50%)
        msLayer.setTransparency(DataAccess.MS_LAYER_TRANSPARENCY);
        // the projection part for this layer, if any
        /*
         * if (SRText != null) { // parses the GAS internal proj4 format to
         * extract valid set of parameters for the mapfile // in case of DB
         * source, should provide a mechanism to convert from OpenGIS SRText and
         * Proj4 parameters, // if proj4text column is not present in the
         * spatial_ref_sys table Projection prj = new Projection(); String
         * prjParams = SRText.substring(SRText.indexOf("|") + 1,
         * SRText.lastIndexOf("<")); // example of proj4 params as stored in the
         * spatial_ref_sys postgis table is: // +proj=utm +zone=56 +south
         * +ellps=WGS84 +datum=WGS84 +units=m no_defs prjParams =
         * prjParams.replace('+', ' '); StringTokenizer tk = new
         * StringTokenizer(prjParams); while (tk.hasMoreElements()) {
         * prj.addAttribute("\"" + tk.nextToken() + "\""); }
         * msLayer.setProjection(prj); }
         */
    }

    public static Layer createMapServerLayer(int geomType, File file,
            RGB color) {
        // should construct it
        Layer msLayer = new Layer();
        msLayer.setName(file.getName());
        // Data specific layer properties

        msLayer.setData(file.getAbsolutePath());

        // a default display class for this geoobject
        org.geogurus.mapserver.objects.Class c = new org.geogurus.mapserver.objects.Class();
        // sets the name to the theme name, by default, without extension
        if (file.getName().lastIndexOf(".") > -1) {
            c.setName(file.getName().substring(0,
                    file.getName().lastIndexOf(".")));
        } else {
            c.setName(file.getName());
        }

        DataAccessHelper.setMSLayerColorProperties(msLayer, geomType, color, c);

        return msLayer;
    }

    /**
     * Reads a hypothetic ESRI prj projection file associated with the
     * geometryClass and returns the SRText contained in it, or null if no
     * projection information is found
     * 
     * @return String the projection's SRTEXT (see OGC SFSQL standard for SRTEXT
     *         desc.)
     */
    public static String readProjectionFile(File file, Logger logger) {
        String dsName = file.getAbsolutePath();
        String projFile = dsName.substring(0, dsName.indexOf(".")) + ".prj";
        String projection = null;
        try {
            if (new File(projFile).exists()) {
                BufferedReader in = new BufferedReader(new FileReader(projFile));
                String s = "";
                int i = 0;
                while ((s = in.readLine()) != null) {
                    if (i == 0) {
                        projection = s;
                    } else {
                        projection += "|" + s;
                    }
                    i++;
                }
                in.close();
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return projection;
    }
}
