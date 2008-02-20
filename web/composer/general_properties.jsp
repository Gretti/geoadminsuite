<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.GeometryClass" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<html:html locale="true">
    <head><title><bean:message key="layer_configuration"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <script type="text/javascript">
        <!--
        // checks form fields to avoid passing bad values to the servlet
        function checkForm() {
            var curForm = document.forms["LayerForm"];
            if (document.getElementsByName("defaultMsLayer.minScale").item(0).value.length != 0 && 
                isNaN(document.getElementsByName("defaultMsLayer.minScale").item(0).value)) {
                alert("<bean:message key="err_scale_min"/>");
                return false;
            }
            if (document.getElementsByName("defaultMsLayer.maxScale").item(0).value.length != 0 && 
                isNaN(document.getElementsByName("defaultMsLayer.maxScale").item(0).value)) {
                alert("<bean:message key="err_scale_max"/>");
                return false;
            }
            if (isNaN(document.getElementsByName("defaultMsLayer.transparency").item(0).value) || 
                document.getElementsByName("defaultMsLayer.transparency").item(0).value < 0 || 
                document.getElementsByName("defaultMsLayer.transparency").item(0).value > 100) {
                alert("<bean:message key="err_transparency"/>");
                return false;
            }
            return true;
        }

        function sub() {
            if (checkForm()) {
                Ext.Ajax.request({
                url:'layerProperties.do',
                waitMsg:'Loading',
                params: Ext.Ajax.serializeForm(document.forms["LayerForm"]),
                success: function(){
                        GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});
                    }
                });                
                return true;
            }
        }
        //-->
        </script>
    </head>
    <body class='body2' style="margin:0;background-color:#FFF;">
        <html:form method="post" action="layerProperties.do">
            <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
                <tr> 
                    <th class="th0" colspan="2"><bean:message key="layer_param"/></th>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="source_name"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="name"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="source_origin"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceName"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="source_type"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceTypeAsString"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="geometry_type"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="ogisType"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="num_objects_lower"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="numGeometries"/></b></td>
                </tr>
                <tr> 
                    <th class="th0" colspan="2">&nbsp;</th>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="max_scale"/></td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.maxScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="min_scale"/></td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.minScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="filter"/></td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filter" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="filter_item"/></td>
                    <td class='td4tiny'> 
                        <span class='tiny'>
                            <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filterItem" styleClass="tiny">
                                <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="columnNamesInfo"/>
                            </html:select>
                        </span>
                    </td>
                </tr>
                <tr>
                    <td class='td4tiny'><bean:message key="transparency"/></td>
                    <td class='td4tiny'>
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.transparency" styleClass="tiny"/>
                    </td>
                </tr>
            </table>
        </html:form>
    </body>
</html:html>
