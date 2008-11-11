/*
 * DisplayHelpAction.java
 *
 * Created on 12 decembre 2006, 19:12
 *
 */
package org.geogurus.gas.actions;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

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
        //Parses server list
        Properties serverList = new Properties();
        serverList.load(getClass().getClassLoader().getResourceAsStream("org/geogurus/gas/resources/serverlist.properties"));

        ListHostDescriptorBean listHostDescriptorBean = new ListHostDescriptorBean();

        for (Enumeration e = serverList.keys(); e.hasMoreElements();) {
            String source = serverList.getProperty((String) e.nextElement());
            source = source.replaceAll("\\\\", "\\\\\\\\");
            HostDescriptorBean host = new HostDescriptorBean(source);
            listHostDescriptorBean.addHost(host);
        }
        //Stores server list in session
        request.getSession().setAttribute("listHostDescriptorBean", listHostDescriptorBean);

        //Gets I18N from bundle
        ResourceBundle bundle = null;
        try {
            Locale locale = (Locale) request.getSession().getAttribute("org.apache.struts.action.LOCALE");
            bundle = ResourceBundle.getBundle("org.geogurus.gas.resources.ApplicationResource", locale);
        } catch (NullPointerException npe) {
            bundle = ResourceBundle.getBundle("org.geogurus.gas.resources.ApplicationResource", Locale.ENGLISH);
        } catch (MissingResourceException mre) {
            bundle = ResourceBundle.getBundle("org.geogurus.gas.resources.ApplicationResource", Locale.ENGLISH);
        }
        String jsValues = "";
        String key = "";
        String value = "";
        boolean first = true;
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            key = keys.nextElement();
            value = bundle.getString(key);
            //Deals with first value
            if (first) {
                first = false;
            } else {
                jsValues += ",\n";
            }

            jsValues += "\"" + key + "\":\"" + value + "\"";
        }

        request.setAttribute("I18N", "{" + jsValues + "}");
        //Cleans session
        request.getSession().removeAttribute(ObjectKeys.CLASSIF_MESSAGE);

        forward = mapping.findForward("configServer");

        return forward;
    }
}
