// this file will be included inside a <script></script> tag in the main GAS layout
// it may contain any global javascript you might need to reference in the individual 
// subpages

/*
var pref_carto = new OpenLayers.Layer.WMS( "fond carto",
                    "http://meije.wrk.cby/tilecache/tilecache.py",
                    {layers: 'pref'},
                    { buffer:0,
                      displayInLayerSwitcher:true,
                      isBaseLayer:true,
                      maxExtent:new OpenLayers.Bounds(586087,1110499,621881,1146513),
                      units: 'm',
                      projection:'EPSG:27571',
                      resolutions:[17.63887936389403,8.819439681947015,3.527775872778806,1.763887936389403,0.8819439681947016,0.35277758727788067]
                    });
var pref_navteq = new OpenLayers.Layer.WMS( "fond navteq",
                    "http://meije.wrk.cby/tilecache/tilecache.py",
                    {layers: 'pref-ortho'},
                    { buffer:0,
                      displayInLayerSwitcher:true,
                      isBaseLayer:true,
                      resolutions:[0.8819439681947016,1.763887936389403,3.527775872778806,8.819439681947015,17.63887936389403],
                      maxExtent: new OpenLayers.Bounds(590000, 2420000 ,610000,2435000),
                      units: 'm',
                      projection: 'EPSG:27582'
                  });
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;

*/

function customizeConfigurationMap(map) {
/*
    for(var i=0;i<map.layers.length;i++) {
      map.layers[i].displayInLayerSwitcher = false;
    }
    map.addControl(new OpenLayers.Control.LayerSwitcher());
    var l1 = pref_carto.clone();
    var l2 = pref_navteq.clone();
    map.addLayer(l1);
    map.addLayer(l2);
    map.setBaseLayer(l1);
*/
}

function customizePublisherMap(map) {
    //same as for config map
    customizeConfigurationMap(map)
}


