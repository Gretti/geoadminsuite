/**
 * Class: GeoSIE.Util
 * A bench of tools.
 */

GeoSIE.Util = {
		
	    /**
	     * Method: getFeatureTable
	     * Get the db name origine of a feature fid.
	     * Ex: eau.120, return eau
	     * 
	     * Parameters:
	     * feature - {<OpenLayers.Vector.Feature>}
	     * 
	     * Returns:
	     * {String} Fid feature table
	     */
	    getFeatureTable: function(feature){
            if(feature.fid) {
    	    	var end = feature.fid.lastIndexOf(".");
                var name = feature.fid.substring(0, end);
                return name;
            } else {
            	return null;
            }
	    },
	    
	    /**
	     * Method: getFeatureID
	     * Get the feature id.
	     * 
	     * Parameters:
	     * feature - {<OpenLayers.Vector.Feature>}
	     * 
	     * Returns:
	     * {String} Feature ID (db key)
	     */
	    getFeatureID: function(feature){
            if(feature.fid) {
            	var end = feature.fid.lastIndexOf(".");
            	var id = feature.fid.substring(end+1, feature.fid.length);
            	return id;
            } else {
            	return null;
            }
	    },
	    
	    /**
	     * Method: getSrid
	     * Get the srid from a epsg code.
	     * 
	     * Parameters:
	     * srs - {String} SRS code ex: EPSG:4326
	     * 
	     * Returns:
	     * {String} The srid ex: 4326
	     */
	    getSrid: function(srs){
            var end = srs.lastIndexOf(":");
            var srid = srs.substring(end+1,srs.length);
	    	//var srid = srs.substring(srs.lastIndexOf(":"));
	    	return srid;
	    },		
		
		/**
		 * Method: getUrlVars
		 * 
		 * Parameters:
		 * {String} URL 
		 * 
		 * Returns: 
		 * {Array}
		 */
		getUrlVars: function(url) {
		    var vars = [], hash;
		    var hashes = url.slice(url.indexOf('?') + 1).split('&');
		    for(var i = 0; i < hashes.length; i++)
		    {
		        hash = hashes[i].split('=');
		        vars.push(hash[0]);
		        vars[hash[0]] = hash[1];
		    }
		    return vars;
		},
		
	
		/**
		 * Method: displayWidget
		 * Set the style for a bar.
		 * 
		 * Parameters:
		 * widgets - {String} controls list.
		 * name - {String} bar name.
		 * width - {Integer} bar width.
		 * left - {Integer} 
		 * 
		 * Returns:
		 * {Boolean} bar exist.
		 */
		displayWidgets: function(widgets, name, width, left){
	    	if(widgets.length>0){
	    		var elTitle = OpenLayers.Util.getElement(name + "Title");
	    		var elBar = OpenLayers.Util.getElement(name + "ButtonBar");

	    		elTitle.style.display = "block";
	    		elTitle.style.width = width + "px";
	    		elTitle.style.left = left + "px";
	    		
	    		elBar.style.display = "block";
	    		elBar.style.width = width + "px";
	    		elBar.style.left = left + "px";	
	    		
	    		return true;
	    	}    	
	    	return false;
		}
};
