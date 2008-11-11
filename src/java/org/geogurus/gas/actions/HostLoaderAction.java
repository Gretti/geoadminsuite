/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.data.Datasource;
import org.geogurus.gas.managers.DatasourceManager;
import org.geogurus.gas.objects.HostDescriptorBean;
import org.geogurus.gas.objects.ListHostDescriptorBean;
import org.geogurus.gas.utils.BeanPropertyUtil;
import org.geogurus.gas.utils.ObjectKeys;

/**
 * 
 * @author GNG
 */
public class HostLoaderAction extends Action {

    transient Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession();

        ActionForward forward = null;
        /*
         * V1 ListHostDescriptorForm lhdf = (ListHostDescriptorForm) form;
         * ListHostDescriptorBean listHost = lhdf.getListHost();
         */
        ListHostDescriptorBean listHostDescriptor = new ListHostDescriptorBean();

        Hashtable<String, Vector<Datasource>> hostList = new Hashtable<String, Vector<Datasource>>();
        Hashtable<String, HostDescriptorBean> listHost = new Hashtable<String, HostDescriptorBean>();
        Vector<Datasource> datasources = null;
        HostDescriptorBean host = null;

        // Parses request parameters
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String nam = (String) en.nextElement();
            String[] vals = request.getParameterValues(nam);

            String curHostIndex = nam.substring(nam.indexOf("[") + 1, nam
                    .indexOf("]"));
            String curParam = nam.substring(nam.indexOf(".") + 1);
            if (listHost.keySet().contains(curHostIndex)) {
                host = (HostDescriptorBean) listHost.get(curHostIndex);
            } else {
                host = new HostDescriptorBean();
                listHost.put(curHostIndex, host);
            }
            try {
                // horrible hack to change recurse type
                BeanPropertyUtil.getSetter(curParam, host, vals);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // List is complete, must update session content to avoid losing list
        for (Iterator<HostDescriptorBean> i = listHost.values().iterator(); i
                .hasNext();) {
            listHostDescriptor.addHost(i.next());
        }

        // for (int i=0; i < listHost.getNbHosts(); i++) {
        for (Iterator<String> iteHost = listHost.keySet().iterator(); iteHost
                .hasNext();) {

            // host = listHost.getHost(i);
            host = (HostDescriptorBean) listHost.get(iteHost.next());

            datasources = DatasourceManager.getInstance().getDatasourcesM(host);

            if (datasources != null) {
                for (Datasource datasource : datasources) {

                    String dsName = datasource.getHost();
                    if (hostList.get(dsName) != null) {
                        // a list of datasources already exists for this host:
                        // add the new list to the existing one
                        hostList.get(dsName).add(datasource);
                    } else {
                        // cannot add a null object in hashtable skip it
                        Vector<Datasource> list = new Vector<Datasource>();
                        list.add(datasource);
                        hostList.put(dsName, list);
                    }
                }
            }
        }

        session.setAttribute("listHostDescriptorBean", listHostDescriptor);
        session.setAttribute(ObjectKeys.HOST_LIST, hostList);
        // cleans session
        session.removeAttribute(ObjectKeys.CLASSIF_MESSAGE);

        // forward = mapping.findForward("mapgenerator");
        forward = mapping.findForward("mapCatalog");

        return forward;
    }
}
