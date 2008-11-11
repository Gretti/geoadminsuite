<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>

<html:html >
    <head><title><bean:message key="ms_layer_title"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <SCRIPT>
            <!--
            // checks form fields to avoid passing bad values to the servlet
            function checkForm() {
                var el = document.CartowebIniConfigurationForm.elements;
                if (isNaN(el["queryConf.weightQueryByPoint"].value) ||
                    isNaN(el["queryConf.weightQueryByBbox"].value) ||
                    isNaN(el["queryConf.weightQueryByCircle"].value) ||
                    isNaN(el["queryConf.weightQueryByPolygon"].value)) {
                    alert("<bean:message key="cartoweb.location.numericValueExpected"/>");
                    return false;
                }
                return true;
            }

            // submit the form by calling a struts action.
            // no validation will be perfomed on the server side, as Cartoweb INI logic
            // is not know by the GAS.
            // for future use, a message can be returned by the server into a JSON structure
            // and displayed somewhere in this page.
            function sub() {

                var ok = checkForm();

                if (ok) {
                    GASselectAll(document.CartowebIniConfigurationForm.elements['queryConf.mapSizesAsString']);
                    Ext.Ajax.request({
                        url:'CWIniProperties.do',
                        params: Ext.Ajax.serializeForm(document.CartowebIniConfigurationForm)
                    });
                }
            }
            
                    //-->
        </SCRIPT>
</head>
<body class='body2' leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <html:form action="CWIniProperties.do" method="POST">
        <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
            <tr align="center">
                <th class="th0" colspan="2"><bean:message key="cartoweb.query.configuration"/></th>
            </tr>
            <tr>
                <td class="td4tiny">persistentQueries</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.persistentQueries" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">displayExtendedSelection</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.displayExtendedSelection" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">queryLayers</td>
                <td class="td4tiny">
                    <html:text property="queryConf.queryLayers" size="60" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">returnAttributesActive</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.returnAttributesActive" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">defaultPolicy</td>
                <td class="td4tiny">
                    <html:radio property="queryConf.defaultPolicy" value="0" styleClass="tiny">POLICY_XOR</html:radio><br/>
                    <html:radio property="queryConf.defaultPolicy" value="1" styleClass="tiny">POLICY_UNION</html:radio><br/>
                    <html:radio property="queryConf.defaultPolicy" value="2" styleClass="tiny">POLICY_REPLACE</html:radio><br/>
                    <html:radio property="queryConf.defaultPolicy" value="3" styleClass="tiny">POLICY_INTERSECTION</html:radio>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">defaultMaskmode</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.defaultMaskmode" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">defaultHilight</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.defaultHilight" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">defaultAttributes</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.defaultAttributes" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">defaultTable</td>
                <td class="td4tiny">
                    <html:checkbox property="queryConf.defaultTable" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">weightQueryByPoint</td>
                <td class="td4tiny">
                    <html:text property="queryConf.weightQueryByPoint" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">weightQueryByBbox</td>
                <td class="td4tiny">
                    <html:text property="queryConf.weightQueryByBbox" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">weightQueryByPolygon</td>
                <td class="td4tiny">
                    <html:text property="queryConf.weightQueryByPolygon" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">weightQueryByCircle</td>
                <td class="td4tiny">
                    <html:text property="queryConf.weightQueryByCircle" size="7" styleClass="tiny"/>
                </td>
            </tr>
        </table>
    </html:form>
</body>
</html:html>
    