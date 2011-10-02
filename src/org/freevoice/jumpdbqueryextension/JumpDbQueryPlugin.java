/*
 *
 *  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
 *  JUMP is Copyright (C) 2003 Vivid Solutions
 *
 *  This file is part of the JUMP DB Query Plugin.
 *
 *  The JUMP DB Query Plugin is free software; you can redistribute it and/or
 *  modify it under the terms of the Lesser GNU General Public License as
 *  published *  by the Free Software Foundation; either version 3 of the
 *  License, or  (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freevoice.jumpdbqueryextension;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import org.freevoice.jumpdbqueryextension.ui.QueryDialog;
import org.freevoice.jumpdbqueryextension.util.DbQueryProperties;
import org.freevoice.jumpdbqueryextension.util.Logger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;

/**
 * JUMP plugin to display results of a SQL "SELECT" query.
 */
public class JumpDbQueryPlugin extends AbstractPlugIn implements Runnable {

    private QueryDialog queryDialog;
    private PlugInContext context;
    /** tru to indicate the layer coming from the current query should be refreshed instead of
     * created
     */
    private boolean refresh;

    @Override
    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuItem(this, new String[]{"Tools"}, "Database Query", false, null, null);
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        this.context = context;
        this.refresh = false;
        File pluginDirectory = context.getWorkbenchContext().getWorkbench().getPlugInManager().getPlugInDirectory();

        String queryPropertiesBundleName = pluginDirectory.getCanonicalPath() + File.separator + "dbquery.properties";

        Logger.logDebug("Reading query properties file: " + queryPropertiesBundleName);

        DbQueryProperties queryProperties = new DbQueryProperties(queryPropertiesBundleName);

        if (this.queryDialog == null) {
            this.queryDialog = QueryDialog.showDialog(this, context.getWorkbenchFrame(), "DB Query",
                    queryProperties.getConnectionParameters());
            this.queryDialog.initUICode();
        }
        queryDialog.setVisible(true);

        return this.queryDialog.isCancelled();
    }

    private boolean checkNullGeometries(FeatureCollection featureCollection) {
        List featureList = featureCollection.getFeatures();

        boolean hasNullGeometries = false;
        for (Object object : featureList) {
            Geometry geometry = ((Feature) object).getGeometry();

            if (geometry == null) {
                hasNullGeometries = true;
            } else if (geometry instanceof GeometryCollection) {
                if (((GeometryCollection) geometry).getNumGeometries() <= 0) {
                    hasNullGeometries = true;
                }
            }

            if (hasNullGeometries) {
                break;
            }

        }

        return hasNullGeometries;
    }

    /**
     * Runs the query in a separate thread, to allow dialog to update its UI
     * @throws Exception if something goes wrong
     */
    public void runQuery() throws Exception {
           this.refresh = false;
           Thread t = new Thread(this);
           t.start();
    }

    /**
     * Refreshes the query in a separate thread, to allow dialog to update its UI
     * @throws Exception if something goes wrong
     */
    public void refreshQuery() throws Exception {
           this.refresh = true;
           Thread t = new Thread(this);
           t.start();
    }

    public void run() {
        queryDialog.initUiBeforeQuery();

        String msg = "";
        try {
            Class queryClazz = null;
            try {
                queryClazz = Class.forName(queryDialog.getQueryClass());
            } catch (ClassNotFoundException cnfe) {
                msg = "Please, choose a database connection from the dropdown menu";
                queryDialog.refreshUiForError(msg);
                context.getWorkbenchFrame().warnUser(msg);
                return;
            }
            Constructor constructor = queryClazz.getConstructor();
            JumpDbQuery dbQuery = (JumpDbQuery) constructor.newInstance();
            String dbDriver = queryDialog.getDriver();
            dbQuery.setupDb(dbDriver, queryDialog.getJdbcUrl(), queryDialog.getUsername(), queryDialog.getPassword());
            //FIXME - get this from the GUI
            int maxRows = 100;
            String queryString = queryDialog.getQuery();
            Logger.logDebug("running query: " + queryString);
            long start = System.currentTimeMillis();

            FeatureCollection featureCollection = null;
                featureCollection = dbQuery.getCollection(queryString, maxRows);
                long end = System.currentTimeMillis();
                long seconds = (end - start) / 1000;
                String secondString = seconds == 1 ? "second." : "seconds.";
                msg = " Query returned " + featureCollection.size() + " features in " + seconds + " " + secondString;
                if (featureCollection.isEmpty()) {
                    msg = "Query didn't return any features.";
                } else if (dbQuery.collectionHasNulls()) {
                    msg = "Some query features have null geometries.";
                }
                // displays messages in OJ and plugin windows
                queryDialog.refreshUiForResult(msg);
                context.getWorkbenchFrame().warnUser(msg);

                if (this.refresh) {
                    // try to find the layer corresponding to the current query (found at cursor)
                    // and refresh its featureCollection, instead of creating a new layer.
                    for (Iterator iter = context.getLayerNamePanel().getLayerManager().getLayers().iterator(); iter.hasNext(); ) {
                        Layer l = (Layer)iter.next();
                        if (l.getDescription().equals(queryString)) {
                            l.setFeatureCollection(featureCollection);
                            break;
                        }
                    }
                } else {
                    //register this successful query to the history
                    this.queryDialog.addToHistoryList(queryString);
                    Layer layer = context.addLayer(StandardCategoryNames.WORKING, queryString, featureCollection);
                    // the description will be used to link between the layer and its query, to be able to refresh it even if its name
                    // was changed
                    layer.setDescription(queryString);
                }
        } catch (Exception ex) {
            msg = ex.getMessage();
            queryDialog.refreshUiForError(msg);
            context.getWorkbenchFrame().warnUser(msg);
        }
        queryDialog.queryProgress.setVisible(false);
    }
}
