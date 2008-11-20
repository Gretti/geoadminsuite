/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var defaultParams = {
    'landscape' : {
        'btn-map'     :{
            'width':'90%',
            'height':'w*0.58',
            'dx':'5%',
            'dy':'middle',
            'handles':'all',
            'preserveRatio':true
        },
        'btn-title'   :{
            'width':250,
            'height':30,
            'dx':'center',
            'dy':'5%',
            'handles':'e w',
            'preserveRatio':false
        },
        'btn-comment' :{
            'width':150,
            'height':45,
            'dx':'5%',
            'dy':'bottom',
            'handles':'e w',
            'preserveRatio':false
        },
        'btn-image'   :{
            'width':40,
            'height':40,
            'dx':'5%',
            'dy':'top',
            'handles':'all',
            'preserveRatio':false
        },
        'btn-scale'   :{
            'width':100,
            'height':10,
            'dx':'5%',
            'dy':'ref-Map-under',
            'handles':'all',
            'preserveRatio':false
        },
        'btn-north'   :{
            'width':50,
            'height':50,
            'dx':'ref-Map-right',
            'dy':'ref-Map-bottom',
            'handles':'all',
            'preserveRatio':true
        },
        'btn-overview':{
            'width':'15%',
            'height':'w*0.58',
            'dx':'right',
            'dy':'bottom',
            'handles':'all',
            'preserveRatio':true
        },
        'btn-legend'  :{
            'width':100,
            'height':150,
            'dx':'ref-Map-right',
            'dy':'ref-Map-top',
            'handles':'all',
            'preserveRatio':false
        }
    },
    'page' : {
        'btn-map'     :{'bgcolor':'#47882D','width':475,'height':294,'dx':0,  'dy':150, 'handles':'all', 'preserveRatio':true},
        'btn-title'   :{'bgcolor':'#2D8847','width':250,'height':30, 'dx':120,'dy':10,  'handles':'e w', 'preserveRatio':false},
        'btn-comment' :{'bgcolor':'#666666','width':150,'height':45, 'dx':0,  'dy':470, 'handles':'e w', 'preserveRatio':false},
        'btn-image'   :{'bgcolor':'#2D4788','width':40, 'height':40, 'dx':0,  'dy':10,  'handles':'all', 'preserveRatio':false},
        'btn-scale'   :{'bgcolor':'#882D47','width':100,'height':10, 'dx':0,  'dy':444, 'handles':'all', 'preserveRatio':false},
        'btn-north'   :{'bgcolor':'#88472D','width':50, 'height':50, 'dx':425,'dy':394, 'handles':'all', 'preserveRatio':true},
        'btn-overview':{'bgcolor':'#47882D','width':161,'height':100,'dx':314,'dy':444, 'handles':'all', 'preserveRatio':true},
        'btn-legend'  :{'bgcolor':'#472D88','width':100,'height':150,'dx':375,'dy':150, 'handles':'all', 'preserveRatio':false}
    }
};
function getCmbsFonts (cmbname,cmblabel){
    return [
        {
            xtype          : 'combo',
            fieldLabel     : cmblabel,
            name         : cmbname,
            width          : 90,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['Courrier','Courrier'],
                    ['Helvetica','Helvetica'],
                    ['Times','Times'],
                    ['Symbol','Symbol'],
                    ['ZapfDingbats','ZapfDingbats']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'Helvetica'
        },{
            xtype          : 'combo',
            fieldLabel     : cmblabel + ' style',
            'name'         : cmbname + 'Style',
            width          : 90,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['None','None'],
                    ['Bold','Bold'],
                    ['Oblique','Oblique'],
                    ['BoldOblique','BoldOblique']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'None'
        }
    ];
}

//Items to push in the configuration form for components. The field's name MUST
//match real yaml keys as servlet will just wrap key: value in the yaml string.
var formconfigs = {
    'btn-map'     :[
        {name: 'name',value:'map',xtype:'textfield',fieldLabel:'PDF layer name'}
    ],
    'btn-title'   :[
        getCmbsFonts('font','Font')[0],
        getCmbsFonts('font','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {xtype:'colorfield',name: 'fontColor',fieldLabel: 'Font color', value: '#000000'},
        {xtype:'colorfield',name: 'backgroundColor',fieldLabel: 'Background', value: '#FFFFFF'}
    ],
    'btn-comment' :[
        getCmbsFonts('font','Font')[0],
        getCmbsFonts('font','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {xtype:'colorfield',name: 'fontColor',fieldLabel: 'Font color', value: '#000000'},
        {xtype:'colorfield',name: 'backgroundColor',fieldLabel: 'Background', value: '#FFFFFF'}
    ],
    'btn-image'   :[
        {xtype:'colorfield',name: 'backgroundColor',fieldLabel: 'Background', value: '#FFFFFF'}
    ],
    'btn-scale'   :[
        {
            xtype          : 'combo',
            fieldLabel     : 'Type',
            name           : 'type',
            width          : 70,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['line','line'],
                    ['bar','bar'],
                    ['bar_sub','bar_sub']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'line'
        },
        getCmbsFonts('font','Font')[0],
        getCmbsFonts('font','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {name:'scaleIntervals',value: 3, fieldLabel:'Intervals'},
        {
            xtype      : 'checkbox',
            fieldLabel: 'Sub intervals',
            checked   : false
        },
        {
            xtype          : 'combo',
            name           : 'units',
            fieldLabel     : 'Units',
            width          : 70,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['meters','m'],
                    ['feet','ft'],
                    ['degrees','degrees']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'm'
        },
        {name:'barSize',value: 5, fieldLabel:'Bar size'},
        {name:'lineWidth',value: 1, fieldLabel:'LineWidth'},
        {
            xtype          : 'combo',
            name           : 'barDirection',
            fieldLabel     : 'Bar direction',
            width          : 70,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['up','right'],
                    ['down','right'],
                    ['left','right'],
                    ['right','right']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'up'
        },
        {
            xtype          : 'combo',
            name           : 'textDirection',
            fieldLabel     : 'Text direction',
            width          : 70,
            store          : new Ext.data.SimpleStore({
                fields: ['name','value'],
                data  : [
                    ['up','right'],
                    ['down','right'],
                    ['left','right'],
                    ['right','right']
                ]}),
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'up'
        },
        {name:'labelDistance',value: 3, fieldLabel:'LineWidth'},
        {xtype:'colorfield',name: 'fontColor',fieldLabel: 'Font color', value: '#000000'},
        {xtype:'colorfield',name: 'color',fieldLabel: 'Color', value: '#000000'},
        {xtype:'colorfield',name: 'barBgColor',fieldLabel: 'Background', value: '#FFFFFF'}
    ],
    'btn-north'   :[
        {xtype:'colorfield',name: 'barckgroundColor',fieldLabel: 'Background', value: '#FFFFFF'},
        {name: 'northimage',value:'default',xtype:'textfield',fieldLabel:'Image'}
    ],
    'btn-overview':[
        {name:'overviewmap',value: 2,fieldLabel:'Scale factor'},
    ],
    'btn-legend'  :[
        {name:'maxIconWidth',value: 8, fieldLabel:'Max icon width'},
        {name:'maxIconHeight',value: 8, fieldLabel:'Max icon height'},
        {name:'classIndentation',value: 20, fieldLabel:'Class identation'},
        {name:'layerSpace',value: 5, fieldLabel:'Layer space'},
        {name:'classSpace',value: 2, fieldLabel:'Class space'},
        {xtype:'colorfield',name: 'barckgroundColor',fieldLabel: 'Background', value: '#FFFFFF'},
        getCmbsFonts('layerFont','Layer font')[0],
        {name:'layerFontSize',value: 12, fieldLabel:'Layer font size'},
        getCmbsFonts('classFont','Class font')[0],
        {name:'classFontSize',value: 8, fieldLabel:'Class font size'},
    ]
};

PrintTemplateMgr =
    function(){
    return {
        printWin     : null,
        jsonOutput   : null,
        updateJsonFormat: function (format) {
            var json = this.printWin.json;
            json.layout = format;
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    this.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                    this.jsonOutput.value = Ext.util.JSON.encode(json);
                }
            }
            return json;
        },
        updateJsonOrientation: function (orientation) {
            var json = this.printWin.json;
            json.orientation = orientation;
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    this.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                    this.jsonOutput.value = Ext.util.JSON.encode(json);
                }
            }
            return json;
        },
        updateJsonComponent: function (el, x, y, url) {
            var curid = el.id;
            if(curid.split("-").length == 2) {
                curid = curid.split("-")[0];
                url = document.images[curid].src;
            }
            /*
             * Variables to calculate a ratio and store in json :
             *  - X and Y are upper left corner of the element calculated regarding to
             *   bottom left corner of the page as a ratio
             *   (dX, dY / page-width and page-height)
             *  - Width and height are a strict ratio from refWidth and refHeight
             */
            var refLeft = Ext.get('printTplLayout').getLeft();
            var refBottom = Ext.get('printTplLayout').getBottom();
            var refWidth = Ext.get('printTplLayout').getWidth();
            var refHeight = Ext.get('printTplLayout').getHeight();

            var json = this.printWin.json;
            var left = x != null ? x : el.getLeft();
            var top = y != null ? y : el.getTop();
            var width = el.getWidth();
            var height = el.getHeight();
            url = url != null ? url : "";
            if(json.components[curid] == null) {
                json.components[curid] = {
                    'dX'        : Math.round((left - refLeft)*100/refWidth)/100,
                    'dY'        : Math.round((refBottom - top)*100/refHeight)/100,
                    'width'     : Math.round(width * 100 / refWidth)/100,
                    'height'    : Math.round(height * 100 /refHeight)/100,
                    'url'       : url
                };
            } else {
                json.components[curid]['dX'] = Math.round((left - refLeft)*100/refWidth)/100;
                json.components[curid]['dY'] = Math.round((refBottom - top)*100/refHeight)/100;
                json.components[curid]['width'] = Math.round(width * 100 / refWidth)/100;
                json.components[curid]['height']= Math.round(height * 100 /refHeight)/100;
                json.components[curid]['url']   = url;
            }
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    this.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                    this.jsonOutput.value = Ext.util.JSON.encode(json);
                }
            }
            return json;
        },
        updateJsonValues: function (id,values) {
            var json = this.printWin.json;
            json.components[id].attributes = values;
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    this.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                    this.jsonOutput.value = Ext.util.JSON.encode(json);
                }
            }
            return json;
        },
        removeFromJson: function (hid) {
            //TODO: There is still a strange behaviour with left components moving
            //when deleting the current may be we should reset constraints to fix it
            Ext.get(hid).fadeOut({
                endOpacity: 0,
                easing: 'easeOut',
                duration: .5,
                remove: true,
                useDisplay: false
            });
            var json = PrintTemplateMgr.printWin.json;
            delete json.components[hid];
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    PrintTemplateMgr.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                    PrintTemplateMgr.jsonOutput.value = Ext.util.JSON.encode(json);
                }
            }
            return json;
        },
        resetJson: function () {
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                    PrintTemplateMgr.jsonOutput.setValue("");
                } catch(err) {
                    PrintTemplateMgr.jsonOutput.value = "";
                }
            }
        },
        showConfig:function(btn_tooltip,btn_id,nimg){
            var h = 400;
            var w = 300;
            var form = new Ext.form.FormPanel({
                id         : 'printTplFormConfig',
                baseCls    : 'x-plain',
                labelWidth : 100,
                defaultType: 'numberfield',
                items      : formconfigs[btn_id]
            });

            var window = new Ext.Window({
                id         : 'printTplWinConfig',
                title      : btn_tooltip,
                width      : w,
                height     : h,
                layout     : 'fit',
                plain      : true,
                modal      : true,
                bodyStyle  : 'padding:5px;',
                buttonAlign:'center',
                items      : form,
                buttons    : [{
                        text: 'Apply',
                        handler: function(){
                            var cmpid = 'printTpl' + btn_tooltip + (nimg == null ? '' : nimg);
                            var vals = Ext.getCmp('printTplFormConfig').form.getValues();
                            PrintTemplateMgr.updateJsonValues(cmpid,vals);
                            Ext.getCmp('printTplFormConfig').destroy();
                            Ext.getCmp('printTplWinConfig').close();
                        }
                    },{
                        text: 'Cancel',
                        handler: function(){
                            Ext.getCmp('printTplWinConfig').close();
                        }
                    }]
            });

            window.show();
        },
        createComponent:function(btn,orientation,text,nimg) {
            var relWidth, relHeight;
            var refEl = Ext.get('printTplLayout');
            if(nimg!=null) {
                var inputimg = new Image();
                inputimg.src = text;
            }
            var params = defaultParams[orientation][btn.id];
            if(isNaN(params.width)) {
                //percentage of container width
                relWidth = Math.round(Ext.get('printTplContainer').getWidth() * parseInt(params.width.split("%")[0])/100);
            } else {
                relWidth = params.width;
            }
            if(isNaN(params.height)) {
                if(params.height.indexOf("%") > -1) {
                    //percentage of container height
                    relHeight = Math.round(Ext.get('printTplContainer').getHeight() * parseInt(params.height.split("%")[0])/100);
                }else if(params.height.indexOf("*") > -1) {
                    //ratio on width
                    relHeight = Math.round(relWidth * parseFloat(params.height.split("*")[1]));
                }
            } else {
                relHeight = params.height;
            }
            refEl.createChild('<div id="printTpl' +
                btn.tooltip + (nimg == null ? '' : nimg) +
                '" class="printTplbasic" style="background-color:white">' +
                '<span class="bcompconf" onclick="javascript:PrintTemplateMgr.showConfig(\'' + btn.tooltip + '\',\'' + btn.id + '\',' + nimg + ');">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;' +
                (nimg == null ? '' : '<span class="bcompdel" onclick="javascript:PrintTemplateMgr.removeFromJson(\'printTpl' + btn.tooltip + nimg + '\');">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;') +
                (nimg == null ? '<span>' + btn.tooltip + '</span>' : '<span class="bimginfo" id="printTpl' + btn.tooltip + nimg + '_img">&nbsp;&nbsp;&nbsp;&nbsp;</span>') + '</div>');

            if(nimg != null) {
                new Ext.ToolTip({
                    target: 'printTpl' + btn.tooltip + nimg + '_img',
                    width: 200,
                    html: '<img src="' + text + '">',
                    dismissDelay: 5000 // auto hide after 5 seconds
                });
            }

            var elt = new Ext.Resizable('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg), {
                width         : relWidth,
                height        : relHeight,
                minWidth      : 10,
                minHeight     : 10,
                dynamic       : true,
                handles       : params.handles,
                preserveRatio : params.preserveRatio,
                constrainTo   : Ext.get('printTplContainer'),
                draggable     : true,
                listeners     :{
                    'resize':function(){
                        //Must manage DD too ... Weird ...
                        this.dd.resetConstraints(true);
                        this.dd.constrainTo(Ext.get('printTplContainer'));
                        //Update json
                        PrintTemplateMgr.updateJsonComponent(this.getEl());
                    }
                }
            });

            elt.dd.constrainTo(Ext.get('printTplContainer'));
            elt.dd.endDrag = function(){
                //Restores DD constraints to container
                elt.dd.resetConstraints(true);
                elt.dd.constrainTo(Ext.get('printTplContainer'));
                PrintTemplateMgr.updateJsonComponent(Ext.get(this.id));
            };
            var dx = 0;
            var dy = 0;
            var strDx = params.dx;
            var strDy = params.dy;
            var refObj;
            //Delta x calculation
            if(isNaN(strDx)) {
                refObj = Ext.get('printTplContainer');
                var xref = refObj.getLeft();
                if(strDx.indexOf("%") > -1) {
                    //percentage of container
                    dx = Math.round(refObj.getWidth() * parseInt(strDx.split("%")[0])/100);
                } else {
                    //key string for page alignement
                    if(strDx == 'left') {
                        dx = 0;
                    } else if(strDx == 'center') {
                        dx = Math.round(refObj.getWidth()/2) - Math.round(relWidth/2);
                    } else if(strDx == 'right') {
                        dx = refObj.getWidth() - relWidth;
                    } else if(strDx.split('-').length > 1) {
                        //key strings for ref-Object-alignement
                        refObj = Ext.get('printTpl' + strDx.split('-')[1]);
                        if(refObj != null) {
                            xref = refObj.getLeft() - Ext.get('printTplContainer').getLeft();
                            if(strDx.split('-')[2] == 'left') {
                                dx = xref;
                            } else if(strDx.split('-')[2] == 'center') {
                                dx = xref + Math.round(refObj.getWidth()/2) - Math.round(relWidth.getWidth()/2);
                            } else if(strDx.split('-')[2] == 'right') {
                                dx = xref + refObj.getWidth() - relWidth;
                            }
                        }
                    }
                }
            } else {
                dx = strDx;
            }


            if(isNaN(strDy)) {
                refObj = Ext.get('printTplContainer');
                var yref = refObj.getTop();
                if(strDy.indexOf("%") > -1) {
                    //percentage of container
                    dy = Math.round(refObj.getHeight() * parseInt(strDy.split("%")[0])/100);
                } else {
                    //key string for page alignement
                    if(strDy == 'top') {
                        dy = 0;
                    } else if(strDy == 'middle') {
                        dy = Math.round(refObj.getHeight()/2) - Math.round(relHeight/2);
                    } else if(strDy == 'bottom') {
                        dy = refObj.getHeight() - relHeight;
                    } else if(strDy.split('-').length > 1) {
                        //key strings for ref-Object-alignement
                        refObj = Ext.get('printTpl' + strDy.split('-')[1]);
                        if(refObj != null) {
                            yref = refObj.getTop() - Ext.get('printTplContainer').getTop();
                            if(strDy.split('-')[2] == 'top') {
                                dy = yref;
                            } else if(strDy.split('-')[2] == 'middle') {
                                dy = yref + Math.round(refObj.getHeight()/2);
                            } else if(strDy.split('-')[2] == 'bottom') {
                                dy = yref + refObj.getHeight() - relHeight;
                            } else if(strDy.split('-')[2] == 'under') {
                                dy = yref + refObj.getHeight();
                            }
                        }
                    }
                }
            } else {
                dy = strDy;
            }
            var refX = Ext.get('printTplContainer').getLeft();
            var refY = Ext.get('printTplContainer').getTop();
            elt.el.moveTo(refX + dx, refY + dy,true);
            PrintTemplateMgr.updateJsonComponent(Ext.get('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg)),refX + dx,refY + dy,text);
            if(nimg != null) {
                nimg++;
            }
            return nimg;
        }
    };

}();