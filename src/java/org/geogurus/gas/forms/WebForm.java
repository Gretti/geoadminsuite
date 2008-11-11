/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.gas.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.geogurus.mapserver.objects.Web;

/**
 *
 * @author gnguessan
 */
public class WebForm extends org.apache.struts.action.ActionForm {
    
    private Web web;
   /**
    *
    */
   public WebForm() {
       super();
       this.web = new Web();
   }

    @Override
   public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
       ActionErrors errors = new ActionErrors();
       return errors;
   }

    public

    Web getWeb() {
        return web;
    }

    public void setWeb(Web web) {
        this.web = web;
    }
}
