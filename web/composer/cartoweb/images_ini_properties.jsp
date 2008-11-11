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
                if (isNaN(el["imagesConf.mapWidth"].value) ||
                    isNaN(el["imagesConf.mapHeight"].value) ||
                    isNaN(el["imagesConf.mapSizesDefault"].value)) {
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
                    GASselectAll(document.CartowebIniConfigurationForm.elements['imagesConf.mapSizesAsString']);
                    Ext.Ajax.request({
                        url:'CWIniProperties.do',
                        params: Ext.Ajax.serializeForm(document.CartowebIniConfigurationForm)
                    });
                }
            }
            
            
            function addNewMapSize() {
                var f = document.CartowebIniConfigurationForm;
                var elSel = f.elements['imagesConf.mapSizesAsString'];
                if (f.mapSizeId.value.length == 0) {
                    Ext.Msg.alert("New MapSize", "id cannot be empty");
                    return;
                }
                if (isNaN(f.mapSizeWidth.value)) {
                    Ext.Msg.alert("New MapSize", "Value must be a number");
                    return;
                }
                if (isNaN(f.mapSizeHeight.value)) {
                    Ext.Msg.alert("New MapSize", "Value must be a number");
                    return;
                }
                var mapSizeAsString = f.mapSizeId.value + " - ";
                mapSizeAsString += f.mapSizeLabel.value + " - ";
                mapSizeAsString += f.mapSizeWidth.value + " - ";
                mapSizeAsString += f.mapSizeHeight.value;
                var elOptNew = document.createElement('option');
                elOptNew.text = mapSizeAsString;
                elOptNew.value = mapSizeAsString;

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
                <th class="th0" colspan="2"><bean:message key="cartoweb.images.configuration"/></th>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2" align='center'>
                    <b><bean:message key="cartoweb.location.scalesList"/></b>
                    <br/>
                    <html:select property="imagesConf.mapSizesAsString" size="6" styleClass="tiny"  multiple="true">
                        <html:options property="imagesConf.mapSizesAsString"/>
                    </html:select>
                    <br/>
                    <input type="button" name="addMapSizeBtn" value="Add" onclick="GASshowDiv('addMapSizeDiv')" class="tiny"/>&nbsp;
                    <input type="button" name="removeMapSizeBtn" value="Remove" onclick="GASremoveElement(document.CartowebIniConfigurationForm.elements['imagesConf.mapSizesAsString'])" class="tiny"/>
                    <div id='addMapSizeDiv' style='visibility:hidden'>
                        id:    <input type="text" name="mapSizeId" size="3" class="tiny"/>
                        label: <input type="text" name="mapSizeLabel" size="6" class="tiny"/>
                        width: <input type="text" name="mapSizeWidth" size="6" class="tiny"/>
                        height: <input type="text" name="mapSizeHeight" size="6" class="tiny"/>
                        <input type="button" name="addNewMapSizeBtn" value="Add" onclick="addNewMapSize()" class="tiny"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mapSizesActive</td>
                <td class="td4tiny">
                    <html:checkbox property="imagesConf.mapSizesActive" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mapWidth</td>
                <td class="td4tiny">
                    <html:text property="imagesConf.mapWidth" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mapHeight</td>
                <td class="td4tiny">
                    <html:text property="imagesConf.mapHeight" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mapSizesDefault</td>
                <td class="td4tiny">
                    <html:text property="imagesConf.mapSizesDefault" size="7" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">collapsibleKeymap</td>
                <td class="td4tiny">
                    <html:checkbox property="imagesConf.collapsibleKeymap" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">noDrawKeymap</td>
                <td class="td4tiny">
                    <html:checkbox property="imagesConf.noDrawKeymap" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">noDrawScalebar</td>
                <td class="td4tiny">
                    <html:checkbox property="imagesConf.noDrawScalebar" styleClass="tiny"/>
                </td>
            </tr>
        </table>
    </html:form>
</body>
</html:html>
    