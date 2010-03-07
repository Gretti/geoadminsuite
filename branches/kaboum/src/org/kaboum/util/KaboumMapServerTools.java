/*
 *
 * Class KaboumMapServerTools from the Kaboum project.
 * This class is the io API between the applet and mapserver.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *

 */

package org.kaboum.util;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Polygon;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.URLEncoder;

import org.kaboum.geom.KaboumGGDIndex;
/**
 *
 * Cette classe s'occupe de traduire les requetes de l'applet
 * sous une forme comprehensible par mapserver.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumMapServerTools {
    
    /** Public constant */
    public static final String K_MODE_MAP = "map";
    public static final String K_MODE_QUERY = "query";
    public static final String K_MODE_NQUERY = "nquery";
    public static final String K_MODE_ITEM_QUERYMAP = "itemquerymap";
    public static final String K_MODE_ITEM_NQUERYMAP = "itemnquerymap";
    public static final String K_MODE_REFERENCE = "reference";
    public static final String K_MODE_BROWSE = "browse";
    public static final String K_TYPE_IMAGE_JPEG = "jpeg";
    
    /** Spatial extent of current map (i.e. "mouse extent" or false extent)*/
    public KaboumExtent extent;
    
    /** Spatial adjusted extent of current map (i.e. real extent) */
    public KaboumExtent realExtent;
    
    /** Spatial extent of reference map (i.e. initial extent) */
    public KaboumExtent referenceExtent;
    
    /** Spatial extent of previous map (i.e. "undo" extent) */
    public KaboumExtent previousExtent;
    
    /** Precision Model */
    public KaboumPrecisionModel pm;
    
    /** Layers (cf: mapserver) */
    public String layersList;
    
    /** Query layers (cf: mapserver) */
    public String queryLayersList;
    
    /** Faux pour deZoomer a l'infini */
    private KaboumExtent restrictedExtent = null;
    
    /** Chemin absolu du map file (cf: mapserver) */
    public String mapPath = null;
    
    /** Chemin relatif a mapserver (cf: mapserver) */
    public String mapserverURL = null;
    
    /** Applet size */
    private Dimension screenSize = null;
    
    /** Image click point */
    public Point img = new Point();
    
    /** History array */
    public Vector historyArray = new Vector();
    
    /** Max size of array history */
    private int historyMaxSize;
    
    /** History index */
    private int historyIndex = 0;
    
    /** True to avoid adding extent when parsing history */
    private boolean historyIsLastCall = false;
    
    /** Current scale */
    public int scale;
    
    /** Max Window Size (cf: EGIS add-on from terraSIP project) */
    public int maxWinSize = 5;
    
    /** Maximum scale allowed */
    private int maxScale = -1;
    
    /** Images are cached by http server if useCache set to true */
    private boolean useCache;
    
    /** Image type */
    private String imageType = "gif";
    
    /** Image quality */
    private int imageQuality = 100;

    /** GGDIndex reference */
    private KaboumGGDIndex GGDIndex = null;
    
    /** If true display Inline Feature */
    public boolean displayInline = false;

    
    /** Inline Feature String */
    private String msInlineString = "";
    
    /** tells if applet is in ITEM_QUERYMAP mode. In this case, specific Mapserver URL
     *   will be maintained to display found features with their hilited color.<br>
     * To do so, the mapserver URL will be modified to include itemquerymap parameters
     *@see msItemQueryMapString property 
     */
    public boolean displayItemQueryMap = false;
    
    /** the mapserver URL string allowing to display the itemquerymap mode (hilited found features) */
    private String msItemQueryMapString = "";

    private String customMsParams;

    /**
     *
     * Constructeur
     *
     * @param mapserverURL MapServer cgi URL (ex: http:://132.149.92.10/cgi-bin/mapserver.cgi)
     * @param mapPath          Absolute path to the map file
     * @param historyMaxSize   Size of history array
     * @param precisionModel   Precision Model
     * @param layersList       Initial layers list
     * @param queryLayersList  Initial query layers
     * @param restrictedExtent Set the maximum valid extent
     * @param maxScale         Maximum scale allow in zoom in
     * @param noCache          Images are cached by http server (FALSE) or no
     * @param referenceExtent  Initial map extent
     * @param screenSize       Size of the window applet
     * @param _GGDIndex        GGDIndex reference
     *
     */
    public KaboumMapServerTools(String _mapserverURL,
    String _mapPath,
    int _historyMaxSize,
    KaboumPrecisionModel _pm,
    String _layersList,
    String _queryLayersList,
    KaboumExtent _restrictedExtent,
    int _maxScale,
    boolean _useCache,
    KaboumExtent _referenceExtent,
    Dimension _screenSize,
    KaboumGGDIndex _GGDIndex
    ) {
        
        
        // Precision Model
        this.pm = _pm;
        
        // mapserver path
        this.mapserverURL = _mapserverURL;
        
        // mapfile path
        this.mapPath = _mapPath;
        
        // Spatial Reference extent
        if (_referenceExtent != null) {
            this.referenceExtent = _referenceExtent;
        }
        else {
            this.referenceExtent = new KaboumExtent();
        }
        
        //  size
        this.screenSize = _screenSize;
        
        // History array
        if (_historyMaxSize < 1) {
            this.historyMaxSize = 5;
        }
        else {
            this.historyMaxSize = _historyMaxSize;
        }
        
        // Layers initialisation
        setLayersList(_layersList);
        setQueryLayersList(_queryLayersList);
        
        // Un-zoom without restriction
        this.restrictedExtent = _restrictedExtent;
        
        // Maximum scale allowed
        this.maxScale = _maxScale;
        // Cache
        this.useCache = _useCache;
        
        // GGDIndex
        this.GGDIndex = _GGDIndex;
        
        // Initialisation
        this.extent = new KaboumExtent();
        this.realExtent = new KaboumExtent();
        this.previousExtent = new KaboumExtent();
        
        this.extent.set(this.referenceExtent);
        setAdjustedCoordinates();
        this.previousExtent.set(this.extent);
        this.customMsParams = "";
        
    }
    
    
    /**
     *
     * Return the internal coordinate corresponding to the mouse position
     *
     * @param x  Mouse x position
     * @param y  Mouse y position
     *
     */
    public KaboumCoordinate mouseXYToInternal(int x, int y) {
        return new KaboumCoordinate(this.mouseXToInternal(x), this.mouseYToInternal(y));
    }
    
    
    /**
     *
     * Cette methode renvois la coordonnee x carte d'un point
     * d'abscisse x dans le repere de la zone d'affichage de
     * la carte (haut pixel)
     *
     */
    public double mouseXToInternal(double x) {
        return KaboumPrecisionModel.makePrecise(this.realExtent.xMin + (x * (this.realExtent.dx())) / (screenSize.width - 1));
    }
    
    
    /**
     *
     * Cette methode renvois la coordonnee y carte d'un point
     * d'ordonnee y dans le repere de la zone d'affichage de
     * la carte (haut pixel)
     *
     */
    public double mouseYToInternal(double y) {
        return KaboumPrecisionModel.makePrecise(this.realExtent.yMax + (y * (this.realExtent.dy())) / (1 - screenSize.height));
    }
    
    
    /**
     *
     * Cette methode renvois la coordonnee x souris d'un point
     * d'abscisse x dans le repere de la carte
     *
     */
    public int internalToMouseX(double x) {
        return (int) (((this.realExtent.xMin - x) * (1 - screenSize.width)) / (this.realExtent.dx()));
    }
    
    
    /**
     *
     * Cette methode renvois la coordonnee y souris d'un point
     * d'abscisse y dans le repere de la carte
     *
     */
    public int internalToMouseY(double y) {
        return (int) (((this.realExtent.yMax - y) * (screenSize.height - 1)) / (this.realExtent.dy()));
    }
    
    
    /**
     *
     * Return the Point(x,y) equivalence of an internal coordinate
     *
     */
    public Point internalToMouseXY(KaboumCoordinate internal) {
        return new Point(this.internalToMouseX(internal.x), this.internalToMouseY(internal.y));
    }
    
    
    /**
     *
     * Convert an array of internal coordinates into
     * a java.awt.Polygon
     *
     */
    public Polygon internalToPolygon(KaboumCoordinate[] internals) {
        
        Polygon polygon = new Polygon();
        int numPoints = internals.length;
        
        for (int i = 0; i < numPoints; i++) {
            polygon.addPoint(this.internalToMouseX(internals[i].x), this.internalToMouseY(internals[i].y));
        }
        
        return polygon;
    }
    
    //***********************************METHODES DE CLASSE************************************
    
    
    /**
     *
     * Set the image type
     *
     * @param imageType Type of image
     *
     */
    public void setImageType(String str) {
        
        if (str == null) {
            this.imageType = null;
        }
        if (str.equalsIgnoreCase("JPEG")) {
            this.imageType = K_TYPE_IMAGE_JPEG;
        }
        else {
            this.imageType = null;
        }
        
        KaboumUtil.debug("Set image type to :" + this.imageType);
        
    }
    
    
    /**
     *
     * Set the image quality
     *
     * @param imageType Type of image
     *
     */
    public void setImageQuality(int i) {
        if ((i <= 0) || (i > 100) ) {
            this.imageQuality = -1;
        }
        else {
            this.imageQuality = i;
        }
        
        KaboumUtil.debug("Set image quality to :" + this.imageQuality);
    }
    
    
    /**
     *
     * Set the current spatial extent
     *
     * @param e New extent
     *
     */
    public void setExtent(KaboumExtent e) {
        
        this.extent.set(this.getValidExtent(e));
        
        // Ajustement des nouvelles coordonnees
        setAdjustedCoordinates();
    }
    
    
    /**
     *

     * Layers initialisation
     *
     * @param str Layers list. The layers list is a string
     * in which each layer name is separated with a ","
     *
     */
    public void setLayersList(String str) {
        
        if (str != null) {
            
            StringTokenizer st = new StringTokenizer(str, ",");
            this.layersList = "";
            while (st.hasMoreTokens()) { this.layersList += "&layer="+st.nextToken(); }
        }
        else { this.layersList = ""; }
        
    }
    
    
    /**
     *
     * Query layers initialisation.
     *
     * @param str Query layer list. Query layers list is
     * a string in which each layer name is separated with
     * a ","
     *
     */
    public void setQueryLayersList(String str) {
        
        if(str != null) {
            StringTokenizer st = new StringTokenizer(str, ",");
            this.queryLayersList = "";
            while (st.hasMoreTokens()) { this.queryLayersList += "&layer="+st.nextToken(); }
        }
        else { this.queryLayersList = ""; }
    }
    
    
    /**
     *
     * Return the current spatial extent
     *
     */
    public KaboumExtent getRealExtent() {
        return this.realExtent;
    }
    
    
    /**
     *
     * Return the uncorrected spatial extent
     *
     */
    public KaboumExtent getExtent() {
        return this.extent;
    }
    
    
    /**
     *
     * Return the previous extent
     *
     */
    public KaboumExtent getPreviousExtent() {
        return this.previousExtent;
    }
    
    
    /**
     *
     * Return the mapserver URL string to draw the map
     *
     */
    public String getMapString() {
        String res = null;
        if (!this.useCache) {
            res = this.getUniqueMapString();
        }
        else {
            res = this.getPrivateMapString();
        }
        // now adds MS image dimension to the URL.
        res += "&mapsize=" + this.screenSize.width + "+" + this.screenSize.height;
        // adds any custom params to the MS URL
        res += this.customMsParams;
        return res;
    }
    
    
    /**
     *
     * Return a unique map String to mapserver by
     * appending a time stamp to the last parameter
     *
     */
    private String getUniqueMapString() {
        return this.getPrivateMapString()+"&timestamp="+System.currentTimeMillis();
    }
    
    
    /**
     *
     * Return mapserver URL string to draw the map
     * in case of item query mode, changes the url to match the good one
     *
     */
    private String getPrivateMapString() {
        if (displayItemQueryMap) {
            return mapserverURL +
            "?map=" + mapPath +
            "&mode="+ K_MODE_ITEM_NQUERYMAP +
             layersList+ 
            "&mapext="+this.extent.msString() +
            this.getItemQueryMapString() + this.getInlineFeatureString();
        }
        
        return mapserverURL+
        "?map="+mapPath+
        "&mode="+K_MODE_MAP+
        layersList+
        "&mapext="+this.extent.msString()+
        this.getImageTypeString()+
		this.getInlineFeatureString();
    }
    
    
    /**
     *
     * Return the image type string
     *
     */
    private String getImageTypeString() {
        if (K_TYPE_IMAGE_JPEG.equals(this.imageType)) {
            if (this.imageQuality != -1) {
                return "&map_imagetype=jpeg&map_imagequality=" + imageQuality;
            }
        }
        return "";
    }
    
   /**
    * Return the inline feature string
    */
    private String getInlineFeatureString() {
    	if (this.displayInline) {
    		return this.msInlineString;
    	}
    	return "";
    }

    /**
    * Return the item query map string
    */
    public String getItemQueryMapString() {
    	if (this.displayItemQueryMap) {
    		return this.msItemQueryMapString;
    	}
    	return "";
    }
   
   /**
    *
    * Set the inline feature string
    *
    */
    public void setInlineFeatureString(String layer, String points, String text) {
    	this.msInlineString = "&map_" + layer + "_feature_points=" + points + "&map_" + layer + "_feature_text=" + text;
    }
    
   /**
    *
    * Set the item query map string
    *@param mode: the mapserver mode, either itemquerymap or itemnquerymap, according to the received kaboumCommand string
    *@param layer: the name of the layer on which the attribute query is performed (currently, only one layer is supported.
    *@param qitem: an array of items (=layer's attribute) on which the search is performed
    *@param extraParams: a string containing extra mapserver parameters to pass for this query. theses
    *                    parameters are treated as they are passed, without any processing.<br>
    *                    this mechanism allows, for instance, to overload mapfile parameters.
    *@param qstring: the mapserver query string, pertinent to the current layer (postgis, dbf, etc.<br>
    * it is up to the user to generate a valid query string.
    */
    public void setItemQueryMapString(String mode, String layer, String[] qitem, String qstring, String extraParams) {
        this.msItemQueryMapString = "&qlayer=" + layer;
        
        if (qitem != null) {
            for (int i = 0; i < qitem.length; i++) {
                msItemQueryMapString += "&qitem=" + qitem[i];
            }
        }
        // Encodes the qstring parameter as it potentially contains characters not compatible with standard URLs
        // in Java 1.1, URLEncoder.encode(String, String) does not exist.
        qstring = "(" + qstring + ")";
        try {
            qstring = URLEncoder.encode(qstring);
        } catch (Exception e) {
        }
    	this.msItemQueryMapString += "&qstring=" + qstring + "&" + extraParams;
    }
    
    /**
     *
     *
     * Return the mapserver URL string to
     * query a layer
     *
     */
    public String getQueryString() {
        return  "&mapserverPath="+mapserverURL+
        "&map="+mapPath+
        "&mode="+K_MODE_NQUERY+
        queryLayersList+
        "&mapext="+this.extent.msString()+
        "&imgext="+this.extent.msString()+
        "&img.x="+img.x+
        "&img.y="+img.y;
    }
    
    
    /**
     *
     * Return the reference map URL string
     *
     */
    public String getReferenceString() {
        return mapserverURL+
        "?map="+mapPath+
        "&mode="+K_MODE_REFERENCE+
        "&mapext="+this.extent.msString();
    }
    
    
    /**
     *
     * Mapserver URL for printing
     *
     * @param w Width of the new map to print
     * @param h Height of the new map to print
     *
     */
    public String getPrintURL(int w, int h) {
        return mapserverURL+
        "?map="+mapPath+
        "&mode="+K_MODE_BROWSE+
        layersList+
        "&mapext="+this.extent.msString()+
        "&mapsize="+w+"+"+h;
    }
    
    
    /**
     *
     * Return the string equivalence of mouse position
     * in either map coordinates and mouse coordinates
     *
     * @param x Coordonnees x du pointeur souris
     * @param y Coordonnees y du pointeur souris
     *
     */
    public String getMapCoordString(int x, int y) {
        return this.pm.writeMapCoords(this.mouseXYToInternal(x,y));
    }
    
    
    /**
     *
     * Return the distance between two points
     *
     * @param coordA Coordinate A (internal representation)
     * @param coordB Coordinate B (internal representation)
     *
     */
    public double getDistance(KaboumCoordinate coordA, KaboumCoordinate coordB) {
        return this.pm.getDistance(coordA, coordB);
    }
    
    /**
     *
     * Return the area between 3 points.
     *
     * @param a - point one (internal representation)
     * @param b - point two (internal representation)
     * @param c - point three (internal representation)
     */
    public double getSurface(KaboumCoordinate a, KaboumCoordinate b, KaboumCoordinate c) {
        return this.pm.getSurface(a, b, c);
    }
    
    /**
     *
     * Return the current map scale.
     * Inferred from msCalculateScale function
     * in mapserver source code (cf: mapscale.c)
     *
     */
    public int getScale() {
        
        double md ,gd;
        
        if ((this.realExtent.xMin == this.realExtent.xMax) || (this.realExtent.yMin == this.realExtent.yMax)) {
            return -1;
        }
        md = (screenSize.width - 1) / ((double) this.pm.DPI *  this.pm.getMagicalNumber());
        gd = this.realExtent.xMax - this.realExtent.xMin;
        this.scale = (int) (gd/md);
        return scale;
    }
    
    
    /**
     *
     * Return the current map scale in pixel units, taken the current
     * panel witdh and map extent into account to compute the scale.
     * @return the scale computed from current extent
     */
    public double getPixelScale() {
        return Math.abs((this.realExtent.xMax - this.realExtent.xMin) / screenSize.width);
    }
    
    
    /**
     *
     * Set the map scale
     *
     */
    public double setScale(int scale) {
        
        if (scale == 0) {
            return 1;
        }
        double factor = (double) this.scale / (double) scale;
        this.scale = scale;
        return factor;
    }
    
    
    //************************************METHODES DIVERSES******************************
    
    /**
     *
     * Calculate map coordinate from a dragged box. If zoomOutRestriction
     * is set to true and the dragged box is outside the bounds, the
     * new box is set into the bounds and freeze to the closest one.
     *
     * @param x1 ULX mouse coordinate
     * @param y1 ULY mouse coordinate
     * @param x1 LRX mouse coordinate
     * @param y2 LRY mouse coordinate
     *
     */
    public void boxToMapExtent(double x1, double y1, double x2, double y2) {
        
        // Save the previous values of the map Extent
        this.previousExtent.set(this.extent);
        
        // Set the new values
        double x1Tmp = mouseXToInternal(x1);
        double x2Tmp = mouseXToInternal(x2);
        
        // Be carefull, y coordinate positive downward
        double y1Tmp = mouseYToInternal(y2);
        double y2Tmp = mouseYToInternal(y1);
        
        this.setExtent(new KaboumExtent(x1Tmp, y1Tmp, x2Tmp, y2Tmp));
        
    }
    
    
    /**
     *
     * Calculate map coordinates centered
     * on a mouse click
     *
     * @param x1 X mouse coordinate
     * @param y1 Y mouse coordinate
     * @param factor Zoom factor (Ex: 0.5 = zoom x-2; 1 = pan; 2 = zoom x2; etc...)
     *
     */
    public void zoomToMapExtent(double x1, double y1, double factor) {
        
        // Save the previous values of the map Extent
        this.previousExtent.set(this.extent);
        
        double tmpDeltaX = ((this.extent.dx()) / 2) / Math.abs(factor);
        double tmpDeltaY = ((this.extent.dy()) / 2) / Math.abs(factor);
        double x1Tmp = mouseXToInternal(x1) - tmpDeltaX;
        double x2Tmp = mouseXToInternal(x1) + tmpDeltaX;
        double y1Tmp = mouseYToInternal(y1) - tmpDeltaY;
        double y2Tmp = mouseYToInternal(y1) + tmpDeltaY;
        
        
        this.setExtent(new KaboumExtent(x1Tmp, y1Tmp, x2Tmp, y2Tmp));
        
    }
    
    
    /**
     *
     * Calculate new extent center on a map coordinates
     *
     * @param internal   Internal coordinate
     *
     */
    public void centerMapExtent(KaboumCoordinate internal) {
        
        // Save the previous values of the map Extent
        this.previousExtent.set(this.extent);
        
        double tmpDeltaX = (this.extent.dx()) / 2.0;
        double tmpDeltaY = (this.extent.dy()) / 2.0;
        double x1Tmp = internal.x - tmpDeltaX;
        double y1Tmp = internal.y - tmpDeltaY;
        double x2Tmp = internal.x + tmpDeltaX;
        double y2Tmp = internal.y + tmpDeltaY;
        
        this.setExtent(new KaboumExtent(x1Tmp, y1Tmp, x2Tmp, y2Tmp));
    }
    
    
    /**
     *
     * Return a valid extent depending on zoomOutRestriction
     * status and maxScale restriction.
     *
     * @param e Extent
     *
     */
    public KaboumExtent getValidExtent(KaboumExtent e) {
        
        if (restrictedExtent != null) {
            
            // New extent is not valid if it contains entirely the reference extent
            if ((e.contains(this.restrictedExtent)) ||
            (e.dx() > this.restrictedExtent.dx()) ||
            (e.dy() > this.restrictedExtent.dy())) {
                return this.restrictedExtent;
            }
            
            if (e.xMin < this.restrictedExtent.xMin) {
                e.xMin = this.restrictedExtent.xMin;
                if (this.extent.dx() > e.dx()) {
                    e.xMax = this.restrictedExtent.xMin + this.extent.xMax - this.extent.xMin;
                }
                else {
                    e.xMax = this.restrictedExtent.xMin + e.xMax - e.xMin;
                }
            }
            if (e.xMax > this.restrictedExtent.xMax) {
                if (this.extent.dx() > e.dx()) {
                    e.xMin = this.restrictedExtent.xMax - this.extent.xMax + this.extent.xMin;
                }
                else {
                    e.xMin = this.restrictedExtent.xMax - e.xMax + e.xMin;
                }
                e.xMax = this.restrictedExtent.xMax;
            }
            if (e.yMax < this.restrictedExtent.yMin) {
                e.yMin = this.restrictedExtent.yMin;
                if (this.extent.dy() > e.dy()) {
                    e.yMax = this.restrictedExtent.yMin + this.extent.yMax - this.extent.yMin;
                }
                else {
                    e.yMax = this.restrictedExtent.yMin + e.yMax - e.yMin;
                }
            }
            if (e.yMax > this.restrictedExtent.yMax) {
                if (this.extent.dy() > e.dy()) {
                    e.yMin = this.restrictedExtent.yMax - this.extent.yMax + this.extent.yMin;
                }
                else {
                    e.yMin = this.restrictedExtent.yMax - e.yMax + e.yMin;
                }
                e.yMax = this.restrictedExtent.yMax;
            }
        }
        
        // Maxscale is set
        if (this.maxScale > 0) {
            
            if ((e.xMin == e.xMax) || (e.yMin == e.yMax)) {
                return e;
            }
            
            int innerscale = (int) ( (e.xMax - e.xMin) / ((screenSize.width - 1) / ((double) this.pm.DPI *  this.pm.getMagicalNumber())));
            if (innerscale < this.maxScale) {
                e.set(this.extent);
            }
        }
        
        return e;
    }
    
    
    /**
     *
     * Compute the spatial extent containing the whole image
     *
     */
    public void setAdjustedCoordinates() {
        
        double cellsize, ox, oy;
        
        cellsize = Math.max((this.extent.dx()) / (screenSize.width - 1), (this.extent.dy()) / (screenSize.height - 1));
        
        if(cellsize <= 0) {
            KaboumUtil.debug(" Error : Invalid coordinates adjustment!");
            return;
        }
        
        ox = ms_NINT( Math.max(((screenSize.width - 1) - (this.extent.dx()) / cellsize) / 2.0, 0.0));
        oy = ms_NINT( Math.max(((screenSize.height - 1) - (this.extent.dy()) / cellsize) / 2.0, 0.0));
        
        this.realExtent.xMin = this.extent.xMin - ox * cellsize;
        this.realExtent.yMin = this.extent.yMin - oy * cellsize;
        this.realExtent.xMax = this.extent.xMax + ox * cellsize;
        this.realExtent.yMax = this.extent.yMax + oy * cellsize;
        
        // Propagate the extent into the objectList class if exists
        if (this.GGDIndex != null) {
            this.GGDIndex.setExtent(this.realExtent);
        }
        
    }
    
    
    /**
     *
     * This method is rewritten from MS_NINT
     * definition in map.h file of the mapserver distribution
     * (cf. http://mapserver.gis.umn.edu)
     *
     */
    private double ms_NINT(double x) {
        if (x >= 0.0)
            return x + 0.5;
        else
            return x - 0.5;
    }
    
    
    /**
     *
     * Add a new extent in history array
     *
     */
    public void historyAddExtent(String extent) {
        if (historyIsLastCall) {
            historyIsLastCall = false;
        }
        else {
            if (historyArray.size() < this.historyMaxSize) {
                for (int i = historyArray.size() - 1;i>historyIndex;i--) {
                    if (historyArray.elementAt(i) != null) {
                        historyArray.removeElementAt(i);
                    }
                }
                historyArray.addElement(extent);
                historyIndex++;
            }
            else {
                historyArray.removeElementAt(0);
                historyArray.addElement(extent);
            }
        }
    }
    
    
    /**
     *
     * Return the previous extent in history
     *
     */
    public String historyPrevExtent() {
        if (historyIndex > 0) {
            if (historyIndex == historyArray.size()) {
                historyIndex = historyIndex - 2;
            }
            else {
                historyIndex--;
            }
            historyIsLastCall = true;
            return (String) historyArray.elementAt(historyIndex);
        }
        else {
            historyIndex = 0;
            return null;
        }
    }
    
    
    /**
     *
     * Return the next extent in history
     *
     */
    public String historyNextExtent() {
        if (historyIndex < historyArray.size()) {
            historyIndex++;
            historyIsLastCall = true;
            return (String) historyArray.elementAt(historyIndex);
        }
        else {
            historyIndex = historyArray.size();
            return null;
        }
    }
    /** returns the custom MapServer parameters string set by Kaboum */
    public String getCustomMsParams() {
        return customMsParams;
    }

    /** sets the custom MS parameters to add at the end of the URL, to allow
     * passing params to Mapserver
     * @param customMsParams
     */
    public void setCustomMsParams(String customMsParams) {
        this.customMsParams = customMsParams;
    }


    /** Returns the screen size */
    public Dimension getScreenSize() {
        return screenSize;
    }

    /** sets a new screenSize */
    public void setScreenSize(Dimension screenSize) {
        this.screenSize = screenSize;
    }

}

