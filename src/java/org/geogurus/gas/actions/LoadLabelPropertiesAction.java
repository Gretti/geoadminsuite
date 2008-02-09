/*
 * LoadLabelPropertiesAction.java
 *
 * Created on 27 fevrier 2007
 *
 */
package org.geogurus.gas.actions;

import com.sun.java.util.collections.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.geogurus.GeometryClass;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Label;
/**
 *
 * @author GNG
 */

public class LoadLabelPropertiesAction extends Action {
    public ActionForward execute(ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        ActionForward forward = null;
        HttpSession session = request.getSession();
        
        GeometryClass gc = (GeometryClass)session.getAttribute(ObjectKeys.CURRENT_GC);
        Label lab = gc.getDefaultMsLayer().getMapClass().getFirstClass().getLabel();
        
        if(lab == null) {
            gc.getDefaultMsLayer().getMapClass().getFirstClass().setLabel(new Label());
        }
        
        if(session.getAttribute("fontList") == null) {
            // find the list of available fonts from font.list GAS file
            try {
                ArrayList fontList = new ArrayList();
                String fontFile = getServlet().getServletContext().getRealPath("");
                
                // check to see if path ends with a /
                if (!fontFile.endsWith(File.separator)) {
                    // tmp modiction, but value saved in other variables.
                    fontFile += File.separator;
                }
                fontFile += "msFiles";
                fontFile += File.separator;
                fontFile += "fonts";
                fontFile += File.separator;
                fontFile += "font.list";
                BufferedReader in = new BufferedReader(new FileReader(fontFile));
                String s = "";
                StringTokenizer tk = null;
                while ((s = in.readLine()) != null) {
                    tk = new StringTokenizer(s);
                    String s2 = tk.nextToken();
                    fontList.add(s2);
                    tk.nextToken();
                }
                in.close();
                session.setAttribute("fontList", fontList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        forward = mapping.findForward("labelProperties");
        
        return forward;
    }
}