<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@page contentType="text/html"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.geogurus.gas.utils.ObjectKeys" %>
<%@page import="org.geogurus.mapserver.objects.Map" %>
<%@page import="org.geogurus.mapserver.objects.ScaleBar" %>
<html:html locale="true">
    <bean:define id="scale" name="<%=ObjectKeys.USER_MAP_BEAN%>" property="mapfile.scaleBar"/>
    <head><title><bean:message key="scalebar_config"/></title>
        <link rel="stylesheet" href="css/gas.css" type="text/css">
        <SCRIPT>
            <!--
            <logic:present name="<%=ObjectKeys.REFRESH_KEY%>" scope="request">
                if (self.opener != null) self.opener.refreshExtent(); 
                else self.close();
            </logic:present>
            
            // opens a new window
            function openWindow(url, name) {
                var w = self.opener.open(url, name, "width=450,height=550,scrollbars=yes,resizable=yes,status=yes,toolbar=no,personalbar=no,locationbar=no,menubar=yes");
                w.focus();
            }

            // valide la forme
            function checkForm() {
                // color check (3 space-separated int)
                var col = document.form1.scalebar_color.value;
                if (col.length > 0) {
                    col = col.split(" ");
                    if (col.length != 3 || isNaN(col[0]) || isNaN(col[1]) || isNaN(col[2])) {
                        alert("<bean:message key="color_msg"/>");return false;
                    }
                }
                // outlinecolor check
                col = document.form1.scalebar_outlinecolor.value;
                if (col.length > 0) {
                    col = col.split(" ");
                    if (col.length != 3 || isNaN(col[0]) || isNaN(col[1]) || isNaN(col[2])) {
                        alert("<bean:message key="color_msg"/>");return false;
                    }
                }
                // imagecolor check
                col = document.form1.scalebar_imagecolor.value;
                if (col.length > 0) {
                    col = col.split(" ");
                    if (col.length != 3 || isNaN(col[0]) || isNaN(col[1]) || isNaN(col[2])) {
                        alert("<bean:message key="color_msg"/>");return false;
                    }
                }
                //backgroundcolor check
                col = document.form1.scalebar_bgcolor.value;
                if (col.length > 0) {
                    col = col.split(" ");
                    if (col.length != 3 || isNaN(col[0]) || isNaN(col[1]) || isNaN(col[2])) {
                        alert("<bean:message key="color_msg"/>");return false;
                    }
                }
                // intervals
                if (isNaN(document.form1.scalebar_intervals.value)) {
                        alert("<bean:message key="intervals_msg"/>");
                }

                var sizex = document.form1.scalebar_size_x.value;
                var sizey = document.form1.scalebar_size_y.value;
                if (isNaN(sizex) || isNaN(sizey)) {
                    alert("<bean:message key="msg_size_class"/>");return false;
                }

                if (document.form1.scalebar_style.value != 0 || document.form1.scalebar_style.value != 0) {
                        alert("<bean:message key="style_msg"/>");return false;
                }
                return true;
            }
            function sub() {
                //if (checkForm()) {
                    document.form1.submit();
                //}
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
                n=document.form1.dec.value
                Hexadec = (toHexa(n));
                if ((n > 0) & (n <= 255)) {document.form1.hex.value = Hexadec};
                if (n>255) {document.form1.dec.value = 255;Hexad()}
                if (n<=1) {document.form1.dec.value = 1;Hexad()};
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
        </SCRIPT>
    </head>
    <body class='body2' bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
        <form name="form1" method="post" action="MSScalebarProperties.jsv">
            <table border="0">
                <tr> 
                    <th colspan="2" class="th0"><bean:message key="scalebar_object_parameter"/>: </th>
                </tr>
                <tr> 
                    <td class="td4tiny">BackgroundColor</td>
                    <td class="td4tiny"> 
                        <span class="sampleColor" id="scalebar_bgcolor"></span>
                        <script>convToHex("<bean:write name="scale" property="backgroundColor"/>", "scalebar_bgcolor")</script>
                        <input type="text" name="backgroundColor" size=10 value="<bean:write name="scale" property="backgroundColor"/>" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">Color</td>
                    <td class="td4tiny"> 
                        <span class="sampleColor" id="scalebar_color"></span>
                        <script>convToHex("<bean:write name="scale" property="color"/>", "scalebar_color")</script>
                        <input type="text" name="color" size=10 value="<bean:write name="scale" property="color"/>" onchange="javascript:convToHex(this.value, 'scalebar_color');" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">ImageColor</td>
                    <td class="td4tiny"> 
                        <span class="sampleColor" id="scalebar_imagecolor"></span>
                        <script>convToHex("<bean:write name="scale" property="imageColor"/>", "scalebar_imagecolor")</script>
                        <input type="text" name="imageColor" size=10 value="<bean:write name="scale" property="imageColor"/>" onchange="javascript:convToHex(this.value, 'scalebar_imagecolor');" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" >Interlace</td>
                    <td height="28" class="td4tiny" > 
                        <html:checkbox name="scale" property="interlace" value="true"/>
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" >Intervals</td>
                    <td height="28" class="td4tiny" > 
                        <html:text name="scale" property="intervals" size="3" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" colspan="2" >
                        <a href='#' onclick="openWindow('MC_mapserver_label_properties.jsp?msobject=scalebartarget', 'labelprops')">
                            Label
                        </a> (<bean:message key="label_msg"/>)
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny">OutlineColor</td>
                    <td class="td4tiny"> 
                        <span class="sampleColor" id="scalebar_outlinecolor"></span>
                        <script>convToHex("<bean:write name="scale" property="outlineColor"/>", "scalebar_outlinecolor")</script>
                        <input type="text" name="outlineColor" size=10 value="<bean:write name="scale" property="outlineColor"/>" onchange="javascript:convToHex(this.value, 'scalebar_outlinecolor');" class="tiny">
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" >Position</td>
                    <td height="28" class="td4tiny" > 
                        <html:select name="scale" property="position" styleClass="tiny">
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.UL)%>">ul</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.UC)%>">uc</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.UR)%>">ur</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.LL)%>">ll</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.LC)%>">lc</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.LR)%>">lr</html:option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td height="28" class="td4tiny" >PostLabelCache</td>
                    <td height="28" class="td4tiny" > 
                        <html:checkbox name="scale" property="postLabelCache" value="true"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Size</td>
                    <td class="td4tiny" >
                        x: <html:text name="scale" property="size.width" size="3" styleClass="tiny"/>
                        y: <html:text name="scale" property="size.height" size="3" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Status</td>
                    <td class="td4tiny" >
                        <html:select name="scale" property="status" styleClass="tiny">
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.ON)%>">On</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.OFF)%>">Off</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.EMBED)%>">Embed</html:option>
                        </html:select>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Style</td>
                    <td class="td4tiny" > 
                        <html:text name="scale" property="style" size="3" styleClass="tiny"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Transparent</td>
                    <td class="td4tiny" > 
                        <html:checkbox name="scale" property="transparent" value="<%=String.valueOf(ScaleBar.ON)%>"/>
                    </td>
                </tr>
                <tr> 
                    <td class="td4tiny" >Units</td>
                    <td class="td4tiny" >
                        <html:select name="scale" property="units" styleClass="tiny">
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.METERS)%>">meters</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.KILOMETERS)%>">kilometers</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.FEET)%>">feet</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.INCHES)%>">inches</html:option>
                            <html:option styleClass="tiny" value="<%=String.valueOf(ScaleBar.MILES)%>">miles</html:option>
                        </html:select>
                    </td>
                </tr>
                <tr align="center"> 
                    <td class="td4tiny" colspan="2" > 
                        <input type="button" name="Submit3" value="<bean:message key="refresh"/>" onClick="sub()" class="tiny">
                        <input type="button" name="close" value="<bean:message key="close"/>" onClick="self.close();" class="tiny">
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html:html>
