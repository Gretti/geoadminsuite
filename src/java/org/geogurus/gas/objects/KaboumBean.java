/*
 * KaboumBean.java
 *
 * Created on 19 janvier 2007, 23:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geogurus.gas.objects;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;
import org.geogurus.GeometryClass;
import org.geogurus.KaboumProperties;
import org.geogurus.tools.DataManager;

/**
 * A bean representing a Kaboum Applet, with its parameters and some methods
 * to represent kaboum (applet HTML parameters) or initialize it (init parameters).<br />
 * it is worth reading the Kaboum Documentation to understand kaboum parameters
 *
 * @author Administrateur
 */
public class KaboumBean implements Serializable {
    
    /** The kaboum map unit, got from current mapfile */
    private String kaboumMapUnits;
    /** The KaboumProperties object, used to write kaboum applet tag in JSP */
    private KaboumProperties kaboumProperties;
    /** The kaboum Applet tag string */
    private String kaboumHtmlRepresentation;
    /** The kaboum applet closing tag */
    private String kaboumCloseTag;
    /** The kaboum applet list of classes */
    private String kaboumClassList;
    /** the JS code used to initialize Kaboum parameters. No setter for this property */
    private String initParamsJs;
    /** the name of the kaboum object inside the JS context (name of the applet object) */
    private String kaboumRefName;
    
    //////// Kaboum INIT Properties see Kaboum doc. //////////////
    /** kaboum Busy Image URL */
    private String kaboumBusyImgUrl;
    private String kaboumMapserverCgiUrl;
    private String kaboumBgColor;
    private String kaboumFontName;
    private int kaboumFontSize;
    private String kaboumFontStyle;
    private int kaboumHistorySize;
    private int kaboumImageQuality;
    private String kaboumImageType;
    private boolean kaboumUseImageCaching;
    private String kaboumLang;
    private double kaboumMaxScale;
    private String kaboumMaxExtent;
    private String kaboumDefaultOpMode;
    private String kaboumOpModeList;
    private boolean kaboumRefMapIsApplet;
    private boolean kaboumSendPositionCoordToJs;
    private double kaboumSurfacePrecision;
    private String kaboumSurfaceUnits;
    private boolean kaboumUseLiveConnect;
    private boolean kaboumUseTooltip;
    private boolean kaboumDebugMode;
    private boolean kaboumShowAppletNameInResult;
    private String kaboumDistanceFgColor;
    private String kaboumDistancePointType;
    private String mapfileUnits;
    private int selectionPixelPrecision;
    private boolean selectionAutoCommit;
    
    /////// List of values for some parameters //////////////
    public String[] fontNameList = {"Courier", "Dialog", "Helvetica", "Symbol", "TimesRoman"};
    public String[] fontStyleList = {"plain", "bold", "italic", "boldItalic"};
    public String[] imgTypeList = {"JPEG", "GIF"};
    public String[] langList = {"FR", "EN"};
    public String[] unitsList = {"MS_SQUARE_METERS", "MS_ARES", "MS_DECARES","MS_HECTARES"};
    public String[] typeList = {"K_TYPE_BOX", "K_TYPE_POINT", "K_TYPE_CIRCLE", "K_TYPE_IMAGE"};
    
    /** Creates a new instance of KaboumBean */
    public KaboumBean() {
        super();
        kaboumRefName = "self.opener.document.kaboum";
        kaboumBgColor = "255,0,0";
        kaboumFontName = fontNameList[0];
        kaboumFontSize = 12;
        kaboumFontStyle = fontStyleList[0];
        kaboumHistorySize = 5;
        kaboumImageQuality = 100;
        kaboumImageType = "GIF";
        kaboumUseImageCaching = true;
        kaboumLang = langList[0];
        kaboumMaxScale = -1;
        kaboumDefaultOpMode = "ZOOMIN";
        kaboumRefMapIsApplet = false;
        kaboumSendPositionCoordToJs = false;
        kaboumSurfacePrecision = 1;
        kaboumSurfaceUnits = unitsList[0];
        kaboumUseLiveConnect = true;
        kaboumUseTooltip = false;
        kaboumDebugMode = false;
        kaboumShowAppletNameInResult = false;
        kaboumDistanceFgColor = "GREEN";
        kaboumDistancePointType = "K_TYPE_BOX";
        System.out.println("bean built");
    }
    /**
     * Returns a String containing applet param for kaboum to declare object's class list:
     * OBJECT_CLASS_LIST, DD_CLASS_LIST, PROPERTIES_CLASS_LIST
     */
    public String getKaboumClassList() {return this.kaboumClassList;}
    public String getKaboumMapUnits() {return kaboumMapUnits;}
    public String getKaboumHtmlRepresentation() {return kaboumHtmlRepresentation;}
    public String getKaboumCloseTag() {return kaboumCloseTag;}
    public KaboumProperties getKaboumProperties() {return kaboumProperties;}
    public String getKaboumBusyImgUrl() {return this.kaboumBusyImgUrl;}
    public String getKaboumRefName() {return kaboumRefName;}
    
    public String[] getImgTypeList() { return imgTypeList; }
    public String[] getFontNameList() { return fontNameList; }
    public String[] getFontStyleList() { return fontStyleList; }
    public String[] getLangList() { return langList; }
    public String[] getUnitsList() { return unitsList; }
    public String[] getTypeList() { return typeList; }
    
    public String getKaboumMapserverCgiUrl() { return kaboumMapserverCgiUrl;}
    public String getKaboumBgColor() {return kaboumBgColor;}
    public String getKaboumFontName() {return kaboumFontName;}
    public int getKaboumFontSize() {return kaboumFontSize;}
    public String getKaboumFontStyle() {return kaboumFontStyle;}
    public int getKaboumHistorySize() {return kaboumHistorySize;}
    public int getKaboumImageQuality() {return kaboumImageQuality;}
    public String getKaboumImageType() {return kaboumImageType;}
    public boolean isKaboumUseImageCaching() {return kaboumUseImageCaching;}
    public String getKaboumLang() {return kaboumLang;}
    public double getKaboumMaxScale() {return kaboumMaxScale;}
    public String getKaboumMaxExtent() {return kaboumMaxExtent;}
    public String getKaboumDefaultOpMode() {return kaboumDefaultOpMode;}
    public String getKaboumOpModeList() {return kaboumOpModeList;}
    public boolean isKaboumRefMapIsApplet() {return kaboumRefMapIsApplet;}
    public boolean isKaboumSendPositionCoordToJs() {return kaboumSendPositionCoordToJs;}
    public double getKaboumSurfacePrecision() {return kaboumSurfacePrecision;}
    public String getKaboumSurfaceUnits() {return kaboumSurfaceUnits;}
    public boolean isKaboumUseLiveConnect() {return kaboumUseLiveConnect;}
    public boolean isKaboumUseTooltip() {return kaboumUseTooltip;}
    public boolean isKaboumDebugMode() {return kaboumDebugMode;}
    public boolean getKaboumDebugMode() {return kaboumDebugMode;}
    public boolean isKaboumShowAppletNameInResult() {return kaboumShowAppletNameInResult;}
    public String getKaboumDistanceFgColor() { return kaboumDistanceFgColor; }
    public String getKaboumDistancePointType() { return kaboumDistancePointType; }
    public String getMafileUnits() { return mapfileUnits; }
    public boolean isSelectionAutoCommit() { return selectionAutoCommit; }
    public int getSelectionPixelPrecision() { return selectionPixelPrecision; }
    /** returns the Javascript commands to initialize kaboum parameters, after it was loaded
     * (kaboumCommand(INIT_PARAMS|<name=value>)
     * @param kaboumRef: the name of the Javascript kaboum applet object. usefull to pass
     * a name from a different page, like parent.opener.document.kaboum. Default to document.kaboum
     * if a null value is passed
     * @return a String containing Javascript Commands to initialize kaboum parameters
     */
    public String getInitParamsJs() {
        String kr = kaboumRefName + ".kaboumCommand(";
        
        StringBuffer res = new StringBuffer();
        if (kaboumBusyImgUrl != null) {
            res.append(kr).append("'INIT_PARAM|KABOUM_BUSY_IMAGE_URL=").append(kaboumBusyImgUrl).append("');\n");
        }
        res.append(kr).append("'INIT_PARAM|KABOUM_BACKGROUND_COLOR=").append(kaboumBgColor).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_FONT_NAME=").append(kaboumFontName).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_FONT_SIZE=").append(kaboumFontSize).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_FONT_STYLE=").append(kaboumFontStyle).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_HISTORY_SIZE=").append(kaboumHistorySize).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_IMAGE_QUALITY=").append(kaboumImageQuality).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_IMAGE_TYPE=").append(kaboumImageType).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_USE_IMAGE_CACHING=").append(kaboumUseImageCaching).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_LANG=").append(kaboumLang).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_MAXIMUM_SCALE=").append(kaboumMaxScale).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_MAXIMUM_EXTENT=").append(kaboumMaxExtent).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_DEFAULT_OPMODE=").append(kaboumDefaultOpMode).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_OPMODES_LIST=").append(kaboumOpModeList).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_REFERENCE_IS_APPLET=").append(kaboumRefMapIsApplet).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_SEND_POSITION_COORDINATES_TO_JS=").append(kaboumSendPositionCoordToJs).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_SURFACE_PRECISION=").append(kaboumSurfacePrecision).append("');\n");
        res.append(kr).append("'INIT_PARAM|SURFACE_UNITS=").append(kaboumSurfaceUnits).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_USE_LIVECONNECT=").append(kaboumUseLiveConnect).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_USE_TOOLTIP=").append(kaboumUseTooltip).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_DEBUG_MODE=").append(kaboumDebugMode).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_SHOW_APPLET_NAME_IN_RESULT=").append(kaboumShowAppletNameInResult).append("');\n");
        res.append(kr).append("'INIT_PARAM|DISTANCE_FOREGROUND_COLOR=").append(kaboumDistanceFgColor).append("');\n");
        res.append(kr).append("'INIT_PARAM|DISTANCE_POINT_TYPE=").append(kaboumDistancePointType).append("');\n");
        res.append(kr).append("'INIT_PARAM|KABOUM_MAPSERVER_CGI_URL=").append(kaboumMapserverCgiUrl).append("');\n");
        res.append(kr).append("'INIT_PARAM|MAPFILE_UNITS=").append(mapfileUnits).append("');\n");
        res.append(kr).append("'INIT_PARAM|SELECTION_PIXEL_PRECISION=").append(selectionPixelPrecision).append("');\n");
        res.append(kr).append("'INIT_PARAM|SELECTION_AUTO_COMMIT=").append(selectionAutoCommit).append("');\n");

        return res.toString();
    }
    
    public void setKaboumProperties(KaboumProperties kp_) {this.kaboumProperties = kp_;}
    public void setKaboumHtmlRepresentation(String kaboumHtmlRepresentation_) {this.kaboumHtmlRepresentation = kaboumHtmlRepresentation_;}
    public void setKaboumCloseTag(String kaboumCloseTag_) {this.kaboumCloseTag = kaboumCloseTag_;}
    public void setKaboumBusyImgUrl(String busy) {this.kaboumBusyImgUrl = busy;}
    public void setKaboumRefName(String name) {kaboumRefName = name;}
    
    public void setKaboumMapserverCgiUrl(String msURL) { kaboumMapserverCgiUrl = msURL;}
    public void setKaboumBgColor(String bgColor) {kaboumBgColor = bgColor;}
    public void setKaboumFontName(String fontName) {kaboumFontName = fontName;}
    public void setKaboumFontSize(int fontSize) {kaboumFontSize = fontSize;}
    public void setKaboumFontStyle(String fontStyle) {kaboumFontStyle = fontStyle;}
    public void setKaboumHistorySize(int histSize) {kaboumHistorySize = histSize;}
    public void setKaboumImageQuality(int imgQuality) {kaboumImageQuality = imgQuality;}
    public void setKaboumImageType(String imgType) {kaboumImageType = imgType;}
    public void setKaboumUseImageCaching(boolean useCaching) {kaboumUseImageCaching = useCaching;}
    public void setKaboumLang(String lang) {kaboumLang = lang;}
    public void setKaboumMaxScale(double maxScale) {kaboumMaxScale = maxScale;}
    public void setKaboumMaxExtent(String maxExtent) {kaboumMaxExtent = maxExtent;}
    public void setKaboumDefaultOpMode(String defOpMode) {kaboumDefaultOpMode = defOpMode;}
    public void setKaboumOpModeList(String opModeList) {kaboumOpModeList = opModeList;}
    public void setKaboumRefMapIsApplet(boolean refIsApplet) {kaboumRefMapIsApplet = refIsApplet;}
    public void setKaboumSendPositionCoordToJs(boolean sendToJS) {kaboumSendPositionCoordToJs = sendToJS;}
    public void setKaboumSurfacePrecision(double surfPrecision) {kaboumSurfacePrecision = surfPrecision;}
    public void setKaboumSurfaceUnits(String surfUnits) {kaboumSurfaceUnits = surfUnits;}
    public void setKaboumUseLiveConnect(boolean useLiveConnect) {kaboumUseLiveConnect = useLiveConnect;}
    public void setKaboumUseTooltip(boolean useTooltip) {kaboumUseTooltip = useTooltip;}
    public void setKaboumDebugMode(boolean debugMode) {
        System.out.println("set debugmode called with value: " + debugMode);
        kaboumDebugMode = debugMode;
    }
    public void setKaboumShowAppletNameInResult(boolean showAppletName) {kaboumShowAppletNameInResult = showAppletName;}    
    public void setKaboumDistanceFgColor(String color) { kaboumDistanceFgColor = color; }
    public void setKaboumDistancePointType(String ptType) { kaboumDistancePointType = ptType; }
    public void setMapfileUnits(String mfUnits) { mapfileUnits = mfUnits; }
    public void setSelectionPixelPrecision(int pixelPrec) { selectionPixelPrecision = pixelPrec; }
    public void setSelectionAutoCommit(boolean autocommit) { selectionAutoCommit = autocommit; }

    /** Load and sets kaboum applet properties according to properties files and UserMapBean parameters
     */
    public void loadKaboumProperties(UserMapBean userMapBean_) {
        /*
        //TODO Pourquoi on le charge a partir d'un fichier plutot que directement a partir du ressourceAsStream ?
        String kpf = rootPath + File.separator + "WEB-INF" + File.separator + "classes" + File.separator + "resources" + File.separator + "kaboum.properties";
         
        kaboumProperties = new KaboumProperties();
        kaboumProperties.loadFromFilePath(kpf);
         */
        try {
            KaboumProperties kaboumProperties = new KaboumProperties();
            kaboumProperties.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/kaboum.properties"));
            kaboumProperties.setProperty("width",new Integer(userMapBean_.getImgX()).toString());
            kaboumProperties.setProperty("height",new Integer(userMapBean_.getImgY()).toString());
            kaboumProperties.setProperty("KABOUM_MAPSERVER_CGI_URL",userMapBean_.getMapserverURL());
            kaboumProperties.setProperty("MAPFILE_PATH",URLDecoder.decode(userMapBean_.getMapfilePath(),"UTF-8"));
            kaboumProperties.setProperty("MAPFILE_EXTENT",userMapBean_.getMapExtent());
            kaboumProperties.setProperty("KABOUM_BUSY_IMAGE_URL",DataManager.getProperty("WAITIMAGE"));
            
            setKaboumProperties(kaboumProperties);
            setKaboumHtmlRepresentation(kaboumProperties.getAppletHtmlRepresentation());
            setKaboumCloseTag(kaboumProperties.closeAppletTag());
            
            // update bean properties
            setKaboumMapserverCgiUrl(kaboumProperties.getProperty("KABOUM_MAPSERVER_CGI_URL"));
            setKaboumBusyImgUrl(kaboumProperties.getProperty("KABOUM_BUSY_IMAGE_URL"));
            setKaboumUseLiveConnect(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_USE_LIVECONNECT")));
            setKaboumRefMapIsApplet(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_REFERENCE_IS_APPLET")));
            setKaboumDebugMode(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_DEBUG_MODE")));
            setKaboumBgColor(kaboumProperties.getProperty("KABOUM_BACKGROUND_COLOR"));
            setKaboumLang(kaboumProperties.getProperty("KABOUM_LANG"));
            setKaboumDefaultOpMode(kaboumProperties.getProperty("KABOUM_DEFAULT_OPMODE"));
            setKaboumOpModeList(kaboumProperties.getProperty("KABOUM_OPMODES_LIST"));
            setKaboumSendPositionCoordToJs(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_SEND_POSITION_COORDINATES_TO_JS")));
            setKaboumSurfaceUnits(kaboumProperties.getProperty("SURFACE_UNITS"));
            setKaboumDistanceFgColor(kaboumProperties.getProperty("DISTANCE_FOREGROUND_COLOR"));
            setKaboumDistancePointType(kaboumProperties.getProperty("DISTANCE_POINT_TYPE"));
            setKaboumFontName(kaboumProperties.getProperty("KABOUM_FONT_NAME"));
            setKaboumFontSize(Integer.parseInt(kaboumProperties.getProperty("KABOUM_FONT_SIZE")));
            setKaboumFontStyle(kaboumProperties.getProperty("KABOUM_FONT_STYLE"));
            setKaboumMaxScale(Double.parseDouble(kaboumProperties.getProperty("KABOUM_MAXIMUM_SCALE")));
            setKaboumMaxExtent(kaboumProperties.getProperty("KABOUM_MAXIMUM_EXTENT"));
            setKaboumSendPositionCoordToJs(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_SEND_POSITION_COORDINATES_TO_JS")));
            setMapfileUnits(kaboumProperties.getProperty("MAPFILE_UNITS"));
            setSelectionPixelPrecision(Integer.parseInt(kaboumProperties.getProperty("SELECTION_PIXEL_PRECISION")));
            setSelectionAutoCommit(Boolean.parseBoolean(kaboumProperties.getProperty("SELECTION_AUTO_COMMIT")));
            setKaboumUseTooltip(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_USE_TOOLTIP")));
            setKaboumImageQuality(Integer.parseInt(kaboumProperties.getProperty("KABOUM_IMAGE_QUALITY")));
            setKaboumHistorySize(Integer.parseInt(kaboumProperties.getProperty("KABOUM_HISTORY_SIZE")));
            setKaboumImageType(kaboumProperties.getProperty("KABOUM_IMAGE_TYPE"));
            setKaboumSurfacePrecision(Integer.parseInt(kaboumProperties.getProperty("KABOUM_SURFACE_PRECISION")));
            setKaboumUseImageCaching(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_USE_IMAGE_CACHING")));
            setKaboumShowAppletNameInResult(Boolean.parseBoolean(kaboumProperties.getProperty("KABOUM_SHOW_APPLET_NAME_IN_RESULT")));

            System.out.println("properties loaded...");
            
       } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
     /**
     * Builds the String containing applet param for kaboum to declare object's class list:
     * OBJECT_CLASS_LIST, DD_CLASS_LIST, PROPERTIES_CLASS_LIST
     */
    public void setKaboumClassList(Hashtable userLayerList_) {
        StringBuffer res = new StringBuffer();
        StringBuffer list = new StringBuffer();
        
        GeometryClass gc = null;
        
        for (Iterator iter = userLayerList_.values().iterator(); iter.hasNext();) {
            Object obj = iter.next();
            
            if (obj instanceof GeometryClass) {
                gc = (GeometryClass)obj;
                if (gc.displayInMapserver) {
                    list.append(gc.getID());
                    if (iter.hasNext()) {
                        list.append(",");
                    }
                }
            }
        }
        res.append("<param name=\"OBJECT_CLASS_LIST\" value=\"").append(list.toString()).append("\">\n");
        res.append("<param name=\"DD_CLASS_LIST\" value=\"").append(list.toString()).append("\">\n");
        res.append("<param name=\"PROPERTIES_CLASS_LIST\" value=\"").append(list.toString()).append("\">\n");
        
        this.kaboumClassList = res.toString();
    }
    
}
