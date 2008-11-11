<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>
<%@page import="org.geogurus.mapserver.objects.MsLayer" %>
<html:html locale="true">
    <head><title><bean:message key="ms_layer_title"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <SCRIPT>
            <!--
            <%
            // look if this page should refresh the mapfile
            String refresh = request.getParameter("refreshmap");
            if (refresh != null) {
                out.println("if (self.opener != null) self.opener.refreshExtent(); else self.close()");
            }
            %>
            // checks form fields to avoid passing bad values to the servlet
            function checkForm() {
                if (isNaN(document.form1.ms_maxscale.value) || isNaN(document.form1.ms_maxscale.value)) {
                    alert("<bean:message key="scale_msg"/>");return false;
                }
                return true;
            }

            function sub() {
                if (checkForm()) {
                    document.form1.submit();
                    return true;
                }
            }
            //-->
        </SCRIPT>
    </head>
    <body class='body2' bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <form name="form1" method="post" action="MSLayerProperties.jsv">
            <table border="0" bordercolor='black' class='tiny' cellspacing='1'>
                <tr> 
                    <th colspan="2" class="th0"><bean:message key="ms_layer_param"/></th>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="name_lower"/>: </td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="name"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="source_name"/>: </td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceName"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="source_type"/>: </td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceTypeAsString"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="geometry_type"/>:</td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="ogisType"/></b></td>
                </tr>
                <tr> 
                    <td class='td4tiny'><bean:message key="num_objects_lower"/></td>
                    <td class='td4tiny'><b><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="numGeometries"/></b></td>
                </tr>
                <tr> 
                    <th colspan="2" class="th0"><bean:message key="ms_layer_param_2"/></th>
                </tr>
                <tr> 
                    <td class='td4tiny'>ClassItem:</td>
                    <td class='td4tiny'>
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.classItem" styleClass="tiny">
                            <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" />
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Connection:</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.connection" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>ConnectionType:</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.connectionType" styleClass="tiny">
						<%
 						    for( MsLayer layer:MsLayer.values() ){ %>
		                            <html:option styleClass="tiny" value="<%=layer.toString()%>"><%=layer.getLabel()%></html:option>

						<% } %>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Data:</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.data" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Filter: </td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filter" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>FilterItem</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.filterItem" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Footer</td>
                    <td class='td4tiny'> 
                        <logic:empty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.footer">
                            <input type="text" name="defaultMsLayer.footer.path" value="" class="tiny">
                        </logic:empty>
                        <logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.footer">
                            <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.footer.path" styleClass="tiny"/>
                        </logic:notEmpty>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'><span class="td4tiny">Group</span>: </td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.group" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Header</td>
                    <td class='td4tiny'> 
                        <logic:empty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.header">
                            <input type="text" name="defaultMsLayer.header.path" value="" class="tiny">
                        </logic:empty>
                        <logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.header">
                            <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.header.path" styleClass="tiny"/>
                        </logic:notEmpty>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabeAnglelItem:</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelAngleItem" styleClass="tiny">
                            <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" />
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelCache</td>
                    <td class='td4tiny'> 
                        <html:checkbox name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelCache" value="true" styleClass="tiny"/>On
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelItem:</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelItem" styleClass="tiny">
                            <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" />
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelMaxScale</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelMaxScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelMinScale</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelMinScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelRequires</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelRequires" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>LabelSizeItem:</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelSizeItem" styleClass="tiny">
                            <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" />
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Maxscale: </td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.maxScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Minscale:</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.minScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Name</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.name" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Offsite</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.offSite" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>PostLabelCache</td>
                    <td class='td4tiny'> 
                        <html:checkbox name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.postLabelCache" value="true" styleClass="tiny"/>On
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Status</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.status" styleClass="tiny">
                            <option value="<%=String.valueOf(Layer.ON)%>">ON</option>
                            <option value="<%=String.valueOf(Layer.OFF)%>">OFF</option>
                            <option value="<%=String.valueOf(Layer.DEFAULT)%>">DEFAULT</option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>SizeUnits</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.sizeUnits" styleClass="tiny">
                            <option value="<%=String.valueOf(Layer.PIXELS)%>">pixels</option>
                            <option value="<%=String.valueOf(Layer.FEET)%>">feet</option>
                            <option value="<%=String.valueOf(Layer.INCHES)%>">inches</option>
                            <option value="<%=String.valueOf(Layer.KILOMETERS)%>">kilometers</option>
                            <option value="<%=String.valueOf(Layer.METERS)%>">meters</option>
                            <option value="<%=String.valueOf(Layer.MILES)%>">miles</option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>SymbolScale</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.symbolScale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Template</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.template" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>TileIndex</td>
                    <td class='td4tiny'> 
                        <logic:empty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileIndex">
                            <input type="text" name="defaultMsLayer.tileIndex.path" value="" class="tiny">
                        </logic:empty>
                        <logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileIndex">
                            <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileIndex.path" styleClass="tiny"/>
                        </logic:notEmpty>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>TileItem</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tileItem" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Tolerance</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.tolerance" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>ToleranceUnit</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.toleranceUnit" styleClass="tiny">
                            <option value="<%=String.valueOf(Layer.PIXELS)%>">pixels</option>
                            <option value="<%=String.valueOf(Layer.FEET)%>">feet</option>
                            <option value="<%=String.valueOf(Layer.INCHES)%>">inches</option>
                            <option value="<%=String.valueOf(Layer.KILOMETERS)%>">kilometers</option>
                            <option value="<%=String.valueOf(Layer.METERS)%>">meters</option>
                            <option value="<%=String.valueOf(Layer.MILES)%>">miles</option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Transparency</td>
                    <td class='td4tiny'> 
                        <html:text name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.transparency" styleClass="tiny"/>
                    </td>
                </tr>
                <tr>
                    <td class='td4tiny'>Transform</td>
                    <td class='td4tiny'>
                        <html:checkbox name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.transform" value="true" styleClass="tiny"/>True
                    </td>
                </tr>
                <tr> 
                    <td class='td4tiny'>Type:</td>
                    <td class='td4tiny'> 
                        <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.type" styleClass="tiny">
                            <option value="<%=String.valueOf(Layer.POINT)%>">point</option>
                            <option value="<%=String.valueOf(Layer.LINE)%>">line</option>
                            <option value="<%=String.valueOf(Layer.POLYLINE)%>">polyline</option>
                            <option value="<%=String.valueOf(Layer.POLYGON)%>">polygon</option>
                            <option value="<%=String.valueOf(Layer.ANNOTATION)%>">annotation</option>
                            <option value="<%=String.valueOf(Layer.RASTER)%>">raster</option>
                            <option value="<%=String.valueOf(Layer.QUERYONLY)%>">queryonly</option>
                        </html:select>
                    </td>
                </tr>
                <tr align="center"> 
                    <td colspan="2"> 
                        <input type="button" name="Submit3" value="<bean:message key="refresh"/>" onClick="sub()" class="tiny">
                        <input type="button" name="close" value="<bean:message key="close"/>" onClick="self.close();" class="tiny">
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html:html>
