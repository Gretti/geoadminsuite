/* 
 * 05/01/2013 - Nicolas Ribot
 * 
 * Proof of concept for a local GIS data viewer based on MapServer
 * Defines MapServer JS object able to write themselves as string, in order
 * to write it to the temporary browser FileSystem.
 * 
 * TODO: complete object model
 *       inheritance, good JS practices...
 */

/**
 * MAP object
 * parameter: config:  a JSON object that may contain:
 * {    
 *  "IMAGEPATH": "path to MS writable folder for tmp images",
 * }
 *
 */
function MapObj(config) {
    /** config object */
    this.config = config;
    
    /** List of layers */
    this.layers = [];
}
    
/**
 * Adds or replace the given layer in the layers array.
 * Replacement is based on layer name
 */
MapObj.prototype.addLayer = function (lo, replace) {
    
    if (replace) {
        if (lo) {
            for (var i = 0; i < this.layers.length; i++) {
                if (this.layers[i].config.name == lo.config.name) {
                    this.layers[i] = lo;
                    return;
                }
            }
            this.layers.push(lo);
        } 
    } else {
        this.layers.push(lo);
    }
};

/**
 * removes the given layer in the layers array.
 * Replacement is based on layer name
 */
MapObj.prototype.removeLayer = function (layerName) {
    for (var i = 0; i < this.layers.length; i++) {
        if (this.layers[i].config.name == layerName) {
            this.layers.splice(i,1);
            return;
        }
    }
}

/**
 * gets the layer by name.
 */
MapObj.prototype.getLayer = function (layerName) {
    for (var i = 0; i < this.layers.length; i++) {
        if (this.layers[i].config.name == layerName) {
            return this.layers[i];
        }
    }
}

MapObj.prototype.toString = function() {
    var s = '#aspecialcomment\n';
    s += 'MAP\n';
    // How to handle datasource extent ?
    s += '  EXTENT -180 -90 180 90\n';
    s += '  SIZE 1200 900\n';
    s += '  SHAPEPATH "/Users/nicolas/geodata/10m_cultural"\n';
    s += '  WEB\n';
    s += '      IMAGEPATH "/tmp/"\n';
    s += '  END #WEB\n';
    s += '\n\n';
    for (var i = 0; i < this.layers.length; i++) {
        s += this.layers[i];
    }
    s += '\n';
    // a default symbol for points
    s += '  SYMBOL\n';
    s += '      name "circle"\n';
    s += '      type ellipse\n';
    s += '      filled true\n';
    s += '      POINTS 1 1 END #POINTS\n';
    s += '  END #SYMBOL\n';
    
    s += 'END #MAPFILE\n';
    return s;
}
/**
 * LAYER object.
 * Parameters: options: a config object that must contain:
 * {    
 *  "name": "layer name",
 *  "data":"layer's data", 
 *  "connection": "MapServer connection string, empty for file datasource",
 *  "connectionType": "MapServer Connection type, , empty for file datasource"
 *  }
 * 
 */
function LayerObj(conf) {
    if (!conf) {
        this.config = {
            "data" : "",
            "name" : "",
            "type" : "POLYGON",
            "opacity" : "50",
            "color" : "#FF0000",
            "outlineColor" : "#000000"        
        };
    } else {
        this.config = conf;
    }
}   

/**
 * Returns a mapServer string representation of this layer:
 */
LayerObj.prototype.toString = function() {
    var s = '   LAYER\n';
    s += '      NAME "' + this.config.name + '"\n';
    s += '      TYPE ' + this.config.type + '\n';
    s += '      DATA "' + this.config.data + '"\n';
    s += '      OPACITY ' + this.config.opacity + '\n';
    s += '      STATUS DEFAULT\n';

    if (this.config.connection) {
        s += '      CONNECTION "' + this.config.connection + '"\n';
    }
    if (this.config.connectionType) {
        s += '      CONNECTIONTYPE "' + this.config.connectionType + '"\n';
    }
    if (this.config.type != 'RASTER') {
        s += '      CLASS\n';
        s += '          COLOR "' + this.config.color + '"\n';
        if (this.config.type == 'POLYGON') {
            s += '          OUTLINECOLOR "' + this.config.outlineColor + '"\n';
        } else if (this.config.type == 'POINT') {
            s += '          SYMBOL "circle"\n';
            s += '          SIZE 10\n';
        }
        s += '      END #CLASS\n';
    }
    s += '  END #LAYER\n';

    return s;
};

/**
 * Returns a HTML <li> representation of this layer:
 */
LayerObj.prototype.toHTML = function() {
    
    var s = "";
    var lis = "";
    var lie = "";
    if (this.config.name) {
        lis = "<li draggable='true' id='list_" + this.config.name.replace(/\./g, '_') + "'>";
        lie = "</li>";
        s += lis;
        s += "<img src='images/delete.png' onclick='removeLayer(\"";
        s += this.config.name + "\")'/>&nbsp;" ;
    }
    s += '<i>Layer type:</i> <select id="layerTypeId" class="bold">';
    s += '<option name="empty" value="empty"   selected="true">auto</option>\n';
    s += '<option name="point" value="POINT" >POINT</option>\n';
    s += '<option name="line" value="LINE">LINE</option>\n';
    s += '<option name="polygon" value="POLYGON">POLYGON</option>\n';
    s += '<option name="raster" value="RASTER">RASTER</option>\n';
    s += '</select>\n';
    s += '&nbsp;\n';
    s += '<i>Opacity:</i><input type="number" id="layerOpacityId" value="50" class="bold" ';
    s += 'onchange="updateLayer(\'' + this.config.name + '\', \'opacity\', this.value)">\n';
    s += '&nbsp;\n';
    s += '<i>Color: </i><input type="color" id="layerColorId" value="#FF0000" style="vertical-align: bottom" ';
    s += 'onchange="updateLayer(\'' + this.config.name + '\', \'color\', this.value)">\n';
    s += '<i>OutlineColor: </i><input type="color"  id="layerOutlineColorId" value="#000000" style="vertical-align: bottom"';
    s += 'onchange="updateLayer(\'' + this.config.name + '\', \'outlineColor\', this.value)">';

    if (this.config.name) {
        s += " &mdash; " + this.config.name;
    }

    s += "\n" + lie;
    
    return s;
}

//TOOLS
//Takes any number of objects and returns one merged object
var objectMerge = function() {
    var out = {};
    if(!arguments.length)
        return out;
    for(var i=0; i<arguments.length; i++) {
        for(var key in arguments[i]){
            out[key] = arguments[i][key];
        }
    }
    return out;
}