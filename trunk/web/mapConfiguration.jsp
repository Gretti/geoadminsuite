<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@  page contentType="text/html" language="java"%>
<%@  page import="org.geogurus.Datasource" %>
<%@  page import="org.geogurus.GeometryClass" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>

<script type="text/javascript" charset="utf-8">
<!--
        var generalPropsTree;
        var myLayers = [];
        var children = [];
        var initialOrder = [];
            
        function loadMap() {
            //MAPFISH MAP COMPONENT
            var bounds = new OpenLayers.Bounds(<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapExtent"/>);
            var proj = '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.projection.attributes"/>';
            proj = proj.split('=')[1].split("&")[0];
            var options = {maxResolution: 'auto',maxExtent: bounds, projection: proj};
            
            GeneralLayout.zoombox = new OpenLayers.Control.ZoomBox();
            GeneralLayout.dragpan = new OpenLayers.Control.DragPan();

            GeneralLayout.composermap = new OpenLayers.Map(Ext.getCmp('pnlComposerMap').body.id, options);
            GeneralLayout.composermap.addControl(new OpenLayers.Control.MousePosition());
            GeneralLayout.composermap.addControl(GeneralLayout.zoombox);
            GeneralLayout.composermap.addControl(GeneralLayout.dragpan);
            //GeneralLayout.composermap.addControl(new OpenLayers.Control.Scale());
            
            //Builds layers
            GeneralLayout.composerlayers = [];
            var layer;
            
console.log('<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="userLayerOrder"/>');
            
            //Builds layer tree elements
            <logic:iterate id="order" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="userLayerOrder">
                <bean:define id='geomcoll' name='<%=ObjectKeys.USER_MAP_BEAN%>' property='<%="userLayer(" + order + ")"%>' type='org.geogurus.GeometryClass'/>
            myLayers.push('<bean:write name="geomcoll" property="name"/>'); 
            initialOrder.push('<bean:write name="geomcoll" property="ID"/>');
console.log('<bean:write name="geomcoll" property="name"/>');
            children.push(
                {
                    id: "<bean:write name="geomcoll" property="ID"/>",
                    text: "<bean:write name="geomcoll" property="name"/>",
                    leaf: true,
                    layerName: "myGroup:<bean:write name="geomcoll" property="name"/>",
                    checked: true,
                    icon: 'images/layers.png'
                }
            );
            </logic:iterate>
            //Must reverse children order to reflect Mapserver's layer order
            initialOrder.reverse();
            
            // layer to digitalize
            var drawingLayer = new OpenLayers.Layer.Vector("Draw",{isBaseLayer:true});
            
            layer = new OpenLayers.Layer.MapServer('myGroup', 
                     '<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapserverURL"/>?mode=map&map=<bean:write name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfilePath"/>',
                     {layers: myLayers,format: "image/png",'map_scalebar_status':'OFF'}, 
                     {isBaseLayer:false});
            
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
                text: 'Refresh',
                handler: function(){
                    refreshMap();
                },
                iconCls: 'brefresh'
            });
            
            var actionLegend = new Ext.Action({
                text: 'Legend',
                handler: function(){
                    showLegend();
                },
                iconCls: 'blegend'
            });
                                
            var actionNext = new Ext.Action({
                text: 'Next',
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
                {"text":"<bean:message key="map"/>","id":"mapProps","icon":"images/mapserver.gif"},
                {"text":"<bean:message key="legend"/>","id":"legendProps","icon":"images/palette.gif"},
                {"text":"<bean:message key="scalebar"/>","id":"scalebarProps","icon":"styles/static.gif"},
                {"text":"<bean:message key="reference"/>","id":"referenceProps","icon":"styles/map.png"},
                {"text":"<bean:message key="web"/>","id":"webProps","icon":"styles/world.png"},
                {"text":"MapFile","id":"mapfileProps","icon":"styles/prop.gif"}
            ];

            // adds nodes
            var genPropsNode;
            for(var n=0;n<generalProperties.length;n++) {
                genPropsNode = new Ext.tree.TreeNode({
                    text:generalProperties[n].text,
                    id:generalProperties[n].id,
                    icon:generalProperties[n].icon,
                    draggable:false
                });
                genPropsNode.on("click",function(){showWindow(this.id);});
                generalPropsRoot.appendChild(genPropsNode);
            }
            
            
            //LAYOUT AND MAP RENDERING
            GeneralLayout.composermap.zoomToExtent(bounds);
            
            Ext.getCmp('pnlComposerCtrl').add(GeneralLayout.layertree);
            
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
        }
        
        function loadLayerProperties() {
            Ext.getCmp('layerprops').expand();
            Ext.getCmp('layerprops').doLayout();
            Ext.getCmp('cpLayerProps').load({
                url: "mapConfigurationLayerProperties.jsp",
                scripts:true
            });
        }
        /*
        function showLayerPropertiesWin(id_) {
            if(!GeneralLayout.winProperties) {
                GeneralLayout.winProperties = new Ext.Window({
                    el:'layerprops-win',
                    layout:'fit',
                    width:640,
                    height:480,
                    closeAction:'hide',
                    plain: true,
                    items: new Ext.TabPanel({
                        el: 'layerprops-tabs',
                        autoTabs: true,
                        activeTab: 0,
                        deferredRender: false,
                        border: false
                    }),
                    buttons: [{
                        text:'Apply'
                    },{
                        text: 'Close',
                        handler: function(){
                            GeneralLayout.winProperties.hide();
                        }
                    }]
                });
            }
            GeneralLayout.winProperties.show(this);
        }
        */
        function showLegend() {
            Ext.Msg.alert('coming ...', 'Have to find a solution for this');
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
            while(n<currentOrder.length && !orderChanged) {
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
                            GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()}) 
                            //GeneralLayout.composermap.baseLayer.redraw();
                        }
                });
            }
        }
        
        function showWindow(id) {
            var url, title;
            var width = 500;
            var height = 300;
            if (id == "genLayProps")         {url = 'composer/general_properties.jsp';       title='General Properties';}
            else if (id == "clasLayProps")   {url = 'composer/classification_properties.jsp';title='Layer Classification Properties';}
            else if (id == "layLayProps")    {url = 'composer/layer_properties.jsp';         title='Layer Object Properties';}
            else if (id == "layClasProps")   {url = 'composer/classes_properties.jsp';       title='Layer Classes Properties';}
            else if (id == "mapProps")       {url = 'composer/map_properties.jsp';           title='Map Object Properties';}
            else if (id == "legendProps")    {url = 'composer/legend_properties.jsp';        title='Legend Object Properties';}
            else if (id == "referenceProps") {url = 'composer/reference_properties.jsp';     title='Reference Object Properties';}
            else if (id == "scalebarProps")  {url = 'composer/scalebar_properties.jsp';      title='Scalebar Object Properties';}
            else if (id == "webProps")       {url = 'composer/web_properties.jsp';           title='Web Object Properties';}
            else if (id == "querymapProps")  {url = '';}
            else if (id == "mapfileProps")   {url = 'zipDownload.do?exporttype=-3&cols=90&rows=20';width=800;height=400;title='Full Mapfile';}
            else {return;}
        
            var contentProps = new Ext.Panel({
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

        Ext.onReady(loadMap);
        //-->
</script>
