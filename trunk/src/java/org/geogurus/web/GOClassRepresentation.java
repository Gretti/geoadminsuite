/*
 * GOClassRepresentation.java
 *
 * Created on 15 janvier 2003, 16:33
 */
package org.geogurus.web;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.geogurus.data.DataAccess;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.Symbol;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.DataManager;

/**
 * Servlet to deal with MS Class representation: It calls Mapserver to generate an image showing the
 * class representation: color, outlinecolor, bgcolor, text color, symbol.<br>
 * It generates an URL to mapserver, then calls it and retrieve the image stream.
 * This servlet is called by an HTML image source.
 *
 * @author  nri
 */
public class GOClassRepresentation extends BaseServlet {

    /**
     * inner class pattern that defines the MS symbolname and it's size
     * used as a value in the HashPattern
     */
    private class Pattern {

        public String name = null;
        public int size = 4;

        public Pattern(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }
    private Hashtable hashPtPattern = null;
    private Hashtable hashLnPattern = null;
    private Hashtable hashPgPattern = null;

    @Override
    public void init() {
        Vector v0 = new Vector(1);
        Vector v1 = new Vector(1);
        Vector v2 = new Vector(1);
        Vector v3 = new Vector(1);
        Vector v4 = new Vector(1);
        Vector v5 = new Vector(1);
        Vector v6 = new Vector(1);
        Vector v7 = new Vector(1);
        Vector v8 = new Vector(1);
        Vector v9 = new Vector(1);
        Vector v10 = new Vector(1);
        Vector v11 = new Vector(1);
        Vector v12 = new Vector(1);
        Vector v13 = new Vector(1);
        Vector v14 = new Vector(1);
        Vector v15 = new Vector(1);
        Vector v16 = new Vector(1);
        Vector v17 = new Vector(1);
        Vector v18 = new Vector(1);
        Vector v19 = new Vector(1);
        Vector v20 = new Vector(1);
        Vector v21 = new Vector(1);
        Vector v22 = new Vector(1);
        Vector v23 = new Vector(1);
        Vector v24 = new Vector(1);
        Vector v25 = new Vector(1);
        Vector v26 = new Vector(1);
        Vector v27 = new Vector(1);

        Vector vp1 = new Vector(1);
        Vector vp2 = new Vector(1);
        Vector vp3 = new Vector(1);
        Vector vp4 = new Vector(1);
        Vector vp5 = new Vector(1);
        Vector vp6 = new Vector(1);
        Vector vp7 = new Vector(1);
        Vector vp8 = new Vector(1);
        Vector vp9 = new Vector(1);
        Vector vp10 = new Vector(1);
        Vector vp11 = new Vector(1);
        Vector vp12 = new Vector(1);
        Vector vp13 = new Vector(1);
        Vector vp14 = new Vector(1);
        Vector vp15 = new Vector(1);
        Vector vp16 = new Vector(1);
        Vector vp17 = new Vector(1);
        Vector vp18 = new Vector(1);
        Vector vp19 = new Vector(1);
        Vector vp20 = new Vector(1);
        Vector vp21 = new Vector(1);
        Vector vp22 = new Vector(1);
        Vector vp23 = new Vector(1);
        Vector vp24 = new Vector(1);
        Vector vp25 = new Vector(1);
        Vector vp26 = new Vector(1);
        Vector vp27 = new Vector(1);
        Vector vp28 = new Vector(1);
        Vector vp29 = new Vector(1);
        Vector vp30 = new Vector(1);
        Vector vp36 = new Vector(1);
        Vector vp35 = new Vector(1);
        Vector vp34 = new Vector(1);
        Vector vp33 = new Vector(1);
        Vector vp32 = new Vector(1);
        Vector vp31 = new Vector(1);

        Vector vl0 = new Vector(1, 1);
        Vector vl1 = new Vector(1, 1);
        Vector vl2 = new Vector(1, 1);
        Vector vl3 = new Vector(1, 1);
        Vector vl4 = new Vector(1, 1);
        Vector vl5 = new Vector(1, 1);
        Vector vl6 = new Vector(1, 1);
        Vector vl7 = new Vector(1, 1);
        Vector vl8 = new Vector(1, 1);
        Vector vl9 = new Vector(1, 1);
        Vector vl10 = new Vector(1, 1);
        Vector vl11 = new Vector(1, 1);
        Vector vl12 = new Vector(1, 1);
        Vector vl13 = new Vector(1, 1);
        Vector vl14 = new Vector(1, 1);
        Vector vl15 = new Vector(1, 1);

        v0.add(new Pattern("pattern0", 0));
        v1.add(new Pattern("pg1", 0));
        v2.add(new Pattern("pattern14", 4));
        v3.add(new Pattern("ln1", 2));
        v4.add(new Pattern("pattern10", 2));
        v5.add(new Pattern("pattern11", 3));
        v6.add(new Pattern("pattern12", 4));
        v7.add(new Pattern("pattern13", 6));
        v8.add(new Pattern("pattern4", 10));
        v9.add(new Pattern("pattern4", 5));
        v10.add(new Pattern("pattern4", 3));
        v11.add(new Pattern("pattern4", 10));
        v12.add(new Pattern("pattern4", 5));
        v13.add(new Pattern("pattern8", 10));
        v14.add(new Pattern("pattern8", 5));
        v15.add(new Pattern("pattern8", 3));
        v16.add(new Pattern("pattern8", 10));
        v17.add(new Pattern("pattern8", 5));
        v18.add(new Pattern("pattern5", 10));
        v19.add(new Pattern("pattern5", 5));
        v20.add(new Pattern("pattern5", 15));
        v21.add(new Pattern("pattern6", 10));
        v22.add(new Pattern("pattern6", 15));
        v23.add(new Pattern("pattern6", 5));
        v24.add(new Pattern("ln1", 6));
        v25.add(new Pattern("pattern15", 4));
        v26.add(new Pattern("pattern7", 8));
        v27.add(new Pattern("pattern27", 5));

        vp1.add(new Pattern(ObjectKeys.DEFAULT_POINT_SYMBOL, 8));
        vp2.add(new Pattern("pt2", 8));
        vp3.add(new Pattern("pt3", 8));
        vp4.add(new Pattern("pt4", 8));
        vp5.add(new Pattern("pt5", 8));
        vp6.add(new Pattern("pt6", 8));
        vp7.add(new Pattern("pt7", 8));
        vp8.add(new Pattern("pt8", 8));
        vp9.add(new Pattern("pt9", 8));
        vp10.add(new Pattern("pt10", 8));
        vp11.add(new Pattern("pt11", 8));
        vp12.add(new Pattern("pt12", 8));
        vp13.add(new Pattern("pt13", 8));
        vp14.add(new Pattern("pt14", 8));
        vp15.add(new Pattern("pt15", 8));
        vp16.add(new Pattern("pt16", 8));
        vp17.add(new Pattern("pt17", 8));
        vp18.add(new Pattern("pt18", 8));
        vp19.add(new Pattern("pt19", 8));
        vp20.add(new Pattern("pt20", 8));
        vp21.add(new Pattern("pt21", 8));
        vp22.add(new Pattern("pt22", 8));
        vp23.add(new Pattern("pt23", 8));
        vp24.add(new Pattern("pt24", 8));
        vp25.add(new Pattern("pt25", 8));
        vp26.add(new Pattern("pt26", 8));
        vp27.add(new Pattern("pt27", 8));
        vp28.add(new Pattern("pt28", 8));
        vp29.add(new Pattern("pt29", 8));
        vp30.add(new Pattern("pt30", 8));
        vp36.add(new Pattern("pt31", 8));
        vp35.add(new Pattern("pt32", 8));
        vp34.add(new Pattern("pt33", 8));
        vp33.add(new Pattern("pt34", 8));
        vp32.add(new Pattern("pt35", 8));
        vp31.add(new Pattern("pt36", 8));

        vl0.add(new Pattern("ln2", 1));
        vl1.add(new Pattern("ln1", 1));
        vl2.add(new Pattern("ln3", 1));
        vl3.add(new Pattern("ln3bis", 1));
        vl4.add(new Pattern("ln4", 1));
        vl5.add(new Pattern("ln5", 1));
        vl6.add(new Pattern("ln6", 1));
        vl7.add(new Pattern("ln7", 1));
        vl8.add(new Pattern("ln8", 1));
        vl9.add(new Pattern("ln9", 1));
        vl10.add(new Pattern("ln2", 2));
        vl10.add(new Pattern("ln10", 3));
        vl11.add(new Pattern("ln11", 1));
        vl12.add(new Pattern("ln12", 1));
        vl13.add(new Pattern("ln2", 5));
        vl13.add(new Pattern("ln2", 3));
        vl14.add(new Pattern("ln3", 5));
        vl14.add(new Pattern("ln4", 1));
        vl15.add(new Pattern("ln2", 5));
        vl15.add(new Pattern("ln6", 3));

        hashPgPattern = new Hashtable(28);
        hashPtPattern = new Hashtable(36);
        hashLnPattern = new Hashtable(15);
        hashPgPattern.put("0", v0);
        hashPgPattern.put("1", v1);
        hashPgPattern.put("2", v2);
        hashPgPattern.put("3", v3);
        hashPgPattern.put("4", v4);
        hashPgPattern.put("5", v5);
        hashPgPattern.put("6", v6);
        hashPgPattern.put("7", v7);
        hashPgPattern.put("8", v8);
        hashPgPattern.put("9", v9);
        hashPgPattern.put("10", v10);
        hashPgPattern.put("11", v11);
        hashPgPattern.put("12", v12);
        hashPgPattern.put("13", v13);
        hashPgPattern.put("14", v14);
        hashPgPattern.put("15", v15);
        hashPgPattern.put("16", v16);
        hashPgPattern.put("17", v17);
        hashPgPattern.put("18", v18);
        hashPgPattern.put("19", v19);
        hashPgPattern.put("20", v20);
        hashPgPattern.put("21", v21);
        hashPgPattern.put("22", v22);
        hashPgPattern.put("23", v23);
        hashPgPattern.put("24", v24);
        hashPgPattern.put("25", v25);
        hashPgPattern.put("26", v26);
        hashPgPattern.put("27", v27);

        hashPtPattern.put("0", vp1);
        hashPtPattern.put("1", vp2);
        hashPtPattern.put("2", vp3);
        hashPtPattern.put("3", vp4);
        hashPtPattern.put("4", vp5);
        hashPtPattern.put("5", vp6);
        hashPtPattern.put("6", vp7);
        hashPtPattern.put("7", vp8);
        hashPtPattern.put("8", vp9);
        hashPtPattern.put("9", vp10);
        hashPtPattern.put("10", vp11);
        hashPtPattern.put("11", vp12);
        hashPtPattern.put("12", vp13);
        hashPtPattern.put("13", vp14);
        hashPtPattern.put("14", vp15);
        hashPtPattern.put("15", vp16);
        hashPtPattern.put("16", vp17);
        hashPtPattern.put("17", vp18);
        hashPtPattern.put("18", vp19);
        hashPtPattern.put("19", vp20);
        hashPtPattern.put("20", vp21);
        hashPtPattern.put("21", vp22);
        hashPtPattern.put("22", vp23);
        hashPtPattern.put("23", vp24);
        hashPtPattern.put("24", vp25);
        hashPtPattern.put("25", vp26);
        hashPtPattern.put("26", vp27);
        hashPtPattern.put("27", vp28);
        hashPtPattern.put("28", vp29);
        hashPtPattern.put("29", vp30);
        hashPtPattern.put("30", vp36);
        hashPtPattern.put("31", vp35);
        hashPtPattern.put("32", vp34);
        hashPtPattern.put("33", vp33);
        hashPtPattern.put("34", vp32);
        hashPtPattern.put("35", vp31);

        hashLnPattern.put("0", vl1);
        hashLnPattern.put("1", vl2);
        hashLnPattern.put("2", vl3);
        hashLnPattern.put("3", vl4);
        hashLnPattern.put("4", vl5);
        hashLnPattern.put("5", vl6);
        hashLnPattern.put("6", vl7);
        hashLnPattern.put("7", vl8);
        hashLnPattern.put("8", vl9);
        hashLnPattern.put("9", vl10);
        hashLnPattern.put("10", vl11);
        hashLnPattern.put("11", vl12);
        hashLnPattern.put("12", vl13);
        hashLnPattern.put("13", vl14);
        hashLnPattern.put("14", vl15);
    }

    /**
     * Main method listening to client requests:
     * See this page for the complete list of parameters names
     */
    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);

        // the class id is passed by the JSP in the request.
        String classID = request.getParameter("classid");
        String symbolKey = request.getParameter("symbolkey");
        String fgColor = request.getParameter("color");
        String bgColor = request.getParameter("bgcolor");
        String outColor = request.getParameter("outcolor");
        String txtColor = request.getParameter("txtcolor");
        String genColor = request.getParameter("gencolor");

        // Usermapbean
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);

        // the layer's object of the current GeometryClass: all the GeometryClass were asked to generate
        // their layer object, as their are displayed in the map
        DataAccess gc = (DataAccess) session.getAttribute(ObjectKeys.CURRENT_GC);
        Layer gcLayer = gc.getMSLayer(new RGB(0, 0, 0), false);
        // gets current class to set its representations parameters:
        Class cl = null;

        for (Iterator iter = gcLayer.getMapClass().getClasses(); iter.hasNext();) {
            cl = (Class) iter.next();
            if (classID.equals("" + cl.getID())) {
                break;
            }
        }

        // the string representation of the mapserver URL
        StringBuffer msURL = new StringBuffer(DataManager.getProperty("MAPSERVERURL"));

        // path to special mapfile for class representation. It depends on the current layer type: one mapfile
        // for each type (pt, line, pg)
        String mapPath = getServletContext().getRealPath("").replace('\\', '/') + "/msFiles/templates/symbology_";
        Hashtable h = null;
        switch (gcLayer.getType()) {
            case (Layer.POINT):
                mapPath += "pt.map";
                h = hashPtPattern;
                break;
            case (Layer.POLYLINE):
            case (Layer.LINE):
                mapPath += "ln.map";
                h = hashLnPattern;
                break;
            default:
                mapPath += "pg.map";
                h = hashPgPattern;
                break;
        }

        // sets class parameters
        if (genColor != null && genColor.length() > 0) {
            cl.setColor(new RGB(genColor));
        } else if (fgColor != null && fgColor.length() > 0) {
            cl.setColor(new RGB(fgColor));
        } else {
            cl.setColor(null);
        }

        if (bgColor != null && bgColor.length() > 0) {
            cl.setBackgroundColor(new RGB(bgColor));
        } else {
            cl.setBackgroundColor(null);
        }

        if (outColor != null && outColor.length() > 0) {
            cl.setOutlineColor(new RGB(outColor));
        } else {
            cl.setOutlineColor(null);
        }

        //FIXME: default color for labels is black (never transparent)
        if (cl.getLabel() == null) {
            //creates a new label
            Label newLabel = new Label();
            cl.setLabel(newLabel);
        }
        if (txtColor == null || txtColor.length() == 0) {
            txtColor = "0 0 0";
        }
        cl.getLabel().setColor(new RGB(txtColor));

        // symbol name: it can be the name of the symbol, or null to indicate to remove the symbol
        if (symbolKey != null && symbolKey.length() > 0) {
            // manage userSymbolSet object
            updateUserSymbolSet(symbolKey, umb, h, cl.getSymbol());

            if (symbolKey.equals("0") && gcLayer.getType() != Layer.POINT) {
                cl.setSymbol(null);
                // removes color as the empty symbol represent an outlined polygon only for a polygon type, of course:
                if (gcLayer.getType() == Layer.POLYGON) {
                    cl.setColor(null);
                    cl.setBackgroundColor(null);
                }
            } else {
                // heavy mechanism to put a size for each symbol
                if ((Pattern) (((Vector) h.get(symbolKey)).get(0)) != null) {
                    cl.setSymbol(((Pattern) ((Vector) h.get(symbolKey)).get(0)).name);
                    cl.setSize(((Pattern) ((Vector) h.get(symbolKey)).get(0)).size);

                    if (((Vector) h.get(symbolKey)).size() == 2) {
                        cl.setOverlaySymbol(((Pattern) ((Vector) h.get(symbolKey)).get(1)).name);
                        cl.setOverlaySize(((Pattern) ((Vector) h.get(symbolKey)).get(1)).size);

                        // for overlay symbols, forces an overlay color
                        if (cl.getOverlayColor() == null) {
                            // probably the firt time a symbol is applied, or color was explicity reset:
                            cl.setOverlayColor(cl.getColor());
                            cl.setColor(new RGB(0, 0, 0));
                        }

                        if (genColor != null && genColor.length() > 0) {
                            // generated color parameter is passed when a classification with graduated color was generated.
                            cl.setOverlayColor(new RGB(genColor));
                            cl.setColor(new RGB(0, 0, 0));
                        } else if (fgColor != null && fgColor.length() > 0) {
                            cl.setOverlayColor(new RGB(fgColor));
                            cl.setColor(new RGB(0, 0, 0));
                            cl.setOverlayBackgroundColor(new RGB(fgColor));
                            cl.setBackgroundColor(new RGB(0, 0, 0));
                        }
                    } else {
                        // must remove the overlaysymbol
                        cl.setOverlaySymbol(null);
                    }
                } else {
                    cl.setSymbol(((Pattern) ((Vector) h.get("0")).get(0)).name);
                    cl.setSize(((Pattern) ((Vector) h.get("0")).get(0)).size);
                }
            }
        }

        // build mapserverurl according to class parameters: this servlet can be called with only classid parameter,
        msURL.append("?mode=map&map=");
        try {
            msURL.append(java.net.URLEncoder.encode(mapPath, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException uee) {
            msURL.append(mapPath);
        }

        if (cl.getColor() != null) {
            msURL.append("&map_layer_0_class_color=");
            msURL.append(cl.getColor().toString().replace(' ', '+'));
        }
        if (cl.getBackgroundColor() != null) {
            msURL.append("&map_layer_0_class_backgroundcolor=");
            msURL.append(cl.getBackgroundColor().toString().replace(' ', '+'));
        }
        if (cl.getOutlineColor() != null) {
            msURL.append("&map_layer_0_class_outlinecolor=");
            msURL.append(cl.getOutlineColor().toString().replace(' ', '+'));
        }
        if (cl.getLabel() != null && cl.getLabel().getColor() != null) {
            msURL.append("&map_layer_0_class_label_color=");
            msURL.append(cl.getLabel().getColor().toString().replace(' ', '+'));
        }
        if (cl.getSymbol() != null) {
            msURL.append("&map_layer_0_class_symbol=");
            msURL.append(cl.getSymbol().toString().replace(' ', '+'));
        }

        // Refresh the mapfile if coming from colorPicker, otherwise, skips this step
        if (genColor == null) {
            UserMapBeanManager umbmgr = new UserMapBeanManager();
            umbmgr.setUserMapBean(umb);
            umbmgr.writeMapFile();
        }
        // msURL is ready, can send the mapserver image as a response
        // Set content type
        response.setContentType("image/gif");

        OutputStream imgOut = null;
        URLConnection URLConn;
        URL u = null;

        // Encodes mapfile when sending it as an URL, to avoid errors with spaces in mapfile path
        try {
            mapPath = URLEncoder.encode(mapPath, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
        }

        try {
            imgOut = response.getOutputStream();
            u = new URL(msURL.toString());
            URLConn = u.openConnection();
            // Setup the connection
            URLConn.setDoOutput(false);
            URLConn.setDoInput(true);
            URLConn.setUseCaches(false);

            // Open up an input stream to get the response from mapserver
            DataInputStream in = new DataInputStream(URLConn.getInputStream());

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                imgOut.write(buf, 0, count);
            }
            // clean-up
            in.close();
            imgOut.close();
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
        return;
    }

    /**
     * Method to update the UserMapBean.userSymbolSet object by removing unused symbols and adding
     * new one
     */
    private void updateUserSymbolSet(String symbolKey, UserMapBean umb, Hashtable h, String oldSymbol) {
        SymbolSet uss = umb.getUserSymbolSet();
        String newSymbol = ((Pattern) ((Vector) h.get(symbolKey)).get(0)).name;
        Symbol sym = null;
        ArrayList al = uss.getArrayListSymbol();
        boolean newSymbolExists = false;
        Hashtable gasSym = (Hashtable) this.getServletContext().getAttribute(ObjectKeys.GAS_SYMBOL_LIST);
        Vector layerList = umb.getMapfile().getLayers();
        Layer l = null;
        Class c = null;
        int symCounter = 0;
        Symbol s = null;
        if (gasSym == null) {
            return;
        }

        // removes old symbol if necessary
        for (Iterator iter = layerList.iterator(); iter.hasNext();) {
            l = (Layer) iter.next();

            if (l.getMapClass() != null) {
                for (Iterator iter2 = l.getMapClass().getClasses(); iter2.hasNext();) {
                    c = (Class) iter2.next();

                    if (c.getSymbol() != null && c.getSymbol().equalsIgnoreCase(oldSymbol)) {
                        symCounter++;
                    }
                }
            }
        }
        if (symCounter == 1) {
            // only one symbol in the map: associated with the current class: can remove it
            s = new Symbol();
            s.setName(oldSymbol);
            uss.getArrayListSymbol().remove(uss.getArrayListSymbol().indexOf(s));
        }

        // add new symbol only if it does not already exist
        for (Iterator iter = al.iterator(); iter.hasNext();) {
            sym = (Symbol) iter.next();

            if (sym.getName().equalsIgnoreCase(newSymbol)) {
                newSymbolExists = true;
            }
        }
        if (!newSymbolExists) {
            s = (Symbol) gasSym.get(newSymbol);
            if (s != null) {
                uss.getArrayListSymbol().add(s);
            }
        }
    }
}
