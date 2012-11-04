

/**
 * Class: GeoSIE.Messages
 * Content message for window alert
 */

GeoSIE.Messages = {
		
	/**
	 * Constants: string messages
	 * zoomInToolTip - {String}
	 * zoomOutToolTip - {String}
	 * zoomToMaxExtentToolTip - {String}
	 * getFeatureToolTip - {String}
	 * dragFeatureToolTip - {String}
	 * wpsSelectToolTip - {String} 
	 * wpsGetFeatureToolTip - {String} 
	 * wpsDownloadToolTip - {String} 
	 * snappingToolTip - {String} 
	 * editPolygonToolTip - {String}
	 * editLineToolTip - {String}
	 * editPointToolTip - {String}
	 * navigationHistoryNextToolTip - {String}
	 * navigationHistoryPreviousToolTip - {String}
	 * printToolTip - {String}
	 * dragPanToolTip - {String}
	 * helpToolTip - {String}
	 * contactToolTip - {String}
	 * fullScreenMinimizeToolTip - {String}
	 * fullScreenMaximizeToolTip - {String}
	 * noObstacleMessage - {String} 
     * scaleMessage - {String} 
     * errorGotoCoordinatesMessage - {String} 
     * noZoneGotoMessage - {String}
     * noLegendMessage - {String} 
     * selectWpsProcessMessage - {String} 
     * noQuerableLayerMessage - {String} 
     * noResultWpsMessage - {String} 
     * noSnappableLayerMessage - {String} 
     * noResultWpsDownloadMessage - {String} 
     * noResultGetFeatureWpsMessage - {String} 
     * errorWMSGetCapabilitiesMessage - {String}
     * errorWMSGetFeatureInfoMessage - {String}
     * noToponymeWpsMessage - {String} 
     * errorServerWpsMessage - {String} 
     * tooMuchFeaturesWpsMessage - {String} 
     * geonamesNoResultMessage - {String} 
     * geonamesTooBusyMessage - {String}
	 */	
	zoomInToolTip: "Sélectionner une zone à zoomer",
	zoomOutToolTip: "Cliquer sur la carte pour dezoomer",
	zoomToMaxExtentToolTip: "Afficher carte entière",
	getFeatureToolTip: "Sélectionner un ensemble de géometries",
	dragFeatureToolTip: "Déplacer une géometrie",
	wpsSelectToolTip: "Cliquer pour sélectionner une procédure",
	wpsGetFeatureToolTip: "Cliquer sur un troncon",
	wpsDownloadToolTip: "Télécharger le résultat",
	measureToolTip: "Mesurer une distance",
	snappingToolTip: "Créer un point avec assistance par aimantation",
	editPolygonToolTip: "Créer une aire polygonale",
	editLineToolTip: "Créer une ligne brisée",
	editPointToolTip: "Créer un point",
	navigationHistoryNextToolTip: "Zoom suivant",
	navigationHistoryPreviousToolTip: "Zoom précédent",
	printToolTip: "Imprimer",
	dragPanToolTip: "Se déplacer sur la carte",	
	fullScreenMinimizeToolTip: "Réduire la carte",
	fullScreenMaximizeToolTip: "Agrandir la carte",
	helpToolTip: "Aide",
	contactToolTip: "Contact",
	noObstacleMessage: "Aucune résultat sélectionné, veuillez sélectionner au moins un résultat.",
    scaleMessage: "L'outil n'est pas utilisable à cette échelle (utilisable à partir du 1/",
    errorGotoCoordinatesMessage:"Les coordonnées ne sont pas valides.",
    noZoneGotoMessage:"Les coordonnées ne correspondent à aucune zone.",
    selectWpsProcessMessage: "Sélectionner une procédure.",
    noQuerableLayerMessage: "Aucune couche est interrogeable.",
    noResultWpsMessage: "Erreur : aucun résultat FME.",
    noResultWFS: "Pas de données pour cette sélection.",
    noSnappableLayerMessage: "Aucune couche est aimantable.",
    noResultWpsDownloadMessage: "Erreur : aucun résultat FME à télécharger.",
    noResultGetFeatureWpsMessage: " Erreur : aucun tronçon, zoomer puis cliquer sur un autre tronçon.",
    errorWMSGetCapabilitiesMessage:"Erreur : requête WMS GetCapabilities.",
    errorWMSGetFeatureInfoMessage:"Erreur : requête WMS GetFeatureInfo.",
    noToponymeWpsMessage:"Il n'y a pas de toponyme pour ce fleuve, la procédure n'est pas exécutable.",
    errorServerWpsMessage : "Erreur : connexion au serveur FME.",
    tooMuchFeaturesWpsMessage :"Plus de 2 tronçons sont sélectionnés, sélectionner 1 seul tronçon.",
    geonamesNoResultMessage: "Il n'y a pas de commune correspondant à votre recherche.",
    geonamesTooBusyMessage: "Le service de recherche n'est pas disponible."
}