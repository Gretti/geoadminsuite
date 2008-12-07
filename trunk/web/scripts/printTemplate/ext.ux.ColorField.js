Ext.namespace('Ext.ux');
Ext.ux.ColorField = function(config){
    Ext.ux.ColorField.superclass.constructor.call(this, config);
    
    this.addEvents({'colorChange': true});
    
    this.on('change', function(m, value){
        if(this.menu){
            this.menu.palette.value = value;
            this.menu.palette.drawGradient();
        }
        this.el.setStyle('background', value);
        this.detectFontColor();
        this.fireEvent('colorChange', m, value);
    }, this);
};
Ext.extend(Ext.ux.ColorField, Ext.form.TriggerField, {
    
    triggerClass : 'x-form-color-trigger',
    
    // private
    menuListeners : {
        colorChange: function(m, d){
            this.setValue(d);
            this.el.setStyle('background', d);
            this.detectFontColor();
            this.fireEvent('colorChange', m, d);
        },
        select: function(m, d){
            this.setValue(d);
        },
        show : function(){ // retain focus styling
            this.onFocus();
        },
        hide : function(){
            this.focus.defer(10, this);
            var ml = this.menuListeners;
            this.menu.un("colorChange", ml.colorChange, this);
            this.menu.un("select", ml.select,  this);
            this.menu.un("show", ml.show,  this);
            this.menu.un("hide", ml.hide,  this);
        }
    },
    
    onRender : function(ct, position){
        Ext.ux.ColorField.superclass.onRender.call(this, ct, position);
        this.fireEvent('change', this, this.value);  
    },
    
    // private
    // Implements the default empty TriggerField.onTriggerClick function to display the ColorPicker
    onTriggerClick : function(){
        if(this.disabled){
            return;
        }
        if(this.menu == null){
            this.menu = new Ext.ux.ColorMenu({
                width: 200,
                hideOnClick: false,
                value: this.value
            });
        }
        this.menu.on(Ext.apply({}, this.menuListeners, {
            scope:this
        }));
        this.menu.show(this.el);
    },
    
    // private
    // Detects whether the font color should be white or black, according to the
    // current color of the background
    detectFontColor : function(){
        var value;
        if(!this.menu || !this.menu.palette.rawValue){
            var h2d = function(d){ return parseInt(d, 16); }
            value = [
                h2d(this.value.slice(1, 3)),
                h2d(this.value.slice(3, 5)),
                h2d(this.value.slice(5))
            ];
        }else
            value = this.menu.palette.rawValue;
        var avg = (value[0] + value[1] + value[2]) / 3;
        this.el.setStyle('color', (avg > 128) ? '#000' : '#FFF');
    }
    
});

Ext.ux.ColorMenu = function(config){
    Ext.ux.ColorMenu.superclass.constructor.call(this, config);
    this.plain = true;
    var ci = new Ext.ux.ColorItem(config);
    this.add(ci);
    /**
     * The {@link Ext.ux.ColorPicker} instance for this ColorMenu
     * @type ColorPicker
     */
    this.palette = ci.palette;
    /**
     * @event select
     * @param {ColorPicker} palette
     * @param {String} color
     */
    this.relayEvents(ci, ["select", "colorChange"]);
};
Ext.extend(Ext.ux.ColorMenu, Ext.menu.Menu);

Ext.ux.ColorItem = function(config){
    Ext.ux.ColorItem.superclass.constructor.call(this, new Ext.ux.ColorPicker(config), config);
    this.palette = this.component;
    this.relayEvents(this.palette, ["select", "colorChange"]);
    if(this.selectHandler){
        this.on('select', this.selectHandler, this.scope);
    }
};
Ext.extend(Ext.ux.ColorItem, Ext.menu.Adapter);

Ext.ux.ColorPicker = function(config){
    Ext.ux.ColorPicker.superclass.constructor.call(this, config);
    this.addEvents(
        /**
	     * @event select
	     * Fires when a color is selected
	     * @param {ColorPalette} this
	     * @param {String} color The 6-digit color hex code (without the # symbol)
	     */
        'select'
    );
    
    this.addEvents({'colorChange': true});
    if(!this.value)
        this.value = this.defaultValue;

    if(this.handler){
        this.on("select", this.handler, this.scope, true);
        this.on("colorChange", this.handler, this.scope, true);
    }
};
Ext.extend(Ext.ux.ColorPicker, Ext.ColorPalette, {
    canvasSupported: true,
    itemCls: 'x-color-picker',
    defaultValue: "#0000FF",
    imagePath:"images/",
    // private
    onRender : function(container, position){
        if(!this.value)
            this.value = this.defaultValue;
        var el = document.createElement("div");
        el.className = this.itemCls;
        container.dom.insertBefore(el, position);
        this.canvasdiv = Ext.get(el).createChild({
            tag: 'div'
        });
        this.wheel = this.canvasdiv.dom.appendChild(document.createElement("canvas"));
        this.wheel.setAttribute('width', '200');
        this.wheel.setAttribute('height', '200');
        this.wheel.setAttribute('class', 'x-color-picker-wheel');
        
        if(!this.wheel.getContext || !this.wheel.getContext('2d').getImageData){
            this.canvasSupported = false;
            this.itemCls = 'x-color-palette';
            while(container.dom.firstChild){ container.dom.removeChild(container.dom.firstChild); }
            Ext.ux.ColorPicker.superclass.onRender.call(this, container, position);
            return;
        }
        
        /* Draw the wheel image onto the container */
        this.wheelImage = new Image();
        this.wheelImage.onload = function(){
            this.wheel.getContext('2d').drawImage(this.wheelImage, 0, 0);
            this.drawGradient();
        }.createDelegate(this);
        //finds out current directory to point images
        this.wheelImage.src = (colorFieldImagePath != null ? colorFieldImagePath : this.imagePath) + 'wheel.png';
        
        Ext.get(this.wheel).on('click', this.select, this);
        
        this.el = Ext.get(el);
    },
    
    // private
    afterRender : function(){
        Ext.ColorPalette.superclass.afterRender.call(this);
        if(!this.canvasSupported) return;
        /* Fire selection events on drag */
        var t = new Ext.dd.DragDrop(this.wheel);
        var self = this;
        t.onDrag = function(e, t){
            self.select(e, this.DDM.currentTarget);
        };
    },
    
    select : function(e, t){
        if(!this.canvasSupported){
            this.value = e;
            Ext.ux.ColorPicker.superclass.select.call(this, e);
            this.fireEvent('colorChange', this, '#'+this.value); 
            return;
        }
        var context = this.wheel.getContext('2d');
        var coords = [
            e.xy[0] - Ext.get(t).getLeft(),
            e.xy[1] - Ext.get(t).getTop()
        ];
        
        try{
            var data = context.getImageData(coords[0], coords[1], 1, 1);
        }catch(e){ return; } // The user selected an area outside the <canvas>
        
        // Disallow selecting transparent regions
        if(data.data[3] == 0){
            var context = this.gradient.getContext('2d');
            var data = context.getImageData(coords[0], coords[1], 1, 1);
            if(data.data[3] == 0) return;
            
            this.rawValue = data.data;
            this.value = this.hexValue(data.data[0], data.data[1], data.data[2]);
            this.fireEvent('colorChange', this, this.value);
        }else{
            this.rawValue = data.data;
            this.value = this.hexValue(data.data[0], data.data[1], data.data[2]);
            this.drawGradient();
            this.fireEvent('colorChange', this, this.value);
        }
    },
    
    // private
    drawGradient : function(){
        if(!this.gradient){
            this.gradient = this.canvasdiv.dom.appendChild(document.createElement("canvas"));
            this.gradient.setAttribute('width', '200');
            this.gradient.setAttribute('height', '200');
            this.gradient.setAttribute('class', 'x-color-picker-gradient');
            if(typeof G_vmlCanvasManager != 'undefined') 
                this.gradient = G_vmlCanvasManager.initElement(this.gradient);
            Ext.get(this.gradient).on('click', this.select, this);
        }
        var context = this.gradient.getContext('2d');
        var center = [97.5, 98];
        
        // Clear the canvas first
        context.clearRect(0, 0, this.gradient.width, this.gradient.height)
        
        context.beginPath();
        context.fillStyle = this.value;
        context.strokeStyle = this.value;
        context.arc(center[0], center[0], 65, 0, 2*Math.PI, false);
    	context.closePath();
    	context.fill();
    	
        /* Draw the wheel image onto the container */
        if(!this.gradientImage){
            this.gradientImage = new Image();
            this.gradientImage.onload = function(){
                this.gradient.getContext('2d').drawImage(this.gradientImage, 33, 32);
            }.createDelegate(this);
            this.gradientImage.src = (colorFieldImagePath != null ? colorFieldImagePath : this.imagePath) + 'gradient.png';
        }else
            this.gradient.getContext('2d').drawImage(this.gradientImage, 33, 32);
        
    },
    
    // private
    hexValue : function(r,g,b){
        var chars = '0123456789ABCDEF';
        return '#'+(
            chars[parseInt(r/16)] + chars[parseInt(r%16)] +
            chars[parseInt(g/16)] + chars[parseInt(g%16)] +
            chars[parseInt(b/16)] + chars[parseInt(b%16)]
        );
    }
});
Ext.reg('colorfield', Ext.ux.ColorField);