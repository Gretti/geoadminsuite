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
<%@ page contentType="text/html"%>
<%@ page import="org.geogurus.data.Datasource" %>
<%@ page import="org.geogurus.data.DataAccess" %>
<%@ page import="org.geogurus.data.DataAccessType" %>
<%@ page import="org.geogurus.gas.utils.ObjectKeys" %>

<%
    //--General Info--
%>
<%
    String srcType = "images/";
%>

<%@page import="org.geogurus.data.DatasourceType"%><logic:notEqual name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=DataAccessType.POSTGIS.name()%>">
    <logic:notEqual name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=DataAccessType.ORACLE.displayname()%>">
        <%
            srcType += "folder.gif";
        %>
    </logic:notEqual>
</logic:notEqual>
<logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=DataAccessType.POSTGIS.displayname()%>">
    <%
        srcType += "database.png";
    %>
</logic:equal>
<logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=DataAccessType.ORACLE.displayname()%>">
    <%
        srcType += "database.png";
    %>
</logic:equal>


<script type="text/javascript" charset="utf-8">
<!--
        //GENERAL INFO DIV
        if(Ext.getCmp('layerGeneralInfo')) Ext.getCmp('layerGeneralInfo').destroy();
        
        var c = '<div class="res-block"><div class="res-block-inner">' +
                '<h3 align="center"><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="name"/></h3>' +
                '<ul>' + 
                '<li><img src="images/server.png" title=\'<bean:message key="server"/>\' alt=\'<bean:message key="server"/>\'>&nbsp;' + 
                '<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="host"/></li>'+
                '<li><img src=\'<%=srcType%>\' title=\'<bean:message key="source"/>\' alt=\'<bean:message key="source"/>\'>&nbsp;' +
                '<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceName"/></li>' +
                '</ul>' + 
                '</div></div>';
        var layerGeneralInfo = new Ext.Panel({
            id: 'layerGeneralInfo',
            layout:'fit',
            html: c,
            border: false
        });
        
        //PROPERTIES TREE
        if(Ext.getCmp('layerPropsTree')) Ext.getCmp('layerPropsTree').destroy();

        var layerPropsTree = new Ext.tree.TreePanel({
            id:'layerPropsTree',
            animate: true,
            containerScroll: true,
            rootVisible: false,
            border: false
        });

        Ext.getCmp('cpLayerProps').add(layerPropsTree);

        // set the root node
        var layerPropsRoot = new Ext.tree.TreeNode({text:'rootLayProps',id:'rootLayProps'});
        layerPropsTree.setRootNode(layerPropsRoot);
        var layerProperties = [
                                {"text":"<bean:message key="general"/>","id":"genLayProps","icon":"styles/example.gif","leaf":true}
                            ];

        <logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType.type" value="<%=DatasourceType.VECTOR.toString()%>">
	        layerProperties.push({"text":"<bean:message key="classif"/>","id":"clasLayProps","icon":"styles/expand-all.gif","leaf":true});
	        layerProperties.push({"text":"<bean:message key="labels"/>","id":"clasLabProps","icon":"styles/information.png","leaf":true});
    	</logic:equal>
        // adds nodes
        var layPropsNode;
        for(var n=0;n<layerProperties.length;n++) {
            layPropsNode = new Ext.tree.TreeNode({
                text:layerProperties[n].text,
                id:layerProperties[n].id,
                icon:layerProperties[n].icon,
                leaf:layerProperties[n].leaf,
                draggable:false
            });
            layPropsNode.on("click", function(){showWindow(this.id);});
            layerPropsRoot.appendChild(layPropsNode);
        }
        
        Ext.getCmp('cpLayerProps').add(layerGeneralInfo);
        Ext.getCmp('cpLayerProps').doLayout();
//-->
</script>