/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freevoice.jumpdbqueryextension.util;

/**
 * A String utility class
 * @author nicolas Ribot
 */
public class StringUtils {

    /**
     * Returns the number of occurences of c into given String s
     * @param haystack the string to search occurences from
     * @param needle the char to count 
     * @return the number of occurences of c in s
     */
    public static int countOccurrences(String haystack, char needle) {
        int count = 0;
        if (haystack != null) {
            for (int i = 0; i < haystack.length(); i++) {
                if (haystack.charAt(i) == needle) {
                    count++;
                }
            }
        }
        return count;
    }
}
