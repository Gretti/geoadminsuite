/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freevoice.jumpdbqueryextension.util;

/**
 * A helper object to extract the text located at caret position, contained between query separators (";" or end of line)
 *  - if current caret position is end of line, runs the query on the line
 * (including previous lines if query expand on several lines)
 *  - else runs the query found between ";" separators or end of text.
 * @author nicolas ribot
 */
public class QueryExtractor {

    private String query;
    private int caretPosition;
    private String text;

    public QueryExtractor(String text, int caretPosition) {
        this.text = text;
        this.caretPosition = caretPosition;

        this.query = "";
    }

    /**
     * Returns the text located at caret position, contained between query separators (";" or end of line)
     * @return the extracted text
     * @throws IllegalArgumentException if object was built with invalid parameters: null text, negative caret position
     */
    public String getQuery() throws IllegalArgumentException {
        if (text == null || caretPosition < 0) {
            throw new IllegalArgumentException("Null text to parse, or negative caret position given to this class");
        }
        if (text.length() == 0) {
            return query;
        }

        if (caretAtEndOfLine()) {
            // a ";" may be at the left of the caret. if so, force rewind caret position
            rewindCaret();
            query = findQueryBeforeCaret();
        } else {
            query = findQueryBeforeCaret() + findQueryAfterCaret();
        }

        return query;
    }

    /**
     *
     * @return true if a ";" separator is at left of caret, and not char exists
     * between separator and caret
     */
    public boolean separatorBeforeCaret() {
        String s = text.substring(0, caretPosition);
        int delim = s.lastIndexOf(";");
        //test if cursor is at begining of line
        if (caretPosition > 0 && System.getProperty("line.separator").equals(String.valueOf(s.charAt(caretPosition - 1)))) {
            return false;
        }
        s = s.substring(delim+1).trim();
        return (s.length() == 0);
    }

    /**
     * if caret is at end of line, a ";" may be at the left of the caret. if so, and only spaces are present,
     * forces rewind caret position
     */
    public void rewindCaret() {
        if (separatorBeforeCaret()) {
            this.caretPosition = text.substring(0, caretPosition).trim().lastIndexOf(";");
        }
    }

    /**
     *
     * @return true if caret if at the end of a line
     */
    private boolean caretAtEndOfLine() {
        if (caretPosition == 0) return false;
        
        boolean res = ( text.substring(caretPosition).trim().length() == 0 ||
                (text.substring(caretPosition).contains(System.getProperty("line.separator"))) && separatorBeforeCaret()) ;
        return res;
    }

    /**
     *
     * @return the string delimited by the caret position at its right, and the
     * previous query delimiter (";" ) at its left
     */
    private String findQueryBeforeCaret() {
        String res = text.substring(0, caretPosition).trim();
        int delim = res.lastIndexOf(";");

        if (delim > -1) {
            res = res.substring(delim+1).trim();
        }
        return res;
    }

    /**
     *
     * @return the string delimited by the caret position at its left, and the
     * previous query delimiter (";" ) at its right
     */
    private String findQueryAfterCaret() {
        String res = text.substring(caretPosition).trim();
        int delim = res.indexOf(";");

        if (delim > -1) {
            res = res.substring(0, delim).trim();
        }
        return res;
    }
}
