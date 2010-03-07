package org.kaboum;

/*
 *
 * Class KaboumLang from the Kaboum project.
 * This class manage the current map units.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *

 */

import java.util.Properties;
import java.io.InputStream;
import java.net.URL;
import org.kaboum.util.KaboumUtil;

/**
 *
 * This class manages the language code by loading the corresponding properties file from inside the jar.
 *
 * @author Jerome Gasperi aka jrom
 *
 */
public class KaboumLang {
    
    /** Lang array */
    private Properties langArray = new Properties();
    
    /** Lang ID */
    private String langID;
    
    
    /**
     *
     * Constructor
     *
     * @param String Lang ID
     *
     */
    public KaboumLang(String langID, URL codeBase) {
        
        this.langID = langID;
        
        if (this.langID == null) {
            this.langID = "FR";
        }
        
        InputStream is = null;
        
        String basename = "/org/kaboum/properties/lang_";
        String basename2 = "org/kaboum/properties/lang_";
        
        try {
            is = this.getClass().getResourceAsStream(basename + this.langID + ".properties");
        } catch (Exception e1) {}
        
        if (is == null) {
            try {
                is = (new URL(codeBase, basename2 + this.langID + ".properties")).openStream();
            } catch (Exception e2) {}
        }
        
        if (is == null) {
            

            KaboumUtil.debug(" WARNING ! : properties file not found for lang " + this.langID + ". Using lang FR");
            
            try {
                is = this.getClass().getResourceAsStream(basename + "FR.properties");
            } catch (Exception e) {}
        }
        
        if (is == null) {
            try {
                is = (new URL(codeBase,  basename2 + "FR.properties")).openStream();
            } catch (Exception e) {
                KaboumUtil.debug(" WARNING ! : properties file not found for lang FR. Abort.");
            }
        }
        
        if (is != null) {
            try {
                langArray.load(is);
            } catch (Exception e) {
                KaboumUtil.debug(" WARNING ! : cannot load lang array !");
            }
        }
        
    }
    
    
    /**
     *
     * This method allows to overload the default
     * values
     *
     * @param code Object code
     * @param value New value for this code
     *
     */
    public void set(String code, String value) {
        langArray.put(code, value);
    }
    
    
    /**
     *
     * Convert an input int to its corresponding
     * string value
     *
     * @param code String code equivalent value
     *
     */
    public String getString(String code) {
        String str = (String) this.langArray.get(code);
        if (str == null) {
            return "";
        }
        return str;
    }
    
}
