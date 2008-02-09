<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Map" %>
<%@page import="org.geogurus.mapserver.objects.Legend" %>
<html:html locale="true">
    <head>
        <title>Legend Configuration</title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <script type="text/javascript">
            
            function sub() {
                    /*Ext.Ajax.request({
                    url:'MSLegendProperties.jsv',
                    waitMsg:'Loading',
                    params: Ext.Ajax.serializeForm(document.forms["LegendForm"]),
                    success: function(){
                            // Do nothing for the moment. Legend is not displayed.
                            //GeneralLayout.composermap.baseLayer.mergeNewParams({'timestamp':Math.random()});
                        }
                    });*/             
            }
             
            //GNG : HexConverter
            h=new Array(16);
            for (i=0;i<10;i++){h[i]=i.toString();}
            h[10]='A';h[11]='B';h[12]='C';h[13]='D';h[14]='E';h[15]='F';

            function toHexa(n){
                n=Number(n);
                return(h[Math.floor(n/16)]+h[n%16]);
            }

            function Hexad(){
                n=document.forms["LegendForm"].dec.value
                Hexadec = (toHexa(n));
                if ((n > 0) & (n <= 255)) {document.forms["LegendForm"].hex.value = Hexadec};
                if (n>255) {document.forms["LegendForm"].dec.value = 255;Hexad()}
                if (n<=1) {document.forms["LegendForm"].dec.value = 1;Hexad()};
            }

            function checkValue(value){
                if ((value > 0) & (value <= 255)){return value};
                if (value>255){return 255};
                if (value<1){return 0};
            }
            
            // Convertit le triplet donné en hexadécimal
            function convToHex(val_, id_){
                //Le triplet transmis est séparé par des espaces ou égal à "null"
                var val = val_.split(" ");
                if(val.length == 3) {
                    r =  checkValue(val[0]);
                    v = checkValue(val[1]);
                    b = checkValue(val[2]);
                    nouv_couleur =  '#' + (toHexa(r) +  toHexa(v) + toHexa(b));
                    document.getElementById(id_).style.borderStyle = "solid";
                    document.getElementById(id_).style.borderWidth = "1px";
                    document.getElementById(id_).style.backgroundColor = nouv_couleur;
                    document.getElementById(id_).innerHTML = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                 } else {
                    document.getElementById(id_).style.borderStyle = "none";
                    document.getElementById(id_).style.borderWidth = "0px";
                    document.getElementById(id_).innerHTML = "";
                 }
            }
        </script>
    </head>
    <body class='body2' style="background-color:white;margin:0 0 0 0;">
        <bean:define id="leg" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.legend"/>
        <form name="LegendForm" method="post" action="MSLegendProperties.jsv">
            <table border="0">
                <tr> 
                    <th colspan="2" class="th0">Paramètres de l'objet Legend: </th>
                </tr>
                <tr>
                    <td class="td4tiny">ImageColor</td>
                    <td class="td4tiny">
                        <span class="sampleColor" id="legend_imagecolor"></span>
                        <script>convToHex("<bean:write name="leg" property="imageColor"/>", "legend_imagecolor")</script>
                        <input type="text" name="legend_imagecolor" size=10 value="<bean:write name="leg" property="imageColor"/>" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" >Interlace</td>
                    <td height="28" class="td4tiny" > 
                        <html:radio name="leg" property="interlace" styleClass="tiny" value="<%=String.valueOf(Legend.ON)%>"/>On
                        <html:radio name="leg" property="interlace" styleClass="tiny" value="<%=String.valueOf(Legend.OFF)%>"/>Off
                    </td>
                </tr>
                <tr> 
                    <td colspan="2" class="td4tiny" >
                        <a href='#' onclick="openWindow('MC_mapserver_label_properties.jsp?msobject=legendtarget', 'legendprops')">
                            Label
                        </a>
                    <span class="tinygrey">(<bean:message key="label_msg"/>)</span></td>
                </tr>
                <tr> 
                    <td class="td4tiny" >OutlineColor</td>
                    <td class="td4tiny" > 
                        <span class="sampleColor" id="legend_outlinecolor"></span>
                        <script>convToHex("<bean:write name="leg" property="outlineColor"/>", "legend_outlinecolor")</script>
                        <input type="text" name="legend_outlinecolor" size=10 value="<bean:write name="leg" property="outlineColor"/>" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Position</td>
                    <td class="td4tiny" > 
                        <html:select name="leg" property="position" styleClass="tiny">
                            <option value="<%=String.valueOf(Legend.UL)%>">ul</option>
                            <option value="<%=String.valueOf(Legend.UC)%>">uc</option>
                            <option value="<%=String.valueOf(Legend.UR)%>">ur</option>
                            <option value="<%=String.valueOf(Legend.LL)%>">ll</option>
                            <option value="<%=String.valueOf(Legend.LC)%>">lc</option>
                            <option value="<%=String.valueOf(Legend.LR)%>">lr</option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >KeySize</td>
                    <td class="td4tiny" >
                        x: <html:text name="leg" property="keySize.width" styleClass="tiny"/>
                        y: <html:text name="leg" property="keySize.height" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >KeySpacing</td>
                    <td class="td4tiny" >
                        x: <html:text name="leg" property="keySpacing.width" styleClass="tiny"/>
                        y: <html:text name="leg" property="keySpacing.height" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >PostLabelCache</td>
                    <td class="td4tiny" > 
                        <html:radio name="leg" property="postLabelCache" styleClass="tiny" value="true"/>True
                        <html:radio name="leg" property="postLabelCache" styleClass="tiny" value="false"/>False
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Template</td>
                    <td class="td4tiny" > 
                        <logic:notEmpty name="leg" property="template">
                            <html:text name="leg" property="template.path" styleClass="tiny"/>
                        </logic:notEmpty>
                        <logic:empty name="leg" property="template">
                            <input type="text" name="template.path" class="tiny" value=""/>
                        </logic:empty>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Status</td>
                    <td class="td4tiny" > 
                        <html:select name="leg" property="status" styleClass="tiny">
                            <option value="<%=String.valueOf(Legend.ON)%>">On</option>
                            <option value="<%=String.valueOf(Legend.OFF)%>">Off</option>
                            <option value="<%=String.valueOf(Legend.EMBED)%>">Embeded</option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Transparent</td>
                    <td class="td4tiny" >
                        <html:radio name="leg" property="transparence" styleClass="tiny" value="<%=String.valueOf(Legend.ON)%>"/> On
                        <html:radio name="leg" property="transparence" styleClass="tiny" value="<%=String.valueOf(Legend.OFF)%>"/> Off
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html:html>
