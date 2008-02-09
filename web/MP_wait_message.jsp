<%@  page import="java.util.*" %>
<%@ page contentType="text/html"%>

<html>
<%
//internationalization:
ResourceBundle messages = (ResourceBundle)session.getAttribute("messages");
%>
<head>
<title><%=messages.getString("msg_wait_download")%></title>
<link rel="stylesheet" href="geonline.css" type="text/css">
<script>
</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<%=messages.getString("msg_wait_download")%>
</BODY>
</html>