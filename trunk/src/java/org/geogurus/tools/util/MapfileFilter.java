/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geogurus.tools.util;

import java.io.File;
import java.io.FilenameFilter;

import org.geogurus.gas.managers.MapFileFactoryStrategy;

/**
 *
 * @author nicolas
 */
public class MapfileFilter implements FilenameFilter {

	MapFileFactoryStrategy s = new MapFileFactoryStrategy();
	
	/**
     * FilenameFilter interface implementation: 
     * Returns true if given name is a .map file.
     * @param dir
     * @param name
     * @return if and only if the name should be included in the file list; false otherwise.
     */
    public boolean accept(File dir, String name) {
        return s.canCreateFrom(new File(dir, name));
    }

}
