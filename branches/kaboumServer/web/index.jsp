<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<html>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <head>
        <title>KaboumServer test application</title>
    </head>
    <style type="text/css" media="screen">
        body {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 12px;
            text-align: justify;
            background-color: #FFFFFF;
        }
        th   {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 9pt;
            font-weight: bold;
            background-color: #000099;
            color: #FFFFFF;
        }
        td   {  
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 9pt;
        }
        form   {  
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 9pt;
        }
        h1   {  
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 16pt;
            font-weight: bold;
        }
        h2   {  
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 14pt;
            font-weight: bold;
        }
        h3   {  
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 12pt;
            font-weight: bold;
        }
        h4   { 
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 10pt;
            font-weight: bold;
        }
        h5   {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 8pt;
            font-weight: bold;
        }
        h6   {
            font-family: Verdana, Arial, Helvetica, sans-serif;
            font-size: 6pt;
            font-weight: bold;
        }
        .tiny {  font-family: Verdana, Arial, Helvetica, sans-serif; color: #000000;font-size: 8pt;}
    </style>
    <body>
        <form name="myForme">
            <table width="100%"  border="0">
                <tr>
                    <td style="vertical-align:top;background-color:#ccc;">
                        <br><br>
                        <p><strong><a href="readme.html">KaboumServer Installation Guide (French)</a></strong></p>
                        <p><strong><a href="TestDS">Test DS</a></strong></p>
                        <p><strong><a href="index.jsp?testEnv=true">Test the configuration</a></strong></p>
                        <p><strong><a href='data/kaboum.html'>Launch application</a></strong></p>
                        <p><strong><a href='#testpage'>How to Configure application</a></strong></p>
                        <p><strong><a href='#postgis'>How to Install Postgis database</a></strong></p>
                        <!--<p><strong><a href='data/kaboum_orthographic.html'>Kaboum orthographic projection test</a></strong></p>-->
                    </td>
                    <td style="vertical-align:top;">
                        <!-- declares some test functions -->
<%!
// returns true if all needed files exists
    public boolean checkBaseDataFile(String wp) {
        File kaboumHtml = null;
        File mapfile = null;
        File shape = null;
        File raster = null;
        File jar = null;
        File js = null;

        try {
            kaboumHtml = new File(wp + "/data/kaboum.html");
            mapfile = new File(wp + "/data/mapserver/test.map");
            shape = new File(wp + "/data/files/departements.shp");
            raster = new File(wp + "/data/images/CRG_1810.tif");
            jar = new File(wp + "/data/kaboum/kaboum.jar");
            js = new File(wp + "/data/kaboum/kaboum.js");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // test file existence
        return (kaboumHtml.exists() && mapfile.exists() && shape.exists() &&
                raster.exists() && jar.exists() && js.exists());
    }

//returns an array of Strings. arr[0] is the mapserver URL, arr[1] is the mapfile path
    public String[] getAppletParameters(File kaboumHtml) {
        String[] res = new String[2];
        String msURL = null;
        String mapPath = null;

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(kaboumHtml));
            String line = null;

            while ((line = reader.readLine()) != null) {
                int idxMap = line.indexOf("<param name=\"MAPFILE_PATH\" value=\"");
                int idxMS = line.indexOf("<param name=\"KABOUM_MAPSERVER_CGI_URL\" value=\"");

                if (idxMap >= 0) {
                    // extract mapfile path
                    mapPath = line.substring(idxMap + ("<param name=\"MAPFILE_PATH\" value=\"".length()), line.lastIndexOf("\">")).trim();
                }
                if (idxMS >= 0) {
                    // extract MS URL
                    msURL = line.substring(idxMS + ("<param name=\"KABOUM_MAPSERVER_CGI_URL\" value=\"".length()), line.lastIndexOf("\">")).trim();
                }
            }
            reader.close();
            res[0] = msURL;
            res[1] = mapPath;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

// return true if given string can be converted to a valid URL
    public boolean testURL(String u) {
        if (u == null) {
            return false;
        }

        URL url = null;
        try {
            url = new URL(u);
            url.openStream();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

// returns true if the given string can be converted to an existing file
    public boolean testFile(String f) {
        if (f == null) {
            return false;
        }

        File file = null;
        try {
            file = new File(f);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean testPostgis(String gtParams) {
        if (gtParams == null) {
            return false;
        }
        Connection c = null;
        try {
            StringTokenizer tok = new StringTokenizer(gtParams, ",");
            String host = null;
            String dbname = null;
            String dbport = null;
            String user = null;
            String pwd = null;

            while (tok.hasMoreTokens()) {
                StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), "=");
                String p = tok2.nextToken().trim();
                String v = tok2.nextToken().trim();

                if (p.equalsIgnoreCase("database")) {
                    dbname = v;
                } else if (p.equalsIgnoreCase("host")) {
                    host = v;
                } else if (p.equalsIgnoreCase("port")) {
                    dbport = v;
                } else if (p.equalsIgnoreCase("user")) {
                    user = v;
                } else if (p.equalsIgnoreCase("passwd")) {
                    pwd = v;
                }
            }

            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://" + host + "/" + dbname, user, pwd);
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("select postgis_full_version()");

            if (!rs.next()) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean testDatastoreParam(String param) {
        //System.out.println(param);
        if (param == null) {
            return false;
        }

        if (param.indexOf("url=") >= 0) {
            return testURL(param.substring(param.indexOf("url=") + "url=".length()).trim());
        } else if (param.indexOf("postgis") >= 0) {
            return testPostgis(param);
        }
        //TODO: add other datasource tests.
        return false;
    }

// extract all _DATASTORE_PARAMS kaboum parameter and returns a vector of string
// containing them
    public Vector getDatastoreParams(File props) {
        if (props == null || !props.exists()) {
            return null;
        }
        BufferedReader reader = null;
        Vector vec = new Vector();
        try {
            reader = new BufferedReader(new FileReader(props));
            String line = null;
            int idx = -1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && line.trim().charAt(0) != '#') {
                    // skips comments
                    idx = line.indexOf("_DATASTORE_PARAMS=");
                    if (idx >= 0) {
                        String param = line.substring(idx + "_DATASTORE_PARAMS=".length()).trim();
                        if (param.length() > 0) {
                            vec.add(line.substring(idx + "_DATASTORE_PARAMS=".length()).trim());
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return vec;
    }

// gets featureURL from applet paramters
    public String getKaboumServerURL(File kaboumHtml) {
        if (kaboumHtml == null) {
            return "Null Kaboum html page";
        }
        String res = null;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(kaboumHtml));
            String line = null;

            while ((line = reader.readLine()) != null) {
                if (line.indexOf("KABOUM_FEATURESERVER_URL") >= 0) {
                    res = line.trim();
                    res = res.substring(res.indexOf("<param name=\"KABOUM_FEATURESERVER_URL\" value=\"") +
                            "<param name=\"KABOUM_FEATURESERVER_URL\" value=\"".length());
                    res = res.substring(0, res.lastIndexOf("\">"));
                    break;
                }
            }
            reader.close();


        } catch (Exception e) {
            return "an exception occured: " + e.getMessage();
        }
        return res;
    }
%>
<%
//perform tests

            if (request.getParameter("testEnv") != null) {
                String msg = "<font color='green'> Environment seems to be valid !</font><h3><a href='data/kaboum.html'>";
                msg += "Launch the application</a></h3><hr/>";

                out.print("<p>Below is a generated message telling if KaboumServer environment test is valid or not<br>");
                out.print("If it is not, a message will tell you how to fix it </p>");
                out.print("<hr/>");


                String invalidEnvMessage = "<table border=1 style='border-color: red;'><tr><td><font color='red'><strong>The test environment seems to be invalid.</strong></font><p>";
                String endInvalidEnvMessage = "</td></tr></table>";
                String webAppPath = config.getServletContext().getRealPath("");
                File kaboumProps = new File(webAppPath + System.getProperty("file.separator") + "kaboumServer.properties");
                File kaboumHtml = new File(webAppPath + System.getProperty("file.separator") + "data" + System.getProperty("file.separator") + "kaboum.html");
                String[] params = getAppletParameters(kaboumHtml);
                boolean isValid = true;
                String serverURL = getKaboumServerURL(kaboumHtml);


                if (!checkBaseDataFile(webAppPath)) {
                    msg = invalidEnvMessage;
                    msg += "Missing some test files<p>";
                    msg += "Please, checkout the KaboumData module in the same folder as the one containing";
                    msg += "this KaboumServer module and builds it with its ant script.<p>";
                    msg += "Then, re-test the environment with this page and follow instructions";
                    msg += endInvalidEnvMessage;
                    isValid = false;
                } else if (!testURL(params[0])) {
                    msg = invalidEnvMessage;
                    msg += "Invalid MapServer URL: <code>" + params[0] + "</code><p>";
                    msg += "Edit the data/kaboum.html file and set the KABOUM_MAPSERVER_CGI_URL applet properties to a valid URL to MapServer";
                    msg += endInvalidEnvMessage;
                    isValid = false;
                } else if (!testURL(serverURL)) {
                    msg = invalidEnvMessage;
                    msg += "Invalid KaboumServer URL: <code>" + serverURL + "</code><p>";
                    msg += "Edit the data/kaboum.html file and set the KABOUM_FEATURESERVER_URL applet properties to point to this webapp URL";
                    msg += endInvalidEnvMessage;
                    isValid = false;
                } else if (!testFile(params[1])) {
                    msg = invalidEnvMessage;
                    msg += "Invalid MapServer Mapfile: <code>" + params[1] + "</code><p>";
                    msg += "Edit the data/kaboum.html file and set the MAPFILE_PATH applet properties to point to " + webAppPath + "/data/mapserver/test.map<br>";
                    msg += endInvalidEnvMessage;
                    isValid = false;
                } else if (!kaboumProps.exists()) {
                    msg = invalidEnvMessage;
                    msg += "Invalid KaboumServer properties file: <code>" + kaboumProps.getAbsolutePath() + "</code><p>";
                    msg += "To run KaboumServer test, you must leave the provided kaboumServer.properties file in its folder and just edit it";
                    msg += endInvalidEnvMessage;
                    isValid = false;
                } else {
                    Vector vec = getDatastoreParams(kaboumProps);
                    if (vec != null) {
                        for (int i = 0; i < vec.size(); i++) {
                            if (!testDatastoreParam((String) vec.get(i))) {
                                msg = invalidEnvMessage;
                                msg += "Invalid datastore parameters: <code>" + (String) vec.get(i) + "</code><p>";
                                msg += "Check the KaboumServer properties file to see if configured parameters for Data Store are valid";
                                msg += endInvalidEnvMessage;
                                isValid = false;
                                break;
                            }
                        }
                    }
                }
                out.print(msg);
            }
%>
                        <h1>KaboumServer module Test Page</h1>
                        <p>KaboumServer is a Java Server component (servlet and java classes) allowing to retrieve geographic objects to manipulate them in the Kaboum Applet. (Thus, KaboumServer depends on the Kaboum applet project).</p>
                        <p>Kaboum applet accesses a Kaboum Server through the configured server URL. According to the properties defined in Kaboum Server, geometric objects are retrieved from configured data sources and passed to the applet.</p>
                        
                        
                        <h2><a name="testpage">How to configure the module test page</a></h2>
                        <ul>
                            <li>First, download the KaboumData module in the same folder as the KaboumServer is. You should have the 3 Kaboum modules in the same folder, like this:<br/>
                                <img src="folders.png" width="292" height="120">
                            </li><br/>
                            <li>Follow instructions in readme.html to configure the KaboumData test environment.</li>
                            <li>When it's done (you see a map of the France in your web browser), execute the build.xml ant script:<br>
                            this script copies the KaboumData data into the KaboumServer 'data' subfolder </li>
                            <li>Edit the kaboum.html file that was copied in the previous step in the 'data' folder of this webapp</li>
                            <li><strong>Add the following parameter </strong>in the list of Kaboum Applet parameters:<br>
                                <br>
                                name: <code>KABOUM_FEATURESERVER_URL</code><br>
                            value: the URL to this webapp (Ex. <code>http://yourhost/KaboumServer/KaboumFeatureServlet</code>)</li>
                            <li><strong>Clean and rebuild this webapp </strong>after editing the kaboum.html file<br>
                            </li>
                            <li>You can then use the &quot;test the configuration&quot; link in the left part of this page to test the configuration. </li>
                            <li>Click on the &quot;<strong><a href="data/kaboum.html">Launch application</a></strong>&quot; link to run this webapp ! </li>
                        </ul>              
                        
                        <h2><a name="postgis">How to install the test Postgis database</a></h2>
                        <p>To test KaboumServer, a Postgis database can be created to acces demo data (points, lines, polygons)</p>
                        <ul>
                            <li>First install a valid <a href="http://postgresql.org">Postgresql</a>/<a href="http://www.postgis.org">Postgis</a> instance.</li>
                            <li>Then, in the 'DB folder' of the KaboumData module, you will find a script to create the kaboum_test database.</li>
                            <li>Launch it or adapt it to match your current database configuration </li>
                            <li>Edit the KaboumServer.properties file (you will find it at the root of the KaboumServer webapp)and configure your datasource(s) </li>
                        </ul>
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>
