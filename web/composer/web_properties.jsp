<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Web" %>

<bean:define id="web" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="web"/>

<script type="text/javascript">
    <!--

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
                                    title: "<bean:message key="ms_web_title"/>",
                                    autoHeight:true,
                                    autoWidth:true,
                                    defaultType: 'textfield',
                                    defaults: {msgTarget: 'side', grow:true},
                                    items: [
                                        {
                                            fieldLabel: "Error URL",
                                            name: 'error',
                                            value='<bean:write name="web" property="error"/>'
                                        },{
                                            fieldLabel: "Footer File",
                                            name: 'footer',
                                            value='<bean:write name="web" property="footer"/>'
                                        },{
                                            fieldLabel: "Header File",
                                            name: 'header',
                                            value='<bean:write name="web" property="header"/>'
                                        },{
                                            fieldLabel: "Image Path",
                                            name: 'imagePath',
                                            value='<bean:write name="web" property="imagePath"/>'
                                        },{
                                            fieldLabel: "Image URL",
                                            name: 'imageURL',
                                            value='<bean:write name="web" property="imageURL"/>'
                                        },{
                                            fieldLabel: "Log Location",
                                            name: 'log',
                                            value='<bean:write name="web" property="log"/>'
                                        }, new Ext.form.NumberField({
                                            fieldLabel: "MaxScale",
                                            name: 'maxScale',
                                            value='<bean:write name="web" property="maxScale"/>',
                                            allowDecimals: false,
                                            allowNegative: false,
                                            decimalPrecision: 0
                                        }),{
                                            fieldLabel: "Maximum Template",
                                            name: 'maxTemplate',
                                            value='<bean:write name="web" property="maxTemplate"/>'
                                        }, new Ext.form.NumberField({
                                            fieldLabel: "MinScale",
                                            name: 'minScale',
                                            value='<bean:write name="web" property="minScale"/>',
                                            allowDecimals: false,
                                            allowNegative: false,
                                            decimalPrecision: 0
                                        }),{
                                            fieldLabel: "Minimum Template",
                                            name: 'minTemplate',
                                            value='<bean:write name="web" property="minTemplate"/>'
                                        },{
                                            fieldLabel: "Template",
                                            name: 'template',
                                            value='<bean:write name="web" property="template"/>'
                                        }
                                    ]
                                }]

            });

    Ext.getCmp('contentProps').add(formProps);
    Ext.getCmp('contentProps').doLayout();

    function sub() {
        if (Ext.getCmp('formProps').form.isValid()) {
            Ext.Ajax.request({
            url:'webProperties.do',
            waitMsg:'Loading',
            params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.id),
            callback: function(){
                    //No need to redraw as web do not change displayed map
                    //GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});
                    
                }
            });
            return true;
        }
    }
    //-->
    </script>    
    
    
