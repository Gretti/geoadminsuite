/*
 * MapFileFilter.java
 *
 * Created on 21 mars 2002, 12:17
 */

package org.geogurus.mapserver.gui;

import java.io.File;

/**
 *  Accepts only MapFile files
 *
 * @author  Bastien VIALADE
 */

public class MapFileFilter extends javax.swing.filechooser.FileFilter {
    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
	if (extension != null) {
            return (extension.equals("map")||extension.equals("map2")) ;
     	}
        return false;
    }
    
    
    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    public String getDescription() {
        return "MapFile files: *.map";
    }
    
}

