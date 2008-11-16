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

var formconfigs = {
    'btn-map'     :[],
    'btn-title'   :[],
    'btn-comment' :[],
    'btn-image'   :[],
    'btn-scale'   :[],
    'btn-north'   :[],
    'btn-overview':[],
    'btn-legend'  :[]
};

var nimg = 0;
//sizes are calculated with an overhead of 
//  - 104px for width (left panel + borders)
//  - 58px  for height (borders only)
var orientationSizes = {
    'landscape':{width:904, height:624},
    'page'     :{width:599, height:758}
};

PrintTemplate = Ext.extend(Ext.Component, {
    format: 'A3',
    components: [
        {'id':'btn-map',     'text':'Map',     'width':'100','height':'100','cls':'bmap'},
        {'id':'btn-title',   'text':'Title',   'width':'100','height':'100','cls':'btitle'},
        {'id':'btn-comment', 'text':'Comment', 'width':'100','height':'100','cls':'bcomment'},
        {'id':'btn-image',   'text':'Image',   'width':'100','height':'100','cls':'bimage'},
        {'id':'btn-scale',   'text':'Scale',   'width':'100','height':'100','cls':'bscale'},
        {'id':'btn-north',   'text':'North',   'width':'100','height':'100','cls':'bnorth'},
        {'id':'btn-overview','text':'Overview','width':'100','height':'100','cls':'bmap'},
        {'id':'btn-legend',  'text':'Legend',  'width':'100','height':'100','cls':'blegend'}
    ],
    win: null,
    pnlLayout: null,
    json:null,
    create: function() {
        this.json = Ext.util.JSON.decode('{layout: "A3",orientation : "landscape",components :{}}'),
        this.pnlLayout = new Ext.Panel({
            id             : 'printTplLayout',
            region         : 'center',
            bodyStyle      : {'background-color':'#CCC'},
            html           : '<div id="printTplContainer"><!--spacer--></div>'
        });
        
        // Panel for the components (west)
        var itemsComponents = this.initComponents();
        var pnlComponents = new Ext.Panel({
            id             : 'printTplComponents',
            region         : 'west',
            split          : true,
            width          : 80,
            minSize        : 80,
            maxSize        : 80,
            collapsible    : false,
            margins        : '3 0 3 3',
            cmargins       : '3 3 3 3',
            items          : itemsComponents
        }); 

        this.win = new Ext.Window({
            id       : 'printTplLayoutWin',
            title    : 'Layout',
            resizable: false,
            width    : 904,
            height   : 624,
            //modal    : true,
            plain    : true,
            layout   : 'border',
            bbar     : ['->',
                {
                    text    : 'Reset',
                    cls     : 'x-btn-text-icon',
                    iconCls : 'breset',
                    handler : function(){
                        PrintTemplateMgr.printWin.destroy();
                    }
                },{
                    text    : 'Apply',
                    cls     : 'x-btn-text-icon',
                    iconCls : 'bapply',
                    handler : function(){
                        PrintTemplateMgr.printWin.hide();
                    }
                }
            ],
            items    : [pnlComponents, this.pnlLayout]
        });

    },
    initComponents: function() {
        var comps = [];
        //Format combobox and orientation chekbox
        var formatsStore = new Ext.data.SimpleStore({
            fields: ['name','value'],
            data  : [['A4','A4'],['A3','A3'],['A2','A2'],['A1','A1'],['A0','A0']]
        });
        comps.push(new Ext.form.ComboBox({
            id             : 'printTplCmbFormat',
            width          : 70,
            store          : formatsStore,
            displayField   : 'name',
            typeAhead      : true,
            mode           : 'local',
            triggerAction  : 'all',
            selectOnFocus  : true,
            forceSelection : true,
            value          : 'A3',
            listeners      : {
                'select' : function(){
                    PrintTemplateMgr.updateJsonFormat(this.value);
                }
            }
        }));
        comps.push(new Ext.form.Checkbox({
            id        : 'printTplChkLandscape',
            boxLabel  : 'landscape<br><hr>',
            checked   : true,
            listeners : {
                'check' : function(){
                    var orientation = this.checked ? 'landscape' : 'page';
                    //Resize window to reflect orientation
                    Ext.getCmp('printTplLayoutWin').setWidth(orientationSizes[orientation].width);
                    Ext.getCmp('printTplLayoutWin').setHeight(orientationSizes[orientation].height);
                    //TODO : Should resize and move all elements that fall outside the print area
                    PrintTemplateMgr.updateJsonOrientation(orientation);
                }
            }
        }));

        //Buttons
        for (var i = 0; i < this.components.length; i++) {
            if(this.components[i].id != 'btn-image') {
                comps.push(new Ext.Button({
                    id            : this.components[i].id,
                    tooltip       : this.components[i].text,
                    iconCls       : this.components[i].cls,
                    enableToggle  : true,
                    toggleHandler   : function(btn, state){
                        if(!state) {
                            PrintTemplateMgr.removeFromJson('printTpl'+ btn.tooltip);
                        } else {
                            var orientation = Ext.getCmp('printTplChkLandscape').checked ? 'landscape' : 'page';
                            PrintTemplateMgr.createComponent(btn, defaultParams[orientation][btn.id], null, null);
                        }
                    }
                }));
            } else {
                comps.push(new Ext.Button({
                    id            : this.components[i].id,
                    tooltip       : this.components[i].text,
                    iconCls       : this.components[i].cls,
                    handler   : function(btn){
                        Ext.Msg.prompt('Image URL', 'Please enter url for image:', function(bouton, text){
                            if (bouton == 'ok'){
                                var orientation = Ext.getCmp('printTplChkLandscape').checked ? 'landscape' : 'page';
                                nimg = PrintTemplateMgr.createComponent(btn, defaultParams[orientation][btn.id], text, nimg);
                            }
                        });
                    }
                }));
            }
        }
        return comps;
    },
    getPrintTplJson: function() {
        return this.json;
    },
    show: function(el) {
        this.win.show(el);
    },
    hide: function() {
        this.win.hide();
    },
    destroy: function() {
        var items = Ext.getCmp('printTplComponents').items.items;
        for(var b=0; b < items.length; b++) {
            if(items[b].pressed) {
                items[b].toggle(false);
            }
        }
        while(Ext.get('printTplContainer').first() != null) {
            Ext.get('printTplContainer').first().remove();
        }
                
        PrintTemplateMgr.resetJson();
        this.win.hide();
    }
});

