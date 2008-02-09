/*
 * DisplayHelpAction.java
 *
 * Created on 12 d�cembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import org.geogurus.web.LayerGeneralProperties;
import org.geogurus.gas.utils.ObjectKeys;
import java.io.IOException;
import java.util.Hashtable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author GNG
 */
public class InfoDatasourcesAction extends Action {

    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;

        HttpSession session = request.getSession();
        Hashtable hl = (Hashtable) session.getAttribute(ObjectKeys.HOST_LIST);
        String rp = getServlet().getServletConfig().getServletContext().getRealPath("");
        String hostName = request.getParameter("host");
        String layerID = request.getParameter("layerID");

        LayerGeneralProperties lgp = new LayerGeneralProperties();
        lgp.setImgX(250);
        lgp.setImgY(250);
        lgp.setHostList(hl);
        lgp.setHost(hostName);
        lgp.setSessionID(session.getId());
        lgp.setRootPath(rp);
        lgp.setLayerID(layerID);

        request.getSession().setAttribute("dgp", lgp);

        forward = mapping.findForward("mapCatalogLayerDetail");

        return forward;
    }
}
