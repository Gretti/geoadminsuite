<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>

<html:html >
    <head><title><bean:message key="ms_layer_title"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <script type="text/javascript">
            // tree and rootnode
            var cwtree,cwtreeroot;
            // the selected node
            var currentNode;
            // shorthand
            var Tree = Ext.tree;

            // sets the user defined attributes for the current clicked layer
            function setLayerAttributes() {                
                var frm = document.forms['CartowebIniConfigurationForm'];
                if (isNaN(frm.mdMaxScale.value) ||isNaN(frm.mdMinScale.value)){
                    Ext.Msg.alert("Message", "<bean:message key="cartoweb.location.numericValueExpected"/>");
                }
                if (!currentNode || !currentNode.attributes || !currentNode.attributes.cwattributes ) {
                    Ext.Msg.alert("Message","invalid current node");
                    return;
                }
                var cmp;
                var attributes = currentNode.attributes.cwattributes; 
                for (var k in attributes){
                    cmp = eval('frm.' + k);
                    if(cmp) {
                        if(cmp.type == 'checkbox') {
                            attributes[k] = String(cmp.checked);
                        } else if(cmp.type == 'select') {
                            attributes[k] = cmp.options[cmp.selectedIndex].value;
                        } else {
                            attributes[k] = cmp.value;
                        }
                    }
                }
            }
            // checks form fields to avoid passing bad values to the servlet
            function checkForm() {
                return true;
            }
            
            // helper functions
            function trim(str, chars) {
                return ltrim(rtrim(str, chars), chars);
            }

            function ltrim(str, chars) {
                chars = chars || "\\s";
                return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
            }

            function rtrim(str, chars) {
                chars = chars || "\\s";
                return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
            }
            
            Array.prototype.inArray = function (search_phrase) {
                for( var i = 0; i < this.length; i++ )
                  if( search_phrase == this[i] )
                    return i;
                return false;
            }
            
            // get attributes in json compatible format
            function getAttributeString(attr) {
                text = "";
                // only save valid attributes
                typesToSave = new Array("boolean","string","number");
                for (var key in attr)
                  if (typesToSave.inArray(typeof(attr[key])))
                    text += '"'+key+'": "'+attr[key]+'",';
                return trim(text,",");
            }

            Ext.override(Ext.tree.TreeNode, {
                toJSON: function (node) {
                    if (node == null) node = this;
                    node.expandChildNodes();
                    var result = "";
                    result += "{" + getAttributeString(node.attributes) + ',cwattributes:{' + getAttributeString(node.attributes.cwattributes)+'}';
                    // add child nodes
                    if (node.childNodes.length > 0) {
                        result += ', "children": [';
                        node.eachChild(function(node){
                            if(!node.isLoaded) node.expand(true);
                            result += this.toJSON(node)+",";
                        });
                        result = trim(result,",")+"],";
                    }
                    return trim(result,",")+"}";
                }
            });

            function sub() {
                //parses tree to generate json
                setLayerAttributes();
                var stree = cwtreeroot.toJSON();
                Ext.Ajax.request({
                    url:'saveLayerTree.do',
                    params: {'<%=ObjectKeys.CW_LAYER_TREE_JSON%>':stree},
                    method: 'POST',
                    success: function(response, options){
                            //Ext.Msg.alert('response', Ext.util.JSON.decode(response.responseText));
                            //Ext.Msg.alert('response', response.responseText);
                        },
                    failure: function(){
                            Ext.Msg.alert('failure','Server failure, sorry...');
                        }
                });

            }

            function addCWNode(isLeaf) {
                Ext.Msg.prompt('Name', 'Please enter your name:', function(btn, text){
                    if (btn == 'ok') {
                        var clsType = isLeaf ? 'file' : 'folder';
                        // process text value and close...
                        var newNode = new Tree.TreeNode({
                            text: text,
                            draggable:true,
                            leaf: isLeaf,
                            id: text.replace(" ", "_").replace(".", "_")
                        });
                        var calcId = text.replace(' ', '_').toLowerCase();
                        newNode.attributes.cwattributes={
                            'id':calcId,
                            'msLayer':text,
                            'label':text,
                            'icon':'',
                            'link':'',
                            'children':'',
                            'switchId':'',
                            'aggregate':'false',
                            'rendering':'',
                            'mdMinScale':'',
                            'mdMaxScale':''
                        };
                        if(!cwtreeroot.childNodes[0].isExpanded) cwtreeroot.childNodes[0].expand(true,true);
                        cwtreeroot.childNodes[0].appendChild(newNode);
                    }
                });
            }

            function fillLayerAttributes(attributes) {
                var frm = document.forms['CartowebIniConfigurationForm'];
                var cmp;
                for (var k in attributes){
                    val = attributes[k];
                    cmp = eval('frm.' + k);
                    if(cmp) {
                        if(cmp.type == 'checkbox') {
                            cmp.checked = (val == "true");
                        } else if(cmp.type == 'select') {
                            cmp.selectedIndex = cmp.options[val];
                        } else {
                            cmp.value = val;
                        }
                    }
                }
                frm.removeNode.disabled = false;
            }
            
            function removeSelectedLayer() {
                Ext.MessageBox.confirm('<bean:message key="cartoweb.layer.removeLayer"/>', '<bean:message key="cartoweb.layer.removeLayer"/> ?', function(btn){
                    if(btn == 'yes') {
                        currentNode.remove();
                        currentNode = null;
                        document.forms['CartowebIniConfigurationForm'].reset();
                        document.forms['CartowebIniConfigurationForm'].removeNode.disabled = true;
                    };
                });
            }
            
            Ext.onReady(function(){

                cwtree = new Tree.TreePanel({
                    el:'tree-div',
                    useArrows:true,
                    autoScroll:true,
                    animate:true,
                    enableDD:true,
                    containerScroll: true,
                    rootVisible: false,
                    loader: new Tree.TreeLoader({
                        dataUrl:'loadCWLayerTree.do',
                        preloadChildren: true
                    }),
                    tbar:[
                             {
                                 text: '<bean:message key="cartoweb.layer.newgroup"/>',
                                 handler: function() {addCWNode(false);}
                             },{
                                 text: '<bean:message key="cartoweb.layer.newlayer"/>',
                                 handler: function() {addCWNode(true);}
                             }
                        ]
                });

                // set the root node
                cwtreeroot = new Tree.AsyncTreeNode({
                    id: 'cw_layer_tree',
                    text: 'Root',
                    draggable:false,
                    expanded: true
                });
                cwtree.setRootNode(cwtreeroot);
                //adds listener for click function
                cwtree.on('click',function(clickedNode,e) {
                    //save current node.
                    currentNode = clickedNode;
                    fillLayerAttributes(clickedNode.attributes.cwattributes);
                });
                // render the tree
                cwtree.render();
                cwtreeroot.expand();
            });
            
        </script>
</head>
<body class='body2' leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
    <html:form action="CWIniProperties.do" method="POST">
        <table class="tableb" cellpadding="0" cellspacing="1" width="100%">
            <!--
            <tr align="center">
                <th class="th0" colspan="2"><bean:message key="cartoweb.layer.configuration"/></th>
            </tr>
            -->
            <tr>
                <td class="td4tiny">autoClassLegend</td>
                <td class="td4tiny">
                    <html:checkbox property="layerConf.autoClassLegend" styleClass="tiny"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2">
                    <b><bean:message key="cartoweb.layer.layerTree"/></b><br/>
                    <bean:message key="cartoweb.layer.layerTreeDescription"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2">
                    <div id="tree-div" style=" height:300px;width:250px;border:1px solid #c3daf9;"></div>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">msLayer</td>
                <td class="td4tiny">
                    <input type=text name='msLayer' value='' size=15 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">label</td>
                <td class="td4tiny">
                    <input type=text name='label' value='' size=15 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">icon</td>
                <td class="td4tiny">
                    <input type=text name='icon' value='' size=15 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">link</td>
                <td class="td4tiny">
                    <input type=text name='link' value='' size=15 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">switchId</td>
                <td class="td4tiny">
                    <input type=text name='switchId' value='' size=5 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">aggregate</td>
                <td class="td4tiny">
                    <input type=checkbox name='aggregate' class='tiny'  onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">rendering</td>
                <td class="td4tiny">
                    <select name="rendering" onchange="setLayerAttributes()" class="tiny">
                        <option value="tree">tree</option>
                        <option value="block">block</option>
                        <option value="radio">radio</option>
                        <option value="dropdown">dropdown</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mdMinScale</td>
                <td class="td4tiny">
                    <input type=text name='mdMinScale' value='' size=5 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny">mdMaxScale</td>
                <td class="td4tiny">
                    <input type=text name='mdMaxScale' value='' size=5 class='tiny' onchange="setLayerAttributes()"/>
                </td>
            </tr>
            <tr>
                <td class="td4tiny" colspan="2" align="center">
                    <input type=button disabled name='removeNode' onclick="removeSelectedLayer()" value='<bean:message key="cartoweb.layer.removeLayer"/>' class='tiny'/>
                </td>
            </tr>
        </table>
    </html:form>


</body>
</html:html>
