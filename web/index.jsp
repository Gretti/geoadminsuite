<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@  page contentType="text/html"%>
<%@  page import="org.apache.struts.Globals"%>

<html:html locale="true">
    <head>
        <title><bean:message key="main_title" /></title>
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
        <META HTTP-EQUIV="Pragma" CONTENT="No-cache">
        <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
        <META HTTP-EQUIV="Expires" CONTENT=0>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <link rel="shortcut icon" href="favicon.ico">
        <SCRIPT>
            <!--
            // event handler for language select. Refreshes this 
            // page with language parameter.
            function changeLanguage(lang) {
                document.location = "switchLang.do?lang=" + lang + "&cty=" + lang.toUpperCase();
            }
            //-->
        </SCRIPT>
    </head>
    <body class='body2'>
        <table class='tablea' width='75%' border="0"  cellspacing='0' cellpadding='5' align=center>
            <tr align="center" style="background: url(images/bgindex.gif) repeat-x left top;" bgcolor='#DEE3E7'> 
                <td  width="107" height='47' align="left">
                    <a href="http://geogurus.org"><img src="images/link_geogurus.gif" alt="<bean:message key="official_site" />" title="<bean:message key="official_site"/>"></a>&nbsp;
                    <logic:equal name="<%=Globals.LOCALE_KEY%>" property="language" value="fr">
                        <a target='_blank' href='gas_documentation_fr.html'><img src="images/link_documentation.gif" alt="Documentation" title="Documentation"></a>
                    </logic:equal>
                    <logic:notEqual name="<%=Globals.LOCALE_KEY%>" property="language" value="fr">
                        <a target='_blank' href='gas_documentation.html'><img src="images/link_documentation.gif" alt="Documentation" title="Documentation"></a>
                    </logic:notEqual >
                    <a href="todo.jsp"><img src="images/debug.gif" alt="Roadmap" title="Roadmap"></a>
                </td> 
                <td align="center" nowrap><h2><img src="images/geogurus.png"><img src="images/adminsuite.png"></h2></td>
                <td  width="107" height='47'>
                    <span class='tiny'>
                        <img alt="FR" style="cursor:pointer;" src="images/fr.gif" onclick="javascript:changeLanguage('fr');">
                        <img alt="EN" style="cursor:pointer;" src="images/en.gif" onclick="javascript:changeLanguage('en');">
                    </span>
                </td>
            </tr>
            <TR><TD colspan='3'>&nbsp;</TD></TR>
            <TR><TD colspan='3'><bean:message key="index_td1" /></TD></TR>
            <TR><TD colspan='3'><bean:message key="index_td2" /></TD></TR>
            <!--S�lecteur de serveurs-->
            <TR>
                <TD colspan='3'>
                    <TABLE class='tableb' width='95%' cellspacing='0' cellpadding='10' align=center>
                        <TR>
                            <TD><bean:message key="index_td3" /></TD>
                            <TD align=right>
                                <A href='images/ss_config.gif' target='_blank'>
                                    <IMG src='images/small_ss_config.gif' alt='<bean:message key="click_enlarge" />' border='0'>
                                </A>
                            </TD>
                        </TR>
                    </TABLE>
                </TD>
            </TR>
            <!--Catalogue-->
            <TR>
                <TD colspan='3'>
                    <TABLE class='tableb' width='95%' cellspacing='0' cellpadding='10' align=center>
                        <TR>
                            <TD><bean:message key="index_td4" /></TD>
                            <TD align=right>
                                <A href='images/ss_catalog.gif' target='_blank'>
                                    <IMG src='images/small_ss_catalog.gif' alt='<bean:message key="click_enlarge" />' border='0'>
                                </A>
                            </TD>
                        </TR>
                    </TABLE>
                </TD>
            </TR>
            <!--Zone de carte-->
            <TR>
                <TD colspan='3'>
                    <TABLE class='tableb' width='95%' cellspacing='0' cellpadding='10' align=center>
                        <TR>
                            <TD><bean:message key="index_td5" /></TD>
                            <TD align=right>
                                <A href='images/ss_carte.gif' target='_blank'>
                                    <IMG src='images/small_ss_carte.gif' alt='><bean:message key="click_enlarge" />' border='0'>
                                </A>
                            </TD>
                        </TR>
                    </TABLE>
                </TD>
            </TR>
            <!--Publication-->
            <TR>
                <TD colspan='3'>
                    <TABLE class='tableb' width='95%' cellspacing='0' cellpadding='10' align=center>
                        <TR>
                            <TD><bean:message key="index_td6" /></TD>
                            <TD align=right><A href='images/ss_publish.gif' target='_blank'><IMG src='images/small_ss_publish.gif' alt='<bean:message key="click_enlarge" />' border='0'></A></TD>
                        </TR>
                    </TABLE>
                </TD>
            </TR>
            <BR>
            <TR>
                <TD colspan='3'>
                    <TABLE class='tablec' width='95%' cellspacing='0' cellpadding='0' align=center>
                        <tr><th class='th0' align=center colspan='3'><bean:message key="links" /></th></tr> 
                        <tr><td align=center class='tinygrey'><bean:message key="web_mapping_engine" />&nbsp;:&nbsp;</TD><TD><a target='_blank' class='small' href="http://mapserver.gis.umn.edu">MapServer</a></TD></TR>
                        <tr><td align=center class='tinygrey'><bean:message key="oodb" />&nbsp;:&nbsp;</TD><TD><a target='_blank' class='small' href="http://www.postgresql.org">PostgreSQL</a></TD></TR>
                        <tr><td align=center class='tinygrey'><bean:message key="geographic_database" />&nbsp;:&nbsp;</TD><TD><a target='_blank' class='small' href="http://postgis.refractions.net">PostGIS</a></TD></TR> 
                    </TABLE>
                </TD>
            </TR>
            <TR>
                <TD colspan='3'>
                    <TABLE class='tableb' width='40%' cellspacing='0' cellpadding='0' align=center>
                        <tr><td align=center><a class='big' href="listDatasources.do"><bean:message key="try" /></a></td></tr>
                    </TABLE>
                </TD>
            </TR>
            <tr><td colspan='3' align="right"><DIV class='tinygrey'><bean:message key="geogurus_solution" /></DIV></td></tr>
        </table>
    </body>
</html:html>