<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@  page import="org.geogurus.Datasource" %>
<%@  page import="org.geogurus.GeometryClass" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>

<html:html locale="true">
    <head>
        <title>Geogurus Administration Suite</title>
        <link rel="shortcut icon" href="favicon.ico" />
            
        <link rel="stylesheet" type="text/css" href="scripts/refexportfiles/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" href="scripts/refexportfiles/resources/css/xtheme-gray.css" />
        <link rel="stylesheet" type="text/css" href="styles/layouts.css" />

        <script type="text/javascript" src="scripts/refexportfiles/adapter/ext/ext-base.js"></script>
        <script type="text/javascript" src="scripts/refexportfiles/ext-all.js"></script>
        <script type="text/javascript" src="scripts/refexportfiles/mapfish/MapFish.js"></script>          

<!--            
        <link rel="stylesheet" type="text/css" href="scripts/mfbase/ext/resources/css/ext-all.css" />
        <link rel="stylesheet" type="text/css" href="scripts/mfbase/ext/resources/css/xtheme-gray.css" />
        <script type="text/javascript" src="scripts/mfbase/openlayers/lib/OpenLayers.js"></script>
        <script type="text/javascript" src="scripts/mfbase/ext/adapter/ext/ext-base.js"></script>     
        <script type="text/javascript" src="scripts/mfbase/ext/ext-all.js"></script>
        <script type="text/javascript">
            // Because of a bug in Firefox 2 we need to specify the MapFish base path.
            // See https://bugzilla.mozilla.org/show_bug.cgi?id=351282
            var gMfLocation = "scripts/mfbase/mapfish/";
        </script>
        <script type="text/javascript" src="scripts/mfbase/mapfish/MapFish.js"></script>
-->
        <script type="text/javascript" src="scripts/GeneralLayout.js"></script>
        <script type="text/javascript" src="scripts/Host.js"></script>
        <script type="text/javascript">
            <!--
            Ext.BLANK_IMAGE_URL = 'scripts/mfbase/ext/resources/images/default/s.gif';
            GeneralLayout.setLanguage('fr');
            Ext.onReady(GeneralLayout.init);
            
            //-->
        </script>
    </head>
    <body>
        <!--<div id="north" style="align:center;">
            <img src="images/geogurus.png"><img src="images/adminsuite.png">
        </div>-->
        
        <div id="serverconfig">
            <div id="frmServer" style="width:600px;"></div>
            <script type="text/javascript">
                <!--
                var hostList = [];
                var initialHostList = [];
                <logic:iterate id="host" name="listHostDescriptorBean" property="listHostDescriptor" indexId="cntDs">
                var curhost = new Host ({
                    type:  '<bean:write name="host" property="type"/>',
                    name:  '<bean:write name="host" property="name"/>',
                    path:  '<bean:write name="host" property="path"/>',
                    port:  '<bean:write name="host" property="port"/>',
                    uname: '<bean:write name="host" property="uname"/>',
                    upwd:  '<bean:write name="host" property="upwd"/>'
                });
                hostList[hostList.length] = curhost;
                initialHostList[initialHostList.length] = curhost;
                </logic:iterate>
                //-->
            </script>                
        </div>
        <div id="catalog">
            <div id="data_list"></div>
            <div id="data_detail"></div>
        </div>
        
        <div id="composer">
            <div id="composer_props">
                <div id="layer_props"></div>
                <div id="general_props"></div>
            </div>
            <div id="composer_map"></div>
            <div id="composer_ctrl"></div>
        </div>
        <div id="layerprops-win" class="x-hidden">
            <div class="x-window-header">Layer Properties</div>
            <div id="layerprops-tabs">
                <!-- Auto create tab-->
                <div class="x-tab" title="General Properties">
                    <p>General Properties</p>
                </div>
                <!-- Auto create tab-->
                <div class="x-tab" title="Symbology">
                    <p>Symbology</p>
                </div>
            </div>
        </div>
        <img id="composer_legend" src="legend.png" width="150" height="150" style="position:absolute;left:0;top:0;"/>

        <div id="publisher">
            <div id="publisher_components">
                <div id="publisher_component_list"></div>
                <div id="publisher_view_result"></div>
            </div>
            <div id="publisher_download">
                <div id="publisher_download_layers"></div>
                <div id="publisher_download_mapfile"></div>
            </div>
        </div>
        
    </body>
    </html:html>
    