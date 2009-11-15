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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@  page contentType="text/html"%>
<%@  page import="java.util.*" %>
<%@  page import="java.io.*" %>
<%@  page import="java.net.*" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@  page import="org.geogurus.tools.DataManager" %>
<%@  page import="org.geogurus.data.Datasource" %>
<%@  page import="org.geogurus.data.DataAccess" %>
<%@  page import="org.geogurus.gas.objects.GeometryClassFieldBean" %>
<%@  page import="org.geogurus.gas.objects.LayerGeneralProperties" %>
<%@  page import="org.geogurus.mapserver.objects.Layer" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<logic:present name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>">
    <script type="text/javascript">
            var pAttributeInfo;
            var pFileInfo;
            var map, layer, vlayer;
            
            function initLayout() {

                //------------------------------------------//
                //                 QUICKVIEW                //
                //------------------------------------------//
                loadMap('<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="imgURL" />');

                //------------------------------------------//
                //                 DATA VIEW                //
                //------------------------------------------//
                GeneralLayout.headers = [];
                GeneralLayout.columnsModels = [];
                if(GeneralLayout.store) {
                    GeneralLayout.store.destroy();
                    GeneralLayout.store = null;
                }
                if(GeneralLayout.grid) {
                    GeneralLayout.grid.destroy();
                    GeneralLayout.grid = null;
                }

                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="tifffile">
                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="imgfile">
                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="WMS">
                //Attributes Grid
                //headers
                <logic:iterate id="fs" name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="fields">
                GeneralLayout.headers.push({'name' : '<bean:write name="fs" property="name"/>'});
                GeneralLayout.columnsModels.push({header: '<bean:write name="fs" property="name"/>', width: 80, sortable: true, dataIndex: '<bean:write name="fs" property="name"/>'});
                </logic:iterate>

                GeneralLayout.store = new Ext.data.Store({
                    proxy: new Ext.data.HttpProxy({
                        url: 'getSampleData.do'
                    }),
                    // create reader that reads the Topic records
                    reader: new Ext.data.JsonReader({
                        root: 'enregistrements',
                        totalProperty: 'totalCount',
                        fields: GeneralLayout.headers
                    })
                });
                
                GeneralLayout.grid = new Ext.grid.GridPanel({
                    id:'gridData',
                    cm: new Ext.grid.ColumnModel(GeneralLayout.columnsModels),
                    store: GeneralLayout.store,
                    viewConfig: {
                        //forceFit:true
                        autoFill:true
                    },
                    bbar: new Ext.PagingToolbar({
                        pageSize: 50,
                        store: GeneralLayout.store,
                        displayInfo: true,
                        displayMsg: i18n.records + ' {0} - {1} ' + i18n.of + ' {2}',
                        emptyMsg: i18n.none + ' ' + i18n.records
                    })
                });

                // render grid
                Ext.getCmp('data').add(GeneralLayout.grid);
                // trigger the data store load
                GeneralLayout.store.load({params:{start:0, limit:50}});
                Ext.getCmp('data').doLayout();
                Ext.getCmp('pnlDataDetail').unhideTabStripItem('data');
                </logic:notEqual>
                </logic:notEqual>
                </logic:notEqual>
                
                //Hides Data Tab
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="tifffile">
                    Ext.getCmp('pnlDataDetail').hideTabStripItem('data');
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="imgfile">
                    Ext.getCmp('pnlDataDetail').hideTabStripItem('data');
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="WMS">
                    Ext.getCmp('pnlDataDetail').hideTabStripItem('data');
                </logic:equal>

                //------------------------------------------//
                //               METADATA VIEW              //
                //------------------------------------------//
                if(!Ext.getCmp('pFileInfo')) {
                    //Panel
                    pFileInfo = new Ext.Panel({
                        id: 'pFileInfo',
                        title: '<bean:message key="file_info"/>',
                        collapsible:true,
                        width:400
                    });
                    Ext.getCmp('metadata').add(pFileInfo);
                }
                //updates previous form if existing
                if(Ext.getCmp('frmFileInfo')) {
                    Ext.getCmp('frmFileInfo').getForm().setValues({
                        metadata_name:       '<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="name"/>',
                        metadata_source_type:'<bean:message name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType"/>',
                        metadata_object_type:'<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="geoType"/>',
                        metadata_num_objects:'<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="numRecords"/>',
                        metadata_projection: '<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="projection"/>'
                    });
                } else {
                    //builds new form
                    var frmFileInfo= new Ext.FormPanel({
                        id:'frmFileInfo',
                        labelWidth: 75,
                        frame:true,
                        bodyStyle:'padding:5px 5px 0',
                        width: '100%',
                        defaults: {width: 260},
                        defaultType: 'textfield',

                        items: [{
                                name:"metadata_name",
                                fieldLabel: "<bean:message key="name"/>",
                                value: "<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="name"/>",
                                disabled:true
                            },{
                                name:"metadata_source_type",
                                fieldLabel: "<bean:message key="source_type_upper"/>",
                                value: "<bean:message name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType"/>",
                                disabled:true
                            },{
                                name:"metadata_object_type",
                                fieldLabel: "<bean:message key="object_type"/>",
                                value: "<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="geoType"/>",
                                disabled:true
                            }, {
                                name:"metadata_num_objects",
                                fieldLabel: "<bean:message key="num_objects"/>",
                                value: "<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="numRecords"/>",
                                disabled:true
                            }, {
                                name:"metadata_projection",
                                fieldLabel: "<bean:message key="projection"/>",
                                value: "<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="projection"/>",
                                disabled:true
                            }
                        ]
                    });
                    pFileInfo.add(frmFileInfo);
                }
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="imgfile">
                    if(Ext.getCmp('pAttributeInfo')) Ext.getCmp('pAttributeInfo').hide();
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="tifffile">
                    if(Ext.getCmp('pAttributeInfo')) Ext.getCmp('pAttributeInfo').hide();
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="WMS">
                    if(Ext.getCmp('pAttributeInfo')) Ext.getCmp('pAttributeInfo').hide();
                </logic:equal>
                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="tifffile">
                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="imgfile">
                <logic:notEqual name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dsType" value="WMS">
                if(!Ext.getCmp('pAttributeInfo')) {
                    pAttributeInfo = new Ext.Panel({
                        id: 'pAttributeInfo',
                        title: '<bean:message key="attribute_info"/>',
                        collapsible:true,
                        width:400
                    });
                    Ext.getCmp('metadata').add(pAttributeInfo);
                }
                Ext.getCmp('pAttributeInfo').show();
                
                // create the data store
                var aiData = [];
                <logic:iterate id="fs" name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="fields">
                aiData.push(['<bean:write name="fs" property="name"/>','<bean:write name="fs" property="type"/>','<bean:write name="fs" property="length"/>','<bean:write name="fs" property="nullable"/>']);
                </logic:iterate>
                //fills the grid
                if(Ext.getCmp('gridAttributeInfo')) {
                    Ext.getCmp('gridAttributeInfo').getStore().removeAll();
                    Ext.getCmp('gridAttributeInfo').getStore().loadData(aiData);
                } else {
                    var aistore = new Ext.data.SimpleStore({
                        fields: [
                           {name: '<bean:message key="name" />'},
                           {name: '<bean:message key="type_upper" />'},
                           {name: '<bean:message key="length" />'},
                           {name: '<bean:message key="nullable" />'}
                        ]
                    });
                    aistore.loadData(aiData);

                    // create the Grid
                    var gridAttributeInfo = new Ext.grid.GridPanel({
                        id: 'gridAttributeInfo',
                        store: aistore,
                        columns: [
                            {header: "<bean:message key="name" />", width: 160},
                            {header: "<bean:message key="type_upper" />", width: 75},
                            {header: "<bean:message key="length" />", width: 75},
                            {header: "<bean:message key="nullable" />", width: 75}
                        ],
                        height:350,
                        width:400
                    });
                    pAttributeInfo.add(gridAttributeInfo);
                    pAttributeInfo.doLayout();
                }
                </logic:notEqual>
                </logic:notEqual>
                </logic:notEqual>
            }

            function loadMap(imgUrl) {
                
                var bounds = new OpenLayers.Bounds(<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="dataAccess.extent.bounds"/>);
                var options = {maxResolution: 'auto',maxExtent: bounds};
                if(!map) {
                    map = new OpenLayers.Map(Ext.getCmp('view').body.id, options);
                    map.addControl(new OpenLayers.Control.MousePosition());
                } else {
                    map.setOptions(options);
                }
                if(layer) {
                    layer.destroy();
                }
                if(imgUrl != 'null') {
                    layer = new OpenLayers.Layer.MapServer('<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="name"/>', 
                             '<%=DataManager.getProperty("MAPSERVERURL")%>?mode=map&map=<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="escapedRootPath"/>',
                             {layers: '<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="name"/>'}, 
                             {singleTile: false,'buffer':0, transitionEffect:'resize'});
                    map.addLayer(layer);
                   // console.log("building a new layer: " + layer);
                    map.zoomToExtent(bounds);
                    catalogMsUrl = '<%=DataManager.getProperty("MAPSERVERURL")%>?mode=map&map=<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="escapedRootPath"/>&layers=<bean:write name="<%=ObjectKeys.LAYER_GENERAL_PROPERTIES%>" property="name"/>&map_size=400+400';
                } else {
                    map.destroy();
                    map = null;
                    catalogMsUrl = null;
                }
                
            }

            Ext.onReady(
                function() {
                    initLayout();
                    if(Ext.getCmp('data').ownerCt.activeTab == Ext.getCmp('data'))
                        Ext.getCmp('gridData').getView().renderUI();
                }
            );
    </script>
</logic:present>
