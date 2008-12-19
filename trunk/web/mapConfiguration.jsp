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
<%@  page contentType="text/html" language="java"%>
<%@  page import="org.geogurus.data.Datasource" %>
<%@  page import="org.geogurus.data.DataAccess" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@  page import="org.geogurus.tools.DataManager" %>
<%@  page import="org.geogurus.mapserver.objects.Map" %>
<%@  page import="java.io.File" %>

<%
        String rootPath = this.getServletConfig().getServletContext().getRealPath("") + File.separator;
        rootPath = rootPath.replace('\\', '/');
%>

<script type="text/javascript" src="scripts/Utils.js"></script>
<script type="text/javascript" charset="utf-8">
    var generalPropsTree;
    var myLayers = [];
    var children = [];
    var initialOrder = [];
    var activeLayer = null;
    var locChildren = [];

    // the globally-defined properties windows content displaying classes, labels, CW, etc. properties
    // this is an ext Panel with .url properties.
    var contentProps = null;

    function loadMap() {
        //MAPFISH MAP COMPONENT
        var bounds = new OpenLayers.Bounds(<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapExtent"/>);
        //var bounds = new OpenLayers.Bounds(-20037508, -20037508,20037508, 20037508.34);


        GeneralLayout.composerActiveExtent = bounds;
        var options = {maxResolution: 'auto',maxExtent: bounds};
            <logic:notEmpty name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection">
                    var proj = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection.attributes" />';
                    if (proj.length > 2) {
                        proj = proj.split('=')[1].split("&")[0];
                    }
                    options.projection = proj;
            </logic:notEmpty>
            <logic:equal name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.units" value="<%=String.valueOf(Map.METERS)%>">
                    options.units = 'm';
            </logic:equal>
//                    options.projection = new OpenLayers.Projection("EPSG:900913");
//                    options.displayProjection = new OpenLayers.Projection("EPSG:4326");

                    GeneralLayout.zoombox = new OpenLayers.Control.ZoomBox();
                    GeneralLayout.zoomoutbox = new OpenLayers.Control.ZoomBox({out:true});
                    GeneralLayout.dragpan = new OpenLayers.Control.DragPan();

                    GeneralLayout.composermap = new OpenLayers.Map(Ext.getCmp('pnlComposerMap').body.id, options);
                    GeneralLayout.composermap.theme = 'theme/default/style.css';

                    GeneralLayout.composermap.addControl(new OpenLayers.Control.MousePosition());
                    GeneralLayout.composermap.addControl(GeneralLayout.zoombox);
                    GeneralLayout.composermap.addControl(GeneralLayout.zoomoutbox);
                    GeneralLayout.composermap.addControl(GeneralLayout.dragpan);
                    GeneralLayout.composermap.addControl(new OpenLayers.Control.Scale());
                    GeneralLayout.composermap.addControl(new OpenLayers.Control.ScaleLine());

                    //Adds a searcher to the map
                    if(Ext.getCmp('infoTool').pressed) Ext.getCmp('infoTool').toggle();
                    Ext.getCmp('infoTool').disable();
                    if(GeneralLayout.searcher) GeneralLayout.searcher.destroy();
                    var protocol = mapfish.Protocol.decorateProtocol({
                        protocol: new mapfish.Protocol.MapFish({
                            url: 'searchFeature.do',
                            params: {maxfeatures: 1}
                        }),
                        TriggerEventDecorator: {
                            eventListeners: {
                                crudfinished: function(reponse) {
                                    if(reponse.priv.responseText == '') {
                                        Ext.Msg.alert('Empty result','No features found');
                                        return;
                                    }
                                    var feats = Ext.util.JSON.decode(reponse.priv.responseText);
                                    var fields = feats.fields;
                                    var attributes = feats.attributes;
                                    if (attributes && attributes.length > 0) {
                                        // build ugly HTML table
                                        var html = "<table>";
                                        for (var k = 0 ; k < fields.length; k++) {
                                            html += "<tr>";
                                            html += "<th><b>" + fields[k] + " : </b></th>";
                                            html += "<td>" + attributes[k] + "</td>";
                                            html += "<tr>";
                                        }
                                        html += "</table>";
                                        var popup = new OpenLayers.Popup.FramedCloud(
                                            "mapfish_popup",    // popup id
                                            GeneralLayout.searcher.popupLonLat,   // OpenLayers.LonLat object
                                            null,               // popup is autosized
                                            html,               // html string
                                            null,               // no anchor
                                            true                // close button
                                        );
                                        GeneralLayout.searcher.map.addPopup(popup, true);
                                    }
                                }
                            }
                        }
                    });

                    GeneralLayout.searcher = new mapfish.Searcher.Map({
                        mode: mapfish.Searcher.Map.CLICK,
                        protocol: protocol,
                        displayDefaultPopup: false
                    });

                    GeneralLayout.composermap.addControl(GeneralLayout.searcher);

                    //Builds layers
                    GeneralLayout.composerlayers = [];
                    var layer;
                    var strLyrs='';

                    //Builds layer tree elements
            <logic:iterate id="order" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="userLayerOrder">
                <bean:define id='geomcoll' name='<%=ObjectKeys.USER_MAP_BEAN%>' property='<%="userLayer(" + order + ")"%>' type='org.geogurus.data.DataAccess'/>
                        myLayers.push('<bean:write name="geomcoll" property="name"/>');
                        strLyrs += '<bean:write name="geomcoll" property="name"/> ';
                        initialOrder.push('<bean:write name="geomcoll" property="ID"/>');
                        children.push(
                        {
                            id: "<bean:write name="geomcoll" property="ID"/>",
                            text: "<bean:write name="geomcoll" property="name"/>",
                            leaf: true,
                            layerName: 'mapserverLayer:<bean:write name="geomcoll" property="name"/>',
                            extent:new OpenLayers.Bounds(<bean:write name="geomcoll" property="extent"/>),
                            checked: true,
                            icon: 'images/layers.png'
                        }
                    );
            </logic:iterate>
                    //Must reverse children order to reflect Mapserver's layer order
                    initialOrder.reverse();

                    // layer to digitalize
                    var drawingLayer = new OpenLayers.Layer.Vector("Draw",{isBaseLayer:true});
                    var format = 'image/<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.imageTypeAsString"/>';
                    layer = new OpenLayers.Layer.MapServer('mapserverLayer',
                    '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>',
                    {layers: myLayers,format: format},
                    {transitionEffect:'resize',singleTile:true,isBaseLayer:false});

                    GeneralLayout.composerMsUrl = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>';
                    var locSces = '<bean:write name="<%=ObjectKeys.LOCALIZATION_SERVICES%>"/>';
                    var layerSelection;
                    var layersLoc = [];
                    if(locSces.length > 0) {
                        var aryLocSces = locSces.split('|');
                        for (var ls = 0; ls < aryLocSces.length; ls++) {
                            var curLs = aryLocSces[ls];
                            if(curLs == 'google') {
                                locChildren.push({
                                    id: "locScesGoogle",
                                    text: "Google Maps",
                                    leaf: true,
                                    layerName: 'google',
                                    checked: true,
                                    icon: 'images/layers.png'
                                });
                                layersLoc.push(new OpenLayers.Layer.Google(
                                    "google",
                                    {'sphericalMercator':true}
                                ));
                            }
                            if(curLs == 'yahoo') {
                                locChildren.push({
                                    id: "locScesYahoo",
                                    text: "Yahoo Maps",
                                    leaf: true,
                                    layerName: 'yahoo',
                                    checked: true,
                                    icon: 'images/layers.png'
                                });
                                layersLoc.push(new OpenLayers.Layer.Yahoo(
                                    "yahoo",
                                    {'sphericalMercator':true}
                                ));
                            }
                        }
                        layerSelection = [{
                            id:'gasuserLayerSelection',
                            text:'Layers',
                            expanded:true,
                            children:children
                        },{
                            id:'gasuserLocSelection',
                            text:'Localization',
                            expanded:true,
                            children:locChildren
                        }];
                    } else {
                        layerSelection = children;
                    }
                    var model = [
                        {
                            text: "Layer tree",
                            expanded: true,
                            checked: true,
                            children: layerSelection
                        }
                    ];
                    GeneralLayout.composerlayers[GeneralLayout.composerlayers.length] = layer;

                    GeneralLayout.composermap.addLayers(GeneralLayout.composerlayers);
                    if(layersLoc.length > 0)
                        GeneralLayout.composermap.addLayers(layersLoc);
                    
                    GeneralLayout.composermap.addLayer(drawingLayer);

                    //MAPFISH TREE COMPONENT
                    if(GeneralLayout.layertree) Ext.getCmp('layerTree').destroy();

                    var actionRefresh = new Ext.Action({
                        text: i18n.refresh,
                        handler: function(){
                            refreshMap();
                        },
                        iconCls: 'brefresh'
                    });

                    var actionLegend = new Ext.Action({
                        text: i18n.legend,
                        handler: function(){
                            showLegend();
                        },
                        iconCls: 'blegend'
                    });

                    var actionNext = new Ext.Action({
                        text: i18n.next,
                        handler: GeneralLayout.gotoPublisher,
                        iconCls: 'bnext'
                    });


                    GeneralLayout.layertree = new mapfish.widgets.LayerTree({
                        id:'layerTree',
                        map: GeneralLayout.composermap,
                        model: model,
                        enableDD:true,
                        width: '100%',
                        height: '100%',
                        border: false,
                        buttonAlign: 'center',
                        autoScroll: true,
                        tbar: [actionRefresh,actionLegend],
                        buttons: [actionNext]
                    });

                    //PROPERTIES TREES
                    if(Ext.getCmp('generalPropsTree')) Ext.getCmp('generalPropsTree').destroy();

                    var generalPropsTree = new Ext.tree.TreePanel({
                        id:'generalPropsTree',
                        animate: false,
                        containerScroll: true,
                        rootVisible: false,
                        border: false
                    });

                    Ext.getCmp('generalprops').add(generalPropsTree);

                    // set the root node
                    var generalPropsRoot = new Ext.tree.TreeNode({text:'rootGenProps',id:'rootGenProps'});
                    generalPropsTree.setRootNode(generalPropsRoot);

                    var generalProperties = [
                        {"text":"<bean:message key="map"/>","id":"mapProps","icon":"images/mapserver.gif","leaf":true},
                        {"text":"<bean:message key="legend"/>","id":"legendProps","icon":"images/palette.gif","leaf":true},
                        {"text":"<bean:message key="scalebar"/>","id":"scalebarProps","icon":"styles/static.gif","leaf":true},
                        {"text":"<bean:message key="map_file"/>","id":"mapfileProps","icon":"styles/prop.gif","leaf":true},
                        {"text":"Cartoweb","id":"CWProps","icon":"styles/application.png","leaf":false,"nodes":[
                                {"text":"<bean:message key="cartoweb.locationIni"/>","id":"CWLocationProps","icon":"styles/application_home.png","leaf":true},
                                {"text":"<bean:message key="cartoweb.imagesIni"/>","id":"CWImagesProps","icon":"styles/application_view_gallery.png","leaf":true},
                                {"text":"<bean:message key="cartoweb.queryIni"/>","id":"CWQueryProps","icon":"styles/application_lightning.png","leaf":true},
                                {"text":"<bean:message key="cartoweb.layerIni"/>","id":"CWLayerProps","icon":"styles/application_side_tree.png","leaf":true},
                                {"text":"<bean:message key="cartoweb.loadIniFile"/>","id":"CWLoadIniFiles","icon":"styles/application_get.png","leaf":true}
                            ]}
                        /*
                {"text":"reference","id":"referenceProps","icon":"styles/map.png"},
                {"text":"web","id":"webProps","icon":"styles/world.png"},
                {"text":"querymap","id":"querymapProps","icon":"styles/information.png"},
                         */
                    ];

                    // adds nodes
                    var genPropsNode, genPropsSubNode;
                    for(var n=0;n<generalProperties.length;n++) {
                        genPropsNode = new Ext.tree.TreeNode({
                            text:generalProperties[n].text,
                            id:generalProperties[n].id,
                            icon:generalProperties[n].icon,
                            leaf:generalProperties[n].leaf,
                            draggable:false
                        });

                        if(genPropsNode.leaf) {
                            genPropsNode.on("click",function(){showWindow(this.id);});
                        } else {
                            for(var sn=0;sn<generalProperties[n].nodes.length;sn++) {
                                genPropsSubNode = new Ext.tree.TreeNode({
                                    text:generalProperties[n].nodes[sn].text,
                                    id:generalProperties[n].nodes[sn].id,
                                    icon:generalProperties[n].nodes[sn].icon,
                                    leaf:generalProperties[n].nodes[sn].leaf,
                                    draggable:false
                                });
                                if(genPropsSubNode.leaf) {
                                    genPropsSubNode.on("click", function(){showWindow(this.id);});
                                }
                                genPropsNode.appendChild(genPropsSubNode);
                            }
                        }

                        generalPropsRoot.appendChild(genPropsNode);
                    }


                    //LAYOUT AND MAP RENDERING
                    customizeConfigurationMap(GeneralLayout.composermap);
                    GeneralLayout.composermap.zoomToExtent(GeneralLayout.composermap.baseLayer.maxExtent);

                    Ext.getCmp('pnlComposerCtrl').add(GeneralLayout.layertree);
                    if(Ext.getCmp('pnlComposerPrint')) Ext.getCmp('pnlComposerPrint').destroy();
                    Ext.getCmp('pnlComposerCtrl').add({
                        id: 'pnlComposerPrint',
                        xtype: 'print-simple',
                        title: "Print",
                        bodyStyle: 'padding: 7px;',
                        collapsible:true,
                        collapsed:true,
                        plain:true,
                        formConfig: {
                            labelWidth: 85,
                            defaults: {
                                width: 140
                            },
                            items: [
                                {
                                    xtype: 'textfield',
                                    fieldLabel: "Title",
                                    name: 'mapTitle',
                                    value: ''
                                },{
                                    xtype: 'textarea',
                                    fieldLabel: "Comments",
                                    name: 'comment',
                                    height: 100,
                                    value: ''
                                },{
                                    xtype: 'hidden',
                                    name: 'config',
                                    value: ''
                                }
                            ]
                        },
                        border: false,
                        configUrl:GeneralLayout.printurl,
                        map: GeneralLayout.composermap,
                        tbar:[
                            {
                                text: 'Define Template',
                                handler: function(){
                                    if(GeneralLayout.printWin.win == null) GeneralLayout.printWin.create();
                                    PrintTemplateMgr.printWin = GeneralLayout.printWin;
                                    GeneralLayout.printWin.show(this);
                                    PrintTemplateMgr.jsonOutput = Ext.getCmp('pnlComposerPrint').find('name','config')[0];
                                }
                            }
                        ]
                    });

                    /******************************************************************************
                     ******************************************************************************/
                    Ext.getCmp('generalprops').doLayout();
                    Ext.getCmp('pnlComposerCtrl').doLayout();

                    GeneralLayout.layertree.render();

                    //Add onClick event on each node of the tree
                    var rootNode = GeneralLayout.layertree.getRootNode();
                    //if there is a location layer, then childnodes are one level downer
                    var childNodes;
                    if(locSces.length > 0) {
                        childNodes = rootNode.childNodes[0].childNodes[0].childNodes;
                    } else {
                        childNodes = rootNode.childNodes[0].childNodes;
                    }
                    for(var i=0; i<childNodes.length; i++) {
                        childNodes[i].on('click', function() {
                            if(Ext.getCmp('infoTool').disabled) Ext.getCmp('infoTool').enable();
                            if(activeLayer != null) {
                                var exActiveLayer = GeneralLayout.layertree.root.firstChild.findChild('id',activeLayer);
                                if(exActiveLayer != null) {
                                    exActiveLayer.ui.textNode.style.fontWeight = 'normal';
                                }
                            }
                            activeLayer = this.id;
                            this.ui.textNode.style.fontWeight = 'bold';
                            GeneralLayout.composerActiveExtent = this.attributes.extent;
                            Ext.Ajax.request({
                                url:'displayLayerProperties.do',
                                waitMsg:'Loading',
                                params: {layerID:this.id},
                                success: function(){
                                    loadLayerProperties();
                                }
                            });

                        }
                    );
                    }

                    //Empties cpLayerProps
                    if(Ext.getCmp('cpLayerProps').body) {
                        Ext.getCmp('cpLayerProps').body.dom.innerHTML = "";
                    }
                } // end loadMap

                function loadLayerProperties() {
                    Ext.getCmp('layerprops').expand();
                    Ext.getCmp('layerprops').doLayout();
                    Ext.getCmp('cpLayerProps').load({
                        url: "mapConfigurationLayerProperties.jsp",
                        scripts:true
                    });
                }

                function showLegend() {
                    var legHtml = '';
                    var legUrlTrunk = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>' +
                        '?map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>' +
                        '&version=1.1.1' +
                        '&service=WMS' +
                        '&request=GetLegendGraphic' +
                        '&format=image/gif' +
                        '&tc=' + Math.random();
                    for (var l = 0; l < myLayers.length; l++) {
                        legHtml += '<img src="' + legUrlTrunk + '&layer=' + myLayers[l] + '"><br>';
                    }

                    if(Ext.getCmp('legend-win')) Ext.getCmp('legend-win').destroy();

                    var pan = new Ext.Panel({
                        labelAlign: 'top',
                        bodyStyle:'padding:5px',
                        width: 200,
                        id:'legendPanel',
                        layout:'fit',
                        html: legHtml,
                        autoScroll: true,
                        buttons: [{
                                text: i18n.close,
                                handler: function() {
                                    winLegend.destroy();
                                }
                            }]
                    });
                    var winLegend = new Ext.Window({
                        id:'legend-win',
                        layout:'fit',
                        width:220,
                        height:480,
                        closeAction:'hide',
                        modal: true,
                        items: [pan]
                    });
                    Ext.getCmp('legend-win').show();
                }

                function refreshMap() {
                    //Retrieve layer order by parsing layertree
                    var rootNode = GeneralLayout.layertree.getRootNode();
                    var childNodes = rootNode.childNodes[0].childNodes;
                    var currentOrder = [];
                    for(var i=0; i<childNodes.length; i++) {
                        currentOrder.push(childNodes[i].id);
                    }

                    currentOrder.reverse();
                    orderChanged = false;
                    n = 0;
                    while (n<currentOrder.length && !orderChanged) {
                        if(currentOrder[n] != initialOrder[n]) orderChanged = true;
                        n++;
                    }

                    if(orderChanged) {
                        var newOrder = '';
                        for (var o=currentOrder.length; o>0; o--) {
                            if(o < currentOrder.length) newOrder += ",";
                            newOrder += currentOrder[o-1];
                        }
                        initialOrder = currentOrder;
                        Ext.Ajax.request({
                            url:'refreshLayerOrder.do',
                            params: {layerorder:newOrder},
                            success: function(){
                                refreshComposerMap();
                            }
                        });
                    }
                }

                function refreshComposerMap() {
                    GeneralLayout.composermap.getLayersByName('mapserverLayer')[0].mergeNewParams({'timestamp':Math.random()});
                }

                function showWindow(id) {
                    var url, title;
                    var width = 500;
                    var height = 300;
                    if (id == "genLayProps") {
                        url = 'composer/general_properties.jsp';
                        title='General Properties';
                    } else if (id == "clasLayProps") {
                        url = 'composer/classification_properties.jsp';
                        title='Layer Classification Properties';
                    } else if (id == "clasLabProps") {
                        url = 'loadLabelProperties.do';
                        title='Label Properties';
                    } else if (id == "layLayProps") {
                        url = 'composer/layer_properties.jsp';
                        title='Layer Object Properties';
                    } else if (id == "layClasProps") {
                        url = 'composer/classes_properties.jsp';
                        title='Layer Classes Properties';
                    } else if (id == "CWLocationProps") {
                        url = 'composer/cartoweb/location_ini_properties.jsp';
                        title='Cartoweb3 Location.ini Properties';
                        height = 500;
                    } else if (id == "CWImagesProps") {
                        url = 'composer/cartoweb/images_ini_properties.jsp';
                        title='Cartoweb3 Images.ini Properties';
                    } else if (id == "CWQueryProps") {
                        url = 'composer/cartoweb/query_ini_properties.jsp';
                        title='Cartoweb3 Query.ini Properties';
                        height = 500;
                    } else if (id == "CWLayerProps") {
                        url = 'composer/cartoweb/layer_ini_properties.jsp';
                        title='Cartoweb3 Layer.ini Properties';
                        width = 300;
                        height = 550;
                    } else if (id == "CWLoadIniFiles") {
                        url = 'composer/cartoweb/load_ini_files.jsp';
                        title='Cartoweb3 Configuration files';
                    } else if (id == "mapProps")       {
                        url = 'composer/map_properties.jsp';
                        title='Map Object Properties';
                    } else if (id == "legendProps")    {
                        url = 'composer/legend_properties.jsp';
                        title='Legend Object Properties';
                    } else if (id == "referenceProps") {
                        url = 'composer/reference_properties.jsp';
                        title='Reference Object Properties';
                    } else if (id == "scalebarProps")  {
                        url = 'composer/scalebar_properties.jsp';
                        title='Scalebar Object Properties';
                    } else if (id == "webProps")       {
                        url = 'composer/web_properties.jsp';
                        title='Web Object Properties';
                    } else if (id == "querymapProps")  {
                        url = 'composer/web_properties.jsp';
                        title='QueryMap Object Properties';
                    } else if (id == "mapfileProps")   {
                        url = 'zipDownload.do?exporttype=EXPORT_TYPE_HTML&cols=90&rows=20';
                        title='Full Mapfile';
                        width=800;
                        height=400;
                    } else {
                        return;
                    }

                    //var contentProps = new Ext.Panel({
                    contentProps = new Ext.Panel({
                        id: 'contentProps',
                        //layout:'fit',
                        autoScroll: true,
                        border: false
                    });

                    var window = new Ext.Window({
                        id: 'floatingProps',
                        autoScroll: true,
                        title: title,
                        width: width,
                        height: height,
                        layout: 'fit',
                        modal:true,
                        bodyStyle:'padding:5px;align:center;',
                        items:[contentProps],
                        buttonAlign:'center',
                        buttons: [{
                                id:'floatingPropsApplyBtn',
                                text:'Apply',
                                handler: function(){
                                    sub();
                                }
                            },{
                                id:'floatingPropsCloseBtn',
                                text: 'Close',
                                handler: function(){
                                    Ext.getCmp("floatingProps").close();
                                }
                            }]
                    });

                    window.show();

                    contentProps.load({
                        url: url,
                        scripts:true
                    });
                }

                Ext.onReady(function(){
                    loadMap();
                });
</script>
