<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Map" %>
<%@page import="org.geogurus.mapserver.objects.Legend" %>

<bean:define id="leg" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.legend"/>
        
<script type="text/javascript">
    <!--
    var interlaces = [
        ['<%=String.valueOf(Legend.ON)%>','ON'],
        ['<%=String.valueOf(Legend.OFF)%>','OFF']
    ];
    var booleans = [
        ['true','True'],
        ['false','False']
    ];
    var status = [
        ['<%=String.valueOf(Legend.ON)%>','ON'],
        ['<%=String.valueOf(Legend.OFF)%>','OFF'],
        ['<%=String.valueOf(Legend.EMBED)%>','EMBED']
    ];
    var positions = [
        ['<%=String.valueOf(Legend.UL)%>','UL'],
        ['<%=String.valueOf(Legend.UC)%>','UC'],
        ['<%=String.valueOf(Legend.UR)%>','UR'],
        ['<%=String.valueOf(Legend.LL)%>','LL'],
        ['<%=String.valueOf(Legend.LC)%>','LC'],
        ['<%=String.valueOf(Legend.LR)%>','LL']
    ];

    var tp = "";
    <logic:notEmpty name="leg" property="template">
    tp = "<bean:write name="leg" property="template.path"/>";
    </logic:notEmpty>
   
   //Builds FormPanel to inject in opened Window
    if(Ext.getCmp('formProps')) Ext.getCmp('formProps').destroy();
    
    var formProps = new Ext.FormPanel({
                    id: 'formProps',
                    labelWidth: 150,
                    frame:true,
                    width: '100%',
                    autoScroll:true,
                    border: false,
                    items: [{
                                xtype: 'fieldset',
                                title: "Param&egrave;tres de l'objet Legend: ",
                                autoHeight:true,
                                autoWidth:true,
                                defaultType: 'textfield',
                                defaults: {msgTarget: 'side', grow:true},
                                items: [
                                    {
                                        fieldLabel: "Image Color",
                                        name: 'imgColor',
                                        listeners: {
                                            'render': function(f,e) {checkRgb(f,e);},
                                            'blur': function(f,e) {checkRgb(f,e);}
                                        },
                                        value: "<bean:write name="leg" property="imageColor"/>"
                                    },
                                    new Ext.form.ComboBox({
                                        fieldLabel: "Interlace",
                                        hiddenName: 'interlace',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['type', 'name'],
                                            data : interlaces
                                        }),
                                        displayField:'name',
                                        valueField:'type',
                                        value: "<bean:write name="leg" property="interlace"/>",
                                        typeAhead: true,
                                        autoWidth: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                                    }),
                                    {
                                        fieldLabel: "Outline Color",
                                        name: 'outlnColor',
                                        listeners: {
                                            'render': function(f,e) {checkRgb(f,e);},
                                            'blur': function(f,e) {checkRgb(f,e);}
                                        },
                                        value: "<bean:write name="leg" property="outlineColor"/>"
                                    },
                                    new Ext.form.ComboBox({
                                        fieldLabel: "Position",
                                        hiddenName: 'position',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['type', 'name'],
                                            data : positions
                                        }),
                                        displayField:'name',
                                        valueField:'type',
                                        value: "<bean:write name="leg" property="position"/>",
                                        typeAhead: true,
                                        autoWidth: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                                    }),
                                    new Ext.form.NumberField({
                                        fieldLabel: "Key Size Width",
                                        name: 'keySize.width',
                                        value: "<bean:write name="leg" property="keySize.width"/>",
                                        decimalPrecision: 0,
                                        allowDecimals : false,
                                        allowNegative : false
                                    }),
                                    new Ext.form.NumberField({
                                        fieldLabel: "Key Size Height",
                                        name: 'keySize.height',
                                        value: "<bean:write name="leg" property="keySize.height"/>",
                                        decimalPrecision: 0,
                                        allowDecimals : false,
                                        allowNegative : false
                                    }),
                                    new Ext.form.NumberField({
                                        fieldLabel: "Key Spacing Width",
                                        name: 'keySpacing.width',
                                        value: "<bean:write name="leg" property="keySpacing.width"/>",
                                        decimalPrecision: 0,
                                        allowDecimals : false,
                                        allowNegative : false
                                    }),
                                    new Ext.form.NumberField({
                                        fieldLabel: "Key Spacing Height",
                                        name: 'keySpacing.height',
                                        value: "<bean:write name="leg" property="keySpacing.height"/>",
                                        decimalPrecision: 0,
                                        allowDecimals : false,
                                        allowNegative : false
                                    }),
                                    new Ext.form.ComboBox({
                                        fieldLabel: "PostLabelCache",
                                        hiddenName: 'postLabelCache',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['type', 'name'],
                                            data : booleans
                                        }),
                                        displayField:'name',
                                        valueField:'type',
                                        value: "<bean:write name="leg" property="postLabelCache"/>",
                                        typeAhead: true,
                                        autoWidth: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                                    }),
                                    {
                                        fieldLabel: "Template",
                                        name: 'template.path',
                                        value: tp
                                    },
                                    new Ext.form.ComboBox({
                                        fieldLabel: "Status",
                                        hiddenName: 'status',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['type', 'name'],
                                            data : status
                                        }),
                                        displayField:'name',
                                        valueField:'type',
                                        value: "<bean:write name="leg" property="status"/>",
                                        typeAhead: true,
                                        autoWidth: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                                    }),
                                    new Ext.form.ComboBox({
                                        fieldLabel: "Transparence",
                                        hiddenName: 'transparence',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['type', 'name'],
                                            data : interlaces
                                        }),
                                        displayField:'name',
                                        valueField:'type',
                                        value: "<bean:write name="leg" property="transparence"/>",
                                        typeAhead: true,
                                        autoWidth: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        listClass: 'x-combo-list-small'
                                    })
                                ]
                            }]

        });

    Ext.getCmp('contentProps').add(formProps);
    Ext.getCmp('contentProps').doLayout();

    function sub() {
        if (Ext.getCmp('formProps').form.isValid()) {
            Ext.Ajax.request({
            url:'legendProperties.do',
            waitMsg:'Loading',
            params: Ext.Ajax.serializeForm(Ext.getCmp('formProps').form.id)/*,
            callback: function(){
                     No need to redraw as legend is not displayed
                    GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});
                    
                }*/
            });
            return true;
        }
    }

//-->
</script>