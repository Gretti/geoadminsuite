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
<%@ page import="org.geogurus.GeometryClass" %>

<script type="text/javascript" charset="utf-8">
    // the column model has information about grid columns
    // dataIndex maps the column to the specific data field in
    // the data store (created below)
    var datasourcetypes = [];
<%
    String dst;
    Byte dstb;
    for (int i=0; i < GeometryClass.DATASOURCE_TYPES_ASSTRING.length; i++) {
        dst = GeometryClass.DATASOURCE_TYPES_ASSTRING[i];
        dstb = GeometryClass.DATASOURCE_TYPES_ASBYTE[i];
%>
datasourcetypes.push(['<%=dstb%>','<%=dst%>'])
<%
    }
%>
    var cm = new Ext.grid.ColumnModel([
            {
               id:'layer',
               header: "Layer",
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
    var proj = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection.attributes"/>';
    proj = proj.split('=')[1].split("&")[0];
    var options = {maxResolution: 'auto',maxExtent: bounds, projection: proj};
            
    GeneralLayout.publishermap = new OpenLayers.Map(Ext.getCmp('pnlPublisherResult').body.id, options);
            
    //Builds layers
    GeneralLayout.publisherlayers = [];
    var layer;
    var myLayers = [];
            
    // by default columns are sortable
    cm.defaultSortable = true;
    // create the Data Store
    var myData = [];
    var dlString, params;
    
<logic:iterate id="order" indexId="cnt" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="userLayerOrder">
    <bean:define id='gc' name='<%=ObjectKeys.USER_MAP_BEAN%>' property='<%="userLayer(" + order + ")"%>' type='org.geogurus.GeometryClass'/>
    
    params = '\'<bean:write name="order"/>\',';
    params += '\'<bean:write name="cnt"/>\'';
    
    dlString = '<a href="javascript:void(0);" onclick="javascript:downloadData(' + params + ');">';
    dlString += '<img src="ext/styles/package_go.png" alt=\"<bean:message key="download"/>\" title=\"<bean:message key="download"/>\" border=\"0\">';
    dlString += "</a>";
    
    myData.push(['<bean:write name="gc" property="name"/>','<bean:write name="gc" property="datasourceTypeAsString"/>','',dlString]);
    myLayers.push('<bean:write name="gc" property="name"/>'); 
</logic:iterate>
                     
    layer = new OpenLayers.Layer.MapServer('myGroup', 
             '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>',
             {layers: myLayers,format: "image/png",'map_scalebar_status':'OFF'}, 
             {isBaseLayer:true});
    GeneralLayout.publisherlayers[GeneralLayout.publisherlayers.length] = layer;

    GeneralLayout.publishermap.addLayers(GeneralLayout.publisherlayers);
            
    var store = new Ext.data.SimpleStore({
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
        store: store,
        cm: cm,
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
    strdlmap += '<a href="ZipDownload.jsv?exporttype=-1" target="_blank" alt=\"<bean:message key="see"/>\">';
    strdlmap += '<img src="ext/styles/report.png" border="0" alt=\"<bean:message key="see"/>\" title=\"<bean:message key="see"/>\">';
    strdlmap += '</a>';
    strdlmap += '</td>';
    strdlmap += '<td>';
    strdlmap += '<a href="ZipDownload.jsv?exporttype=-2" alt=\"<bean:message key="download_map"/>\">';
    strdlmap += '<img src="ext/styles/report_go.png" border="0" alt=\"<bean:message key="download"/>\" title=\"<bean:message key="download"/>\">';
    strdlmap += '</a>';
    strdlmap += '</td>';
    
    // trigger the data store load
    store.loadData(myData);
    Ext.getCmp('pnlPublisherDownload').add(grid);
    Ext.getCmp('pnlPublisherDownload').add(other);
    Ext.getCmp('pnlPublisherDownload').doLayout();
    grid.render;
    Ext.getCmp('pnlOtherDownload').body.dom.innerHTML = strdlmap;
    GeneralLayout.publishermap.zoomToExtent(bounds);
    
    function downloadData(order,cnt) {
        var dlItem =grid.store.data.items[parseInt(cnt)];
        console.log(dlItem);
        //controls if datasource db name is specified in case of db type selected
        if(dlItem.data.type == '<%=GeometryClass.STRING_PGCLASS%>') {
            if(dlItem.data.dbname == '') {
                Ext.Msg.show({
                   title:'<bean:message key="error"/>',
                   msg: "<bean:message key="noDBname"/>",
                   buttons: Ext.Msg.OK,
                   icon: Ext.MessageBox.ERROR
                });
                return false;
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
        var url = 'ZipDownload.jsv?currentlayer=' + order + '&exporttype=' + dltype + '&dbname=' + db;
        var w = window.open(url, "wait", "width=300,height=200");
        w.focus();
    }
    
</script>