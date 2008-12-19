/*
 * Copyright (C) 2003-2008  Gretti N'Guessan, Nicolas Ribot
 *
 * This file is part of GeoAdminSuite
 *
 * GeoAdminSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoAdminSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GeoAdminSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geogurus.tools.util;



import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;



public class FileProperties extends Properties {

    private String m_fileName;

    

    public Object get(String keyName) {

        return super.get(keyName);

    }

    

    private void loadFile(Properties fileProps) throws Exception {

        for(Iterator keysIter = fileProps.keySet().iterator(); keysIter.hasNext(); ) {

            String key = (String)keysIter.next();

            String value = (String)fileProps.getProperty(key);

            

            for (int i=0; i<5; i++) {

                if(key.indexOf("INCLUDE"+i) != -1) {

                    FileProperties includeProps = new FileProperties((String)value);

                    for(Iterator keysIter2 = includeProps.keySet().iterator(); keysIter2.hasNext(); ) {

                        String key2 = (String)keysIter2.next();

                        String value2 = (String)includeProps.get(key2);

                        super.put(key2, value2);

                    }

                }

            }

            

            super.put(key, value);

        }

    }

    

    public void dump() {

        for(Iterator keysIter = keySet().iterator(); keysIter.hasNext(); ) {

            String key = (String)keysIter.next();

        }

    }

    

    public FileProperties(String fileName) {

        m_fileName = fileName;

        InputStream is = null;

        

        try {

            is = getClass().getResourceAsStream(m_fileName);

        }

        catch(Exception e) {

            e.printStackTrace();

            return;

        }

        

        Properties fileProps = new Properties();

        

        try {

            fileProps.load(is);

        }

        catch (Exception e) {

            e.printStackTrace();

            return;

        }

        

        try {

            loadFile(fileProps);

        }

        catch (Exception e) {

            e.printStackTrace();

            return;

        }

    }

    

    public static Vector getValues(Properties properties, String cle) {

        Vector values = new Vector();

        int i = 0;

        String tmp;

        

        while (true) {

            tmp = properties.getProperty(cle + String.valueOf(i));

            if (tmp == null)

                break;

            values.addElement(tmp);

            i++;

        }

        

        return values;

    }

}