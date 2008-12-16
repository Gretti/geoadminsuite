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

import org.geogurus.mapserver.objects.Symbol;

/**
 * A bean representing a GAS symbology: MS Symbol + size to set to the class
 * @author nicolas
 */
public class SymbologyBean {
    /** the size at which the Symbol must be rendered to represent this symbology */
    private int size;
    private Symbol symbol;
    /** the optional overlaySymbol for this symbology */
    private Symbol overlaySymbol;
    /** the size at which the OverlaySymbol must be rendered to represent this symbology */
    private int overlaySize;
    /** the symbol icon */
    private String icon;
    private String id;
    public SymbologyBean() {
        
    }
    public SymbologyBean(String id, int size, int overlaySize, Symbol symbol, Symbol overlaySymbol, String icon) {
        this.size = size;
        this.overlaySize = overlaySize;
        this.symbol = symbol;
        this.overlaySymbol = overlaySymbol;
        this.id = id;
        this.icon = icon;
    }

    public SymbologyBean(String id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Symbol getOverlaySymbol() {
        return overlaySymbol;
    }

    public void setOverlaySymbol(Symbol overlaySymbol) {
        this.overlaySymbol = overlaySymbol;
    }

    public int getOverlaySize() {
        return overlaySize;
    }

    public void setOverlaySize(int overlaySize) {
        this.overlaySize = overlaySize;
    }
    
}
