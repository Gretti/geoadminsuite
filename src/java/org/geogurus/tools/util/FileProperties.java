package org.geogurus.tools.util;



import java.io.*;

import java.util.*;



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