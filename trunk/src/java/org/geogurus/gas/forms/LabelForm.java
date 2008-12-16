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
import org.geogurus.mapserver.objects.Label;

/**
 *
 * @author gnguessan
 */
public class LabelForm extends org.apache.struts.action.ActionForm {

    private Label label;
    private String labelItem;
    private String labelAngleItem;
    private String labelSizeItem;
    private int bitmapSize;
    private int truetypeSize;
    private String labelColor;
    private String labelOutlineColor;
    private Integer labelOutlineWidth;
    private String labelWrap;
    private Integer labelAlign;
    private Integer labelMaxLength;
    private String labelBackgroundColor;

    /**
     *
     */
    public LabelForm() {
        super();
        this.label = new Label();
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public String getLabelItem() {
        return labelItem;
    }

    public void setLabelItem(String labelItem) {
        this.labelItem = labelItem;
    }

    public String getLabelAngleItem() {
        return labelAngleItem;
    }

    public void setLabelAngleItem(String labelAngleItem) {
        this.labelAngleItem = labelAngleItem;
    }

    public String getLabelSizeItem() {
        return labelSizeItem;
    }

    public void setLabelSizeItem(String labelSizeItem) {
        this.labelSizeItem = labelSizeItem;
    }

    public int getBitmapSize() {
        return bitmapSize;
    }

    public void setBitmapSize(int bitmapSize) {
        this.bitmapSize = bitmapSize;
    }

    public int getTruetypeSize() {
        return truetypeSize;
    }

    public void setTruetypeSize(int truetypeSize) {
        this.truetypeSize = truetypeSize;
    }

    public String getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
    }

    public String getLabelOutlineColor() {
        return labelOutlineColor;
    }

    public Integer getLabelOutlineWidth() {
        return labelOutlineWidth;
    }

    public void setLabelOutlineWidth(Integer labelOutlineWidth) {
        this.labelOutlineWidth = labelOutlineWidth;
    }

    public void setLabelOutlineColor(String labelOutlineColor) {
        this.labelOutlineColor = labelOutlineColor;
    }

    public String getLabelBackgroundColor() {
        return labelBackgroundColor;
    }

    public void setLabelBackgroundColor(String labelBackgroundColor) {
        this.labelBackgroundColor = labelBackgroundColor;
    }
    
    public void setLabelWrap(String labelWrap) {
        this.labelWrap=labelWrap;
    }
    
    public String getLabelWrap() {
        return labelWrap;
    }

    public Integer getLabelAlign() {
        return labelAlign;
    }

    public void setLabelAlign(Integer labelAlign) {
        this.labelAlign = labelAlign;
    }

    public Integer getLabelMaxLength() {
        return labelMaxLength;
    }

    public void setLabelMaxLength(Integer labelMaxLength) {
        this.labelMaxLength = labelMaxLength;
    }
    
    
    
    
}
