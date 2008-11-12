/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
PrintTemplateMgr = 
    function(){
    return {
        printWin     : null,
        jsonOutput   : null,
        updateJson: function (el, x, y, url) {
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
            var refLeft = Ext.get('conteneur').getLeft();
            var refBottom = Ext.get('conteneur').getBottom();
            var refWidth = Ext.get('conteneur').getWidth();
            var refHeight = Ext.get('conteneur').getHeight();

            var json = PrintTemplateMgr.printWin.json;
            var left = x != null ? x : el.getLeft();
            var top = y != null ? y : el.getTop();
            var width = el.getWidth();
            var height = el.getHeight();
            url = url != null ? url : "";
            
            json.components[curid] = {
                'dX'  : Math.round((left - refLeft)*100/refWidth)/100,
                'dY'  : Math.round((refBottom - top)*100/refHeight)/100,
                'width' : Math.round(width * 100 / refWidth)/100,
                'height': Math.round(height * 100 /refHeight)/100,
                'url'   : url
            };
            if(PrintTemplateMgr.jsonOutput != null) {
                try {
                  PrintTemplateMgr.jsonOutput.setValue(Ext.util.JSON.encode(json));
                } catch(err) {
                  PrintTemplateMgr.jsonOutput.value = Ext.util.JSON.encode(json);
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
        }

    };

}();