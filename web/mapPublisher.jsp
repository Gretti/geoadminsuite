<%-- 
    Document   : mapPublisher
    Created on : 16 janv. 2008, 22:42:36
    Author     : gnguessan
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="org.geogurus.gas.objects.UserMapBean" %>
<%@ page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@ page import="org.geogurus.data.DataAccessType" %>
<%@ page import="org.geogurus.data.DatasourceType" %>

<script type="text/javascript" charset="utf-8">
    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store (created below)
    var datasourcetypes = [];
<%	
    for (DataAccessType dataType:DataAccessType.values()) {
        String displayName = dataType.displayname();
        if(dataType.getType() == DatasourceType.VECTOR) {
%>
	    datasourcetypes.push(['<%=dataType%>','<%=displayName%>']);
<%
        }
    }
%>
    var cmVector = new Ext.grid.ColumnModel([
            {
               id:'layer',
               header: '<bean:message key="publisher.dl.layer"/>',
               dataIndex: 'layer',
               width: 150
            },{
               header: "Type",
               dataIndex: 'type',
               width: 130,
               editor: new Ext.form.ComboBox({
                                        store: new Ext.data.SimpleStore({
                                            fields: ['typeCode', 'type'],
                                            data : datasourcetypes
                                        }),
                                        displayField:'type',
                                        valueField:'type',
                                        typeAhead: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: false,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                })
            },{
               header: "DB Name",
               dataIndex: 'dbname',
               width: 70,
               editor: new Ext.form.TextField({allowBlank: true})
            },{
               header: "Download",
               dataIndex: 'download',
               width: 70
            }
    ]);
   //MAPFISH MAP COMPONENT
    var bounds = new OpenLayers.Bounds(<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapExtent"/>);
    var options = {maxResolution: 'auto',maxExtent: bounds, projection: proj};
    <logic:notEmpty name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection">
    var proj = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection.attributes" />';
    if (proj.length > 2) {
        proj = proj.split('=')[1].split("&")[0];
    }
    //options.push({projection: proj});
    options.projection = proj;
    </logic:notEmpty>
    
    Ext.get('pnlPublisherResult').update("<div id='publishertoolbar' style='width:600px'></div><div id='publishertree' style='position:absolute;left:620px;'></div><div id='publishermap' style='width:600px;height:600px;border:1px solid black'></div>");
    GeneralLayout.publishermap = new OpenLayers.Map($('publishermap'), options);
    GeneralLayout.publishermap.theme = 'theme/default/style.css';
    
    //Builds layers
    GeneralLayout.publisherlayers = [];
    var mylayer;
    var myLayers = [];
            
    // by default columns are sortable
    cmVector.defaultSortable = true;
    // create the Data Store
    var myData = [];
    var dlString, params;
    var pchildren = [];
    
<logic:iterate id="order" indexId="cnt" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="userLayerOrder">
    <bean:define id='gc' name='<%=ObjectKeys.USER_MAP_BEAN%>' property='<%="userLayer(" + order + ")"%>' type='org.geogurus.data.DataAccess'/>
    
    params = '\'<bean:write name="order"/>\',';
    params += '\'<bean:write name="cnt"/>\'';
    
    dlString = '<a href="javascript:void(0);" onclick="javascript:downloadData(' + params + ');">';
    dlString += '<img src="styles/package_go.png" alt=\"<bean:message key="download"/>\" title=\"<bean:message key="download"/>\" border=\"0\">';
    dlString += "</a>";
    <logic:equal name="gc" property="datasourceType.type" value="<%=DatasourceType.VECTOR.toString()%>">
        myData.push(['<bean:write name="gc" property="name"/>','<bean:write name="gc" property="datasourceTypeAsString"/>','',dlString]);
    </logic:equal>
    myLayers.push('<bean:write name="gc" property="name"/>'); 
    pchildren.push(
        {
            id: "<bean:write name="gc" property="ID"/>",
            text: "<bean:write name="gc" property="name"/>",
            leaf: true,
            layerName: "mapserverLayer:<bean:write name="gc" property="name"/>",
            checked: true,
            icon: 'images/layers.png'
        }
    );
</logic:iterate>
    var pmodel = [
            {
                text: "Layer tree",
                expanded: true,
                checked: true,
                children: pchildren
            }
        ];
    
    mylayer = new OpenLayers.Layer.MapServer('mapserverLayer', 
             '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>',
             {layers: myLayers,format: "image/png"}, 
             {transitionEffect:'resize',isBaseLayer:false,singleTile:true});
    GeneralLayout.publisherlayers[GeneralLayout.publisherlayers.length] = mylayer;
    var drawingLayer = new OpenLayers.Layer.Vector("Draw",{isBaseLayer:true});

    GeneralLayout.publishermap.addLayer(drawingLayer);
    GeneralLayout.publishermap.addLayers(GeneralLayout.publisherlayers);

    //Adds pre-added controls if existing, parsing treeSelectedControls nodes
    for(var sel=0; sel<Ext.getCmp('treeSelectedComponents').root.childNodes.length; sel++) {
        var c = Ext.getCmp('treeSelectedComponents').root.childNodes[sel].id;
        if(c.split('_')[0] == 'OL') {
            var objCtrlToAdd = GeneralLayout.correspControls[c];
            var ctrlToAdd = eval(objCtrlToAdd.tool);
            ctrlToAdd.id = 'ctrl_' + c;
            //adds control to the map and refreshes it
            GeneralLayout.publishermap.addControl(ctrlToAdd,null);
        } else {
           if(c == 'MF_LayerTree') {
               //need to rebuild tree in case of changes inside
               GeneralLayout.publishertree.destroy();
               GeneralLayout.publishertree = new mapfish.widgets.LayerTree({
                    id:'publisherTree',
                    map: GeneralLayout.publishermap, 
                    model: pmodel,
                    enableDD:true,
                    width: '100%',
                    height: '100%',
                    border: false,
                    autoScroll: true
               });
               GeneralLayout.publishertree.render('publishertree');
           }

           if(c == 'MF_NavToolbar') {
               //**FIXME : rebuild bar instead of displaying it
               GeneralLayout.publishertoolbar.destroy();
               GeneralLayout.publishertoolbar = new mapfish.widgets.toolbar.Toolbar({map: GeneralLayout.publishermap, configurable: false});
               GeneralLayout.publishertoolbar.render('publishertoolbar');
               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.ZoomBox(), {iconCls: 'bzoomin', toggleGroup: 'map'});
               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DragPan({isDefault: true}), {iconCls: 'bdrag', toggleGroup: 'map'});
               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.ZoomToMaxExtent(), {iconCls: 'bzoomtomax', toggleGroup: 'map'});

               GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Spacer());
               GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Separator());
               GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Spacer());

               var vectorLayer = GeneralLayout.publishermap.getLayersByName('Draw')[0];

               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Point), {iconCls: 'bdrawpoint', toggleGroup: 'map'});
               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Path), {iconCls: 'bdrawline', toggleGroup: 'map'});
               GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Polygon), {iconCls: 'bdrawpolygon', toggleGroup: 'map'});

               GeneralLayout.publishertoolbar.activate();               
           }
        }
    }
            
    var storeVector = new Ext.data.SimpleStore({
        fields: [
           {name: 'layer'},
           {name: 'type'},
           {name: 'dbname'},
           {name: 'download'}
        ]
    });
    
    // create the editor grid
    var grid = new Ext.grid.EditorGridPanel({
        id:'publisherDownloadGrid',
        store: storeVector,
        cm: cmVector,
        autoExpandColumn:'layer',
        title:'Layers',
        frame:false,
        clicksToEdit:1,
        bodyBorder:true,
        border:true,
        collapsible:true,
        animCollapse:false,
        viewConfig: {forceFit: true}
    });
    
    // panel for other download
    var other = new Ext.Panel({
        id: 'pnlOtherDownload',
        title: 'Other Downloads',
        animCollapse:false,
        collapsible: true,
        margins:'3 0 3 3',
        cmargins:'3 3 3 3',
        autoScroll:true
    });
                
    var strdlmap = '<table style="border:0;width:100%;"><tr>';
    strdlmap += '<td style="font-family:arial,tahoma,helvetica,sans-serif;font-size:11px;"><bean:message key="map_file"/></td>';
    strdlmap += '<td>';
    strdlmap += '<a href="zipDownload.do?exporttype=EXPORT_TYPE_TEXT" target="_blank" alt=\"<bean:message key="see"/>\">';
    strdlmap += '<img src="styles/report.png" border="0" alt=\"<bean:message key="see"/>\" title=\"<bean:message key="see"/>\">';
    strdlmap += '</a>';
    strdlmap += '</td>';
    strdlmap += '<td>';
    strdlmap += '<a href="zipDownload.do?exporttype=EXPORT_TYPE_FULL" alt=\"<bean:message key="download_map"/>\">';
    strdlmap += '<img src="styles/report_go.png" border="0" alt=\"<bean:message key="download"/>\" title=\"<bean:message key="download"/>\">';
    strdlmap += '</a>';
    strdlmap += '</td>';
    strdlmap += '<tr>';
    strdlmap += '<td style="font-family:arial,tahoma,helvetica,sans-serif;font-size:11px;"><bean:message key="cartoweb.iniConfFiles"/></td>';
    strdlmap += '<td>';
    strdlmap += '&nbsp;';
    strdlmap += '</td>';
    strdlmap += '<td>';
    strdlmap += '<a href="zipDownload.do?exporttype=EXPORT_TYPE_CARTOWEB" alt=\"<bean:message key="download"/>\">';
    strdlmap += '<img src="styles/report_go.png" border="0" alt=\"<bean:message key="download"/>\" title=\"<bean:message key="download"/>\">';
    strdlmap += '</a>';
    strdlmap += '</td></tr></table><br>'
    strdlmap += GeneralLayout.createBoxHelp('<img src="images/help.png">','To Internat:Liste des autres types d\'export possibles : export des donn&eacute;es avec changement de format, exports de la configuration cartographique.<br>Cliquer sur les liens images pour lancer le téléchargement.');
    
    // trigger the data store load
    storeVector.loadData(myData);
    Ext.getCmp('pnlPublisherDownload').add(grid);
    Ext.getCmp('pnlPublisherDownload').add(other);
    Ext.getCmp('pnlPublisherDownload').doLayout();
    grid.render;
    Ext.getCmp('pnlOtherDownload').body.dom.innerHTML = strdlmap;
    
    customizeConfigurationMap(GeneralLayout.publishermap);
    GeneralLayout.publishermap.zoomToExtent(GeneralLayout.publishermap.baseLayer.maxExtent);
    
    function downloadData(order,cnt) {
        var dlItem =grid.store.data.items[parseInt(cnt)];
        //controls if datasource db name is specified in case of db type selected
        if(dlItem.data.type == '<%=DataAccessType.POSTGIS.displayname()%>') {
            if(dlItem.data.dbname == '') {
                Ext.Msg.show({
                   title:'<bean:message key="error"/>',
                   msg: "<bean:message key="noDBname"/>",
                   buttons: Ext.Msg.OK,
                   icon: Ext.MessageBox.ERROR
                });
                return;
            } else {
                db = dlItem.data.dbname;
            }
        }
        var dltype;
        for (var d=0; d<datasourcetypes.length;d++){
            if(datasourcetypes[d][1] == dlItem.data.type){
                dltype = datasourcetypes[d][0];
                break;
            }
        }
        var db = "";
        var url = 'zipDownload.do?currentlayer=' + order + '&exporttype=' + dltype + '&dbname=' + db;
        var w = window.open(url, "wait", "width=300,height=200");
        w.focus();
    }
    
</script>