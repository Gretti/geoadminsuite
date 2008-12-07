package org.geogurus.cartoweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Base object representing a Cartoweb configuration file (cartoweb ini files)
 * A ini file is represented by its attributes. It can be loaded from an ini file
 * or write to an ini file.
 * An internal Properties object represents the writable properties.
 * See derived objects representing actual CW configuration
 * @author nicolas
 */
public abstract class CartowebConf implements Serializable {
    /** the header writen in the ini file */
    public static final String GAS_HEADER = "# File generated by the GAS (geoadmin.camptocamp.net)";
    /** the Java object representing the ini file (key=value pairs) */
    protected Properties iniProps;
    /** the class' logger */
    protected Logger logger;
    /** The file from wich this object was loaded */
    protected File iniFile;
    
    /**
     * load this object from the given cartoweb3 .ini configuration file
     * @param iniFile
     * @return true if the object is correctly loaded, false otherwise. 
     *         In this case, see the generated log (warning level)
     */
    public boolean loadFromFile(File iniFile) {
        if (iniFile == null) {
            logger.warning("null file to load from...");
            return false;
        }
        this.iniFile = iniFile;
        if (!this.iniFile.isFile()) {
            logger.warning("given ini file path is not a valid file: " + this.iniFile);
            return false;
        }
        boolean ok = false;
        try {
            ok = loadFromFile(new FileInputStream(iniFile));
        } catch (IOException ioe) {
            logger.warning("Exception during properties loading: " + ioe.getMessage());
            return false;
        }
        return ok;
    }
    
    /**
     * load this object from the given cartoweb3 .ini configuration file
     * @param iniFile the inputStream of the file to load. caller must close the stream
     * @return true if the object is correctly loaded, false otherwise. 
     *         In this case, see the generated log (warning level)
     */
    public boolean loadFromFile(InputStream iniStream) {
        if (iniStream == null) {
            logger.warning("null file stream to load from...");
            return false;
        }
        iniProps = new Properties();
        try {
            iniProps.load(iniStream);
        } catch (IOException ioe) {
            logger.warning("Exception during properties loading: " + ioe.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * load this object from the given cartoweb3 .ini configuration file path
     * @param iniFile
     * @return true if the object is correctly loaded, false otherwise. 
     *         In this case, see the generated log (warning level)
     */
    public boolean loadFromFile(String iniFilePath) {
        if (iniFilePath == null) {
            logger.warning("null file path to load from...");
            return false;
        }
        return loadFromFile(iniFile);
    }
    
    /**
     * Saves this object into the given file, as a .ini structure (key=value lines)
     * @param fileToSave
     * @return true if file was correctly saved, false otherwise.
     *         In this case, see the generated log (warning level)
     */
    public boolean saveAsFile(File fileToSave) {
        if (fileToSave == null) {
            logger.warning("null file to save to...");
            return false;
        }
        /*
        synchronizeProperties();
        try {
            iniProps.store(new FileOutputStream(fileToSave), CartowebConf.GAS_HEADER);
        } catch (IOException ioe) {
            logger.warning("Exception during save: " + ioe.getMessage());
            return false;
        }
         */
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileToSave));
            out.write(GAS_HEADER);
            out.newLine();
            Date d = new Date();
            out.write("# " + d.toString());
            out.newLine();
            out.write(this.toString());
            out.flush();
            out.close();
        } catch (IOException ioe) {
            logger.warning("Exception during save: " + ioe.getMessage());
            return false;
        }
        return true;
    }
    
    /**
     * Gets the id contained in the given key.
     * Key must be of the form:
     * <name1>.id.<name2>.<name3>.<name4>
     * Then, id, the second token will be extracted and returned.
     * Key is tokenized with '.' char separator and second token is returned
     * @param key the key to extract id from
     * @return the extracted id string or null if key format is invalid
     */
    public static String getIdFromKey(String key) {
        if (key == null) {
            return null;
        }
        String[] tokens = ConversionUtilities.explodeKey(key);
        if (tokens.length < 2) {
            return null;
        }
        return tokens[1];
    }
}