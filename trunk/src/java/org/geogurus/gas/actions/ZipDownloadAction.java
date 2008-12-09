/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import org.geogurus.data.operations.ToFeatureStoreOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.data.DataAccessType;
import org.geogurus.data.Option;
import org.geogurus.data.database.PostgisDataAccess;
import org.geogurus.data.files.ShpDataAccess;
import org.geogurus.data.files.TiffDataAccess;
import org.geogurus.gas.forms.cartoweb.IniConfigurationForm;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.gas.utils.SysCommandExecutor;
import org.geogurus.tools.DataManager;
import org.geogurus.tools.util.ZipEngine;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.postgis.Geometry;

/**
 * Action to generate Exported files and pack them into a zipFile sent back to
 * the client Either uses shp2pgsql or pgsql2shp to convert from/to shapefile
 * to/from postgis.
 * 
 * @author gnguessan
 */
public class ZipDownloadAction extends org.apache.struts.action.Action {

    Logger log = Logger.getLogger(ZipDownloadAction.class.getName());
    /* forward name="success" path="" */
    private static String EXPORT_TYPE_TEXT = "EXPORT_TYPE_TEXT";
    private static String EXPORT_TYPE_FULL = "EXPORT_TYPE_FULL";
    private static String EXPORT_TYPE_HTML = "EXPORT_TYPE_HTML";
    private static String EXPORT_TYPE_MAPFISH = "EXPORT_TYPE_MAPFISH";
    private static String EXPORT_TYPE_CARTOWEB = "EXPORT_TYPE_CARTOWEB";

    /**
     * This is the action called from the Struts framework.
     * 
     * @param mapping
     *            The ActionMapping used to select this instance.
     * @param form
     *            The optional ActionForm bean for this request.
     * @param request
     *            The HTTP Request we are processing.
     * @param response
     *            The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession(true);
        UserMapBean usermapbean = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        // the current layer identifier
        String currentLayer = request.getParameter("currentlayer");
        String exportType = request.getParameter("exporttype");
        String dbName = request.getParameter("dbname");

        Hashtable<String, DataAccess> layerList = usermapbean.getUserLayerList();
        // needed only for data export, not for mapfile export
        DataAccess gc = null;
        if (currentLayer != null) {
            gc = (DataAccess) layerList.get(currentLayer);
        }
        ZipDownloadParams params = new ZipDownloadParams();
        // deals with mapfile download before other actions
        // then looks at the export type and datasource type to determine if a
        // conversion is required
        // before zipping the data:
        // conversion is required if datatype is esri or postgis and export type
        // is postgis or shapefile,
        // respectively, or export type is cartoweb, then only the .INI files
        // will be exported

        if (exportType.equals(EXPORT_TYPE_CARTOWEB)) {
            exportToCartoWeb(request, params);
        } else if (exportType.equals(EXPORT_TYPE_FULL)) {
            exportFull(session, params);
        } else if (exportType.equals(EXPORT_TYPE_TEXT)) {
            return exportToText(response, session);
        } else if (exportType.equals(EXPORT_TYPE_HTML)) {
            return exportToHTML(request, response, session);
        } else if (gc instanceof PostgisDataAccess && exportType.equals(DataAccessType.POSTGIS.name())) {
            // builds SQL file before zipping
            params.files = generateSQLDump(gc, dbName);
            params.shouldRemoveFiles = true;
        } else if (gc instanceof ShpDataAccess && exportType.equals(DataAccessType.SHP.name())) {
            // builds the 3 files corresponding to the shapefile
            params.files = new String[3];
            params.files[0] = gc.resource(File.class).get().getName();
            // look if filename is lower or upper case
            boolean isUpper = params.files[0].charAt(params.files[0].lastIndexOf(".") + 1) == 'S';
            params.files[1] = params.files[0].substring(0, params.files[0].lastIndexOf(".")) + (isUpper ? ".DBF" : ".dbf");
            params.files[2] = params.files[0].substring(0, params.files[0].lastIndexOf(".")) + (isUpper ? ".SHX" : ".shx");

            params.zipName = gc.getName() + ".zip";
        } else if (gc instanceof TiffDataAccess) {
            params.files = new String[1];
            params.files[0] = gc.resource(File.class).get().getAbsolutePath();
            params.zipName = gc.getName() + ".zip";
        } else if (gc instanceof ShpDataAccess && exportType.equals(DataAccessType.POSTGIS.name())) {
            // a call to shp2pgsql is required
            params.files = wrapShp2Pgsql(gc, dbName, session.getId(),
                    params.msg);
            params.zipName = gc.getName() + ".sql.zip";
            params.shouldRemoveFiles = true;
        } else if (gc instanceof PostgisDataAccess && exportType.equals(DataAccessType.SHP.name())) {
            // a call to pgsql2shp is required
            params.files = Pgsql2Shp(gc, session.getId(), params.msg);
            params.zipName = gc.getName() + ".zip";
            params.shouldRemoveFiles = true;
        }

        // looks if an error message exists. If so, send a txt response to
        // calling page:
        if (params.msg.length() > 0) {
            // response.setContentType("text/html");
            try {
                PrintWriter pw = response.getWriter();
                pw.print(params.msg.toString());
                pw.close();
                removeFiles(params.files);
            } catch (IOException ioe0) {
                ioe0.printStackTrace();
            }
        } else {
            log.fine("msg IS empty");
        }

        // creating zip filename and outpustream
        response.setHeader("Content-Disposition", "filename=\"" + params.zipName + "\"");
        response.setContentType("application/x-zip-compressed");

        try {
            OutputStream out = response.getOutputStream();
            // ByteArrayOutputStream zipout = new ByteArrayOutputStream();
            if (params.shouldRenameFiles) {
                params.fileNames = new String[params.files.length];
                for (int f = 0; f < params.files.length; f++) {
                    params.fileToZip = new File(params.files[f]);
                    params.fileNames[f] = params.fileToZip.getName();
                }
            }
            // ZipEngine.zipToStream(zipout, params.files, params.fileNames);
            ZipEngine.zipToStream(out, params.files, params.fileNames);
            // zipout.flush();
            // zipout.writeTo(out);
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (params.shouldRemoveFiles) {
            removeFiles(params.files);
        }

        return null;
    }

    /**
     * @param request
     * @param response
     * @param session
     * @return
     */
    private ActionForward exportToHTML(HttpServletRequest request,
            HttpServletResponse response, HttpSession session) {
        // try to change the download name of the mapfile, and force a
        // download window
        // build the path to the mapfile
        File userMapfile = new File(getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map");
        // set a binary MIME to force download window in user browser
        response.setContentType("text/html");
        try {
            BufferedReader in = new BufferedReader(new FileReader(userMapfile));
            PrintWriter pwOut = new PrintWriter(response.getOutputStream());
            // disabled because file length does not correspond to response
            // length
            // response.setContentLength((int)userMapfile.length());
            String l = null;
            pwOut.println("<script type=\"text/javascript\">");
            pwOut.println("function sub() {");
            pwOut.println("    Ext.Ajax.request({\n" +
                    "        url:'submitMapfile.do',\n" +
                    "        params: {fullMapfile:Ext.getCmp('txtFullMapfile').value},\n" +
                    "        callback: function(){\n" +
                    "            GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});\n" +
                    "            Ext.getCmp('floatingProps').destroy();\n" +
                    "        }\n" +
                    "    });\n");
            pwOut.println("}");
            StringBuilder fullMapFile = new StringBuilder();
            while ((l = in.readLine()) != null) {
                l = l.replaceAll("\\\\", "/");
                fullMapFile.append(l+"\\n");
            }
            pwOut.println("var formProps = new Ext.form.FormPanel({\n" +
                    "    id: 'frmFullMapfile',\n" +
                    "    baseCls: 'x-plain',\n" +
                    "    labelWidth: 55,\n" +
                    "    items: [{\n" +
                    "        id: 'txtFullMapfile',\n" +
                    "        xtype: 'textarea',\n" +
                    "        hideLabel: true,\n" +
                    "        name: 'fullMapfile',\n" +
                    "        anchor: '100%',\n" +
                    "        value:'" + fullMapFile.toString() + "'\n" +
                    "    }]\n" +
                    "});\n");
            pwOut.println("Ext.getCmp('contentProps').add(formProps);");
            pwOut.println("Ext.getCmp('contentProps').doLayout();");
            pwOut.println("Ext.getCmp('txtFullMapfile').setHeight(Ext.getCmp('contentProps').body.getHeight() * 0.98);");
            pwOut.println("</script>");

//            pwOut.println("<form name='frmFullMapfile' action='submitMapfile.do'><textarea style='width:100%;height:100%;' name='fullMapfile' rows='" + request.getParameter("rows") + "' cols='" + request.getParameter("cols") + "'>");
//            while ((l = in.readLine()) != null) {
//                pwOut.println(l);
//            }
//            pwOut.print("</textarea></form>");
            pwOut.flush();
            pwOut.close();
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    /**
     * @param response
     * @param session
     * @return
     */
    private ActionForward exportToText(HttpServletResponse response,
            HttpSession session) {
        // try to change the download name of the mapfile, and force a
        // download window
        // build the path to the mapfile
        File userMapfile = new File(getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map");
        // set a binary MIME to force download window in user browser
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "filename=\"geonline.map\"");
        try {
            BufferedReader in = new BufferedReader(new FileReader(userMapfile));
            PrintWriter pwOut = new PrintWriter(response.getOutputStream());
            // disabled because file length does not correspond to response
            // length
            // response.setContentLength((int)userMapfile.length());
            String l = null;
            while ((l = in.readLine()) != null) {
                pwOut.println(l);
            }
            pwOut.flush();
            pwOut.close();
            in.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return null;
    }

    /**
     * @param session
     * @param params
     */
    private void exportFull(HttpSession session, ZipDownloadParams params) {
        // builds the files corresponding to the map parameters i.e : map,
        // sym, font.list, fonts for symbols (only esri_3.ttf needed)
        // and fonts used in mapfile (for labels, ...)

        params.files = new String[3];
        params.files[0] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map";
        params.files[1] = getServlet().getServletContext().getRealPath("") + "/msFiles/templates/symbols.sym";
        params.files[2] = getServlet().getServletContext().getRealPath("") + "/msFiles/fonts/font.list";
        params.fileNames = new String[3];
        params.fileNames[0] = "map.map";
        params.fileNames[1] = "/symbols/symbols.sym";
        params.fileNames[2] = "/fonts/font.list";
        params.shouldRenameFiles = false;

        // FIXME : Should parse given map to get needed fonts : means parse
        // using font, search correspondance in font.list

        // builds the zip
        params.zipName = "gas.zip";
    }

    /**
     * @param request
     * @param params
     */
    private void exportToCartoWeb(HttpServletRequest request,
            ZipDownloadParams params) {
        // builds the files corresponding to the Cartoweb3 INI configuration
        // (4 files)
        IniConfigurationForm cwIniConf = request.getSession().getAttribute(
                ObjectKeys.CW_INI_CONF_BEAN) == null ? new IniConfigurationForm()
                : (IniConfigurationForm) request.getSession().getAttribute(
                ObjectKeys.CW_INI_CONF_BEAN);

        params.files = new String[4];
        params.files[0] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/location.ini";
        if (!cwIniConf.getLocationConf().saveAsFile(new File(params.files[0]))) {
            log.warning("cannot save ini file: " + params.files[0]);
        }
        params.files[1] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/images.ini";
        if (!cwIniConf.getImagesConf().saveAsFile(new File(params.files[1]))) {
            log.warning("cannot save ini file: " + params.files[1]);
        }
        params.files[2] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/layers.ini";
        if (!cwIniConf.getLayerConf().saveAsFile(new File(params.files[2]))) {
            log.warning("cannot save ini file: " + params.files[2]);
        }
        params.files[3] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/query.ini";
        if (!cwIniConf.getQueryConf().saveAsFile(new File(params.files[3]))) {
            log.warning("cannot save ini file: " + params.files[3]);
        }
        params.fileNames = new String[4];
        params.fileNames[0] = "server_side/location.ini";
        params.fileNames[1] = "client_side/images.ini";
        params.fileNames[2] = "client_side/layers.ini";
        params.fileNames[3] = "client_side/query.ini";
        params.zipName = "cartoweb_conf.zip";
        params.shouldRemoveFiles = true;
    }

    /**
     * Removes the files pointed by the paths contained in the
     * <code>files</code> argument
     * 
     * @param files
     *            a String array of file paths
     */
    protected void removeFiles(String[] files) {
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File f = new File(files[i]);
            if (f.isFile()) {
                // System.out.println("deleting tmp file: " + files[i]);
                f.delete();
            }
        }
    }

    /**
     * Calls the underlying shp2pgsql program to transform a shapefile type
     * geometry class into a postgis SQL script.<br>
     * MUST deal with error message, to store in the msg parameter
     * 
     *@param gc
     *            the GeometryClass to dump
     *@param dbName
     *            the name of the database to load into.
     *@param msg
     *            the stringbuffer containing the error message, if any
     *@return a String[] containing the name of the generated SQL file
     */
    protected String[] wrapShp2Pgsql(DataAccess gc, String dbName, String id,
            StringBuffer msg) {
        String[] res = new String[1];
        String exePath = DataManager.getProperty("SHP2PGSQL");

        if (exePath == null) {
            return res;
        }

        StringBuffer cmd = new StringBuffer(exePath);
        // path to the generated SQL file, to be zipped
        String sqlFile = getServlet().getServletContext().getRealPath("") + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + "tmp" + id + ".sql";

        // full path to the shapefile
        String dataFile = gc.resource(File.class).get().getAbsolutePath();

        // builds the command
        cmd.append(" -s ").append(gc.getSrid()).append(" ");
        cmd.append(dataFile);
        cmd.append(" ");
        cmd.append(dbName);

        // System.out.println("cmd sent: " + cmd.toString());

        try {
            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(cmd.toString());
            String cmdMessage = cmdExecutor.getCommandOutput();

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(sqlFile)));
            out.println(cmdMessage);

        } catch (Exception e) {
            // must manage errors gracefully
            e.printStackTrace();
        }
        res[0] = sqlFile;
        return res;
    }

    enum ShpExt {

        SHP, DBF, SHX, PRJ, QIX, FIX
    }

    /**
     * Calls the underlying pgsql2shp program to dump a postgis table into
     * shapefile. This methods is very close to wrapShp2Pgsql.<br>
     * Shapefile name will be the name of the geographic table
     * 
     *@param gc
     *            the GeometryClass to dump
     *@param id
     *            : the session id, used to generate a unique file for this user
     *@param msg
     *            the stringbuffer containing the error message, if any
     *@return a String[] containing the name of the generated SQL file
     */
    protected String[] Pgsql2Shp(DataAccess gc, String id, StringBuffer msg) {
        String shpFileName = getServlet().getServletContext().getRealPath("");
        String name = gc.getName() + id;
        shpFileName += File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + name;

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(name);
        builder.init(gc.featureType().get());

        ShapefileDataStore ds;
        try {
            ds = new ShapefileDataStore(new URL(shpFileName + ".shp"));
            ds.createSchema(builder.buildFeatureType());

            FeatureStore<SimpleFeatureType, SimpleFeature> featureSource = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource();
            ToFeatureStoreOp op = new ToFeatureStoreOp();

            gc.run(op, featureSource, Query.ALL);

            List<String> files = new ArrayList<String>();
            for (ShpExt rawExt : ShpExt.values()) {
                String ext = "." + rawExt.name().toLowerCase();
                String fileName = shpFileName + ext;
                if (new File(fileName).exists()) {
                    files.add(fileName);
                }
            }

            return files.toArray(new String[files.size()]);
        } catch (IOException e) {
            e.printStackTrace();
            msg.append("Error writing shapefiles from Postgis database: " + e.getMessage());
        }
        return new String[3];

    // String[] res = new String[3];
    // String exePath = DataManager.getProperty("PGSQL2SHP");
    //
    // if (exePath == null) {
    // return res;
    // }
    //
    // Process p = null;
    // StringBuffer cmd = new StringBuffer(exePath);
    // // generates the name and path of the shapefile: in the applicatin
    // tmp
    // // directory,
    // // to allow us to delete these files after zipping
    // String fileName = getServlet().getServletContext().getRealPath("");
    // fileName += File.separator + "msFiles" + File.separator + "tmpMaps"
    // + File.separator + gc.getTableName() + id;
    //
    // // builds the command
    // cmd.append(" -f ").append(fileName);
    // cmd.append(" -h ").append(gc.getHost()).append(" -p ").append(
    // gc.getDBPort());
    // cmd.append(" -P ").append(gc.getUserPwd()).append(" -u ").append(
    // gc.getUserName());
    // cmd.append(" ").append(gc.getDatasourceName()).append(" ").append(
    // gc.getTableName());
    //
    // log.fine("cmd sent by wrapPgsql2Shp: " + cmd.toString());
    //
    // try {
    // SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    // int exitStatus = cmdExecutor.runCommand(cmd.toString());
    // String cmdMessage = cmdExecutor.getCommandOutput();
    //
    // // Analyses the command result: if it contains an error, saves it
    // if (cmdMessage.indexOf("ERROR") != -1) {
    // msg
    // .append(
    // "<font color=red>PGSQL2SHP returned an error message:</font><br>");
    // msg.append(cmdMessage.substring(cmdMessage.indexOf("ERROR")));
    // msg
    // .append(
    // "<br>This probably means that the table contains null values.<br>Export cannot be done."
    // );
    // msg
    //.append("<p>One solution is to create a view without null values:<br>"
    // );
    // msg
    // .append(
    // "<code>create view v1 as (select * from mytable where geo_column is not null)</code>"
    // );
    // }
    // log.warning("msg: " + msg);
    // // p.waitFor();
    // } catch (Exception e) {
    // // must manage errors gracefully
    // e.printStackTrace();
    // }
    // // Nicolas 28/03/2008: if GC has a SRText value, writes a .prj file
    // if (gc.getSRText() != null && gc.getSRText().length() > 0) {
    // try {
    // BufferedWriter br = new BufferedWriter(new FileWriter(fileName
    // + ".prj"));
    // br.write(gc.getSRText());
    // br.close();
    // res = new String[4];
    // res[3] = fileName + ".prj";
    // } catch (IOException ioe) {
    // log.severe(ioe.getMessage());
    // }
    //
    // }
    // res[0] = fileName + ".shp";
    // res[1] = fileName + ".shx";
    // res[2] = fileName + ".dbf";
    // return res;
    }

    /**
     * Generates the SQL file corresponding to the dump of the given
     * geomtryClass, whose type is DBCLASS. THis dump is generated for a
     * database nammed dbName
     * 
     *@param da
     *            the DataAccess to dump
     *@param dbName
     *            the name of the database for which the dump is needed.
     *@return a String[] containing the name of the sql file generated by this
     *         method
     */
    protected String[] generateSQLDump(DataAccess da, String dbName) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String[] res = new String[1];
        String dataPath = "";

        try {
            // sql out file:
            PostgisDataAccess gc = (PostgisDataAccess) da;
            String tableName = gc.getName();
            dataPath = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + tableName + ".sql";
            String basename = tableName;

            BufferedWriter sout = new BufferedWriter(new FileWriter(dataPath));
            Option<Connection> option = gc.resource(Connection.class);
            if (option.isNone()) {
                return res;
            }
            con = option.get();
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer();
            String strCreate = "create table " + basename + " (";
            String strInsert = "insert into " + basename + " values(";
            ArrayList<String> alString = new ArrayList<String>();
            // the number of column to fetch
            DatabaseMetaData dbm = con.getMetaData();
            rs = dbm.getColumns(null, null, basename, "%");
            query.append("select ");
            String columnName = "";
            String columnType = "";
            // Builds the query string
            int f = 1;
            while (rs.next()) {
                columnName = rs.getString(4);
                columnType = rs.getString(6);
                if (columnType.equalsIgnoreCase("geometry")) {
                    if (rs.isLast()) {
                        query.append("astext(" + columnName + ")");
                    } else {
                        query.append("astext(" + columnName + "), ");
                    }
                } else {
                    strCreate += rs.isFirst() ? columnName + " " + columnType
                            : ", " + columnName + " " + columnType;
                    if (rs.isLast()) {
                        query.append(columnName);
                    } else {
                        query.append(columnName + ", ");
                    }
                }
                if (columnType.equalsIgnoreCase("varchar") || columnType.equalsIgnoreCase("text") || columnType.equalsIgnoreCase("char")) {
                    alString.add("" + f);
                }
                f++;
            }
            rs = null;
            strCreate += ");\n";
            strCreate += "select AddGeometryColumn('" + dbName + "','" + basename + "','the_geom','" + gc.getSrid() + "','" + Geometry.getTypeString(gc.getType()) + "',2);\n";
            sout.write(strCreate);
            sout.write("begin;");
            sout.newLine();
            query.append(" from ");
            query.append(gc.getName());
            rs = stmt.executeQuery(query.toString());
            String dblQuoted = "";
            while (rs.next()) {
                sout.write(strInsert);
                for (int k = 1; k <= rs.getMetaData().getColumnCount(); k++) {
                    if (rs.getMetaData().getColumnName(k).equalsIgnoreCase(
                            "astext")) {
                        if (rs.getString(k) != null) {
                            sout.write("GeometryFromText(");
                        }
                    }
                    if (rs.getString(k) != null) {
                        dblQuoted = rs.getString(k).replaceAll("'", "''");
                        sout.write("'" + dblQuoted + "'");
                        if (rs.getMetaData().getColumnName(k).equalsIgnoreCase(
                                "astext")) {
                            sout.write("," + gc.getSrid() + ")");
                        }
                        if (k != rs.getMetaData().getColumnCount()) {
                            sout.write(",");
                        }
                    }
                }
                sout.write(" );");
                sout.newLine();
            }
            sout.write("end;");
            sout.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        res[0] = dataPath;
        return res;
    }

    private static class ZipDownloadParams {
        // The list of filenames to zip

        String[] files = null;
        String[] fileNames = null;
        String zipName = null;
        File fileToZip = null;
        boolean shouldRenameFiles = true;
        // should remove temporary files ?
        boolean shouldRemoveFiles = false;
        // the export error message, if any
        StringBuffer msg = new StringBuffer();
    }
}
