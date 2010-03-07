/*
 * KaboumVectorServerTools.java
 *
 * Created on 6 aout 2005, 19:19
 */
package org.kaboum.util;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.kaboum.Kaboum;
import org.kaboum.geom.KaboumGeometry;
import org.kaboum.geom.KaboumGeometryCollection;
import org.kaboum.geom.KaboumGeometryDisplayDescriptor;
import org.kaboum.geom.KaboumGeometryGlobalDescriptor;
import org.kaboum.geom.KaboumGeometryPropertiesDescriptor;
import org.kaboum.geom.KaboumPoint;

/**
 * This class is similar to KaboumMapServerTools, except it deals with the kaboum feature objets server engine.<p>
 * Stores URL and parameters for this server and deals with geometry handling<p>
 * Deals with all geometries parameters (global, display, properties, tooltips, etc.).
 * This class should be the only entry point to get/set geometric parameters<p>
 * This classe keeps a reference on the kaboum Applet to be able to call its methods and variables.
 * (This is needed now that this class deals with geographic objects instead of Kaboum applet).
 *
 * @author Nicolas
 */
public class KaboumFeatureServerTools {

        public static final short LIVE = 0;
        public static final short KEEP = 1;
        public static final short KEEP_CACHE = 2;
        /**
         * The stored geometry allowing to cancel the spatial analysis operation
         * that modified existing geometry is modified
         */
        KaboumGeometry storedGeometry;
        /**
         * The stored geometry class name allowing to cancel the spatial analysis operation
         * that modified existing geometry is modified
         */
        String storedClassName;
        /** indicates if features retrieval is on (true) or off.
         * Users can control this by sending the following kaboumCommand:
         * OBJECT|ON to allow feature retrieval<br>
         * OBJECT|OFF to block it, for instance when the map extent is too big
         */
        private boolean standby;
        /** this extent is used to test if it is necessary to reload features from server:
         * if this extent contains current map extent, it is not necessary to load objects
         * except if all objects where not retrieved because of a server count limit.
         * If current map extent contains this extent, it will be the new biggest extent
         */
        private KaboumExtent biggestExtent = null;
        /** The reference on Kaboum Applet */
        private Kaboum parent = null;
        /** The URL (absolute or relative) to the Kaboum Server Feature Engine */
        private String featureServerURL = null;
        /** The initial Map Extent */
        private KaboumExtent mapExtent = null;
        /** the geometry restore mode, allowing to cancel a spatial analysis operation */
        private boolean isRestoreMode = false;
        /** the client live mode */
        public short liveMode = KaboumFeatureServerTools.LIVE;
        private String propertiesFile;

        /** Creates a new instance of KaboumFeatureServerTools with an URL to the server engine and a file path
         * to the feature objects properties
         */
        public KaboumFeatureServerTools(Kaboum applet, String featureServerURL, KaboumExtent mapExt) {
                parent = applet;
                this.featureServerURL = featureServerURL;
                this.mapExtent = mapExt;
                standby = false;

                // initialize features biggest extent to be slightly smaller than mapExt,
                // to force features loading the first time getFeatures is called
                // build an extent 1% smaller than mapExt
                biggestExtent = new KaboumExtent(
                        mapExt.xMin + (mapExt.xMax - mapExt.xMin) * 0.01,
                        mapExt.yMin + (mapExt.yMax - mapExt.yMin) * 0.01,
                        mapExt.xMax - (mapExt.xMax - mapExt.xMin) * 0.01,
                        mapExt.yMax - (mapExt.yMax - mapExt.yMin) * 0.01);
        }

        /**
         * Parses list of parameters contained in the shuttle and refreshes kaboum <p>
         * The format for global parameters is:<br>
         *
         */
        private void loadServerParameters(KaboumFeatureShuttle shuttle) {
                if (shuttle == null) {
                        KaboumUtil.debug("KaboumFeatureServerTools.loadServerParameters: null input shuttle");
                        return;
                }
                if (shuttle.parameters == null || shuttle.parameters.size() == 0) {
                        KaboumUtil.debug("KaboumFeatureServerTools.loadServerParameters: null or empty shuttle's parameters Hashtable");
                        return;
                }
                String param = null;
                StringTokenizer st = null;

                // default values for kaboum parameters are loaded in the properties file by the server and
                // eventually overloaded by the user's properties file

                parent.opModePropertiesHash.put("KABOUM_USE_TOOLTIP", shuttle.parameters.getProperty("KABOUM_USE_TOOLTIP", Kaboum.FALSE));
                parent.opModePropertiesHash.put("TOOLTIP_DISPLAY_PERIMETER", shuttle.parameters.getProperty("TOOLTIP_DISPLAY_PERIMETER", Kaboum.FALSE));
                parent.opModePropertiesHash.put("TOOLTIP_DISPLAY_AREA", shuttle.parameters.getProperty("TOOLTIP_DISPLAY_AREA", Kaboum.FALSE));
                parent.opModePropertiesHash.put("TOOLTIP_BOX_BORDER_SIZE", shuttle.parameters.getProperty("TOOLTIP_BOX_BORDER_SIZE", "1"));
                parent.opModePropertiesHash.put("TOOLTIP_BOX_BORDER_COLOR", shuttle.parameters.getProperty("TOOLTIP_BOX_BORDER_COLOR", "black"));
                parent.opModePropertiesHash.put("TOOLTIP_BOX_COLOR", shuttle.parameters.getProperty("TOOLTIP_BOX_COLOR", "white"));
                parent.opModePropertiesHash.put("TOOLTIP_TEXT_COLOR", shuttle.parameters.getProperty("TOOLTIP_TEXT_COLOR", "black"));
                parent.opModePropertiesHash.put("TOOLTIP_HORIZONTAL_MARGIN", shuttle.parameters.getProperty("TOOLTIP_HORIZONTAL_MARGIN", "5"));
                parent.opModePropertiesHash.put("TOOLTIP_VERTICAL_MARGIN", shuttle.parameters.getProperty("TOOLTIP_VERTICAL_MARGIN", "5"));
                parent.opModePropertiesHash.put("TOOLTIP_OFFSET", shuttle.parameters.getProperty("TOOLTIP_OFFSET", "5"));

                parent.opModePropertiesHash.put("DISTANCE_FOREGROUND_COLOR", shuttle.parameters.getProperty("DISTANCE_FOREGROUND_COLOR", "blue"));
                parent.opModePropertiesHash.put("DISTANCE_POINT_TYPE", shuttle.parameters.getProperty("DISTANCE_POINT_TYPE", "K_TYPE_CIRCLE"));
                parent.opModePropertiesHash.put("DISTANCE_POINT_HEIGHT", shuttle.parameters.getProperty("DISTANCE_POINT_HEIGHT", "5"));
                parent.opModePropertiesHash.put("DISTANCE_POINT_WIDTH", shuttle.parameters.getProperty("DISTANCE_POINT_WIDTH", "5"));

                parent.opModePropertiesHash.put("SURFACE_FOREGROUND_COLOR", shuttle.parameters.getProperty("SURFACE_FOREGROUND_COLOR", "blue"));
                parent.opModePropertiesHash.put("SURFACE_POINT_TYPE", shuttle.parameters.getProperty("SURFACE_POINT_TYPE", "K_TYPE_CIRCLE"));
                parent.opModePropertiesHash.put("SURFACE_POINT_HEIGHT", shuttle.parameters.getProperty("SURFACE_POINT_HEIGHT", "5"));
                parent.opModePropertiesHash.put("SURFACE_POINT_WIDTH", shuttle.parameters.getProperty("SURFACE_POINT_WIDTH", "5"));
                // Font parameters
                parent.opModePropertiesHash.put("KABOUM_FONT_NAME", shuttle.parameters.getProperty("KABOUM_FONT_NAME", "Courier"));
                parent.opModePropertiesHash.put("KABOUM_FONT_STYLE", shuttle.parameters.getProperty("KABOUM_FONT_STYLE", "plain"));
                parent.opModePropertiesHash.put("KABOUM_FONT_SIZE", shuttle.parameters.getProperty("KABOUM_FONT_SIZE", "12"));

                // Properties of geometrical objects
                parent.opModePropertiesHash.put("GEOMETRY_ROUGHNESS", shuttle.parameters.getProperty("GEOMETRY_ROUGHNESS", "1"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_TO_CANCEL_GEOMETRY", shuttle.parameters.getProperty("GEOMETRY_ALLOW_TO_CANCEL_GEOMETRY", "FALSE"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_TO_DRAG_POINT", shuttle.parameters.getProperty("GEOMETRY_ALLOW_TO_DRAG_POINT", "FALSE"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_TO_SUPRESS_GEOMETRY_WITHIN_COLLECTION", shuttle.parameters.getProperty("GEOMETRY_ALLOW_TO_SUPRESS_GEOMETRY_WITHIN_COLLECTION", "TRUE"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_TO_SUPRESS_OBJECT", shuttle.parameters.getProperty("GEOMETRY_ALLOW_TO_SUPRESS_OBJECT", "FALSE"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_GEOMETRY_COLLECTION", shuttle.parameters.getProperty("GEOMETRY_ALLOW_GEOMETRY_COLLECTION", "TRUE"));
                parent.opModePropertiesHash.put("GEOMETRY_ALLOW_HOLE_IN_GEOMETRY", shuttle.parameters.getProperty("GEOMETRY_ALLOW_HOLE_IN_GEOMETRY", "TRUE"));
                parent.opModePropertiesHash.put("GEOMETRY_COMPUTATION_PRECISION", shuttle.parameters.getProperty("GEOMETRY_COMPUTATION_PRECISION", "1"));
                parent.opModePropertiesHash.put("GEOMETRY_PIXEL_PRECISION", shuttle.parameters.getProperty("GEOMETRY_PIXEL_PRECISION", "1"));
                parent.opModePropertiesHash.put("GEOMETRY_DOUBLE_CLICK_TIME", shuttle.parameters.getProperty("GEOMETRY_DOUBLE_CLICK_TIME", "500"));
                parent.opModePropertiesHash.put("GEOMETRY_ENABLE_VERTEX_SNAPPING", shuttle.parameters.getProperty("GEOMETRY_ENABLE_VERTEX_SNAPPING", "FALSE"));
                parent.opModePropertiesHash.put("GEOMETRY_ENABLE_EDGE_SNAPPING", shuttle.parameters.getProperty("GEOMETRY_ENABLE_EDGE_SNAPPING", "FALSE"));
                parent.opModePropertiesHash.put("GEOMETRY_SNAPPING_TOLERANCE", shuttle.parameters.getProperty("GEOMETRY_SNAPPING_TOLERANCE", "5"));

                // default values are provided by the drawer itself.
                //TODO: correct all keys whose default values are provided by their drawers
                parent.opModePropertiesHash.put("GEOMETRY_FUSION_ARROW_COLOR", shuttle.parameters.getProperty("GEOMETRY_FUSION_ARROW_COLOR", "red"));
                parent.opModePropertiesHash.put("GEOMETRY_FUSION_ARROW_LENGTH", shuttle.parameters.getProperty("GEOMETRY_FUSION_ARROW_LENGTH", "10"));

                // server properties
                parent.opModePropertiesHash.put("SERVER_VALIDATE_TO_DATASTORE", shuttle.parameters.getProperty("SERVER_VALIDATE_TO_DATASTORE", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DELETE_FROM_DATASTORE", shuttle.parameters.getProperty("SERVER_DELETE_FROM_DATASTORE", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_UNION", shuttle.parameters.getProperty("SERVER_DO_UNION", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_INTERSECTION", shuttle.parameters.getProperty("SERVER_DO_INTERSECTION", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_DIFFERENCE", shuttle.parameters.getProperty("SERVER_DO_DIFFERENCE", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_SYM_DIFFERENCE", shuttle.parameters.getProperty("SERVER_DO_SYM_DIFFERENCE", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_FUSION", shuttle.parameters.getProperty("SERVER_DO_FUSION", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_ERASING", shuttle.parameters.getProperty("SERVER_DO_POLYGON_ERASING", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_FITTING", shuttle.parameters.getProperty("SERVER_DO_POLYGON_FITTING", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_HOLES_COMPLETION", shuttle.parameters.getProperty("SERVER_DO_HOLES_COMPLETION", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_SPLITTING", shuttle.parameters.getProperty("SERVER_DO_POLYGON_SPLITTING", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_SPLITTING_BY_LINE", shuttle.parameters.getProperty("SERVER_DO_POLYGON_SPLITTING_BY_LINE", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_COMPLETION", shuttle.parameters.getProperty("SERVER_DO_POLYGON_COMPLETION", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_DO_POLYGON_COMPLETION_FITTING", shuttle.parameters.getProperty("SERVER_DO_POLYGON_COMPLETION_FITTING", "TRUE"));
                parent.opModePropertiesHash.put("SERVER_LIVE_MODE", shuttle.parameters.getProperty("SERVER_LIVE_MODE", "0"));
                parent.opModePropertiesHash.put("CLIENT_MANAGE_SPLIT", shuttle.parameters.getProperty("CLIENT_MANAGE_SPLIT", "TRUE"));

                // looks which LIVE mode is configured.
                String mode = parent.getOpModeProperty("SERVER_LIVE_MODE");
                if (Kaboum.MODE_LIVE.equalsIgnoreCase(mode)) {
                        liveMode = KaboumFeatureServerTools.LIVE;
                } else if (Kaboum.MODE_KEEP.equalsIgnoreCase(mode)) {
                        liveMode = KaboumFeatureServerTools.KEEP;
                } else if (Kaboum.MODE_KEEP_CACHE.equalsIgnoreCase(mode)) {
                        liveMode = KaboumFeatureServerTools.KEEP_CACHE;
                }

                //
                // Folowing the DEFAULT display descriptor parameters.
                // Parameters from others display descriptor (except geoName
                // which must be unique) inherit from DEFAULT parameters
                // unless they are redefined
                //
                parent.defaultDD = new KaboumGeometryDisplayDescriptor("DEFAULT",
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_COLOR"), null),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_FILL_COLOR"), null),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_HILITE_COLOR"), null),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_SUPER_HILITE_COLOR"), null),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_MODIFIED_COLOR"), null),
                        KaboumCoordinate.stoi(shuttle.parameters.getProperty("DEFAULT_DD_POINT_TYPE")),
                        KaboumUtil.stoi(shuttle.parameters.getProperty("DEFAULT_DD_POINT_HEIGHT")),
                        KaboumUtil.stoi(shuttle.parameters.getProperty("DEFAULT_DD_POINT_WIDTH")),
                        KaboumUtil.stoi(shuttle.parameters.getProperty("DEFAULT_DD_LINE_WIDTH")),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_POINT_COLOR"), null),
                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty("DEFAULT_DD_POINT_HILITE_COLOR"), null),
                        Kaboum.readImage(parent, KaboumUtil.toURL(shuttle.parameters.getProperty("DEFAULT_DD_POINT_IMAGE"))),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_DD_IS_FILLED"), false));

                parent.geometryDDHash.put("DEFAULT", parent.defaultDD);
                parent.currentDD = parent.defaultDD;

                //
                // Display descriptor class list
                // These classes inherit from DEFAULT class
                //
                param = shuttle.parameters.getProperty("DD_CLASS_LIST");
                if (param != null) {
                        // Variables definition
                        KaboumGeometryDisplayDescriptor kaboumDisplayDescriptor;
                        st = new StringTokenizer(param, ",");
                        // Awfull trick to avoid null st when only one
                        // geometry is specified. In this case add a ","
                        // at the end of the param string
                        if (st.countTokens() == 0) {
                                String s = param + ",";
                                st = new StringTokenizer(param, ",");
                        }
                        // Loops over all defined class names
                        while (st.hasMoreTokens()) {
                                // Geometry Class name
                                String geoName = st.nextToken();
                                // Skip DEFAULT geometry if specified in the list
                                if (geoName.equals("DEFAULT")) {
                                        continue;
                                }
                                // Button type
                                int geoPointType;
                                if (shuttle.parameters.getProperty(geoName + "_DD_POINT_TYPE") == null) {
                                        geoPointType = parent.defaultDD.getPointType();
                                } else {
                                        geoPointType = KaboumCoordinate.stoi(shuttle.parameters.getProperty(geoName + "_DD_POINT_TYPE"));
                                }
                                // Object image path
                                Image geoPointImage;
                                if (shuttle.parameters.getProperty(geoName + "_DD_POINT_IMAGE") == null) {
                                        geoPointImage = parent.defaultDD.getPointImage();
                                } else {
                                        geoPointImage = Kaboum.readImage(parent, KaboumUtil.toURL(shuttle.parameters.getProperty(geoName + "_DD_POINT_IMAGE")));
                                }
                                // Add this geometry class to the list of geometry classes
                                kaboumDisplayDescriptor = new KaboumGeometryDisplayDescriptor(geoName,
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_COLOR"), parent.defaultDD.getColor()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_FILL_COLOR"), parent.defaultDD.getFillColor()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_HILITE_COLOR"), parent.defaultDD.getHiliteColor()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_SUPER_HILITE_COLOR"), parent.defaultDD.getSuperHiliteColor()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_MODIFIED_COLOR"), parent.defaultDD.getModifiedColor()),
                                        geoPointType,
                                        KaboumUtil.stoi(shuttle.parameters.getProperty(geoName + "_DD_POINT_HEIGHT"), parent.defaultDD.getPointHeight()),
                                        KaboumUtil.stoi(shuttle.parameters.getProperty(geoName + "_DD_POINT_WIDTH"), parent.defaultDD.getPointWidth()),
                                        KaboumUtil.stoi(shuttle.parameters.getProperty(geoName + "_DD_LINE_WIDTH"), parent.defaultDD.getLineWidth()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_POINT_COLOR"), parent.defaultDD.getPointColor()),
                                        KaboumUtil.getColorParameter(shuttle.parameters.getProperty(geoName + "_DD_HILITE_COLOR"), parent.defaultDD.getHiliteColor()),
                                        geoPointImage,
                                        KaboumUtil.stob(shuttle.parameters.getProperty(geoName + "_DD_IS_FILLED"), parent.defaultDD.getFilling()));
                                parent.geometryDDHash.put(geoName, kaboumDisplayDescriptor);
                        }
                }
                // Folowing the DEFAULT geometry parameters.
                // Parameters from others geometry inherit from DEFAULT parameters
                // unless they are redefined
                parent.defaultPD = new KaboumGeometryPropertiesDescriptor("DEFAULT",
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_IS_VISIBLE"), true),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_IS_COMPUTED"), false),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_IS_SURROUNDING"), false),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_IS_LOCKED"), false),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_VERTEX_SNAPPING"), false),
                        KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_PD_EDGE_SNAPPING"), false));
                parent.defaultPD.setMultiSupported(KaboumUtil.stob(shuttle.parameters.getProperty("DEFAULT_MULTI_OBJECTS"), true));
                parent.geometryPDHash.put("DEFAULT", parent.defaultPD);
                parent.currentPD = parent.defaultPD;
                // Properties class list
                // These classes inherit from DEFAULT properties class
                param = shuttle.parameters.getProperty("PD_CLASS_LIST");
                if (param != null) {
                        // Variables definition
                        KaboumGeometryPropertiesDescriptor kaboumObjectProperties;
                        st = new StringTokenizer(param, ",");
                        // Awfull trick to avoid null st when only one
                        // geometry is specified. In this case add a ","
                        // at the end of the param string
                        if (st.countTokens() == 0) {
                                String s = param + ",";
                                st = new StringTokenizer(param, ",");
                        }
                        // Loops over all geometries
                        while (st.hasMoreTokens()) {
                                // Properties class name
                                String propName = st.nextToken().trim();
                                // Skip DEFAULT geometry if specified in the list
                                if (propName.equals("DEFAULT")) {
                                        continue;
                                }
                                kaboumObjectProperties = new KaboumGeometryPropertiesDescriptor(propName,
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_IS_VISIBLE"), parent.defaultPD.isVisible()),
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_IS_COMPUTED"), parent.defaultPD.isComputed()),
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_IS_SURROUNDING"), parent.defaultPD.isSurrounding()),
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_IS_LOCKED"), parent.defaultPD.isLocked()),
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_VERTEX_SNAPPING"), parent.defaultPD.isVertexSnappable()),
                                        KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_PD_EDGE_SNAPPING"), parent.defaultPD.isEdgeSnappable()));
                                kaboumObjectProperties.setMultiSupported(KaboumUtil.stob(shuttle.parameters.getProperty(propName + "_MULTI_OBJECTS"), parent.defaultPD.isMultiSupported()));

                                parent.geometryPDHash.put(propName, kaboumObjectProperties);
                        }
                }
                // Active Display Descriptor
                param = shuttle.parameters.getProperty("GEOMETRY_ACTIVE_DD");
                if (param != null) {
                        parent.currentDD = (KaboumGeometryDisplayDescriptor) parent.geometryDDHash.get(param);
                        if (parent.currentDD == null) {
                                parent.currentDD = parent.defaultDD;
                        }
                }
                // Active Properties
                param = shuttle.parameters.getProperty("GEOMETRY_ACTIVE_PD");
                if (param != null) {
                        parent.currentPD = (KaboumGeometryPropertiesDescriptor) parent.geometryPDHash.get(param);
                        if (parent.currentPD == null) {
                                parent.currentPD = parent.defaultPD;
                        }
                }
        }

        /**
         * Sends a addFeature or UpdateFeature command to the server. according to the newGeom value
         * if it is a new one, adds the geometry, else updates it.
         * Builds a shuttle and sets it to the corresponding mode,
         * setting its geometries vector with the geometry to validate.<br/>
         * sends a kaboumResult command to the JS client telling which geometry was updated or added
         * with a json representation of the geometries and current operation (ADDED or UPDATED)
         * @param geom the geometry to add or to update
         * @param geomID the internal Kaboum geometry's unique id.
         * @param className the name of the geometry's class
         * @param newGeom true if the geometries to add are new (add operation) or not (update operation)
         *@return false if shuttle returned an error code
         */
        //public boolean validateGeometries(Vector<KaboumGeometry> geoms, String className, boolean newGeom) {
        public boolean validateGeometries(Vector geoms, String className, boolean newGeom) {
                if (geoms == null) {
                        KaboumUtil.debug("validategeometry: null input geometries to validate...");
                        return false;
                }
                KaboumFeatureShuttle tmpShuttle = new KaboumFeatureShuttle(
                        parent.mapServerTools.getRealExtent().externalString(" ", " "),
                        propertiesFile);

                tmpShuttle.geometries.put(className, geoms);
                tmpShuttle.mode = newGeom ? KaboumFeatureModes.K_ADD_FEATURES : KaboumFeatureModes.K_UPDATE_FEATURES;
                String kw = tmpShuttle.mode == KaboumFeatureModes.K_ADD_FEATURES ? "ADDED" : "UPDATED";
                tmpShuttle = getServerResponse(tmpShuttle);
                if (tmpShuttle == null) {
                        parent.kaboumResult("ALERT|NULL_SERVER_RESPONSE");
                        return false;
                }
                if (tmpShuttle.errorCode == KaboumFeatureModes.K_NO_GENERATED_ID) {
                        // geometry insertion is done here
                        getFeatures();
                        //parent.kaboumResult("GEOMETRY|" + kw + "|" + getIDList(tmpShuttle));
                        parent.kaboumResult("GEOMETRY|" + geometriesToJson(tmpShuttle, kw));
                } else if (tmpShuttle.errorCode > 0) {
                        parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(tmpShuttle.errorCode));
                        if (isRestoreMode) {
                                // in restore mode...
                                restoreGeometry();
                        }
                        // should insert or not ??
                        return false;
                } else {
                        // inserts all validated geometries, to update their coordinates in Kaboum
                        insertGeometries(tmpShuttle, false);
                        //parent.kaboumResult("GEOMETRY|" + kw + "|" + getIDList(tmpShuttle));
                        parent.kaboumResult("GEOMETRY|" + geometriesToJson(tmpShuttle, kw));
                }
                return true;
        }

        /**
         * Sends a addFeature or UpdateFeature command to the server. according to the geometry's id
         * if it is a new one, adds the geometry, else updates it.
         * Builds a shuttle and sets it to the corresponding mode,
         * setting its geometries vector with the geometry to validate.<br/>
         * sends a kaboumResult command to the JS client telling which geometry was updated or added
         * with a json representation of the geometries and current operation (ADDED or UPDATED)
         * @param geom the geometry to add or to update
         * @param geomID the internal Kaboum geometry's unique id.
         * @param className the name of the geometry's class
         *@return false if shuttle returned an error code
         */
        public boolean validateGeometry(KaboumGeometry geom, String className) {
                if (geom == null || className == null) {
                        return false;
                }
                //Vector<KaboumGeometry> geoms = new Vector(1);
                Vector geoms = new Vector(1);
                geoms.add(geom);
                return validateGeometries(geoms, className, Kaboum.K_NEW_GEOMETRY.equals(geom.id));
        }

        /**
         * Sends an addFeature command to the server with the given geometries
         * @param geoms the vector of geometries to add
         * @param className the name of the class geometries belong to
         * @return true if the Add operation did not generate an error message, false otherwise:
         * kaboum will send to the client the error code.
         */
        public boolean AddGeometries(Vector geoms, String className) {
                if (geoms == null) {
                        KaboumUtil.debug("addGeometries: null input geometries to add...");
                        return false;
                }
                KaboumFeatureShuttle tmpShuttle = new KaboumFeatureShuttle(
                        parent.mapServerTools.getRealExtent().externalString(" ", " "),
                        propertiesFile);

                tmpShuttle.geometries.put(className, geoms);
                tmpShuttle.mode = KaboumFeatureModes.K_ADD_FEATURES;
                String kw = "ADDED";
                tmpShuttle = getServerResponse(tmpShuttle);
                if (tmpShuttle == null) {
                        parent.kaboumResult("ALERT|NULL_SERVER_RESPONSE");
                        return false;
                }
                if (tmpShuttle.errorCode == KaboumFeatureModes.K_NO_GENERATED_ID) {
                        // geometry insertion is done here
                        getFeatures();
                        parent.kaboumResult("GEOMETRY|" + geometriesToJson(tmpShuttle, kw));
                } else if (tmpShuttle.errorCode > 0) {
                        parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(tmpShuttle.errorCode));
                        return false;
                } else {
                        // inserts all validated geometries, to update their coordinates in Kaboum
                        insertGeometries(tmpShuttle, false);
                        parent.kaboumResult("GEOMETRY|" + geometriesToJson(tmpShuttle, kw));
                }
                return true;
        }

        /**
         * Deals with splitting operation by
         * Updating the first object contained in the given GGD's geometry and adding all other objects,
         *  if any.
         * <p>
         * The add operation will take place only if update was successful.
         * Then the opMode corresponding to the opModeName will be respawn.
         *</p>
         * This method is only called if KaboumServer properties file variable CLIENT_MANAGE_SPLIT is set to true
         * @param ggdToValidate the KaboumGeometryGlobalDescriptor containing the geometry elements to update and add on the server
         * @param opModeName the name of the opMode to respwan after successful validation/removal
         */
        public void updateAndAdd(KaboumGeometryGlobalDescriptor ggdToValidate, String opModeName) {
                if (ggdToValidate != null) {
                        //Vector<KaboumGeometry> geomsToAdd = null;
                        Vector geomsToAdd = null;
                        // prepare the geometries to add and update.
                        KaboumGeometry geomToUpdate = ggdToValidate.geometry.getNumGeometries() == 1 ? ggdToValidate.geometry
                                : ggdToValidate.geometry.getGeometryN(0);
                        // force a collection if input geometry class is multi
                        if (ggdToValidate.pd.isMultiSupported()) {
                                geomToUpdate = KaboumGeometryCollection.forceCollection(geomToUpdate);
                        }
                        geomToUpdate.id = ggdToValidate.id;


                        if (ggdToValidate.geometry.getNumGeometries() > 1) {
                                //geomsToAdd = new Vector<KaboumGeometry>(ggdToValidate.geometry.getNumGeometries() - 1);
                                geomsToAdd = new Vector(ggdToValidate.geometry.getNumGeometries() - 1);
                                for (int i = 1; i < ggdToValidate.geometry.getNumGeometries(); i++) {
                                        KaboumGeometry geom = ggdToValidate.geometry.getGeometryN(i);
                                        // forces distinct ids to allow GT to add all the features and not just the first one
                                        if (ggdToValidate.pd.isMultiSupported()) {
                                                geom = KaboumGeometryCollection.forceCollection(geom);
                                        }
                                        geom.id = Kaboum.K_NEW_GEOMETRY + i;
                                        geomsToAdd.add(geom);
                                }
                        }

                        if (validateGeometry(geomToUpdate, ggdToValidate.dd.name)) {
                                validateGeometries(geomsToAdd, ggdToValidate.dd.name, true);
                        } else {
                                KaboumUtil.debug("unable to validate the first geometry in the collection on the server");
                        }
                }
                // now unfreeze Kaboum and respawn opMode
                parent.standbyOff();
                parent.kaboumCommand(opModeName);
        }

        /**
         * Validates the given geometry on the server, and removes all geometries
         * contained in the vector.
         * <p>
         * The removal will take place only if validation was successful.
         * Then the opMode corresponding to the opModeName will be respawn.
         *</p>
         * @param ggdToValidate the KaboumGeometryGlobalDescriptor to validate to the server
         * @param ggdToRemove the vector of KaboumGeometryGlobalDescriptor to remove on the server
         * (if id != K_NEW_OBJECT) and on the client.
         * @param opModeName the name of the opMode to respwan after successful validation/removal
         */
        public void validateAndRemove(
                KaboumGeometryGlobalDescriptor ggdToValidate,
                Vector ggdToRemove,
                String opModeName) {

                if (ggdToValidate != null) {
                        if (validateGeometry(ggdToValidate.geometry, ggdToValidate.dd.name)) {
                                if (!ggdToValidate.id.equals(Kaboum.K_NEW_GEOMETRY)
                                        && ("POLYGON".equals(opModeName) || "LINESTRING".equals(opModeName))) {
                                        // if ( !ggdToValidate.id.equals(Kaboum.K_NEW_GEOMETRY)) {
                                        // special case: no need to remove the geometry: it will be replaced when inserting the
                                        // new one
                                        // just change the geometries edition mode:
                                        ggdToValidate.setSelected(false);
                                } else {
                                        removeGeometries(ggdToRemove, ggdToValidate.dd.name, null);
                                }
                        }
                }
                // now unfreeze Kaboum and respawn opMode
                parent.standbyOff();
                parent.kaboumCommand(opModeName);
        }

        /**
         * Sends a removeFeature command to the server.
         * Builds a valid shuttle and sets it to the corresponding mode,
         * setting its geometries vector with the geometry to remove<br>
         * the geometry to remove will only contain the ID field, not coordinates.
         * @param geomID the geometry id to remove
         * @param className the name of the geometry's class
         * @param opMode the name of the opMode to respawn after successful removal, or
         * null if no opMode should be respawned
         */
        public void removeGeometry(String geomID, String className, String opMode) {
                if (geomID == null) {
                        return;
                }
                Vector vec = new Vector(1);
                vec.addElement(geomID);
                removeGeometries(vec, className, opMode);
        }

        /**
         * Sends a removeFeature command to the server with all the given KaboumGeometryGlobalDescriptor.
         * Builds a shuttle and sets it to the corresponding mode,
         * setting its geometries vector with the given geometries to remove<br>
         * the geometries to remove will only contain the ID field, not coordinates.
         * @param geometries a vector of KaboumGeometryGlobalDescriptor or String id to remove
         * @param className the name of the geometry's class
         * @param opMode the name of the opMode to respawn after successful removal, or
         * null if no opMode should be respawned
         */
        public void removeGeometries(Vector geometries, String className, String opMode) {
                if (geometries == null) {
                        return;
                }
                String geomID = null;
                KaboumFeatureShuttle tmpShuttle = new KaboumFeatureShuttle(
                        parent.mapServerTools.getRealExtent().externalString(" ", " "),
                        propertiesFile);
                Vector vec = new Vector(geometries.size());
                // build a new Empty dummy geometry with the given ID
                KaboumGeometry geom = null;
                for (int i = 0; i < geometries.size(); i++) {
                        Object o = geometries.elementAt(i);
                        if (o instanceof String) {
                                geomID = (String) o;
                        } else if (o instanceof KaboumGeometryGlobalDescriptor) {
                                geomID = ((KaboumGeometryGlobalDescriptor) o).id;
                        }
                        if (!geomID.equals(Kaboum.K_NEW_GEOMETRY)) {
                                geom = new KaboumPoint(null);
                                geom.id = geomID;
                                vec.addElement(geom);
                        }
                }
                if (vec.size() > 0) {
                        // send a command only if there is something to remove
                        tmpShuttle.geometries.put(className, vec);
                        tmpShuttle.mode = KaboumFeatureModes.K_REMOVE_FEATURES;
                        //gets server response
                        tmpShuttle = getServerResponse(tmpShuttle);
                        // manages error codes
                        if (tmpShuttle == null) {
                                parent.kaboumResult("ALERT|NULL_SERVER_RESPONSE");
                                return;
                        } else if (tmpShuttle.errorCode > 0) {
                                parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(tmpShuttle.errorCode));
                                return;
                        }
                        //parent.kaboumResult("GEOMETRY|REMOVED|" + getIDList(tmpShuttle));
                        parent.kaboumResult("GEOMETRY|" + geometriesToJson(tmpShuttle, "REMOVED"));
                }
                // can remove all geometries from Kaboum, as they are removed on the backend
                for (int i = 0; i < geometries.size(); i++) {
                        Object o = geometries.elementAt(i);
                        if (o instanceof String) {
                                geomID = (String) o;
                        } else if (o instanceof KaboumGeometryGlobalDescriptor) {
                                geomID = ((KaboumGeometryGlobalDescriptor) o).id;
                        }
                        parent.GGDIndex.removeGeometry(geomID);
                }
                if (opMode != null) {
                        parent.standbyOff();
                        parent.kaboumCommand(opMode);
                }
        }

        /**
         * Inserts all geometries contained in this shuttle into Kaboum and sets
         * their modified status to the given value.
         *<p>
         * Geometry insertion is controlled by the server SERVER_LIVE_MODE parameter:
         * if mode = live, existing geometries are removed from kaboum before inserting new ones
         * if mode = keep, existing geometries are kept,
         * if mode = keep_cache, existing geometries are kept and a caching mechanism is set on
         *</p>
         *@param shuttle the KaboumFeatureShuttle containing the geometries to insert.
         *@param removeAll true to remove all geometries from kaboum before inserting new geometries
         */
        public void insertGeometries(KaboumFeatureShuttle shuttle, boolean removeAll) {
                // inserts the geometry in Kaboum:
                if (shuttle == null || shuttle.geometries == null) {
                        KaboumUtil.debug("insertGeometries: null shuttle or null geometries");
                        return;
                }
                // manage retrieval mode
                if (liveMode == KaboumFeatureServerTools.LIVE && removeAll) {
                        KaboumUtil.debug("liveMode enabled, removing all features from kaboum");
                        //int beforeDel = parent.GGDIndex.size();
                        int del = parent.GGDIndex.deleteAllGeometries();
                        //System.out.println("removed: " + del + "/" + beforeDel);
                }
                Enumeration keys = shuttle.geometries.keys();
                int i = 0;

                parent.GGDIndex.shouldReconstruct = false;

                while (keys.hasMoreElements()) {
                        String className = (String) keys.nextElement();
                        Vector vec = (Vector) shuttle.geometries.get(className);

                        if (vec != null) {
                                // gets the Display Descriptor and properties descriptor corresponding to this className
                                KaboumGeometryPropertiesDescriptor tmpPD = (KaboumGeometryPropertiesDescriptor) parent.geometryPDHash.get(className);

                                if (tmpPD == null) {
                                        KaboumUtil.debug("WARNING ! : Properties Descriptor " + className + " doesn't exist. Using DEFAULT instead ");
                                        tmpPD = parent.defaultPD;
                                }
                                KaboumGeometryDisplayDescriptor tmpDD = (KaboumGeometryDisplayDescriptor) parent.geometryDDHash.get(className);
                                if (tmpDD == null) {
                                        KaboumUtil.debug("WARNING ! : Display Descriptor " + className + " doesn't exist. Using DEFAULT instead ");
                                        tmpDD = parent.defaultDD;
                                }
                                for (i = 0; i < vec.size(); i++) {
                                        KaboumGeometry geom = (KaboumGeometry) vec.elementAt(i);
                                        KaboumGeometryGlobalDescriptor gd = parent.GGDIndex.addGeometry(geom, tmpDD, tmpPD, null, false);
                                        //System.out.println("gd added: " + gd.toString());

                                        if (geom.isClosed()) {
                                                parent.currentSurface = geom.getArea();
                                        }
                                        parent.currentPerimeter = geom.getPerimeter();
                                        /*
                                        KaboumWKTWriter writer = new KaboumWKTWriter(parent.getPrecisionModel());
                                        try {
                                        System.out.println("inserted: " + geom.id + " " + writer.write(geom));
                                        } catch (IOException ex) {
                                        KaboumUtil.debug(ex.getMessage());
                                        }
                                         */
                                } // end for
                                //KaboumUtil.debug("inserted " + i + " geometries for class: " + className);
                        }
                } // end while

                // now all is added, force visible geometries reconstruction:
                parent.GGDIndex.shouldReconstruct = true;
                parent.GGDIndex.reconstructVisibleGeometries();
                parent.repaint();
        }

        /**
         * Retrieves geometric objects for the current extent, from the server.
         *<p>
         * Features retrieval is ruled by the SERVER_LIVE_MODE parameter:
         * if SERVER_LIVE_MODE=LIVE, all objects are retrieved for each new map extent,
         * removing all previously loaded objects from Kaboum.
         * if SERVER_LIVE_MODE=KEEP, retrieved objects are kept in kaboum. It is up to the user
         * to remove objects explicitly
         * if SERVER_LIVE_MODE=KEEP_CACHE, same mode as keep, with a caching mechanism
         * to skip feature retrieval if current map extent is contained in the previous map extent.
         *
         *</p>
         *<p>
         * If standbyOn() was called on this class, this method does nothing.<br>
         * re-enable features retrieval by calling standbyOff().
         *</p>
         */
        public void getFeatures() {
                if (standby) {
                        return;
                }

                if (liveMode == KaboumFeatureServerTools.KEEP_CACHE && biggestExtent.contains(parent.mapServerTools.extent)) {
                        KaboumUtil.debug("skipping feature loading...");
                        //return;
                } else {
                        KaboumFeatureShuttle shuttle = new KaboumFeatureShuttle(
                                parent.mapServerTools.getRealExtent().externalString(" ", " "),
                                propertiesFile);
                        shuttle.mode = KaboumFeatureModes.K_GET_FEATURES;

                        //gets server response
                        shuttle = getServerResponse(shuttle);

                        // manages error codes
                        if (shuttle == null) {
                                parent.kaboumResult("ALERT|NULL_SERVER_RESPONSE");
                                return;
                        } else if (shuttle.errorCode > 0) {
                                parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(shuttle.errorCode));

                                //only exist in case of serious error
                                if (shuttle.errorCode != KaboumFeatureModes.K_MAX_FEATURES_REACHED) {
                                        return;
                                }
                        }
                        insertGeometries(shuttle, true);

                        // swaps extents if current contains biggest
                        biggestExtent.set(parent.mapServerTools.getRealExtent());
                }
        }

        /** Sends shuttle object to the server, asking it to response with a filled shuttle,
         * based on the mode and extra parameters.
         */
        public KaboumFeatureShuttle getServerResponse(KaboumFeatureShuttle shuttleIn) {
                KaboumFeatureShuttle shuttleOut = null;
                ObjectOutputStream oout = null;
                ObjectInputStream oin = null;

                // now passes the kaboum.userMetadata request parameter, under the KABOUM_USER_METADATA key.

                shuttleIn.userMetadata = parent.userMetadata;
                /*
                String param = "";
                if (parent.userMetadata != null) {
                if (featureServerURL.contains("?")) {
                param = "&";
                } else {
                param = "?";
                }
                try {
                String enc = URLEncoder.encode(parent.userMetadata, "UTF-8");
                param += "KABOUM_USER_METADATA=" + enc;
                } catch (UnsupportedEncodingException uee) {
                uee.printStackTrace();
                }
                }
                 */
                URL serverURL = KaboumUtil.toURL(featureServerURL);
                URLConnection con = null;

                try {
                        long t0 = System.currentTimeMillis();

                        KaboumUtil.debug("FeatureServer call: " + serverURL.toString());

                        con = serverURL.openConnection();
                        con.setDoInput(true);
                        con.setDoOutput(true);
                        con.setUseCaches(false);
                        con.setDefaultUseCaches(false);
                        con.setRequestProperty("Content-Type", "application/octet-stream");
                        // explicitly allows compression for servers sniffing compression type,
                        // like KaboumServer
                        con.setRequestProperty("Accept-Encoding", "gzip,deflate");
                        long tcon = System.currentTimeMillis();

                        // send command object to server
                        //KaboumUtil.debug("sending shuttle: " + shuttleIn.toString());

                        oout = new ObjectOutputStream(con.getOutputStream());
                        oout.writeObject(shuttleIn);
                        oout.flush();
                        oout.close();
                        shuttleIn = null;

                        long twrite = System.currentTimeMillis();

                        // now deals with the server response: a shuttle object
                        //oin = new ObjectInputStream(con.getInputStream());
                        oin = getObjectInputStream(con);
                        shuttleOut = (KaboumFeatureShuttle) oin.readObject();

                        if (shuttleOut != null) {
                                //KaboumUtil.debug("received shuttle: " + shuttleOut.toString());
                        }
                        long tread = System.currentTimeMillis();
                        // refreshes parameters
                        loadServerParameters(shuttleOut);

                        oin.close();

                        long tend = System.currentTimeMillis();

                        /*
                        StringBuffer buf = new StringBuffer("time taken for entire connection : ");
                        buf.append((tend - t0)).append("\n");
                        buf.append("time taken for openConnection    : ");
                        buf.append((tcon - t0)).append("\n");
                        buf.append("time taken for sending shuttle   : ");
                        buf.append((twrite - tcon)).append("\n");
                        buf.append("time taken for receiving shuttle : ");
                        buf.append((tread - twrite)).append("\n");
                        buf.append("time taken for params parsing    : ");
                        buf.append((tend - tread)).append("\n");
                         */
                        //KaboumUtil.debug(buf.toString());
                        //System.out.println(KaboumUtil.getMemoryUsage());

                } catch (OutOfMemoryError oofm) {
                        parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(KaboumFeatureModes.K_OUT_OF_MEMORY));
                        return null;
                } catch (Exception ioe) {
                        ioe.printStackTrace();
                        KaboumUtil.debug(ioe.getMessage());
                }
                return shuttleOut;
        }

        /**
         * Returns an object input stream from the given URLCOnnection, deflating
         * it if needed (gzip compression set for the content-encoding.
         *<p>
         *KaboumServer can be configured to compress response above a certain threshold
         *</p>
         */
        public ObjectInputStream getObjectInputStream(URLConnection con) throws IOException {
                if (con == null) {
                        return null;        // guess if server response is gzip-compressed
                }
                String contentEncoding = con.getHeaderField("Content-Encoding");
                boolean isCompression = (contentEncoding != null && contentEncoding.indexOf("gzip") >= 0);
                ObjectInputStream in = null;

                if (isCompression) {
                        KaboumUtil.debug("receiving compressed stream from server.");
                        in = new ObjectInputStream(new GZIPInputStream(con.getInputStream()));
                } else {
                        KaboumUtil.debug("receiving uncompressed stream from server.");
                        in = new ObjectInputStream(con.getInputStream());
                }
                return in;
        }

        /**
         * blocks server features retrieval. No more server connection will be made after this call.<br>
         * call standbyOff() to re-enable features retrieval.
         *<p>
         *This method is controled by the OBJECT|OFF kaboum command
         *</p>
         */
        public void standbyOn() {
                this.standby = true;
        }

        /**
         * Allows server features retrieval. Call this method to re-enable features retrieval
         * after a call to standbyOn().
         * This method is controled by the OBJECT|ON kaboum command
         */
        public void standbyOff() {
                this.standby = false;
        }

        /**
         * Call the server with given geometries and mode to process a spatial analysis.
         *
         *@param className the name of the Geometries kaboum class
         *@param geometries a vector of KaboumGeometry
         *@param spatialAnalysis one of the KaboumFeatureModes values for modes
         *@param preserveGeometry true to keep a reference on the first geometry contained in the vector,
         * to be later used if operation is canceled.
         */
        public void processSpatialAnalysis(String className, Vector geometries, short spatialAnalysis, boolean preserveGeometry) {
                if (geometries == null || geometries.size() == 0) {
                        return;
                }
                isRestoreMode = preserveGeometry;

                if (preserveGeometry) {
                        // stores the first geometry in the vector to allow a cancel or restore operation
                        storedGeometry = (KaboumGeometry) geometries.elementAt(0);
                        storedClassName = className;
                }

                KaboumFeatureShuttle shuttle = new KaboumFeatureShuttle();
                Hashtable geoms = new Hashtable();

                shuttle.mode = spatialAnalysis;
                geoms.put(className, geometries);
                shuttle.geometries = geoms;

                shuttle = getServerResponse(shuttle);

                if (shuttle == null) {
                        parent.kaboumResult("ALERT|NULL_SERVER_RESPONSE");
                        return;
                }

                String idList = "";
                // build the kaboumCommand to reload last OpMode
                String cmd = "GEOMETRY_" + KaboumFeatureModes.getMode(spatialAnalysis);

                if (shuttle.errorCode > 0) {
                        //should reset opModeStatus ?
                        parent.kaboumResult("ALERT|" + KaboumFeatureModes.getErrorCode(shuttle.errorCode));
                        // should insert or not ??
                        // removes selected id list stored in Kaboum to reinit the drawer.
                } else {
                        insertGeometries(shuttle, false);
                }
                parent.standbyOff();
                parent.kaboumCommand(cmd);
        }

        /** reinserts the storedGeometry in Kaboum.
         * this method can be used to cancel a spatial analysis
         */
        public void restoreGeometry() {
                if (storedClassName != null) {
                        KaboumFeatureShuttle shuttle = new KaboumFeatureShuttle();
                        Vector vec = new Vector(1);
                        vec.addElement(storedGeometry);
                        shuttle.geometries.put(storedClassName, vec);

                        insertGeometries(shuttle, false);

                        storedClassName = null;
                        storedGeometry = null;
                }
        }

        /**
         * gets a comma-separated list of KaboumGeometries ids contained in the given shuttle.
         *@param shuttle the KaboumFeatureShuttle containing geometries
         *@return a String with id separated by commas
         */
        public String getIDList(KaboumFeatureShuttle shuttle) {
                if (shuttle == null) {
                        return null;
                }
                StringBuilder builder = new StringBuilder();
                /**
                for (Vector<KaboumGeometry> vec : shuttle.geometries.values()) {
                for (KaboumGeometry geom : vec) {
                builder.append(geom.id).append(",");
                builder.append(geom.getPerimeter()).append(",");
                builder.append(geom.getArea()).append(",");
                builder.append(geom.getToolTip());
                }
                builder.append(";");
                }
                 */
                for (Iterator iter = shuttle.geometries.values().iterator(); iter.hasNext();) {
                        Vector vec = (Vector) iter.next();
                        for (Iterator iter2 = vec.iterator(); iter2.hasNext();) {
                                KaboumGeometry geom = (KaboumGeometry) iter2.next();
                                builder.append(geom.id).append(",");
                                builder.append(geom.getPerimeter()).append(",");
                                builder.append(geom.getArea()).append(",");
                                builder.append(geom.getToolTip());
                        }
                        builder.append(";");
                }
                builder.deleteCharAt(builder.length() - 1);
                return builder.toString();

        }

        /**
         * Sets the path to the KaboumServer properties file.
         * This path will be pass (ah ah) to the server, allowing clients to override
         * default properties file configured in the servlet.
         *@param props the path (relative to the webapp) to the KaboumServer properties file to use
         */
        public void setPropertiesFile(String props) {
                propertiesFile = props;
        }

        /**
        Debug method returning a String representation of KaboumGeometries contained in the given
        shuttle object

        @param shuttle the KaboumFeatureShuttle object whose geometries will be output as Strings
        @return a String representation of the KaboumGeometries
         */
        public String displayShuttleObjects(KaboumFeatureShuttle shuttle) {
                if (shuttle == null || shuttle.geometries == null || shuttle.geometries.size() == 0) {
                        return "null or empty geometries";
                }
                StringBuffer buf = new StringBuffer();
                Enumeration e = shuttle.geometries.keys();
                while (e.hasMoreElements()) {
                        String key = (String) e.nextElement();
                        buf.append("Geoms for class: ").append(key);
                        Vector vec = (Vector) shuttle.geometries.get(key);
                        for (int i = 0; i < vec.size(); i++) {
                                buf.append("");
                        }
                }
                return buf.toString();
        }

        /**
         * Returns the JSON String representation of the given geometries, namely:
         * {'operation': 'ADDED|UPDATED',
         *  'geomlist': [{'id':'classid_3', 'perimeter':'222', 'area':'12.2', 'tooltip':'attrib: toto'},{},...]}
         * @param shuttle
         * @param operation
         * @return
         */
        public String geometriesToJson(KaboumFeatureShuttle shuttle, String operation) {
                if (shuttle == null || shuttle.geometries == null || shuttle.geometries.size() == 0) {
                        return "null or empty geometries";
                }
                StringBuilder builder = new StringBuilder("{\\'operation\\':\\'");
                builder.append(operation).append("\\',\\'geomlist\\':");
                builder.append("[");
                String kgeomId = "";
                // try {
                //for (Vector<KaboumGeometry> vec : shuttle.geometries.values()) {
                //    for (KaboumGeometry geom : vec) {
                for (Iterator iter = shuttle.geometries.values().iterator(); iter.hasNext();) {
                        Vector vec = (Vector) iter.next();
                        for (Iterator iter2 = vec.iterator(); iter2.hasNext();) {
                                KaboumGeometry geom = (KaboumGeometry) iter2.next();

                                kgeomId = geom.id;
                                builder.append("{");
                                builder.append("\\'id\\':\\'").append(kgeomId).append("\\',");
                                builder.append("\\'perimeter\\':\\'").append(geom.getPerimeter()).append("\\',");
                                builder.append("\\'area\\':\\'").append(geom.getArea()).append("\\',");
                                builder.append("\\'tooltip\\':\\'").append(geom.getToolTip()).append("\\',");
                                builder.append("\\'wkt\\':\\'\\'");
                                //builder.append("\\'wkt\\':\\'").append(writer.write(geom));
                                builder.append("},");
                        }
                        builder.deleteCharAt(builder.length() - 1);
                        builder.append(",");
                }
                // } catch (IOException ioe) {
                //     KaboumUtil.debug("cannot get WKT from geom: " + kgeomId + " message: " + ioe.getMessage());
                // }

                builder.deleteCharAt(builder.length() - 1);
                builder.append("]}");
                return builder.toString();
        }
}
