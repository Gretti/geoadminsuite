package fr.sie.brique.model;

/**
 * Transfert Object (TO) lié aux points.
 * 
 * @author quique
 *
 */
public class Point implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8805587810897770705L;
	
	
	public Integer gid;
	public String classification;
	public String attribut_2;
	public String attribut_3;
	public String the_geom;
	
	

	public Integer getGid() {
		return gid;
	}
	public void setId_brique(Integer id_brique) {
		this.gid = id_brique;
	}
	
	public String getClassification() {
		return classification;
	}
	public void setClassification(String classification) {
		this.classification = classification;
	}
	public String getAttribut_2() {
		return attribut_2;
	}
	public void setAttribut_2(String attribut_2) {
		this.attribut_2 = attribut_2;
	}
	public String getAttribut_3() {
		return attribut_3;
	}
	public void setAttribut_3(String attribut_3) {
		this.attribut_3 = attribut_3;
	}
	public String getThe_geom() {
		return the_geom;
	}
	public void setThe_geom(String the_geom) {
		this.the_geom = the_geom;
	}
}
