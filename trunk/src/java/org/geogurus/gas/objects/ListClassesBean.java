/*
 * ListClassesBean.java
 *
 * Created on 22 february 2006, 11:45
 */

package org.geogurus.gas.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.geogurus.mapserver.objects.MapClass;

/**
 *
 * @author Gretti
 */
public class ListClassesBean implements Serializable {
    
    protected ArrayList m_listClasses;
    
    /** Creates a new instance of ListClassesBean */
    public ListClassesBean() {
        m_listClasses = new ArrayList();
    }
    
    /**
     * Gets an iterator on ListClasses's ArrayList.
     * @return java.util.Iterator
     */
    public Iterator getClasses() {
        return m_listClasses.iterator();
    }
    
    /**
     * Gets the first class of the list.
     * @param i_
     * @return MapClass
     */
    public MapClass getFirstClass() {
        return (MapClass)m_listClasses.get(0);
    }
    
    /**
     * Find a Class using index.
     * @param i_
     * @return MapClass
     */
    public MapClass getClass(int i_) {
        return (MapClass)m_listClasses.get(i_);
    }
    
    /**
     * Return number of Classes in list.
     * @return int
     */
    public int getNbClasses() {
        return m_listClasses.size();
    }
    
    /**
     * Add a MapClass in list.
     * @param class_
     */
    public void addClass(MapClass class_) {
        m_listClasses.add(class_);
    }
    
    /**
     * @param class_
     */
    public void setClasses(ArrayList classes_) {
        m_listClasses = classes_;
    }
    
    /**
     * @param class_
     */
    public void removeAllClasses(ArrayList classes_) {
        m_listClasses.removeAll(classes_);
    }
    /**
     * @param class_
     */
    public void addAllClasses(ArrayList classes_) {
        m_listClasses.addAll(classes_);
    }
    
    /**
     * Replace native toString method
     * @return String
     */
    public String toString() {
        String s = "ListClassesBean " + m_listClasses.size() + " element(s)\n";
        
        for(Iterator ite = m_listClasses.iterator(); ite.hasNext();) {
            MapClass c = (MapClass)ite.next();
            s += c.toString();
            s += "\n";
        }
        
        return s;
    }
    
}
