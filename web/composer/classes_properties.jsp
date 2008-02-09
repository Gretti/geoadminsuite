<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page contentType="text/html"%>
<html:html locale="true">
    <head><title>Classification</title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <SCRIPT>
            <!--
            <%
            // look if this page should refresh the mapfile
            String refresh = request.getParameter("refreshmap");
            if (refresh != null) {
                out.println("if (self.opener != null) self.opener.refreshExtent(); else self.close()");
            }
            %>
            //toggles all checkboxes status
            function allCheck() {
                for (var i = 0; i < document.form1.elements.length; i++) {
                    if (document.form1.elements[i].type == "checkbox" && document.form1.elements[i].name != "labels") {
                        document.form1.elements[i].checked = !document.form1.elements[i].checked;
                    }
                }
            }
            function openWindow(url) {
                var w = self.opener.open(url, "msclassprops", "width=450,height=550,scrollbars=yes,resizable=yes,status=yes,toolbar=no,personalbar=no,locationbar=no,menubar=yes");
                w.focus();
            }
            //-->
        </SCRIPT>
    </head>
    <body class='body2' bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <form name="form1" method="post" action="MSClassificationProperties.jsv">
            <table border="0">
                <tr>
                    <th class="th0" align="center" colspan="2"><bean:message key="classif"/></th>
                </tr>
                <tr> 
                    <th align="center" class="th0"><a href="#" onclick="allCheck()"><bean:message key="all"/></a></th>
                    <th align="center" class="th0"><bean:message key="name_lower"/></th>
                </tr>
                <logic:notEmpty name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass.classes">
                    <logic:iterate id="cl" name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass.classes">
                        <tr>
                            <td class="td4tiny"><html:checkbox name="cl" property="ID"/></td>
                            <td class="td4tiny">
                                <a href="#" onclick="openWindow('MC_mapserver_class_properties.jsp?classid=<bean:write name="cl" property="ID"/>')">
                                    <bean:write name="cl" property="name"/>
                                </a>
                            </td>
                        </tr>
                    </logic:iterate>
                </logic:notEmpty>
                <tr> 
                    <td class="td4tiny" align="center" colspan="2"> 
                        <input type="submit" name="save" value="<bean:message key="save"/>" class="tiny">
                        <input type="button" name="Submit3" value="<bean:message key="close"/>" class="tiny" onclick="self.close()">
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html:html>
