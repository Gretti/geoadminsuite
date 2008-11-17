/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
            id          : 'printTplLayoutWin',
            title       : 'Layout',
            resizable   : false,
            width       : 904,
            height      : 624,
            closeAction : 'hide',
            //modal     : true,
            plain       : true,
            layout      : 'border',
            bbar        : ['->',
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
            items       : [pnlComponents, this.pnlLayout]
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
                            PrintTemplateMgr.createComponent(btn, orientation, null, null);
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
                                nimg = PrintTemplateMgr.createComponent(btn, orientation, text, nimg);
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

