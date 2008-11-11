/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.LegendGenerator;

/**
 *
 * @author GNG
 */
public class DisplayLayerPropertiesAction extends Action {
    private Logger logger = Logger.getLogger(DisplayLayerPropertiesAction.class.getName());
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;
        HttpSession session = request.getSession();
        String layerId = request.getParameter("layerID");
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        DataAccess gc = (DataAccess) umb.getUserLayerList().get(layerId);
        session.setAttribute(ObjectKeys.CURRENT_GC, gc);
        String legendMessage = null;
        //generates MapServer legend for current geometryClass Layer
        try {
            String rootPath = getServlet().getServletConfig().getServletContext().getRealPath("") + File.separator;

            LegendGenerator lg = new LegendGenerator();
            boolean legendOk = lg.setLayerLegend(
                    umb.getMapserverURL(), 
                    umb.getMapfilePath(), 
                    rootPath + "msFiles" + File.separator + "tmpMaps" + File.separator, 
                    "msFiles/tmpMaps/",
                    rootPath + "msFiles" + File.separator+ "templates" + File.separator + "legend.html", 
                    gc.getMSLayer(), 
                    rootPath + "images/empty.gif");
            if (!legendOk) {
                logger.warning(lg.getErrorResponse());
                legendMessage = lg.getErrorResponse();
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        //cleans session
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        session.setAttribute(ObjectKeys.LEGEND_MESSAGE, legendMessage);

        return forward;
    }
}