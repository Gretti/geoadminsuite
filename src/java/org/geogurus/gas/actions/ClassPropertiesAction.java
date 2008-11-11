/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.actions;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.SymbologyBean;
import org.geogurus.gas.objects.SymbologyListBean;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.LegendGenerator;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Symbol;
/**
 * Action dealing with MS class representation.
 * Reads the class parameters from request direcly object
 * @author nicolas
 */
public class ClassPropertiesAction extends org.apache.struts.action.Action {
    
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
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        HttpSession session = request.getSession();
        DataAccess gc = (DataAccess) session.getAttribute(ObjectKeys.CURRENT_GC);
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        
        String symbologyId = request.getParameter("selectedSymbologyId");
        String color = request.getParameter("classColor");
        String bgColor = request.getParameter("classBGColor");
        String olColor = request.getParameter("classOLColor");
        String message = "";
        int classId = Integer.parseInt(request.getParameter("selectedClassId"));
        Class cl = null;
        String classIcon = "";
        LegendGenerator lg = new LegendGenerator();
        cl = gc.getDefaultMsLayer().getMapClass().getClassById(classId);
        if (cl == null) {
            message = "cannot find class for id: " + classId;
        } else {
            // color may be null (no fill for this layer)
            if (color != null && color.length() > 0) {
                cl.setColor(new org.geogurus.mapserver.objects.RGB(color));
            } else {
                cl.setColor(null);
            }
            if (olColor != null && olColor.length() > 0) {
                cl.setOutlineColor(new org.geogurus.mapserver.objects.RGB(olColor));
            } else {
                cl.setOutlineColor(null);
            }
            if (bgColor != null && bgColor.length() > 0) {
                cl.setBackgroundColor(new org.geogurus.mapserver.objects.RGB(bgColor));
            } else {
                cl.setBackgroundColor(null);
            }
            if (symbologyId == null) {
                message = "missing symbology id (expected HTTP parameters is: selectedSymbologyId)";
            } else  {
                if (symbologyId.length() == 0) {
                    //removes class symbol by using its name.
                    // todo: stores existing symbols to be able to remove it
                    //Symbol oldSymbol = new Symbol();
                    //oldSymbol.setName(cl.getSymbol());
                    //umb.getMapfile().getSymbolSet().removeSymbol(oldSymbol);
                    cl.setSymbol(null);
                } else {
                    SymbologyListBean symbologyList = (SymbologyListBean) getServlet().getServletContext().getAttribute(ObjectKeys.GAS_SYMBOL_LIST);
                    SymbologyBean sym = symbologyList.getSymbolList().get(symbologyId);
                    if (sym == null) {
                        message = "cannot get Symbology for id: " + symbologyId;
                    } else {
                        cl.setSize(sym.getSize());
                        Symbol s = sym.getSymbol();
                        if (s == null) {
                            message = "Symbology " + symbologyId + " does not have a MapServer symbol. Check symbols.sym and symbology.properties configuration files";
                        } else {
                            umb.getMapfile().getSymbolSet().addSymbol(sym.getSymbol());
                            cl.setSymbol(sym.getSymbol().getName());
                        }
                    }
                }
            }
            
            //Build mapserver icon for this class
            //Updates and Rewrites user map
            umb.getUserLayerList().put(gc.getID(), gc);

            UserMapBeanManager manager = new UserMapBeanManager();
            manager.setUserMapBean(umb);
            manager.writeMapFile();
            //generates MapServer legend for current geometryClass Layer and mapfile
            try {
                String rootPath = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator;

                boolean legendOk = lg.setLayerLegend(
                        umb.getMapserverURL(), 
                        umb.getMapfilePath(), 
                        rootPath + "msFiles" + File.separator + "tmpMaps" + File.separator, 
                        "msFiles/tmpMaps/",
                        rootPath + "msFiles" + File.separator+ "templates" + File.separator + "legend.html", 
                        gc.getMSLayer(), 
                        rootPath + "images/empty.gif");
                if (!legendOk) {
                    message = lg.getErrorResponse();
                }
            } catch (Exception e) {
                message = e.getMessage();
            }
            classIcon = cl.getLegendURL();
        }
        StringBuilder json = new StringBuilder("{'classIcon':'").append(classIcon).append("'");
        json.append(", 'message':'");
        json.append(message);
        json.append("'}");
        //response.setContentType("application/x-json");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
        return null;
    }
}