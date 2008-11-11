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