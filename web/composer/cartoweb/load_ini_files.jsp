<%/*Copyright (C) 2007-2008  Camptocamp

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
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>

<html:html >
    <head>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <SCRIPT>
            function checkForm() {
                var el = document.CartowebIniFileForm.elements;
                if (el['iniFile'].value.length == 0) {
                    Ext.Msg.alert('<bean:message key="cartoweb.upload.checkFields"/>', '<bean:message key="cartoweb.upload.chooseLocalFile"/>');
                        return false;
                }
                return true;
            }
            
            function sub() {
                var ok = checkForm();
                if (ok) {
                    Ext.Ajax.request({ 
                         form: document.CartowebIniFileForm, 
                         success: function(response, options) { 
                             Ext.Msg.alert('Message', Ext.util.JSON.decode(response.responseText).message);
                         }, 
                         failure: function(response, options) { 
                             alert('File could not be uploaded'); 
                         }
                    });
                }
            }
            
                    //-->
        </SCRIPT>
</head>
<body class='body2' leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <html:form action="CWLoadIniFile.do" method="POST" enctype="multipart/form-data">
        <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
            <tr align="center">
                <th class="th0" colspan="2"><bean:message key="cartoweb.loadIniFiles"/></th>
            </tr>
            <tr>
                <td class="td4tiny"><bean:message key="cartoweb.upload.fileType"/></td>
                <td class="td4tiny">
                    <html:select property="fileType" styleClass="tiny">
                        <html:option value="LOCATION_INI">Location.ini</html:option>
                        <html:option value="LAYERS_INI">Layers.ini</html:option>
                        <html:option value="QUERY_INI">query.ini</html:option>
                        <html:option value="IMAGES_INI">Images.ini</html:option>
                    </html:select>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">
                    <bean:message key="cartoweb.chooseFile"/>
                </td>
                <td class="td4tiny">
                    <html:file  styleClass="tiny" property="iniFile"/>
                </td>
            </tr>
        </table>
    </html:form>
</body>
</html:html>
    