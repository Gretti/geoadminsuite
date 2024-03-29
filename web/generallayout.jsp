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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@  page contentType="text/html" pageEncoding="iso-8859-1"%>
<%@  page import="org.geogurus.data.Datasource" %>
<%@  page import="org.geogurus.data.DataAccess" %>
<%@  page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@  page import="org.geogurus.tools.DataManager" %>

<html:html locale="true">
    <head>
        <meta http-equiv="Content-Type" content="text/html;charset=iso-8859-1">
        <title><bean:message key="main_title" /></title>
        <link rel="shortcut icon" href="favicon.ico">

        <!-- Application css -->
        <link rel="stylesheet" type="text/css" href="scripts/colorpicker/assets/skins/sam/colorpicker-skin.css">
        <link rel="stylesheet" type="text/css" href="styles/layouts.css">
        <link rel="stylesheet" type="text/css" href="scripts/printTemplate/PrintTemplate.css">
        <link rel="stylesheet" type="text/css" href="scripts/printTemplate/ext.ux.ColorField.css" />

        <logic:notEqual parameter="debug" value="true">
            <!-- Ext css -->
            <link rel="stylesheet" type="text/css" href="scripts/mapfish/mfbase/ext/resources/css/ext-all.css">
            <link rel="stylesheet" type="text/css" href="scripts/mapfish/mfbase/ext/resources/css/xtheme-gray.css">
            <!-- debug mode
            For this mode to work as expected the whole mapfish tree must be in the WAR file. For
                go the project properties and remove scripts/mapfish/** from the Exlude From WAR File
                list.
            -->
            <script type="text/javascript">
                // Because of a bug in Firefox 2 we need to specify the MapFish base path.
                // See https://bugzilla.mozilla.org/show_bug.cgi?id=351282
                var gMfLocation = "scripts/mapfish/mfbase/mapfish/";
            </script>
            <script type="text/javascript" src="scripts/mapfish/mfbase/ext/adapter/ext/ext-base.js"></script>
            <script type="text/javascript" src="scripts/mapfish/mfbase/ext/ext-all.js"></script>
            <!--<script type="text/javascript" src="scripts/mapfish/mfbase/openlayers/lib/Firebug/firebug.js"></script>
            <script type='text/javascript' src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>-->
            <script type="text/javascript" src="scripts/mapfish/mfbase/openlayers/lib/OpenLayers.js"></script>
            <script type="text/javascript" src="scripts/mapfish/mfbase/mapfish/MapFish.js"></script>
            <script type="text/javascript" src="scripts/mapfish/mfbase/mapfish/lang/fr.js"></script>
        </logic:notEqual>
        <logic:equal parameter="debug" value="true">
            <!-- Ext css -->
            <link rel="stylesheet" type="text/css" href="scripts/refexportfiles/resources/css/ext-all.css">
            <link rel="stylesheet" type="text/css" href="scripts/refexportfiles/resources/css/xtheme-gray.css">
            <script type="text/javascript" src="scripts/refexportfiles/adapter/ext/ext-base.js"></script>
            <script type="text/javascript" src="scripts/refexportfiles/ext-all.js"></script>
            <!-- production mode -->
            <script type="text/javascript" src="scripts/refexportfiles/mapfish/MapFish.js"></script>
        </logic:equal>
        <script type="text/javascript" src="scripts/refexportfiles/mapfish_patches.js"></script>

        <!-- Application scripts -->
        <script type="text/javascript" src="scripts/Utils.js"></script>
        <script type="text/javascript" src="scripts/GeneralLayout.js"></script>
        <script type="text/javascript" src="scripts/Host.js"></script>
        <script type="text/javascript" src="scripts/printTemplate/ext.ux.ColorField.js"></script>
        <script type="text/javascript" src="scripts/printTemplate/PrintTemplateMgr.js"></script>
        <script type="text/javascript" src="scripts/printTemplate/PrintTemplate.js"></script>

        <!-- YUI Dependencies -->
        <script type="text/javascript" src="scripts/colorpicker/utilities/utilities.js" ></script>
        <script type="text/javascript" src="scripts/colorpicker/slider/slider-min.js" ></script>

        <!-- YUI Color Picker source files for CSS and JavaScript -->
        <script type="text/javascript" src="scripts/colorpicker/colorpicker-min.js" ></script>
        <!-- Webmapping services (Google, Yahoo, etc.)-->
        <script src="http://maps.google.com/maps?file=api&amp;v=2&amp;key=<%=DataManager.getProperty("GOOGLE_KEY")%>" type="text/javascript"></script>
        <script src="http://api.maps.yahoo.com/ajaxymap?v=3.8&appid=<%=DataManager.getProperty("YAHOO_KEY")%>"></script>
        <script src='http://dev.virtualearth.net/mapcontrol/mapcontrol.ashx?v=6.1'></script>
        
        <script type="text/javascript">
            Ext.BLANK_IMAGE_URL = 'scripts/refexportfiles/resources/images/default/s.gif';
            var i18n = <bean:write name="I18N" scope="request" filter="false"/>;
            var colorFieldImagePath = 'scripts/printTemplate/images/';
            Ext.onReady(function() {
                GeneralLayout.init();
                //GeneralLayout.printurl='http://localhost:8084/geoadminsuite/pdf/info.json';
                GeneralLayout.printurl='<%=DataManager.getProperty("MAPFISHPRINTURL")%>';
                // extra parameters for publishing windows: mapfile path and publication mapserver URL
                GeneralLayout.publishmapserverurl='<%=DataManager.getPublishMapserverUrl()%>';
                GeneralLayout.publishmapfilepath='<%=DataManager.getProperty("PUBLISH_MAPFILEPATH")%>';
            });

            // event handler for language select. Refreshes this
            // page with language parameter.
            function changeLanguage(lang) {
                document.location = "switchLang.do?lang=" + lang + "&cty=" + lang.toUpperCase();
            }

            // calls the ResetSessionAction and display result in an ext window
            function resetUserSession() {
                Ext.Ajax.request({
                    url:'resetSession.do',
                    success: function(response){
                        Ext.Msg.alert('Message', Ext.util.JSON.decode(response.responseText).message);
                    },
                    failure: function(response){
                        Ext.Msg.alert('Error', Ext.util.JSON.decode(response.responseText).message);
                    }
                });

            }
        </script>

        <!-- this script contains generic gas functions -->
        <script type="text/javascript" src="scripts/Utils.js"></script>
        <!-- this script contains user defined functions to override the default behavior
        of the openlayers maps used in the composer and publisher windows -->
        <script type="text/javascript" src="scripts/General_user.js"></script>
    </head>
    <body>
        <!--<div id="north" style="align:center;">
            <img src="images/geogurus.png"><img src="images/adminsuite.png">
        </div>-->

        <div id="serverconfig">
            <div id="confApp" style="width:600px;"></div>
            <div id="frmServer" style="width:600px;"></div>
            <script type="text/javascript">
                var hostList = [];
                var initialHostList = [];
                    <logic:iterate id="host" name="listHostDescriptorBean" property="listHostDescriptor" indexId="cntDs">
                        var curhost = new Host ({
                            type:      '<bean:write name="host" property="type"/>',
                            recurse:   '<bean:write name="host" property="recurse"/>',
                            name:      '<bean:write name="host" property="name"/>',
                            path:      '<bean:write name="host" property="path"/>',
                            port:      '<bean:write name="host" property="port"/>',
                            uname:     '<bean:write name="host" property="uname"/>',
                            upwd:      '<bean:write name="host" property="upwd"/>',
                            instance:  '<bean:write name="host" property="instance"/>'
                        });
                        hostList[hostList.length] = curhost;
                        initialHostList[initialHostList.length] = curhost;
                    </logic:iterate>
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
        <div id="param-export-win"></div>

    </body>
</html:html>
