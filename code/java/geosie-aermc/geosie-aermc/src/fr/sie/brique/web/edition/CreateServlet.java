package fr.sie.brique.web.edition;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.sie.brique.dao.DAOService;
import fr.sie.brique.model.Line;
import fr.sie.brique.model.Point;
import fr.sie.brique.model.Polygon;
import fr.sie.brique.web.action.GeobsResponse;

/**
 * Servlet pour mettre creer une geometrie.
 * Utilisation en POST, recupere un objet JSON.
 * 
 * @author quique
 *
 */
public class CreateServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1856002754096407342L;

	private final static Log log = LogFactory
	.getLog(CreateServlet.class);
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

			// une fois mise a jour je supprime le cache
		    GeobsResponse geoSieResp = null;
		    
			String spec = Util.getSpecFromPostBody(request);
			//String convertedStr = new String(spec.getBytes(), "UTF-8");

			try {
				JSONObject jsonSpec = new JSONObject(spec);
				
				String type = jsonSpec.getString("type");
				String classification = jsonSpec.getString("classification");
				String att2 =jsonSpec.getString("attribut2");
				String att3 =jsonSpec.getString("attribut3");
				String the_geom=jsonSpec.getString("geom");
				log.info(type+" "+classification+" "+att2+" "+att3+" "+the_geom);
				
				if(type.equals("point")){
					Point pt = new Point();
					pt.setClassification(classification);
					pt.setAttribut_2(att2);
					pt.setAttribut_3(att3);
					pt.setThe_geom(the_geom);
					
					geoSieResp = DAOService.createPoint(pt);
				} else if (type.equals("line")) {
					Line line = new Line();
					line.setClassification(classification);
					line.setAttribut_2(att2);
					line.setAttribut_3(att3);
					line.setThe_geom(the_geom);
					geoSieResp = DAOService.createLine(line);

				} else if (type.equals("polygon")) {
					Polygon poly = new Polygon();
					poly.setClassification(classification);
					poly.setAttribut_2(att2);
					poly.setAttribut_3(att3);
					poly.setThe_geom(the_geom);
					
					geoSieResp = DAOService.createPolygon(poly);
				} else {
					log.error("le type de geometrie est manquant");
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//response.setStatus(response.SC_OK);
			/*response.setContentType("application-xml");
			response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><status>ok</status>");
			response.getWriter().flush();
			response.getWriter().close();*/
			
	        response.setContentType("text/html;charset=UTF-8");
	        try {
	            response.getOutputStream().write(geoSieResp.toString().getBytes("UTF-8"));
	        } catch (UnsupportedEncodingException e) {
	            log.error("Erreur lors du retour depuis l'action de mise à jour.", e);
	        } catch (IOException e) {
	            log.error("Erreur lors du retour depuis l'action de mise à jour.", e);
	        }

	        log.debug("End point");
	}

}
