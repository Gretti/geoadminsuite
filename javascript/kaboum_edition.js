/******************************************************************************
 FILENAME:  kaboum_edition.js
 AUTHOR:    Nicolas Ribot
 VERSION:   1.0
 DATE:      Juillet 2002
 PURPOSE:   isolate code to deal with kaboum object edition modes (edition, selection, etc...)
            This file is to link with the html page containing kaboum applet only if vector
            objects are intended to de handled by kaboum.
            if the variable kServletPath exists, edition commands are propagated to them
            in the geonline AS format: action=<validate or remove or selection>value=<full kaboum command>
            

    Author               Date           Changes made
   ------------------   ------------   -------------------------------
   Nico Ribot           22/juillet/02       Original definition

******************************************************************************/
// this var, defined in kaboum.js, allow kaboum commands concerning edition to be treated
// by this file's functions.
// Linking this file in the html page (AFTER kaboum.js) allows to process edition/selection commands
kEditionMode = true;

// the kaboum keyword to identify newly created objects
// set with the OBJECT_DEFAULT_ID kaboum parameter.
kNewObjectID = "NEW";
kUID = 0;
// true to delete entire record, false to set geo object to null
kHardRemove = false;
// a special treatment for GAS object adding
kGASNewObject = false;
/*
Public function.
Treats the OBJECT|... command sent by kaboum.
Syntax for commands is:
OBJECT|<classname>|<id>|<surface>|<wkt> or:
OBJECT|<classname>|<id>|<surface>|REMOVE
Parameters:
str: the kaboum command, as a string
*/
function kProcessObject(str) {
    var keyword = str.split("|");
    if (keyword[3] == "REMOVE") {
        // object removal
        if (kServletPath) {
            // DB (or file) removal: very hazardous: displays a confirmation window
            // only if removal concerns an existing object (id != "NEW")
            if (keyword[2] == kNewObjectID) {
                document.kaboum.standbyOff();
                document.kaboum.kaboumCommand("EDITION");
                return;
            }
            var txt = "Attention: supprimer un objet est une operation definitive.\n";
            txt += "Etes-vous sur de vouloir continuer ?";
            var ok = confirm(txt);
            if (ok) {
                var url = kServletPath + "?action=remove&value=" + escape(str) + "&hardremove=" + kHardRemove;
                openPopup(450, 450, url, "kremove");
            } else {
                // reloads the geometry in kaboum: needs to get WKT for the removed object
                document.kaboum.standbyOff();
                //document.kaboum.kaboumCommand("OBJECT|" + keyword[1] + "|" + keyword[2] + "|" + keyword[4]); 
            }
        }
        // release kaboum and reset edition mode
        document.kaboum.standbyOff();
        document.kaboum.kaboumCommand(str);
        document.kaboum.kaboumCommand("EDITION");
    } else {
        // object creation/modification
        if (!kServletPath) {
            //just reload the created object into kaboum
            document.kaboum.standbyOff();
            document.kaboum.kaboumCommand(keyword[0] + "|" + keyword[1] + "|" + keyword[2] + (kUID++) + "|" + keyword[4]);
            document.kaboum.kaboumCommand("EDITION");
        } else {
            // sends command to server to update database
            if (keyword[2] == kNewObjectID) {
                // creation.
                if (kGASNewObject) {
                    // GAS will receive the command
                    var url = "MC_kaboum_new_geometry.jsp?kaboumObj=" + str;
                    openPopup(800, 600, url, "kvalidation");
                }
            } else {
                // modification
                var url = kServletPath + "?action=validate&value=" + escape(str);
                openPopup(450, 450, url, "kvalidation");
                document.kaboum.standbyOff();
                document.kaboum.kaboumCommand("EDITION");
            }
        }
    }
}

/*
Public function.
Treats the TOPOLOGY command. Syntax is:
TOPOLOGY|<operation>|<kaboum_object>
where:
<operation> is one of the predefined operations (like UNION, INTERSECTION, DIFFERENCE, SYMDIFFERENCE)
(operations can be set by the kaboum initialisation parameter: "TOPOLOGY_OPERATORS"
<kaboum_object> is a comlete kaboum object: <classname>|<id>|<wkt>
*/
function kProcessTopology(str) {
    var keyword = str.split("|");
    if (keyword.length != 6) {
        alert("Commande non comprise: " + str);
        return;
    }
    if (kServletPath) {
        var url = kServletPath + "?action=topology&value=" + escape(str);
        kBuffer.sendData(url);
    }
}

/*
Public function.
Treats the ALERT|... command sent by kaboum.
Parameters:
str: the kaboum command, as a string
*/
function kProcessAlert(str) {
    var keyword = str.split("|");
    
    if (keyword[1] == "AUTOINTERSECT") {
        msg = "Il n'est pas possible de valider ce polygone:\nIl s'auto intersecte.";
        msg += "\n\nModifier ses points pour eviter l'autointersection";
        
    } else if (keyword[1] == "OVERLAP") {
        msg = "Il n'est pas possible de valider ce polygone:\nIl contient entierement un autre objet.";
        msg += "\nModifier ses points pour le rendre valide.";
    }
    alert(msg);
}

/*
Public function.
Treats the SELECTION|<list of sel. objs> command sent by kaboum.
Parameters:
str: the kaboum command, as a string
*/
function kProcessSelection(str) {
}

// special functions to deal with silent communication between client and server, from IBM tutorials
// works only if the following code is present in html page:
//<IFRAME name=myframe style="width:0;height:0">
//<LAYER name=myframe WIDTH=0 HEIGHT=0 visibility="hide">
//</LAYER>
//</IFRAME>

var kBuffer;

//call this function in page onload event handler
function initializeBuffer() {
  kBuffer = new exchanger();
}

//call this function when data needs to be sent to server
function sendDataToServer(url) {
  kBuffer.sendData(url);
}

//call this function to check what the server returns.
function showReturnData() {
//    var newObj = kBuffer.retrieveData("kComputedObject");
}
/***********************************************************
Code from IBM for silent comm...
***********************************************************/
//The browser detection function.
//This function can be used for other purposes also.

function UserAgent() 
{
  var b=navigator.appName.toUpperCase();

  if (b=="NETSCAPE") this.b="ns";
  else if (b=="MICROSOFT INTERNET EXPLORER") this.b="ie";
  else if (b=="OPERA") this.b="op";
  else this.b=b;

  this.version=navigator.appVersion;
  this.v=parseInt(this.version);

  this.ns=(this.b=="ns" && this.v>=4);
  this.ns4=(this.b=="ns" && this.v==4);
  this.ns5=(this.b=="ns" && this.v==5);

  this.ie=(this.b=="ie" && this.v>=4);
  this.ie4=(this.version.indexOf('MSIE 4')>0);
  this.ie5=(this.version.indexOf('MSIE 5')>0);
  this.ie55=(this.version.indexOf('MSIE 5.5')>0);
  this.ie6=(this.version.indexOf('MSIE 6')>0);

  this.op = (this.b=="op");
  this.op4 = (this.b=="op" && this.v==4);
  this.op5 = (this.b=="op" && this.v==5);
}

at=new UserAgent();

//if you want to create the frame or layer dynamically, do not
//specify a name, do something like this, new exchanger();

function exchanger(name)
{
  //hold the dynamically created iframe or layer
  this.lyr = null;

  //to remember if the iframe or layer is created dynamically.
  this.isDynamic = false;

  this.name=name||"";
  this.fakeid=0;

  if (name == null || name=="")
  {
    this.isDynamic = true;
    this.create();
  }
  else
  {
    this.name=name;
    if (at.ns4)
    {
      this.lyr = window.document.layers[this.name];
    }
  }
}

//this function should not be called directly
exchanger.prototype.create=function()
{
  if (at.ns4) 
  {
    this.lyr=new Layer(0);
    this.visibility = "hide";
  }
  else if (at.ie || at.ns5) 
  {
    this.lyr=document.createElement("IFRAME");
    this.lyr.width=0;
    this.lyr.height=0;
    this.lyr.marginWidth=0;
    this.lyr.marginHeight=0;
    this.lyr.frameBorder=0;
    this.lyr.style.visibility="hidden";
    this.lyr.style.position="absolute";
    this.lyr.src="";
    this.name="tongIFrame"+window.frames.length;  
    //this will make IE work.
    this.lyr.setAttribute("id",this.name);
    //this will make netscape work.
    this.lyr.setAttribute("name",this.name);
    document.body.appendChild(this.lyr);
  }
}

exchanger.prototype.sendData=function(url)
{
  this.fakeid += 1;
  var newurl = "";
  if (url.indexOf("?") >= 0)
    newurl = url + "&fakeId" + this.fakeid;
  else
    newurl = url + "?fakeId" + this.fakeid;

  if (this.isDynamic||at.ns4)
    this.lyr.src=newurl;
  else {
    if (at.ie || at.ns5 || at.op) {
      window.frames[this.name].document.location.replace(newurl);
    }
  }
}


exchanger.prototype.retrieveData=function(varName)
{
  if (at.ns4) 
  {
    return eval("this.lyr." + varName);
  }
  else if (at.ie || at.ns5 || at.op) 
  {
    return eval("window.frames['" + this.name + "']." + varName);
  }
}
