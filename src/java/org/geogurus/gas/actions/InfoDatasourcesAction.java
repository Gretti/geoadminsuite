/*
 * DisplayHelpAction.java
 *
 * Created on 12 d�cembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.Datasource;
import org.geogurus.gas.objects.LayerGeneralProperties;
import org.geogurus.gas.utils.ObjectKeys;

/**
 *
 * @author GNG
 */
public class InfoDatasourcesAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {

        ActionForward forward = null;

        HttpSession session = request.getSession();
        Hashtable<String, Vector<Datasource>> hl = (Hashtable<String, Vector<Datasource>>) session.getAttribute(ObjectKeys.HOST_LIST);
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

        request.getSession().setAttribute(ObjectKeys.LAYER_GENERAL_PROPERTIES, lgp);

        forward = mapping.findForward("mapCatalogLayerDetail");

        return forward;
    }
}
