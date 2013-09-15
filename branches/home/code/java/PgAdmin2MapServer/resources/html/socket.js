// Defines a simple test socket to communicate with the server

// opens the WebSocket channel to receive updates from server
webSocket = new WebSocket('ws://localhost:8887/configDispatcher');

webSocket.onopen = function(event) {
    // just to test
    //var token = '{"type": "login", "username" : "user", "password" : "user"}';
    //var lToken = {"type": "login", "ns": "org.jWebSocket.plugins.system", "username" : "user", "password" : "user"};
    //console.log('sending login token to socket server: ' + token);
    console.log('socket opened on client');
    //webSocket.send(token);
};

webSocket.onmessage = function(event) {
    if (event.data.indexOf("{") == 0) {
        // a json object as repsonse
        // TODO: JSON framework here
        console.log("socket server message: " + event.data);
        var response = JSON.parse(event.data);

        if (response.treeModel) {
            // Force refresh the page :(
            // time to find how to refresh the map
            // with new MapConfig
            window.location.href = "/map";
            //
            // server sends us a new mapConfig.
            // Removes layers from map:
            /*
             var i = removeMapLayers();
             console.log(i + " layer(s) removed");
             // remove layers from layerTree
             removeLayersFromTree();
             
             // refreshes the map
             var map = createMap(response);
             
             Ext.getCmp('map').map = map;
             //refreshes the tree model
             addLayersToTree(response.treeModel);
             Ext.getCmp('tree').render();
             viewport.doLayout();
             console.log(map);
             map.zoomToMaxExtent();
             */
        }
    } else {
        // a server message: TODO: command manager
        console.log("socket server sent message: " + event.data);
        if ("refreshConfig" == event.data) {
            // server received a new map configuration: loads it
            map.destroy();
            init();
        }
    }
}


// called when server closes this websocket
webSocket.onclose = function(event) {
    console.log('server closed connection');
}
