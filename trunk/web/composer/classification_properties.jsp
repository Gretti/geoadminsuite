<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.data.DataAccess" %>
<%@page import="org.geogurus.mapserver.objects.Class" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>

<link rel="stylesheet" href="css/gas.css" type="text/css">
<script type="text/javascript">
    var spl = [];
    var sym;
    var layerType = <bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.type"/>;
    
    // the lists of numeric attributes columns for the choosen GC. 
    var elOptNew;
    var numericColList = [];
<logic:iterate name="<%=ObjectKeys.CURRENT_GC%>" property="numericAttributeData" id="col">
    elOptNew = document.createElement('option');
    elOptNew.text = "<bean:write name="col"/>";
    elOptNew.value = "<bean:write name="col"/>";
    numericColList[numericColList.length] = elOptNew;
</logic:iterate>
        
    var fullColList = [];
<logic:iterate name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" id="col">
    elOptNew = document.createElement('option');
    elOptNew.text = "<bean:write name="col"/>";
    elOptNew.value = "<bean:write name="col"/>";
    fullColList[fullColList.length] = elOptNew;
</logic:iterate>

    var legendMessage = "";
<logic:present name="<%=ObjectKeys.LEGEND_MESSAGE%>">
    legendMessage = "<bean:write name='<%=ObjectKeys.LEGEND_MESSAGE%>' filter='true'/>";
    if (legendMessage.length > 0) {
        Ext.MessageBox.show({
           title: 'Legend error',
           msg: legendMessage,
           buttons: Ext.MessageBox.OK,
           icon: Ext.MessageBox.WARNING
        });
    }
</logic:present>

<logic:present name="<%=ObjectKeys.CLASSIF_MESSAGE%>">
    var classifMessage = "<bean:write name='<%=ObjectKeys.CLASSIF_MESSAGE%>'/>";
    spl = classifMessage.split(",");
</logic:present>
    if (spl.length == 2) {
            var txt = 'Classification error';

            // servlet generated a message concerning the number of classes
            if (spl[0] == "classlimitation") {
                    <%//txt = '<bean:message key="msg_class_limit_1" />' + "\n" + spl[1] + "\n" + '<bean:message key="msg_class_limit_2" />';%>
                    Ext.MessageBox.show({
                       title: 'Classification error',
                       msg: txt,
                       buttons: Ext.MessageBox.OK,
                       icon: Ext.MessageBox.WARNING
                    });
            }
            // servlet generated a message concerning the column type: not eligible
            // for a range classif
            if (spl[0] == "classrange") {
                    <%//txt = '<bean:message key="msg_item_type"/>';%>
                    Ext.MessageBox.show({
                       title: 'Classification error',
                       msg: txt,
                       buttons: Ext.MessageBox.OK,
                       icon: Ext.MessageBox.WARNING
                    });
            }
    }

    //toggles all checkboxes status
    function allCheck() {
        for (var i = 0; i < document.forms["LayerForm"].elements.length; i++) {
            if (document.forms["LayerForm"].elements[i].type == "checkbox" && document.forms["LayerForm"].elements[i].name != "labels") {
                document.forms["LayerForm"].elements[i].checked = !document.forms["LayerForm"].elements[i].checked;
            }
        }
    }
    
    function openRepresentationWindow(classid) {
        var layerform = document.forms['LayerForm'];
        layerform.selectedClassId.value = classid;
        if(Ext.getCmp('symbology-win')) Ext.getCmp('symbology-win').destroy();
        var symwin = createSymbologyWin();
        symwin.show(this);
        var strcolor = layerform.elements['c'+ classid +'color'];
        var strbgcolor = layerform.elements['c'+ classid +'bgcolor'];
        var strolcolor = layerform.elements['c'+ classid +'olcolor'];
        var color = null;
        var bgcolor = null;
        var olcolor = null;
        var vals;
        //instanciate color values reading hidden fields
        if( strcolor && strcolor.value != "") {
            vals = strcolor.value.split(" ");
            color = 'rgb(' + vals[0] + ',' + vals[1] + ',' + vals[2]+ ')';
            $('selected-foreground-color').style.backgroundColor = color;
            $('selected-foreground-color').innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
        } else {
            $('selected-foreground-color').style.backgroundColor = "white";
            $('selected-foreground-color').innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
        } 
        if($('selected-background-color')) {
            if(strbgcolor && strbgcolor.value != "") {
                vals = strbgcolor.value.split(" ");
                bgcolor = 'rgb(' + vals[0] + ',' + vals[1] + ',' + vals[2]+ ')';
                $('selected-background-color').style.backgroundColor = bgcolor;
                $('selected-background-color').innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
            } else {
                $('selected-background-color').style.backgroundColor = "white";
                $('selected-background-color').innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
            }
        }
        if($('selected-border-color')) {
            if(strolcolor && strolcolor.value != "") {
                vals = strolcolor.value.split(" ");
                olcolor = 'rgb(' + vals[0] + ',' + vals[1] + ',' + vals[2]+ ')';
                $('selected-border-color').style.backgroundColor = olcolor;
                $('selected-border-color').innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
            } else {
                $('selected-border-color').style.backgroundColor = "white";
                $('selected-border-color').innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
            }
        }
    }
    
    // Resets the color of the given component writing a red bold '/' on a white background
    function deleteColor(id) {
        $(id).style.backgroundColor = 'white';
        $(id).innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
    }
    // listen to representation window validation and refreshes class representation (mapserver image source) accordingly
    // type is the type of params passed to this function: either "symbol" or color"
    // value is the value of the parameter:
    //  if "symbol", value is the symbol name;
    //  if "color", value is the color, val2 is bgcolor, val3 is outlinecolor;
    function refreshClassRepresentation(symbol_, fgcolor_, bgcolor_, olcolor_) {
        var layerform = document.forms['LayerForm'];
        var color = layerform.elements['c' + currentClassID  + 'color'];
        var bgcolor = layerform.elements['c' + currentClassID  + 'bgcolor'];
        var olcolor = layerform.elements['c' + currentClassID  + 'olcolor'];
        layerform.value = symbol_;
        var currentClassID = layerform.selectedClassId.value;
        //builds Servlet URL with parameters to refresh this image
        //updates class color parameters hidden fields
        var servletURL = "GOClassRepresentation.jsv?classid=" + currentClassID;
        if(sym != null) {
            servletURL += "&symbolkey=" + symbol_;
        }
        if(color && fgcolor_ != null && fgcolor_.length > 0) {
            servletURL += "&color=" + escape(fgcolor_);
            color.value = fgcolor_;
        } else if(color) {
                color.value = '';
        }
        if(bgcolor && bgcolor_ != null && bgcolor_.length > 0) {
            servletURL += "&bgcolor=" + escape(bgcolor_);
            bgcolor.value = bgcolor_;
        } else if(bgcolor) {
            bgcolor.value = '';
        }
        if(olcolor && olcolor_ != null && olcolor_.length > 0) {
            servletURL += "&outcolor=" + escape(olcolor_);
            olcolor.value = olcolor_;
        } else if(olcolor) {
            olcolor.value = '';
        }
        servletURL += "&id=" + getUniqueID();
        // refreshes the image source
        document.images["img_class_" + currentClassID].src = servletURL;
    }

    // generates a unique identifier
    function getUniqueID() {
        var d = new Date();
        return "id" + d.getDay() + d.getMonth() + d.getHours() + d.getMinutes() + d.getSeconds();
    }
    
    // fills the column list select element with correct values (either the full list of columns
    // in case of unique value classif. or the list of numeric columns in case of range classif.   
    // @param classifType (the curent classification type: "range" or "uniqueavalue" strings)
    function fillColumnList(classifType) {
        var colSelect = document.forms['LayerForm'].elements['defaultMsLayer.classItem'];
        var curOptions;
        if ("range" == classifType) {
            curOptions = numericColList
        } else if ("uniquevalue" == classifType) {
            curOptions = fullColList;
        } else {
            return;
        }
        for (i = colSelect.options.length - 1; i>=0; i--) {
            colSelect.remove(i);
        }
        for (i = 0; i < curOptions.length; i++) {
            colSelect.options[i] = curOptions[i];
        }
    }

    function showFields() {

        trNumClasses = document.getElementById("class_number");
        trClassifItem = document.getElementById("classif_item");
        if(document.forms["LayerForm"].classificationType.value == "range") {
            fillColumnList("range");
            trNumClasses.style.display = "";
            trClassifItem.style.display = "";
        } else if (document.forms["LayerForm"].classificationType.value == "uniquevalue") {
            fillColumnList("uniquevalue");
            trNumClasses.style.display = "none";
            trClassifItem.style.display = "";
        } else if (document.forms["LayerForm"].classificationType.value == "singleclass") {
            trNumClasses.style.display = "none";
            trClassifItem.style.display = "none";
        }
    }

    function sub() {
        Ext.getCmp("contentProps").load({
            url: "classificationProperties.do",
            params: Ext.Ajax.serializeForm(document.forms["LayerForm"]),
            scripts:true,
            callback: function(){
                refreshComposerMap();
            }
        });
    }

    function selectSymbol(imgsrc, sym_) {
        sym = sym_;
        document.LayerForm.selectedSymbologyId.value = sym_;
        $('selected-symbol').innerHTML = '<img src="' + imgsrc + '">';
    }
    
    function deleteSymbol() {
        document.LayerForm.selectedSymbologyId.value = "";
        $('selected-symbol').innerHTML = '';
    }
    
    function loadSymbols() {
        var lstImages;
        if(layerType == <%=Layer.POINT%>) {
            lstImages = <bean:write name="<%=ObjectKeys.GAS_SYMBOL_LIST%>" property="pointIcons" filter="false"/>;
        }
        if(layerType == <%=Layer.LINE%> || layerType == <%=Layer.POLYLINE%>) {
            lstImages = <bean:write name="<%=ObjectKeys.GAS_SYMBOL_LIST%>" property="lineIcons" filter="false"/>;
        }
        if(layerType == <%=Layer.POLYGON%>) {
            lstImages = <bean:write name="<%=ObjectKeys.GAS_SYMBOL_LIST%>" property="polygonsIcons" filter="false"/>;
        }
        var lstSize = <bean:write name="<%=ObjectKeys.GAS_SYMBOL_LIST%>" property="size"/>;
        var strHtml = '';
        if(lstImages != null) {
            strHtml += '<table style=\"align:center;border:1px solid black;cellpadding:1px;cellspacing:1px\">';
            var newTr = 0;
            var n = 4;
            var i = 0;
            for (symName in lstImages) {
                //Starts a new line each n symbols
                var symIcon = lstImages[symName];
                if(newTr == 0) {
                    strHtml +='<tr>';
                }
                strHtml +='<td style="cursor:pointer;" onclick="javascript:selectSymbol(\'' + symIcon + '\',\'' + symName + '\');">' +
                          '<img src=\"' + symIcon + '\" style=\"border:0\">' +
                          '</td>';
                if(newTr == n && i++ != lstSize - 1) {
                    strHtml +='</tr>';
                    newTr = 0;
                } else {
                    newTr++;
                }
            }
            while(newTr <= n) {
                strHtml += '<td>&nbsp;</td>';
                newTr++;
            }
            strHtml += '</tr>';
            strHtml += '</table>';

            return strHtml;
        }
        return false;
    }
    
    function bgColorToRgb(bgColor) {
        if (bgColor.length == 0) {
            return "";
        }
        var arrayRGB = bgColor.split('(')[1].split(')')[0].split(',');
        return arrayRGB[0] + " " + arrayRGB[1] + " " + arrayRGB[2];
    }
    
    function createSymbologyWin() {
            /*************************************************************************************************************************/
            /*********************************************SYMBOLOGY WINDOW************************************************************/
            /*************************************************************************************************************************/
            //var reminderHtml = '<form name="frmSelSymbolColor">Symbol : <span id="selected-symbol"></span>&nbsp;' + 
            var reminderHtml = '<form name="frmSelSymbolColor"><span id="selected-symbol"></span>&nbsp;' + 
                               /*'<img src="images/trashcan.png" onclick="deleteSymbol()" title="Transparent" alt="Transparent">&nbsp;&nbsp;' + */
                               '  - <input type="radio" name="sellayer" value="selected-foreground-color" checked="checked">&nbsp;' +
                               'Foreground : <span class="reminderColor" ' +
                               'id="selected-foreground-color">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>' +
                               '&nbsp;&nbsp;<img src="images/trashcan.png" onclick="deleteColor(\'selected-foreground-color\')" title="Transparent" alt="Transparent">&nbsp;&nbsp;'
            if(layerType == <%=Layer.POLYGON%>) {
                reminderHtml += ' - <input type="radio" name="sellayer" value="selected-background-color">&nbsp;' +
                                'Background : <span class="reminderColor" ' +
                                'id="selected-background-color">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>' +
                                '&nbsp;&nbsp;<img src="images/trashcan.png" onclick="deleteColor(\'selected-background-color\')" title="Transparent" alt="Transparent">&nbsp;&nbsp;' +
                                ' - <input type="radio" name="sellayer" value="selected-border-color">&nbsp;' +
                                'Border : <span class="reminderColor" ' +
                                'id="selected-border-color">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>' +
                                '&nbsp;&nbsp;<img src="images/trashcan.png" onclick="deleteColor(\'selected-border-color\')" title="Transparent" alt="Transparent">'
            }
            reminderHtml += '</form>';


            //Tab panel
            var tab = new Ext.FormPanel({
                labelAlign: 'top',
                bodyStyle:'padding:5px',
                width: 600,
                id:'frmcolorSymbolPicker',
                items: [{
                    layout:'fit',
                    border:true,
                    height:70,
                    title: 'Selection Reminder',
                    html: '<p><br />'+ reminderHtml + '</p>'
                },{
                    xtype:'tabpanel',
                    plain:true,
                    activeTab: 1,
                    height:235,
                    defaults:{bodyStyle:'padding:10px'},
                    items:[{
                        title:'Symbols',
                        id:'symbols',
                        layout:'fit',
                        html: loadSymbols()
                    },{
                        title:'Color',
                        id:'color',
                        layout:'fit',
                        items:{
                                xtype:'panel',
                                id:'colorpicker-container',
                                border:false
                            }
                    }]
                }],

                buttons: [{
                    text: 'Apply',
                    handler: function() {
                            var fgcol = "";
                            var bgcol = "";
                            var olcol = "";
                            var txtcol = "";
                            if($('selected-foreground-color').innerHTML.lastIndexOf('/') == -1) {
                                fgcol = bgColorToRgb($('selected-foreground-color').style.backgroundColor);
                            }
                            if($('selected-background-color') != null &&
                               $('selected-background-color').innerHTML.lastIndexOf('/') == -1) {
                                bgcol = bgColorToRgb($('selected-background-color').style.backgroundColor);
                            }
                            if($('selected-border-color') != null &&
                               $('selected-border-color').innerHTML.lastIndexOf('/') == -1) {
                                olcol = bgColorToRgb($('selected-border-color').style.backgroundColor);
                            }
                            refreshClassRepresentation(sym, fgcol, bgcol, olcol, txtcol);
                            var actionParams = "selectedClassId=";
                            actionParams += document.forms['LayerForm'].selectedClassId.value;
                            actionParams += "&selectedSymbologyId=";
                            actionParams += document.forms['LayerForm'].selectedSymbologyId.value == 'undefined' ? '' :
                                document.forms['LayerForm'].selectedSymbologyId.value; 
                            actionParams += "&classColor=";
                            actionParams += fgcol;
                            actionParams += "&classBGColor=";
                            actionParams += bgcol;
                            actionParams += "&classOLColor=";
                            actionParams += olcol + "&";
                            Ext.Ajax.request({
                                url:'classProperties.do',
                                params: actionParams,
                                success: function (result, request) {
                                        var res = Ext.util.JSON.decode(result.responseText);
                                        // sets image for choosen class
                                        if (res.message.length > 0) {
                                            Ext.Msg.alert("Class properties error", res.message);
                                        } else {
                                            document.images["img_class_" + document.forms['LayerForm'].selectedClassId.value].src = res.classIcon;
                                        }
                                },
                                failure: function (result, request) {
                                        Ext.Msg.alert("Class properties error", 
                                        "Server was unable to process request. see server logs. Sent params: " + actionParams +
                                        "\n server response:" + result.responseText);
                                }
                            });
                            refreshComposerMap();
                            Ext.getCmp('symbology-win').hide();
                        }
                },{
                    text: 'Close',
                    handler: function() {
                            Ext.getCmp('symbology-win').hide();
                        }
                }]
            });
            var winSymbology = new Ext.Window({
                id:'symbology-win',
                layout:'fit',
                width:640,
                height:480,
                closeAction:'hide',
                modal: true,
                items: [tab]
            });
            Ext.getCmp('color').on('activate', function() {
                Ext.getCmp('colorpicker-container').load({
                    url: "symbology/colorPicker.jsp",
                    waitMsg:'Loading ...',
                    scripts:true
                });
                Ext.getCmp('colorpicker-container').doLayout();
            });
            return winSymbology;
    }
    
    Ext.onReady(function() {            
            /*************************************************************************************************************************/
            /*************************************************FIELD DISPLAY***********************************************************/
            /*************************************************************************************************************************/
            showFields();
        });
</script>
<html:form method="post" action="classificationProperties.do">
    <input type='hidden' value="" name="selectedClassId"/>
    <input type='hidden' value="" name="selectedSymbologyId"/>
    <!--classification field-->
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
                <html:options name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" styleClass="tiny"/>
            </html:select>
        </td>
        </tr>
        <tr>
            <td class="td4tiny" align="center" colspan="2">
                <table class="tablec" cellspacing="1" cellpadding='1'>
                    <tr>
                        <!--<th class="th0" align="center"><a href="#" class='tinynocolor' onclick="allCheck()"><bean:message key="all"/></a></th>-->
                        <th class="th0" align="center">&nbsp;</th>
                        <th class="th0" align="center"><bean:message key="color"/></th>
                        <th class="th0" align="center"><bean:message key="name_lower"/></th>
                        <th class="th0" align="center"><bean:message key="expression"/></th>
                    </tr>
                    <bean:define id="classes" name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass"/>
                    <logic:iterate name="classes" property="classes" id="cl" indexId="cntCl">
                    <tr>
                        <tr>
                            <!--<td class="td4tiny"><input type='checkbox' name='c<bean:write name="cl" property="ID"/>_check' value='checkbox' CHECKED></td>-->
                            <td class="td4tiny" align='center' valign='middle'>
                                <img src='images/properties.gif' style="width:15px;height:15px;border:0;">
                            </td>
                            <td class="td4tiny" align='center' valign='middle'>
                                <a href = '#' onClick="openRepresentationWindow(<bean:write name="cl" property="ID"/>);">
                                    <img  style="width:40px;height:22px;border:0;" src='<bean:write name="cl" property="legendURL"/>' name='img_class_<bean:write name="cl" property="ID"/>'>
                                    <input type='hidden' name='c<bean:write name="cl" property="ID"/>color' value='<bean:write name="cl" property="color" ignore="true"/>'>
                                    <input type='hidden' name='c<bean:write name="cl" property="ID"/>bgcolor' value='<bean:write name="cl" property="backgroundColor" ignore="true"/>'>
                                    <input type='hidden' name='c<bean:write name="cl" property="ID"/>olcolor' value='<bean:write name="cl" property="outlineColor" ignore="true"/>'>
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
    </table>
</html:form>
