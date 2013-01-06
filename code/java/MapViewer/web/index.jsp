<!DOCTYPE html>
<html>
    <head>

        <style>
            .bold {
                font-weight: bold;
            }
            .example {
                padding: 10px;
                border: 1px solid #CCC;
            }
            #example-list-fs ul {
                padding-left: 0;
            }
            #example-list-fs li {
                list-style: none;
            }
            #example-list-fs img {
                vertical-align: middle;
            }
            button {
                padding: 5px 8px;
                cursor: pointer;
                text-shadow: 1px 1px white;
                font-weight: 700;
                font-size: 10pt;
            }
            body {
                font: 14px Verdana;
            }

        </style>

        <script type="text/javascript" src="scripts/poc.js"></script>
        <script type="text/javascript">
            // The local MapFile URL used in the iframe to display
            // Chrome filesystem mapFile
            // TODO: ln or guess the true Filepath, but how ?
            var MAPFILE_URL="http://localhost/cgi-bin/mapserv?mode=browse&template=openlayers&map=/Users/nicolas/Library/Application%20Support/Google/Chrome/Default/File%20System/002/t/00/00000007&"
            window.requestFileSystem = window.requestFileSystem || window.webkitRequestFileSystem;
            var fs = null;
            // the mapfile FileEntry object
            var mapFileEntry = null;
            // the Mapfile object. TODO: config...
            var mapFile = new MapObj();
            
            function errorHandler(e) {
                var msg = '';
                switch (e.code) {
                    case FileError.QUOTA_EXCEEDED_ERR:
                        msg = 'QUOTA_EXCEEDED_ERR';
                        break;
                    case FileError.NOT_FOUND_ERR:
                        msg = 'NOT_FOUND_ERR';
                        break;
                    case FileError.SECURITY_ERR:
                        msg = 'SECURITY_ERR';
                        break;
                    case FileError.INVALID_MODIFICATION_ERR:
                        msg = 'INVALID_MODIFICATION_ERR';
                        break;
                    case FileError.INVALID_STATE_ERR:
                        msg = 'INVALID_STATE_ERR';
                        break;
                    default:
                        msg = 'Unknown Error';
                        break;
                };
                document.querySelector('#example-list-fs-ul').innerHTML = 'Error: ' + msg;
            }
      
            // inits the filesystem
            function initFS() {
                window.requestFileSystem(window.TEMPORARY, 1024*1024, function(filesystem) {
                    fs = filesystem;
                }, errorHandler);
            }
            
            // writes the mapfile
            function writeMapFile() {
                // generates mapfile
                fs.root.getFile('local.map', {create: true}, function(fileEntry) {
                    mapFileEntry = fileEntry
                    // Create a FileWriter object for our FileEntry (log.txt).
                    mapFileEntry.createWriter(function(fileWriter) {

                        fileWriter.onwriteend = function(e) {
                            console.log('Write completed.');
                        };

                        fileWriter.onerror = function(e) {
                            console.log('Write failed: ' + e.toString());
                        };

                        // Create a new Blob and write it to local.map.
                        var s = mapFile.toString();
                        //console.log(s);
                        var blob = new Blob([s], {type: 'text/plain'});
                        fileWriter.write(blob);

                    }, errorHandler);

                }, errorHandler);
                var filelist = document.querySelector('#example-list-fs-ul');
                filelist.innerHTML = 'MapFile created: <button onclick="showMapFileEntry()">view</button>';
            }
            
            // shows the mapfile content in another window
            function showMapFileEntry() {
                if (mapFileEntry == null) {
                    console.log("null MapFileEntry...");
                } else {
                    window.open(mapFileEntry.toURL());

                }
            }
            
            /**
             * Returns a LAYER object from the given dragged file, trying to guess the layer's type
             * from extension
             */
            function getLayerFromDrag(file) {
                var lay = new LayerObj();
                
                if (file) {
                    var type = document.querySelector('#layerTypeId').value;
                    if (type == "empty") {
                        if ( (/\.(shp|mif|tab|gml|kml|js|GEOJSON|SHP|MIF|TAB|GML|KML|JS|GEOJSON)$/i).test(file.name) ) {
                            type = "POLYGON";
                        } else if ( (/\.(gif|jpg|jpeg|tiff|tif|png|GIF|JPG|JPEG|TIFF|TIF|PNG)$/i).test(file.name) ) {
                            type = "RASTER";
                        }
                    }
                    lay = new LayerObj({
                        "data" : file.name,
                        "name" : file.name,
                        "type" : type,
                        "opacity" : document.querySelector('#layerOpacityId').value,
                        "color" : document.querySelector('#layerColorId').value,
                        "outlineColor" : document.querySelector('#layerOutlineColorId').value
                    });

                }
                return lay;
            }
            
            /**
             * creates a layer from the given file object, applying the current page configuration
             * writes the new mapfile
             * with this layer definition and displays it in the iframe
             */
            function displayDraggedLayer(file) {
                var lay = getLayerFromDrag(file);
                var h = document.querySelector('#layerList').innerHTML;
                document.querySelector('#layerList').innerHTML = h + "\n" + lay.toHTML();
                document.querySelector('#layersSwitch').hidden = false;
                // mapfile generation
                mapFile.addLayer(lay, document.querySelector('#addModeReplaceId').checked);
                
                writeMapFile();
                document.querySelector('#mapIframe').src = MAPFILE_URL;            
            }
            
            // adds a test layer
            function addTestLayer() {
                var layer = new LayerObj({
                    "name" : "ne_110m_admin_0_countries.shp",
                    "data" : "ne_110m_admin_0_countries.shp",
                    "type" : "polygon"
                });
                mapFile.addLayer(layer, true);
                writeMapFile();
                document.querySelector('#mapIframe').src = MAPFILE_URL;
            }
            
            // loads the mapfile into MapServer
            function loadMap() {
                document.location.href = "http://localhost/cgi-bin/mapserv?mode=browse&template=openlayers&map=" + mapFileEntry.toURL();
            }
            
            /**
             * Removes the given layer from the mapfile
             */
            function removeLayer(layerName) {
                mapFile.removeLayer(layerName);
                writeMapFile();
                document.querySelector('#mapIframe').src = MAPFILE_URL;
                var inner = '';
                for (var i = 0; i < mapFile.layers.length; i++) {
                    inner += mapFile.layers[i].toHTML() + "\n"; 
                }
                document.querySelector('#layerList').innerHTML = inner;
            }
            
            /**
             * Updates the given layer for the given property
             */
            function updateLayer(layerName, propName, propValue) {
                console.log("l: " + layerName + " prop: " + propName + ' val: ' + propValue);
                mapFile.getLayer(layerName).config[propName] = propValue;
                writeMapFile();
                document.querySelector('#mapIframe').src = MAPFILE_URL;
            }
            
            // onload handler
            function init() {
                var buttons = document.querySelectorAll('#example-list-fs button');
                // adds event handlers for buttons
                if (buttons.length >= 1) {
                    buttons[0].addEventListener('click', function(e) {
                        if (!fs) {
                            return;
                        }
                        writeMapFile();
                    }, false);
      
                    buttons[1].addEventListener('click', function(e) {
                        if (!fs) {
                            console.log ("filesystem not initialized...")
                            return;
                        }
                        addTestLayer();
                        //loadMap();
                    }, false);
                }
                
                // initialize Drag'n'drop
                var dropZone = document.querySelector('#example-list-fs');
                dropZone.addEventListener('drop', function(e) {
                    if (event.preventDefault) { 
                        event.preventDefault();
                    }
                    console.log(e.dataTransfer.files[0]);
                    displayDraggedLayer(e.dataTransfer.files[0]);
                    /*
                    var file = e.dataTransfer.files[0];
                    var lay = new LayerObj({
                        "data" : file.name,
                        "name" : file.name,
                        "type" : "polygon"
                    });
                    mapFile.addLayer(lay, true);
                    writeMapFile();
                    document.querySelector('#mapIframe').src = MAPFILE_URL;
                     */
                    
                }, false);
                
                // Initiate filesystem on page load.
                if (window.requestFileSystem) {
                    initFS();
                }
                
                // displays the empty layer components
                var l = new LayerObj();
                document.querySelector('#emptyLayerSpan').innerHTML = l.toHTML();
            }
      
        </script>

    </head>
    <body onload="init()">
        <div id="example-list-fs" class="example rounded">
            <i>Add Layer mode:</i> 
            <input type="radio" name="addMode" id="addModeReplaceId" checked="true">
            <label for="addModeReplaceId" class="bold">replace</label>

            <input type="radio" name="addMode" id="addModeAppendId">
            <label for="addModeAppendId" class="bold">append</label>
            &mdash;
            <span id="emptyLayerSpan"></span>
           <!--
            <i>Layer type:</i> <select id="layerTypeId" class="bold">
                <option name="empty" value="empty"   selected="true">auto</option>
                <option name="point" value="POINT" >POINT</option>
                <option name="line" value="LINE">LINE</option>
                <option name="polygon" value="POLYGON">POLYGON</option>
                <option name="raster" value="RASTER">RASTER</option>
            </select>
            &mdash;
            <i>Opacity:</i><input type="number" id="layerOpacityId" value="50" class="bold">
            &mdash;
            <i>Color: </i><input type="color" id="layerColorId" value="#FF0000" style="vertical-align: bottom">
            <i>OutlineColor: </i><input type="color"  id="layerOutlineColorId" value="#000000" style="vertical-align: bottom">
            -->

            <div id="layersSwitch" hidden style="border: none;"><ul id="layerList"></ul></div>
            <button>Create empty mapfile</button><button>Test</button>&nbsp;&nbsp;&nbsp;<span id="example-list-fs-ul"></span>
        </div>

        <!-- iframe div, to display MapServer maps -->
        <div id="mapIframeDiv">
            <br/>
            <iframe style="border: none;" id="mapIframe" src="about:blank" width="1200" height="900" />
        </div>
    </body>
</html>