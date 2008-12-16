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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"/>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page contentType="text/html"%>
<%@ page import="java.io.File" %>
<%@ page import="org.geogurus.tools.DataManager" %>
<%@ page import="org.geogurus.data.Datasource" %>
<%@ page import="org.geogurus.data.DatasourceType" %>
<%@ page import="org.geogurus.data.DataAccess" %>
<%@ page import="org.geogurus.data.DataAccessType" %>
<%@ page import="org.geogurus.gas.utils.ObjectKeys" %>

        <script type="text/javascript">
                var i, j, k, img, nodeServer, nodeDatasource, nodeLayer;
                var catalogMsUrl, nodeText;
                var nodeSchemas = [];
                var imgbase = 'images/';
                var imgserver    = imgbase + 'server.png';
                var imgdatabase  = imgbase + 'database.png';
                var imgfolder    = imgbase + 'folder.gif';
                var imgfile      = imgbase + 'page_url.gif';
                var imglayer     = imgbase + 'layers.png';
                var imglayerPg   = imgbase + 'layers_pg.png';
                var imglayerTiff = imgbase + 'layers_tiff.png';
                var imglayerShp  = imgbase + 'layers_shp.png';
                var imglayerImg  = imgbase + 'layers_img.png';
                var imglayerEcw  = imgbase + 'layers_ecw.png';
                var imglayerMap  = imgbase + 'layers_map.png';
                var imglayerOra  = imgbase + 'layers_ora.png';
                var imglayerWms  = imgbase + 'layers_map.png';
                var imgschema    = imgbase + 'database_table.png';

                //destroy previous tree and panel if existing
                if(Ext.getCmp('treeHost')) {
                    Ext.getCmp('treeHost').destroy();
                }
                if(Ext.getCmp('btnGotoComposer')) {
                    Ext.getCmp('btnGotoComposer').destroy();
                }

                //-----------------------------//
                //           SERVER TREE       //
                //-----------------------------//

                var tree = new Ext.tree.TreePanel({
                    id:'treeHost',
                    animate: false,
                    containerScroll: true,
                    rootVisible: false
                });
                Ext.getCmp('pnlDataList').add(tree);
                // set the root node
                var root = new Ext.tree.TreeNode({text: 'Server list', id:'source'});
                tree.setRootNode(root);
                
                //overrides the getChecked() method to circumvent  Ext tree bug with checkboxes.
                // see: http://extjs.com/forum/showthread.php?p=125884
                Ext.override(Ext.tree.TreeNodeUI,{
                    toggleCheck : function(value){
                        var cb = this.checkbox;
                        if(cb){
                            var checkvalue = (value === undefined ? !cb.checked : value);
                            cb.checked = checkvalue;
                            this.node.attributes.checked = checkvalue;
                        }
                    }
                }); 

                <logic:present name="<%=ObjectKeys.HOST_LIST%>" scope="session">
                    <%// Loops on servers%>
                    <logic:iterate id="host" indexId="cntHost" name="<%=ObjectKeys.HOST_LIST%>">
                i = 'host' + <bean:write name="cntHost"/>;
                nodeServer = new Ext.tree.TreeNode({
                    text:'<bean:write name="host" property="key"/>', 
                    id:i, 
                    icon:imgserver});

                root.appendChild(nodeServer);
                <%// Loops on datasources%>
                <logic:iterate id="datasource" indexId="cntDs" name="host" property="value">
                nodeSchema = null;
                j = '<bean:write name="datasource" property="id"/>';
                img = imgdatabase;
                <logic:equal name="datasource" property="type" value="<%=DatasourceType.FOLDER.toString()%>">
                img = imgfolder;
                </logic:equal>
                <logic:equal name="datasource" property="type" value="<%=DatasourceType.MAPFILE.toString()%>">
                img = imgfile;
                </logic:equal>
                
                //datasource UI id will be comma-separated list of host name and datasource id
                nodeDatasource = new Ext.tree.TreeNode({
                    text: '<bean:write name="datasource" property="escapedName"/>',
                    id: "<bean:write name="datasource" property="host"/>," + j, 
                    checked:false, 
                    icon: img});
                
                nodeServer.appendChild(nodeDatasource);
                var nodeSchema;
                <%// Loops on layers%>
                <logic:iterate id="gc" indexId="cntGc" name="datasource" property="sortedDataList" type="DataAccess">
                k = j + 'gc' + <bean:write name="cntGc"/>;
                nodeText = "<bean:write name="gc" property="escapedName"/>";
                <%// Choose layer icon according to GC type%>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.POSTGIS.toString()%>">
                    imglayer = imglayerPg;
                    var nodeExists = false;
                    var e;
                    for (e = 0 ; e < nodeSchemas.length ; e++) {
                        if(nodeSchemas[e].id == (j + "_<bean:write name="gc" property="connectionParams.schema"/>")) {
                            nodeExists = true;
                            break;
                        }
                    }
                    if(!nodeExists) {
                        nodeSchema = new Ext.tree.TreeNode({
                            text: '<bean:write name="gc" property="connectionParams.schema"/>', 
                            id: j + "_<bean:write name="gc" property="connectionParams.schema"/>", 
                            checked:false, 
                            icon: imgschema
                        });
                        nodeSchema.on('checkchange',function(n){
                            if(!n.childrenRendered)n.renderChildren();
                            for (var i=0; i < n.childNodes.length; i++) {
                                n.childNodes[i].ui.toggleCheck(n.attributes.checked);
                            };
                        });
                        nodeDatasource.appendChild(nodeSchema);
                        nodeSchemas.push(nodeSchema);
                    } else {
                        nodeSchema = nodeSchemas[e];
                    }
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.ORACLE.toString()%>">
                    imglayer = imglayerOra;
                    nodeExists = false;
                    for (e=0 ; e < nodeSchemas.length ; e++) {
                        if(nodeSchemas[e].id == (j + "_<bean:write name="gc" property="connectionParams.schema"/>")) {
                            nodeExists = true;
                            break;
                        }
                    }
                    if(!nodeExists) {
                        nodeSchema = new Ext.tree.TreeNode({
                            text: '<bean:write name="gc" property="connectionParams.schema"/>', 
                            id: j + "_<bean:write name="gc" property="connectionParams.schema"/>", 
                            checked:false, 
                            icon: imgschema
                        });
                        nodeSchema.on('checkchange',function(n){
                            if(!n.childrenRendered)n.renderChildren();
                            for (var i=0; i < n.childNodes.length; i++) {
                                n.childNodes[i].ui.toggleCheck(n.attributes.checked);
                            };
                        });
                        nodeDatasource.appendChild(nodeSchema);
                        nodeSchemas.push(nodeSchema);
                    } else {
                        nodeSchema = nodeSchemas[e];
                    }
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.SHP.toString()%>">
                    imglayer = imglayerShp;
                    nodeSchema = null;
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.TIFF.toString()%>">
                    imglayer = imglayerTiff;
                    nodeSchema = null;
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.IMG.toString()%>">
                    imglayer = imglayerImg;
                    nodeSchema = null;
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.ECW.toString()%>">
                    imglayer = imglayerEcw;
                    nodeSchema = null;
                </logic:equal>
                <logic:equal name="gc" property="datasourceType" value="<%=DataAccessType.WMS.toString()%>">
                    imglayer = imglayerWms;
                    nodeSchema = null;
                </logic:equal>
                
                nodeLayer = new Ext.tree.TreeNode({
                    text: nodeText,
                    id: "<bean:write name="datasource" property="host"/>,<bean:write name="gc" property="ID"/>", 
                    checked: false,
                    icon:imglayer, 
                    leaf:true});
                
                nodeLayer.on('click',function(){
                                var paramstr = 'st=data&host=<bean:write name="host" property="key" filter="false"/>&layerID=<bean:write name="gc" property="ID"/>&dsID=<bean:write name="cntDs"/>';

                                Ext.MessageBox.show({
                                    msg: '<bean:message key="retrieving_info"/>',
                                    progressText: '<bean:message key="reading_datasource"/>',
                                    width:300,
                                    wait:true,
                                    waitConfig: {interval:200}
                                });

                                Ext.get("data_detail").load({
                                    url: "infoDatasources.do",
                                    params: paramstr,
                                    scripts:true,
                                    callback: function() {
                                        Ext.MessageBox.hide();
                                    }
                                });
                });
                if (nodeSchema) {
                    nodeSchema.appendChild(nodeLayer);
                } else {
                    nodeDatasource.appendChild(nodeLayer);
                }
                nodeDatasource.on('checkchange',function(n){
                    if(!n.childrenRendered)n.renderChildren();
                    for (var i=0; i < n.childNodes.length; i++) {
                        //Case of schema node
                        if(!n.childNodes[i].isLeaf()) {
                            if(!n.childNodes[i].childrenRendered)n.childNodes[i].renderChildren();
                            for (var m=0; m < n.childNodes[i].childNodes.length; m++) {
                                n.childNodes[i].childNodes[m].ui.toggleCheck(n.attributes.checked);
                            }
                        }
                        n.childNodes[i].ui.toggleCheck(n.attributes.checked);
                    };
                });
                            </logic:iterate>
                        </logic:iterate>
                    </logic:iterate>
                </logic:present>


                //-----------------------------//
                //           BUTTON NEXT       //
                //-----------------------------//
                    
                   var action = new Ext.Action({
                        text: i18n.next,
                        handler: GeneralLayout.gotoComposer,
                        iconCls: 'bnext'
                   });
                    
                   var panel = new Ext.Panel({
                        id:'btnGotoComposer',
                        bodyStyle: 'padding:10px;',
                        autoHeight: true,
                        bodyBorder: false,
                        html:'<br>' + GeneralLayout.createBoxHelp('<img src="images/help.png">','To Internat:Ce panneau pr&eacute;sente sous forme d\'arbre la liste des sources de donn&eacute;es trouv&eacute;es sur les serveurs. En cliquant sur les noms des couches, le panneau de d&eacute;tail ci-contre propose : <ul><li>une vue rapide navigable dans l\'onglet Vue,</li><li>une navigation dans la table attributaire pour les donn&eacute;es vectorielles dans l\'onglet Donn&eacute;es</li><li>une pr&eacute;sentation de quelques informations descriptives de la donn&eacute;e dans l\'onglet Metadonn&eacute;es.</li></ul> Le bouton Suivant permet d\'acc&eacute;der au composeur de cartes.'),
                        buttonAlign: 'center',
                        items: [
                           new Ext.Button(action)
                        ]
                    });
                    
                    Ext.getCmp('pnlDataList').add(panel);
    
                    // render the tree
                    Ext.getCmp('pnlDataList').doLayout();
                    tree.render();
                    root.expand();
                    //Hides loading msgbox
                    Ext.Msg.hide();
        </script>
        
