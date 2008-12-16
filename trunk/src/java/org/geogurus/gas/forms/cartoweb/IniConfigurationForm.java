/*
 * Copyright (C) 2007-2008  Camptocamp
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

package org.geogurus.gas.forms.cartoweb;

import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.geogurus.cartoweb.ImagesConf;
import org.geogurus.cartoweb.LayerConf;
import org.geogurus.cartoweb.LocationConf;
import org.geogurus.cartoweb.QueryConf;

/**
 * A FormBean to manage CW configuration objects. can be used to be stored in session
 * to represent the current CW user configuration
 * @author nicolas
 */
public class IniConfigurationForm extends org.apache.struts.action.ActionForm {
    private LayerConf layerConf;
    private QueryConf queryConf;
    private ImagesConf imagesConf;
    private LocationConf locationConf;

    private Logger logger;
    public IniConfigurationForm() {
        logger = Logger.getLogger(this.getClass().getName());
        logger.info("ini conf initialized...");
    }
    
    /**
     * Gets the list of ini files generated into the given path, representing
     * the current Cartoweb ini files configuration
     * 
     * @param path the path to write ini files into (must point to an existing valid folder)
     * (null Ini configuration objets won't generate any file), thus the size of the returned vector
     * is the size of valid CW configuration objects
     * @return A vector of generated Files into the given path.
     * Following files will be generated:
     * <ul>
     * <li>LayerConf: layers.ini</li>
     * <li>QueryConf: query.ini</li>
     * <li>ImagesConf: image.ini</li>
     * <li>LocationConf: Location.ini</li>
     * </ul>
     */
    public Vector<File> getIniFiles(File path) {
        if (path == null || ! path.isDirectory()) {
            logger.warning("null or invalid path to write CW ini files to...");
            return null;
        }
        Vector<File> files = new Vector<File>(4);
        return files;
    }
    public ImagesConf getImagesConf() {
        if (imagesConf == null) {
            imagesConf = new ImagesConf();
        }
        return imagesConf;
    }

    public void setImagesConf(ImagesConf imagesConf) {
        this.imagesConf = imagesConf;
    }

    public LayerConf getLayerConf() {
        if (layerConf == null) {
            layerConf = new LayerConf();
        }
        return layerConf;
    }

    public void setLayerConf(LayerConf layerConf) {
        this.layerConf = layerConf;
    }

    public LocationConf getLocationConf() {
        if (locationConf == null) {
            locationConf = new LocationConf();
        }
        return locationConf;
    }

    public void setLocationConf(LocationConf locationConf) {
        this.locationConf = locationConf;
    }

    public QueryConf getQueryConf() {
        if (queryConf == null) {
            queryConf = new QueryConf();
        }
        return queryConf;
    }

    public void setQueryConf(QueryConf queryConf) {
        this.queryConf = queryConf;
    }
    
    /**
     * Sets boolean values to false for each non-null objects of this form
     * @param arg0
     * @param arg1
     */
    @Override
    public void reset(ActionMapping arg0, HttpServletRequest arg1) {
        super.reset(arg0, arg1);
        //reset layers.ini properties
        getLayerConf().setAutoClassLegendStruts(false);
        
        getImagesConf().setCollapsibleKeymap(Boolean.FALSE);
        getImagesConf().setMapSizesActive(Boolean.FALSE);
        getImagesConf().setNoDrawKeymap(Boolean.FALSE);
        getImagesConf().setNoDrawScalebar(Boolean.FALSE);
        getImagesConf().setMapSizesAsString(new String[0]);
        
        getLocationConf().setNoBboxAdjusting(Boolean.FALSE);
        getLocationConf().setRefLinesActive(Boolean.FALSE);
        getLocationConf().setScaleModeDiscrete(Boolean.FALSE);
        getLocationConf().setScalesAsString(new String[0]);
        getLocationConf().setShortcutsAsString(new String[0]);
        
        getQueryConf().setDefaultAttributes(Boolean.TRUE);
        getQueryConf().setDefaultHilight(Boolean.TRUE);
        getQueryConf().setDefaultMaskmode(Boolean.FALSE);
        getQueryConf().setDefaultTable(Boolean.TRUE);
        getQueryConf().setDisplayExtendedSelection(Boolean.FALSE);
        getQueryConf().setPersistentQueries(Boolean.FALSE);
        getQueryConf().setReturnAttributesActive(Boolean.FALSE);
    }
    
}
