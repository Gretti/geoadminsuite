<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@ page import="java.lang.reflect.*" %>

<HTML>
<HEAD>
	<TITLE>Scope Objects</TITLE>
	<STYLE>
		BODY  {font-family: Verdana; font-size: 8pt;}
		TABLE {font-family: Verdana; font-size: 8pt;}
		TD	{font-family: Verdana; font-size: 8pt;}
		FONT	{font-family: Verdana; font-size: 8pt;}
	</STYLE>
	<META HTTP-EQUIV="Pragma" CONTENT="No-cache">
	<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
	<META HTTP-EQUIV="Expires" CONTENT=0>
</HEAD>

<BODY>
<% 

//*** formatage des dates ***
SimpleDateFormat dateFormater = new SimpleDateFormat ("dd/MM/yyyy HH:mm:ss");

%>

<h3>Objets disponibles:</h3>
<ul>
  <li><b><a href="#application">Application</a></b></li>
  <li><b><a href="#session">Session</a></b></li>
</ul>
<FORM METHOD=POST >
  <!-- Paramètres de la session ------------------------------------------------------------------------------->
  <B><a name="session"></a>Paramètres de la session :</B><BR>
	<TABLE CELLPADDING="2" CELLSPACING="1" BORDER="0">
	<TR>
		<TD BGCOLOR="#A8A8A8">Paramètre</TD>
		<TD BGCOLOR="#A8A8A8">Valeur</TD>
	</TR>
	<TR>
		<TD BGCOLOR="#E1E1E1">Date de création :&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1"><%=dateFormater.format(new Date(session.getCreationTime()))%>&nbsp;</TD>
	</TR>
	<TR>
		<TD BGCOLOR="#E1E1E1">Dernier accès à la session :&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1"><%=dateFormater.format(new Date(session.getLastAccessedTime()))%>&nbsp;</TD>
	</TR>
	<TR>
		<TD BGCOLOR="#E1E1E1">Id de session :&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1"><%=session.getId()%>&nbsp;</TD>
	</TR>
	<TR>
		<TD BGCOLOR="#E1E1E1">Durée de session :&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1"><%=session.getMaxInactiveInterval()/60%> min&nbsp;</TD>
	</TR>
	<TR>
		<TD BGCOLOR="#E1E1E1">Nouvelle session (par 'isNew()') :&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1"><%=session.isNew()%>&nbsp;</TD>
	</TR>
	</TABLE>



	<!-- Liste des objets de la session ------------------------------------------------------------------------->
	<br><B>Attributs en session :</B><BR>
	<TABLE CELLPADDING="2" CELLSPACING="1" BORDER="0">
	<TR>
		<TD BGCOLOR="#A8A8A8" ALIGN="center">Name / Type</TD>
		<TD BGCOLOR="#A8A8A8" ALIGN="center">Contain</TD>
	</TR>

<% 	Enumeration names = session.getAttributeNames(); 
	while (names.hasMoreElements()) { 
            String objName = (String)names.nextElement(); 
%>
	<TR>
		<TD BGCOLOR="#E1E1E1"><B><FONT SIZE="2"><%=objName%></FONT></B><BR>&nbsp;</TD>
		<TD BGCOLOR="#E1E1E1">
			<FONT COLOR="red"><%=session.getAttribute(objName).toString()%></FONT><BR><%=session.getAttribute(objName).getClass().getName()%>&nbsp;
			<TABLE CELLSPACING="1" CELLPADDING="2" BORDER="0" BGCOLOR="black">
				<TR>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Méthode</B></TD>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Retourne</B></TD>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Type</B></TD>
				</TR>

<% 
			//Liste des methodes de cet objet et de ce qu'elles renvoient 
			Method[] m = session.getAttribute(objName).getClass().getMethods();
			for (int cpt=0; cpt < m.length; cpt++) {
				// si le nom de la method contient 'get', quelle ne prend aucun parametre
				if (((m[cpt].getName().indexOf("get")!=-1) || (m[cpt].getName().indexOf("is")!=-1)) && 
				(m[cpt].getParameterTypes().length==0)) {
%>
				<TR>
					<TD BGCOLOR="#F1F1F1" align="right"><%=m[cpt].getName()%> =&nbsp;&nbsp;</TD>
					<TD BGCOLOR="#F1F1F1">
						<% try { %>
							<FONT COLOR="#0000FF">&nbsp;&nbsp;
								<%=m[cpt].invoke(session.getAttribute(objName),(Object[])null)%>
							</FONT>
						<% } catch (Exception e) { %>
							<FONT COLOR="#FF3300"><%=e.getMessage()%></FONT>
						<% } %>
					</TD>
					<TD BGCOLOR="#F1F1F1"><%=m[cpt].getReturnType().getName()%> </TD>
				</TR>
				<%
				}
			}
			%>
			</TABLE>
		</TD>
	</TR>

	<% } %>
	</TABLE>
	<BR>
  <hr>
  <!-- Paramètres de l'application ------------------------------------------------------------------------------->
  <b><a name="application"></a>Application parameters:</b><br>
  <table cellpadding="2" cellspacing="1" border="0">
    <tr> 
      <td bgcolor="#A8A8A8">Parameter</td>
      <td bgcolor="#A8A8A8">Value</td>
    </tr>
    <tr> 
      <td bgcolor="#E1E1E1">Major Version :&nbsp;</td>
      <td bgcolor="#E1E1E1"><%=application.getMajorVersion()%>&nbsp;</td>
    </tr>
    <tr> 
      <td bgcolor="#E1E1E1">Minor Version :&nbsp;</td>
      <td bgcolor="#E1E1E1"><%=application.getMinorVersion()%>&nbsp;</td>
    </tr>
    <tr> 
      <td bgcolor="#E1E1E1">Real Path :&nbsp;</td>
      <td bgcolor="#E1E1E1"><%=application.getRealPath("")%>&nbsp;</td>
    </tr>
    <tr> 
      <td bgcolor="#E1E1E1">Server Info :&nbsp;</td>
      <td bgcolor="#E1E1E1"><%=application.getServerInfo()%> min&nbsp;</td>
    </tr>
  </table><br>
  <B><a name="application"></a>Application Init parameters:</B><BR>
	<TABLE CELLPADDING="2" CELLSPACING="1" BORDER="0">
	<TR>
		<TD BGCOLOR="#A8A8A8">Paramètre</TD>
		<TD BGCOLOR="#A8A8A8">Valeur</TD>
	</TR>
<% 	 names = application.getInitParameterNames(); 
	while (names.hasMoreElements()) { 
            String objName = (String)names.nextElement(); 
%>
	<TR>
		<TD BGCOLOR="#E1E1E1"><%=objName%></TD>
		<TD BGCOLOR="#E1E1E1"><%=application.getInitParameter(objName)%>&nbsp;</TD>
	</TR>
<%}%>
	</TABLE>
  <!-- Liste des objets de l'application ------------------------------------------------------------------------->
  <br>
  <B>Application Attributes :</B><BR>
	<TABLE CELLPADDING="2" CELLSPACING="1" BORDER="0">
	<TR>
		<TD BGCOLOR="#A8A8A8" ALIGN="center">Name / Type</TD>
		<TD BGCOLOR="#A8A8A8" ALIGN="center">Contain</TD>
	</TR>

<% 	 names = application.getAttributeNames(); 
	while (names.hasMoreElements()) { 
            String objName = (String)names.nextElement(); 
%>
	<TR>
		<TD BGCOLOR="#E1E1E1"><B><FONT SIZE="2"><%=objName%></FONT></B><BR>&nbsp;</TD>
		
      <TD BGCOLOR="#E1E1E1"> <FONT COLOR="red"><%=application.getAttribute(objName).toString()%></FONT><BR>
        <%=application.getAttribute(objName).getClass().getName()%>&nbsp;
			<TABLE CELLSPACING="1" CELLPADDING="2" BORDER="0" BGCOLOR="black">
				<TR>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Méthode</B></TD>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Retourne</B></TD>
					<TD BGCOLOR="#E1E1E1" align="center"><B>Type</B></TD>
				</TR>

<% 
			//Liste des methodes de cet objet et de ce qu'elles renvoient 
			Method[] m = application.getAttribute(objName).getClass().getMethods();
			for (int cpt=0; cpt < m.length; cpt++) {
				// si le nom de la method contient 'get', quelle ne prend aucun parametre
				if (((m[cpt].getName().indexOf("get")!=-1) || (m[cpt].getName().indexOf("is")!=-1)) && 
				(m[cpt].getParameterTypes().length==0)) {
%>
				<TR>
					<TD BGCOLOR="#F1F1F1" align="right"><%=m[cpt].getName()%> =&nbsp;&nbsp;</TD>
					<TD BGCOLOR="#F1F1F1">
						<% try { %>
							<FONT COLOR="#0000FF">&nbsp;&nbsp;
								<%=m[cpt].invoke(application.getAttribute(objName),(Object[])null)%>
							</FONT>
						<% } catch (Exception e) { %>
							<FONT COLOR="#FF3300"><%=e.getMessage()%></FONT>
						<% } %>
					</TD>
					<TD BGCOLOR="#F1F1F1"><%=m[cpt].getReturnType().getName()%> </TD>
				</TR>
				<%
				}
			}
			%>
			</TABLE>
		</TD>
	</TR>

	<% } %>
	</TABLE>
  <hr>
</FORM>
<!-- Liste des objets de la session ------------------------------------------------------------------------->
</BODY>
</HTML>



