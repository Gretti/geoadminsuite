<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page contentType="text/html"%>
<%@ page import="java.io.File" %>
<%@ page import="org.geogurus.tools.DataManager" %>
<%@ page import="org.geogurus.Datasource" %>
<%@ page import="org.geogurus.GeometryClass" %>
<%@ page import="org.geogurus.gas.utils.ObjectKeys" %>

        <script type="text/javascript">
                <!--
                var i, j, k, img, nodeServer, nodeDatasource, nodeLayer;
                var imgserver = 'images/server.png';
                var imgdatabase = 'images/database.png';
                var imgfolder = 'images/folder.gif';
                var imglayer = 'images/layers.png';

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

                <logic:present name="<%=ObjectKeys.HOST_LIST%>" scope="session">
                    <%// Loops on servers%>
                    <logic:iterate id="host" indexId="cntHost" name="<%=ObjectKeys.HOST_LIST%>">
                i = 'host' + <bean:write name="cntHost"/>;
                nodeServer = new Ext.tree.TreeNode({text:'<bean:write name="host" property="key"/>', id:i, icon:imgserver});

                root.appendChild(nodeServer);
                <%// Loops on datasources%>
                        <logic:iterate id="datasource" indexId="cntDs" name="host" property="value">
                j = i + 'ds' + <bean:write name="cntDs"/>;
                            <logic:equal name="datasource" property="type" value="<%=new Short(Datasource.FOLDER).toString()%>">
                img = imgfolder;
                            </logic:equal>
                            <logic:notEqual name="datasource" property="type" value="<%=new Short(Datasource.FOLDER).toString()%>">
                img = imgdatabase;
                            </logic:notEqual>
                nodeDatasource = new Ext.tree.TreeNode({text: '<bean:write name="datasource" property="name"/>', id: j, icon: img});
                nodeServer.appendChild(nodeDatasource);
                            <%// Loops on layers%>
                            <logic:iterate id="gc" indexId="cntGc" name="datasource" property="sortedDataList" type="GeometryClass">
                k = j + 'gc' + <bean:write name="cntGc"/>;
                nodeLayer = new Ext.tree.TreeNode({text: '<bean:write name="gc" property="name"/>', id: "<bean:write name="datasource" property="host"/>,<bean:write name="gc" property="ID"/>", checked: false,icon:imglayer});
                nodeLayer.on('click',function(n){
                                var paramstr = 'st=data&host=<bean:write name="host" property="key" filter="false"/>&layerID=<bean:write name="gc" property="ID"/>&dsID=<bean:write name="cntDs"/>';
                                Ext.get("data_detail").load({
                                    url: "infoDatasources.do",
                                    params: paramstr,
                                    scripts:true
                                });
                                //Ext.getCmp('pnlDataDetail').activate('view');
                });
                nodeDatasource.appendChild(nodeLayer);
                            </logic:iterate>
                        </logic:iterate>
                    </logic:iterate>
                </logic:present>


                //-----------------------------//
                //           BUTTON NEXT       //
                //-----------------------------//
                    
                   var action = new Ext.Action({
                        text: 'Next',
                        handler: GeneralLayout.gotoComposer,
                        iconCls: 'bnext'
                   });
                    
                   var panel = new Ext.Panel({
                        id:'btnGotoComposer',
                        width:600,
                        height:300,
                        bodyStyle: 'padding:10px;',
                        autoHeight: true,
                        bodyBorder: false,
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
                //-->
        </script>
        
