/*
 * ListClassesBean.java
 *
 * Created on 22 february 2006, 11:45
 */

package org.geogurus.gas.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.geogurus.mapserver.objects.Class;

/**
 *
 * @author Gretti
 */
public class ListClassesBean implements Serializable {
    
    protected ArrayList m_listClasses;
    
    /** the message that can be associated with this classification. Useful for UI when 
     * generating a new classification (=>  a new ListClassBean Object)
     */
    protected String message;
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
    public Class getFirstClass() {
        Class res = new Class();
        if(m_listClasses.size() > 0) {
            res = (Class)m_listClasses.get(0);
        }
        return res;
    }
    
    /**
     * returns the ith class in the internal arrayList.
     * todo: remove this method: user getClasses().get() instead
     * @param i_
     * @return MapClass
     */
    public Class getClass(int i_) {
        return (Class)m_listClasses.get(i_);
    }
    
    /**
     * Find a Class using class name.
     * @param className the name of the class to get
     * @return MapClass
     */
    public Class getClass(String className) {
        if (m_listClasses == null) {
            return null;
        }
        for (int i = 0; i < m_listClasses.size(); i++) {
            Class c = (Class)m_listClasses.get(i);
            if (c != null && c.getName().equals(className)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Find a Class using class ID (hashcode).
     * todo: rename to getClass after existing getClass() removal
     * @param className the name of the class to get
     * @return MapClass
     */
    public Class getClassById(int id) {
        if (m_listClasses == null) {
            return null;
        }
        for (int i = 0; i < m_listClasses.size(); i++) {
            Class c = (Class)m_listClasses.get(i);
            if (c != null && c.getID() == id) {
                return c;
            }
        }
        return null;
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
    public void addClass(Class class_) {
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
    
    public void clear() {
        m_listClasses.clear();
    }

    public void setMessage(String message) {
        this.message=message;
    }
    
    /**
     * Replace native toString method
     * @return String
     */
    @Override
    public String toString() {
        String s = "ListClassesBean " + m_listClasses.size() + " element(s)\n";
        
        for(Iterator ite = m_listClasses.iterator(); ite.hasNext();) {
            Class c = (Class)ite.next();
            s += c.toString();
            s += "\n";
        }
        
        return s;
    }

    public String getMessage() {
        return message;
    }

    
}
