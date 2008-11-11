<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Map" %>
<%@page import="org.geogurus.mapserver.objects.ScaleBar" %>

<bean:define id="scale" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.scaleBar"/>
<bean:define id="size" name="scale" property="size" type="java.awt.Dimension"/>

<script type="text/javascript">
    <!--
    var interlaces = [
        ['<%=String.valueOf(ScaleBar.ON)%>','ON'],
        ['<%=String.valueOf(ScaleBar.OFF)%>','OFF']
    ];
    var units = [
        ['<%=String.valueOf(ScaleBar.METERS)%>','meters'],
        ['<%=String.valueOf(ScaleBar.KILOMETERS)%>','kilometers'],
        ['<%=String.valueOf(ScaleBar.FEET)%>','feet'],
        ['<%=String.valueOf(ScaleBar.INCHES)%>','inches'],
        ['<%=String.valueOf(ScaleBar.MILES)%>','miles']
    ];
    var booleans = [
        ['true','True'],
        ['false','False']
    ];
    var status = [
        ['<%=String.valueOf(ScaleBar.ON)%>','ON'],
        ['<%=String.valueOf(ScaleBar.OFF)%>','OFF'],
        ['<%=String.valueOf(ScaleBar.EMBED)%>','EMBED']
    ];
    var positions = [
        ['<%=String.valueOf(ScaleBar.UL)%>','UL'],
        ['<%=String.valueOf(ScaleBar.UC)%>','UC'],
        ['<%=String.valueOf(ScaleBar.UR)%>','UR'],
        ['<%=String.valueOf(ScaleBar.LL)%>','LL'],
        ['<%=String.valueOf(ScaleBar.LC)%>','LC'],
        ['<%=String.valueOf(ScaleBar.LR)%>','LR']
    ];

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
                                    title: "<bean:message key="scalebar_object_parameter"/>",
                                    autoHeight:true,
                                    autoWidth:true,
                                    defaultType: 'textfield',
                                    defaults: {msgTarget: 'side', grow:true},
                                    items: [
                                        {
                                            fieldLabel: "Color",
                                            name: 'fgcolor',
                                            listeners: {
                                                'render': function(f,e) {checkRgb(f,e);},
                                                'blur': function(f,e) {checkRgb(f,e);}
                                            },
                                            value: "<bean:write name="scale" property="color"/>"
                                        },
                                        {
                                            fieldLabel: "Background Color",
                                            name: 'bgcolor',
                                            listeners: {
                                                'render': function(f,e) {checkRgb(f,e);},
                                                'blur': function(f,e) {checkRgb(f,e);}
                                            },
                                            value: "<bean:write name="scale" property="backgroundColor"/>"
                                        },
                                        /*
                                        {
                                            fieldLabel: "Image Color",
                                            name: 'imageColor',
                                            listeners: {
                                                'render': function(f,e) {checkRgb(f,e);},
                                                'blur': function(f,e) {checkRgb(f,e);}
                                            },
                                            value: "<bean:write name="scale" property="imageColor"/>"
                                        },
                                        */
                                        {
                                            fieldLabel: "Outline Color",
                                            name: 'olcolor',
                                            listeners: {
                                                'render': function(f,e) {checkRgb(f,e);},
                                                'blur': function(f,e) {checkRgb(f,e);}
                                            },
                                            value: "<bean:write name="scale" property="outlineColor"/>"
                                        },
                                        new Ext.form.ComboBox({
                                            fieldLabel: "Interlace",
                                            hiddenName: 'interlace',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['type', 'name'],
                                                data : interlaces
                                            }),
                                            displayField:'name',
                                            valueField:'type',
                                            value: "<bean:write name="scale" property="interlace"/>",
                                            typeAhead: true,
                                            autoWidth: true,
                                            mode: 'local',
                                            triggerAction: 'all',
                                            forceSelection: true,
                                            selectOnFocus:true,
                                            listClass: 'x-combo-list-small'
                                        }),
                                        new Ext.form.NumberField({
                                            fieldLabel: "Intervals",
                                            name: 'intervals',
                                            value: "<bean:write name="scale" property="intervals"/>",
                                            decimalPrecision: 0,
                                            allowDecimals : false,
                                            allowNegative : false
                                        }),
                                        new Ext.form.ComboBox({
                                            fieldLabel: "Position",
                                            hiddenName: 'position',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['type', 'name'],
                                                data : positions
                                            }),
                                            displayField:'name',
                                            valueField:'type',
                                            value: "<bean:write name="scale" property="position"/>",
                                            typeAhead: true,
                                            autoWidth: true,
                                            mode: 'local',
                                            triggerAction: 'all',
                                            forceSelection: true,
                                            selectOnFocus:true,
                                            listClass: 'x-combo-list-small'
                                        }),
                                        new Ext.form.ComboBox({
                                            fieldLabel: "PostLabelCache",
                                            hiddenName: 'postLabelCache',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['type', 'name'],
                                                data : booleans
                                            }),
                                            displayField:'name',
                                            valueField:'type',
                                            value: "<bean:write name="scale" property="postLabelCache"/>",
                                            typeAhead: true,
                                            autoWidth: true,
                                            mode: 'local',
                                            triggerAction: 'all',
                                            forceSelection: true,
                                            selectOnFocus:true,
                                            listClass: 'x-combo-list-small'
                                        }),
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
                                        new Ext.form.ComboBox({
                                            fieldLabel: "Status",
                                            hiddenName: 'status',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['type', 'name'],
                                                data : status
                                            }),
                                            displayField:'name',
                                            valueField:'type',
                                            value: "<bean:write name="scale" property="status"/>",
                                            typeAhead: true,
                                            autoWidth: true,
                                            mode: 'local',
                                            triggerAction: 'all',
                                            forceSelection: true,
                                            selectOnFocus:true,
                                            listClass: 'x-combo-list-small'
                                        }),
                                        new Ext.form.NumberField({
                                            fieldLabel: "Style",
                                            name: 'style',
                                            value: "<bean:write name="scale" property="style"/>",
                                            allowDecimals: false,
                                            allowNegative: false,
                                            decimalPrecision: 0,
                                            minValue: 0,
                                            maxValue: 2
                                        }),
                                        new Ext.form.ComboBox({
                                            fieldLabel: "Transparent",
                                            hiddenName: 'transparent',
                                            store: new Ext.data.SimpleStore({
                                                fields: ['type', 'name'],
                                                data : interlaces
                                            }),
                                            displayField:'name',
                                            valueField:'type',
                                            value: "<bean:write name="scale" property="transparent"/>",
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
                                            value: "<bean:write name="scale" property="units"/>",
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
            url:'scaleBarProperties.do',
            waitMsg:'Loading',
            params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.id),
            callback: function(){
                    //No need to redraw as scalebar is not displayed
                    //GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});
                    
                }
            });
            return true;
        }
    }
    //-->
    </script>