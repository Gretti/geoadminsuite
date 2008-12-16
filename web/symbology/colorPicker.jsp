<%/*Copyright (C) Gretti N'Guessan, Nicolas Ribot

This file is part of GeoAdminSuite

GeoAdminSuite is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GeoAdminSuite is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.*/%>

<script type="text/javascript">
    //Picker Dialog
    picker = new YAHOO.widget.ColorPicker(Ext.getCmp('colorpicker-container').body.id, {
        showhsvcontrols: true,
        showhexcontrols: false,
        showwebsafe:false,
        images: {
            PICKER_THUMB: "/scripts/colorpicker/assets/picker_thumb.png",
            HUE_THUMB: "/scripts/colorpicker/assets/hue_thumb.png"
        }
    });
    
    var onRgbChange = function(o) { 
	    /*o is an object
	        { newValue: (array of R, G, B values),
	          prevValue: (array of R, G, B values),
	          type: "rgbChange"
	         }
	    */
           //sets color to UI
            var strRgb = 'rgb(' + o.newValue + ')';
            var selectedLayer = Ext.getCmp('frmcolorSymbolPicker').form.getValues().sellayer;
            $(selectedLayer).style.backgroundColor = strRgb;
            $(selectedLayer).innerHTML = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
	} 
    picker.on("rgbChange", onRgbChange); 
</script>