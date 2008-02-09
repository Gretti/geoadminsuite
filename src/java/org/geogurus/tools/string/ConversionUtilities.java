package org.geogurus.tools.string;

/** Title:        ConversionUtilities
 * Description:  Get the extent (BOX3D) of a parcel from postgresql database.
 *               Crop an input tif image along this extent.
 * Copyright:    Copyright (c) 2001
 * Company:      SCOT
 * @author Jerome Gasperi, aka jrom, Bastien Vialade
 * @version 1.0
 */

public class ConversionUtilities {

    /** Empty constructor
     */    
  public ConversionUtilities() { }


  /**
   * Convert numerical string into double
   *
   * @param s Input string
   * @return Converted double
   */
  public static double stod(String s) {

    double n = Double.MIN_VALUE;
    if (s == null) {
       return n;
    }
    try {
	n = new Double(s.trim()).doubleValue();
    } catch(NumberFormatException e) {}

    return(n);
  }


  /**
   * Convert numerical string into integer
   *
   * @param s Input string
   * @return Converted integer
   */
  public static int stoi(String s) {
    int n = Integer.MIN_VALUE;
    
    if (s == null) {
       return n;
    }

    try {
	n = new Integer(s.trim()).intValue();
    } catch(NumberFormatException e) {}

    return(n);

  }

  /**
   * Convert numerical string into float
   *
   * @param s Input string
   * @return Converted float
   */
  public static float stof(String s) {
    float n = Float.MIN_VALUE;

    if (s == null) {
       return n;
    }

    try {
	n = new Float(s.trim()).floatValue();
    } catch(NumberFormatException e) {}

    return(n);

  }

 /**
  * Returns a new String where all occurences of ' (single quote) are replaced by '' (double quotes)
  *@param s the string to replace
  *@return a new String with chars replaced.
  */
    public static String escapeSingleQuotes(String s){
        StringBuffer sb = new StringBuffer();
        java.util.StringTokenizer st = new java.util.StringTokenizer(s, "'", true);
        String token = null;

        while (st.hasMoreTokens())   {
            token = st.nextToken();
            sb.append(token.equals("'") ? "''" : token);
       }
       return sb.toString();
    }
    
    /**
     * Tokenizes a string given a delimiter.
     * @param   string the string to tokenize
     * @param   limit  the delimiter (be aware that the characters are considered separately)
     * @return  the array of tokens
     **/
    public static String[] tokenize(String string, String limit) {
        java.util.StringTokenizer st = new java.util.StringTokenizer(string, limit);
        java.util.ArrayList v = new java.util.ArrayList();
        while(st.hasMoreElements()) {
            v.add(st.nextToken());
        }
        return (String[]) v.toArray(new String[0]) ;
    }
    
    /**
     * Tokenizes a string (the default delimiter is the space string " ").
     * @param  string the string to tokenize
     * @return        the array of tokens
     * @see #tokenize(String string, String limit)
     **/
    public static String[] tokenize(String string) {
        return tokenize(string, " ");
    }
    
 /**
  * Returns a new String where all occurences of the given char are removed
  *@param s the string to replace
  *@param c the char to remove
  *@return a new String with chars replaced.
  */
    public static String removeCharOccurence(String s, char c){
        StringBuffer buff = new StringBuffer();
        String[] array = tokenize(s,String.valueOf(c));
        for (int i=0; i< array.length; i++) {
            buff.append(array[i]);
        }
        return buff.toString();
    }
    
    /** Returns the extracted string without double quotes
     * @param s String to extract
     * @return String without double quote
     */    
    public static String getStringBetweenDoubleQuotes(String s) {
        return getStringBetweenChar(s,'\"');        
    }
    
    
    /** Returns the extracted string without simple quotes
     * @param s String to extract
     * @return String without simple quote
     */    
    public static String getStringBetweenSimpleQuotes(String s) {
        return getStringBetweenChar(s,'\'');        
    }

    /** Gets string between two same chars.
     * @param s String to extract from
     * @return Extracted String.
     *         null if extraction impossible.
     */    
    public static String getStringBetweenChar(String s, char c) {
        String ch = String.valueOf(c);
        if (s==null) return null;
        int firstIndex = s.indexOf(ch);
        int lastIndex = s.lastIndexOf(ch);
        // Case where:
        //      - no char
        //      - only one char
        if (firstIndex==lastIndex) return null;
        // Otherwise return what there is between chars
        return s.substring(firstIndex+1,lastIndex);        
    }

    /**
     *
     * Return the value of a mapfile line.
     * Assume the first token is the keyword.
     * The rest of the tokens is the value.
     *
     */
    public static String getValueFromMapfileLine(String line) {
        String[] tokens = tokenize(line.trim());
        
        if (tokens.length < 2) {
            return null;
        }
        
        StringBuffer sb = new StringBuffer("");
        
        if (tokens[1].indexOf('\"') != -1) {
            // Bug correction: if line is composed of several words, like a connection string for a layer:
            // connection "a b c d", must rebuild the full String before extracting value between quotes
            sb.append(tokens[1]);
            
            if (tokens.length > 2) {
                for (int i = 2; i < tokens.length; i++) {
                    sb.append(" ").append(tokens[i]);
                }
            } 
            return getStringBetweenDoubleQuotes(sb.toString());
        }
        
        for (int i = 1; i < tokens.length; i++) {
            sb.append(" ").append(tokens[i]);
        }
         
        return sb.toString().trim();
        
    }
    
    /**
     *
     * Remove the " charachter from a string
     *
     */
   public static String removeDoubleQuotes(String s) {
        return removeCharOccurence(s, '\"');
   }
   
   /**
    *
    * Add double quotes to the input String
    *
    */
   public static String quotes(String s) {
        return '"' + s + '"';
   }
   
}