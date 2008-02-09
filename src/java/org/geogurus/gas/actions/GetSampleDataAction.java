/*
 * GetSampleDataAction.java
 *
 * Created on 26 novembre 2007, 13:09
 */

package org.geogurus.gas.actions;

import java.io.Writer;
import java.util.Iterator;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.GeometryClass;
import org.geogurus.gas.objects.GeometryClassFieldBean;
import org.geogurus.web.LayerGeneralProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author gnguessan
 * @version
 */

public class GetSampleDataAction extends Action {
    private Log log = LogFactory.getLog(GetSampleDataAction.class);
    
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    public ActionForward execute(ActionMapping mapping, ActionForm  form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        //calls for records from start to start + limit received in request parameters
        LayerGeneralProperties lgp = (LayerGeneralProperties)request.getSession().getAttribute("dgp");
        
        GeometryClass gc = lgp.getGeometryClass();
        int from = Integer.valueOf(request.getParameter("start")).intValue();
        int to = from + Integer.valueOf(request.getParameter("limit")).intValue();
        
        if (log.isDebugEnabled()) log.debug("Getting sample data from " + from + " to " + to);
        
        Vector values = gc.getSampleData(from, to);
        Vector fields = gc.getColumnInfo();
        
        //writes Json object
        String totalCount = "\"totalCount\":";
        String root = "\"enregistrements\":";
        
        StringBuilder json = new StringBuilder("{");
        json.append(totalCount);
        json.append("\"");
        json.append(gc.getNumGeometries());
        json.append("\",");
        json.append(root);
        json.append("[");
        //builds structure "field":"value",...
        //parses values
        int rec =0;
        int reg =0;
        for (Iterator iteValues = values.iterator(); iteValues.hasNext();){
            Vector currow = (Vector)iteValues.next();
            for (reg = 0; reg<fields.size(); reg++) {
                //Opens new json record with "{" if first or ",{"
                if(reg == 0 && rec == 0) json.append("{");
                if(reg == 0 && rec != 0) json.append(",{");
                
                //appends with "field":"value"
                json.append("\"" + ((GeometryClassFieldBean)fields.get(reg)).getName() + "\":\"" + String.valueOf(currow.get(reg)) + "\"");
                //closes with ',' if other values left or with '}' if last one
                if(reg < fields.size() - 1) {json.append(",");} else {json.append("}");}
            }
            rec++;
        }
        json.append("]}");
        
        boolean scriptTag = false;
        
        response.setContentType("application/x-json");
        Writer out = response.getWriter();
        out.write(json.toString());
        out.flush();
        out.close();
        
        return null;
        
    }
}
