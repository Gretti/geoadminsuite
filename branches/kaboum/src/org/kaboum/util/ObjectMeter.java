/*
 * ObjectMeter.java
 *
 * Created on 7 aout 2005, 17:26
 */

package org.kaboum.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipOutputStream;

public class ObjectMeter{
    
    /**
     * Determines the size of an object in bytes when it is serialized.
     * This should not be used for anything other than optimization
     * testing since it can be memory and processor intensive.
     */
    public static long getObjectSize(Object object){
        if(object==null){
            System.err.println("Object is null. Cannot measure.");
            return -1;
        }
        
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            oos.close();
            baos.close();
            return bytes.length;
        } catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Determines the size of an object in bytes when it is serialized and zipped.
     * This should not be used for anything other than optimization
     * testing since it can be memory and processor intensive.
     * This version gives the size of the zipped file containing this object
     */
    public static long getZippedObjectSize(Object object){
        if(object==null){
            System.err.println("Object is null. Cannot measure.");
            return -1;
        }
        File f = null;
        long res = 0L;
        
        try {
            f = new File(System.getProperty("java.io.tmpdir") + "toto.zip");
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(f));
            ObjectOutputStream oos = new ObjectOutputStream(zipOut);
            
            oos.writeObject(object);
            oos.close();
            zipOut.close();
            res = f.length();
            
            //deletes file.
            f.delete();
            
            return res;
        } catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }
}