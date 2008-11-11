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
        <SCRIPT TYPE="JAVASCRIP" SRC="scripts/Utils.js"/>
        <SCRIPT>
            <!--
            // checks form fields to avoid passing bad values to the servlet
            function checkForm() {
                var el = document.CartowebIniConfigurationForm.elements;
                if (isNaN(el["locationConf.minScale"].value) ||
                    isNaN(el["locationConf.maxScale"].value) ||
                    isNaN(el["locationConf.zoomFactor"].value) ||
                    isNaN(el["locationConf.recenterMargin"].value) ||
                    isNaN(el["locationConf.recenterDefaultScale"].value) ||
                    isNaN(el["locationConf.refMarksSymbolSize"].value) ||
                    isNaN(el["locationConf.refMarksSize"].value) ||
                    isNaN(el["locationConf.refMarksTransparency"].value) ||
                    isNaN(el["locationConf.refLinesSize"].value) ||
                    isNaN(el["locationConf.refLinesFontSize"].value)) {
                    alert("<bean:message key="cartoweb.location.numericValueExpected"/>");
                    return false;
                }
                if (el["locationConf.refMarksColor"].value.length > 0) {
                    rgb = el["locationConf.refMarksColor"].value.split(' ');
                    if (rgb.length != 3) {
                        alert("<bean:message key="cartoweb.location.invalidColorFormat"/>");
                        return false;
                    }
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
                    GASselectAll(document.CartowebIniConfigurationForm.elements['locationConf.scalesAsString']);
                    GASselectAll(document.CartowebIniConfigurationForm.elements['locationConf.shortcutsAsString']);
                    Ext.Ajax.request({
                        url:'CWIniProperties.do',
                        params: Ext.Ajax.serializeForm(document.CartowebIniConfigurationForm)
                        /*
                        callback: function (response, options) {
                                if (Ext.util.JSON.decode(response.responseText).success == false) {
                                    Ext.Msg.alert('ERROR', Ext.util.JSON.decode(response.responseText).message);
                                } else {
                                    //Ext.Msg.alert('Server response', 
                                    //Ext.util.JSON.decode(response.responseText).message);
                                }
                        }*/
                    });
                }
            }
            
            
            function addNewScale() {
                var f = document.CartowebIniConfigurationForm;
                var elSel = f.elements['locationConf.scalesAsString'];
                if (f.scaleId.value.length == 0) {
                    Ext.Msg.alert("New Scale", "id cannot be empty");
                    return;
                }
                if (isNaN(f.scaleValue.value)) {
                    Ext.Msg.alert("New Scale", "Value must be a number");
                    return;
                }
                var scaleAsString = f.scaleId.value + " - ";
                scaleAsString += f.scaleLabel.value + " - ";
                scaleAsString += f.scaleValue.value + " - ";
                scaleAsString += f.scaleVisible.checked ? "true" : "false";
                var elOptNew = document.createElement('option');
                elOptNew.text = scaleAsString;
                elOptNew.value = scaleAsString;

                try {
                    elSel.add(elOptNew, null); // standards compliant; doesn't work in IE
                } catch(ex) {
                    elSel.add(elOptNew); // IE only
                }
                // sets the selected index to this added entry, to force list scroll
                elSel.selectedIndex = elSel.options.length-1;
            }
            function addNewShortcut() {
                var f = document.CartowebIniConfigurationForm;
                var elSel = f.elements['locationConf.shortcutsAsString'];
                if (f.shortcutId.value.length == 0) {
                    Ext.Msg.alert("New Shortcut", "id cannot be empty");
                    return;
                }
                if (f.shortcutLabel.value.length == 0) {
                    Ext.Msg.alert("New Shortcut", "Label cannot be empty");
                    return;
                }
                if (isNaN(f.xmin.value) ||
                   isNaN(f.xmax.value) ||
                   isNaN(f.ymin.value) ||
                   isNaN(f.ymax.value)) {
                    Ext.Msg.alert("New Shorcut", "Bbox must be a number");
                    return;
                }
                var shortcutAsString = f.shortcutId.value + " - ";
                shortcutAsString += f.shortcutLabel.value + " - ";
                shortcutAsString += f.xmin.value + ",";
                shortcutAsString += f.xmax.value + ",";
                shortcutAsString += f.ymin.value + ",";
                shortcutAsString += f.ymax.value;
                var elOptNew = document.createElement('option');
                elOptNew.text = shortcutAsString;
                elOptNew.value = shortcutAsString;

                try {
                    elSel.add(elOptNew, null); // standards compliant; doesn't work in IE
                } catch(ex) {
                    elSel.add(elOptNew); // IE only
                }
                // sets the selected index to this added entry, to force list scroll
                elSel.selectedIndex = elSel.options.length-1;
            }
            
                    //-->
        </SCRIPT>
</head>
<body class='body2' leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <html:form action="CWIniProperties.do" method="POST">
        <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
            <tr align="center">
                <th class="th0" colspan="2"><bean:message key="cartoweb.location.configuration"/></th>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2" align='center'>
                    <b><bean:message key="cartoweb.location.scalesList"/></b>
                    <br/>
                    <html:select property="locationConf.scalesAsString" size="6" styleClass="tiny"  multiple="true">
                        <html:options property="locationConf.scalesAsString"/>
                    </html:select>
                    <br/>
                    <input type="button" name="addScaleBtn" value="Add" onclick="GASshowDiv('addScaleDiv')" class="tiny"/>&nbsp;
                    <input type="button" name="removeScaleBtn" value="Remove" onclick="GASremoveElement(document.CartowebIniConfigurationForm.elements['locationConf.scalesAsString'])" class="tiny"/>
                    <div id='addScaleDiv' style='visibility:hidden'>
                        id:    <input type="text" name="scaleId" size="3" class="tiny"/>
                        label: <input type="text" name="scaleLabel" size="6" class="tiny"/>
                        value: <input type="text" name="scaleValue" size="6" class="tiny"/>
                        visible: <input type="checkBox" name="scaleVisible" checked class="tiny"/>
                        <input type="button" name="addNewScaleBtn" value="Add" onclick="addNewScale()" class="tiny"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2" align='center'>
                    <b><bean:message key="cartoweb.location.shortcutsList"/></b>
                    <br/>
                    <html:select property="locationConf.shortcutsAsString" size="6" styleClass="tiny"  multiple="true">
                        <html:options property="locationConf.shortcutsAsString"/>
                    </html:select>
                    <br/>
                    <input type="button" name="addShortcutBtn" value="Add" onclick="showDiv('addShortcutDiv')" class="tiny"/>&nbsp;
                    <input type="button" name="removeShortcutBtn" value="Remove" onclick="GASremoveElement(document.CartowebIniConfigurationForm.elements['locationConf.shortcutsAsString'])" class="tiny"/>
                    <div id='addShortcutDiv' style='visibility:hidden'>
                        id:    <input type="text" name="shortcutId" size="3" class="tiny"/>
                        label: <input type="text" name="shortcutLabel" size="6" class="tiny"/>
                        bbox: <input type="text" name="xmin" size="6" class="tiny"/>&nbsp;
                              <input type="text" name="ymin" size="6" class="tiny"/>&nbsp;
                              <input type="text" name="xmax" size="6" class="tiny"/>&nbsp;
                              <input type="text" name="ymax" size="6" class="tiny"/>&nbsp;
                        <input type="button" name="addNewShortcutBtn" value="Add" onclick="addNewShortcut()" class="tiny"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">minScale</td>
                <td class="td4tiny">
                    <html:text property="locationConf.minScale" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">maxScale</td>
                <td class="td4tiny">
                    <html:text property="locationConf.maxScale" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">scaleModeDiscrete</td>
                <td class="td4tiny">
                    <html:checkbox property="locationConf.scaleModeDiscrete" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">zoomFactor</td>
                <td class="td4tiny">
                    <html:text property="locationConf.zoomFactor" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">noBboxAdjusting</td>
                <td class="td4tiny">
                    <html:checkbox property="locationConf.noBboxAdjusting" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">recenterMargin</td>
                <td class="td4tiny">
                    <html:text property="locationConf.recenterMargin" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">recenterDefaultScale</td>
                <td class="td4tiny">
                    <html:text property="locationConf.recenterDefaultScale" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksSymbol</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksSymbol" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksSymbolSize</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksSymbolSize" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksSize</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksSize" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksColor</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksColor"  styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksTransparency</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksTransparency" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refMarksOrigin</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refMarksOrigin" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refLinesActive</td>
                <td class="td4tiny">
                    <html:checkbox property="locationConf.refLinesActive" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refLinesSize</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refLinesSize" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">refLinesFontSize</td>
                <td class="td4tiny">
                    <html:text property="locationConf.refLinesFontSize" size="7" styleClass="tiny"/>
                </td>
            </tr>
        </table>
    </html:form>
</body>
</html:html>
    