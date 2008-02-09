/*
 * LayerPropertiesAction.java
 *
 * Created on 5 fevrier 2007, 22:58
 */

package org.geogurus.gas.actions;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForward;
import org.geogurus.gas.forms.MapForm;
import org.geogurus.gas.managers.UserMapBeanManager;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.MSExtent;
import org.geogurus.mapserver.objects.Map;
import org.geogurus.mapserver.objects.RGB;
import org.geogurus.mapserver.objects.SymbolSet;
import org.geogurus.web.ColorGenerator;
/**
 *
 * @author Administrateur
 * @version
 */

public class MapPropertiesAction extends Action {
    
    /* forward name="success" path="" */
    private final static String SUCCESS = "mapProperties";
    
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
        
        HttpSession session = request.getSession();
        MapForm mapForm = (MapForm)form;
        UserMapBean umb = (UserMapBean)session.getAttribute(ObjectKeys.USER_MAP_BEAN);
        Boolean sizeChanged = new Boolean(false);
        
        if (mapForm.getExtent().length() > 0 && mapForm.getExtent().split(" ").length == 4){
            StringTokenizer tok = new StringTokenizer(mapForm.getExtent());
            umb.getMapfile().setExtent(new MSExtent(new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue(),
                    new Double(tok.nextToken()).doubleValue()));
        }
        
        umb.getMapfile().setImageType(mapForm.getImageType().byteValue());
        umb.getMapfile().setImageQuality(mapForm.getImageQuality().byteValue());
        
        umb.getMapfile().setImageColor(new RGB(mapForm.getImageColor()));
        
        umb.getMapfile().setInterlace(mapForm.getInterlace().byteValue());
        if (mapForm.getName().length() > 0) umb.getMapfile().setName(mapForm.getName());
        if(umb.getMapfile().getSize().getWidth() != mapForm.getWidth().doubleValue() ||
                umb.getMapfile().getSize().getHeight() != mapForm.getHeight().doubleValue()){
            umb.getMapfile().setSize(new Dimension(mapForm.getWidth().intValue(),mapForm.getHeight().intValue()));
            sizeChanged = new Boolean(true);
        }
        umb.getMapfile().setResolution(mapForm.getResolution().intValue());
        umb.getMapfile().setScale(mapForm.getScale().doubleValue());
        umb.getMapfile().setStatus(mapForm.getStatus().byteValue());
        
        if(new File(mapForm.getFontSet()).exists()) umb.getMapfile().setFontSet(new File(mapForm.getFontSet()));
        if(new File(mapForm.getShapePath()).exists()) umb.getMapfile().setShapePath(new File(mapForm.getShapePath()));
        
        SymbolSet symbolSet = new SymbolSet();
        try{
            BufferedReader br = new BufferedReader(new FileReader(new File(mapForm.getCanonicalPath())));
            symbolSet.load(br);
            umb.getMapfile().setSymbolSet(symbolSet);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        umb.getMapfile().setTransparent(mapForm.getTransparent().byteValue());
        umb.getMapfile().setUnits(mapForm.getUnits().byteValue());
        
        UserMapBeanManager manager = new UserMapBeanManager();
        manager.setUserMapBean(umb);
        manager.generateUserMapfile((ColorGenerator)session.getAttribute(ObjectKeys.COLOR_GENERATOR));
        
        request.setAttribute(ObjectKeys.REFRESH_KEY,ObjectKeys.REFRESH_KEY);
        request.setAttribute(ObjectKeys.MAP_SIZE_CHANGED,sizeChanged);
        return mapping.findForward(SUCCESS);
        
    }
}
