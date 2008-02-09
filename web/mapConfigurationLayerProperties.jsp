<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page contentType="text/html"%>
<%@ page import="org.geogurus.Datasource" %>
<%@ page import="org.geogurus.GeometryClass" %>
<%@ page import="org.geogurus.gas.utils.ObjectKeys" %>

<%//--General Info--%>
<%String srcType = "images/";%>
<logic:notEqual name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=new Byte(GeometryClass.PGCLASS).toString()%>">
    <logic:notEqual name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=new Byte(GeometryClass.ORACLASS).toString()%>">
        <%srcType += "folder.gif";%>
    </logic:notEqual>
</logic:notEqual>
<logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=new Byte(GeometryClass.PGCLASS).toString()%>">
    <%srcType += "database.png";%>
</logic:equal>
<logic:equal name="<%=ObjectKeys.CURRENT_GC%>" property="datasourceType" value="<%=new Byte(GeometryClass.ORACLASS).toString()%>">
    <%srcType += "database.png";%>
</logic:equal>


<script type="text/javascript" charset="utf-8">
<!--
        //GENERAL INFO DIV
        if(Ext.getCmp('layerGeneralInfo')) Ext.getCmp('layerGeneralInfo').destroy();
        
        var c = '<div class="res-block"><div class="res-block-inner">' +
                '<h3 align="center"><bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="tableName"/></h3>' + 
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
                                {"text":"<bean:message key="general"/>","id":"genLayProps","icon":"ext/styles/example.gif"},
                                {"text":"<bean:message key="classif"/>","id":"clasLayProps","icon":"ext/styles/expand-all.gif"}/*,
                                {"text":"<bean:message key="layer"/>","id":"layLayProps","icon":"ext/styles/method.gif"},
                                {"text":"<bean:message key="classes"/>","id":"layClasProps","icon":"ext/styles/pkg.gif"}*/
                            ];

        // adds nodes
        var layPropsNode;
        for(var n=0;n<layerProperties.length;n++) {
            layPropsNode = new Ext.tree.TreeNode({
                text:layerProperties[n].text,
                id:layerProperties[n].id,
                icon:layerProperties[n].icon,
                draggable:false
            });
            layPropsNode.on("click", function(){showWindow(this.id);});
            layerPropsRoot.appendChild(layPropsNode);
        }
        
        Ext.getCmp('cpLayerProps').add(layerGeneralInfo);
        Ext.getCmp('cpLayerProps').doLayout();
//-->
</script>