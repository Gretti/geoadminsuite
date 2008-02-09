<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.GeometryClass" %>
<%@page import="org.geogurus.mapserver.objects.MapClass" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>

<link rel="stylesheet" href="css/gas.css" type="text/css">
<script type="text/javascript">
    <!--
    function generate(w) {
        if (w) {document.forms["LayerForm"].act.value = "generate";} 
        else {document.forms["LayerForm"].act.value = "save";}

        Ext.getCmp("contentProps").load({
            url: "classificationProperties.do",
            params: Ext.Ajax.serializeForm(document.forms["LayerForm"]),
            scripts:true
        });
    }

    //toggles all checkboxes status
    function allCheck() {
        for (var i = 0; i < document.forms["LayerForm"].elements.length; i++) {
            if (document.forms["LayerForm"].elements[i].type == "checkbox" && document.forms["LayerForm"].elements[i].name != "labels") {
                document.forms["LayerForm"].elements[i].checked = !document.forms["LayerForm"].elements[i].checked;
            }
        }
    }

    function openWindow(url, name, chromeless) {
        var chrome = "menubar=yes";
        if (chromeless != null && chromeless) {
            chrome = "menubar=no";
        }
        var w = self.opener.open(url, name, "width=450,height=600,scrollbars=yes,resizable=yes,status=yes,toolbar=no,personalbar=no,locationbar=no," + chrome);
        w.focus();
    }

    var spl = 0;
    <logic:present name="<%=ObjectKeys.CLASSIF_MESSAGE%>" scope="session">
        var classifMessage = "<bean:write name="<%=ObjectKeys.CLASSIF_MESSAGE%>"/>";
        spl = classifMessage.split(",");
    </logic:present>
    if (spl.length == 2) {
            // servlet generated a message concerning the number of classes
            if (spl[0] == "classlimitation") {
                    var txt = "<bean:message key="msg_class_limit_1"/>" + spl[1];
                    txt += "\n" + "<bean:message key="msg_class_limit_2"/>";
                    Ext.Msg.alert(txt);
            }
            // servlet generated a message concerning the column type: not elligible
            // for a range classif
            if (spl[0] == "classrange") {
                    var txt = "<bean:message key="msg_item_type"/>";
                    Ext.Msg.alert(txt);
            }
    }

    // opens class representation window and passes classid parameter
    function openRepresentationWindow(classid) {
        currentClassID = classid;
        var url = "MC_geonline_symbol_frameset.jsp";
        var w = self.open(url, "classrepresentation", "width=400,height=355,scrollbars=yes,resizable=yes,status=no,toolbar=no,personalbar=no,locationbar=no,menubar=no");
        w.focus();
    }

    // the class identifier for which the representation window was opened.
    // set when user clicked on a class image
    var currentClassID = "";

    // listen to representation window validation and refreshes class representation (mapserver image source) accordingly
    // type is the type of params passed to this function: either "symbol" or color"
    // value is the value of the parameter:
    //  if "symbol", value is the symbol name;
    //  if "color", value is the color, val2 is bgcolor, val3 is outlinecolor, val4 is txtcolor;
    function refreshClassRepresentation(type, value, val2, val3, val4) {
        var symbolName = "";
        var colors = "";

        // first, find the the hidden field for the current class color
        var hid = "document.forms['LayerForm'].c" + currentClassID + "_color";
        var hid = eval(hid);

        if (type == "symbol") {
            symbolName = value;
            hid.value = "null";
        } else {
            hid.value = value;
        }

        // second, find the image representation for the clicked class
        var img = document.images["img_class_" + currentClassID];

        //builds Servlet URL with parameters to refresh this image
        var servletURL = "GOClassRepresentation.jsv?classid=" + currentClassID;
        if (type == 'symbol') {
            servletURL += "&symbolkey=" + symbolName;
            servletURL += "&id=" + getUniqueID();   
        } else {
            servletURL += "&color=" + escape(value) + "&bgcolor=" + escape(val2) + "&outcolor=" + escape(val3) + "&txtcolor=" + escape(val4);
            servletURL += "&id=" + getUniqueID();   
        }
        // refreshes the image source
        img.src = servletURL;
    }

    // generates a unique identifier
    function getUniqueID() {
        var d = new Date();
        return "id" + d.getDay() + d.getMonth() + d.getHours() + d.getMinutes() + d.getSeconds();
    }

    function showFields() {
        trNumClasses = document.getElementById("class_number");
        trClassifItem = document.getElementById("classif_item");
        if(document.forms["LayerForm"].classificationType.value == "range") {
            trNumClasses.style.display = "";
            trClassifItem.style.display = "";
        } else if (document.forms["LayerForm"].classificationType.value == "uniquevalue") {
            trNumClasses.style.display = "none";
            trClassifItem.style.display = "";
        } else if (document.forms["LayerForm"].classificationType.value == "singleclass") {
            trNumClasses.style.display = "none";
            trClassifItem.style.display = "none";
        }

    }

    function sub() {
        GeneralLayout.composermap.baseLayer.mergeNewParams({'timestamp':Math.random()});
    }

    // returns a MS URL to get legend image for given layer name
    function getClassLegendImageURL(layerName) {
        var size = '';
        var mapPath = '';
        var msURL = '';
        return msURL + '?mode=legend&map=' + mapPath + '&layer=' + layerName + size + '&uid=' + new Date().getTime();
    }

    showFields();
    //-->
</script>
<html:form method="post" action="classificationProperties.do">
    <input type="hidden" name="act" value="generate"> 
    <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
        <tr align="center"> 
            <th class="th0" colspan="2"><bean:message key="classif"/></th>
        </tr>
        <tr> 
            <td class="td4tiny"><bean:message key="classif_type"/></td>
            <td class="td4tiny"> 
                <html:select name="LayerForm" property="classificationType" styleClass="tiny"  onchange="javascript:showFields();">
                    <html:option value="singleclass"><bean:message key="unique_class"/></html:option>
                    <html:option value="uniquevalue"><bean:message key="unique_value"/></html:option>
                    <html:option value="range"><bean:message key="interval"/></html:option>
                </html:select>
            </td>
        </tr>
        <tr id="class_number" style="display:none;"> 
            <td class="td4tiny"><bean:message key="class_number"/></td>
            <td class="td4tiny"> 
                <input type="text" name="numClasses" value="10" class="tiny" size="5">
            </td>
        </tr>
        <tr id="classif_item" style="display:none;">
        <td class="td4tiny"><bean:message key="classif_item"/></td>
        <td class="td4tiny"> 
            <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.classItem" styleClass="tiny">
                <option value=''></option>
                <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="columnNamesInfo" styleClass="tiny"/>
            </html:select>
        </td>
        <tr> 
            <td class="td4tiny"><bean:message key="label_item"/></td>
            <td class="td4tiny"> 
                <html:select name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelItem" styleClass="tiny">
                    <option value=''></option>
                    <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="columnNamesInfo" styleClass="tiny"/>
                </html:select>
                <a href='#' onclick="openWindow('loadLabelProperties.do', 'labelprops')"><bean:message key="advanced_parameters"/></a>
            </td>
        </tr>
        <tr> 
            <td class="td4tiny" align="center" colspan="2">
                <input type="button" name="generer" value="<bean:message key="generate_classes"/>" onClick="generate(true)" class="tiny">
            </td>
        </tr>
        <tr> 
            <logic:present name="<%=ObjectKeys.CLASSIF_TYPE%>" scope="session">
                <bean:define id="classes" name="<%=ObjectKeys.TMP_CLASSIFICATION%>"/>
                <td class="td4tiny" align="center" colspan="2"><bean:message key="existing_classes"/></td>
            </logic:present>
            <logic:notPresent name="<%=ObjectKeys.CLASSIF_TYPE%>" scope="session">
                <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.UNIQUE).toString()%>">
                    <bean:define id="classes" name="<%=ObjectKeys.CURRENT_GC%>" property="nullMsLayer.mapClass"/>
                    <td class="td4tiny" align="center" colspan="2"><bean:message key="generated_classes"/></td>
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.SINGLE).toString()%>">
                    <bean:define id="classes" name="<%=ObjectKeys.CURRENT_GC%>" property="nullMsLayer.mapClass"/>
                    <td class="td4tiny" align="center" colspan="2"><bean:message key="generated_classes"/></td>
                </logic:equal>
                <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.RANGE).toString()%>">
                    <bean:define id="classes" name="<%=ObjectKeys.CURRENT_GC%>" property="nullMsLayer.mapClass"/>
                    <td class="td4tiny" align="center" colspan="2"><bean:message key="generated_classes"/></td>
                </logic:equal>
            </logic:notPresent>
        </tr>
        <tr> 
            <td class="td4tiny" align="center" colspan="2"> 
                <input type="button" name="save" value="<bean:message key="save"/>" onClick="generate(false)" class="tiny">
            </td>
        </tr>
        <tr> 
            <td class="td4tiny" align="center" colspan="2"> 
                <table class="tablec" cellspacing="1" cellpadding='1'>
                    <tr> 
                        <th class="th0" align="center"><a href="#" class='tinynocolor' onclick="allCheck()"><bean:message key="all"/></a></th>
                        <th class="th0" align="center">&nbsp;</th>
                        <th class="th0" align="center"><bean:message key="color"/></th>
                        <th class="th0" align="center"><bean:message key="name_lower"/></th>
                        <th class="th0" align="center"><bean:message key="expression"/></th>
                    </tr>
                    <logic:notPresent name="<%=ObjectKeys.CLASSIF_TYPE%>" scope="session">
                        <bean:define id="classes" name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass"/>
                    </logic:notPresent>
                    <logic:present name="<%=ObjectKeys.CLASSIF_TYPE%>" scope="session">
                        <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.UNIQUE).toString()%>" scope="session">
                            <bean:define id="classes" name="<%=ObjectKeys.TMP_CLASSIFICATION%>"/>
                        </logic:equal>
                        <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.SINGLE).toString()%>" scope="session">
                            <bean:define id="classes" name="<%=ObjectKeys.TMP_CLASSIFICATION%>"/>
                        </logic:equal>
                        <logic:equal name="<%=ObjectKeys.CLASSIF_TYPE%>" value="<%=new Short(ObjectKeys.RANGE).toString()%>" scope="session">
                            <bean:define id="classes" name="<%=ObjectKeys.TMP_CLASSIFICATION%>"/>
                        </logic:equal>
                    </logic:present>
                    <logic:iterate name="classes" property="classes" id="cl" indexId="cntCl">
                    <tr>
                        <tr>
                            <td class="td4tiny"><input type='checkbox' name='c<bean:write name="cl" property="ID"/>_check' value='checkbox' CHECKED></td>
                            <td class="td4tiny" align='center' valign='middle'>
                                <logic:notPresent name="<%=ObjectKeys.TMP_CLASSIFICATION%>" scope="session">
                                    <a href='#' onClick="openWindow('MC_mapserver_class_properties.jsp?classid=<bean:write name="cl" property="ID"/>', 'classprops');">
                                        <img src='images/properties.gif' style="width:15px;height:15px;border:0;">
                                    </a>
                                </logic:notPresent>
                                <logic:present name="<%=ObjectKeys.TMP_CLASSIFICATION%>" scope="session">
                                    <span class="tinyred" style="font-weight:700;" title="<bean:message key="save_to_edit"/>">*</span>
                                </logic:present>
                            </td>
                            <td class="td4tiny" align='center' valign='middle'>
                                <a href = '#' onClick="openRepresentationWindow(<bean:write name="cl" property="ID"/>);">
                                    <img  style="width:40px;height:22px;border:0;" src='GOClassRepresentation.jsv?classid=<bean:write name="cl" property="ID"/>&amp;gencolor=<bean:write name="cl" property="color"/>' name='img_class_<bean:write name="cl" property="ID"/>'>
                                    <input type='hidden' name='c<bean:write name="cl" property="ID"/>_color' value='<bean:write name="cl" property="color"/>'>
                                </a>
                            </td>
                            <td class="td4tiny">
                                <input type='text' name='<bean:write name="cl" property="ID"/>_name' value='<bean:write name="cl" property="name"/>' class='tiny'>
                            </td>
                            <td class="td4tiny">
                                <input type='text' name='<bean:write name="cl" property="ID"/>_expression' value='<bean:write name="cl" property="expression"/>' class='tiny'>
                            </td>
                        </tr>
                    </tr>
                    </logic:iterate>
                </table>
            </td>
        </tr>
        <tr> 
            <td class="td4tiny" align="center" colspan="2"> 
                <input type="button" name="save" value="<bean:message key="save"/>" onClick="generate(false)" class="tiny">
            </td>
        </tr>
    </table>
</html:form>
