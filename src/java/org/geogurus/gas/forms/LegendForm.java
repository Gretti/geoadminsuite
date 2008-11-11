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
 