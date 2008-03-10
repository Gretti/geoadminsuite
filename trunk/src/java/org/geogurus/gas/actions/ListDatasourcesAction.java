/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.gas.objects.ListHostDescriptorBean;
import org.geogurus.gas.utils.ObjectKeys;
/**
 *
 * @author GNG
 */

public class ListDatasourcesAction extends Action {
    @Override
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = null;
        Properties serverList = new Properties();
        serverList.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/serverlist.properties"));
        
        ListHostDescriptorBean listHostDescriptorBean = new ListHostDescriptorBean();
        
        for (Enumeration e = serverList.keys(); e.hasMoreElements();){
            String source = serverList.getProperty((String)e.nextElement());
            source = source.replaceAll("\\\\", "\\\\\\\\");
            HostDescriptorBean host = new HostDescriptorBean(source);
            listHostDescriptorBean.addHost(host);
        }
        
        request.getSession().setAttribute("listHostDescriptorBean", listHostDescriptorBean);
        //cleans session
        request.getSession().removeAttribute(ObjectKeys.CLASSIF_MESSAGE);
        request.getSession().removeAttribute(ObjectKeys.CLASSIF_TYPE);
        request.getSession().removeAttribute(ObjectKeys.TMP_CLASSIFICATION);
        
        forward = mapping.findForward("configServer");
        
        return forward;
    }
}