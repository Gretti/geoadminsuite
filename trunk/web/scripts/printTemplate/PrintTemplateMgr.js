/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
        createComponent:function(btn,params,text,nimg) {
            var relWidth, relHeight;
            var refEl = Ext.get('printTplLayout');
            var refX = refEl.getLeft();
            var refY = refEl.getTop();
            if(nimg!=null) {
                var inputimg = new Image();
                inputimg.src = text;
            }
            
            relWidth = params.width;
            relHeight = params.height;
            refEl.createChild('<div id="printTpl' +
                btn.tooltip + (nimg == null ? '' : nimg) + 
                '" class="printTplbasic" style="background-color:white">' + 
                '<img src="/gas/scripts/printTemplate/images/component_conf.png" width="10px" height="10px" onclick="javascript:alert(\'configuration\')">&nbsp;&nbsp;' +
                nimg == null ? '' : '<img src="/gas/scripts/printTemplate/images/component_conf.png" width="10px" height="10px" onclick="javascript:alert(\'suppression\')">&nbsp;&nbsp;' +
                '&nbsp;&nbsp;' +
                btn.tooltip +
                '</div>');
            
            if(nimg != null) {
                new Ext.ToolTip({
                    target: 'printTpl'+ btn.tooltip + nimg,
                    html: '<img src="' + text + '">',
                    title: 'Image',
                    dismissDelay: 5000
                });
            }

            /*new Ext.Button({
                iconCls  : 'compconf',
                renderTo : Ext.get('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg)),
                handler  : function(){
                    alert('configuration');
                }
            });*/
            if(nimg != null) {
                new Ext.Button({
                    iconCls  : 'compdel',
                    renderTo : Ext.get('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg)),
                    handler  : function(){
                        alert('suppression');
                    }
                });
            }
            var elt = new Ext.Resizable('printTpl'+ btn.tooltip + (nimg == null ? '' : nimg), {
                width         : relWidth,
                height        : relHeight,
                minWidth      : 10,
                minHeight     : 10,
                dynamic       : true,
                preserveRatio : params.preserveRatio,
                constrainTo   : Ext.get('printTplContainer'),
                draggable     : true,
                listeners     :{
                    'resize':function(){
                        PrintTemplateMgr.updateJsonComponent(this.getEl());
                        this.proxy.setBox(Ext.get('printTplContainer').getBox());
                        this.proxy.update();
                    }
                }
            });
            
            elt.dd.constrainTo(Ext.get('printTplContainer'));
            elt.dd.endDrag = function(){
                PrintTemplateMgr.updateJsonComponent(Ext.get(this.id));
                elt.dd.constrainTo(Ext.get('printTplContainer'));
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