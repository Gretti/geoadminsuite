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
                '<span class="bcompconf" onclick="javascript:alert(\'configuration\')">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;' +
                (nimg == null ? '' : '<span class="bcompdel" onclick="javascript:PrintTemplateMgr.removeFromJson(\'printTpl' + btn.tooltip + nimg + '\')">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;') +
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