package fr.sie.brique.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Classe utilitaire côté DAO.  
 * 
 * @author mauclerc
 */
public class DAOUtils {

    public static final Logger LOGGER = Logger.getLogger("fr.brgm.util");

    /*public static Integer generateUniqueId(String idName, String tableName, Connection conn) throws SQLException {

        Integer max = 1;
        //Test si il existe des id dans la table obstacle_referentiel,
        //si oui on prend la valeur max+1
        //sinon on prend max = 1
        String maxQuery = "SELECT max(" + idName + ") FROM " + tableName + ";";
        Statement stat = conn.createStatement();
        ResultSet res = stat.executeQuery(maxQuery);
        if (res != null) {
            if (res.next()) {
                if (res.getObject("max") != null) {
                    max = ((Integer) res.getObject("max")) + 1;
                }
            }
        }
        return max;
    }*/
    public static Integer getId(String idName, String tableName, Connection conn) throws SQLException {

        Integer max = 1;
        //Test si il existe des id dans la table obstacle_referentiel,
        //si oui on prend la valeur max+1
        //sinon on prend max = 1
        String maxQuery = "SELECT max(" + idName + ") FROM " + tableName + ";";
        Statement stat = conn.createStatement();
        ResultSet res = stat.executeQuery(maxQuery);
        if (res != null) {
            if (res.next()) {
                if (res.getObject("max") != null) {
                    max = ((Integer) res.getObject("max"));
                }
            }
        }
        return max;
    }
    
    public static Integer executeSQLUpdate(String queryString, Connection conn) throws SQLException {
        Statement stat = conn.createStatement();
        
        Integer rowCount = stat.executeUpdate(queryString); //,Statement.RETURN_GENERATED_KEYS
        //LOGGER.info("row: "+rowCount);
        //Les clefs auto-générées sont retournées sous forme de ResultSet
        /*ResultSet clefs = stat.getGeneratedKeys();
        if(clefs.next()){
        	LOGGER.info("La première clef auto-générée vaut ");
        	LOGGER.info(""+clefs.getObject(1));  
        }*/
        return rowCount;
    }

    public static String getSQLSetUpdateRequestString(Object obj,
            Class<? extends Object> classObj) {
        // TODO Auto-generated method stub
        classObj.cast(obj);
        String str = "";
        Field[] fields = classObj.getFields();
        // 1 : Parcours la liste des attributs 
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                String fieldClassName = field.getType().getName();
                if (field.getName().contains("the_geom")) {
                    str += field.getName() + " = st_transform(st_GeometryFromText( '" 
                            + ((String) field.get(obj)).split(";")[1] + "', " 
                            + ((String) field.get(obj)).split(";")[0].split("=")[1] + "), 2154), ";
                } else if (fieldClassName.contains("Integer") 
                        || fieldClassName.contains("Boolean")
                        || fieldClassName.contains("Double")) {
                    if (field.get(obj) != null && !field.get(obj).toString().equals("-1")) {
                        str += field.getName() + " = " + field.get(obj) + ", ";
                    } else {
                        str += field.getName() + " = null, ";
                    }
                } else if (fieldClassName.contains("String")) {
                    if (field.get(obj) != null && !field.get(obj).equals("")) {
                        str += field.getName() + " = '" + StringEscapeUtils.escapeSql((String) field.get(obj)) + "', ";
                    } else {
                        str += field.getName() + " = null, ";
                    }
                } else if (fieldClassName.contains("Date")) {
                    Date date = (Date) field.get(obj);
                    if (date != null) {
                        str += field.getName() + " = '" + (new Timestamp(date.getTime())).toString() + "', ";
                    } else {
                        str += field.getName() + " = null, ";
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        if (str.lastIndexOf(',') != -1) {
            str = str.substring(0, str.lastIndexOf(','));
        }
        return str;
    }

    public static String getSQLInsertIntoByReflection(String tableName, Object obj,
            Class<? extends Object> classObj) {
        // TODO Auto-generated method stub
        classObj.cast(obj);
        String strInsert = "INSERT INTO " + tableName + " (";
        String strValue = " VALUES (";
        Field[] fields = classObj.getFields();
        // 1 : Parcours la liste des attributs 
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            try {
                String fieldClassName = field.getType().getName();
                if (field.get(obj) != null) {
                    //						if (i == 0) {
                    //							//Si c'est l'id de la table on le génère
                    //							strInsert += field.getName()+", ";
                    //							strValue += GeobsUtils.generateUniqueId(field.getName(), tableName)+", ";
                    //							LOGGER.log(Level.FINE,"strvalue = "+strValue);
                    //						} else 
                    /*if (field.getName().contains("the_geom")) {
                        //Si c'est le champ est une Geometry
                        strInsert += field.getName() + ", ";
                        strValue += "GeometryFromText( '" + ((String) field.get(obj)).split(";")[1] + "', " + ((String) field.get(obj)).split(";")[0].split("=")[1] + "), ";

                    } else */
                	if(field.getName().contains("id_")){
                		// use nextval postgresql
                	} else  if (fieldClassName.contains("Integer") || fieldClassName.contains("Boolean") || fieldClassName.contains("Double")) {
                        if (field.get(obj) != null && !field.get(obj).toString().equals("-1")) {//the -1 value means : the sql request does not contain this field to insert or update.
                            strInsert += field.getName() + ", ";
                            strValue += field.get(obj) + ", ";
                        }

                    } else if (fieldClassName.contains("String")) {
                        if (field.get(obj) != null && !field.get(obj).equals("")) {
                            strInsert += field.getName() + ", ";
                            strValue += "'" + StringEscapeUtils.escapeSql((String) field.get(obj)) + "', ";
                        }

                    } else if (fieldClassName.contains("Date")) {
                        Date date = (Date) field.get(obj);
                        if (date != null) {
                            strInsert += field.getName() + ", ";
                            strValue += "'" + (new Timestamp(date.getTime())).toString() + "', ";
                        }

                    }
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        if (strValue.lastIndexOf(',') != -1) {
            strValue = strValue.substring(0, strValue.lastIndexOf(','));
        }
        if (strInsert.lastIndexOf(',') != -1) {
            strInsert = strInsert.substring(0, strInsert.lastIndexOf(','));
        }
        strInsert += ")";
        strValue += ")";
        LOGGER.log(Level.FINE, strInsert + strValue);
        return (strInsert + strValue);
    }

    @SuppressWarnings("unchecked")
    public static Object getObjectByReflection(Object obj, final ResultSet res, final Class classObj) throws SQLException {
        try {
            classObj.cast(obj);

            /* 1 : Parcours des methodes de la classe obstacle */
            for (Method meth : classObj.getMethods()) {

                String methName = meth.getName();
                if (methName.contains("set")) {

                    /* 2 : Supprime le prefixe 'set' du nom du setter */
                    String colName = methName.substring(3, methName.length()).toLowerCase();

                    /* 3 : Test si une colonne à un setter attribué
                     * si une SQL exception est attrapés alors on passe a la colonne suivante
                     */
                    try {
                        //LOGGER.log(Level.INFO,res.findColumn(colName) + " " + colName + " " + res.getObject(colName));
                        if (res.getObject(colName) != null) {
                            /* 4 : Execute le setter*/
                            try {
                                if (res.getObject(colName).getClass().getName().contains("geometry")) {
                                    meth.invoke(obj, res.getObject(colName).toString());
                                } else {
                                    meth.invoke(obj, res.getObject(colName));
                                }
                            } catch (IllegalArgumentException e) {
                                LOGGER.log(Level.WARNING, "La fonction " + meth.getName() + " de la classe " + classObj.getName() + " ne prend pas un argument de type " + res.getObject(colName).getClass().getName() + "");
                            }
                        }
                    } catch (SQLException e) {
                        LOGGER.log(Level.FINE, "La colonne " + colName + " n'existe pas !!!!");
                    }
                }
            }
            return obj;
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Une securité empêche l'accès a la methode !!! ");
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Pas les droits pour executer cette opération !!! ", e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Ne peut être invoqué !!! ", e);
        }
        return null;
    }

    /**
     * This method returns a folder located in data folder from the current war file.
     * @param folder
     * @return
     */
    public static File getSpecificFolderFromWar(final String folder, ServletContext sc) {
        File result;
        // getting the current directory located in the current deployed war file
        final File data = new File(sc.getRealPath(""));
        result = new File(data, folder);

        return result;
    }

    /**
     * This method loads the Properties file located in the admin folder with the file name.
     * @param name
     * @return Properties object.
     */
    public static Properties loadPropertiesFile(String name, File folder) {
        Properties result = new Properties();
        File f = new File(folder, name);
        if (!f.exists()) {
            throw new IllegalArgumentException("There is no properties file in the specified folder name : " + name + " under the folder " + folder);
        } else {
            try {
                FileInputStream in = new FileInputStream(f);
                result.load(in);
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(DAOUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
}
