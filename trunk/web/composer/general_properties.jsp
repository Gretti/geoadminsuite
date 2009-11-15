<%/*Copyright (C) Gretti N'Guessan, Nicolas Ribot

This file is part of GeoAdminSuite

GeoAdminSuite is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GeoAdminSuite is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.*/%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.data.DataAccess" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<script type="text/javascript">
    var flds = [];
<logic:iterate id="field" name="<%=ObjectKeys.CURRENT_GC%>" property="columnInfo">
    flds.push(['<bean:write name="field" property="name"/>']);
</logic:iterate>

    //Builds FormPanel to inject in opened Window
    if(Ext.getCmp('formProps')) Ext.getCmp('formProps').destroy();
    var html = "<table>";
    html += "<tr><td><bean:message key="source_name"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="name"/></td></tr>";
    html += "<tr><td><bean:message key="source_origin"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceName"/></td></tr>";
    html += "<tr><td><bean:message key="source_type"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceTypeAsString"/></td></tr>";
    html += "<tr><td><bean:message key="geometry_type"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="ogisType"/></td></tr>";
    <logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="numGeometries" value="-1">
        html += "<tr><td><bean:message key="num_objects_lower"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;Unknown</td></tr>";
    </logic:equal>
    <logic:notEqual name="<%=ObjectKeys.CURRENT_GC%>" property="numGeometries" value="-1">
        html += "<tr><td><bean:message key="num_objects_lower"/>&nbsp;</td><td style=\"font-weight:bold\">&nbsp;<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="numGeometries"/></td></tr>";
    </logic:notEqual>
    html += "</table>";
    //Prepares raster fieldset in case
    var isCollapsedRaster = true;
<logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileItem">
    isCollapsedRaster = false;
</logic:notEmpty>
    var fsRaster = {
                        xtype:'fieldset',
                        id:'isRasterTile',
                        checkboxToggle:true,
                        title: i18n.is_tile,
                        autoHeight:true,
                        autoWidth:true,
                        defaultType: 'textfield',
                        collapsed: isCollapsedRaster,
                        items :[new Ext.form.ComboBox({
                                    fieldLabel: "Tile Item",
                                    name: 'defaultMsLayer.tileItem',
                                    store: new Ext.data.SimpleStore({
                                        fields: ['name'],
                                        data : flds
                                    }),
                                    displayField:'name',
                                    valueField:'name',
                                    value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileItem"/>",
                                    typeAhead: true,
                                    autoWidth: true,
                                    mode: 'local',
                                    triggerAction: 'all',
                                    forceSelection: false,
                                    selectOnFocus:true,
                                    listClass: 'x-combo-list-small'
                                })]
                    };

    var formProps = new Ext.FormPanel({
                        id: 'formProps',
                        labelWidth: 150,
                        frame:true,
                        autoScroll: true,
                        width: '100%',
                        border: false,
                        items: [{
                                    xtype: 'fieldset',
                                    title: '<bean:message key="general_info"/>',
                                    autoHeight:true,
                                    autoWidth:true,
                                    html:html
                                },{
                                    xtype: 'fieldset',
                                    title: '<bean:message key="layer_param"/>',
                                    autoHeight:true,
                                    autoWidth:true,
                                    defaultType: 'textfield',
                                    defaults: {msgTarget: 'side', grow:true},
                                    items: [
                                        new Ext.form.NumberField({
                                            fieldLabel: "<bean:message key="min_scale"/>",
                                            name: 'defaultMsLayer.minScale',
                                            value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.minScale" ignore="true"/>",
                                            decimalPrecision: 0,
                                            allowDecimals : false,
                                            allowNegative : false
                                        }),
                                        new Ext.form.NumberField({
                                            fieldLabel: "<bean:message key="max_scale"/>",
                                            name: 'defaultMsLayer.maxScale',
                                            value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.maxScale" ignore="true"/>",
                                            decimalPrecision: 0,
                                            allowDecimals : false,
                                            allowNegative : false
                                        }),
                                        {
                                            fieldLabel: "<bean:message key="filter"/>",
                                            name: 'defaultMsLayer.filter',
                                            value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filter" ignore="true"/>"
                                        },
                                        new Ext.form.ComboBox({
                                            fieldLabel: "<bean:message key="filter_item"/>",
                                            name: 'defaultMsLayer.filterItem',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['name'],
                                                data : flds
                                            }),
                                            displayField:'name',
                                            valueField:'name',
                                            value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filterItem" ignore="true"/>",
                                            typeAhead: true,
                                            autoWidth: true,
                                            mode: 'local',
                                            triggerAction: 'all',
                                            forceSelection: false,
                                            selectOnFocus:true,
                                            listClass: 'x-combo-list-small'
                                        }),
                                        new Ext.form.NumberField({
                                            fieldLabel: "<bean:message key="transparency"/>",
                                            name: 'defaultMsLayer.transparency',
                                            value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.transparency" ignore="true"/>",
                                            allowDecimals: false,
                                            allowNegative: false,
                                            allowBlank: false,
                                            decimalPrecision: 0,
                                            minValue: 0,
                                            maxValue: 100
                                        })
                                    ]
                                }
<logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.type" value="<%=String.valueOf(Layer.POLYGON)%>">
                                ,fsRaster
</logic:equal>
<logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileItem">
                                ,fsRaster
</logic:notEmpty>
                            ]
                        });

        Ext.getCmp('contentProps').add(formProps);
        Ext.getCmp('contentProps').doLayout();
        
        if(Ext.getCmp('isRasterTile')) 
            Ext.getCmp('isRasterTile').on("collapse",function(){
            Ext.getCmp('formProps').form.findField('defaultMsLayer.tileItem').clearValue();
        });

        function sub() {
            if (!Ext.getCmp('formProps').form.isValid()) {
                Ext.MessageBox.show({
                   title: 'Invalid form',
                   msg: 'Correct errors in form before validating ...',
                   buttons: Ext.MessageBox.OK,
                   icon: Ext.MessageBox.WARNING
                });
                return;
            }
            var max = Ext.getCmp('formProps').form.findField('defaultMsLayer.maxScale').value
            var min = Ext.getCmp('formProps').form.findField('defaultMsLayer.minScale').value
            if (max < min) {
                Ext.MessageBox.show({
                   title: 'Invalid form',
                   msg: 'Maximum scale can not be lesser than minimum scale',
                   buttons: Ext.MessageBox.OK,
                   icon: Ext.MessageBox.ERROR
                });
                return;
            }

            if (Ext.getCmp('formProps').form.isValid()) {
                Ext.Ajax.request({
                    url:'layerProperties.do',
                    waitMsg:'Loading',
                    params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.id),
                    callback: function(){
                            refreshComposerMap();
                        }
                    });
                return true;
            }
        }
</script>
