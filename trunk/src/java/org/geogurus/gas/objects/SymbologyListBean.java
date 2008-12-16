/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.objects;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Logger;

import org.geogurus.mapserver.objects.Symbol;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.tools.string.ConversionUtilities;

/**
 * Class to represent the list of supported GAS symbols
 * @author nicolas
 */
public class SymbologyListBean {
    public final static byte ALL = 0;
    public final static byte POINT = 1;
    public final static byte LINESTRING = 2;
    public final static byte POLYGON = 3;
    /**
     * The list of GAS symbols (MapServer Symbol objects indexed by their names
     * See msFiles/symbols.sym for the list of GAS defined symbols
     */
    private Hashtable<String, SymbologyBean> symbolList;
    /** the size of the list, shorcut for UI component like STRUTS */
    private int size;
    private Logger logger;
    
    public SymbologyListBean() {
        logger = Logger.getLogger(this.getClass().getName());
    }
    public Hashtable<String, SymbologyBean> getSymbolList() {
        return symbolList;
    }
    
    
    public void setSymbolList(Hashtable<String, SymbologyBean> symbolList) {
        this.symbolList = symbolList;
    }
    
    /**
     * Load the bean from the given mapserver symbol file and the symbology properties
     * (see msFiles/symbols.sym and symbology.properties)
     * @param symFile 
     * @param p
     */
    public void load(String symFile, Properties p) {
        if (symFile == null || p == null) {
            return;
        }
        SymbologyBean symBean = null;
        Symbol s = null;
        Hashtable<String, Symbol> symbols = new Hashtable<String, Symbol>();
        SymbolSet symbolSet = null;
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(symFile));
            symbolSet = new SymbolSet();
            if (!symbolSet.load(br)) {
                logger.warning("cannot load symbol file: " + symFile);
            }
            br.close();
        } catch (Exception e) {
            logger.warning(e.getMessage());
            return;
        }
        symbolList = new Hashtable<String, SymbologyBean>(symbols.size());
        Enumeration e = p.propertyNames();
        while (e.hasMoreElements()) {
            String prop = (String) e.nextElement();

            String[] keys = ConversionUtilities.explodeKey(prop);
            if (keys.length > 2 && "symbology".equalsIgnoreCase(keys[0])) {
                symBean = symbolList.containsKey(keys[1]) ? symbolList.get(keys[1]) : new SymbologyBean(keys[1]);
                if ("msSymbol".equalsIgnoreCase(keys[2])) {
                    s = symbolSet.getSymbol(p.getProperty(prop));
                    symBean.setSymbol(s);
                } else if ("msOverlaySymbol".equalsIgnoreCase(keys[2])) {
                    s = symbolSet.getSymbol(p.getProperty(prop));
                    symBean.setOverlaySymbol(s);
                } else if ("icon".equalsIgnoreCase(keys[2])) {
                    symBean.setIcon(p.getProperty(prop));
                } else if ("size".equalsIgnoreCase(keys[2])) {
                    symBean.setSize(new Integer(p.getProperty(prop)).intValue());
                } else if ("overlaySize".equalsIgnoreCase(keys[2])) {
                    symBean.setOverlaySize(new Integer(p.getProperty(prop)).intValue());
                }
                symbolList.put(keys[1], symBean);
            }
        }
    }
    
    /**
     * Returns the Mapserver Symbol object for given Symbology id
     * @param symbologyId
     * @return
     */
    public Symbol getSymbol(String symbologyId) {
        if (symbolList == null) {
            return null;
        }
        return symbolList.get(symbologyId).getSymbol();
    }
    
    /** returns the list of MS symbols */
    public ArrayList<Symbol> getSymbols() {
        if (symbolList == null) {
            return null;
        }
        ArrayList<Symbol> res = new ArrayList(symbolList.size());
        Enumeration<SymbologyBean> symBeans = symbolList.elements();
        while (symBeans.hasMoreElements()) {
            res.add(symBeans.nextElement().getSymbol());
        }
        return res;
    }
    
    /** returns a JSON/JS dictionnary list of point symbols:
     * Symbol name begins wiht "pt" keyword
     * @return a Dict structure {symName: "sym icon", ...}
     */
    public String getPointIcons() {
        return toString(SymbologyListBean.POINT);
    }
    /** returns a JSON/JS dictionnary list of linestring symbols:
     * Symbol name begins wiht "ln" keyword
     * @return a Dict structure {symName: "sym icon", ...}
     */
    public String getLineIcons() {
        return toString(SymbologyListBean.LINESTRING);
    }
    /** returns a JSON/JS dictionnary list of polygons symbols:
     * Symbol name begins wiht "pg" keyword
     * @return a Dict structure {symName: "sym icon", ...}
     */
    public String getPolygonsIcons() {
        return toString(SymbologyListBean.POLYGON);
    }
    
    /** returns a JSON/JS dictionnary list of symbols:
     * @return a Dict structure {symName: "sym icon", ...}
     */
    @Override
    public String toString() {
        return toString(SymbologyListBean.ALL);
    }
    
    private String toString(byte type) {
        if (symbolList == null || symbolList.size() == 0) {
            return "{}";
        }
        String token = "";
        switch (type) {
            case SymbologyListBean.ALL :
                token = "";
                break;
            case SymbologyListBean.POINT :
                token = "pt";
                break;
            case SymbologyListBean.LINESTRING :
                token = "ln";
                break;
            case SymbologyListBean.POLYGON :
                token = "pg";
                break;
            default:
                token = "";
                break;
        }
        Enumeration<String> names = symbolList.keys();
        StringBuilder res = new StringBuilder("{");
        
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            if (n.indexOf(token) == 0) {
                res.append(n).append(":\"").append(symbolList.get(n).getIcon()).append("\",");
            }
        }
        //removes trailing ","
        if (res.length() > 1) {
            res.deleteCharAt(res.length()-1);
        }
        res.append("}");
        return res.toString();
    }

    public int getSize() {
        if (symbolList == null) {
            size = 0;
        } else {
            size = symbolList.size();
        }
        return size;
    }
}
