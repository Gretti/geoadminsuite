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

<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <link rel="shortcut icon" href="favicon.ico">
        <title>GAS Quick Administration Page</title>
    </head>
    <body>
        <p><a href="index.jsp">Home</a></p>
        <html:form action="checkConfiguration">
            <table class='tableb' cellspacing='0' cellpadding='0' align=center>
                <tr><th colspan="2" align=center>Parameters</th></tr>
                <tr><td><bean:message key="mapserver_url"/>&nbsp;:</td><td><html:text property="mapserverURL" size="50" /></td></tr>
                <tr><td><bean:message key="index.shp2pgsql"/>&nbsp;:</td><td><html:text property="shp2pgsql" size="50"/></td></tr>
                <tr><td><bean:message key="index.pgsql2shp"/>&nbsp;:</td><td><html:text property="pgsql2shp" size="50"/></td></tr>
                <tr><td><bean:message key="index.dbparams"/>&nbsp;:</td><td><html:text property="gasDbReproj" size="50"/></td></tr>
                <tr><td colspan="2" align="center"><html:submit><bean:message key='index.checkConfiguration'/></html:submit></td></tr>
                <tr><th colspan="2" align="center">Result</th></tr>
                <tr>
                    <td align=center colspan="2">
                        <span class="tinyred"><html:errors /></span>
                        <logic:present scope="request" name="checkConfig">
                            <span class='tinygreen'><bean:message key='index.configurationOk'/></span><br/>
                        </logic:present>
                    </td>
                </tr>
            </table>
        </html:form>
    </body>
</html>
