/*******************************************************************************
Checks RGB syntax of a Ext TextField. If valid, colorize background of component
*******************************************************************************/
function checkRgb(f,e) {
    
    if(f.getValue().length == 0) {return;}
    
    var rgb = f.getValue().split(' ');
    if(rgb.length == 3 && (!isNaN(rgb[0]) && !isNaN(rgb[1]) && !isNaN(rgb[2]))) {
        var col = 'rgb(' + rgb[0] + ',' + rgb[1]  + ',' +  rgb[2] + ')';
        f.el.dom.style.backgroundColor = col;
    } else {
        Ext.MessageBox.show({
            title: 'Wrong Syntax',
            msg: 'Wrong RGB Syntax ...',
            buttons: Ext.MessageBox.OK,
            icon: Ext.MessageBox.WARNING
        });
    }
}

// selects all options in the given select element 
//@param el: the select element
function GASselectAll(sel) {
    if (!sel || (sel.type != "select-multiple" && sel.type != "select-one")) {
        return;
    }
    for (var i = 0; i < sel.options.length; i++) {
        sel.options[i].selected = true;
    }
}

//shows/hide the given div id
function GASshowDiv(id) {
    $(id).style.visibility = $(id).style.visibility == "hidden" ?
    "visible" : "hidden";
}

//removes the selected option(s) in the given select element
//@param el: the select element
function GASremoveElement(el) {
    if (!el || (el.type != "select-multiple" && el.type != "select-one")) {
        Ext.Msg.alert('bad element', el);
        return;
    }
    //var el = document.CartowebIniConfigurationForm.elements[id];
    for (var i = el.length - 1; i>=0; i--) {
        if (el.options[i].selected) {
          el.remove(i);
        }
    }
   el.selectedIndex = el.options.length-1;
}