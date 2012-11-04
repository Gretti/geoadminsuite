package fr.sie.brique.web.print;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.servlet.MapPrinterServlet;
import org.pvalsecc.misc.FileUtilities;

import com.lowagie.text.DocumentException;

/**
 * Servlet implementation class MapPrinterServletYaml
 */
public class MapPrinterServletYaml extends MapPrinterServlet {
	private static final long serialVersionUID = 1L;
	/**
	 * Log
	 */
	private final static Log log = LogFactory
			.getLog(MapPrinterServletYaml.class);

	private static HashMap<Thread, String> yamlConfig = new HashMap<Thread, String>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public MapPrinterServletYaml() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String yConfig = "-";
		try {
			String yamlText = request.getParameter("yaml");
			String yamlURL = request.getParameter("yamlURL");
			if (yamlText != null && !yamlText.equalsIgnoreCase("")) {
				// yaml from client
				yConfig = yamlText;
			} else if (yamlURL != null && !yamlURL.equalsIgnoreCase("")) {
				// yaml from URL
				if (!yamlURL.startsWith("http://")) {
					// yaml from local
					yConfig = FileReader.read(getServletContext().getRealPath(
							yamlURL));
				} else {
					// yaml from URL
					URL urlConfig = new URL(yamlURL);
					URLConnection connection = urlConfig.openConnection();
					yConfig = FileReader.read(connection.getInputStream());
				}
			}
			yamlConfig.put(Thread.currentThread(), yConfig);
		} catch (Exception ex) {
			error(response, new Exception("Invalid yaml configuration"));
			return;
		}
		super.doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	protected synchronized MapPrinter getMapPrinter() throws ServletException {
		String yConfig = yamlConfig.get(Thread.currentThread());
		yamlConfig.remove(Thread.currentThread());
		log("getMP " + yConfig);
		return yConfig.equalsIgnoreCase("-") ? super.getMapPrinter()
				: new MapPrinter(yConfig, ".");
	}

	@Override
	protected TempFile doCreatePDFFile(String spec,
			HttpServletRequest httpServletRequest) throws IOException,
			DocumentException, ServletException {

		// TODO : Ici ajout code pour afficher les attributs des resultats dans des
		// fiches sur le PDF avec le bloque de type "records" declare dans le fichier YAML.
		// Realiser le traitement pour recuperer des infos en base pour les
		// ajoutees au spec
		
		return super.doCreatePDFFile(spec, httpServletRequest);
	}

	@Override
	protected void sendPdfFile(HttpServletResponse httpServletResponse,
			TempFile tempFile, boolean inline) throws IOException,
			ServletException {
		FileInputStream pdf = new FileInputStream(tempFile);
		final OutputStream response = httpServletResponse.getOutputStream();
		try {
			httpServletResponse.setContentType("application/pdf");
			if (inline != true) {
				// TODO : erreur sur getMapPrinter() yConfig = null
				// final String
				// fileName=tempFile.getOutputFileName(getMapPrinter());
				String fileName = "";
				if (tempFile.outputFileName != null) {
					fileName = tempFile.outputFileName;
				} else {
					fileName = tempFile.getName();
				}

				httpServletResponse.setHeader("Content-disposition",
						"attachment; filename=" + fileName);
			}
			FileUtilities.copyStream(pdf, response);
		} finally {
			try {
				pdf.close();
			} finally {
				response.close();
			}
		}
	}
}
