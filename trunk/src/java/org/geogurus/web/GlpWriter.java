/*
 * GlpWriter.java
 *
 * Created on 9 octobre 2002, 10:30
 */
package org.geogurus.web;
import javax.servlet.http.*;
import java.util.*;
import org.geogurus.Datasource;
import java.sql.*;
import java.io.*;
import java.lang.*;
import java.net.URLDecoder;
import org.geogurus.tools.sql.ConPool;
import org.geogurus.tools.LogEngine;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.GeometryClass;
/**Servlet listening to listProjection.jsp page: allows a user to choose/change a projection
 * for the choosen layer (for the moment, only ESRI shapefiles and TIFF images can have a projection file
 * this servlet can only be called for valid GeometryClasses concerning the projection file
 * Writes a Glp file (geOnline projection) (same directory, same basename with glp extension)
 */
public class GlpWriter extends BaseServlet {
    protected final String listproj_jsp = "listProjection.jsp";
    /**
     * Receives user requests.
     * Request parameters are:
     * layerID=<layer id>: the unique GeometryClass identifier
     * host=<host name>: the name of the host (computer) storing this GeometryClass
     * curproj=<proj name /sub name>: the name and group of the projection
     * curparams=<proj4 list of parameters>: the proj4 list of parameters
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
debugParameters(request);       
        //Gets the current session
        HttpSession session = request.getSession(true);
        //Gets the parameters from the request received
        String layerID = request.getParameter("layerid");
        String host = request.getParameter("host");
        //Instanciate variables
        String proj = request.getParameter("curproj");   
        String projParams = request.getParameter("curparams");
        // just to propagate it to the jsp page
        String caller = request.getParameter("caller");
        
        // gets the GeometryClass whose id is layerID:
        //(code is the same as in DatasourceManager)
        GeometryClass gc = getGeometryClass(layerID, host, (Hashtable)session.getAttribute(ObjectKeys.HOST_LIST));
        
        if (gc == null) {
            // invalid layerid ?? Maybe a session problem. Should provide a clear error message to user
            LogEngine.log("Invalid layerid (" + layerID+ ")...");
            return;
        }
        
        // builds the path where projection file for the given GeometryClass will be written
        String projFile = gc.getDatasourceName();
        // check to see if path ends with a /
        if (projFile.lastIndexOf(System.getProperty("file.separator")) != projFile.length() -1) {
            // tmp modification, but value saved in other variables.
            projFile += System.getProperty("file.separator");
        }
        
        BufferedWriter in = null;
        try {
            projFile += gc.getTableName().substring(0, gc.getTableName().indexOf("."));
            projFile += ".glp";
            in = new BufferedWriter(new FileWriter(projFile));
            //Projection is stored this way : Proj / SubProj
            in.write(proj);
            in.newLine();
            // Projection parameters are stored on a new line
            in.write(projParams);
            in.newLine();
        } catch (Exception e) {
            log(e.getMessage());
        } finally {
            try {in.close();} catch (Throwable th) {}
        }
        // update gc to reflect new projection
        gc.setSRText(proj + "|" + projParams);
        
        // then redirect to the calling page, to allow modifying the projection,
        // passes back necessary parameters for the page to display correctly
        // past proj is now proj...
        // a refresh parameter is passed to indicate to refresh projection field in Quicklook page
        dispatch(request, response, listproj_jsp + "?layerid=" + layerID + "&host=" + host + "&pastproj=" + proj + "&caller=" + caller + "&refresh=true");
    }
    
    /** method to retrieve the geometryClass from the session hostList object 
     * Cycle through all datasources for the given host.
     * GeometryClass identifiers are unique
     */
    private GeometryClass getGeometryClass(String layerID, String host, Hashtable hostList) {
        Vector vec = (Vector)hostList.get(host);
        for (Iterator iter = vec.iterator(); iter.hasNext();) {
            Datasource ds = (Datasource)iter.next();
            if (ds.getDataList().get(layerID) != null) {
                // bingo, found the one
                return (GeometryClass)(ds.getDataList().get(layerID));
            }
        }
        return null;
    }
}
