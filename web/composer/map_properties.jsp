<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Map" %>
<html:html locale="true">
    <head><title><bean:message key="map_title"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <script type="text/javascript">
            <!--
            // opens a new window
            function openWindow(width, height, url, name) {
                var w = window.open(url, name, "width=" + width + ",height=" + height + ",scrollbars=yes,resizable=yes,statusbar=yes,toolbar=no,personalbar=no,locationbar=no,menubar=yes");
                w.focus();
            }

            // checks form fields to avoid passing bad values to the servlet
            function checkForm() {
                // extent check (4 space-separated double)
                var ext = document.getElementsByName("extent")[0].value;
                var tbl = ext.split(" ");
                if (tbl.length != 4 || isNaN(tbl[0]) || isNaN(tbl[1]) || isNaN(tbl[2]) || isNaN(tbl[3])) {
                    alert("<bean:message key="extent_msg"/>");return false;
                }

                // color check (3 space-separated int)
                var col = document.getElementsByName("imageColor")[0].value;
                tbl = col.split(" ");
                if (tbl.length != 3 || isNaN(tbl[0]) || isNaN(tbl[1]) || isNaN(tbl[2])) {
                    alert("<bean:message key="color_msg"/>");return false;
                }

                // imagequality check
                if (isNaN(document.getElementsByName("imageQuality")[0].value)) {
                    alert("<bean:message key="imagequality_msg"/>");return false;
                }
                // size check:
                if (isNaN(document.getElementsByName("width")[0].value) || isNaN(document.getElementsByName("height")[0].value)) {
                    alert("<bean:message key="size_msg"/>");return false;
                }
                // scale check:
                if (isNaN(document.getElementsByName("scale")[0].value)) {
                    alert("<bean:message key="scale_msg"/>");return false;
                }
                // resolution check
                if (isNaN(document.getElementsByName("resolution")[0].value)) {
                    alert("<bean:message key="resolution_msg"/>");return false;
                }
                return true;
            }

            function sub() {
                if (checkForm()) {
                    Ext.Ajax.request({
                    url:'mapProperties.do',
                    waitMsg:'Loading',
                    params: Ext.Ajax.serializeForm(document.forms["MapForm"]),
                    callback: function(){
                            GeneralLayout.composermap.layers[1].mergeNewParams({'timestamp':Math.random()});
                        }
                    });                
                    return true;
                }
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
                n=document.forms[0].dec.value
                Hexadec = (toHexa(n));
                if ((n > 0) & (n <= 255)) {document.forms[0].hex.value = Hexadec};
                if (n>255) {document.forms[0].dec.value = 255;Hexad()}
                if (n<=1) {document.forms[0].dec.value = 1;Hexad()};
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
            //-->
        </script>
    </head>
    <body class='body2' bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <bean:define id="mapfile" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile"/>
        <bean:define id="size" name="mapfile" property="size" type="java.awt.Dimension"/>
        <bean:define id="symbolSetFile" name="mapfile" property="symbolSet.symbolSetFile"/>
        <html:form method="post" action="mapProperties.do">
            <table border="0">
                <tr> 
                    <th class="th0" colspan="2" class="tinynocolor"><bean:message key="map_param"/>: </th>
                </tr>
                <tr> 
                    <td class="td4tiny">Extent (space-separated)</td>
                    <td class="td4tiny">
                        <html:text name="mapfile" property="extent" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">FontSet</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="fontSet" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">ImageColor</td>
                    <td class="td4tiny"> 
                        <span class="sampleColor" id="imageColor"></span>
                        <script>convToHex("<bean:write name="mapfile" property="imageColor"/>", "imageColor")</script>
                        <html:text name="mapfile"  property="imageColor" styleClass="tiny" onchange="javascript:convToHex(this.value,'imageColor');"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">ImageType</td>
                    <td class="td4tiny"> 
                        <html:select name="mapfile" property="imageType" styleClass="tiny">
                            <html:option value="<%=String.valueOf(Map.GIF)%>" styleClass="tiny">gif</html:option>
                            <html:option  value="<%=String.valueOf(Map.JPEG)%>" styleClass="tiny">jpeg</html:option>
                            <html:option  value="<%=String.valueOf(Map.PNG)%>" styleClass="tiny">png</html:option>
                            <html:option  value="<%=String.valueOf(Map.WBMP)%>" styleClass="tiny">wbmp</html:option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">ImageQuality</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="imageQuality" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Interlace</td>
                    <td class="td4tiny">
                        <html:radio name="mapfile" property="interlace" styleClass="tiny" value="<%=String.valueOf(Map.ON)%>"/>On
                        <html:radio name="mapfile" property="interlace" styleClass="tiny" value="<%=String.valueOf(Map.OFF)%>"/>Off
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Name</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="name" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Size</td>
                    <td class="td4tiny"> 
                        Width: <html:text name="size" property="width" styleClass="tiny"/><br>
                        Height: <html:text name="size" property="height" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Resolution</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="resolution" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Scale</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="scale" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Status</td>
                    <td class="td4tiny"> 
                        <html:radio name="mapfile" property="status" styleClass="tiny" value="<%=String.valueOf(Map.ON)%>"/>On
                        <html:radio name="mapfile" property="status" styleClass="tiny" value="<%=String.valueOf(Map.OFF)%>"/>Off
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Shapepath</td>
                    <td class="td4tiny"> 
                        <html:text name="mapfile" property="shapePath" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Symbolset</td>
                    <td class="td4tiny"> 
                        <html:text name="symbolSetFile" property="canonicalPath" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Transparent</td>
                    <td class="td4tiny"> 
                        <html:radio name="mapfile" property="transparent" styleClass="tiny" value="<%=String.valueOf(Map.ON)%>"/>On
                        <html:radio name="mapfile" property="transparent" styleClass="tiny" value="<%=String.valueOf(Map.OFF)%>"/>Off
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Units</td>
                    <td class="td4tiny"> 
                        <html:select name="mapfile" property="units" styleClass="tiny">
                            <html:option value="<%=String.valueOf(Map.METERS)%>" styleClass="tiny">meters</html:option>
                            <html:option value="<%=String.valueOf(Map.KILOMETERS)%>" styleClass="tiny">kilometers</html:option>
                            <html:option value="<%=String.valueOf(Map.FEET)%>" styleClass="tiny">feet</html:option>
                            <html:option value="<%=String.valueOf(Map.INCHES)%>" styleClass="tiny">inches</html:option>
                            <html:option value="<%=String.valueOf(Map.MILES)%>" styleClass="tiny">miles</html:option>
                            <html:option value="<%=String.valueOf(Map.DD)%>" styleClass="tiny">dd</html:option>
                        </html:select>
                    </td>
                </tr>
            </table>
        </html:form>
    </body>
</html:html>