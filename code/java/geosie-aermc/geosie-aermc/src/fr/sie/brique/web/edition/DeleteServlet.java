package fr.sie.brique.web.edition;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.sie.brique.dao.DAOService;
import fr.sie.brique.web.action.GeobsResponse;

/**
 * Servlet pour mettre supprimer une geometrie.
 * Utilisation en POST, recupere un objet JSON.
 * 
 * @author quique
 *
 */
public class DeleteServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5156248776582077967L;
	private final static Log log = LogFactory.getLog(DeleteServlet.class);

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		GeobsResponse geoSieRespPoint = null;
		GeobsResponse geoSieRespLine = null;
		GeobsResponse geoSieRespPolygone = null;
		GeobsResponse geoSieRespGeom = new GeobsResponse();
		List<String> deleteIds = new ArrayList<String>();
		Boolean dialogClose = true;
		String spec = Util.getSpecFromPostBody(request);
		//String convertedStr = new String(spec.getBytes(), "UTF-8");

		// TODO : a savoir pk sa merde
		try {
			JSONArray features= new JSONArray(spec);

			List<String> deletePoints = new ArrayList<String>();
			List<String> deleteLines = new ArrayList<String>();
			List<String> deletePolygones = new ArrayList<String>();

			for (int i = 0; i < features.length(); i++) {
				JSONObject feature = features.getJSONObject(i);

				String type = feature.getString("type");
				Integer id = feature.getInt("id");

				log.info(type + " " + id);

				if (type.equals("point")) {
					deletePoints.add(id.toString());
				} else if (type.equals("line")) {
					deleteLines.add(id.toString());
				} else if (type.equals("polygon")) {
					deletePolygones.add(id.toString());
				} else {
					log.error("le type de geometrie est manquant");
				}
			}

			if (deletePoints.size() > 0) {
				geoSieRespPoint = DAOService.deletePoints(deletePoints);
				deleteIds.addAll(geoSieRespPoint.getIdsToDel());
				if(!geoSieRespPoint.isCloseDialog()) {
					dialogClose = false;
				};
			}
			if (deleteLines.size() > 0) {
				geoSieRespLine = DAOService.deleteLines(deleteLines);
				deleteIds.addAll(geoSieRespLine.getIdsToDel());
				if(!geoSieRespLine.isCloseDialog()) {
					dialogClose = false;
				};
			}
			if (deletePolygones.size() > 0) {
				geoSieRespPolygone = DAOService.deletePolygons(deletePolygones);
				deleteIds.addAll(geoSieRespPolygone.getIdsToDel());
				if(!geoSieRespPolygone.isCloseDialog()) {
					dialogClose = false;
				};
			}
			geoSieRespGeom.setIdsToDel(deleteIds);
			geoSieRespGeom.setCloseDialog(dialogClose);
			//geoSieRespGeom.
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		// response.setStatus(response.SC_OK);
		/*
		 * response.setContentType("application-xml");
		 * response.getWriter().write
		 * ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>ok</status>");
		 * response.getWriter().flush(); response.getWriter().close();
		 */

		response.setContentType("text/html;charset=UTF-8");
		try {
			response.getOutputStream().write(
					geoSieRespGeom.toString().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.error("Erreur lors du retour depuis l'action de mise à jour.",
					e);
		} catch (IOException e) {
			log.error("Erreur lors du retour depuis l'action de mise à jour.",
					e);
		}

		log.debug("End point");
	}
}
