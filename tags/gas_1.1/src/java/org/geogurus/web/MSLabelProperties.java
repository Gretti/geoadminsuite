/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * MSLabelProperties.java
 *
 * Created on 12 janvier 2003, 16:42
 */
package org.geogurus.web;

import java.awt.Dimension;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.geogurus.data.DataAccess;
import org.geogurus.gas.objects.UserMapBean;
import org.geogurus.gas.utils.ObjectKeys;
import org.geogurus.mapserver.objects.Class;
import org.geogurus.mapserver.objects.Label;
import org.geogurus.mapserver.objects.Layer;
import org.geogurus.mapserver.objects.RGB;

/**
 * Servlet to deal with a Label object, and its corresponding HTML configuration page:
 * MS_mapserver_label_properties.jsp <br>
 * Label object can belong to a Class, Legend or scalebar object.
 * 
 * @author  nri
 */
public class MSLabelProperties extends BaseServlet {

    protected final String mc_mslab_jsp = "MC_mapserver_label_properties.jsp";

    /**
     * Main method listening to client requests:
     */
    public void process(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        debugParameters(request);
        // the UserMapBean stored in session.
        UserMapBean umb = (UserMapBean) session.getAttribute(ObjectKeys.USER_MAP_BEAN);

        if (umb == null) {
            // session: expiration
            String error = "UserMapBean ou layerid manquant. La session a du expirer.";
            session.setAttribute(ObjectKeys.SERVLET_ERROR, error);
            dispatch(request, response, BaseServlet.JSP_ERROR_PAGE);
            return;
        }
        // the request parameter telling which MS object label is to set:
        String msObject = request.getParameter("msobject");

        doUpdate(request, umb, msObject);
        // put in the request a parameter to refresh the mapfile in the main window
        dispatch(request, response, mc_mslab_jsp + "?refreshmap=true&msobject=" + msObject);
    }

    /**
     * update the current Mapfile object with user-modified parameters.
     * The current mapfile is stored in the UserMapBean bean
     */
    protected void doUpdate(HttpServletRequest request, UserMapBean umb, String msObject) {
        Label lab = getLabel(request, umb, msObject);

        if (lab == null) {
            lab = new Label();
            lab.setColor(new RGB(0, 0, 0));
        }
        //control is done by Javascript
        if (request.getParameter("label_angle").length() > 0) {
            String ang = request.getParameter("label_angle");
            if (ang.equalsIgnoreCase("auto")) {
                lab.setAngle(Label.AUTO);
            } else {
                lab.setAngle(new Double(ang).doubleValue());
            }
        } else {
            lab.setAngle(Label.UNDEF);
        }
        lab.setAntialias((request.getParameter("label_antialias") != null));

        if (request.getParameter("label_font").length() > 0) {
            lab.setFont(request.getParameter("label_font"));
        }
        if (request.getParameter("label_color").length() > 0) {
            lab.setColor(new RGB(request.getParameter("label_color")));
        }
        // for background and outline color: empty value means null color
        if (request.getParameter("label_backgroundcolor").length() > 0) {
            lab.setBackgroundColor(new RGB(request.getParameter("label_backgroundcolor")));
        } else {
            lab.setBackgroundColor(null);
        }
        if (request.getParameter("label_backgroundshadowcolor").length() > 0) {
            lab.setBackgroundShadowColor(new RGB(request.getParameter("label_backgroundshadowcolor")));
        } else {
            lab.setBackgroundShadowColor(null);
        }
        if (request.getParameter("label_shadowcolor").length() > 0) {
            lab.setShadowColor(new RGB(request.getParameter("label_shadowcolor")));
        } else {
            lab.setShadowColor(null);
        }
        if (request.getParameter("label_outlinecolor").length() > 0) {
            lab.setOutlineColor(new RGB(request.getParameter("label_outlinecolor")));
        } else {
            lab.setOutlineColor(null);
        }
        if (request.getParameter("label_outlinewidth").length() > 0) {
            lab.setOutlineWidth(new Integer(request.getParameter("label_outlinewidth")).intValue());
        } else {
            lab.setOutlineWidth(1);
        }
        if (request.getParameter("label_bgshadowsize_x").length() > 0 &&
                request.getParameter("label_bgshadowsize_y").length() > 0) {

            int x = new Integer(request.getParameter("label_bgshadowsize_x")).intValue();
            int y = new Integer(request.getParameter("label_bgshadowsize_y")).intValue();
            lab.setBackgroundShadowSize(new Dimension(x, y));
        } else {
            lab.setBackgroundShadowSize(null);
        }

        if (request.getParameter("label_shadowsize_x").length() > 0 &&
                request.getParameter("label_shadowsize_y").length() > 0) {

            int x = new Integer(request.getParameter("label_shadowsize_x")).intValue();
            int y = new Integer(request.getParameter("label_shadowsize_y")).intValue();
            lab.setShadowSize(new Dimension(x, y));
        } else {
            lab.setShadowSize(null);
        }
        if (request.getParameter("label_offset_x").length() > 0 &&
                request.getParameter("label_offset_y").length() > 0) {

            int x = new Integer(request.getParameter("label_offset_x")).intValue();
            int y = new Integer(request.getParameter("label_offset_y")).intValue();
            lab.setOffset(new Dimension(x, y));
        } else {
            lab.setOffset(null);
        }
        if (request.getParameter("label_buffer").length() > 0) {
            lab.setBuffer(new Integer(request.getParameter("label_buffer")).intValue());
        } else {
            lab.setBuffer(0);
        }
        if (request.getParameter("label_maxsize").length() > 0) {
            lab.setMaxSize(new Integer(request.getParameter("label_maxsize")).intValue());
        } else {
            lab.setMaxSize(256);
        }
        if (request.getParameter("label_minsize").length() > 0) {
            lab.setMinSize(new Integer(request.getParameter("label_minsize")).intValue());
        } else {
            lab.setMinSize(4);
        }
        if (request.getParameter("label_mindistance").length() > 0) {
            lab.setMinDistance(new Integer(request.getParameter("label_mindistance")).intValue());
        } else {
            lab.setMinDistance(0);
        }
        if (request.getParameter("label_minfeaturesize").length() > 0) {
            String feat = request.getParameter("label_minfeaturesize");
            if (feat.equalsIgnoreCase("auto")) {
                lab.setMinFeatureSize(Label.AUTO);
            } else {
                lab.setMinFeatureSize(new Integer(feat).intValue());
            }
        } else {
            lab.setMinFeatureSize(Label.UNDEF);
        }

        lab.setForce((request.getParameter("label_force") != null));
        lab.setPartials((request.getParameter("label_partials") != null));

        String pos = request.getParameter("label_position");
        if (pos.equalsIgnoreCase("ul")) {
            lab.setPosition(Label.UL);
        } else if (pos.equalsIgnoreCase("uc")) {
            lab.setPosition(Label.UC);
        } else if (pos.equalsIgnoreCase("ur")) {
            lab.setPosition(Label.UR);
        } else if (pos.equalsIgnoreCase("cl")) {
            lab.setPosition(Label.CL);
        } else if (pos.equalsIgnoreCase("cc")) {
            lab.setPosition(Label.CC);
        } else if (pos.equalsIgnoreCase("cr")) {
            lab.setPosition(Label.CR);
        } else if (pos.equalsIgnoreCase("ll")) {
            lab.setPosition(Label.LL);
        } else if (pos.equalsIgnoreCase("lc")) {
            lab.setPosition(Label.LC);
        } else if (pos.equalsIgnoreCase("lr")) {
            lab.setPosition(Label.LR);
        } else if (pos.equalsIgnoreCase("auto")) {
            lab.setPosition(Label.AUTO);
        }
        if (request.getParameter("label_size").length() > 0) {
            String s = request.getParameter("label_size");
            if (s.equalsIgnoreCase("tiny")) {
                lab.setSize(Label.TINY);
            } else if (s.equalsIgnoreCase("medium")) {
                lab.setSize(Label.MEDIUM);
            } else if (s.equalsIgnoreCase("large")) {
                lab.setSize(Label.LARGE);
            } else if (s.equalsIgnoreCase("small")) {
                lab.setSize(Label.SMALL);
            } else if (s.equalsIgnoreCase("giant")) {
                lab.setSize(Label.GIANT);
            } else {
                lab.setSize(new Integer(s).intValue());
            }
        }
        String typ = request.getParameter("label_type");
        if (typ.equalsIgnoreCase("bitmap")) {
            lab.setType(Label.BITMAP);
        } else if (typ.equalsIgnoreCase("truetype")) {
            lab.setType(Label.TRUETYPE);
        }
        if (request.getParameter("label_wrap").length() > 0) {
            lab.setWrap(request.getParameter("label_wrap"));
        } else {
            lab.setWrap(null);
        }

        setLabel(request, lab, umb);
    //umb.generateUserMapfile();
    }

    /**
     * Returns a label object based on the given <code>msObject</code> parameter:<br>
     * <ul>
     * <li>if msObject == classtarget, then a "classid" request parameter is also passed.
     * It is the current Layer class identifier, whose label is to set.</li> 
     *<li>if msObject == legendtarget, the label object of the mapfile's Legend object
     * is to set</li>
     *<li>if msObject == scalebartarget, the label object of the mapfile's Scalebar object
     * is to set</li>
     * </ul>
     *
     *@param request the HttpServletRequest
     *@param umb the UserMapBean, session object containing all user-specific informations
     * concerning the current map.
     *@param msObject the request parameter telling which Label object is to set
     *@return a <code>Label</code> object
     */
    protected Label getLabel(HttpServletRequest request, UserMapBean umb, String msObject) {
        if (msObject == null) {
            return null;
        }
        Label lab = null;

        HttpSession session = request.getSession();

        if ("classtarget".equalsIgnoreCase(msObject)) {
            // target MS object is a class
            // get the current GeometryClass' layer object whose class is to retrieve:
            String layerid = (String) session.getAttribute(ObjectKeys.CURRENT_GC);
            // the layer's object of the current GeometryClass: all the GeometryClass were asked to generate
            // their layer object, as they are displayed in the map
            DataAccess gc = (DataAccess) umb.getUserLayerList().get(layerid);
            Layer gcLayer = gc.getMSLayer(new RGB(0, 0, 0), false);
            String classID = request.getParameter("classid");
            // find the mapClass based on the given id:
            Class cl = null;
            for (Iterator iter = gcLayer.getMapClass().getClasses(); iter.hasNext();) {
                cl = (Class) iter.next();
                if (classID.equals("" + cl.getID())) {
                    break;
                }
            }
            lab = cl.getLabel();
        } else if ("scalebartarget".equalsIgnoreCase(msObject)) {
            // target MS object is the ScaleBar
            lab = umb.getMapfile().getScaleBar().getLabel();
        } else if ("legendtarget".equalsIgnoreCase(msObject)) {
            // target MS object is the Legend
            lab = umb.getMapfile().getLegend().getLabel();
        }
        return lab;
    }

    /**
     * Sets the given label object to a Mapserver Object:
     * Sets it according to the value of msObject:
     * <ul>
     * <li>if msObject == classtarget, sets the label to the current MSClass.</li> 
     *<li>if msObject == legendtarget, sets the label to the mapfile legend object</li>
     *<li>if msObject == scalebartarget, sets the label to the mapfile's Scalebar object</li>
     * </ul>
     *
     *@param request the HttpServletRequest
     *@param lab the Label to set
     *@param umb the UserMapBean containg all user-specific map informations
     **/
    protected void setLabel(
            HttpServletRequest request,
            Label lab,
            UserMapBean umb) {
        String msObject = request.getParameter("msobject");
        String classID = request.getParameter("classid");
        HttpSession session = request.getSession();

        if ("classtarget".equalsIgnoreCase(msObject)) {
            // target MS object is a class
            // get the current GeometryClass' layer object whose class is to retrieve:
            String layerid = (String) session.getAttribute(ObjectKeys.CURRENT_GC);
            // the layer's object of the current GeometryClass: all the GeometryClass were asked to generate
            // their layer object, as they are displayed in the map
            DataAccess gc = (DataAccess) umb.getUserLayerList().get(layerid);
            Layer gcLayer = gc.getMSLayer(new RGB(0, 0, 0), false);
            // find the mapClass based on the given id:
            Class cl = null;
            for (Iterator iter = gcLayer.getMapClass().getClasses(); iter.hasNext();) {
                cl = (Class) iter.next();
                if (classID.equals("" + cl.getID())) {
                    break;
                }
            }
            cl.setLabel(lab);
        } else if ("scalebartarget".equalsIgnoreCase(msObject)) {
            // target MS object is the ScaleBar
            umb.getMapfile().getScaleBar().setLabel(lab);
        } else if ("legendtarget".equalsIgnoreCase(msObject)) {
            // target MS object is the Legend
            umb.getMapfile().getLegend().setLabel(lab);
        }
    }
}
