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

<script type="text/javascript">
    var layerType = <bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.type"/>;
    var dataClassifResult = [];
    var symbolsInfo = {};
    var selectedClassId = null;
    var selectedSymbologyId = null;
    // Emphasize the classification icon
    function makeItBig(img) {
        img.style.width = '40px';
        img.style.height = '22px';
    }
    // Restores the small classification icon
    function makeItSmall(img) {
        img.style.width = '20px';
        img.style.height = '11px';
    }
    // Nothing for the moment
    function editSymbol(img) {
        var clid = 'c' + img.id.split('_')[2];
        openRepresentationWindow(clid);
    }
    // Generate a new classification based on specified params in form
    function generate() {
        var maskClassif = new Ext.LoadMask(Ext.getCmp('formProps').getEl(),{
            msg:'processing ...'
        });
        maskClassif.show();
        Ext.Ajax.request({
            url: "classificationProperties.do",
            params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.getEl().dom),
            failure: function(){
                maskClassif.hide();
                Ext.MessageBox.show({
                    title: 'Classification error',
                    msg: 'Error during classification process !',
                    buttons: Ext.MessageBox.OK,
                    icon: Ext.MessageBox.WARNING
                });
            },
            success: function(result){
                maskClassif.hide();
                //Gets the new classification reading response
                //response is a string : "cl1_icon,cl1_name,cl1_expression|cl2_icon,cl2_name,cl2_expression|..."
                var strDataClasses = result.responseText;
                var arrayDataClasses = [];
                var splittedClasses = strDataClasses.split("|");
                var arrayClassInfo;
                symbolsInfo = new Array(0);
                for(var c = 0;c < splittedClasses.length; c++) {
                    arrayClassInfo = splittedClasses[c].split("&&&");
                    arrayDataClasses.push([
                        '<img id=\'img_class_' + arrayClassInfo[0] + '\''+
                        'style="width:20px;height:11px;border:0;" ' +
                        'onmouseover="javascript:makeItBig(this);" ' +
                        'onmouseout="javascript:makeItSmall(this);" ' +
                        'onclick="javascript:editSymbol(this);" ' +
                        'src=\'' + arrayClassInfo[1] + '\'>',
                        arrayClassInfo[2],
                        arrayClassInfo[3]
                    ]);
                    symbolsInfo['c'+arrayClassInfo[0]] = {
                        'color':arrayClassInfo[4],
                        'bgcolor':arrayClassInfo[5] == 'null' ? '' : arrayClassInfo[5],
                        'olcolor':arrayClassInfo[6] == 'null' ? '' : arrayClassInfo[6]
                    };
                }
                storeClassifResult.loadData(arrayDataClasses);
                gridClassifResult.view.render();
                gridClassifResult.view.refresh();
                refreshComposerMap();
            }
        });
    }
    // Submit GridEditor content to reflect changes in current classification
    function sub() {
        refreshComposerMap();
    }
    // Opens the classification representation window
    function openRepresentationWindow(classid) {
        selectedClassId = classid;
        var strcolor = symbolsInfo[classid].color;
        var strbgcolor = symbolsInfo[classid].bgcolor;
        var strolcolor = symbolsInfo[classid].olcolor;
        var color = null;
        var bgcolor = null;
        var olcolor = null;
        var vals;
        if(Ext.getCmp('symbology-win')) Ext.getCmp('symbology-win').destroy();
        var symwin = createSymbologyWin();
        symwin.show(this);
        //instanciate color values reading hidden fields
        if( strcolor && strcolor != "") {
            vals = strcolor.split(" ");
            color = 'rgb(' + vals[0] + ',' + vals[1] + ',' + vals[2]+ ')';
            $('selected-foreground-color').style.backgroundColor = color;
            $('selected-foreground-color').innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
        } else {
            $('selected-foreground-color').style.backgroundColor = "white";
            $('selected-foreground-color').innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
        }
        if($('selected-background-color')) {
            if(strbgcolor && strbgcolor != "") {
                vals = strbgcolor.split(" ");
                bgcolor = 'rgb(' + vals[0] + ',' + vals[1] + ',' + vals[2]+ ')';
                $('selected-background-color').style.backgroundColor = bgcolor;
                $('selected-background-color').innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;';
            } else {
                $('selected-background-color').style.backgroundColor = "white";
                $('selected-background-color').innerHTML = '&nbsp;&nbsp;/&nbsp;&nbsp;';
            }
        }
        if($('selected-border-color')) {
            if(strolcolor && strolcolor != "") {
                vals = strolcolor.split(" ");
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
        var color = symbolsInfo[selectedClassId].color;
        var bgcolor = symbolsInfo[selectedClassId].bgcolor;
        var olcolor = symbolsInfo[selectedClassId].olcolor;
        //builds Servlet URL with parameters to refresh this image
        //updates class color parameters hidden fields
        var servletURL = "GOClassRepresentation.jsv?classid=" + selectedClassId;
        if(symbol_ != null) {
            servletURL += "&symbolkey=" + symbol_;
        }
        if(color && fgcolor_ != null && fgcolor_.length > 0) {
            servletURL += "&color=" + escape(fgcolor_);
            symbolsInfo[selectedClassId].color = fgcolor_;
        } else if(color) {
            symbolsInfo[selectedClassId].color = '';
        }
        if(bgcolor && bgcolor_ != null && bgcolor_.length > 0) {
            servletURL += "&bgcolor=" + escape(bgcolor_);
            symbolsInfo[selectedClassId].bgcolor = bgcolor_;
        } else if(bgcolor) {
            symbolsInfo[selectedClassId].bgcolor = '';
        }
        if(olcolor && olcolor_ != null && olcolor_.length > 0) {
            servletURL += "&outcolor=" + escape(olcolor_);
            symbolsInfo[selectedClassId].olcolor = olcolor_;
        } else if(olcolor) {
            symbolsInfo[selectedClassId].olcolor = '';
        }
        servletURL += "&id=" + getUniqueID();
        // refreshes the image source
        document.images["img_class_" + selectedClassId.substring(1)].src = servletURL;
    }
    // generates a unique identifier
    function getUniqueID() {
        var d = new Date();
        return "id" + d.getDay() + d.getMonth() + d.getHours() + d.getMinutes() + d.getSeconds();
    }
    //Updates selected symbol in representation window
    function selectSymbol(imgsrc, sym_) {
        selectedSymbologyId = sym_;
        $('selected-symbol').innerHTML = '<img src="' + imgsrc + '">';
    }
    //Resets selected symbol
    function deleteSymbol() {
        selectedSymbologyId = null;
        $('selected-symbol').innerHTML = '';
    }
    //Creates a table containing all available symbols for selected layer type
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
            for (var symName in lstImages) {
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
    //Transforms background-color property style to a RGB triplet
    function bgColorToRgb(bgColor) {
        if (bgColor.length == 0) {
            return "";
        }
        var arrayRGB = bgColor.split('(')[1].split(')')[0].split(',');
        return arrayRGB[0] + " " + arrayRGB[1] + " " + arrayRGB[2];
    }
    //Creates and displays Symbology window
    function createSymbologyWin() {
            /*************************************************************************************************************************/
            /*********************************************SYMBOLOGY WINDOW************************************************************/
            /*************************************************************************************************************************/
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
                            refreshClassRepresentation(selectedSymbologyId, fgcol, bgcol, olcol, txtcol);
                            var actionParams = "selectedClassId=";
                            actionParams += selectedClassId;
                            actionParams += "&selectedSymbologyId=";
                            actionParams += selectedSymbologyId == null ? '' : selectedSymbologyId;
                            actionParams += "&classColor=";
                            actionParams += fgcol;
                            actionParams += "&classBGColor=";
                            actionParams += bgcol;
                            actionParams += "&classOLColor=";
                            actionParams += olcol + "&";
                            Ext.Ajax.request({
                                url:'classProperties.do',
                                params: actionParams,
                                success: function (result) {
                                        var res = Ext.util.JSON.decode(result.responseText);
                                        // sets image for choosen class
                                        if (res.message.length > 0) {
                                            Ext.Msg.alert("Class properties error", res.message);
                                        } else {
                                            document.images["img_class_" + selectedClassId.substring(1)].src = res.classIcon;
                                        }
                                },
                                failure: function (result) {
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

    // the lists of numeric attributes columns for the choosen GC.
    var numericFields = [];
    var allFields = [];
<logic:iterate name="<%=ObjectKeys.CURRENT_GC%>" property="numericAttributeData" id="numcol">
    numericFields.push(['<bean:write name="numcol"/>','<bean:write name="numcol"/>']);
</logic:iterate>
<logic:iterate name="<%=ObjectKeys.CURRENT_GC%>" property="attributeDataNames" id="allcol">
    allFields.push(['<bean:write name="allcol"/>','<bean:write name="allcol"/>']);
</logic:iterate>

<logic:present name="<%=ObjectKeys.LEGEND_MESSAGE%>">
    var legendMessage = '<bean:write name="<%=ObjectKeys.LEGEND_MESSAGE%>" filter="true"/>';
    Ext.MessageBox.show({
        title: 'Legend error',
        msg: legendMessage,
        buttons: Ext.MessageBox.OK,
        icon: Ext.MessageBox.WARNING
    });
</logic:present>

<logic:present name="<%=ObjectKeys.CLASSIF_MESSAGE%>">
    var classifMessage = '<bean:write name="<%=ObjectKeys.CLASSIF_MESSAGE%>"/>';
    var spl = classifMessage.split(",");
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
</logic:present>
    var classifTypes = [
        ["singleclass","<bean:message key="unique_class"/>"],
        ["uniquevalue","<bean:message key="unique_value"/>"],
    ];
    var classifModes = [
        ["minmax","Arythmetic division"],
        ["percentile","Percentiles"],
        ["stddev","Standard deviation"]
    ];
    if(numericFields.length > 0) {
        classifTypes.push(["range","<bean:message key="interval"/>"]);
    }
    var cmbClassifType = new Ext.form.ComboBox({
        id:'cmbClassifType',
        fieldLabel: '<bean:message key="classif_type"/>',
        hiddenName: 'classificationType',
        store: new Ext.data.SimpleStore({
            fields: ['value','type'],
            data : classifTypes
        }),
        displayField:'type',
        valueField:'value',
        value: "singleclass",
        typeAhead: true,
        autoWidth: true,
        mode: 'local',
        triggerAction: 'all',
        forceSelection: true,
        selectOnFocus:true,
        listClass: 'x-combo-list-small',
        onSelect: function(record) {
            this.collapse();
            this.setValue(record.data.value);
            if(record.data.value == "singleclass") {
                Ext.getCmp('cmbClassItem').hide();
                Ext.getCmp('cmbClassItem').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('cmbClassifModes').hide();
                Ext.getCmp('cmbClassifModes').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('clrClassFieldFrom').hide();
                Ext.getCmp('clrClassFieldFrom').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('clrClassFieldTo').hide();
                Ext.getCmp('clrClassFieldTo').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('nbrNumClasses').hide();
                Ext.getCmp('nbrNumClasses').getEl()
                .up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('formProps').setHeight(110);
            }else if(record.data.value == "uniquevalue") {
                Ext.getCmp('cmbClassifModes').hide();
                Ext.getCmp('cmbClassifModes').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('clrClassFieldFrom').hide();
                Ext.getCmp('clrClassFieldFrom').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('clrClassFieldTo').hide();
                Ext.getCmp('clrClassFieldTo').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('cmbClassItem').store.loadData(allFields,false);
                Ext.getCmp('cmbClassItem').show();
                Ext.getCmp('cmbClassItem').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                if(Ext.getCmp('cmbClassItem').getValue() == '') {
                    Ext.getCmp('cmbClassItem').setValue(allFields[0][0]);
                }
                Ext.getCmp('nbrNumClasses').hide();
                Ext.getCmp('nbrNumClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('formProps').setHeight(140);
            }else if(record.data.value == "range") {
                Ext.getCmp('cmbClassifModes').show();
                Ext.getCmp('cmbClassifModes').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                Ext.getCmp('cmbClassItem').store.loadData(numericFields,false);
                Ext.getCmp('cmbClassItem').show();
                Ext.getCmp('cmbClassItem').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                Ext.getCmp('clrClassFieldFrom').show();
                Ext.getCmp('clrClassFieldFrom').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                Ext.getCmp('clrClassFieldTo').show();
                Ext.getCmp('clrClassFieldTo').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                if(Ext.getCmp('cmbClassItem').getValue() == '') {
                    Ext.getCmp('cmbClassItem').setValue(numericFields[0][0]);
                } else {
                    var resetValue = true;
                    for(var idxFld = 0 ; idxFld < numericFields.length; idxFld++) {
                        if(Ext.getCmp('cmbClassItem').getValue() == numericFields[idxFld][0]) {
                            resetValue = false;
                            break;
                        }
                    }
                    if(resetValue) Ext.getCmp('cmbClassItem').setValue(numericFields[0][0]);
                }
                Ext.getCmp('nbrNumClasses').show();
                Ext.getCmp('nbrNumClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
                Ext.getCmp('formProps').setHeight(215);
            }
        }
    });
    var cmbClassifModes = new Ext.form.ComboBox({
        id:'cmbClassifModes',
        fieldLabel: "Modes",
        hiddenName: 'classifMode',
        store: new Ext.data.SimpleStore({
            fields: ['value','type'],
            data : classifModes
        }),
        hidden : true,
        hideLabel : true,
        displayField:'type',
        valueField:'value',
        typeAhead: true,
        autoWidth: true,
        mode: 'local',
        allowBlank : false,
        blankText : 'Select a field',
        value:'minmax',
        triggerAction: 'all',
        forceSelection: true,
        selectOnFocus:true,
        listClass: 'x-combo-list-small',
        onSelect: function(record) {
            this.collapse();
            this.setValue(record.data.value);
            if(record.data.value == "stddev") {
                Ext.getCmp('nbrNumClasses').hide();
                Ext.getCmp('nbrNumClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('cmbClassifStdDevClasses').show();
                Ext.getCmp('cmbClassifStdDevClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
            } else {
                Ext.getCmp('cmbClassifStdDevClasses').hide();
                Ext.getCmp('cmbClassifStdDevClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item x-hide-label";
                Ext.getCmp('nbrNumClasses').show();
                Ext.getCmp('nbrNumClasses').getEl().
                    up('.x-form-item').dom.className = "x-form-item";
            }
        }
    });
    var cmbClassifStdDevClasses = new Ext.form.ComboBox({
        id:'cmbClassifStdDevClasses',
        fieldLabel: "Classes",
        hiddenName: 'stddevClasses',
        store: new Ext.data.SimpleStore({
            fields: ['value','type'],
            data : [
                ['quater','1/4 sqrt'],
                ['half','1/2 sqrt'],
                ['one','1 sqrt']
            ]
        }),
        hidden : true,
        hideLabel : true,
        displayField:'type',
        valueField:'value',
        typeAhead: true,
        autoWidth: true,
        mode: 'local',
        allowBlank : false,
        blankText : 'Select a step',
        value:'quater',
        triggerAction: 'all',
        forceSelection: true,
        selectOnFocus:true,
        listClass: 'x-combo-list-small'
    });
    var clrClassifFields = new Ext.Panel({
            id:'clrClassifFields',
            layout:'column',
            border : false,
            items:[{
                columnWidth:.5,
                layout: 'form',
                labelWidth: 50,
                items: [{
                    id:'clrClassFieldFrom',
                    xtype:'colorfield',
                    hidden : true,
                    hideLabel : true,
                    name: 'fromColor',
                    fieldLabel: 'from',
                    value: '#FFFFFF'
                }]
            },{
                columnWidth:.5,
                layout: 'form',
                labelWidth: 50,
                items: [{
                    id:'clrClassFieldTo',
                    xtype:'colorfield',
                    hidden : true,
                    hideLabel : true,
                    name: 'toColor',
                    fieldLabel: 'to',
                    value: '#FF0000'
                }]
            }]
    });

    var cmbClassItem = new Ext.form.ComboBox({
        id:'cmbClassItem',
        fieldLabel: "<bean:message key="classif_item"/>",
        name: 'defaultMsLayer.classItem',
        store: new Ext.data.SimpleStore({
            fields: ['value','type'],
            data : allFields
        }),
        hidden : true,
        hideLabel : true,
        displayField:'type',
        valueField:'value',
        typeAhead: true,
        autoWidth: true,
        mode: 'local',
        allowBlank : false,
        blankText : 'Select a field',
        triggerAction: 'all',
        forceSelection: true,
        selectOnFocus:true,
        listClass: 'x-combo-list-small'
    });
    var fldNumClasses = new Ext.form.NumberField({
        id:'nbrNumClasses',
        fieldLabel: "<bean:message key="class_number"/>",
        name: 'numClasses',
        value: 5,
        allowDecimals: false,
        allowNegative: false,
        allowBlank: false,
        hidden: true,
        hideLabel : true,
        decimalPrecision: 0,
        minValue: 0,
        maxValue: 250
    });

    /**Panel handling classification choices**/
    if(Ext.getCmp('formProps')) Ext.getCmp('formProps').destroy();
    var formProps = new Ext.FormPanel({
        id: 'formProps',
        collapsible: true,
        labelWidth: 150,
        title:'<bean:message key="classif"/>',
        frame:true,
        bodyStyle:'padding:5px 5px 0',
        autoWidth: true,
        autoHeight: false,
        height: 185,
        items: [cmbClassifType,cmbClassifModes,cmbClassItem,fldNumClasses,cmbClassifStdDevClasses,clrClassifFields],
        buttons:[{
                text:'Generate',
                handler: function() {
                    generate();
                }
            }]
    });

    /**Panel handling grid of classes**/
    var cmClassifResult = new Ext.grid.ColumnModel([
        {
            id:'classifColIcon',
            header: "<bean:message key="color"/>",
            dataIndex: 'icon',
            width: 70
        },{
            id:'classifColName',
            header: "<bean:message key="name_lower"/>",
            dataIndex: 'name',
            width: 150,
            editor: new Ext.form.TextField({
                allowBlank: false
            })
        },{
            id:'classifColExpr',
            header: "<bean:message key="expression"/>",
            dataIndex: 'expression',
            editor: new Ext.form.TextField({
                allowBlank: true
            })
        }
    ]);

    <logic:iterate name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass.classes" id="cl" indexId="cntCl">
        dataClassifResult.push([
            '<img id=\'img_class_<bean:write name="cl" property="ID"/>\''+
            'style="width:20px;height:11px;border:0;" ' +
            'onmouseover="javascript:makeItBig(this);" ' +
            'onmouseout="javascript:makeItSmall(this);" ' +
            'onclick="javascript:editSymbol(this);" ' +
            'src=\'<bean:write name="cl" property="legendURL"/>\'>',
            '<bean:write name="cl" property="name"/>',
            '<bean:write name="cl" property="expression" ignore="true"/>'
        ]);
        symbolsInfo['c<bean:write name="cl" property="ID"/>'] = {
                'color':'<bean:write name="cl" property="color" ignore="true"/>',
                'bgcolor':'<bean:write name="cl" property="backgroundColor" ignore="true"/>',
                'olcolor':'<bean:write name="cl" property="outlineColor" ignore="true"/>'
            };
    </logic:iterate>

        var storeClassifResult = new Ext.data.SimpleStore({
            fields: [
                {name: 'icon'},
                {name: 'name'},
                {name: 'expression'}
            ]
        });
        var gridClassifResult = new Ext.grid.EditorGridPanel({
            id: 'gridClassifResult',
            store: storeClassifResult,
            cm: cmClassifResult,
            autoExpandColumn:'classifColExpr',
            border: false
        });

        storeClassifResult.loadData(dataClassifResult);

        /**Add panels to content panel**/
        Ext.getCmp('contentProps').add(formProps);
        Ext.getCmp('contentProps').add(gridClassifResult);
        Ext.getCmp('contentProps').doLayout();
        Ext.getCmp('formProps').setHeight(110);
        gridClassifResult.view.render();
        gridClassifResult.view.refresh();
</script>
