/*
 * KaboumPropertiesForm.java
 *
 * Created on April 2, 2007, 5:56 PM
 */

package org.geogurus.gas.forms;

import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.geogurus.gas.objects.KaboumBean;

/**
 *
 * @author nicolas
 * @version
 */

public class KaboumPropertiesForm extends org.apache.struts.action.ActionForm {
    
    private KaboumBean kaboumBean;
    
    /**
     * @return KaboumBean
     */
    public KaboumBean getKaboumBean() {
        return kaboumBean;
    }
    
    /**
     * @param bean the kaboum bean to set
     */
    public void setKaboumBean(KaboumBean bean) {
        kaboumBean = bean;
    }
    
    /**
     *
     */
    public KaboumPropertiesForm() {
        super();
        // TODO Auto-generated constructor stub
    }
    /**
     * Creates a new object with the given kaboum bean
     */
    public KaboumPropertiesForm(KaboumBean kb) {
        super();
        // TODO Auto-generated constructor stub
        System.out.println("setting kaboum bean from ctor");
        this.setKaboumBean(kb);
    }

    /*
    
    public void reset(ActionMapping mapping, ServletRequest request) {
        System.out.println("setting bean in reset...");
        HttpServletRequest req = (HttpServletRequest)request;
        this.setKaboumBean((KaboumBean)req.getSession(false).getAttribute(ObjectKeys.KABOUM_BEAN));
    }
     */

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (kaboumBean == null) {
            System.out.println("null kaboum bean in validate ?!");
        } else {
            // validation is done on client side
            // sets boolean values, as only checked boxes are passed into parameter
            Enumeration enume = request.getAttributeNames();
            while (enume.hasMoreElements()) {
                System.out.println("name: " + enume.nextElement());
            }
        }
        System.out.println("inside validate: ");
        return errors;   
    }
}
