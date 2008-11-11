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


<script type="text/javascript" charset="utf-8">
<!--
        var generalPropsTree;
        var myLayers = [];
        var children = [];
        var initialOrder = [];

        // the globally-defined properties windows content displaying classes, labels, CW, etc. properties
        // this is an ext Panel with .url properties.
        var contentProps = null;

        function loadMap() {
            //MAPFISH MAP COMPONENT
            var bounds = new OpenLayers.Bounds(<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapExtent"/>);
            var options = {maxResolution: 'auto',maxExtent: bounds};
            <logic:notEmpty name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection">
            var proj = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection.attributes" />';
            if (proj.length > 2) {
                proj = proj.split('=')[1].split("&")[0];
            }
            //options.push({projection: proj});
            options.projection = proj;
            </logic:notEmpty>
            <logic:equal name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.units" value="<%=String.valueOf(Map.METERS)%>">
                options.units = 'm';
            </logic:equal>

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
                    layerName: "mapserverLayer:<bean:write name="geomcoll" property="name"/>",
                    checked: true,
                    icon: 'images/layers.png'
                }
            );
            </logic:iterate>
            //Must reverse children order to reflect Mapserver's layer order
            //initialOrder.reverse();

            // layer to digitalize
            var drawingLayer = new OpenLayers.Layer.Vector("Draw",{isBaseLayer:true});
            var format = 'image/<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.imageTypeAsString"/>';
            layer = new OpenLayers.Layer.MapServer('mapserverLayer',
                     '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>',
                     {layers: myLayers,format: format},
                     {transitionEffect:'resize',singleTile:true,isBaseLayer:false});

            GeneralLayout.composerMsUrl = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>';

            var model = [
                    {
                        text: "Layer tree",
                        expanded: true,
                        checked: true,
                        children: children
                    }
                ];

            GeneralLayout.composerlayers[GeneralLayout.composerlayers.length] = layer;

            GeneralLayout.composermap.addLayer(drawingLayer);
            GeneralLayout.composermap.addLayers(GeneralLayout.composerlayers);

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
                                                            GeneralLayout.printWin.create();
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
            var childNodes = rootNode.childNodes[0].childNodes;
            var currentOrder = [];
            for(var i=0; i<childNodes.length; i++) {
                childNodes[i].on('click', function() {
                        //showLayerPropertiesWin(this.id);

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
            var lnames = "";
            for (var l = 0; l < myLayers.length; l++) {
                lnames += l!=0 ? "+" + myLayers[l] : myLayers[l];
            }
            var html = '<img src=\"<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>' +
                          '?mode=legend&' +
                          'map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>' +
                          '&layers=' + lnames +
                          '&ts=' + new Date().getUTCMilliseconds() +
                          '\"/>'

            if(!Ext.getCmp('legend-win')) {
                var pan = new Ext.Panel({
                    labelAlign: 'top',
                    bodyStyle:'padding:5px',
                    width: 200,
                    id:'legendPanel',
                    layout:'fit',
                    html: html,
                    autoScroll: true,
                    buttons: [{
                        text: 'Close',
                        handler: function() {
                                winLegend.hide();
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
            } else {
               Ext.getCmp('legendPanel').body.dom.innerHTML = html;
            }

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
            if (id == "genLayProps")         {url = 'composer/general_properties.jsp';       title='General Properties';}
            else if (id == "clasLayProps")   {url = 'composer/classification_properties.jsp';title='Layer Classification Properties';}
            else if (id == "clasLabProps")   {url = 'loadLabelProperties.do';                title='Label Properties';}
            else if (id == "layLayProps")    {url = 'composer/layer_properties.jsp';         title='Layer Object Properties';}
            else if (id == "layClasProps")   {url = 'composer/classes_properties.jsp';       title='Layer Classes Properties';}
            else if (id == "CWLocationProps") {
                url = 'composer/cartoweb/location_ini_properties.jsp';
                title='Cartoweb3 Location.ini Properties';
                // changes height to test a taller windows
                height = 500;
            }
            else if (id == "CWImagesProps")  {url = 'composer/cartoweb/images_ini_properties.jsp'; title='Cartoweb3 Images.ini Properties';}
            else if (id == "CWQueryProps")   {
                url = 'composer/cartoweb/query_ini_properties.jsp';
                title='Cartoweb3 Query.ini Properties';
                height = 500;
            }
            else if (id == "CWLayerProps")   {
                url = 'composer/cartoweb/layer_ini_properties.jsp';
                title='Cartoweb3 Layer.ini Properties';
                width = 300;
                height = 550;
            }
            else if (id == "CWLoadIniFiles") {url = 'composer/cartoweb/load_ini_files.jsp';  title='Cartoweb3 Configuration files';}
            else if (id == "mapProps")       {url = 'composer/map_properties.jsp';           title='Map Object Properties';}
            else if (id == "legendProps")    {url = 'composer/legend_properties.jsp';        title='Legend Object Properties';}
            else if (id == "referenceProps") {url = 'composer/reference_properties.jsp';     title='Reference Object Properties';}
            else if (id == "scalebarProps")  {url = 'composer/scalebar_properties.jsp';      title='Scalebar Object Properties';}
            else if (id == "webProps")       {url = 'composer/web_properties.jsp';           title='Web Object Properties';}
            else if (id == "querymapProps")  {url = 'composer/web_properties.jsp';           title='QueryMap Object Properties';}
            else if (id == "mapfileProps")   {url = 'zipDownload.do?exporttype=EXPORT_TYPE_HTML&cols=90&rows=20';width=800;height=400;title='Full Mapfile';}
            else {return;}

            //var contentProps = new Ext.Panel({
            contentProps = new Ext.Panel({
                id: 'contentProps',
                layout:'fit',
                autoScroll: true,
                border: false
            });

            var window = new Ext.Window({
                id: 'floatingProps',
                autoScroll: true,
                title: title,
                width: width,
                height: height,
                minWidth: 300,
                minHeight: 200,
                layout: 'fit',
                plain:true,
                modal:true,
                shadow: true,
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
                            Ext.getCmp("floatingProps").hide();
                            Ext.getCmp("floatingProps").destroy();
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
        
        //-->
</script>
