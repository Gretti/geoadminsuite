GeneralLayout = 
    function(){
        
        var layout,top;
        var cpServerConfigConf;
        var cpDataList,cpDataDetail;
        var cpComposerMap,cpComposerProps,cpComposerCtrl,composerMsUrl;
        var composermap,composernav,searcher,zoombox,zoomoutbox,dragpan,zoomtomax,layertree, composerlayers,printurl,winProperties,formParamXport;
        var cpPublisherMapfish,cpPublisherDownload,publishermap,publishertree,publishertoolbar,publisherlayers,correspControls,paramXportWin;
        var catalogInst, composerInst, publisherInst;
        
        return {
            
            headers : [],
            grid : null,
            store: null,
            columnsModels: [],
            colModel : null,
            dsArray : [],
            composermap : null,
            composernav : null,
            composerActiveExtent : null,
            zoombox : null,
            zoomoutbox : null,
            zoomtomax : null,
            searcher: null,
            dragpan : null,
            layertree : null,
            publishertree : null,
            publishertoolbar : null,
            composerlayers : null,
            composerMsUrl : null,
            publishermap : null,
            publisherlayers : [],
            printurl : null,
            printwin : null,
            correspControls: null,
            catalogInst : false, 
            composerInst : false, 
            publisherInst : false,
            displayHelp : 'none',
            
            init : function(){
                
                Ext.QuickTips.init();
                    
                //--------------------------//
                //      CATALOG PANELS      //
                //--------------------------//
                //Center panel
                                    
                //Mapserver direct call button
                var testMap = new Ext.Action({
                    id: 'btnCatalogTestMap',
                    handler: function(){
                        if(catalogMsUrl) {
                            window.open(catalogMsUrl);
                        }
                    },
                    iconCls: 'btry'
                });
                    
                cpDataDetail = new Ext.TabPanel({
                    id: 'pnlDataDetail',
                    region: 'center',
                    margins:'3 3 3 0',
                    activeTab: 0,
                    defaults:{
                        autoScroll:true
                    },
                    contentEl: 'data_detail',
                    items:[{
                        id: 'view',
                        title: i18n.view,
                        xtype: 'panel',
                        bbar: ['->',testMap]
                    },{
                        id: 'data',
                        title: i18n.data,
                        xtype: 'panel'
                    },{
                        id: 'metadata',
                        title: i18n.metadata,
                        xtype: 'panel'
                    }]
                });

                // West panel
                cpDataList = new Ext.Panel({
                    id: 'pnlDataList',
                    title: i18n.data_list,
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
                GeneralLayout.printWin = new PrintTemplate();
                PrintTemplateMgr.jsonOutput = null;

                //Center panel
                cpComposerMap = new Ext.Panel({
                    id: 'pnlComposerMap',
                    title: i18n.map,
                    split: true,
                    region: 'center',
                    margins:'3 3 3 0',
                    autoScroll:true,
                    contentEl: 'composer_map',
                    tbar:[{
                        id:'zoominTool',
                        tooltip: i18n.zoomin,
                        enableToggle: true,
                        toggleGroup: 'map',
                        toggleHandler: function(){
                            if(this.pressed) {
                                GeneralLayout.zoombox.activate();
                            } else {
                                GeneralLayout.zoombox.deactivate();
                            }
                        },
                        iconCls: 'bzoomin',
                        pressed: false
                    },{
                        id:'zoomoutTool',
                        tooltip: i18n.zoomout,
                        enableToggle: true,
                        toggleGroup: 'map',
                        toggleHandler: function(){
                            if(this.pressed) {
                                GeneralLayout.zoomoutbox.activate();
                            } else {
                                GeneralLayout.zoomoutbox.deactivate();
                            }
                        },
                        iconCls: 'bzoomout',
                        pressed: false
                    },{
                        id:'dragTool',
                        tooltip: i18n.previousView,
                        toggleGroup: 'map',
                        enableToggle: true,
                        toggleHandler: function(){
                            if(this.pressed) {
                                GeneralLayout.dragpan.activate();
                            } else {
                                GeneralLayout.dragpan.deactivate();
                            }
                        },
                        iconCls: 'bdrag',
                        pressed: false
                    },{
                        id:'zoomtolayerTool',
                        tooltip: i18n.layerExtent,
                        handler: function(){
                            GeneralLayout.composermap.zoomToExtent(GeneralLayout.composerActiveExtent, false);
                        },
                        iconCls: 'bzoomtolayer'
                    },{
                        id:'infoTool',
                        tooltip: i18n.infoTool,
                        toggleGroup: 'map',
                        enableToggle: true,
                        disabled: true,
                        toggleHandler: function(){
                            if(this.pressed) {
                                GeneralLayout.searcher.activate();
                            } else {
                                GeneralLayout.searcher.deactivate();
                            }
                        },
                        iconCls: 'binfotool',
                        pressed: false
                    },{
                        id:'zoomtomaxTool',
                        tooltip: i18n.fullExtent,
                        handler: function(){
                            GeneralLayout.composermap.zoomToMaxExtent();
                        },
                        iconCls: 'bzoomtomax'
                    },
                    ' ','-',' ',
                    {
                        id: 'exportImgTool',
                        tooltip: i18n.exportImg,
                        handler: function(){
                            var m = GeneralLayout.composermap;
                            var e = m.getExtent();
                            window.open(GeneralLayout.composerMsUrl
                                +"&map_size="+m.size.w+"+"+m.size.h
                                +"&map_extent="+e.left+"+"+e.bottom+"+"+e.right+"+"+e.top
                                +"&layers="+m.getLayersByName('mapserverLayer')[0].params.layers.join(' '));
                        },
                        iconCls: 'btry'
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
                    title: i18n.properties,
                    region: 'west',
                    split: true,
                    width: 250,
                    collapsible: true,
                    margins:'3 0 3 3',
                    cmargins:'3 3 3 3',
                    autoScroll:true,
                    contentEl: 'composer_props',
                    layout:'accordion',
                    layoutConfig:{
                        animate:true
                    },
                    items:[{
                        id: 'generalprops',
                        contentEl: 'general_props',
                        title: i18n.general_properties,
                        border: false
                    },{
                        id: 'layerprops',
                        contentEl: 'layer_props',
                        title: i18n.layer_properties,
                        items : [cpLayerProps],
                        border:false
                    }]

                });
                    
                // East panel
                cpComposerCtrl = new Ext.Panel({
                    id: 'pnlComposerCtrl',
                    title: i18n.controls,
                    region: 'east',
                    split: true,
                    width: 250,
                    collapsible: true,
                    margins:'3 0 3 3',
                    cmargins:'3 3 3 3',
                    autoScroll:true,
                    contentEl: 'composer_ctrl',
                    html:GeneralLayout.createBoxHelp('<img src="images/help.png">',i18n.help_composer)
                });
                    
                //--------------------------//
                //     PUBLISHER PANELS     //
                //--------------------------//
                //Tree panel for OL and MF components
                var components = new Ext.tree.TreePanel({
                    id:'treeComponents',
                    animate: true,
                    rootVisible: true,
                    enableDrag:false,
                    containerScroll: true
                });
                    
                var root = new Ext.tree.TreeNode({
                    text: i18n.components,
                    id:'source',
                    expanded : true
                });
                components.setRootNode(root);
                var nodeOL = new Ext.tree.TreeNode({
                    text:i18n.openlayers_controls,
                    id:'OLCtrl',
                    expanded : true
                });
                var nodeMF = new Ext.tree.TreeNode({
                    text:i18n.mapfish_controls,
                    id:'MFCtrl',
                    expanded : true
                });
                    
                var subNodesOL = [
                new Ext.tree.TreeNode({
                    text:'Layer Switcher&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.OL_LayerSwitcher">',
                    id:'OL_LayerSwitcher',
                    leaf:true
                }),
                new Ext.tree.TreeNode({
                    text:'Mouse Position&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.OL_MousePosition">',
                    id:'OL_MousePosition',
                    leaf:true
                }),
                new Ext.tree.TreeNode({
                    text:'Overview Map&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.OL_OverviewMap">',
                    id:'OL_OverviewMap',
                    leaf:true
                }),
                new Ext.tree.TreeNode({
                    text:'Editing Toolbar&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.OL_EditingToolbar">',
                    id:'OL_EditingToolbar',
                    leaf:true
                }),
                new Ext.tree.TreeNode({
                    text:'Scale&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.OL_Scale">',
                    id:'OL_Scale',
                    leaf:true
                })
                ];
                    
                GeneralLayout.correspControls = {
                    'OL_EditingToolbar':{
                        tool:'new OpenLayers.Control.EditingToolbar()',
                        incompatible:[]
                    },
                    'OL_LayerSwitcher': {
                        tool:'new OpenLayers.Control.LayerSwitcher()',
                        incompatible:[]
                    },
                    'OL_MousePosition': {
                        tool:'new OpenLayers.Control.MousePosition()',
                        incompatible:[]
                    },
                    //'OL_MouseToolbar':  {tool:'new OpenLayers.Control.MouseToolbar()',incompatible:['OL_NavToolbar']},
                    //'OL_NavToolbar':    {tool:'new OpenLayers.Control.NavToolbar()',incompatible:['OL_MouseToolbar']},
                    'OL_OverviewMap':   {
                        tool:'new OpenLayers.Control.OverviewMap()',
                        incompatible:[]
                    },
                    'OL_PanZoomBar':    {
                        tool:'new OpenLayers.Control.PanZoomBar()',
                        incompatible:[]
                    },
                    //'OL_Permalink':     {tool:'new OpenLayers.Control.Permalink()',incompatible:[]},
                    'OL_Scale':         {
                        tool:'new OpenLayers.Control.Scale()',
                        incompatible:[]
                    }
                //'OL_Navigation':    {tool:'new OpenLayers.Control.Navigation()',incompatible:[]},
                //'OL_PanZoom':       {tool:'new OpenLayers.Control.PanZoom()',incompatible:['OL_PanZoomBar']},
                };
                    
                var subNodesMF = [
                new Ext.tree.TreeNode({
                    text:'Layer Tree&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.MF_LayerTree">',
                    id:'MF_LayerTree',
                    leaf:true
                }),
                new Ext.tree.TreeNode({
                    text:'MapFish Toolbar&nbsp;&nbsp;&nbsp;&nbsp;<img src="styles/cog_add.png" id="img.MF_NavToolbar">',
                    id:'MF_NavToolbar',
                    leaf:true
                })
                ];
                    
                root.appendChild(nodeOL);
                root.appendChild(nodeMF);
                for(var sn=0; sn < subNodesOL.length; sn++) {
                    subNodesOL[sn].on('click',function(n){
                        GeneralLayout.toggleComponent(this);
                    });
                    nodeOL.appendChild(subNodesOL[sn]);
                }
                for(var sm=0; sm < subNodesMF.length; sm++) {
                    subNodesMF[sm].on('click',function(n){
                        GeneralLayout.toggleComponent(this);
                    });
                    nodeMF.appendChild(subNodesMF[sm]);
                }
                var selectedcomponents = new Ext.tree.TreePanel({
                    id:'treeSelectedComponents',
                    animate:true,
                    autoScroll:true,
                    //rootVisible: false,
                    containerScroll: true,
                    enableDD:false,
                    dropConfig: {
                        appendOnly:true
                    }
                });

                // add a tree sorter in folder mode
                new Ext.tree.TreeSorter(selectedcomponents, {
                    folderSort:true
                });

                // add the root node
                var root2 = new Ext.tree.TreeNode({
                    id:'rootSelectedComponents',
                    text: i18n.selected_components,
                    draggable:false
                });
                selectedcomponents.setRootNode(root2);
                    
                //add export button
                var xportAction = new Ext.Action({
                    text: i18n.exportit,
                    handler: function(){
                            
                        //Displays export parameters form
                        if(!paramXportWin){
                            formParamXport = new Ext.FormPanel({
                                labelWidth: 100,
                                frame:true,
                                width: '100%',
                                defaults: {
                                    width: 180
                                },
                                border: false,
                                defaultType: 'textfield',
                                items: [
                                {
                                    fieldLabel: i18n.mapserver_url,
                                    name: 'mapserver_url',
                                    value: 'http://localhost/cgi-bin/mapserv',
                                    allowBlank:true
                                },{
                                    fieldLabel: i18n.mapfile_target_path,
                                    name: 'mapfile_path',
                                    value: '/path/to/my/mapfile/geonline.map',
                                    allowBlank:true
                                },
                                {
                                    fieldLabel: i18n.keep_absolute_data_path,
                                    xtype: 'checkbox',
                                    id: 'keepAbsoluteDataPath',
                                    name: 'keepAbsoluteDataPath',
                                    checked: false
                                }],
                                buttons: [{
                                    text:i18n.continuer,
                                    handler: function() {
                                        Ext.Msg.show({
                                            msg: i18n.exporting_files,
                                            progressText: i18n.loading,
                                            width:300,
                                            wait:true,
                                            waitConfig: {
                                                interval:200
                                            }
                                        });
                                        //Finds selected components parsing treeSelectedComponents
                                        var selected_components = '&selected_components=';
                                        var arraySelComp = Ext.getCmp('treeSelectedComponents').root.childNodes;
                                        for(var sc = 0; sc < arraySelComp.length; sc++) {
                                            selected_components += (sc==0 ?'':'|') + arraySelComp[sc].id;
                                        }
                                        window.open('sitePublisher.do?' + formParamXport.getForm().getValues(true) + selected_components, "wait", "width=300,height=200");
                                        paramXportWin.hide();
                                        Ext.MessageBox.hide();
                                    }
                                },{
                                    text: i18n.cancel,
                                    handler: function(){
                                        paramXportWin.hide();
                                    }
                                }]
                            });

                            paramXportWin = new Ext.Window({
                                el:'param-export-win',
                                layout:'fit',
                                title: '<img src="styles/cog_edit.png">&nbsp;' + i18n.export_parameters,
                                width:350,
                                height:200,
                                closeAction:'hide',
                                modal: true,
                                fill:true,
                                plain: true,
                                items: [formParamXport]
                            });
                        }
                            
                        paramXportWin.show();
                            
                    },
                    iconCls: 'bexport'
                });
                    
                //creates panel
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
                    items:[components,selectedcomponents],
                    html:GeneralLayout.createBoxHelp('<img src="images/help.png">','toInternat:Ce panneau offre un ensemble de composants à intégrer dans le site final. <br>Cliquer sur les composants de l\'arbre du haut pour les rajouter, cliquer sur les composants ajoutés dans l\'arbre du bas pour les supprimer. Une fois les éléments sélectionnés, exporter le site en cliquant sur le bouton Export dans la barre d\'outils au dessus de l\'arbre.'),
                    tbar: [xportAction]
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
                    title: i18n.site_publisher,
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
                    title: i18n.download,
                    split: true,
                    collapsible: true,
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
                            title: i18n.config_server + '<img src="images/help.png" onclick=\'GeneralLayout.getHelp();\' alt="Help" title="Help">',
                            autoScroll:true
                        },{
                            contentEl:'catalog',
                            id:'pnlCatalog',
                            layout: 'border',
                            title: i18n.catalog + '<img src="images/help.png" onclick=\'GeneralLayout.getHelp();\' alt="Help" title="Help">',
                            autoScroll:true,
                            items:[cpDataList,cpDataDetail]
                        },{
                            contentEl:'composer',
                            id:'pnlComposer',
                            title: i18n.composer + '<img src="images/help.png" onclick=\'GeneralLayout.getHelp();\' alt="Help" title="Help">',
                            layout: 'border',
                            autoScroll:true,
                            items:[cpComposerProps,cpComposerMap,cpComposerCtrl]
                        },{
                            contentEl:'publisher',
                            id:'pnlPublisher',
                            title: i18n.publisher + '<img src="images/help.png" onclick=\'GeneralLayout.getHelp();\' alt="Help" title="Help">',
                            autoScroll:true,
                            //layout: 'border',
                            layout: 'accordion',
                            items:[cpPublisherMapfish,cpPublisherDownload]
                        }]
                    })
                    ]
                });
                //Adds a panel to server config for links to configuration and language
                cpServerConfigConf = new Ext.Panel({
                    id:'pnlServerconfigConf',
                    title: i18n.config_server,
                    collapsible:true,
                    plain:true,
                    autoScroll:true,
                    html: GeneralLayout.createBoxHelp('<img src="images/help.png">','toInternat:Ce panneau pr&eacute;sente les options de configuration de l\'application : langue, acc&egrave;s &agrave; la page de d&eacute;finition des pr&eacute;f&eacute;rences') +
                    '</div><br />' +
                    '<span style="width:100%;" class="label">' + i18n.choose_language +
                    '<img alt="FR" style="cursor:pointer;" src="images/fr.png" onclick="javascript:changeLanguage(\'fr\');">&nbsp;' +
                    '<img alt="EN" style="cursor:pointer;" src="images/us.png" onclick="javascript:changeLanguage(\'en\');"></span><br />'+
                    '<span style="width:100%" class="label">' + i18n.access_admin + '<a href="admin.jsp">' +
                    '<img src="images/link_conf.png" alt="Administration" title="Administration">' +
                    '</a></span>'  +
                    '<br />&nbsp;' +
                    '<span style="width:100%" class="label">' + i18n.reset_session + '<a href="javascript:resetUserSession()">' +
                    '<img src="styles/cancel.png" alt="Administration" title="Administration">' +
                    '</a></span>' +
                    '<br />&nbsp;'
                });
                cpServerConfigConf.render('confApp');
                //renders Server form in server config form div element (frmServer)
                GeneralLayout.renderHosts(hostList, 'frmServer');
                    
                //Forces panel data rendering on activate
                Ext.getCmp('data').on('activate', function() {
                    Ext.getCmp('data').doLayout()
                    });
                    
                //Forces panel metadata rendering on activate
                Ext.getCmp('metadata').on('activate', function() {
                    Ext.getCmp('metadata').doLayout()
                    });
                    
                //Checks if panel has been loaded in case Next button is not used
                Ext.getCmp('pnlCatalog').on('activate', function() {
                    if(!GeneralLayout.catalogInst) {
                        GeneralLayout.gotoCatalog();
                    }
                            
                });
                Ext.getCmp('pnlComposer').on('activate', function() {
                    if(!GeneralLayout.composerInst) {
                        GeneralLayout.gotoComposer();
                    }
                            
                });
                Ext.getCmp('pnlPublisher').on('activate', function() {
                    if(!GeneralLayout.publisherInst) {
                        GeneralLayout.gotoPublisher();
                    }
                            
                });
                    
                    
            },
           
            renderHosts : function(hosts, el) {
                hostItems = [];
                var IMG_FOLDER = 'images/folderclose.gif';
                var IMG_PG = 'images/pg.gif';
                var IMG_ORA = 'images/ORA.gif';
                var IMG_WMS = 'images/wms.png';
                var IMG_WFS = 'images/wms.png';
                for (var i = 0; i < hosts.length; i++) {
                    var comboType = new Ext.form.ComboBox({
                        fieldLabel: i18n.type,
                        id:'host[' + i + '].type',
                        name:'host[' + i + '].type',
                        store: new Ext.data.SimpleStore({
                            fields: ['typeCode', 'type'],
                            data : [['folder','folder'],['pg','pg'],['oracle','oracle'],['wms','wms'],['wfs','wfs']]
                        }),
                        displayField:'type',
                        width:80,
                        typeAhead: true,
                        mode: 'local',
                        triggerAction: 'all',
                        emptyText:i18n.select_type,
                        forceSelection: true,
                        selectOnFocus:true,
                        value:hosts[i].type,
                        onSelect:function(record){
                            this.collapse();
                            this.setValue(record.data.type);
                            var curHost = this.name.split('.')[0];
                            var hostImage = document.getElementById('img_' + curHost);
                            if(hostImage) {
                                if (record.data.type == 'folder') {
                                    hostImage.src = IMG_FOLDER;
                                } else if (record.data.type == 'pg') {
                                    hostImage.src = IMG_PG;
                                } else if (record.data.type == 'oracle') {
                                    hostImage.src = IMG_ORA;
                                } else if (record.data.type == 'wms') {
                                    hostImage.src = IMG_WMS;
                                } else if (record.data.type == 'wfs') {
                                    hostImage.src = IMG_WFS;
                                }
                            }
                            if(record.data.type == 'folder') {
                                Ext.getCmp(curHost + '.name').enable();
                                Ext.getCmp(curHost + '.path').enable();
                                Ext.getCmp(curHost + '.port').disable();
                                Ext.getCmp(curHost + '.uname').disable();
                                Ext.getCmp(curHost + '.upwd').disable();
                                Ext.getCmp(curHost + '.instance').disable();
                                Ext.getCmp(curHost + '.recurse').enable();
                            } else if (record.data.type == 'pg' || record.data.type == 'oracle'){
                                Ext.getCmp(curHost + '.port').enable();
                                Ext.getCmp(curHost + '.uname').enable();
                                Ext.getCmp(curHost + '.upwd').enable();
                                Ext.getCmp(curHost + '.path').disable();
                                Ext.getCmp(curHost + '.instance').enable();
                                Ext.getCmp(curHost + '.recurse').disable();
                            } else if (record.data.type == 'wms' || record.data.type == 'wfs'){
                                Ext.getCmp(curHost + '.name').enable();
                                Ext.getCmp(curHost + '.path').enable();
                                Ext.getCmp(curHost + '.port').disable();
                                Ext.getCmp(curHost + '.uname').disable();
                                Ext.getCmp(curHost + '.upwd').disable();
                                Ext.getCmp(curHost + '.instance').disable();
                                Ext.getCmp(curHost + '.recurse').disable();
                            }
                        }
                    });

                    var txtName = new Ext.form.TextField({
                        fieldLabel: i18n.name,
                        id: 'host[' + i + '].name',
                        name: 'host[' + i + '].name',
                        grow:true,
                        growMax:400,
                        allowBlank: false,
                        value:hosts[i].name
                    });

                    var txtPath = new Ext.form.TextField({
                        fieldLabel: i18n.path_url,
                        id: 'host[' + i + '].path',
                        name: 'host[' + i + '].path',
                        grow:true,
                        growMax:400,
                        allowBlank: false,
                        value:hosts[i].path
                    });

                    var numPort = new Ext.form.NumberField({
                        fieldLabel: i18n.port,
                        id: 'host[' + i + '].port',
                        name: 'host[' + i + '].port',
                        maxValue: 65532,
                        minValue: 1,
                        allowNegative : false,
                        grow:true,
                        growMax:400,
                        value:hosts[i].port,
                        disabled:(hosts[i].type == 'folder'||hosts[i].type == 'wms'||hosts[i].type == 'wfs')
                    });
                    var txtUname = new Ext.form.TextField({
                        fieldLabel: i18n.user_name,
                        id: 'host[' + i + '].uname',
                        name: 'host[' + i + '].uname',
                        grow:true,
                        growMax:400,
                        value:hosts[i].uname,
                        disabled:(hosts[i].type == 'folder'||hosts[i].type == 'wms'||hosts[i].type == 'wfs')
                    });
                    var txtUpwd = new Ext.form.TextField({
                        fieldLabel: i18n.user_pwd,
                        id: 'host[' + i + '].upwd',
                        name: 'host[' + i + '].upwd',
                        grow:true,
                        growMax:400,
                        inputType: 'password',
                        value:hosts[i].upwd,
                        disabled:(hosts[i].type == 'folder'||hosts[i].type == 'wms'||hosts[i].type == 'wfs')
                    });
                    var txtinstance = new Ext.form.TextField({
                        fieldLabel: 'Template',
                        id: 'host[' + i + '].instance',
                        name: 'host[' + i + '].instance',
                        grow:true,
                        growMax:400,
                        value:hosts[i].instance,
                        disabled:(hosts[i].type == 'folder'||hosts[i].type == 'wms'||hosts[i].type == 'wfs')
                    });
                                    
                    var checkRecurse = new Ext.form.Checkbox({
                        fieldLabel: i18n.recurse,
                        id: 'host[' + i + '].recurse',
                        name: 'host[' + i + '].recurse',
                        checked: ("on" == hosts[i].recurse),
                        disabled:(hosts[i].type != 'folder')
                    });
                                    
                    var actionTest = new Ext.Action({
                        text: i18n.test,
                        id:'test_' + i,
                        handler: function(){
                            //Ajax call to test datasources access and content
                            //parameters
                            var hid = this.id.split('_')[1];
                            var type =  $('host[' + hid + '].type').value;
                            var recurse = $('host[' + hid + '].recurse').checked;
                            var name =  $('host[' + hid + '].name').value;
                            var path =  $('host[' + hid + '].path').value;
                            var port =  $('host[' + hid + '].port').value;
                            var uname = $('host[' + hid + '].uname').value;
                            var upwd =  $('host[' + hid + '].upwd').value;
                            var instance =  $('host[' + hid + '].instance').value;
                            var mask = new Ext.LoadMask(Ext.getCmp('fsHost' + hid).getEl(),{
                                msg:i18n.testing_server
                                });
                            mask.show();
                            Ext.Ajax.request({
                                url:'testServer.do',
                                params: {
                                    'type':type,
                                    'recurse':recurse,
                                    'name':name,
                                    'path':path,
                                    'port':port,
                                    'uname':uname,
                                    'upwd':upwd,
                                    'instance':instance
                                },
                                success: function(result){
                                    //Gets the response of AJaX and parses it to make a JSon object
                                    var server = Ext.util.JSON.decode(result.responseText);
                                    if(server.state == 'ko') {
                                        Ext.getCmp('test_' + hid).setIconClass('bko');
                                        Ext.getCmp('test_' + hid).setText(i18n.access_not_available);
                                    } else {
                                        Ext.getCmp('test_' + hid).setIconClass('bok');
                                        Ext.getCmp('test_' + hid).setText(server.nbds + ' ' + i18n.datasources);
                                    }
                                    mask.hide();
                                }
                            });
                                                    
                        },
                        tooltip: i18n.test_source_validity,
                        iconCls: 'btry'
                    });
                    img = '<img src="';
                    if (hosts[i].type == 'folder') {
                        img += IMG_FOLDER;
                    } else if (hosts[i].type == 'pg') {
                        img += IMG_PG;
                    } else if (hosts[i].type == 'oracle') {
                        img += IMG_ORA;
                    } else if (hosts[i].type == 'wms') {
                        img += IMG_WMS;
                    } else if (hosts[i].type == 'wfs') {
                        img += IMG_WFS;
                    }
                           
                    img += '" id="img_host[' + i + ']"';
                    img += '" alt="' + hosts[i].type + ' datasource"';
                    img += '" title="' + hosts[i].type + ' datasource">';
                    hostItems[i] = {
                        id:'fsHost' + i,
                        xtype:'fieldset',
                        title: img + ' ' + hosts[i].name, //'Host ' + i,
                        autoHeight:true,
                        autoWidth:true,
                        buttonAlign:'center',
                        collapsible:true,
                        collapsed:true,
                        items: [comboType,txtName,txtPath,checkRecurse,numPort,txtUname,txtUpwd, txtinstance],
                        buttons : [actionTest,{
                            text: i18n.remove,
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
                                    Ext.Msg.alert(i18n.warning,i18n.server_list_empty);
                                }
                            },
                            tooltip: i18n.remove_source,
                            iconCls: 'btrash'
                        }]
                    }
                }

                var frmHosts = new Ext.FormPanel({
                    'url': 'hostLoader.do',
                    'labelAlign': 'right',
                    'frame':true,
                    'title': i18n.server_list,
                    'bodyStyle':'padding:5px 5px 0',
                    'width': 600,
                    'items': [{
                        'layout':'column',
                        'html': GeneralLayout.createBoxHelp('<img src="images/help.png">','toInternat:Ce panneau pr&eacute;sente la liste des serveurs pr&eacute;configur&eacute;s. Il est possible d\'ajouter de nouveau serveurs ainsi que de modifier ou supprimer des serveurs de la liste. Un bouton de test permet de v&eacute;rifier l\'accessibilit&eacute; aux serveurs configur&eacute;s.'),
                        'items':[{
                            'columnWidth':1,
                            'layout': 'form',
                            'items': hostItems
                        }]
                    }],
                    'buttons':[{
                        'text':i18n.reset,
                        handler:function() {
                            Ext.get('frmServer').update('');
                            hostList = [].concat(initialHostList);
                            GeneralLayout.renderHosts(initialHostList, 'frmServer');
                        },
                        'tooltip': i18n.init_list,
                        'iconCls': 'brefresh'
                    },{
                        'text': i18n.add,
                        'handler': GeneralLayout.addHost,
                        'tooltip': i18n.add_server,
                        'iconCls': 'badd'
                    },{
                        'text': i18n.next,
                        'handler': GeneralLayout.gotoCatalog,
                        'tooltip': i18n.goto_catalog,
                        'iconCls': 'bnext'
                    }]
                });

                frmHosts.render(el);

            },

            addHost : function(){
                if(hostList[hostList.length - 1].name != '') {
                    var h = new Host();
                    hostList.push(h);
                    Ext.get('frmServer').update('');
                    GeneralLayout.renderHosts(hostList, 'frmServer');
                } else {
                    Ext.Msg.show({
                        msg: i18n.finish_parametrize,
                        title: i18n.warning,
                        buttons: Ext.Msg.OK,
                        icon: Ext.MessageBox.WARNING
                    });
                }
            },
           
            gotoCatalog : function(){
                Ext.Msg.show({
                    msg: i18n.reading_datasource,
                    progressText: i18n.loading,
                    width:300,
                    wait:true,
                    waitConfig: {
                        interval:200
                    }
                });
                Ext.Ajax.request({
                    url:'hostLoader.do',
                    waitMsg:i18n.loading,
                    params: Ext.Ajax.serializeForm(document.forms[0]),
                    success: function(){
                        GeneralLayout.initCatalog();
                    }
                });
                GeneralLayout.catalogInst=true;
                if(GeneralLayout.displayHelp != 'none') GeneralLayout.getHelp();
                
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
                //gets the checked nodes in the tree
                var checkedNodes = Ext.getCmp('treeHost').getChecked();
                var selectedIds = '';
                //passes the ids to the ComposeMapAction
                if(checkedNodes.length == 0) {
                    if(Ext.getCmp('pnlCatalog').ownerCt.activeTab != Ext.getCmp('pnlCatalog'))
                        Ext.getCmp('pnlCatalog').show();
                    
                    Ext.MessageBox.show({
                        title: i18n.no_datasource_selected,
                        msg: i18n.select_one_datasource,
                        buttons: Ext.MessageBox.OK,
                        icon: Ext.MessageBox.INFO
                    });
                    
                    return;
                } else {
                    GeneralLayout.composerInst=true;
                    if(GeneralLayout.displayHelp != 'none') GeneralLayout.getHelp();
                    Ext.getCmp('pnlComposer').show();
                    for (var n=0; n<checkedNodes.length; n++) {
                        if(checkedNodes[n].isLeaf()) {
                            if(selectedIds == '') {
                                selectedIds += checkedNodes[n].id;
                            } else {
                                selectedIds += '|' + checkedNodes[n].id;
                            }
                        }
                    }
                    Ext.Msg.show({
                        msg: i18n.preparing_map,
                        progressText: i18n.loading,
                        width:300,
                        wait:true,
                        waitConfig: {
                            interval:200
                        }
                    });

                    Ext.Ajax.request({
                        url:'composeMap.do',
                        params: {
                            SELECTED_IDS:selectedIds,
                            screenWidth:window.innerWidth,
                            screenHeight:window.innerWidth
                            },
                        timeout: 60000,
                        success: function(){
                            if(Ext.getCmp('legend-win')) {
                                Ext.getCmp('legend-win').destroy();
                            }
                            GeneralLayout.initComposer();
                        }
                    });
                }
            },
           
            initComposer : function(){
                cpComposerMap.load({
                    url: "mapConfiguration.jsp",
                    scripts:true,
                    callback: function() {
                        Ext.Msg.hide();
                    }
                });
            },
           
            gotoPublisher : function(){
                if(GeneralLayout.displayHelp != 'none') GeneralLayout.getHelp();
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
                GeneralLayout.publisherInst=true;
            },
            toggleComponent : function(cmp) {
                var parentNodeId = cmp.parentNode.attributes.id;
                if(parentNodeId == 'rootSelectedComponents') {
                    GeneralLayout.removeComponent(cmp);
                } else {
                    GeneralLayout.addComponent(cmp);
                }
            },
           
            addComponent : function(cmp) {
                //retreives treenode to move it
                var elt = cmp.attributes.id;
                var selnode = cmp;
                Ext.getCmp('treeSelectedComponents').root.appendChild(selnode);
               
                //modifies text
                var toolname = selnode.text.split("<")[0];
                var newtext = toolname + '<img src="styles/cog_delete.png" id="' + cmp.id + '"/>';
               
                //selnode.disable();
                selnode.setText (newtext);
               
                Ext.getCmp('treeSelectedComponents').root.expand();
               
                if(elt.split('_')[0] == 'OL') {
                    //adds control to the map if OL control
                    var objCtrlToAdd = GeneralLayout.correspControls[elt];
                    var ctrlToAdd = eval(objCtrlToAdd.tool);
                    ctrlToAdd.id = 'ctrl_' + elt;
               
                    GeneralLayout.publishermap.addControl(ctrlToAdd,null);
               
                    //removes incompatible controls
                    for (var n=0;n < objCtrlToAdd.incompatible.length;n++) {
                        if(GeneralLayout.publishermap.getControl('ctrl_' + objCtrlToAdd.incompatible[n])) {
                            GeneralLayout.removeComponent($('img.' + objCtrlToAdd.incompatible[n]));
                        }
                    }
                } else {
                    if(elt == 'MF_LayerTree') {
                        GeneralLayout.publishertree = new mapfish.widgets.LayerTree({
                            id:'publisherTree',
                            map: GeneralLayout.publishermap, 
                            model: pmodel,
                            enableDD:true,
                            width: '100%',
                            height: '100%',
                            border: false,
                            autoScroll: true
                        });
                        GeneralLayout.publishertree.render('publishertree');
                    }
                   
                    if(elt == 'MF_NavToolbar') {
                        GeneralLayout.publishertoolbar = new mapfish.widgets.toolbar.Toolbar({
                            map: GeneralLayout.publishermap,
                            configurable: false
                        });
                        GeneralLayout.publishertoolbar.render('publishertoolbar');
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.ZoomBox({
                            isDefault: true
                        }), {
                            iconCls: 'bzoomin',
                            toggleGroup: 'map'
                        });
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.ZoomBox({
                            out:true
                        }), {
                            iconCls: 'bzoomout',
                            toggleGroup: 'map'
                        });
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DragPan(), {
                            iconCls: 'bdrag',
                            toggleGroup: 'map'
                        });
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.ZoomToMaxExtent(), {
                            iconCls: 'bzoomtomax',
                            toggleGroup: 'map'
                        });
 
                        GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Spacer());
                        GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Separator());
                        GeneralLayout.publishertoolbar.add(new Ext.Toolbar.Spacer());
                       
                        var vectorLayer = GeneralLayout.publishermap.getLayersByName('Draw')[0];
                       
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Point), {
                            iconCls: 'bdrawpoint',
                            toggleGroup: 'map'
                        });
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Path), {
                            iconCls: 'bdrawline',
                            toggleGroup: 'map'
                        });
                        GeneralLayout.publishertoolbar.addControl(new OpenLayers.Control.DrawFeature(vectorLayer, OpenLayers.Handler.Polygon), {
                            iconCls: 'bdrawpolygon',
                            toggleGroup: 'map'
                        });
 
                        GeneralLayout.publishertoolbar.activate();
                    }
                }
               
            },
           
            removeComponent : function(cmp) {
                //retreives treenode to move it
                var elt = cmp.attributes.id;
                var parentNode = elt.split('_')[0] + 'Ctrl';
                var selnode = cmp;
                Ext.getCmp('treeComponents').root.findChild('id',parentNode).appendChild(selnode);
               
                //modifies text
                var toolname = selnode.text.split("<")[0];
               
                var newtext = toolname + '<img src="styles/cog_add.png" id="' + cmp.id + '"/>';
               
                selnode.setText (newtext);
               
               
                Ext.getCmp('treeComponents').root.findChild('id',parentNode).expand();
               
                //removes control from the map if OL Control
                if(elt.split('_')[0] == 'OL') {
                    var c2r = GeneralLayout.publishermap.getControl('ctrl_' + elt);
                    if(c2r) {
                        GeneralLayout.publishermap.removeControl(c2r);
                    }
                } else {
                    if(elt == 'MF_LayerTree') {
                        GeneralLayout.publishertree.destroy();
                    }
                    if(elt == 'MF_NavToolbar') {
                        GeneralLayout.publishertoolbar.destroy();
                    }
                }
            },
            getHelp : function() {
                if(GeneralLayout.displayHelp == 'none') {
                    GeneralLayout.displayHelp = 'block';
                } else {
                    GeneralLayout.displayHelp = 'none';
                }
                var elts = document.getElementsByName('divhelp');
                for(var y=0;y<elts.length;y++) {
                    elts[y].style.display = GeneralLayout.displayHelp;
                }
            },
            createBoxHelp: function (t, s) {
                return ['<div name="divhelp" style="display:none;" class="msg">',
                '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
                '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc">', t, '&nbsp;', s, '</div></div></div>',
                '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
                '</div>'].join('');
            }

        };

    }();