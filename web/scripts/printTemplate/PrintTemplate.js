/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
var defaultParams = {
    'btn-map'    :{'bgcolor':'#47882D','width':600,'height':400,'dx':25, 'dy':50,  'handles':'all'},
    'btn-title'  :{'bgcolor':'#2D8847','width':250,'height':30, 'dx':220,'dy':10,  'handles':'e w'},
    'btn-comment':{'bgcolor':'#666666','width':150,'height':45, 'dx':25, 'dy':470, 'handles':'e w'},
    'btn-image'  :{'bgcolor':'#2D4788','width':40, 'height':40, 'dx':25, 'dy':10,  'handles':'all'},
    'btn-scale'  :{'bgcolor':'#882D47','width':100,'height':10, 'dx':25, 'dy':450, 'handles':'all'},
    'btn-north'  :{'bgcolor':'#88472D','width':50, 'height':50, 'dx':575,'dy':400, 'handles':'all'},
    'btn-legend' :{'bgcolor':'#472D88','width':100,'height':150,'dx':525,'dy':50,  'handles':'all'}
};
var nimg = 0;

PrintTemplate = Ext.extend(Ext.Component, {
    format: 'A3',
    components: [
        {'id':'btn-map','text':'Map','width':'100','height':'100','cls':'bmap'},
        {'id':'btn-title','text':'Title','width':'100','height':'100','cls':'btitle'},
        {'id':'btn-comment','text':'Comment','width':'100','height':'100','cls':'bcomment'},
        {'id':'btn-image','text':'Image','width':'100','height':'100','cls':'bimage'},
        {'id':'btn-scale','text':'Scale','width':'100','height':'100','cls':'bscale'},
        {'id':'btn-north','text':'North','width':'100','height':'100','cls':'bnorth'},
        {'id':'btn-legend','text':'Legend','width':'100','height':'100','cls':'blegend'}
    ],
    win: null,
    pnlLayout: null,
    json:null,
    create: function() {
        this.json = Ext.util.JSON.decode('{layout: "A3",orientation : "landscape",components :{}}'),
        this.pnlLayout = new Ext.Panel({
            id             : 'printTplLayout',
            region         : 'center',
            bodyStyle      : {align:'center'},
            html           : '<div id="conteneur"><!--spacer--></div>'
        });
        
        // Panel for the components (west)
        var itemsComponents = this.initComponents();
        var pnlComponents = new Ext.Panel({
            id             : 'printTplComponents',
            region         : 'west',
            split          : true,
            width          : 50,
            minSize        : 50,
            maxSize        : 50,
            collapsible    : false,
            margins        : '3 0 3 3',
            cmargins       : '3 3 3 3',
            items          : itemsComponents
        }); 

        this.win = new Ext.Window({
            id       : 'printTplLayoutWin',
            title    : 'Layout',
            resizable: false,
            width    : 750,
            height   : 600,
            //modal    : true,
            //border : false,
            plain    : true,
            layout   : 'border',
            bbar     : ['->',
                {
                    text    : 'Close',
                    handler : function(){
                        PrintTemplateMgr.printWin.json = null;
                        PrintTemplateMgr.printWin.destroy();
                    }
                },{
                    text    : 'Apply',
                    handler : function(){
                        PrintTemplateMgr.printWin.destroy();
                    }
                }
            ],
            items    : [pnlComponents, this.pnlLayout]
        });

    },
    initComponents: function() {
        var buttons = [];
        for (var i = 0; i < this.components.length; i++) {
            if(this.components[i].id != 'btn-image') {
                buttons.push(new Ext.Button({
                    id            : this.components[i].id,
                    tooltip       : this.components[i].text,
                    iconCls       : this.components[i].cls,
                    enableToggle : true,
                    toggleHandler   : function(btn, state){
                        if(!state) {
                            Ext.get('printTpl'+ btn.tooltip).remove();
                            PrintTemplateMgr.removeFromJson('printTpl'+ btn.tooltip);
                        } else {
                            Ext.get('conteneur').createChild('<div id="printTpl'+ btn.tooltip + '" class="printTplbasic" style="background-color:white">' + btn.tooltip + '</div>');
                            var refX = Ext.get('conteneur').getLeft();
                            var refY = Ext.get('conteneur').getTop();
                            var elt = new Ext.Resizable('printTpl'+ btn.tooltip, {
                                minWidth      : 10,
                                minHeight     : 10,
                                width         : defaultParams[btn.id].width,
                                height        : defaultParams[btn.id].height,
                                dynamic       : true,
                                constrainTo   : Ext.get('printTplLayoutWin'),
                                handles       : defaultParams[btn.id].handles,
                                draggable     : true,
                                listeners     :{
                                    'resize':function(){
                                        PrintTemplateMgr.updateJson(this.getEl());
                                        this.proxy.setBox(Ext.get('conteneur').getBox());
                                        this.proxy.update();
                                    }
                                }
                            });
                            elt.dd.constrainTo(Ext.get('conteneur'));
                            elt.dd.endDrag = function(){
                                PrintTemplateMgr.updateJson(Ext.get(this.id), null, null);
                                elt.dd.constrainTo(Ext.get('conteneur'));
                            };
                            elt.el.moveTo(refX + defaultParams[btn.id].dx, refY + defaultParams[btn.id].dy,true);
                            PrintTemplateMgr.updateJson(elt.el,refX + defaultParams[btn.id].dx,refY + defaultParams[btn.id].dy);
                        }
                    }
                }));
            } else {
                buttons.push(new Ext.Button({
                    id            : this.components[i].id,
                    tooltip       : this.components[i].text,
                    iconCls       : this.components[i].cls,
                    handler   : function(btn){
                        Ext.Msg.prompt('Image URL', 'Please enter url for image:', function(bouton, text){
                            if (bouton == 'ok'){
                                var inputimg = Ext.get('conteneur').createChild('<img id="printTpl'+ btn.tooltip + nimg + '" src="' + text + '"/>');
                                var relWidth = defaultParams[btn.id].width;
                                var relHeight = defaultParams[btn.id].width*(inputimg.dom.naturalHeight/inputimg.dom.naturalWidth);
                                console.log(inputimg);
                                
                                var elt = new Ext.Resizable('printTpl'+ btn.tooltip + nimg, {
                                    wrap          : true,
                                    width         : relWidth,
                                    height        : relHeight,
                                    minWidth      : 10,
                                    minHeight     : 10,
                                    dynamic       : true,
                                    constrainTo   : Ext.get('conteneur'),
                                    draggable     : true,
                                    listeners     :{
                                        'resize':function(){
                                            PrintTemplateMgr.updateJson(Ext.get(this.getEl()));
                                            this.proxy.setBox(Ext.get('conteneur').getBox());
                                            this.proxy.update();
                                        }
                                    }
                                });
                                var refX = Ext.get('conteneur').getLeft();
                                var refY = Ext.get('conteneur').getTop();
                                elt.dd.constrainTo(Ext.get('conteneur'));
                                elt.dd.endDrag = function(){
                                    PrintTemplateMgr.updateJson(Ext.get(this.id), null, null);
                                    elt.dd.constrainTo(Ext.get('conteneur'));
                                };
                                elt.el.moveTo(refX + defaultParams[btn.id].dx, refY + defaultParams[btn.id].dy,true);
                                PrintTemplateMgr.updateJson(Ext.get('printTpl'+ btn.tooltip + nimg),refX + defaultParams[btn.id].dx,refY + defaultParams[btn.id].dy,text);
                                nimg++;
                            }
                        });
                    }
                }));
            }
        }
        return buttons;
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
        this.win.destroy();
    }
});

