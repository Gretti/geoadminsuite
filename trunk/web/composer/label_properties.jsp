<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.data.DataAccess" %>
<%@page import="org.geogurus.tools.DataManager" %>
<%@page import="org.geogurus.mapserver.objects.Class" %>
<%@page import="org.geogurus.mapserver.objects.RGB" %>
<%@page import="org.geogurus.mapserver.objects.Layer" %>
<%@page import="org.geogurus.mapserver.objects.Label" %>

<bean:define id="lab" name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.mapClass.firstClass.label"/>

<script type="text/javascript">
        <!--

        function checkForm() {

            var c2 = null;
            // angle check: empty, 'auto' or int
            var c = Ext.getCmp('labelProps').form.findField('label.angle').value;
            if (c.length > 0) {
                if (isNaN(c) && c.toLowerCase() != 'auto') {
                    Ext.MessageBox.show({
                       title: 'Error',
                       msg: "<bean:message key="msg_angle"/>",
                       buttons: Ext.MessageBox.OK,
                       icon: Ext.MessageBox.WARNING
                    });
                    return false;
                }
            }
            
            // font type: if truetype choosen, must select a font alias in select list
            if (Ext.getCmp('labelProps').form.findField('label.type').value == '<%=String.valueOf(Label.TRUETYPE)%>' && 
                Ext.getCmp('labelProps').form.findField('label.font').value == '') {
                    Ext.MessageBox.show({
                       title: 'Error',
                       msg: "<bean:message key="msg_font"/>",
                       buttons: Ext.MessageBox.OK,
                       icon: Ext.MessageBox.WARNING
                    });
                    return false;
            }
            
            return true;
        }

        // some functions to control logic of font types: truetypes or bitmap
        // when font type is set to true type
        function fontTypeChanged (fld,evt) {
            var frm = Ext.getCmp('labelProps').form;
            if (fld.getValue() == "<%=String.valueOf(Label.BITMAP)%>") {
                // disables font and truetype size
                frm.findField('label.font').disable();
                frm.findField('truetypeSize').disable();
                // enables bitmap size
                frm.findField('bitmapSize').enable();
                // forces redrawing (workaround for enabling hiddenValue)
                frm.findField('bitmapSize').show();
            } else {
                // enables font and truetype size
                frm.findField('label.font').enable();
                frm.findField('truetypeSize').enable();
                // puts default size if value is negative
                if(frm.findField('truetypeSize').getValue() < 0) {
                    frm.findField('truetypeSize').setValue('10');
                }
                // disables bitmap size
                frm.findField('bitmapSize').disable();
            }
        }
        
        var types = [
            ['<%=String.valueOf(Label.BITMAP)%>','Bitmap'],
            ['<%=String.valueOf(Label.TRUETYPE)%>','TrueType']
        ];
        
        var fonts = [];
        <logic:iterate id="font" name="fontList" scope="session">
        fonts.push(['<bean:write name="font"/>']);
        </logic:iterate>
        
        var bmpsizes = [
            ['<%=String.valueOf(Label.TINY)%>','tiny'],
            ['<%=String.valueOf(Label.SMALL)%>','small'],
            ['<%=String.valueOf(Label.MEDIUM)%>','medium'],
            ['<%=String.valueOf(Label.LARGE)%>','large'],
            ['<%=String.valueOf(Label.GIANT)%>','giant'],
        ];
        var positions = [
            ['<%=String.valueOf(Label.UL)%>','UL'],
            ['<%=String.valueOf(Label.UC)%>','UC'],
            ['<%=String.valueOf(Label.UR)%>','UR'],
            ['<%=String.valueOf(Label.CL)%>','CL'],
            ['<%=String.valueOf(Label.CC)%>','CC'],
            ['<%=String.valueOf(Label.UR)%>','CR'],
            ['<%=String.valueOf(Label.LL)%>','LL'],
            ['<%=String.valueOf(Label.LC)%>','LC'],
            ['<%=String.valueOf(Label.LR)%>','LR'],
            ['<%=String.valueOf(Label.AUTO)%>','AUTO']
        ];
        
        var flds = [];
        <logic:iterate id="field" name="<%=ObjectKeys.CURRENT_GC%>" property="columnInfo">
            flds.push(['<bean:write name="field" property="name"/>']);
        </logic:iterate>

        var angle = '<bean:write name="lab" property="angle" ignore="true"/>';
        <logic:equal name="lab" property="angle" value="<%=String.valueOf(Label.AUTO)%>">
            angle='auto';
        </logic:equal>
        <logic:equal name="lab" property="angle" value="<%=String.valueOf(Label.UNDEF)%>">
            angle='auto';
        </logic:equal>

        //Builds FormPanel to inject in opened Window
        if(Ext.getCmp('labelProps')) Ext.getCmp('labelProps').destroy();
        
        var labelProps = new Ext.FormPanel({
                            id: 'labelProps',
                            labelWidth: 150,
                            frame:true,
                            width: '100%',
                            autoScroll:true,
                            border: false,
                            items: [{
                                        xtype: 'fieldset',
                                        title: "<bean:message key="label_param"/>",
                                        autoHeight:true,
                                        autoWidth:true,
                                        defaultType: 'textfield',
                                        defaults: {msgTarget: 'side', grow:true},
                                        items: [
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="label_item"/>",
                                                name: 'labelItem',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['name'],
                                                    data : flds
                                                }),
                                                displayField:'name',
                                                valueField:'name',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelItem"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            {
                                                fieldLabel: "<bean:message key="color"/>",
                                                name: 'labelColor',
                                                listeners: {
                                                    'render': function(f,e) {checkRgb(f,e);},
                                                    'blur': function(f,e) {checkRgb(f,e);}
                                                },
                                                value: "<bean:write name="lab" property="color"/>"
                                            },
                                            {
                                                fieldLabel: "<bean:message key="outline_color"/>",
                                                name: 'labelOutlineColor',
                                                listeners: {
                                                    'render': function(f,e) {checkRgb(f,e);},
                                                    'blur': function(f,e) {checkRgb(f,e);}
                                                },
                                                value: "<bean:write name="lab" property="outlineColor"/>"
                                            },
                                            <% if(DataManager.getMSVersionMajor()>5 || 
                                                    (DataManager.getMSVersionMajor()==5 && DataManager.getMSVersionMinor()>=2)) {%>
                                             <logic:empty name="lab" property="outlineWidth">
                                                 <jsp:setProperty name="lab" property="outlineWidth" value="1" />
                                             </logic:empty>
                                            new Ext.form.NumberField({
                                                fieldLabel: "<bean:message key="outline_width"/>",
                                                name: 'labelOutlineWidth',
                                                value: "<bean:write name="lab" property="outlineWidth"/>",
                                                allowDecimals: false,
                                                allowNegative: false,
                                                decimalPrecision: 0,
                                                minValue: 1,
                                                maxValue: 7
                                            }),
                                            <% } %>
                                            <% if(DataManager.getMSVersionMajor()>5 || 
                                                    (DataManager.getMSVersionMajor()==5 && DataManager.getMSVersionMinor()>=3)) {%>
                                             <logic:empty name="lab" property="align">
                                                 <jsp:setProperty name="lab" property="align" value="<%= Label.LEFT %>" />
                                             </logic:empty>
                                             <logic:empty name="lab" property="maxLength">
                                                 <jsp:setProperty name="lab" property="maxLength" value="10" />
                                             </logic:empty>
                                            <% } %>
                                            {
                                                fieldLabel: "<bean:message key="bg_color"/>",
                                                name: 'labelBackgroundColor',
                                                listeners: {
                                                    'render': function(f,e) {checkRgb(f,e);},
                                                    'blur': function(f,e) {checkRgb(f,e);}
                                                },
                                                value: "<bean:write name="lab" property="backgroundColor"/>"
                                            },
                                            {
                                                fieldLabel: "<bean:message key="angle"/> (<bean:message key="angle_msg"/>)",
                                                name: 'label.angle',
                                                value: "<bean:write name="mapfile" property="angle" ignore="true"/>"
                                            },
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="type"/>",
                                                hiddenName: 'label.type',
                                                listeners: {
                                                    'select': function(f,e) {fontTypeChanged(f,e);}
                                                },
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : types
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="lab" property="type"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="font"/>",
                                                name: 'label.font',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['name'],
                                                    data : fonts
                                                }),
                                                displayField:'name',
                                                valueField:'name',
                                                value: "<bean:write name="lab" property="font"/>",
                                                disabled: '<bean:write name="lab" property="type"/>' != '<%=String.valueOf(Label.TRUETYPE)%>',
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="size"/> (Bitmap)",
                                                hiddenName: 'bitmapSize',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : bmpsizes
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="lab" property="size"/>",
                                                disabled: '<bean:write name="lab" property="type"/>' != '<%=String.valueOf(Label.BITMAP)%>',
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "<bean:message key="size"/> (TrueType)",
                                                name: 'truetypeSize',
                                                value: "<bean:write name="lab" property="size"/>",
                                                disabled: '<bean:write name="lab" property="type"/>' != '<%=String.valueOf(Label.TRUETYPE)%>',
                                                allowDecimals: false,
                                                allowNegative: false,
                                                decimalPrecision: 0,
                                                minValue: 4,
                                                maxValue: 256
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="position"/>",
                                                hiddenName: 'label.position',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['type', 'name'],
                                                    data : positions
                                                }),
                                                displayField:'name',
                                                valueField:'type',
                                                value: "<bean:write name="lab" property="position"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="rotation_item"/>",
                                                name: 'labelAngleItem',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['name'],
                                                    data : flds
                                                }),
                                                displayField:'name',
                                                valueField:'name',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelAngleItem"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.ComboBox({
                                                fieldLabel: "<bean:message key="size_item"/>",
                                                name: 'labelSizeItem',
                                                store: new Ext.data.SimpleStore({
                                                    fields: ['name'],
                                                    data : flds
                                                }),
                                                displayField:'name',
                                                valueField:'name',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelSizeItem"/>",
                                                typeAhead: true,
                                                autoWidth: true,
                                                mode: 'local',
                                                triggerAction: 'all',
                                                forceSelection: false,
                                                selectOnFocus:true,
                                                listClass: 'x-combo-list-small'
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "<bean:message key="min_display_scale"/>",
                                                name: 'labelMinScale',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelMinScale"/>",
                                                decimalPrecision: 0,
                                                allowDecimals : false,
                                                allowNegative : false
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "<bean:message key="max_display_scale"/>",
                                                name: 'labelMaxScale',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.labelMaxScale"/>",
                                                decimalPrecision: 0,
                                                allowDecimals : false,
                                                allowNegative : false
                                            }),
                                            new Ext.form.NumberField({
                                                fieldLabel: "<bean:message key="scale_labels"/>",
                                                name: 'symbolScale',
                                                value: "<bean:write name="<%=ObjectKeys.CURRENT_GC%>" property="defaultMsLayer.symbolScale"/>",
                                                decimalPrecision: 0,
                                                allowDecimals : false,
                                                allowNegative : false
                                            }),
                                            new Ext.form.Field({
                                                fieldLabel: "<bean:message key="wrap_character"/>",
                                                name: 'labelWrap',
                                                value: '<bean:write name="lab" property="wrap"/>',
                                                size: 1
                                            })
                                            
                                        ]
                                    }]

                });
                
        Ext.getCmp('contentProps').add(labelProps);
        Ext.getCmp('contentProps').doLayout();

        function sub() {
            
            var ok = checkForm();
            
            if (Ext.getCmp('labelProps').form.isValid() && ok) {
                Ext.Ajax.request({
                url:'labelProperties.do',
                waitMsg:'Loading',
                params: Ext.Ajax.serializeForm(Ext.getCmp('labelProps').form.id),
                callback: function(){
                        refreshComposerMap();
                    }
                });
                return true;
            }
        }

        //-->
</script>
