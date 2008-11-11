/*
 * Copyright (C) 2008  Camptocamp
 *
 * This file is part of MapFish Server
 *
 * MapFish Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with MapFish Server.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geogurus.gas.print;

import org.mapfish.print.*;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import org.json.JSONException;
import org.json.JSONWriter;
import org.pvalsecc.misc.FileUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

/**
 * Main print servlet.
 */
public class MapPrinterServlet extends BaseMapServlet {

    private static final String INFO_URL = "/info.json";
    private static final String PRINT_URL = "/print.pdf";
    private static final String CREATE_URL = "/create.json";
    private static final String TEMP_FILE_PREFIX = "mapfish-print";
    private static final String TEMP_FILE_SUFFIX = ".pdf";
    private static final int TEMP_FILE_PURGE_SECONDS = 600;
    private File tempDir = null;
    /**
     * Map of temporary files.
     */
    private final Map<String, TempFile> tempFiles = new HashMap<String, TempFile>();
    private static final String USER_CONFIG_COMPONENTS_PREFIX = "printTpl";

    public enum PrintComponent {

        header("header"),
        footer("footer"),
        map(USER_CONFIG_COMPONENTS_PREFIX + "Map"),
        title(USER_CONFIG_COMPONENTS_PREFIX + "Title"),
        comment(USER_CONFIG_COMPONENTS_PREFIX + "Comment"),
        scale(USER_CONFIG_COMPONENTS_PREFIX + "Comment"),
        legend(USER_CONFIG_COMPONENTS_PREFIX + "Comment"),
        image(USER_CONFIG_COMPONENTS_PREFIX + "Comment"),
        north(USER_CONFIG_COMPONENTS_PREFIX + "Comment"),
        overview(USER_CONFIG_COMPONENTS_PREFIX + "Scale");
        /** L'attribut qui contient la valeur associé à l'enum */
        private final String value;

        /** Le constructeur qui associe une valeur à l'enum */
        private PrintComponent(String value) {
            this.value = value;
        }

        /** La méthode accesseur qui renvoit la valeur de l'enum */
        public String getValue() {
            return this.value;
        }
    };

    public String getPrintComponentYaml(PrintComponent component) {
        String yaml = null;
        switch (component) {
            case header:
                yaml = "dpis:\n" +
                        "  - 254\n" +
                        "  - 190\n" +
                        "  - 127\n" +
                        "  - 56\n" +
                        "\n" +
                        "scales:\n" +
                        "  - 500\n" +
                        "  - 1000\n" +
                        "  - 2500\n" +
                        "  - 5000\n" +
                        "  - 10000\n" +
                        "  - 25000\n" +
                        "  - 50000\n" +
                        "  - 100000\n" +
                        "  - 250000\n" +
                        "  - 500000\n" +
                        "  - 1000000\n" +
                        "  - 2500000\n" +
                        "  - 5000000\n" +
                        "  - 10000000\n" +
                        "  - 15000000\n" +
                        "  - 20000000\n" +
                        "  - 25000000\n" +
                        "  - 50000000\n" +
                        "  - 100000000\n" +
                        "  - 250000000\n" +
                        "  \n" +
                        "hosts:\n" +
                        "  - !localMatch\n" +
                        "    dummy: true\n" +
                        "  - !ipMatch\n" +
                        "    ip: www.camptocamp.org\n" +
                        "  - !dnsMatch\n" +
                        "    host: labs.metacarta.com\n" +
                        "    port: 80\n" +
                        "\n" +
                        "layouts:\n" +
                        "  User Defined:\n" +
                        "   mainPage:\n" +
                        "      pageSize: [pagesize]\n" +
                        "      rotation: true\n" +
                        "      landscape:[orientation]\n";
                break;
            case map:
                yaml = "            - !map\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              height: $h\n";
                break;
            case title:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !text\n" +
                        "                  font: Helvetica\n" +
                        "                  fontSize: 30\n" +
                        "                  text: '${mapTitle}'\n";
                break;
            case comment:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !text\n" +
                        "                  text: |\n" +
                        "                    ${comment}\n" +
                        "                    \n" +
                        "                    Angle: ${rotation}°   Scale: 1:${format %,d scale}\n";
                break;
            case scale:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !scalebar\n" +
                        "                  maxSize: $w\n" +
                        "                  height: $h\n" +
                        "                  type: 'bar'\n" +
                        "                  units: m\n" +
                        "                  intervals: 5\n";
                break;
            case legend:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !legends\n" +
                        "                  maxIconWidth: 8\n" +
                        "                  maxIconHeight: 8\n" +
                        "                  classIndentation: 20\n" +
                        "                  layerSpace: 5\n" +
                        "                  classSpace: 2\n" +
                        "                  layerFont: Helvetica\n" +
                        "                  layerFontSize: 10\n" +
                        "                  classFont: Helvetica\n" +
                        "                  classFontSize: 8\n";
                break;
            case image:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !image\n" +
                        "                  maxWidth: $w\n" +
                        "                  maxHeight: $w\n" +
                        "                  url: '$u'\n";
                break;
            case north:
                yaml = 
                        "            - !columns\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n" +
                        "              width: $w\n" +
                        "              items: \n" +
                        "                - !image\n" +
                        "                  maxWidth: $w\n" +
                        "                  maxHeight: $h\n" +
                        "                  rotation: '${rotation}'\n" +
                        "                  url: 'http://localhost:8084/geoadminsuite/images/north.gif'\n              rotation: '${rotation}'\n";
                break;
            case overview:
                yaml = "            - !map\n" +
                        "              width: $w\n" +
                        "              height: $h\n" +
                        "              overviewMap: 2\n" +
                        "              absoluteX: $x\n" +
                        "              absoluteY: $y\n";
                break;
            case footer:
                yaml = "      footer:\n" +
                        "        height: 30\n" +
                        "        items:\n" +
                        "          - !columns\n" +
                        "            items:\n" +
                        "              - !text\n" +
                        "                align: left\n" +
                        "                text: Produced by geoadminsuite\n" +
                        "                fontSize:8\n" +
                        "              - !text\n" +
                        "                align: right\n" +
                        "                fontSize:8\n" +
                        "                text: 'Page ${pageNum}'";
                break;
        }
        return yaml;
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final String additionalPath = httpServletRequest.getPathInfo();
        if (additionalPath.equals(PRINT_URL)) {
            createAndGetPDF(httpServletRequest, httpServletResponse);
        } else if (additionalPath.equals(INFO_URL)) {
            getInfo(httpServletRequest, httpServletResponse, getBaseUrl(httpServletRequest));
        } else if (additionalPath.startsWith("/") && additionalPath.endsWith(TEMP_FILE_SUFFIX)) {
            getPDF(httpServletResponse, additionalPath.substring(1, additionalPath.length() - 4));
        } else {
            error(httpServletResponse, "Unknown method: " + additionalPath, 404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        final String additionalPath = httpServletRequest.getPathInfo();
        if (additionalPath.equals(CREATE_URL)) {
            createPDF(httpServletRequest, httpServletResponse, getBaseUrl(httpServletRequest));
        } else {
            error(httpServletResponse, "Unknown method: " + additionalPath, 404);
        }
    }

    @Override
    public void init() throws ServletException {
        //get rid of the temporary files that where present before the applet was started.
        File dir = getTempDir();
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            final String name = file.getName();
            if (name.startsWith(TEMP_FILE_PREFIX) &&
                    name.endsWith(TEMP_FILE_SUFFIX)) {
                deleteFile(file);
            }
        }
    }

    @Override
    public void destroy() {
        synchronized (tempFiles) {
            for (File file : tempFiles.values()) {
                deleteFile(file);
            }
            tempFiles.clear();
        }
        super.destroy();
    }

    /**
     * All in one method: create and returns the PDF to the client.
     */
    private void createAndGetPDF(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        //get the spec from the query
        try {
            httpServletRequest.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final String spec = httpServletRequest.getParameter("spec");
        if (spec == null) {
            error(httpServletResponse, "Missing 'spec' parameter", 500);
            return;
        }

        File tempFile = null;
        try {
            MapPrinter printer = getMapPrinter();
            tempFile = doCreatePDFFile(printer, spec);
            sendPdfFile(httpServletResponse, tempFile);
        } catch (Throwable e) {
            error(httpServletResponse, e);
        } finally {
            deleteFile(tempFile);
        }
    }

    /**
     * Create the PDF and returns to the client (in JSON) the URL to get the PDF.
     */
    private void createPDF(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String basePath) throws ServletException {
        TempFile tempFile = null;
        try {
            purgeOldTemporaryFiles();

            BufferedReader data = httpServletRequest.getReader();
            StringBuilder spec = new StringBuilder();
            String cur;
            while ((cur = data.readLine()) != null) {
                spec.append(cur).append("\n");
            }

            MapPrinter printer;
            String strSpec;
            JSONObject jsonSpec = new JSONObject(spec.toString());
            if (!jsonSpec.getJSONArray("pages").getJSONObject(0).has("config") ||
                    jsonSpec.getJSONArray("pages").getJSONObject(0).getString("config").length() == 0) {
                printer = getMapPrinter();
                strSpec = spec.toString();
            } else {

                jsonSpec.put("layout", "User Defined");
                strSpec = jsonSpec.toString();
                int marginLeft = 40;
                int marginRight = 40;
                int marginTop = 20;
                int marginBottom = 20;
                JSONObject jsonConf = new JSONObject(jsonSpec.getJSONArray("pages").getJSONObject(0).getString("config"));

                StringBuilder yamlString = new StringBuilder("");
                //Builds a new yaml string to build printer from config key sent
                //First dumps a yaml config with header and footer pre-parametrized
                String header = getPrintComponentYaml(PrintComponent.header);
                header = header.replace("[orientation]", String.valueOf(jsonConf.getString("orientation").equals("landscape")));
                header = header.replace("[pagesize]", jsonConf.getString("layout"));
                yamlString.append(header);

                //Fills layouts part with passed userconfig
                Rectangle rect = PageSize.getRectangle(jsonConf.getString("layout"));
                float height = rect.getHeight() - (marginTop + marginBottom);
                float width = rect.getWidth() - (marginLeft + marginRight);
                if (jsonConf.getString("orientation").equals("landscape")) {
                    width = rect.getHeight() - (marginTop + marginBottom);
                    height = rect.getWidth() - (marginLeft + marginRight);
                }
                float left = rect.getLeft() + marginLeft;
                float bottom = rect.getBottom() + marginBottom;

                JSONObject components = new JSONObject(jsonConf.getString("components"));
                yamlString.append("      items:\n");
                for (Iterator iteComp = components.keys(); iteComp.hasNext();) {
                    String compkey = (String) iteComp.next();
                    String compModKey = compkey;
                    if (compkey.contains("Image")) {
                        compModKey = "printTplImage";
                    }
                    PrintComponent printComp = PrintComponent.valueOf(compModKey.substring(USER_CONFIG_COMPONENTS_PREFIX.length()).toLowerCase());
                    String compString = getPrintComponentYaml(printComp);
                    JSONObject comp = new JSONObject(components.getString(compkey));
                    long w = Math.round((comp.getDouble("width") * width));
                    long h = Math.round((comp.getDouble("height") * height));
                    long x = Math.round((comp.getDouble("dX") * width) + left);
                    long y = Math.round((comp.getDouble("dY") * height) + bottom);
                    String url = comp.getString("url");
                    
                    compString = compString.replace("$x", String.valueOf(x));
                    compString = compString.replace("$y", String.valueOf(y));
                    compString = compString.replace("$w", String.valueOf(w));
                    if (compString.contains("$h")) {
                        compString = compString.replace("$h", String.valueOf(h));
                    }
                    if (compString.contains("$u")) {
                        compString = compString.replace("$u", url);
                    }
                    yamlString.append(compString);
                }
                String footer = getPrintComponentYaml(PrintComponent.footer);
                yamlString.append(footer);

                System.out.println("YAML = \n" + yamlString.toString());

                printer = new MapPrinter(yamlString.toString(), ".");
            //printer = getMapPrinter();
            }


            tempFile = doCreatePDFFile(printer, strSpec);
            if (tempFile == null) {
                error(httpServletResponse, "Missing 'spec' parameter", 500);
                return;
            }
        } catch (Throwable e) {
            deleteFile(tempFile);
            error(httpServletResponse, e);
        }

        final String id = generateId(tempFile);
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try {
            final PrintWriter writer = httpServletResponse.getWriter();
            JSONWriter json = new JSONWriter(writer);
            json.object();
            {
                json.key("getURL").value(basePath + "/" + id + TEMP_FILE_SUFFIX);
            }
            json.endObject();
        } catch (JSONException e) {
            deleteFile(tempFile);
            throw new ServletException(e);
        } catch (IOException e) {
            deleteFile(tempFile);
            throw new ServletException(e);
        }
        synchronized (tempFiles) {
            tempFiles.put(id, tempFile);
        }
    }

    /**
     * To get the PDF created previously.
     */
    private void getPDF(HttpServletResponse httpServletResponse, String id) throws IOException {
        final File file;
        synchronized (tempFiles) {
            file = tempFiles.get(id);
        }
        if (file == null) {
            error(httpServletResponse, "File with id=" + id + " unknown", 404);
            return;
        }

        sendPdfFile(httpServletResponse, file);
    }

    /**
     * To get (in JSON) the information about the available formats and CO.
     */
    protected void getInfo(HttpServletRequest req, HttpServletResponse resp, String basePath) throws ServletException, IOException {
        MapPrinter printer = getMapPrinter();
        resp.setContentType("application/json; charset=utf-8");
        final PrintWriter writer = resp.getWriter();

        final String var = req.getParameter("var");
        if (var != null) {
            writer.print(var + "=");
        }

        JSONWriter json = new JSONWriter(writer);
        try {
            json.object();
            {
                printer.printClientConfig(json);
                json.key("printURL").value(basePath + PRINT_URL);
                json.key("createURL").value(basePath + CREATE_URL);
            }
            json.endObject();
        } catch (JSONException e) {
            throw new ServletException(e);
        }
        if (var != null) {
            writer.print(";");
        }
        writer.close();
    }

    /**
     * Do the actual work of creating the PDF temporary file.
     */
    private TempFile doCreatePDFFile(MapPrinter printer, String spec) throws IOException, DocumentException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generating PDF for spec=" + spec);
        }

        //create a temporary file that will contain the PDF
        TempFile tempFile = new TempFile(File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX, getTempDir()));
        try {
            FileOutputStream out = new FileOutputStream(tempFile);

            printer.print(spec, out);
            out.close();
            return tempFile;
        } catch (IOException e) {
            deleteFile(tempFile);
            throw e;
        } catch (DocumentException e) {
            deleteFile(tempFile);
            throw e;
        }
    }

    /**
     * copy the PDF into the output stream
     */
    private void sendPdfFile(HttpServletResponse httpServletResponse, File tempFile) throws IOException {
        FileInputStream pdf = new FileInputStream(tempFile);
        final OutputStream response = httpServletResponse.getOutputStream();
        httpServletResponse.setContentType("application/pdf");
        FileUtilities.copyStream(pdf, response);
        pdf.close();
        response.close();
    }

    private void error(HttpServletResponse httpServletResponse, Throwable e) {
        try {
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.setStatus(500);
            PrintWriter out = httpServletResponse.getWriter();
            out.println("Error while generating PDF:");
            e.printStackTrace(out);
            out.close();

            LOGGER.error("Error while generating PDF", e);
        } catch (IOException ex) {
            throw new RuntimeException(e);
        }
    }

    private void error(HttpServletResponse httpServletResponse, String message, int code) {
        try {
            httpServletResponse.setContentType("text/plain");
            httpServletResponse.setStatus(code);
            PrintWriter out = httpServletResponse.getWriter();
            out.println("Error while generating PDF:");
            out.println(message);
            out.close();
            LOGGER.error("Error while generating PDF: " + message);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private File getTempDir() {
        if (tempDir == null) {
            tempDir = (File) getServletContext().
                    getAttribute("javax.servlet.context.tempdir");
            LOGGER.debug("Using '" + tempDir.getAbsolutePath() + "' as temporary directory");
        }
        return tempDir;
    }

    /**
     * If the file is defined, delete it.
     */
    private void deleteFile(File file) {
        if (file != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleting PDF file: " + file.getName());
            }
            if (!file.delete()) {
                LOGGER.warn("Cannot delete file:" + file.getAbsolutePath());
            }
        }
    }

    private String generateId(File tempFile) {
        final String name = tempFile.getName();
        return name.substring(
                TEMP_FILE_PREFIX.length(),
                name.length() - TEMP_FILE_SUFFIX.length());
    }

    private String getBaseUrl(HttpServletRequest httpServletRequest) {
        final String additionalPath = httpServletRequest.getPathInfo();
        String fullUrl = httpServletRequest.getParameter("url");
        if (fullUrl != null) {
            return fullUrl.replaceFirst(additionalPath + "$", "");
        } else {
            return httpServletRequest.getRequestURL().toString().replaceFirst(additionalPath + "$", "");
        }
    }

    /**
     * Will purge all the known temporary files older than TEMP_FILE_PURGE_SECONDS.
     */
    private void purgeOldTemporaryFiles() {
        final long minTime = System.currentTimeMillis() - TEMP_FILE_PURGE_SECONDS * 1000L;
        synchronized (tempFiles) {
            Iterator<Map.Entry<String, TempFile>> it = tempFiles.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, TempFile> entry = it.next();
                if (entry.getValue().creationTime < minTime) {
                    deleteFile(entry.getValue());
                    it.remove();
                }
            }
        }
    }

    private static class TempFile extends File {

        private final long creationTime;

        public TempFile(File tempFile) {
            super(tempFile.getAbsolutePath());
            creationTime = System.currentTimeMillis();
        }
    }
}
