/*
 * Map.java
 *
 * Created on 20 mars 2002, 09:52
 */
package org.geogurus.mapserver.objects;

import java.io.File;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Vector;
import org.geogurus.tools.string.ConversionUtilities;
import org.geogurus.mapserver.tools.MapTools;

/**
 * Defines the master object of the MapFile, that is the object
 * that holds all other objects (i.e. the "root").
 * It defines application/map wide parameters.
 *
 * @author  Bastien VIALADE
 */
public class Map extends MapServerObject implements java.io.Serializable {
    // Constants for Image type
    public static final byte GIF = 0;
    public static final byte PNG = 1;
    public static final byte JPEG = 2;
    public static final byte WBMP = 3;
    // Constants for interlace transparence and status
    public static final byte ON = 0;
    public static final byte OFF = 1;
    // Constants defines units
    public static final byte DD = 0;
    public static final byte FEET = 1;
    public static final byte INCHES = 2;
    public static final byte KILOMETERS = 3;
    public static final byte METERS = 4;
    public static final byte MILES = 5;
    // Constants defining layer order
    public static final byte TOP = 0;
    public static final byte BOTTOM = 1;
    /** The spatial extent of the map to be created.
     * Most often you will want to specify this,
     * although mapserver will extrapolate one if none is specified. */
    private MSExtent extent;
    /** Full filename of fontset file to use. */
    private File fontSet;
    /** Color to initialize the map with (i.e. background color). */
    private RGB imageColor;
    /** Compression quality for JPEG output. */
    private int imageQuality;
    /** Output image type.
     * This is dependent on how the MapServer executable was compiled and the GD used. */
    private byte imageType;
    /** Should output images be interlaced? */
    private byte interlace;
    /** Signals the start of a LAYER object. */
    private Vector layers;
    /** Signals the start of a LEGEND object. */
    private Legend legend;
    /** Prefix attached to map, scalebar and legend GIF filenames created using this MapFile.
     * It should be kept short. */
    private String name;
    /** Signals the start of a PROJECTION object.*/
    private Projection projection;
    /** Signals the start of a QUERYMAP object. */
    private QueryMap queryMap;
    /** Signals the start of a REFERENCE MAP object. */
    private ReferenceMap referenceMap;
    /** Sets the pixels per inch for output,
     * only affects scale computations and nothing else, default is 72 */
    private int resolution;
    /** Computed scale of the map. Set most often by the application */
    private double scale;
    /** Signals the start of a SCALEBAR object. */
    private ScaleBar scaleBar;
    /** Path to the directory holding the shapefiles or tiles.
     * There can be further subdirectories under SHAPEPATH. */
    private File shapePath;
    /** Size in pixels of the output image (i.e. the map). */
    private Dimension size;
    /** Is the map active?
     * Sometimes you may wish to turn this off to use only the reference map
     * or scale bar. */
    private byte status;
    /** Full filename of the symbolset to use. */
    private SymbolSet symbolSet;
    /** Signals the start of a SYMBOL object.*/
    private ArrayList symbols;
    /** Should the background color for the maps be transparent.
     * Default is off. */
    private byte transparent;
    /** Units of the map coordinates.
     * Used for scalebar and scale computations. */
    private byte units;
    /** Signals the start of a WEB object. */
    private Web web;
    /** the file in which this object was written
     * Set for convenience.
     */
    private File mapFile;
    /** a unique identifier for this object */
    private String id = null;

    /** Empty constructor */
    public Map() {
        this(new MSExtent(0.0, 0.0, 0.0, 0.0),
                null,
                new RGB(255, 255, 255),
                0,
                Map.GIF,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                new Dimension(450, 450),
                null,
                Map.METERS,
                null);
        mapFile = null;
    }

    /** Empty constructor */
    public Map(org.geogurus.mapserver.MapFile f) {
        this(new MSExtent(0.0, 0.0, 0.0, 0.0),
                null,
                new RGB(255, 255, 255),
                0,
                Map.GIF,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                new Dimension(450, 450),
                null,
                Map.METERS,
                null);
        mapFile = f;
    }

    /** Creates a new instance of Map */
    public Map(MSExtent extent_, File fontSet_, RGB imageColor_, int imageQuality_, byte imageType_,
            Legend legend_, String name_, Projection projection_, QueryMap queryMap_,
            ReferenceMap referenceMap_, double scale_, ScaleBar scaleBar_, File shapePath_, Dimension size_,
            SymbolSet symbolSet_, byte units_, Web web_) {
        id = "" + System.identityHashCode(this);
        extent = extent_;
        fontSet = fontSet_;
        imageColor = imageColor_;
        imageQuality = imageQuality_;
        imageType = imageType_;
        interlace = Map.ON;
        layers = null;
        legend = legend_;
        name = name_;
        projection = projection_;
        queryMap = queryMap_;
        referenceMap = referenceMap_;
        resolution = 72;
        scale = scale_;
        scaleBar = scaleBar_;
        shapePath = shapePath_;
        size = size_;
        status = Map.ON;
        symbolSet = symbolSet_;
        symbols = new ArrayList();
        transparent = Map.OFF;
        units = units_;
        web = web_;
    }

    // Set and get methods
    public void setExtent(MSExtent extent_) {
        extent = extent_;
    }

    public void setFontSet(File fontSet_) {
        fontSet = fontSet_;
    }

    public void setImageColor(RGB imageColor_) {
        imageColor = imageColor_;
    }

    public void setImageQuality(int imageQuality_) {
        imageQuality = imageQuality_;
    }

    public void setImageType(byte imageType_) {
        imageType = imageType_;
    }

    public void setInterlace(byte interlace_) {
        interlace = interlace_;
    }

    public void setLayers(Vector layers_) {
        layers = layers_;
    }

    public void setLegend(Legend legend_) {
        legend = legend_;
    }

    public void setName(String name_) {
        name = name_;
    }

    public void setProjection(Projection projection_) {
        projection = projection_;
    }

    public void setQueryMap(QueryMap queryMap_) {
        queryMap = queryMap_;
    }

    public void setReferenceMap(ReferenceMap referenceMap_) {
        referenceMap = referenceMap_;
    }

    public void setResolution(int resolution_) {
        resolution = resolution_;
    }

    public void setScale(double scale_) {
        scale = scale_;
    }

    public void setScaleBar(ScaleBar scaleBar_) {
        scaleBar = scaleBar_;
    }

    public void setShapePath(File shapePath_) {
        shapePath = shapePath_;
    }

    public void setSize(Dimension size_) {
        size = size_;
    }

    public void setStatus(byte status_) {
        status = status_;
    }

    public void setSymbolSet(SymbolSet symbolSet_) {
        symbolSet = symbolSet_;
    }

    public void setSymbols(ArrayList symbols_) {
        symbols = symbols_;
    }

    public void setTransparent(byte transparent_) {
        transparent = transparent_;
    }

    public void setUnits(byte units_) {
        units = units_;
    }

    public void setWeb(Web web_) {
        web = web_;
    }

    public void setMapFile(File mapFile_) {
        mapFile = mapFile_;
    }

    public void addSymbol(Symbol symbol) {
        if (symbols == null) {
            symbols = new ArrayList();
        }
        symbols.add(symbol);
    }

    /**
     * Adds the given layer at the end of the Layer vector
     */
    public void addLayer(Layer layer) {
        if (layers == null) {
            layers = new Vector();
        }
        layers.add(layer);
    }

    /**
     * Removes the given layer from the vector of layer
     * Vector of layers will be seached for layers having same:
     * name, type, connectionType as the given layer
     */
    public void removeLayer(Layer layer) {
        if (layer == null) {
            return;
        }

        Layer l = null;

        for (int i = 0; i < layers.size(); i++) {
            l = (Layer) layers.get(i);

            if (l.getName().equals(layer.getName())) {
                layers.remove(i);
                break;
            }
        }
    }

    /** adds the given layer in the corresponding order.
     * for the moment, only top or bottom is supported
     */
    public void addLayer(Layer layer, byte order) {
        if (layers == null) {
            layers = new Vector();
        }

        if (order == Map.TOP) {
            layers.add(layer);
        } else {
            layers.insertElementAt(layer, 0);
        }
    }

    /** inserts the given layer at the given index in the layers Vector
     * @param layer the <code>Layer</code> to insert
     * @param order the index position to insert at
     * @return true if the given layer was correctly inserted, false otherwise:
     * idx < 0 or idx >= vector.size()
     */
    public boolean addLayer(Layer layer, int idx) {
        if (layers == null) {
            layers = new Vector();
        }

        if (idx < 0 || idx > layers.size() - 1) {
            return false;
        }
        layers.insertElementAt(layer, idx);
        return true;
    }

    public String getID() {
        return id;
    }

    public MSExtent getExtent() {
        return extent;
    }

    public File getFontSet() {
        return fontSet;
    }

    public RGB getImageColor() {
        return imageColor;
    }

    public int getImageQuality() {
        return imageQuality;
    }

    public byte getImageType() {
        return imageType;
    }

    public byte getInterlace() {
        return interlace;
    }

    public Vector getLayers() {
        return layers;
    }

    public Legend getLegend() {
        return legend;
    }

    public String getName() {
        return name;
    }

    public Projection getProjection() {
        return projection;
    }

    public QueryMap getQueryMap() {
        return queryMap;
    }

    public ReferenceMap getReferenceMap() {
        return referenceMap;
    }

    public int getResolution() {
        return resolution;
    }

    public double getScale() {
        return scale;
    }

    public ScaleBar getScaleBar() {
        return scaleBar;
    }

    public File getShapePath() {
        return shapePath;
    }

    public Dimension getSize() {
        return size;
    }

    public byte getStatus() {
        return status;
    }

    public SymbolSet getSymbolSet() {
        return symbolSet;
    }

    public ArrayList getSymbols() {
        return symbols;
    }

    public byte getTransparent() {
        return transparent;
    }

    public byte getUnits() {
        return units;
    }

    public Web getWeb() {
        return web;
    }

    public File getMapFile() {
        return mapFile;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        boolean result = true;
        boolean isSize = false;

        try {
            String[] tokens;
            String line;
//FIXME: should return error codes instead of false
            while ((line = br.readLine()) != null) {
                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }

                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("EXTENT")) {
                    extent = new MSExtent();
                    result = extent.load(tokens);
                } else if (tokens[0].equalsIgnoreCase("FONTSET")) {
                    if (tokens.length < 2) {
                        errorMessage = "";
                        return false;
                    }
                    String imagePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    fontSet = new File(imagePathString);
                } else if (tokens[0].equalsIgnoreCase("SYMBOLSET")) {
                    if (tokens.length < 2) {
                        errorMessage = "Invalid syntax for SymbolSet : missing file name";
                        return false;
                    }

                    if (symbolSet == null) {
                        symbolSet = new SymbolSet();
                    }

                    String symbolPathString = ConversionUtilities.getValueFromMapfileLine(line);
                    if (this.getMapFile() != null) {
                        symbolSet.setSymbolSetFile(MapTools.buildFileFromMapPath(this.getMapFile().getParent(), null, symbolPathString));
                        symbolSet.load(br);
                    }
                //symbols = symbolSet.getArrayListSymbol();
                } else if (tokens[0].equalsIgnoreCase("IMAGECOLOR")) {
                    imageColor = new RGB();
                    result = imageColor.load(tokens);
                } else if (tokens[0].equalsIgnoreCase("IMAGEQUALITY")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    imageQuality = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("IMAGETYPE")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("GIF")) {
                        imageType = Map.GIF;
                    } else if (tokens[1].equalsIgnoreCase("PNG")) {
                        imageType = Map.PNG;
                    } else if (tokens[1].equalsIgnoreCase("JPEG")) {
                        imageType = Map.JPEG;
                    } else if (tokens[1].equalsIgnoreCase("WBMP")) {
                        imageType = Map.WBMP;
                    } else {
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("INTERLACE")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        interlace = Map.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        interlace = Map.OFF;
                    } else {
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("NAME")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    name = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("RESOLUTION")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    resolution = Integer.parseInt(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("SCALE")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    scale = Double.parseDouble(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("SHAPEPATH")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    String shapePathString = ConversionUtilities.getValueFromMapfileLine(line);
                    shapePath = new File(shapePathString);
                } else if (tokens[0].equalsIgnoreCase("SIZE")) {
                    if (tokens.length < 3) {
                        return false;
                    }
                    size = new Dimension();
                    size.width = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[1]));
                    size.height = Integer.parseInt(ConversionUtilities.removeDoubleQuotes(tokens[2]));
                    isSize = true;
                } else if (tokens[0].equalsIgnoreCase("STATUS")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        status = Map.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        status = Map.OFF;
                    } else {
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("TRANSPARENT")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("ON")) {
                        transparent = Map.ON;
                    } else if (tokens[1].equalsIgnoreCase("OFF")) {
                        transparent = Map.OFF;
                    } else {
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("UNITS")) {
                    if (tokens.length < 2) {
                        return false;
                    }
                    tokens[1] = ConversionUtilities.removeDoubleQuotes(tokens[1]);
                    if (tokens[1].equalsIgnoreCase("FEET")) {
                        units = Map.FEET;
                    } else if (tokens[1].equalsIgnoreCase("INCHES")) {
                        units = Map.INCHES;
                    } else if (tokens[1].equalsIgnoreCase("KILOMETERS")) {
                        units = Map.KILOMETERS;
                    } else if (tokens[1].equalsIgnoreCase("METERS")) {
                        units = Map.METERS;
                    } else if (tokens[1].equalsIgnoreCase("MILES")) {
                        units = Map.MILES;
                    } else if (tokens[1].equalsIgnoreCase("DD")) {
                        units = Map.DD;
                    } else {
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("LAYER")) {
                    Layer layer = new Layer();
                    result = layer.load(br);
                    addLayer(layer);
                } else if (tokens[0].equalsIgnoreCase("LEGEND")) {
                    legend = new Legend();
                    result = legend.load(br);
                } else if (tokens[0].equalsIgnoreCase("PROJECTION")) {
                    projection = new Projection();
                    result = projection.load(br);
                } else if (tokens[0].equalsIgnoreCase("QUERYMAP")) {
                    queryMap = new QueryMap();
                    result = queryMap.load(br);
                } else if (tokens[0].equalsIgnoreCase("REFERENCE")) {
                    referenceMap = new ReferenceMap();
                    result = referenceMap.load(br);
                } else if (tokens[0].equalsIgnoreCase("SCALEBAR")) {
                    scaleBar = new ScaleBar();
                    result = scaleBar.load(br);
                } else if (tokens[0].equalsIgnoreCase("SYMBOL")) {
                    Symbol symbol = new Symbol();
                    result = symbol.load(br);
                    addSymbol(symbol);
                } else if (tokens[0].equalsIgnoreCase("WEB")) {
                    web = new Web();
                    result = web.load(br);
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    return false;
                }

                // Stop parse file if error detected
                if (!result) {
                    return false;
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Map.load. Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (!isSize) {
            return false;
        }

        return result;
    }

    /**  Saves MAP object to the given BufferedWriter
     * with MapFile style.
     */
    public synchronized boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("map\n");
            if (name != null) {
                bw.write("\t name " + name + "\n");
            }
            if (extent != null) {
                extent.saveAsMapFile(bw);
            }
            if (fontSet != null) {
                bw.write("\t fontset " + ConversionUtilities.quotes(fontSet.getPath()) + "\n");
            }
            if (imageColor != null) {
                bw.write("\t imagecolor ");
                imageColor.saveAsMapFile(bw);
            }
            if (imageQuality > 0) {
                bw.write("\t imagequality " + imageQuality + "\n");
            }
            switch (imageType) {
                case GIF:
                    bw.write("\t imagetype GIF\n");
                    break;
                case PNG:
                    bw.write("\t imagetype PNG\n");
                    break;
                case JPEG:
                    bw.write("\t imagetype JPEG\n");
                    break;
                case WBMP:
                    bw.write("\t imagetype WBMP\n");
                    break;
            }
            switch (interlace) {
                case ON:
                    bw.write("\t interlace ON\n");
                    break;
                case OFF:
                    bw.write("\t interlace OFF\n");
                    break;
            }
            if (resolution != 72) {
                bw.write("\t resolution " + resolution + "\n");
            }
            if (scale > 0) {
                bw.write("\t scale " + scale + "\n");
            }
            if (shapePath != null) {
                bw.write("\t shapepath " + ConversionUtilities.quotes(shapePath.getPath()) + "\n");
            }
            if (size != null) {
                bw.write("\t size " + size.width + " " + size.height + "\n");
            }
            switch (status) {
                case ON:
                    bw.write("\t status ON\n");
                    break;
                case OFF:
                    bw.write("\t status OFF\n");
                    break;
            }
            if (symbolSet != null && symbolSet.getSymbolSetFile() != null) {
                bw.write("\t symbolset " + ConversionUtilities.quotes(symbolSet.getSymbolSetFile().getPath()) + "\n");
                // then writes the symbol file to the disk
                boolean res = symbolSet.saveAsSymFile();
                if (!res) {
                    result = res;
                }
            }
            switch (transparent) {
                case ON:
                    bw.write("\t transparent ON\n");
                    break;
                case OFF:
                    bw.write("\t transparent OFF\n");
                    break;
            }
            switch (units) {
                case FEET:
                    bw.write("\t units FEET\n");
                    break;
                case INCHES:
                    bw.write("\t units INCHES\n");
                    break;
                case KILOMETERS:
                    bw.write("\t units KILOMETERS\n");
                    break;
                case METERS:
                    bw.write("\t units METERS\n");
                    break;
                case MILES:
                    bw.write("\t units MILES\n");
                    break;
                case DD:
                    bw.write("\t units DD\n");
                    break;
            }
            if (legend != null) {
                legend.saveAsMapFile(bw);
            }
            if (projection != null) {
                projection.saveAsMapFile(bw);
            }
            if (queryMap != null) {
                queryMap.saveAsMapFile(bw);
            }
            if (referenceMap != null) {
                referenceMap.saveAsMapFile(bw);
            }
            if (scaleBar != null) {
                scaleBar.saveAsMapFile(bw);
            }
            if (web != null) {
                web.saveAsMapFile(bw);
            }
            if (layers != null) {
                for (int i = 0; i < layers.size(); i++) {
                    ((Layer) layers.get(i)).saveAsMapFile(bw);
                }
            }
            if (symbols != null) {
                for (int i = 0; i < symbols.size(); i++) {
                    ((Symbol) symbols.get(i)).saveAsMapFile(bw);
                }
            }
            bw.write("end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return result;
    }

    /** Returns a string representation of the MAP Object
     * @return a string representation of the MAP Object.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        try {
            buffer.append("MAP OBJECT \n");
            if (extent != null) {
                buffer.append("\n* MAP extent     = ").append(extent.toString());
            }
            if (imageColor != null) {
                buffer.append("\n* MAP imagecolor = ").append(imageColor.toString());
            }
            buffer.append("\n* MAP imageType  = ").append(imageType);
            buffer.append("\n* MAP imageQuality = ").append(imageQuality);
            if (name != null) {
                buffer.append("\n* MAP name       = ").append(name);
            }
            if (shapePath != null) {
                buffer.append("\n* MAP shapePath  = ").append(shapePath.getAbsolutePath());
            }
            if (fontSet != null) {
                buffer.append("\n* MAP fontSet    = ").append(fontSet.getAbsolutePath());
            }
            if (size != null) {
                buffer.append("\n* MAP size       = ").append(size);
            }
            buffer.append("\n* MAP status     = ").append(status);
            buffer.append("\n* MAP units      = ").append(units);
            if (scaleBar != null) {
                buffer.append("\n\n* MAP scaleBar   = ").append(scaleBar.toString());
            }
            if (legend != null) {
                buffer.append("\n\n* MAP legend     = ").append(legend.toString());
            }
            if (layers != null) {
                for (int i = 0; i < layers.size(); i++) {
                    buffer.append("\n\n* MAP LAYER ").append(i).append("     = ").append(((Layer) layers.get(i)).toString());
                }
            }
            if (web != null) {
                buffer.append("\n\n* MAP web         = ").append(web.toString());
            }
            if (symbols != null) {
                for (int i = 0; i < symbols.size(); i++) {
                    buffer.append("\n\n* MAP SYMBOL ").append(i).append("     = ").append(((Symbol) symbols.get(i)).toString());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CAN'T DISPLAY MAP OBJECT\n\n" + ex;
        }
        return buffer.toString();
    }
}

