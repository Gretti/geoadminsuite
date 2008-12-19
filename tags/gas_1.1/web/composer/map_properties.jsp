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
<%@page import="org.geogurus.mapserver.objects.Map" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<%@page import="org.geogurus.mapserver.objects.SymbolSet" %>

        <bean:define id="mapfile" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile"/>
        <bean:define id="size" name="mapfile" property="size" type="java.awt.Dimension"/>

        <script type="text/javascript">
        <!--
        var imageTypes = [
            ['<%=String.valueOf(Map.GIF)%>','gif'],
            ['<%=String.valueOf(Map.JPEG)%>','jpeg'],
            ['<%=String.valueOf(Map.PNG)%>','png'],
            ['<%=String.valueOf(Map.WBMP)%>','wbmp'],
            ['<%=String.valueOf(Map.GTIFF)%>','gtiff']
        ];
        var interlaces = [
            ['<%=String.valueOf(Map.ON)%>','ON'],
            ['<%=String.valueOf(Map.OFF)%>','OFF']
        ];
        var units = [
            ['<%=String.valueOf(Map.METERS)%>','meters'],
            ['<%=String.valueOf(Map.KILOMETERS)%>','kilometers'],
            ['<%=String.valueOf(Map.FEET)%>','feet'],
            ['<%=String.valueOf(Map.INCHES)%>','inches'],
            ['<%=String.valueOf(Map.MILES)%>','miles'],
            ['<%=String.valueOf(Map.DD)%>','dd']
        ];

        var ss = "";
        var sp = "";
        var imgType = 'png';
        
    <logic:notEmpty name="mapfile" property="symbolSet">
        <logic:notEmpty name="mapfile" property="symbolSet.symbolSetFile">
        ss = "<bean:write name="mapfile" property="symbolSet.symbolSetFile.canonicalPath"/>";
        </logic:notEmpty>
    </logic:notEmpty>
    <logic:notEmpty name="mapfile" property="shapePath">
        sp = "<bean:write name="mapfile" property="shapePath"/>";
    </logic:notEmpty>

        //Builds FormPanel to inject in opened Window
        if(Ext.getCmp('formProps')) Ext.getCmp('formProps').destroy();
        var formProps = new Ext.FormPanel({
                            id: 'formProps',
                            labelWidth: 150,
                            frame:true,
                            width: '100%',
                            autoScroll:true,
                            border: false,
                            items: [{
                                        xtype: 'fieldset',
                                        title: "<bean:message key="map_param"/>",
                                        autoHeight:true,
                                        autoWidth:true,
                                        defaultType: 'textfield',
                                        defaults: {msgTarget: 'side', grow:true},
                                        items: [
                                            {
                                                fieldLabel: "Extent (space-separated)",
                                                name: 'extent',
                                                value: "<bean:write name="mapfile" property="extent"/>"
                                            },{
                                                fieldLabel: "FontSet",
                                                name: 'fontSet',
                                                value: "<bean:write name="mapfile" property="fontSet"/>"
                                            },{
                                                fieldLabel: "Image Color",
                                                name: 'imageColor',
                                                listeners: {
                                                    'render': function(f,e) {checkRgb(f,e);},
                                                    'blur': function(f,e) {checkRgb(f,e);}
                                                },
                                                value: "<bean:write name="mapfile" property="imageColor"/>"
                                            },
                                            new Ext.form.ComboBox({
                                                fieldLabel: "ImageType",
                                                hiddenName: 'imageType',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : imageTypes
                                                }),
                                                listeners: {
                                                    'change': function(f, newValue, oldValue) {
                                                                for (var i=0; i<imageTypes.length;i++) {
                                                                    if(imageTypes[i][0] == newValue) {
                                                                        imgType = imageTypes[i][1];
                                                                    }
                                                                }
                                                              }
                                                },
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="mapfile" property="imageType"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "Image Quality",
                                                name: 'imageQuality',
                                                value: "<bean:write name="mapfile" property="imageQuality"/>",
                                                decimalPrecision: 0,
                                                allowDecimals : false,
                                                allowNegative : false
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "Interlace",
                                                hiddenName: 'interlace',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : interlaces
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="mapfile" property="interlace"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: true,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),{
                                                fieldLabel: "Name",
                                                name: 'name',
                                                value: "<bean:write name="mapfile" property="name"/>"
                                            },
                                            new Ext.form.NumberField({
                                                fieldLabel: "Width",
                                                name: 'width',
                                                value: "<bean:write name="size" property="width"/>",
                                                allowDecimals: false,
                                                allowNegative: false,
                                                decimalPrecision: 0,
                                                minValue: 1,
                                                maxValue: 2048
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "Height",
                                                name: 'height',
                                                value: "<bean:write name="size" property="height"/>",
                                                allowDecimals: false,
                                                allowNegative: false,
                                                decimalPrecision: 0,
                                                minValue: 1,
                                                maxValue: 2048
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "Resolution",
                                                name: 'resolution',
                                                value: "<bean:write name="mapfile" property="resolution"/>",
                                                allowDecimals: false,
                                                allowNegative: false,
                                                decimalPrecision: 0
                                            }),
                                            {
                                                fieldLabel: "Scale",
                                                name: 'scale',
                                                value: "<bean:write name="mapfile" property="scale"/>"
                                            },
                                            new Ext.form.ComboBox({
                                                fieldLabel: "Status",
                                                hiddenName: 'status',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : interlaces
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="mapfile" property="status"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: true,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            {
                                                fieldLabel: "Shapepath",
                                                name: 'shapePath',
                                                value: sp
                                            },
                                            {
                                                fieldLabel: "Symbolset",
                                                name: 'canonicalPath',
                                                value: ss
                                            },
                                            new Ext.form.ComboBox({
                                                fieldLabel: "Transparent",
                                                hiddenName: 'transparent',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : interlaces
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="mapfile" property="transparent"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: true,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "Units",
                                                hiddenName: 'units',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : units
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="mapfile" property="units"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: true,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            })
                                        ]
                                    }]

                });

        Ext.getCmp('contentProps').add(formProps);
        Ext.getCmp('contentProps').doLayout();

        function sub() {
            if (Ext.getCmp('formProps').form.isValid()) {
                Ext.Ajax.request({
                url:'mapProperties.do',
                waitMsg:'Loading',
                params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.id),
                callback: function(){
                        /*FIXME : Image type management
                        GeneralLayout.composermap.layers[1].format = 'image/' + imgType;
                        GeneralLayout.composermap.layers[1].params.format = 'image/' + imgType;
                        GeneralLayout.composermap.layers[1].DEFAULT_PARAMS.map_imagetype = imgType
                        */
                        refreshComposerMap();
                    }
                });
                return true;
            }
        }
        //-->
        </script>