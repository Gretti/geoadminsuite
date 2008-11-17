/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var defaultParams = {
    'landscape' : {
        'btn-map'     :{'bgcolor':'#47882D','width':646,'height':400,'dx':15, 'dy':50,  'handles':'all', 'preserveRatio':true},
        'btn-title'   :{'bgcolor':'#2D8847','width':250,'height':30, 'dx':220,'dy':10,  'handles':'e w', 'preserveRatio':false},
        'btn-comment' :{'bgcolor':'#666666','width':150,'height':45, 'dx':15, 'dy':470, 'handles':'e w', 'preserveRatio':false},
        'btn-image'   :{'bgcolor':'#2D4788','width':40, 'height':40, 'dx':15, 'dy':10,  'handles':'all', 'preserveRatio':false},
        'btn-scale'   :{'bgcolor':'#882D47','width':100,'height':10, 'dx':15, 'dy':450, 'handles':'all', 'preserveRatio':false},
        'btn-north'   :{'bgcolor':'#88472D','width':50, 'height':50, 'dx':575,'dy':400, 'handles':'all', 'preserveRatio':true},
        'btn-overview':{'bgcolor':'#47882D','width':161,'height':100,'dx':525,'dy':450, 'handles':'all', 'preserveRatio':true},
        'btn-legend'  :{'bgcolor':'#472D88','width':100,'height':150,'dx':525,'dy':50,  'handles':'all', 'preserveRatio':false}
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
function getCmbsFonts (cmbid,cmbname,cmblabel){
    if(cmbid == null) cmbid='printTplCmbFonts';
    if(cmbname == null) cmbname='cmbFont';
    if(cmblabel == null) cmblabel='Font';
    return [
        new Ext.form.ComboBox({
            id             : cmbid,
            fieldLabel     : cmblabel,
            'name'         : cmbname,
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
        }),
        new Ext.form.ComboBox({
            id             : cmbid + 'Decos',
            fieldLabel     : cmblabel + ' style',
            'name'         : cmbname + 'decos',
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
        })
    ];
}
var cmbPrintUnits = new Ext.form.ComboBox({
    id             : 'printTplCmbPrintUnits',
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
});
var cmbScaleTypes = new Ext.form.ComboBox({
    id             : 'printTplCmbScaleTypes',
    fieldLabel     : 'Type',
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
});

var formconfigs = {
    'btn-map'     :[
        {name: 'map',value:'map',xtype:'textfield',fieldLabel:'PDF layer name)'}
    ],
    'btn-title'   :[
        getCmbsFonts('printTplCmbTitleFonts','titleFonts','Font')[0],
        getCmbsFonts('printTplCmbTitleFonts','titleFonts','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {name: 'fontColor',value:'#000000',xtype:'textfield',fieldLabel:'Font color'},
        {name: 'backgroundColor',value:'#FFFFFF',xtype:'textfield',fieldLabel:'Background'}
    ],
    'btn-comment' :[
        getCmbsFonts('printTplCmbCommentFonts','commentFonts','Font')[0],
        getCmbsFonts('printTplCmbCommentFonts','commentFonts','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {name: 'fontColor',value:'#000000',xtype:'textfield',fieldLabel:'Font color'},
        {name: 'backgroundColor',value:'#FFFFFF',xtype:'textfield',fieldLabel:'Background'}
    ],
    'btn-image'   :[
        {name: 'backgroundColor',value:'#FFFFFF',xtype:'textfield',fieldLabel:'Background'}
    ],
    'btn-scale'   :[
        cmbScaleTypes,
        getCmbsFonts('printTplCmbScaleFonts','scaleFonts','Font')[0],
        getCmbsFonts('printTplCmbScaleFonts','scaleFonts','Font')[1],
        {name:'fontSize',value: 12, fieldLabel:'Font size'},
        {name:'scaleIntervals',value: 3, fieldLabel:'Intervals'},
        new Ext.form.Checkbox({
            id        : 'printTplChkSubInt',
            boxLabel  : 'Sub intervals',
            checked   : false
        }),
        cmbPrintUnits,
        {name:'barSize',value: 5, fieldLabel:'Bar size'},
        {name:'lineWidth',value: 1, fieldLabel:'LineWidth'},
        {barDirection: 'up'},
        {textDirection: 'up'},
        {name:'labelDistance',value: 3, fieldLabel:'LineWidth'},
        {name: 'fontColor',value:'#000000',xtype:'textfield',fieldLabel:'Font color'},
        {name: 'color',value:'#000000',xtype:'textfield',fieldLabel:'Color'},
        {name: 'barBgColor',value:'',xtype:'textfield',fieldLabel:'Bar Background'}
    ],
    'btn-north'   :[
        {name: 'backgroundColor',value:'#FFFFFF',xtype:'textfield',fieldLabel:'Background'},
        {name: 'Image',value:'default',xtype:'textfield',fieldLabel:'Image'}
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
        {name: 'backgroundColor',value:'#FFFFFF',xtype:'textfield',fieldLabel:'Background'},
        getCmbsFonts('printTplCmbLayerFonts','layerFonts','Layer font')[0],
        getCmbsFonts('printTplCmbLayerFonts','layerFonts','Layer font')[1],
        {name:'layerFontSize',value: 12, fieldLabel:'Layer font size'},
        getCmbsFonts('printTplCmbClassFonts','classFonts','Class font')[0],
        getCmbsFonts('printTplCmbClassFonts','classFonts','Class font')[1],
        {name:'classFontSize',value: 8, fieldLabel:'Class font size'},
    ]
};

PrintTemplateMgr = 
    function(){
    return {
        printWin     : null,
        jsonOutput   : null,
        currentComp  : null,
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
            
            json.components[curid] = {
                'dX'    : Math.round((left - refLeft)*100/refWidth)/100,
                'dY'    : Math.round((refBottom - top)*100/refHeight)/100,
                'width' : Math.round(width * 100 / refWidth)/100,
                'height': Math.round(height * 100 /refHeight)/100,
                'url'   : url
            };
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
        showConfig:function(){
            var btn = PrintTemplateMgr.currentComp;
            var h = 400;
            var w = 300;
            var form = new Ext.form.FormPanel({
                baseCls    : 'x-plain',
                labelWidth : 100,
                defaultType: 'numberfield',
                items      : formconfigs[btn.id]
            });

            var window = new Ext.Window({
                id         : 'winCompConfig',
                title      : btn.tooltip,
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
                        handler: function(){Ext.getCmp('winCompConfig').close();}
                    },{
                        text: 'Cancel',
                        handler: function(){Ext.getCmp('winCompConfig').close();}
                    }]
            });

            window.show();
        },
        createComponent:function(btn,orientation,text,nimg) {
            PrintTemplateMgr.currentComp = btn;
            var relWidth, relHeight;
            var refEl = Ext.get('printTplLayout');
            var refX = refEl.getLeft();
            var refY = refEl.getTop();
            if(nimg!=null) {
                var inputimg = new Image();
                inputimg.src = text;
            }
            var params = defaultParams[orientation][btn.id];
            
            relWidth = params.width;
            relHeight = params.height;
            refEl.createChild('<div id="printTpl' +
                btn.tooltip + (nimg == null ? '' : nimg) + 
                '" class="printTplbasic" style="background-color:white">' + 
                '<span class="bcompconf" onclick="javascript:PrintTemplateMgr.showConfig();">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;' +
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
            elt.el.moveTo(refX + params.dx, refY + params.dy,true);
            PrintTemplateMgr.updateJsonComponent(Ext.get('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg)),refX + params.dx,refY + params.dy,text);
            if(nimg != null) {
                nimg++;
            }
            return nimg;
        }
    };

}();