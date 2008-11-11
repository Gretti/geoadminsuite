/*
 * LayerForm.java
 *
 * Created on 5 fevrier 2007, 22:51
 */
package org.geogurus.gas.forms;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.geogurus.mapserver.objects.Layer;

/**
 *
 * @author Administrateur
 * @version
 */
public class LayerForm extends org.apache.struts.action.ActionForm {

    private Layer m_layer;
    private String m_classificationType;
    private Integer m_numClasses;
    private String m_act;
    private String m_advancedLabel;
    private Boolean m_labels;
    // uses this form to also stores current selected class in UI, as
     // class properties UI is contained into layer classification
     //
    /** selected class identifier */
    private String selectedClassId;
    /** the selected symbology identifier */
    private String selectedSymbologyId;
    

    /**
     * @return
     */
    public Layer getDefaultMsLayer() {
        return m_layer;
    }

    /**
     * @param layer_
     */
    public void setDefaultMsLayer(Layer layer_) {
        m_layer = layer_;
    }

    /**
     * @return
     */
    public String getClassificationType() {
        return m_classificationType;
    }

    /**
     * @param classificationType_
     */
    public void setClassificationType(String classificationType_) {
        m_classificationType = classificationType_;
    }

    /**
     * @return
     */
    public Integer getNumClasses() {
        return m_numClasses;
    }

    /**
     * @param numClasses_
     */
    public void setNumClasses(Integer numClasses_) {
        m_numClasses = numClasses_;
    }

    /**
     * @return
     */
    public String getAct() {
        return m_act;
    }

    /**
     * @param act_
     */
    public void setAct(String act_) {
        m_act = act_;
    }

    /**
     * @return
     */
    public String getAdvancedLabel() {
        return m_advancedLabel;
    }

    /**
     * @param advancedLabel_
     */
    public void setAdvancedLabel(String advancedLabel_) {
        m_advancedLabel = advancedLabel_;
    }

    /**
     * @return
     */
    public Boolean getLabels() {
        return m_labels;
    }

    /**
     * @param labels_
     */
    public void setLabels(Boolean labels_) {
        m_labels = labels_;
    }

    /**
     *
     */
    public LayerForm() {
        super();
        m_layer = new Layer();
        m_numClasses = new Integer(10);
    //m_classificationType = "uniquevalue";
    //m_act = "generate";
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (getDefaultMsLayer() == null) {
            errors.add("name", new ActionMessage("error.layer.required"));
        // TODO: add 'error.name.required' key to your resources
        }
        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, ServletRequest request) {
        m_layer = null;
        m_classificationType = "unique";
        m_numClasses = new Integer(1);
        m_act = "save";
        m_advancedLabel = "";
        m_labels = new Boolean(false);
    }

    public String getSelectedClassId() {
        return selectedClassId;
    }

    public void setSelectedClassId(String selectedClassId) {
        this.selectedClassId = selectedClassId;
    }

    public String getSelectedSymbologyId() {
        return selectedSymbologyId;
    }

    public void setSelectedSymbologyId(String selectedSymbologyId) {
        this.selectedSymbologyId = selectedSymbologyId;
    }
    
}
