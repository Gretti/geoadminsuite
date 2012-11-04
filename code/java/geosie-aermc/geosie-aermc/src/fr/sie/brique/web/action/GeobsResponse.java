package fr.sie.brique.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.sie.brique.dao.DAOUtils;


/**
 * Réponse envoyée à la brique cartographique (client) après chaque action. 
 * Cet objet contient les messages à afficher, les actions à exécuter et les identifiants des obstacles à ajouter ou à supprimer de la sélection courante. 
 * 
 * @author mauclerc
 */
public class GeobsResponse {
	private int resultCode = 0;
	private List<String> idsToAdd = new ArrayList<String>();
	private List<String> idsToDel = new ArrayList<String>();
	private boolean refreshAll = false; 
	private boolean closeDialog = false;
	private String otherCommand = null;
	

	public int getResultCode() {
		return resultCode;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public List<String> getIdsToAdd() {
		return idsToAdd;
	}
	public void addIdRefToAdd(Integer refId) {
		int newId = refId.intValue();
		idsToAdd.add(String.valueOf(newId));
	}
	public void addIdParToAdd(Integer parId) {
		int newId = parId.intValue();
		idsToAdd.add(String.valueOf(newId));
	}
	public void cleanIdsToAdd() {
		idsToAdd.clear();
	}

	public List<String> getIdsToDel() {
		return idsToDel;
	}
	public void setIdsToDel(List<String> ids) {
		this.idsToDel = ids;
	}
	public void addIdRefToDel(Integer refId) {
		int newId = refId.intValue();
		idsToDel.add(String.valueOf(newId));
	}
	public void addIdParToDel(Integer parId) {
		int newId = parId.intValue();
		idsToDel.add(String.valueOf(newId));
	}
	public void cleanIdsToDel() {
		idsToDel.clear();
	}

	public boolean isRefreshAll() {
		return refreshAll;
	}
	public void setRefreshAll(boolean refreshAll) {
		this.refreshAll = refreshAll;
	}
	public boolean isCloseDialog() {
		return closeDialog;
	}
	public void setCloseDialog(boolean closeDialog) {
		this.closeDialog = closeDialog;
	}
	public String getOtherCommand() {
		return otherCommand;
	}
	public void setOtherCommand(String otherCommand) {
		this.otherCommand = otherCommand;
	}
	@Override
	public String toString() {
    	String result = "<div id='response'>var data = {";

    	result += "idsToAdd : [";
    	for (int i=0; i<idsToAdd.size(); i++) {
    		String id = idsToAdd.get(i);
    		result += "\"" + id + "\"";
    		if (i<idsToAdd.size()-1) {
    			result += ", ";
    		}
    	}
    	result += "],";
    	
    	result += "idsToDel : [";
    	for (int i=0; i<idsToDel.size(); i++) {
    		String id = idsToDel.get(i);
    		result += "\"" + id + "\"";
    		if (i<idsToDel.size()-1) {
    			result += ", ";
    		}
    	}
    	result += "],";
    	
   		result += "resultCode : " + resultCode + ",";
    	result += "refreshAll : " + refreshAll + ",";
    	result += "closeDialog : " + closeDialog;

    	if (otherCommand != null && otherCommand.length() > 0) {
        	result += ",otherCommand : function() {" + otherCommand + "}";
    	}
    	
    	result += "}</div>";
    	Logger.getLogger(DAOUtils.class.getName()).log(Level.INFO, result);
    	return result;
	}
}
