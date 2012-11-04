/*
 * @include lib/GeoSIE/Brique.js
 */

/**
 * Header: Business
 * Couche métier javascript pour interface avec l'application GeoSIE
 * montre le fonctionnement des listeners
 * les fonctions d'accès à la base de données sont bouchonnés pour les tests
 */

/**
 * Namespace: DialogHandler
 */

var DialogHandler = {
		
    /**
	 * Method: updateResponseText
	 */
    updateResponseText : function (responseText) {
        if($("dialogContent")) {
            document.getElementById("dialogContent").innerHTML = responseText;
        }
    },

    /**
     * Method: closeFormPopup
     */
    closeFormPopup : function () {
        if($("#dialog")) {
            $("#dialog").dialog("close");
        }
    },
	
    /**
     * Method: createFormPopup
     */
    createFormPopup : function () {
        if($("#dialog")) {
            $("#dialog").dialog({
                title: 'Edition',
                bgiframe: true,
                modal: true,
                autoOpen: false,
                minHeight : 90,
                //height: 100
                width:330,
                zIndex: 20000
            });
        }
    }
};

/** 
 * Namespace: Business 
 */

var Business = {
		
    /**
	 * Method: init
	 */	
    init : function() {
        DialogHandler.createFormPopup();

        GeoSIE.Brique.events.on({
            "creationpoint": this.onCreationPoint,
            "creationline": this.onCreationLine,
            "creationpolygon": this.onCreationPolygon,
            "suppression": this.onSuppression,
            "deplacement": this.onDeplacement,
            "consult": this.onConsult,
            scope: this
        });
    },

    /**
     * Callback: onCreationPoint
     * Evénément lancé suite à la création d'un point (obstacle) "from scratch"
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} Le point a creer.
     */
    onCreationPoint: function(feature) {    
        this.creation(feature, "point");
    },

    /**
     * Callback: onCreationLine
     * Evénément lancé suite à la création d'une ligne "from scratch"
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} La ligne a creer.
     */
    onCreationLine: function(feature) {    
        this.creation(feature, "line");
    },
    
    //    /**
    //     * Callback: onDialogClose
    //     * Evénément lancé suite de la fermerture de la boite de dialogue de creation de geometrie.
    //     * 
    //     * Parameters: 
    //     * feature - {<OpenLayers.Feature.Vector>} La geometrie temporaire.
    //     */
    //    onDialogClose: function(feature) {
    //    	OpenLayers.Console.log("Call onDialogClose");
    //
    //    },
    
    /**
     * Callback: onCreationPolygon
     * Evénément lancé suite à la création d'un polygone "from scratch"
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} Le polygone a creer.
     */    
    onCreationPolygon: function(feature) {    
        this.creation(feature, "polygon");
    },    
    
    /**
     * Callback: onSuppression
     * Evénément lancé suite à la suppression d'obstacles
     * 
     * Parameters: 
     * features - {Array(<OpenLayers.Feature.Vector>)} Les geometries a supprimer.
     */    
    onSuppression: function(features) {
        // Teste si le tableau de features existe et si celui ci est composé de plus d'un élément
        if (!features || features.length == 0) {
            alert(GeoSIE.Messages.noObstacleMessage);
            return;
        }

        if (confirm("Confirmation de la suppression ? (Attention ! Cette opération est irréversible) ")) {
            this.suppression(features);
        }
    },

    /**
     * Callback: onDeplacement
     * Evénément lancé suite au déplacement d'un obstacle
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} La geometrie a deplacer.
     */          
    onDeplacement: function(feature) {
        var geom = feature.geometry;
        var displayAttributes=this.getAttributs(feature);
        this.update(feature, displayAttributes[0], displayAttributes[1], displayAttributes[2]);
    },
    
    /**
     * Method: suppression
     * Supprime des geometries
     * 
     * Parameters: 
     * features - {Array(<OpenLayers.Feature.Vector>)} Les geometries a supprimer.
     */
    suppression: function(features) {
        var toDels=new Array();
        var jsonFormatter = new OpenLayers.Format.JSON();

        for(var i=0;i<features.length;i++){
            var feature = features[i];
            var del = {
                type : this.getGeomType(feature),
                id : feature.fid.split(".")[1]	
            };
            toDels.push(del);
        }
        var dataTxt = jsonFormatter.write(toDels);

        var url = "./delete";
        Business.callDao("POST", url, dataTxt,
            function(responseText) {
                OpenLayers.Console.log(responseText);
        			
                eval(replaceTag(responseText));
                    
                if (data.idsToAdd.length > 0 || data.idsToDel.length > 0) {
                    OpenLayers.Console.log(data.idsToDel);
                    GeoSIE.Brique.refreshSelection(data.idsToAdd,data.idsToDel);
                }
                GeoSIE.Brique.forceRefreshLayers();
            }
            );
    },
    
    /**
     * Callback: creation
     * Evénément lancé suite à la creation d'une geometrie (modification)
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} La geometrie a creer.
     * type - {String} Le type de geometrie (point, line...).
     */         
    creation: function(feature,type) {
        // attributs recuperer lors de la creation d un point par snapping
        var idTopo="";
        var nomTopo="";
        if(feature.feature.attributes){
            var attributsTopo = feature.feature.attributes;
            if(attributsTopo["ID"]){
                idTopo=attributsTopo["ID"];
            }
            if(attributsTopo["NOM"]){
                nomTopo=attributsTopo["NOM"];
            }
        }
    	
        var form = 
        '<form id="editForm">'+
        '<fieldset>'+
        '<label for="title">Classification : </label>'+
        '<input type="text" name="attribut1" id="attribut1" style="width:100px" value="";" value="ouvrage" disabled/><br />'+ 
                        
        '<label for="title">Code comm : </label>'+
        '<input type="text" name="attribut2" id="attribut2" style="width:192px" value=""/><br />'+ 
                        
        '<label for="title">Libell&eacute; : </label>'+
        '<input type="text" name="attribut3" id="attribut3" style="width:192px" value=""/><br />'+ 		
                        
        '</fieldset>'+
        '<div style="text-align: center;">'+
        '<input type="submit" style="cursor: pointer;" value="Envoyer" />'+
        '<input type="reset" style="cursor: pointer;" value="Annuler" />'+
        '</div>'+
        '</form>';   
        
        DialogHandler.updateResponseText(form);
    	
        document.getElementById('attribut1').value = "ouvrage";
        //document.getElementById('attribut2').value = idTopo;
        //document.getElementById('attribut3').value = nomTopo;
        document.getElementById('attribut2').value = "";
        document.getElementById('attribut3').value = "";

        // delete the temp vector
        $("#dialog").bind('dialogclose', function(event){
            OpenLayers.Console.log("Close dialog edition");
            GeoSIE.Brique.vectorTempLayer.destroyFeatures([feature.feature]); 
        //Carto.refreshAfterCreation();
        });

        $("#editForm").bind("submit", function(){
            OpenLayers.Console.log(feature);
            // get the form values
            var attribut1 = $( "#attribut1" ).val();
            var attribut2 = $( "#attribut2" ).val();
            var attribut3 = $( "#attribut3" ).val();
			
            //var wgs84 = new OpenLayers.Projection("EPSG:4326");
			
            // converting the digitized Geometry to WGS84 LL
            //var clone = feature.feature.geometry.clone();
            //var geomWgs84=clone.transform(GeoSIE.Brique.map.getProjectionObject(), wgs84);
            //var geom = "SRID=4326;"+geomWgs84.toString(); 
            var geom = "SRID=2154;"+ feature.feature.geometry.toString(); 
			
            var jsonFormatter = new OpenLayers.Format.JSON();
            var params = {
                type:type,
                classification:attribut1,	
                attribut2:attribut2,	
                attribut3:attribut3,
                geom:geom
            };
			
            var dataTxt = jsonFormatter.write(params);
	
            var url = "./create";
            Business.callDao("POST", url, dataTxt,
                function(responseText) {
                    OpenLayers.Console.log(responseText);
            			
                    eval(replaceTag(responseText));
                    if (data.idsToAdd.length > 0) {
                        GeoSIE.Brique.refreshSelection(data.idsToAdd,data.idsToDel);
                        GeoSIE.Brique.forceRefreshLayers();
                        console.log(data.idsToAdd);
                        GeoSIE.Brique.vectorLayer.addFeatures([feature.feature.clone()]);	
  
                        GeoSIE.Brique.refreshAfterCreation(data.idsToAdd);
                        GeoSIE.Brique.forceRefreshLayers();
                    }
                    //OpenLayers.Console.log(data.idsToAdd);

                    if (data.idsToAdd.length > 0 || data.idsToDel.length > 0) {
                        GeoSIE.Brique.refreshSelection(data.idsToAdd,data.idsToDel);
                    }  	
                    GeoSIE.Brique.forceRefreshLayers();

                }
                );
            
            // delete temp geometry
            GeoSIE.Brique.vectorTempLayer.destroyFeatures([feature.feature]);  
            
            $("#dialog").dialog("close");
		
            // prevent normal submit
            return false; 
        });    	
    	
        $("#dialog").dialog("open");        
        
    },	
    
    /**
     * Method: getAttributs
     * Recupere les attributs du feature
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} La geometrie avec ses attributs.
     * 
     * Returns:
     * {Array(String)} Un tableau d'attributs.
     */
    getAttributs: function(feature){
        // form with the getfeatureinfo attributs
        var displayAttributes=[];
        displayAttributes[0]="Non renseigné";
        displayAttributes[1]="Non renseigné";
        displayAttributes[2]="Non renseigné";
        
        if(feature.attributes){
            var attributes = feature.attributes;
            var ind=0;
            for (key in attributes){
                if(attributes[key]!=null){
                    // display only 3 attributes not the id
                    if(ind >= 1 && ind<4){
                        displayAttributes[ind-1]= attributes[key]; // key+" - "+attributes[key]
                    } 
                    ind++;
                }
            }
        }
        
        return displayAttributes;
    },
    
    /**
     * Callback: onConsult
     * Evénément lancé suite à la consultation d'une geometrie (modification)
     * 
     * Parameters: 
     * feature - {<OpenLayers.Feature.Vector>} La geometrie a afficher dans la popup.
     */       
    onConsult: function(feature) {
        console.log("onconsult");
        var displayAttributes=this.getAttributs(feature);
        var form = 
        '<form id="editForm">'+
        '<fieldset>'+
        '<label for="title">Classification : </label>'+
        '<input type="text" name="attribut1" id="attribut1" style="width:100px" value="";"/><br />'+ 
        '<label for="title">Attribut 2 : </label>'+
        '<input type="text" name="attribut2" id="attribut2" style="width:192px" value="";"/><br />'+ 
        '<label for="title">Attribut 3 : </label>'+
        '<input type="text" name="attribut3" id="attribut3" style="width:192px" value="";"/><br />'+ 		
        '</fieldset>'+
        '<div style="text-align: center;">'+
        '<input type="submit" style="cursor: pointer;" value="Envoyer" />'+
        '<input type="reset" style="cursor: pointer;" value="Annuler" />'+
        '</div>'+
        '</form>';   
        
        DialogHandler.updateResponseText(form);    
        
        document.getElementById('attribut1').value = displayAttributes[0];
        document.getElementById('attribut2').value = displayAttributes[1];
        document.getElementById('attribut3').value = displayAttributes[2];    
    	
        $("#editForm").bind("submit", function(){
            OpenLayers.Console.log(feature);
            // Get the form values
            var attribut1 = $( "#attribut1" ).val();
            var attribut2 = $( "#attribut2" ).val();
            var attribut3 = $( "#attribut3" ).val();

            Business.update(feature,attribut1,attribut2,attribut3);
            $("#dialog").dialog("close");
		
            // prevent normal submit
            return false; 
        });    	
    	
        $("#dialog").dialog("open");
    },
    
    /**
     * Methode: update
     * Met a jour une geometrie.
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} La geometrie a mettre a jour.
     * attribut1 - {String} Un attribut.
     * attribut2 - {String} Un attribut.
     * attribut3 - {String} Un attribut.
     */
    update : function(feature,attribut1,attribut2,attribut3){
        var type = Business.getGeomType(feature);
        var id = feature.fid.split(".")[1];

        var wgs84 = new OpenLayers.Projection("EPSG:4326");
		
        // Converting the digitized Geometry to WGS84 LL
        var clone = feature.geometry.clone();
        var geomWgs84=clone.transform(GeoSIE.Brique.map.getProjectionObject(), wgs84);
        var geom = "SRID=4326;"+geomWgs84.toString(); 
		
        var jsonFormatter = new OpenLayers.Format.JSON();
        var params = {
            type:type,
            id:id,
            classification:attribut1,	
            attribut2:attribut2,	
            attribut3:attribut3,
            geom:geom
        };
		
        var dataTxt = jsonFormatter.write(params);

        var url = "./update";
        Business.callDao("POST", url, dataTxt,
            function(responseText) {
                OpenLayers.Console.log(responseText);
        			
                eval(replaceTag(responseText));
                if (data.idsToAdd.length > 0 || data.idsToDel.length > 0) {
                    GeoSIE.Brique.refreshSelection(data.idsToAdd,data.idsToDel);
                }  	
                GeoSIE.Brique.forceRefreshLayers();
            }
            );
    },
    
    /**
     * Method: getGeomType 
     * Retourne le type d une geometrie
     * 
     * Parameters:
     * feature - {<OpenLayers.Feature.Vector>} La geometrie.
     * 
     * Returns:
     * {String} Le type de geometrie.
     */  
    getGeomType : function(feature){
        var type = ""; 
        var classGeom = feature.geometry;
        if(classGeom instanceof OpenLayers.Geometry.Point){
            type="point";
        } else if (classGeom instanceof OpenLayers.Geometry.LineString){
            type="line";
        } else if (classGeom instanceof OpenLayers.Geometry.Polygon) {
            type="polygon";
        }
        OpenLayers.Console.log(type);
        return type;
    },
    
    /**
     * Method: callDao
     * 
     * Parameters:
     * method - {String} La methode POST ou GET.
     * url - {String} URL a interroger.
     * param - params
     * callback - callback
     */
    callDao : function(method, url, param, callback) {
        var xmlhttp = new OpenLayers.Request.XMLHttpRequest();
        /* on définit ce qui doit se passer quand la page répondra */
        xmlhttp.onreadystatechange = function() {
            if (xmlhttp.readyState == 4) /* 4 : état "complete" */
            {
                if (xmlhttp.status == 200) /* 200 : code HTTP pour OK */
                {
                    /*
					Traitement de la réponse.
						Ici on affiche la réponse dans une boîte de dialogue.
                         */
                    if (callback) {
                        callback(xmlhttp.responseText);
                    }
                }
            }
        };
	
        xmlhttp.open(method, url, true);
        if (method == "POST") {
            // Query use utf-8 encoding for prepare urlencode data, force request content-type and charset.
            var contentType = "application/json; charset=UTF-8"; //application/json application/x-www-form-urlencoded
            xmlhttp.setRequestHeader("Content-Type", contentType);
            xmlhttp.send(param);
        } else if (method == "GET") {
            xmlhttp.send("");
        } else {
            alert("La methode " + method + "n'est pas valide !!! ");
        }
    }

};

/**
 * Namespace: window
 */

/**
 * Method: replaceTag 
 */
var replaceTag = function(responseText) {
    return responseText.replace("<div id='response'>", "").replace("</div>", "");
};

/**
 * Method: _getObsIdKVP 
 */
var _getObsIdKVP = function(features) {
    var str = "";
    var j = 0;
    for ( var i = 0; i < features.length; i++) {
        if (features[i] == null) {
            continue;
        }
        str += "obsId" + j + "=" + features[i].fid.split(".")[1];
        str += (i == (features.length - 1) ? "" : "&");
        j++;
    }
    return str;
};
