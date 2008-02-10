GeneralLayout = 
    function(){
        var i18n = 'en';
        var layout,top;
        var cpDataList,cpDataDetail;
        var cpComposerMap,cpComposerProps,cpComposerCtrl;
        var composermap,zoombox,zoomout,dragpan,layertree, composerlayers, winProperties, legendContainer;
        var cpPublisherMapfish,cpPublisherDownload, publishermap, publisherlayers;
        
        return {
            
            headers : [],
            grid : null,
            store: null,
            columnsModels: [],
            colModel : null,
            dsArray : [],
            composermap : null,
            zoombox : null,
            zoomout : null,
            dragpan : null,
            layertree : null,
            composerlayers : null,
            legendContainer : null,
            publishermap : null,
            publisherlayers : null,
            
            init : function(){
                
                    Ext.QuickTips.init();
                    
                    //--------------------------//
                    //      CATALOG PANELS      //
                    //--------------------------//
                    //Center panel
                    cpDataDetail = new Ext.TabPanel({
                        id: 'pnlDataDetail',
                        region: 'center',
                        margins:'3 3 3 0', 
                        activeTab: 0,
                        defaults:{autoScroll:true},
                        contentEl: 'data_detail',
                        items:[{
                            id: 'view',
                            title: 'View',
                            xtype: 'panel'
                        },{
                            id: 'data',
                            title: 'Data',
                            xtype: 'panel'
                        },{
                            id: 'metadata',
                            title: 'Metadata',
                            xtype: 'panel'
                        }]
                    });

                    // West panel
                    cpDataList = new Ext.Panel({
                        id: 'pnlDataList',
                        title: 'Data List',
                        region: 'west',
                        split: true,
                        width: 200,
                        collapsible: true,
                        margins:'3 0 3 3',
                        cmargins:'3 3 3 3',
                        autoScroll:true,
                        contentEl: 'data_list'
                    });
                    
                    //--------------------------//
                    //      COMPOSER PANELS     //
                    //--------------------------//
                    
                    //Center panel
                    cpComposerMap = new Ext.Panel({
                        id: 'pnlComposerMap',
                        title: 'Map',
                        split: true,
                        region: 'center',
                        margins:'3 3 3 0', 
                        autoScroll:true,
                        contentEl: 'composer_map',
                        tbar:[{
                                   id:'zoominTool',
                                   enableToggle: true,
                                   toggleHandler: function(){
                                        if(this.pressed) {
                                            Ext.getCmp('zoomoutTool').toggle(false);
                                            Ext.getCmp('dragTool').toggle(false);
                                            GeneralLayout.zoombox.activate();
                                        } else {
                                            GeneralLayout.zoombox.deactivate();
                                        }
                                   },
                                   iconCls: 'bzoomin',
                                   pressed: false
                             },{
                                   id:'zoomoutTool',
                                   enableToggle: true,
                                   toggleHandler: function(){
                                        if(this.pressed) {
                                            Ext.getCmp('zoominTool').toggle(false);
                                            Ext.getCmp('dragTool').toggle(false);
                                            GeneralLayout.zoomout.activate();
                                        } else {
                                            GeneralLayout.zoomout.deactivate();
                                        }
                                   },
                                   iconCls: 'bzoomout',
                                   pressed: false
                             },{
                                   id:'dragTool',
                                   enableToggle: true,
                                   toggleHandler: function(){
                                        if(this.pressed) {
                                            Ext.getCmp('zoomoutTool').toggle(false);
                                            Ext.getCmp('zoominTool').toggle(false);
                                            GeneralLayout.dragpan.activate();
                                        } else {
                                            GeneralLayout.dragpan.deactivate();
                                        }
                                   },
                                   iconCls: 'bdrag',
                                   pressed: false
                             },'-',{
                                   id:'printTool',
                                   handler: function(){
                                       Ext.Msg.alert('to be continued ...');
                                   },
                                   iconCls: 'bprint'
                             }]
                               
                    });

                    // West panel
                    var cpLayerProps = new Ext.Panel({
                        id: 'cpLayerProps',
                        split: true,
                        autoWidth : true,
                        autoHeight : true,
                        margins:'3 3 3 3',
                        cmargins:'3 3 3 3',
                        autoScroll:true,
                        border: false
                    });

                    cpComposerProps = new Ext.Panel({
                        id: 'pnlComposerProps',
                        title: 'Properties',
                        region: 'west',
                        split: true,
                        width: 250,
                        collapsible: true,
                        margins:'3 0 3 3',
                        cmargins:'3 3 3 3',
                        autoScroll:true,
                        contentEl: 'composer_props',
                        layout:'accordion',
                        layoutConfig:{animate:true},
                        items:[{
                            id: 'generalprops',
                            contentEl: 'general_props',
                            title: 'General Properties',
                            border: false
                        },{
                            id: 'layerprops',
                            contentEl: 'layer_props',
                            title: 'Layer Properties',
                            items : [cpLayerProps],
                            border:false
                        }]

                    });
                    
                    //Makes a resizable with legend image
                    GeneralLayout.legendContainer = new Ext.Resizable('composer_legend', {
                        wrap:true,
                        pinned:true,
                        preserveRatio: true,
                        handles: 'all',
                        draggable:true,
                        dynamic:true
                    });
                    
                    // move to the body to prevent overlap
                    document.body.insertBefore(GeneralLayout.legendContainer.el.dom, document.body.firstChild);
                    GeneralLayout.legendContainer.el.hide();
                    
                    // East panel
                    layertree = new Ext.tree.TreePanel({
                        id:'layerTree',
                        animate: false,
                        containerScroll: true,
                        rootVisible: false
                    });
                    cpComposerCtrl = new Ext.Panel({
                        id: 'pnlComposerCtrl',
                        title: 'Controls',
                        region: 'east',
                        split: true,
                        width: 200,
                        collapsible: true,
                        margins:'3 0 3 3',
                        cmargins:'3 3 3 3',
                        autoScroll:true,
                        contentEl: 'composer_ctrl',
                        items :[layertree]
                    });
                    
                    //--------------------------//
                    //     PUBLISHER PANELS     //
                    //--------------------------//
                    //Tree panel for OL and MF components
                    var components = new Ext.tree.TreePanel({
                        id:'treeComponents',
                        animate: true,
                        containerScroll: true,
                        rootVisible: true,
                        enableDrag:true,
                        containerScroll: true
                    });
                    
                    var root = new Ext.tree.TreeNode({text: 'Components', id:'source'});
                    components.setRootNode(root);
                    var nodeOL = new Ext.tree.TreeNode({text:'Openlayers controls', id:'OLCtrl'});
                    var nodeMF = new Ext.tree.TreeNode({text:'MapFish controls', id:'MFCtrl'});
                    
                    var subNodesOL = [
                        new Ext.tree.TreeNode({text:'Editing Toolbar <img src="styles/cog_add.png" onclick="GeneralLayout.addComponent(\'OL_EditingToolbar\');">',    id:'OL_EditingToolbar', leaf:true}),
                        new Ext.tree.TreeNode({text:'Layer Switcher',     id:'OL_LayerSwitcher',  leaf:true}),
                        new Ext.tree.TreeNode({text:'Mouse Position',     id:'OL_MousePosition',  leaf:true}),
                        new Ext.tree.TreeNode({text:'Mouse Toolbar',      id:'OL_MouseToolbar',   leaf:true}),
                        new Ext.tree.TreeNode({text:'Navigation Toolbar', id:'OL_NavToolbar',     leaf:true}),
                        new Ext.tree.TreeNode({text:'Overview Map',       id:'OL_OverviewMap',    leaf:true}),
                        new Ext.tree.TreeNode({text:'Pan Zoom Bar',       id:'OL_PanZoomBar',     leaf:true}),
                        new Ext.tree.TreeNode({text:'Permalink',          id:'OL_Permalink',      leaf:true}),
                        new Ext.tree.TreeNode({text:'Scale',              id:'OL_Scale',          leaf:true})
                    ];
                    
                    var subNodesMF = [
                        new Ext.tree.TreeNode({text:'Layer Tree',         id:'MF_LayerTree',      leaf:true}),
                        new Ext.tree.TreeNode({text:'Navigation Toolbar', id:'MF_NavToolbar',     leaf:true})
                    ];
                    
                    root.appendChild(nodeOL);
                    root.appendChild(nodeMF);
                    for(var sn=0; sn < subNodesOL.length; sn++) {
                        nodeOL.appendChild(subNodesOL[sn]);
                    }
                    for(var sn=0; sn < subNodesMF.length; sn++) {
                        nodeMF.appendChild(subNodesMF[sn]);
                    }
                    
                    var selectedcomponents = new Ext.tree.TreePanel({
                        id:'treeSelectedComponents',
                        animate:true,
                        autoScroll:true,
                        //rootVisible: false,
                        containerScroll: true,
                        enableDD:true,
                        dropConfig: {appendOnly:true}
                    });

                    // add a tree sorter in folder mode
                    new Ext.tree.TreeSorter(selectedcomponents, {folderSort:true});

                    // add the root node
                    var root2 = new Ext.tree.TreeNode({
                        text: 'Selected components', 
                        draggable:false
                    });
                    selectedcomponents.setRootNode(root2);
                    
                    cpPublisherCpts = new Ext.Panel({
                        id:'pnlPublisherCpts',
                        region: 'west',
                        split: true,
                        width: 200,
                        collapsible: true,
                        autoScroll:false,
                        margins:'3 0 3 3',
                        cmargins:'3 3 3 3',
                        el: 'publisher_component_list',
                        items:[components,selectedcomponents]
                    });
                    
                    //Panel for resulting site display
                    cpPublisherResult = new Ext.Panel({
                        id: 'pnlPublisherResult',
                        region: 'center',
                        layout:'fit',
                        split: true,
                        width: 200,
                        margins:'3 0 3 3',
                        cmargins:'3 3 3 3',
                        autoScroll:false,
                        contentEl: 'publisher_view_result'
                    });
                    
                    cpPublisherMapfish = new Ext.Panel({
                        id: 'pnlPublisherMap',
                        title: 'Site Publisher',
                        split: true,
                        region: 'center',
                        margins:'3 3 3 0', 
                        autoScroll:false,
                        layout:'border',
                        contentEl: 'publisher_components',
                        items:[cpPublisherCpts,cpPublisherResult]
                    });
                    
                    cpPublisherDownload = new Ext.Panel({
                        id: 'pnlPublisherDownload',
                        title: 'Download',
                        split: true,
                        width: 500,
                        minWidth: 300,
                        region: 'east',
                        margins:'3 3 3 0', 
                        autoScroll:false,
                        contentEl: 'publisher_download'
                    });

                    
                    //--------------------------//
                    //      GENERAL LAYOUT      //
                    //--------------------------//
                    layout = new Ext.Viewport({
                        layout:'border',
                        items:[
                            /*new Ext.BoxComponent({
                                region:'north',
                                el: 'north',
                                height:52
                            }),*/
                            new Ext.TabPanel({
                                region:'center',
                                activeTab:0,
                                waitMsgTarget: true,
                                items:[{
                                    contentEl:'serverconfig',
                                    id:'pnlServerconfig',
                                    title: 'Config Server',
                                    autoScroll:true
                                },{
                                    contentEl:'catalog',
                                    id:'pnlCatalog',
                                    layout: 'border',
                                    title: 'Catalog',
                                    autoScroll:true,
                                    items:[cpDataList,cpDataDetail]
                                },{
                                    contentEl:'composer',
                                    id:'pnlComposer',
                                    title: 'Composer',
                                    layout: 'border',
                                    autoScroll:true,
                                    items:[cpComposerProps,cpComposerMap,cpComposerCtrl]
                                },{
                                    contentEl:'publisher',
                                    id:'pnlPublisher',
                                    title: 'Publisher',
                                    autoScroll:true,
                                    layout: 'border',
                                    items:[cpPublisherMapfish,cpPublisherDownload]
                                }]
                            })
                         ]
                    });
                    //renders Server form in server config form div element (frmServer)
                    GeneralLayout.renderHosts(hostList, 'frmServer');
                    
                    //Forces panel data rendering on activate
                    Ext.getCmp('data').on('activate', function() {Ext.getCmp('data').doLayout()}); 
                    
                    //Forces panel metadata rendering on activate
                    Ext.getCmp('metadata').on('activate', function() {Ext.getCmp('metadata').doLayout()}); 

                    
           },
           
           setLanguage : function(lang){
                i18n = lang;
           },

           initServerConfig : function(){
           },

           renderHosts : function(hosts, el) {
                        
                        hostItems = [];
                        var dis;
                        for (var i = 0; i < hosts.length; i++) {
                           
                           var comboType = new Ext.form.ComboBox({
                                        fieldLabel: 'Type',
                                        id:'host[' + i + '].type',
                                        name:'host[' + i + '].type',
                                        store: new Ext.data.SimpleStore({
                                            fields: ['typeCode', 'type'],
                                            data : [['folder','folder'],['pg','pg'],['oracle','oracle'],['wms','wms']]
                                        }),
                                        displayField:'type',
                                        width:80,
                                        typeAhead: true,
                                        mode: 'local',
                                        triggerAction: 'all',
                                        emptyText:'Select a type...',
                                        forceSelection: true,
                                        selectOnFocus:true,
                                        value:hosts[i].type,
                                        onSelect:function(record){
                                            this.collapse();
                                            this.setValue(record.data.type);
                                            var curHost = this.name.split('.')[0];
                                            if(record.data.type == 'folder') {
                                                Ext.getCmp(curHost + '.port').disable();
                                                Ext.getCmp(curHost + '.uname').disable();
                                                Ext.getCmp(curHost + '.upwd').disable();
                                            } else {
                                                Ext.getCmp(curHost + '.port').enable();
                                                Ext.getCmp(curHost + '.uname').enable();
                                                Ext.getCmp(curHost + '.upwd').enable();
                                            }
                                            
                                        }
                                    });
                                    
                           var txtName = new Ext.form.TextField({
                                        fieldLabel: 'Name',
                                        id: 'host[' + i + '].name',
                                        name: 'host[' + i + '].name',
                                        grow:true,
                                        growMax:400,
                                        allowBlank: false,
                                        value:hosts[i].name
                                    });

                           var txtPath = new Ext.form.TextField({
                                        fieldLabel: 'Path/URL',
                                        id: 'host[' + i + '].path',
                                        name: 'host[' + i + '].path',
                                        grow:true,
                                        growMax:400,
                                        allowBlank: false,
                                        value:hosts[i].path
                                    });

                           var numPort = new Ext.form.NumberField({
                                        fieldLabel: 'Port',
                                        id: 'host[' + i + '].port',
                                        name: 'host[' + i + '].port',
                                        maxValue: 65532,
                                        minValue: 1,
                                        allowNegative : false,
                                        grow:true,
                                        growMax:400,
                                        value:hosts[i].port,
                                        disabled:(hosts[i].type == 'folder')
                                    });
                           var txtUname = new Ext.form.TextField({
                                        fieldLabel: 'User Name',
                                        id: 'host[' + i + '].uname',
                                        name: 'host[' + i + '].uname',
                                        grow:true,
                                        growMax:400,
                                        value:hosts[i].uname,
                                        disabled:(hosts[i].type == 'folder')
                                    });
                           var txtUpwd = new Ext.form.TextField({
                                        fieldLabel: 'User Pwd',
                                        id: 'host[' + i + '].upwd',
                                        name: 'host[' + i + '].upwd',
                                        grow:true,
                                        growMax:400,
                                        inputType: 'password',
                                        value:hosts[i].upwd,
                                        disabled:(hosts[i].type == 'folder')
                                    });
                           
                           var actionTest = new Ext.Action({
                                                text: 'Test',
                                                id:'test_' + i,
                                                handler: function(){
                                                    //Ajax call to test datasources access and content 
                                                    //parameters
                                                    var hid = this.id.split('_')[1];
                                                    var type =  $('host[' + hid + '].type').value;
                                                    var name =  $('host[' + hid + '].name').value;
                                                    var path =  $('host[' + hid + '].path').value;
                                                    var port =  $('host[' + hid + '].port').value;
                                                    var uname = $('host[' + hid + '].uname').value;
                                                    var upwd =  $('host[' + hid + '].upwd').value;
                                                    
                                                    Ext.Ajax.request({
                                                        url:'testServer.do',
                                                        params: {'type':type,'name':name,'path':path,'port':port,'uname':uname,'upwd':upwd},
                                                        success: function(result){
                                                                //Gets the response of AJaX and parses it to make a JSon object
                                                                var server = Ext.util.JSON.decode(result.responseText);
                                                                if(server.state == 'ko') {
                                                                    Ext.getCmp('test_' + hid).setIconClass('bko');
                                                                    Ext.getCmp('test_' + hid).setText('access not available');
                                                                } else {
                                                                    Ext.getCmp('test_' + hid).setIconClass('bok');
                                                                    Ext.getCmp('test_' + hid).setText(server.nbds + ' datasources');
                                                                }
                                                            }
                                                    });
                                                },
                                                iconCls: 'btry'
                                            });
                           hostItems[i] = {
                                id:'fsHost' + i,
                                xtype:'fieldset',
                                title: 'Host ' + i,
                                autoHeight:true,
                                autoWidth:true,
                                buttonAlign:'center',
                                collapsible:true,
                                collapsed:true,
                                items: [comboType,txtName,txtPath,numPort,txtUname,txtUpwd],
                                buttons : [actionTest,{
                                                text: 'Remove',
                                                id:'remove_' + i,
                                                handler: function(){
                                                    if(hostList.length > 1) {
                                                        Ext.get('frmServer').update('');
                                                        var idx = parseInt(this.id.split('_')[1]);
                                                        tmphl = [];
                                                        for (var n = 0; n < hostList.length; n++) {
                                                            if(n != idx) tmphl.push(hostList[n]);
                                                        }
                                                        hostList = [].concat(tmphl);
                                                        GeneralLayout.renderHosts(hostList, 'frmServer');
                                                    } else {
                                                        Ext.Msg.alert('Warning','Actually, server list can not be empty ...')
                                                    }
                                                },
                                                iconCls: 'btrash'
                                            }]
                           }
                        }

                        top = new Ext.FormPanel({
                            url: 'hostLoader.do',
                            labelAlign: 'right',
                            frame:true,
                            title: 'Server List',
                            bodyStyle:'padding:5px 5px 0',
                            width: 600,
                            items: [{
                                layout:'column',
                                items:[{
                                    columnWidth:1,
                                    layout: 'form',
                                    items: hostItems
                                }]
                            }],
                            buttons:[{
                                        text:'Reset',
                                        handler:function() {
                                                    Ext.get('frmServer').update('');
                                                    hostList = [].concat(initialHostList);
                                                    GeneralLayout.renderHosts(initialHostList, 'frmServer');
                                                },
                                        iconCls: 'brefresh'
                                    },{
                                        text: 'Add',
                                        handler: GeneralLayout.addHost,
                                        iconCls: 'badd'
                                    },{
                                        text: 'Next',
                                        handler: GeneralLayout.gotoCatalog,
                                        iconCls: 'bnext'
                                    }]
                        });

                        top.render(el);

           },

           addHost : function(){
               if(hostList[hostList.length - 1].name != '') {
                   var h = new Host();
                   hostList.push(h);
                   Ext.get('frmServer').update('');
                   GeneralLayout.renderHosts(hostList, 'frmServer');
               } else {
                   Ext.Msg.show({
                        msg: 'You must finish to parametrize last server before starting another one...',
                        title: 'Warning',
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.WARNING
                    });
               }
           },
           
           gotoCatalog : function(){
                Ext.Msg.show({
                    msg: 'Reading datasources, please wait...',
                    progressText: 'Loading ...',
                    width:300,
                    wait:true,
                    waitConfig: {interval:200}
                    //icon:'ext-mb-download',
                    //animEl: 'mb7'
                });
                Ext.Ajax.request({
                    url:'hostLoader.do',
                    waitMsg:'Loading',
                    params: Ext.Ajax.serializeForm(document.forms[0]),
                    success: function(){
                            GeneralLayout.initCatalog();
                        }
                });
                Ext.getCmp('pnlCatalog').show();
           },
            
           initCatalog : function(){
                cpDataList.load({
                    url: "mapCatalogDatasourceList.jsp",
                    waitMsg:'Loading',
                    scripts:true
                });
           },

           gotoComposer : function(){
                Ext.getCmp('pnlComposer').show();
                //gets the checked nodes in the tree
                var checkedNodes = Ext.getCmp('treeHost').getChecked();
                var selectedIds = '';
                //passes the ids to the ComposeMapAction
                if(checkedNodes.length == 0) {
                    return;
                } else {
                    for (var n=0; n<checkedNodes.length; n++) {
                        if(n == 0) {
                            selectedIds += checkedNodes[n].id;
                        } else {
                            selectedIds += '|' + checkedNodes[n].id;
                        }
                    }
                }
                Ext.Ajax.request({
                url:'composeMap.do',
                params: {SELECTED_IDS:selectedIds,screenWidth:window.innerWidth,screenHeight:window.innerWidth},
                success: function(){
                        GeneralLayout.initComposer();
                    }
                });
           },
           
           initComposer : function(){
                cpComposerMap.load({
                    url: "mapConfiguration.jsp",
                    scripts:true
                });
           },
           
           gotoPublisher : function(){
                Ext.getCmp('pnlPublisher').show();
                GeneralLayout.initPublisher();
           },
           
           initPublisher : function(){
                cpPublisherDownload.load({
                    url: "mapPublisher.jsp",
                    scripts:true
                });
                cpPublisherMapfish.doLayout();
                Ext.getCmp('treeComponents').render();
                Ext.getCmp('treeSelectedComponents').render();
           },
           
           addComponent : function(cmp) {
               console.log(cmp);
           }

     };

}();