/******************************************************************************
 FILENAME:  kaboum.js
 AUTHOR:    Nicolas Ribot
 VERSION:   1.0
 DATE:      Juillet 2002
 PURPOSE:   merge and recode the JS part to control Kaboum Client, map interface.
            Adapted from previous theme.js, toolbar.jr, tools.js

    Author               Date           Changes made
   ------------------   ------------   -------------------------------
   Nico Ribot           17/juillet/01       Original definition
   Nico Ribot           02/sept/2008   I swear I made a lot of changes in this file !!

******************************************************************************/
// Global variables available for all functions in the document containing kaboum applet.

// the image type for toolbar and themes buttons (kNewButton object). All buttons must have the same type
// the image type must be supported by browsers. (use gif, png or jpg image types).
var kButtonType = ".gif"; //".png", ".jpg"
// the array of all buttons (toolbar) in the page
var kButtonArray = new Array();
// the array of all themes in the page
var kThemeArray = new Array();
// the array of all info object associated with themes
var kInfoArray = new Array();
// should map be reloaded each time a theme is selected/deselected
var kThemeAutoReload = true;
// global array for all button images. pre-loaded each time a new button is written
var kButtonImages = new Array();
// tells if the select JS object containing select type themes is written or not
// reset each time lastSelectTheme() is called, to allow a new select object to be created
var kThemeSelectWritten = false;
// the source of the image for the info object
var kInfoImgSrc = "images/infolayer.gif";
// ugly bool to indicate the first map is to be retrieved; store the orignal map extent
var kFirstMap = true;
// initial extent is stored here as the toolBar may not be initialized when the applet
// returns the first extent
var kInitialExtent = "";
// the current extent is always stored, now, for use in debug or specific JS code
var kCurrentExtent = "";
// list of active themes, used for debug mainly
var kActiveThemes = "";
// the path to the geonline application server (servlet) processing actions
var kServletPath = false;
// the boolean telling if this page will deal with editable objects in kaboum
// If yes, the kaboum_edition.js must be linked to the html page, after this page
// this boolean is set to true if the kaboum_edition.js is linked
var kEditionMode = false;
// the boolean telling if the page is used by the GAS. If so, a third js page, 
// kaboum_gas.js, is included in the map view and sets this value to true.
//Used to perform very specific treatments, like storing extent or layer visibility
var kGASIncluded = false;
// the scale textfield object, if it exists
var kScaleField = null;
// the xCoord textfield object, if it exists
var kxCoordField = null;
// the yCoord textfield object, if it exists
var kyCoordField = null;
// the print comment user typed in the print popup, to write in the print template page
var kPrintComment = "";
// the print title user typed in the print popup, to write in the print template page
var kPrintTitle = "";
// the print paper orientation: portrait = true, landscape = false;
var kPrintPortrait = false;
// the print legend: true to print the legend, false otherwise;
var kPrintLegend = false;
// the print properties page URL
var kPrintURL = "print.html";
// boolean telling if print should be made directly, without print properties page
var kPrintDirect = false;
// the help page URL
var kHelpURL = "help.htm";
// the query page URL
var kQueryURL = "query.htm";
// the kaboumResult event handler, allowing specific actions to be taken to overload
// default actions.
var kaboumResultHandler = null;
// the Kaboum user metadata object, allowing client to send parameters to server
// use USER_METADATA|value command to set the value, or use the kSetUserMetadata method
var kUserMetadata = "dummy";
//Debug mode (default enabled)
var kDebugMode = true;

/********************************************************************************
// global variable for browsers detection
********************************************************************************/
var isIE4 = false;
var isIE5 = false;
var isNS4 = false;
var isNS6 = false;
var isMac = false;
var range = "";
var isIE = false;
// only generation 4+ browsers are supported
if (navigator.appVersion.charAt(0) < "4") {
    document.location.href ="Cette_application_necessite_un_navigateur_de_generation_4_ou_superieure___Internet_explorer_ou_Netscape_Navigator";
}

if (navigator.appName.indexOf("Microsoft")!= -1)  {
    range = "all.";
    if (window.print) {isIE5 = true;}
    else {isIE4 = true;}
    isIE = true;
}

if (navigator.appName == "Netscape") {
    range = "";
    if (navigator.appVersion.charAt(0) == "6") {
        alert("Au moment ou je vous parle, Netscape 6 ne supporte pas LiveConnect, technologie de base de cette application. Nous vous conseillons IE4-5 ou NS4 pour visualiser cette application");
        isNS6 = true;
    }
    else {isNS4 = true;}
}

/********************************************************************************
// The main functions to communicate with kaboum (see kaboum api):
********************************************************************************/
// function to pass commands to kaboum. 
// if cmd is appended with "js_", command is not for kaboum, but for JS treatment:
// help, print, etc...
function kaboumCommand(cmd) {
    if (!document.kaboum) {
        if(kDebugMode) {
            alert("Commande: " + cmd + " impossible a envoyer.\nPas d'applet nommee kaboum definie dans la page...");
        }
        return;
    }
    if (cmd.substr(0, 3) == "JS_") {
        var keyword = cmd.split("|"); 
    	switch(keyword[0]) {
    	    case "JS_PRINT":
	            // opens the print popup: get the paper dimension from the user:
	            if (kPrintDirect) {
	                kComputePrintSize("a4", false, true, kPrintTitle, kPrintComment);
	            } else {
	                //the print.html page contains code to recall this document window with paper parameters !!
        	        openPopup(180, 240, kPrintURL, "print");
        	    }
    	    break;
    	    
    	    case "JS_HELP":
    	    	openPopup(530, 440, kHelpURL, "help");
    	    break;
    	    
    	    case "JS_QUERY":
    	    	openPopup(530, 440, kQueryURL, "requetes");
    	    break;
    	    
    	    case "JS_EDIT_POINT":
    	        // a demo command to set kaboum in point edition: call point class and then edition mode
    	        document.kaboum.kaboumCommand("OBJECT_ACTIVE_CLASS|DPOINT");
    	        document.kaboum.kaboumCommand("MULTIEDITION");
    	    break;
    	    
    	    case "JS_EDIT_LINE":
    	        // a demo command to set kaboum in line edition: call line def class and then edition mode
    	        document.kaboum.kaboumCommand("OBJECT_ACTIVE_CLASS|DLINE");
    	        document.kaboum.kaboumCommand("MULTIEDITION");
    	    break;
    	    
    	    case "JS_EDIT_POLYGON":
    	        // a demo command to set kaboum in pg edition: call pg def class and then edition mode
    	        document.kaboum.kaboumCommand("OBJECT_ACTIVE_CLASS|DPG");
    	        document.kaboum.kaboumCommand("MULTIEDITION");
    	    break;

            case "JS_DEBUG":
                kDebug();
            break;
    	}
        // treats non-kaboum commands
        return;
    }
    // treats now all command cases
    keyword = cmd.split("|");
    switch(keyword[0]) {
        // one-keyword actions are handled the same way by the target (Kaboum for the moment)
        case "ZOOMIN":
        case "ZOOMOUT":
        case "QUERY":
        case "CENTER":
        case "EDITION":
        case "MULTIEDITION":
        case "PAN":
        case "QUERY_LAYERS":
        case "LAYERS":
        case "DISTANCE":
        case "SURFACE":
        case "EXTENT":
        case "MULTISELECTION":
        case "SELECTION":
        case "OBJECT":
        case "PRINT_URL":
        case "GEOMETRY":
            document.kaboum.kaboumCommand(cmd);
        break;

        case "HOME":
            document.kaboum.kaboumCommand(kInitialExtent);
        break;

        case "SCALE":
            // check if given value is a valid scale
            var s = keyword[1];
            if (s.search(/^[0-9]+$/) == -1) {
                    //alert ("l'echelle ne doit contenir que des nombres entiers");
                    break;
            }
            // ask mapObject to get a new map
                    document.kaboum.kaboumCommand("SCALE|" + s);
        break;

        case "BACK":
            document.kaboum.kaboumCommand("HISTORY|BACK");
        break;

        case "FORWARD":
            document.kaboum.kaboumCommand("HISTORY|FORWARD");
        break;

        case "USER_METADATA":
            kUserMetadata = keyword[1];
            document.kaboum.kaboumCommand(cmd);
        break;

        default:
            // JGA: pass all commands to kaboum, if not supported
            document.kaboum.kaboumCommand(cmd);
            //alert("Commande '" + keyword[0] + "' non support�e.");
        break;
    }
}

// function to receive commands from kaboum AND kaboumette:
function kaboumResult(str) {
    var tmpStr = str.split("|");
    
    switch(tmpStr[0]) {
        case "CURRENT_OPMODE":
            kProcessCurrentOpMode(tmpStr[1]);
            break;
        case "EDITION_OPMODE":
            kProcessEditionOpMode(tmpStr[1]);
            break;
        case "EXTENT":
            // The first time the applet is loaded, it returns the map extent.
            // it will be used to make the home function.
            // the response's syntax is: EXTENT|MAP|<comma-separated list of extent coordinates>
            if (kFirstMap) {
                kInitialExtent = str;
                kFirstMap = false;
            }
            kCurrentExtent = tmpStr[2];
            // GAS specific treatement. Can be safely removed from here to lighten js code
            if (kGASIncluded) {kStoreExtent();}
            // nico, 07/03/2010: add a method to allow extent processing
            kProcessExtent(str);
            break;
            // REFERENCE: send with extend in case of kaboumette applet, as url in case of image reference
        case 'REFERENCE':
            if (document.kaboumette) {
                document.kaboumette.kaboumCommand(str);
            }
            if (document.images["kRefImage"]) {
                document.images["kRefImage"].src = tmpStr[1]
            }
            break;

        case 'QUERY':
            var tmpURL = tmpStr[1].substring(15)
            var tmpURL = tmpURL.substring(0,tmpURL.indexOf('&')) + '?' + tmpURL.substring(tmpURL.indexOf('&')+1)
            //alert (tmpURL);
            openPopup(400,600,tmpURL,'queryWindow');
            break;

        case "HISTORY": // kaboumette result to propagate to kaboum
            // transform HISTORY -> EXTENT to force Kaboum to display a new map
            var s = "EXTENT|" + tmpStr[1] + "|" + tmpStr[2];
            kaboumCommand(s);
            break;

            // valid only in kaboum Edition mode:
            // kaboum_edition.js file MUST be linked to the html page AFTER this file
        case "SELECTION":
                kProcessSelection(str);
            break;

            // alert is send in edition and when applet is loaded
        case "ALERT":
            kProcessAlert(str);
            break;
            // valid only in kaboum Edition mode:
            // kaboum_edition.js file MUST be linked to the html page AFTER this file
        case "TOPOLOGY":
            if (kEditionMode) {
                kProcessTopology(str);
            }
            break;
            // valid only in kaboum Edition mode:
            // kaboum_edition.js file MUST be linked to the html page AFTER this file
        case "OBJECT":
            if (kEditionMode) {
                kProcessObject(str);
            }
            break;

        case "SCALE":
            // Kaboum respond each time with the new map scale, the response's syntax is:
            // SCALE|<int_value>
            kUpdateScale(tmpStr[1]);
            break;

        case "DISTANCE":
        case "SURFACE":
            if (tmpStr[1].length > 0) {
                kProcessDistSurf(str);
            }
            break;

        case "PRINT_URL":
            // Kaboum has generated the url for the print template page
            //alert(tmpStr[1]);
            var w = window.open(tmpStr[1], "print2", "menubar=1,location=0,directories=0,status=1,scrollbars=1,resizable=1");
            w.focus();
            break;
            
        case "BOXSELECTION":
                //processBoxCmd(str);
            if (kaboumResultHandler != null) {
                kaboumResultHandler(str);
            }
            break;
        case "COORDINATE_STRING":
            kUpdateCoords(tmpStr[1]);
            break;
        // NRI: 21 avril 2004: deal with the new kaboumResult: in itemquerymap, if no result is found,
        // kaboum returns this command: ITEM_NQUERYMAP|NO_RESULT
        case "ITEM_NQUERYMAP":
            if (tmpStr[1] == "NO_RESULT") {
                kItemQueryNoResult();
            }
            break;
        case "GEOMETRY":
            kGeometry(str);
            break;
        default:
            // no log for this application (geosass)
            //log("kaboumResult: action not understood: " + str);
            break;
    }
}

/********************************************************************************
 methods for buttons:
********************************************************************************/

// public function to call to create a new button into the html page, returning it
// for further control
function kNewButton(img_src, alt_txt, mode, action, img_width, img_height) {
    // stores and loads the 3 images for this button
    
    // construct a new button add it to the global array and write its code
    var b = new kButton(kButtonArray.length, img_src, alt_txt, mode, action, img_width, img_height, false);
    kButtonArray[kButtonArray.length] = b;
    b.write();
    return b;
}

function kButton(but_idx, img_src, alt_txt, mode, action, img_width, img_height, is_theme) {
	this.index = but_idx;
	// the image name of a button is always: "img_" + name;
	this.img_src = img_src;
	this.alt_txt = alt_txt;
	this.mode = mode;
	this.action = action;
	this.img_width = img_width;
	this.img_height = img_height;
	this.img_name = "img_" + this.index;
	this.is_theme = is_theme;
	
	// methods:
	this.write = _write;
	// events
	this.over = _over;
	this.up = _up;
	this.down = _down;
	this.out = _out;
	this.click = _click;
	this.clear = _clear;
	
	this.clicked = false;
	// the clicked event is handled by the toolbar to clear other buttons
	// other events are handled directly by the buttons (only graphic modifs)
	// modal buttons don't need mouseDown and mouseUp events
	// sat 31 march: graphical improvement: no more hourglass on click (void 0 + return false in onclick)
	// and no more focus on IE4: this.blur (must test it on NS6)
	function _write() {
		var txt = "";
		
		if (this.mode == "modal") {
                    txt += '\n<A href=\"javascript:void 0\" onClick=\"kButtonArray[' + this.index + '].click();if(!document.layers) this.blur();return false;\"';
                    txt += ' onMouseOver=\"kButtonArray[' + this.index + '].over();return true;\"';
                    txt += ' onMouseOut=\"kButtonArray[' + this.index + '].out(); return true;\">\n<IMG ';
                    txt += ' border=0 name=\"' + this.img_name + '\" src=\"' + this.img_src + kButtonType + '\"';
                    txt += ' width=\"' + this.img_width + '\" height=\"' + this.img_height + '\" alt=\"' + this.alt_txt;
                    txt += '\"' + ' title=\"' + this.alt_txt + '\"></A>';
		}
		else {
                    txt += '\n<A href=\"javascript:void 0\" onClick=\"kButtonArray[' + this.index + '].click();if(!document.layers) this.blur();return false;\"';
                    txt += ' onMouseOver=\"kButtonArray[' + this.index + '].over(); return true;\"';
                    txt += ' onMouseOut=\"kButtonArray[' + this.index + '].out(); return true;\"';
                    txt += ' onMouseDown=\"kButtonArray[' + this.index + '].down(); if(!document.layers) this.blur();return true;\"';
                    txt += ' onMouseUp=\"kButtonArray[' + this.index + '].up(); return true;\">\n<IMG ';
                    txt += ' border=0 name=\"' + this.img_name + '\" src=\"' + this.img_src + kButtonType + '\"';
                    txt += ' width=\"' + this.img_width + '\" height=\"' + this.img_height + '\" alt=\"' + this.alt_txt;
                    txt += '\"' + ' title=\"' + this.alt_txt + '\"></A>';
		}
		document.write(txt);
	}
	
	// change button image to over mode (no effect on clicked buttons)
	function _over() {
		if (this.clicked) {
			return;
		}
		document.images[this.img_name].src = this.img_src + "_over" + kButtonType;
	}
	
	// restore the orignal image if button is not clicked (no effect on clicked buttons)
	function _out() {
		if (this.clicked) {
			return;
		}
		document.images[this.img_name].src = this.img_src + kButtonType;
	}
	
	// only works for action button: change the source to clicked image
	function _down() {
		document.images[this.img_name].src = this.img_src + "_clicked" + kButtonType;
	}

	// only works for action button: change the source to clicked image
	// restore the original image
	function _up() {
		document.images[this.img_name].src = this.img_src + kButtonType;
	}

	// click a button: on action button, only trigger the action
	// on modal buttons, changes the source and trigger the action
	function _click() {
            if (this.clicked && this.is_theme) {
                // a theme image is clicked, unclick it
                this.clear();
                return;
            }
            if (this.mode == "modal") {
                // clean other buttons
            for (var i = 0; i < kButtonArray.length; i++) {
                if (! kButtonArray[i].is_theme) {
                        kButtonArray[i].clear();
                    }
            }
                    this.clicked = true;
                    document.images[this.img_name].src = this.img_src + "_clicked" + kButtonType;
            }
            if (! this.is_theme) {
                kaboumCommand(this.action);
            }
	}

	// releases a clicked button
	function _clear() {
		this.clicked = false;
		document.images[this.img_name].src = this.img_src + kButtonType;
	}
}

/********************************************************************************
 methods for themes:
********************************************************************************/

/*
 public function to call to create and return a new theme into the html page.
public methods can then be used by scripting.
 parameters:
 display_name: a string representing the theme's name that will appear in the html page
               or  the alt/title text displayed when mouse is over the image in case of IMAGE type
 visible: a boolean value telling if this theme should be default selected or not
 info: a boolean value telling if an info object should be added behind this theme 
 css_style: string, the name of the CSS selector to apply to this theme (can be null)
           this will be put in a <span> tag
 img_src: String, the image URL, without extension (see kButtonType)
*/

function kNewTheme(name, display_name, visible, info, type, css_style, img_src) {
    // creates a new theme and adds it to the global array of themes
    var t = new kTheme(kThemeArray.length, name, display_name, visible, info, type, css_style, img_src);
    kThemeArray[kThemeArray.length] = t;
    
    t.write();
    return t;
}

/*
Public function: must be called each time a theme whose type is select is the last one in the select.
Allow js code to close the select tag
*/
function kLastSelectTheme() {
    document.write("</SELECT></span>");
    kThemeSelectWritten = false;
}

/**
a public function to select all checkbox themes at once
Cycle through all checkbox object and check those whose ktype is ms_layer 
(property added by this api)
Then cycle through all themes to set them to visible
CAUTION: using this method in conjunction with theme autoreload can crash the applet
*/
function kSelectAllThemes() {
    // unplug the autoreload to avoid flickering
    var tar = kThemeAutoReload;
    
    if (kThemeAutoReload) {
        kThemeAutoReload = false;
    }
    for (var i = 0; i < document.forms.length; i++) {
        for (var j = 0; j < document.forms[i].elements.length; j++) {
            if (document.forms[i].elements[j].type == "checkbox" && 
                document.forms[i].elements[j].name.indexOf("ktheme") != -1) {
                    document.forms[i].elements[j].checked = true;
            }
        }
    }
    for (var i = 0; i < kThemeArray.length; i++) {
        if (kThemeArray[i].type == "CHECKBOX" || kThemeArray[i].type == "IMAGE") {
            kThemeArray[i].visible = false;
            kThemeArray[i].swap();
            if (kThemeArray[i].type == "IMAGE") {
                kThemeArray[i].kButton.clear();
                kThemeArray[i].kButton.click();
            }
        }
    }
    kRefreshThemes();
    
    //replug the autoreload if it existed
    kThemeAutoReload = tar;
}
/*
Private function.
Event for each select object containing themes.
Reflects the theme status for each theme of the given select
parameters:
sel: select object that has changed
*/
function kUpdateThemesStatus(sel) {
    // should improve this double loop, maybe with a name-index array for SELECT themes
    for (var k = 0; k < sel.options.length; k++) {
        for (var i = 0; i < kThemeArray.length; i++) {
            if (kThemeArray[i].name == sel.options[k].value) {
                kThemeArray[i].visible = sel.options[k].selected;
            }
        }
    }
    if (kThemeAutoReload) {
        kRefreshThemes();
    }
}

/*
 public function to call to send to kaboum the list of active theme
 can be used with a link or button to make themes refreshed when link or button is clicked
 Cycle through themes array and get names of active ones.
*/

function kRefreshThemes() {
    //NRI: modified to do nothing if there are no layers:
    if (kThemeArray == null || kThemeArray.length == 0) {
        return;
    }

    var l = "LAYERS|";
    for (var i = 0; i < kThemeArray.length; i++) {
        if (kThemeArray[i].visible) {
            l += kThemeArray[i].name;
            
            if (i < kThemeArray.length - 1) {
                l += ",";
            }
        }
    }
    kActiveThemes = l;
    // send command to kaboum, clean the command if no layer
    if (l == "LAYERS|") {
        l += "o";
    }
    kaboumCommand(l);
}

/*
private object representing a geographic theme.
kThemeArray.
parameters:
index: int, the index of this theme in the global kThemeArray array
name: the name of the Mapserver layer represented by this theme.
display_name: string, the name of this theme as written in the html page,
              or the the alt/title text displayed when mouse is over the image, in case of IMAGE theme.
visible: boolean, is this theme default visible ?
info: boolean, is an info image should be added to this theme ? (only works for checkbox themes)
type: string, the HTML type of this theme: "CHECKBOX" or "SELECT"
css_style: string, the name of the CSS selector to apply to this theme (can be null)
           this will be put in a <span> tag
img_src: String, the image URL, without extension (see kButtonType)
*/
function kTheme(index, name, display_name, visible, info, type, css_style, img_src) {
    // properties
    this.index = index;
    this.name = name;
    this.display_name = display_name;
    this.visible = visible;
    this.info = info;
    this.type = type;
    this.css_style = css_style;
    this.img_src = img_src;
    // the index of the info object associated with this object, in the kInfoArray variable
    this.info_index = -1;
    // in case of IMAGE theme selector, this is a reference to the kButton used to 
    // control theme visibility
    this.kButton = "";

    this.writeInfo = _writeInfo;
    this.write = _write;
    this.swap = _swap;

    function _writeInfo() {
        if (this.info) {
            // stores the info index for this theme object
            this.info_index = kInfoArray.length;
            
            var i = new kInfoTheme(kInfoArray.length, this.index, this.css_style);
            kInfoArray[kInfoArray.length] = i;
            i.write();
            //nri 17 nov 2004: if theme is not visible, its info status is disabled by default
            if (!visible) {
                i.swap();
            }
        } else {
            // write some space to make themes align
            document.write("&nbsp;&nbsp;");
        }
    }

    function _write() {
        var t = "";

        if (this.type == "CHECKBOX") {
            this.writeInfo();
            // writes only the input part of the form and the associated text, not the form itself
            // all checkbox objects names will be ktheme + their index (to get a unique name)
            t += "<span class=\"" + this.css_style + "\"><input type=\"checkbox\" name=\"ktheme" + this.index + "\" ";
            t += (this.visible ? "CHECKED" : "") + " onClick='!this.checked;kThemeArray[";
            t += this.index + "].swap()'>";
            t += this.display_name + "</span>\n";
        }
        else if (this.type == "SELECT") {
            if (!kThemeSelectWritten) {
                t += "<span class=\"" + this.css_style + "\"><SELECT onchange=\"kUpdateThemesStatus(this);\">";
                kThemeSelectWritten = true;
            }
            t += "<option " + (this.visible ? "SELECTED" : "");
            t += " value=\"" + this.name + "\">" + this.display_name + "</option>\n";
        }
        else if (this.type == "IMAGE") {
            this.writeInfo();

            // construct a new button add it to the global array.
            // this button will not be written by its inner method to allow us to control the click event 
            this.kButton = new kButton(kButtonArray.length, this.img_src, this.display_name, "modal", "", -1, -1, true);
            this.kButton.clicked = this.visible;
            kButtonArray[kButtonArray.length] = this.kButton;
            var bIndex = kButtonArray.length - 1;

            var clicked = (this.visible ? "_clicked" : "");

            t += '\n<A href=\"javascript:void 0\" onClick=\"kButtonArray[' + bIndex + '].click()';
            t += ';kThemeArray[' + this.index + '].swap();'
            t += 'if(!document.layers) this.blur();return false;\"';
            t += ' onMouseOver=\"kButtonArray[' + bIndex + '].over(); return true;\"';
            t += ' onMouseOut=\"kButtonArray[' + bIndex + '].out(); return true;\"';
            t += ' >\n<IMG ';
            t += ' border=0 name=\"' + this.kButton.img_name + '\" src=\"' + this.img_src + clicked + kButtonType + '\"';
            t += 'alt=\"' + this.display_name + '\"' + ' title=\"' + this.display_name + '\"></A>';
        }
        else {
            alert("Bad type for theme: " + this.type + "\nSupported values are CHECKBOX, SELECT or IMAGE");
        }
        document.write(t);
    }

    function _swap() {
        this.visible = !this.visible;
        // in case of directRefresh, the theme itself asks the themeManager to refresh the map
        if (kThemeAutoReload) {
            kRefreshThemes();
        }
        // look if an info object should be set accordingly to this theme, as theme and its info are linked
        if (this.info_index != -1) {
            kInfoArray[this.info_index].select(this.visible);
        }
        // GAS specific treatement. Can be safely removed from here to lighten js code
        if (kGASIncluded) {
            kStoreLayer(this.index, this.visible);
        }
    }
}

/********************************************************************************
 methods for infos:
********************************************************************************/
/**
a public function to select all info at once
*/
function kSelectAllInfo() {
    for (var i = 0; i < kInfoArray.length; i++) {
        kInfoArray[i].select(true);
    }
    kRefreshInfo();
}

/*
Private function to refresh info list
*/
function kRefreshInfo() {
    //NRI: modified to avoid sending command if there are no query layers:
    if (kInfoArray == null || kInfoArray.length == 0) {
        return;
    }
    var list = "QUERY_LAYERS|";
    for (var i=0; i < kInfoArray.length; i++) {
            if (kInfoArray[i].enabled) {
                    list += kThemeArray[kInfoArray[i].theme_index].name + ",";
            }
    }
    kaboumCommand(list);
}

/**
a private info object for a checkbox theme
*/
function kInfoTheme(index, theme_index, css_style) {
	this.enabled = true;
	this.theme_index = theme_index;
	this.index = index;
	this.img_name = "imginfo_" + index;
	this.css_style = css_style;
	
	this.src = kInfoImgSrc;

	// method to swap image source:
	this.swap = _swap;
	this.write = _write;
	// allow to select or deselect an info object according to the boolean value
	this.select = _select;

	function _select(on) {
		img = eval("document." + this.img_name);
		this.enabled = on;
		img.src = on ? this.src.substring(0, this.src.length-4) + "_clicked" + kButtonType : this.src;
                //nri. 16 mars 2005: added a call to kaboum to give it the list of info layers each
                // time an info theme is selected
                if(document.kaboum){
                    kRefreshInfo();
                }
	}

	function _swap() {
		var img = eval("document." + this.img_name);
		this.enabled = !this.enabled;
		img.src = (this.enabled) ? this.src.substring(0, this.src.length-4) + "_clicked" + kButtonType : this.src;
		if(document.kaboum){
                    kRefreshInfo();
                }
	}

	// write the html code in the document
	function _write() {
		var txt = "";
		txt = "<span class='" + this.css_style + "'><a href='void 0' onClick='kInfoArray[";
		txt += this.index + "].swap()";
		txt += ";return false;' onMouseOver='window.status=\"\";return true'>";
		txt += "<img border=0 name='" + this.img_name;
		txt += "' src='";

		if (this.enabled) {
			// display the clicked image
			txt += this.src.substring(0, this.src.length-4) + "_clicked" + kButtonType;
		}
		else {
			txt += this.src;
		}

		txt += "'></a></span>";
		document.write(txt);
		document.close();
	}
}

/*
Public function to display a window containing MS legend
Rajout gng mapext pour �viter l'affichage partiel des donn�es dans le cas d'utilisation de MAXSCALE dans le map
*/
function kGetLegend() {
	var u = document.kaboum.mapServerTools.mapserverURL + "?map=" + document.kaboum.mapServerTools.mapPath;
	// trick by Jrom
	u += "&mode=legend&scale=1&mapext=0 0 1 1";
	// loop to take all the Themes
	for (var i = 0; i < kThemeArray.length; i++) {
	    u += "&layer="+ kThemeArray[i].name;
	}
    openPopup(250, 350, u, "legend");
}
/********************************************************************************
Scale functions:
********************************************************************************/

/*
Public function to create a input text field containing the scale from kaboum
*/
function kNewScale(scaleText, style) {
	var txt = "";
	
	txt += "<span class='" + style + "'>";
	txt += scaleText + "<input type=\"text\" name=\"kscaletext\" size=\"9\" onKeyPress=\"return kValidateScale(event)\" class=\"" + style + "\">";
	txt += "</span>";
	
	document.write(txt);
	document.close();
}

/********************************************************************************
Coordinates functions:(GNG)
********************************************************************************/

/*
Public function to create input text fields containing the coordinates from kaboum
*/
function kNewXCoordinate(xCoordText, style, size) {
	var txt = "";
	
	txt += "<span class='" + style + "'>";
	txt += xCoordText + "<input type=\"text\" name=\"kxCoord\" size=\"" + size + "\" class=\"" + style + "\">";
	txt += "</span>";

	document.write(txt);
	document.close();
}

function kNewYCoordinate(yCoordText,style, size) {
	var txt = "";

	txt += "<span class='" + style + "'>";
	txt += yCoordText + "<input type=\"text\" name=\"kyCoord\" size=\"" + size + "\" class=\"" + style + "\">";
	txt += "</span>";
	
	document.write(txt);
	document.close();
}

/*
Private function to deal with scale validation (enter in the textfield)
*/
function kValidateScale(e) {
    var charCode = (navigator.appName == "Netscape") ? e.which : e.keyCode;
    if (charCode == 13) {
        // find the scale value and send command to kaboumCommand
        kaboumCommand("SCALE|" + kScaleField.value);
       return false;
    }
    return true;
}

/*
 Private function. 
 Refresh the scale value after Kaboum send a SCALE command (zoom, pan, center)
*/
function kUpdateScale(newScale) {
    if (!kScaleField) {
        // search for it in the document
         for (var i = 0; i < document.forms.length; i++) {
            for (var j = 0; j < document.forms[i].elements.length; j++) {
                if (document.forms[i].elements[j].name == "kscaletext") {
                    kScaleField = document.forms[i].elements[j];
                }
            }
        }
    }
    if (kScaleField) {
        kScaleField.value = newScale;
    }
}

/*
 Private function. 
 Refresh the x and y values when receiving a COORDINATESTRING result
*/
function kUpdateCoords(newCoords) {
    var newXCoord = "";
    var newYCoord = "";
    
    var arrayParams = newCoords.split(":");
    if (arrayParams == null || arrayParams.length == 0){
        return;
    }
    
    var tmpStr = arrayParams[arrayParams.length - 1]

    var arrayCoords = tmpStr.split(",")
    if (arrayCoords == null || arrayCoords.length < 2){
        return;
    }

    newXCoord = arrayCoords[0];
    newYCoord = arrayCoords[1];

    if (!kxCoordField) {
        // search for it in the document
         for (var i = 0; i < document.forms.length; i++) {
            for (var j = 0; j < document.forms[i].elements.length; j++) {
                if (document.forms[i].elements[j].name == "kxCoord") {
                    kxCoordField = document.forms[i].elements[j];
                }
            }
        }
    }

    if (kxCoordField) {
        kxCoordField.value = newXCoord;
    }

    if (!kyCoordField) {
        // search for it in the document
         for (var i = 0; i < document.forms.length; i++) {
            for (var j = 0; j < document.forms[i].elements.length; j++) {
                if (document.forms[i].elements[j].name == "kyCoord") {
                    kyCoordField = document.forms[i].elements[j];
                }
            }
        }
    }
    if (kyCoordField) {
        kyCoordField.value = newYCoord;
    }
}

/********************************************************************************
Print functions:
********************************************************************************/
/*
Private function
compute the image size to fit in the given page. Keeps image ratio
*/
function kComputePrintSize(size, portrait, legend, title, text) {
    // set global variables to allow print_tpl to call them to set the page layout
    kPrintTitle = title;
    kPrintComment = text;
    kPrintPortrait = portrait;
    kPrintLegend = legend;
    // the number of pixel/cm for 72 dpi;�
    var pxcm = 37.735;
    // the image ration
    var ratio = document.kaboum.screenSize.width/document.kaboum.screenSize.height;
    var margin = 2.3;
    var lineHeight = 0.6;
    var w=0, h=0;
    // the max size supported by MS
    var max_mssize = 999;
    
    switch(size) {
        case "a4":
            w=21.0; h=29.7;
        break;
        case "a3":
            w=29.7; h=42;
        break;
        case "a5":
            w=14.85; h=21;
        break;
        case "letter":
            w=21.59; h=27.94;
        break;
        default: // is a4
            w=21.0; h=29.7;
        break;
    }
    if (!portrait) {
        // reverse dimension and ratio if landscape
        var tmp = w;w = h;h = tmp;
    }
    
    // see how many lines in title and comment to remove them from image area
    h = title != "" ? h-lineHeight : h;
    
    var cpt = 0, pos = 0, z;
    while ( (z = text.indexOf("<br>", pos)) != -1) {
        cpt++;
        pos = z + 4;
    }
    
    h -= cpt * lineHeight;
    
    // w and h are now the image area, in pix, for the given page/orientation        
    // compute the image area, in pix:
    w = (w - 2*margin) * pxcm;
    h = (h -2*margin) * pxcm;
    
    // adapt the max size to mapServer parameter: 1024 pix
    w = w > max_mssize ? max_mssize : w;
    h = h > max_mssize ? max_mssize : h;
    
    // and then, scale image to match initial map ratio.
    if (w > h) {
        w = (h * document.kaboum.screenSize.width) / document.kaboum.screenSize.height;
    } else {
        h = (w * document.kaboum.screenSize.height) / document.kaboum.screenSize.width;
    }
    // kaboum will respond PRINT_URL|<URL> after this command.
    kaboumCommand("PRINT_URL|" + parseInt(w) + "," + parseInt(h));
}

/********************************************************************************
Misc. functions:
********************************************************************************/
/*
Public function
debug function to log a message in the console: netscape only.
 */
function log(txt) {
	if (isNS4 || isNS6) java.lang.System.out.println(txt);
	if (document.all) window.status = txt;
}

/*
Public function
Opens a debug window: mapserver URL + produced map
*/
function kDebug() {
    var u = document.kaboum.mapServerTools.mapserverURL + "?map=" + document.kaboum.mapServerTools.mapPath;
    u += "&mode=map";
    var toto = kActiveThemes.split("|");
    if (toto == null || toto.length == 0){
        return;
    }
    toto = toto[1].split(",");
    // loop to take all the Themes
    for (var i = 0; i < toto.length; i++) {
      		u += "&layer="+ toto[i];
    }
    // nri: 19 mars 2005: add current extent to the MS query, to reflect exact kaboum map command
    toto = kCurrentExtent.replace(/;/g, " ");
    toto = toto.replace(/,/g, " ");
    u += "&mapext=" + toto;
    var sr = "";
    if (document.layers) sr = "src=\"e\"";
  
    var w = window.open("", "debug", "width=" + (document.kaboum.screenSize.width +20) + ",height=" + (document.kaboum.screenSize.height +100)+",menubar=1,resizable=1");
    w.document.write("<html><frameset onload='window.topFrame.document.write(\"MS URL:<br><font face=mono>" + u + "</font><br>\");' rows=\"120,*\" >");
    w.document.write("<frame name=\"topFrame\" " + sr + ">");
    w.document.write("<frame name=\"mainFrame\" src=\"" + u + "\">");
    w.document.write("</frameset>");
    w.document.write("</html>");
    w.document.close();
    w.focus();
}

/*
Private function
*/
function openPopup(thisWidth,thisHeight,thisDocument,thisWindowName) {
	var windowtools = "toolbar=0,location=0,directories=0,status=0,scrollbars=1,resizable=1,alwaysRaised=1,status=1";
	var w = window.open(thisDocument, thisWindowName, "width=" + thisWidth + ",height=" + thisHeight + "," + windowtools);
	w.focus();
	return w;
}
/*
Private function
*/

function showProps(o) {
	var result = ""
	count = 0
	for (var i in o) {
		result += o + "." + i + "=" + o[i] + "\n"
		count++
		if (count == 25) {
			alert(result)
			result = ""
			count = 0
		}
	}
	alert(result)
}

/********************************************************************************
// methods for geometry mode
********************************************************************************/
/*
Public function: deals with GEOMETRY Kaboum command.
Users can overwrite this method to provide a custom mechanism
When a GEOMETRY... command is sent by Kaboum.
See Kaboum API for the complete list of GEOMETRY commands.
*/
function kGeometry(str) {
    //alert(str);
    document.kaboum.standbyOff();
}

/********************************************************************************
// methods for selection mode
********************************************************************************/
/*
Public function: deals with SELECTION Kaboum command.
Users can overwrite this method to provide a custom mechanism
When a SELECTION|ID... command is sent by Kaboum.
See Kaboum API for the complete list of SELECTION commands.
*/
function kProcessSelection(str) {
    //alert(str);
}
/********************************************************************************
// methods for surface or distance modes
********************************************************************************/
/*
Public function: deals with DISTANCE and SURFACE Kaboum command.
Users can overwrite this method to provide a custom mechanism
When a DISTANCE|<value> or SURFACE|<value> command is sent by Kaboum.
*/
function kProcessDistSurf(str) {
    alert(str);
}

/********************************************************************************
// methods for extent mode
********************************************************************************/
/*
Public function: deals with EXTENT Kaboum command.
Users can overwrite this method to provide a custom mechanism
When a EXTENT|<value> command is sent by Kaboum.
*/
function kProcessExtent(str) {
    alert(str);
}


/********************************************************************************
// methods for item query map mode
********************************************************************************/
// public function: deals with ITEM_QUERYNMAP Kaboum command
// this function can be overloaded by clients to provide a custom mechanism
// when a query mode did not provide any result
function kItemQueryNoResult () {
    alert("Mapserver did not return any result for the query.");
}

//
// this function can be overloaded by clients to provide a custom mechanism
//Treats the ALERT|... command sent by kaboum.
//Parameters:
//str: the kaboum command, as a string
function kProcessAlert(str) {
    var keyword = str.split("|");
    var msg = "";

    if (keyword[1] == "AUTOINTERSECT") {
        msg = "Il n'est pas possible de valider ce polygone:\nIl s'auto intersecte.";
        msg += "\n\nModifier ses points pour eviter l'autointersection";
        
    } else if (keyword[1] == "OVERLAP") {
        msg = "Il n'est pas possible de valider ce polygone:\nIl contient entierement un autre objet.";
        msg += "\nModifier ses points pour le rendre valide.";
    } else if (keyword[1] == "KABOUM_IS_LOADED") {
        // do nothing, as it is a normal behaviour
        return;
    } else {
        msg = keyword[1];
    }

    alert(msg);
}

// deals with the CURRENT_OPMODE kaboumResult:
// this command is sent by kaboum when it receives a CURRENT_OPMODE command.
// The syntax of the returned command is :
// CURRENT_OPMODE|<opModeName>
// where <opModeName> is the name of the current opMode in Kaboum, or "null" if there
// is no current OpMode
function kProcessCurrentOpMode(str) {
    alert(str);
}

// deals with the EDITION_OPMODE kaboumResult:
// this command is sent by kaboum when it receives a EDITION_OPMODE command.
// The syntax of the returned command is :
// EDITION_OPMODE|<opModeName>
// where <opModeName> is the name of the opMode in edition mode, or "null" if there
// is no current edition mode
function kProcessEditionOpMode(str) {
    alert(str);
}

/**
 * Resizes the applet by setting its new size and sending a CANVAS_SIZE command
 **/
function kResizeApplet(width, height) {
    document.kaboum.width = width;
    document.kaboum.height = height;
    kaboumCommand("CANVAS_SIZE|" + width + " " + height);
    kaboumCommand("REFRESH");
}
// sets the value for the current User Metadata object. after this call, both Kaboum
// and JS variable kUserMetadata contains the same value. this value will be passed
// to the server at each request
//@param userMD the string representing the value to pass. Do not pass an object
// to this method. Only strings
function kSetUserMetadata(userMD) {
    kaboumCommand("USER_METADATA|" + userMD);
}
/********************************************************************************
First JS execution for the current page:
********************************************************************************/

/*
Public function used when the page is loaded.
If client overloads the onLoad event handler a function, it must call kInit() in the 
first line of this function
*/
function kInit() {
    kRefreshThemes();
    kRefreshInfo();
    // initialize defautl kaboum metadata
    kSetUserMetadata(kUserMetadata);
    
    // the silent client-server com initialization
    if (kEditionMode) {
        initializeBuffer();
    }
//    alert(document.kaboum.screenSize.width)
//    alert(document.kaboum.screenSize.height)
//    alert(document.kaboum.mapServerTools.mapPath)
//    alert(document.kaboum.mapServerTools.mapserverURL)
}

window.onload = kInit;
