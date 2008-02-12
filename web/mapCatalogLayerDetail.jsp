<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@  page contentType="text/html"%>
<%@  page import="java.util.*" %>
<%@  page import="java.io.*" %>
<%@  page import="java.net.*" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@  page import="org.geogurus.tools.DataManager" %>
<%@  page import="org.geogurus.Datasource" %>
<%@  page import="org.geogurus.GeometryClass" %>
<%@  page import="org.geogurus.gas.objects.GeometryClassFieldBean" %>
<%@  page import="org.geogurus.web.LayerGeneralProperties" %>
<%@  page import="org.geogurus.mapserver.objects.Layer" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<logic:present name="dgp">
    <script type="text/javascript">
                <!--
            var pAttributeInfo;
            var pFileInfo;
            var map, layer;
            
            function initLayout() {

                //------------------------------------------//
                //                 QUICKVIEW                //
                //------------------------------------------//
                loadMap('<bean:write name="dgp" property="imgURL"/>');

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

                <logic:notEqual name="dgp" property="dsType" value="tifffile">
                <logic:notEqual name="dgp" property="dsType" value="imgfile">
                //Attributes Grid
                //headers
                <logic:iterate id="fs" name="dgp" property="fields">
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
                    viewConfig: {forceFit:true},
                    bbar: new Ext.PagingToolbar({
                        pageSize: 50,
                        store: GeneralLayout.store,
                        displayInfo: true,
                        displayMsg: 'Enregistrements {0} - {1} of {2}',
                        emptyMsg: "Aucun enregistrements"
                    })
                });

                // render grid
                Ext.getCmp('data').add(GeneralLayout.grid);
                // trigger the data store load
                GeneralLayout.store.load({params:{start:0, limit:50}});

                </logic:notEqual>
                </logic:notEqual>

                //------------------------------------------//
                //               METADATA VIEW              //
                //------------------------------------------//
                if(!Ext.getCmp('pFileInfo')) {
                    //Panel
                    pFileInfo = new Ext.Panel({
                        id: 'pFileInfo',
                        title: 'File Info',
                        collapsible:true,
                        width:400
                    });
                    Ext.getCmp('metadata').add(pFileInfo);
                }
                //updates previous form if existing
                if(Ext.getCmp('frmFileInfo')) {
                    Ext.getCmp('frmFileInfo').getForm().setValues({
                        metadata_name:"<bean:write name="dgp" property="name"/>",
                        metadata_source_type:"<bean:message name="dgp" property="dsType"/>",
                        metadata_object_type:"<bean:write name="dgp" property="geoType"/>",
                        metadata_num_objects:"<bean:write name="dgp" property="numRecords"/>",
                        metadata_projection:"<bean:write name="dgp" property="projection"/>"
                    });
                } else {
                    //builds new form
                    var frmFileInfo= new Ext.FormPanel({
                        id:'frmFileInfo',
                        labelWidth: 75, // label settings here cascade unless overridden
                        frame:true,
                        bodyStyle:'padding:5px 5px 0',
                        width: 350,
                        defaults: {width: 230},
                        defaultType: 'textfield',

                        items: [{
                                name:"metadata_name",
                                fieldLabel: "<bean:message key="name"/>",
                                value: "<bean:write name="dgp" property="name"/>",
                                disabled:true
                            },{
                                name:"metadata_source_type",
                                fieldLabel: "<bean:message key="source_type_upper"/>",
                                value: "<bean:message name="dgp" property="dsType"/>",
                                disabled:true
                            },{
                                name:"metadata_object_type",
                                fieldLabel: "<bean:message key="object_type"/>",
                                value: "<bean:write name="dgp" property="geoType"/>",
                                disabled:true
                            }, {
                                name:"metadata_num_objects",
                                fieldLabel: "<bean:message key="num_objects"/>",
                                value: "<bean:write name="dgp" property="numRecords"/>",
                                disabled:true
                            }, {
                                name:"metadata_projection",
                                fieldLabel: "<bean:message key="projection"/>",
                                value: "<bean:write name="dgp" property="projection"/>",
                                disabled:true
                            }
                        ]
                    });
                    pFileInfo.add(frmFileInfo);
                }
                <logic:equal name="dgp" property="dsType" value="imgfile">
                    if(Ext.getCmp('pAttributeInfo')) Ext.getCmp('pAttributeInfo').hide();
                </logic:equal>
                <logic:equal name="dgp" property="dsType" value="tifffile">
                    if(Ext.getCmp('pAttributeInfo')) Ext.getCmp('pAttributeInfo').hide();
                </logic:equal>
                <logic:notEqual name="dgp" property="dsType" value="tifffile">
                <logic:notEqual name="dgp" property="dsType" value="imgfile">
                if(!Ext.getCmp('pAttributeInfo')) {
                    pAttributeInfo = new Ext.Panel({
                        id: 'pAttributeInfo',
                        title: 'Attribute Info',
                        collapsible:true,
                        width:400
                    });
                    Ext.getCmp('metadata').add(pAttributeInfo);
                }
                Ext.getCmp('pAttributeInfo').show();
                
                // create the data store
                var aiData = [];
                <logic:iterate id="fs" name="dgp" property="fields">
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
            }

            function loadMap(imgUrl) {
                
                var bounds = new OpenLayers.Bounds(<bean:write name="dgp" property="geometryClass.extent.bounds"/>);
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
                if(imgUrl != 'images/noobjects.gif') {
                    layer = new OpenLayers.Layer.MapServer('<bean:write name="dgp" property="name"/>', 
                             '<%=DataManager.getProperty("MAPSERVERURL")%>' + '?mode=map&map=<bean:write name='dgp' property='rootPath'/>',
                             {layers: '<bean:write name="dgp" property="name"/>'}, 
                             {singleTile: false});
                    map.addLayer(layer);
                    map.zoomToExtent(bounds);
                } else {
                    map.destroy();
                    map = null;
                }
                
            }
            Ext.onReady(initLayout);
            //-->
    </script>
</logic:present>
