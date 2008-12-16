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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.forms;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.geogurus.mapserver.objects.Legend;

/**
 *
 * @author gnguessan
 */
public class LegendForm extends org.apache.struts.action.ActionForm {

    private Legend legend;
    private String imgColor;
    private String outlnColor;

    /**
     *
     */
    public LegendForm() {
        super();
        this.legend = new Legend();
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

    /**
     * @return
     */
    public Legend getLegend() {
        return legend;
    }

    /**
     * @param string
     */
    public void setLegend(Legend legend_) {
        legend = legend_;
    }

    public String getImgColor() {
        return imgColor;
    }

    public void setImgColor(String imgColor) {
        this.imgColor = imgColor;
    }

    public String getOutlnColor() {
        return outlnColor;
    }

    public void setOutlnColor(String outlnColor) {
        this.outlnColor = outlnColor;
    }
}
 