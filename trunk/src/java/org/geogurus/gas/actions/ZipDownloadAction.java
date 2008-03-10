/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.geogurus.GeometryClass;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.gas.utils.SysCommandExecutor;
import org.geogurus.tools.DataManager;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.tools.util.ZipEngine;

/**
 *
 * @author gnguessan
 */
public class ZipDownloadAction extends org.apache.struts.action.Action {

    /* forward name="success" path="" */
    private static int EXPORT_TYPE_FULL = -2;
    private static int EXPORT_TYPE_TEXT = -1;
    private static int EXPORT_TYPE_HTML = -3;

    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
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
        byte exportType = Byte.parseByte(request.getParameter("exporttype"));
        String dbName = request.getParameter("dbname");

        Hashtable layerList = usermapbean.getUserLayerList();
        // needed only for data export, not for mapfile export
        GeometryClass gc = null;
        if (currentLayer != null) {
            gc = (GeometryClass) layerList.get(currentLayer);
        }
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


        // deals with mapfile download before other actions
        // then looks at the export type and datasource type to determine if a conversion is required
        // before zipping the data:
        // conversion is required if datatype is esri or postgis and export type is postgis or shapefile,
        // respectively

        if (exportType == EXPORT_TYPE_FULL) {

            // builds the files corresponding to the map parameters i.e : map, sym, font.list, fonts for symbols (only esri_3.ttf needed)
            //and fonts used in mapfile (for labels, ...)

            files = new String[3];
            files[0] = getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map";
            files[1] = getServlet().getServletContext().getRealPath("") + "/msFiles/templates/symbols.sym";
            files[2] = getServlet().getServletContext().getRealPath("") + "/msFiles/fonts/font.list";
            fileNames = new String[3];
            fileNames[0] = "map.map";
            fileNames[1] = "/symbols/symbols.sym";
            fileNames[2] = "/fonts/font.list";
            shouldRenameFiles = false;

            //Should parse given map to get needed fonts : means parse using font, search correspondance in font.list

            //builds the zip
            zipName = "gas.zip";
            shouldRemoveFiles = false;

        } else if (exportType == EXPORT_TYPE_TEXT) {
            // try to change the download name of the mapfile, and force a download window
            // build the path to the mapfile
            File userMapfile = new File(getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map");
            // set a  binary MIME to force download window in user browser
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "filename=\"geonline.map\"");
            try {
                BufferedReader in = new BufferedReader(new FileReader(userMapfile));
                PrintWriter pwOut = new PrintWriter(response.getOutputStream());
                //disabled because file length does not correspond to response length
                //response.setContentLength((int)userMapfile.length());
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

        } else if (exportType == EXPORT_TYPE_HTML) {
            // try to change the download name of the mapfile, and force a download window
            // build the path to the mapfile
            File userMapfile = new File(getServlet().getServletContext().getRealPath("") + "/msFiles/tmpMaps/user_" + session.getId() + ".map");
            // set a  binary MIME to force download window in user browser
            response.setContentType("text/html");
            try {
                BufferedReader in = new BufferedReader(new FileReader(userMapfile));
                PrintWriter pwOut = new PrintWriter(response.getOutputStream());
                //disabled because file length does not correspond to response length
                //response.setContentLength((int)userMapfile.length());
                String l = null;
                pwOut.println("<script type=\"text/javascript\">");
                pwOut.println("function sub() {");
                pwOut.println("    Ext.Ajax.request({" +
                        "url:'submitMapfile.do'," +
                        "params: Ext.Ajax.serializeForm(document.forms['frmFullMapfile'])," +
                        "callback: function(){" +
                        "        GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()}); " +
                        "        Ext.getCmp('floatingProps').hide();" +
                        "        Ext.getCmp('floatingProps').destroy();" +
                        "    }" +
                        "});");
                pwOut.println("}");
                pwOut.println("</script>");

                pwOut.println("<form name='frmFullMapfile' action='submitMapfile.do'><textarea style='width:100%;height:100%;' name='fullMapfile' rows='" + request.getParameter("rows") + "' cols='" + request.getParameter("cols") + "'>");
                while ((l = in.readLine()) != null) {
                    pwOut.println(l);
                }
                pwOut.print("</textarea></form>");
                pwOut.flush();
                pwOut.close();
                in.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        } else if (gc.getDatasourceType() == GeometryClass.PGCLASS && exportType == GeometryClass.PGCLASS) {
            // builds SQL file before zipping
            files = generateSQLDump(gc, dbName);
            shouldRemoveFiles = true;
        } else if (gc.getDatasourceType() == GeometryClass.ESRIFILECLASS && exportType == GeometryClass.ESRIFILECLASS) {
            // builds the 3 files corresponding to the shapefile
            files = new String[3];
            files[0] = gc.getDatasourceName() + File.separator + gc.getTableName();
            // look if filename is lower or upper case
            boolean isUpper = files[0].charAt(files[0].lastIndexOf(".") + 1) == 'S';
            files[1] = files[0].substring(0, files[0].lastIndexOf(".")) + (isUpper ? ".DBF" : ".dbf");
            files[2] = files[0].substring(0, files[0].lastIndexOf(".")) + (isUpper ? ".SHX" : ".shx");

            zipName = gc.getTableName() + ".zip";
        } else if (gc.getDatasourceType() == GeometryClass.TIFFCLASS) {
            files = new String[1];
            files[0] = gc.getDatasourceName() + File.separator + gc.getTableName();
            zipName = gc.getTableName() + ".zip";
        } else if (gc.getDatasourceType() == GeometryClass.ESRIFILECLASS && exportType == GeometryClass.PGCLASS) {
            // a call to shp2pgsql is required
            files = wrapShp2Pgsql(gc, dbName, session.getId(), msg);
            zipName = gc.getTableName() + ".sql.zip";
            shouldRemoveFiles = true;
        } else if (gc.getDatasourceType() == GeometryClass.PGCLASS && exportType == GeometryClass.ESRIFILECLASS) {
            // a call to pgsql2shp is required
            files = wrapPgsql2Shp(gc, session.getId(), msg);
            zipName = gc.getTableName() + ".zip";
            shouldRemoveFiles = true;
        }

        // looks if an error message exists. If so, send a txt response to calling page:
        if (msg.length() > 0) {
            //response.setContentType("text/html");
            try {
                PrintWriter pw = response.getWriter();
                pw.print(msg.toString());
                pw.close();
                removeFiles(files);
            } catch (IOException ioe0) {
                ioe0.printStackTrace();
            }
        } else {
            System.err.println("msg IS empty");
        }

        //creating zip filename and outpustream
        response.setHeader("Content-Disposition", "filename=\"" + zipName + "\"");
        response.setContentType("application/x-zip-compressed");

        try {
            OutputStream out = response.getOutputStream();
            ByteArrayOutputStream zipout = new ByteArrayOutputStream();
            if (shouldRenameFiles) {
                fileNames = new String[files.length];
                for (int f = 0; f < files.length; f++) {
                    fileToZip = new File(files[f]);
                    fileNames[f] = fileToZip.getName();
                }
            }
            ZipEngine.zipToStream(zipout, files, fileNames);
            response.setContentLength(zipout.size());
            zipout.flush();
            zipout.writeTo(out);
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (shouldRemoveFiles) {
            removeFiles(files);
        }
        return null;

    }

    /**
     * Removes the files pointed by the paths contained in the <code>files</code> argument
     * @param files a String array of file paths
     */
    protected void removeFiles(String[] files) {
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            File f = new File(files[i]);
            if (f.isFile()) {
                //System.out.println("deleting tmp file: " + files[i]);
                f.delete();
            }
        }
    }

    /**
     * Calls the underlying shp2pgsql program to transform a shapefile type geometry class into a
     * postgis SQL script.<br>
     * MUST deal with error message, to store in the msg parameter
     *
     *@param gc the GeometryClass to dump
     *@param dbName the name of the database to load into.
     *@param msg the stringbuffer containing the error message, if any
     *@return a String[] containing the name of the generated SQL file
     */
    protected String[] wrapShp2Pgsql(GeometryClass gc, String dbName, String id, StringBuffer msg) {
        String[] res = new String[1];
        String exePath = DataManager.getProperty("SHP2PGSQL");

        if (exePath == null) {
            return res;
        }

        Process p = null;
        StringBuffer cmd = new StringBuffer(exePath);
        // path to the generated SQL file, to be zipped
        String sqlFile = getServlet().getServletContext().getRealPath("") + File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + "tmp" + id + ".sql";

        // full path to the shapefile
        String dataFile = gc.getDatasourceName() + File.separator + gc.getTableName();

        // builds the command
        cmd.append(" -s ").append(gc.getSRID()).append(" ");
        cmd.append(dataFile).append(gc.getTableName().substring(0, gc.getTableName().lastIndexOf(".")));
        cmd.append(" ");
        cmd.append(dbName);

        //System.out.println("cmd sent: " + cmd.toString());

        try {
            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(cmd.toString());
            String cmdMessage = cmdExecutor.getCommandOutput();

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(sqlFile)));
            out.println(cmdMessage);

        } catch (Exception e) {
            // must manage errors gracefully
            e.printStackTrace();
        }
        res[0] = sqlFile;
        return res;
    }

    /**
     * Calls the underlying pgsql2shp program to dump a postgis table into shapefile.
     * This methods is very close to wrapShp2Pgsql.<br>
     * Shapefile name will be the name of the geographic table
     *
     *@param gc the GeometryClass to dump
     *@param id: the session id, used to generate a unique file for this user
     *@param msg the stringbuffer containing the error message, if any
     *@return a String[] containing the name of the generated SQL file
     */
    protected String[] wrapPgsql2Shp(GeometryClass gc, String id, StringBuffer msg) {
        String[] res = new String[3];
        String exePath = DataManager.getProperty("PGSQL2SHP");

        if (exePath == null) {
            return res;
        }

        Process p = null;
        StringBuffer cmd = new StringBuffer(exePath);
        // generates the name and path of the shapefile: in the applicatin tmp directory,
        // to allow us to delete these files after zipping
        String fileName = getServlet().getServletContext().getRealPath("");
        fileName += File.separator + "msFiles" + File.separator + "tmpMaps" + File.separator + gc.getTableName() + id;

        // builds the command
        cmd.append(" -f ").append(fileName);
        cmd.append(" -h ").append(gc.getHost()).append(" -p ").append(gc.getDBPort());
        cmd.append(" -P ").append(gc.getUserPwd()).append(" -u ").append(gc.getUserName());
        cmd.append(" ").append(gc.getDatasourceName()).append(" ").append(gc.getTableName());

        System.out.println("cmd sent by wrapPgsql2Shp: " + cmd.toString());

        try {
            SysCommandExecutor cmdExecutor = new SysCommandExecutor();
            int exitStatus = cmdExecutor.runCommand(cmd.toString());
            String cmdMessage = cmdExecutor.getCommandOutput();

            // Analyses the command result: if it contains an error, saves it
            if (cmdMessage.indexOf("ERROR") != -1) {
                msg.append("<font color=red>PGSQL2SHP returned an error message:</font><br>");
                msg.append(cmdMessage.substring(cmdMessage.indexOf("ERROR")));
                msg.append("<br>This probably means that the table contains null values.<br>Export cannot be done.");
                msg.append("<p>One solution is to create a view without null values:<br>");
                msg.append("<code>create view v1 as (select * from mytable where geo_column is not null)</code>");
            }
            System.out.println("msg: " + msg);
        //            p.waitFor();
        } catch (Exception e) {
            // must manage errors gracefully
            e.printStackTrace();
        }
        // generated the name of the shapefiles
        res[0] = fileName + ".shp";
        res[1] = fileName + ".shx";
        res[2] = fileName + ".dbf";
        return res;
    }

    /**
     * Generates the SQL file corresponding to the dump of the given geomtryClass, whose type
     * is DBCLASS. THis dump is generated for a database nammed dbName
     *
     *@param gc the geometryClass to dump
     *@param dbName the name of the database for which the dump is needed.
     *@return a String[] containing the name of the sql file generated by this method
     */
    protected String[] generateSQLDump(GeometryClass gc, String dbName) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        String[] res = new String[1];
        String dataPath = "";

        try {
            // sql out file:
            dataPath = getServlet().getServletConfig().getServletContext().getRealPath("") + 
                    File.separator + 
                    "msFiles" + 
                    File.separator + 
                    "tmpMaps" + 
                    File.separator + 
                    gc.getTableName() + ".sql";
            String basename = gc.getTableName();

            BufferedWriter sout = new BufferedWriter(new FileWriter(dataPath));
            con = ConPool.getConnection(gc.getHost(), gc.getDBPort(), gc.getDatasourceName(), gc.getUserName(), gc.getUserPwd(), "postgres");
            stmt = con.createStatement();
            StringBuffer query = new StringBuffer();
            String strCreate = "create table " + basename + " (";
            String strInsert = "insert into " + basename + " values(";
            ArrayList alString = new ArrayList();
            // the number of column to fetch
            DatabaseMetaData dbm = con.getMetaData();
            rs = dbm.getColumns(null, null, basename, "%");
            query.append("select ");
            String columnName = "";
            String columnType = "";
            //Builds the query string
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
                    strCreate += rs.isFirst() ? columnName + " " + columnType : ", " + columnName + " " + columnType;
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
            strCreate += "select AddGeometryColumn('" + dbName + "','" + basename + "','the_geom','" + gc.getSRID() + "','" + gc.getOgisType() + "',2);\n";
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
                    if (rs.getMetaData().getColumnName(k).equalsIgnoreCase("astext")) {
                        if (rs.getString(k) != null) {
                            sout.write("GeometryFromText(");
                        }
                    }
                    if (rs.getString(k) != null) {
                        dblQuoted = rs.getString(k).replaceAll("'", "''");
                        sout.write("'" + dblQuoted + "'");
                        if (rs.getMetaData().getColumnName(k).equalsIgnoreCase("astext")) {
                            sout.write("," + gc.getSRID() + ")");
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
}
