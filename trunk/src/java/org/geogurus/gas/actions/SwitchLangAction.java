/*
 * SwitchLangAction.java
 *
 * Created on 12 d�cembre 2006, 19:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.geogurus.gas.actions;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RedirectingActionForward;
/**
 *
 * @author GNG
 */
public class SwitchLangAction extends Action 
{
   public ActionForward execute(ActionMapping mapping, 
                                ActionForm form, 
                                HttpServletRequest request, 
                                HttpServletResponse response)
      throws IOException, ServletException 
   {
      //recuperation des parametres passes et de l'url du referer
      String country = request.getParameter("cty");
      String language = request.getParameter("lang");
      String referer = request.getHeader("referer");
      HttpSession session = request.getSession();
      ActionForward forward = null;
   
      // d�finition de la locale
      Locale locale = new Locale( language, country );
      setLocale( request, locale );
      
      // redirection vers une page d�finie par d�faut
      if( referer==null )
      {
         forward = (mapping.findForward("index"));
      }
      // redirection vers l'url du referrer.
      else 
      {
         forward = new RedirectingActionForward();
         forward.setPath( referer );
      }
      return forward;
   }
}