/*
 */

// namespace


// pgiraud : currently, this is the only way to set a slideFactor
// for the control in a PanPanel
// See http://trac.openlayers.org/ticket/2320
OpenLayers.Control.Pan.prototype.slideFactor = 200;

/**
 * Class: GeoSIE.Config
 * Object with the application configuration for GeoSIE
 */

GeoSIE.Config = {

    /**
     * Constant: NAVIGATION_BUTTONS
     * {Array{String}} Buttons controls for navigation bar.
     * Buttons available:
     * - zoomIn: zoom +
     * - zoomOut: zoom -
     * - nav: small hand to navigate
     * - maxExtent: zoom to map max extent
     * - history: 2 historic buttons to navigate
     * - print: print the map
     */
    NAVIGATION_BUTTONS: ['zoomIn', 'zoomOut', 'history','print'],	

    /**
     * Constant: EDITION_BUTTONS
     * {Array{String}} Buttons controls for edition bar.
     * Buttons available:
     * - point: draw point
     * - line: draw line
     * - polygon: draw polygon
     * - snapping: draw point with snapping
     * - query: WFS query
     * - drag: drag a geometry
     */
    EDITION_BUTTONS: ['point','line','polygon','query','drag'],	    //,'query','snapping','drag','polygon'
    
    /**
     * Constant: PROCESS_BUTTONS
     * {Array{String}} Buttons controls for process bar.
     * Buttons available :
     * - wps: wps selector buttons
     * - measure: measure tool
     */
    PROCESS_BUTTONS: ['wps','measure'],	        
    
    /**
     * Constant: OTHER_BUTTONS
     * {Array{String}} Buttons controls for most right bar.
     * Buttons available :
     * - full: full map screen
     * - help: link to help page
     * - contact: link to contact
     */
    OTHER_BUTTONS: ['full','help','contact'],	    
    
    /**
     * Constant: PRINT_URL
     * {String} Print service URL.
     */
    PRINT_URL: "print/print.pdf",
	
    /**
     * Constant: PRINT_FORMATS
     * {Array{Object}} Print formats display (A4, A3).
     */
    PRINT_FORMATS: [
        {libelle:"A4", value:"A4"},
        {libelle:"A3", value:"A3"}
    ],	

    /**
     * Constant: PRINT_TEMPLATES
     * {Array{String}} Print templates display (paysage, portrait).
     */
    PRINT_TEMPLATES: [
        {libelle:"Paysage", value:"paysage"},
        {libelle:"Portrait", value:"portrait"}
    ],		

    /**
     * Constant: PRE_TITLE_PRINT
     * {String} Pre title for print title input.
     */
    PRE_TITLE_PRINT: "Pré titre pour GeoSIE",	                  
	                  
    /**
     * Constant: OUTPUT_FILENAME
     * {String} The default filename for the files downloaded from the print servlet.
     * Example: GeoSIE_Brique.pdf
     */
    OUTPUT_FILENAME: "carte",

    /**
     * Constant: ROOT_PATH
     * {String} The rootPath (for images). Example : ../
     */
    ROOT_PATH: "",
    
    /**
     * Constant: FORCED_AREA
     * {String} zone id, init displayed map zone,
     * cf GeoSIE.ZoneManager.ZONES. 
     */
    FORCED_AREA: "metropole", //reunion

    /**
     * Constant: MAP_SCALES
     * {Array({Integer})} The map scales in meters (the map projection unit for all the zones is meters).
     */	
    MAP_SCALES: [
        10000000,
        4000000,
        2000000,
        1000000,
        500000,
        250000,
        100000, 
        50000,
        25000,
        10000,
        5000,
        2500,
        1000
    ],
     
    /**
     * Constant: SNAPPING_MIN_SCALE
     * {Integer} The map min scale in meters authorized to use snapping tool.
     */		
    SNAPPING_MIN_SCALE: 25000,

    /**
     * Constant: EDIT_MIN_SCALE
     * {Integer} The map min scale in meters authorized to use edit tools.
     */		
    EDIT_MIN_SCALE: 50000,     
     
    /**
     * Constant: QUERY_MIN_SCALE
     * {Integer} The map min scale in meters authorized to use query tool.
     */		
    QUERY_MIN_SCALE: 50000,    
    
    /**
     * Constant: DRAG_MIN_SCALE
     * {Integer} The map min scale in meters authorized to use drag tool.
     */		    
    DRAG_MIN_SCALE: 50000, 

    /**
     * Constant: WMC
     * {String} WMC context file URL.
     */
    WMC: "context/context.xml",
	
    /**
     * Constant: STYLE
     * {String} SLD style file URL.
     */		
    STYLE: "sld/style.xml",

    /**
     * Constant: GF_ATTRIBUTS
     * {Array(Object)} Config to display the layer getfeature attributs for the bloc/dialog "Resultat".
     * 
     * A getfeature config properties:
     * * fids - Array{String} The table with feature id.
     * * template - {String} The template to display the feature attributs
     * 
     */		
    GF_ATTRIBUTS: [
        {
            fids: ["ouvrages"],
            template: 
                '<div id="${widgetId}_${idw}" class="listitem" >' +
                '    <div>' +
                '        <div class="obstStyle">' +
                '            <span>' +
                '                <input type="checkbox" id="chk_${widgetId}_${idw}" />' +
                '            </span>' +
                '            <span style="cursor:pointer; margin-left:15px;" title="Consulter/Editer les attributs" id="title_${widgetId}_${idw}">' +                   
                '                Ouvrage n&deg; ${gid} - Classe ${classification}' +
                '            </span>' +
                '        </div>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            num ouvrage' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_2}' +
                '        </span>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            num com loc' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_3}' +
                '        </span>' +      
                '    </div>' +
                '</div>'				        
        },	
        {fids: ["SIE_LIGNE","SIE_POLYGONE"],
            template: 
                '<div id="${widgetId}_${idw}" class="listitem" >' +
                '    <div>' +
                '        <div class="obstStyle">' +
                '            <span>' +
                '                <input type="checkbox" id="chk_${widgetId}_${idw}" />' +
                '            </span>' +
                '            <span style="cursor:pointer; margin-left:15px;" title="Consulter/Editer les attributs" id="title_${widgetId}_${idw}">' +                   
                '                Ligne ou polygone ${id_brique} - Classe ${classification}' +
                '            </span>' +
                '        </div>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            Attribut 2' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_2}' +
                '        </span>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            Attribut 3' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_3}' +
                '        </span>' +      
                '    </div>' +
                '</div>'		 
        },
        {fids: ["ouvrage"],
            template: 
                '<div id="${widgetId}_${idw}" class="listitem" >' +
                '    <div>' +
                '        <div class="obstStyle">' +
                '            <span>' +
                '                <input type="checkbox" id="chk_${widgetId}_${idw}" />' +
                '            </span>' +
                '            <span style="cursor:pointer; margin-left:15px;" title="Consulter/Editer les attributs" id="title_${widgetId}_${idw}">' +                   
                '                Point ${id_brique} - Classe ${classification}' +
                '            </span>' +
                '        </div>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            Attribut 2' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_2}' +
                '        </span>' +
                '    </div>' +
                '    <div>' +
                '        <span>' +
                '            Attribut 3' +
                '        </span> :' +
                '        <span>' +
                '            ${attribut_3}' +
                '        </span>' +      
                '    </div>' +
                '</div>'				        
        }	
    ],	
	
    /**
     * Constant: RESULT_GF_DIALOG
     * {Boolean} Display the WFS getfeature in a dialog box.
     */
    RESULT_GF_DIALOG: false,
	
    /**
     * Constant: MAX_RESULT_GEONAMES
     * {Integer} The maximun result return by GeoNames.
     */			
    MAX_RESULT_GEONAMES: 6,
	
    /**
     * Constant: URL_HELP
     * {String} The URL for the help page.
     */		
    URL_HELP: "http://gesteau.eaufrance.fr/sites/all/modules/custom/geosie/aide/aide.html",
	
    /**
     * Constant: URL_CONTACT
     * {String} The URL for the contact page.
     */		
    URL_CONTACT: "http://gesteau.eaufrance.fr/contact"		
};