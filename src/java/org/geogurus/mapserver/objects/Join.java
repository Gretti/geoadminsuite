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

/*
 * Join.java
 *
 * Created on 20 mars 2002, 10:46
 */
package org.geogurus.mapserver.objects;

import java.io.File;
import java.util.logging.Logger;

import org.geogurus.tools.string.ConversionUtilities;

/**
 * Defines how a specific join is handled.
 * Starts with the keyword JOIN and terminate with the keywrod END.
 * Joins are defined within a query object.
 *
 * @author  Bastien VIALADE
 */
public class Join extends MapServerObject implements java.io.Serializable {

    // Constants. Join types
    public static final byte SINGLE = 0;
    public static final byte MULTIPLE = 1;
    /** Parameters required for the join table's database connection (not required for DBF or CSV joins). 
     * The following is an example for PostgreSQL: 
     * CONNECTION "host=127.0.0.1 port=5432 user=postgres password=postgres dbname=somename"
     */
    private String connection;
    /**
     * Type of connection (not required for DBF or CSV joins). The following is an example for PostgreSQL:
     * CONNECTIONTYPE ogr 
     */
    private String connectionType;
    /** Join item in the shapeFile*/
    private String from;
    /** Unique name for this join required*/
    private String name;
    /** Name of XBase file (DBF, must be a full path) to join TO*/
    private File table;
    /** Template to use with one-to-many joins.
     * The template is processed once for each record and can
     * only contains substitutions for items in the joined table. */
    private File template;
    /** Join item in the table to be joined */
    private String to;
    /** The type of join.*/
    private byte type;

    /** Empty constructor */
    public Join() {
        this(null, null, null, null, null, null, null);
    }

    /** Creates a new instance of Join */
    public Join(String connection_, String connectionType_, String from_, 
            String name_, File table_, File template_, String to_) {
        
        logger = Logger.getLogger(this.getClass().getName());
        connection = connection_;
        connectionType = connectionType_;
        from = from_;
        name = name_;
        table = table_;
        template = template_;
        to = to_;
        type = Join.SINGLE;
    }

    /** Get methods to get join parameters */
    public String getFrom() {
        return from;
    }

    public String getName() {
        return name;
    }

    public File getTable() {
        return table;
    }

    public File getTemplate() {
        return template;
    }

    public String getTo() {
        return to;
    }

    public byte getType() {
        return type;
    }

    /** Sets methods*/
    public void setFrom(String from_) {
        from = from_;
    }

    public void setName(String name_) {
        name = name_;
    }

    public void setTable(File table_) {
        table = table_;
    }

    public void setTemplate(File template_) {
        template = template_;
    }

    public void setTo(String to_) {
        to = to_;
    }

    public void setType(byte type_) {
        type = type_;
    }

    /** Loads data from file
     * and fill Object parameters with.
     * @param br BufferReader containing file data to read
     * @return true is mapping done correctly
     */
    public boolean load(java.io.BufferedReader br) {
        boolean isName = false;

        try {
            String[] tokens;
            String line;

            while ((line = br.readLine()) != null) {

                // Looking for the first util line
                while ((line.trim().length() == 0) || (line.trim().startsWith("#")) || (line.trim().startsWith("%"))) {
                    line = br.readLine();
                }
                tokens = ConversionUtilities.tokenize(line.trim());
                if (tokens[0].equalsIgnoreCase("CONNECTION")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for CONNECTION: " + line);
                        return false;
                    }
                    connection = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("CONNECTIONTYPE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for CONNECTIONTYPE: " + line);
                        return false;
                    }
                    connectionType = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("FROM")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for FROM: " + line);
                        return false;
                    }
                    from = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("NAME")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for NAME: " + line);
                        return false;
                    }
                    name = ConversionUtilities.getValueFromMapfileLine(line);
                    isName = true;
                } else if (tokens[0].equalsIgnoreCase("TABLE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for TABLE: " + line);
                        return false;
                    }
                    table = new File(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("TEMPLATE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for TEMPLATE: " + line);
                        return false;
                    }
                    template = new File(ConversionUtilities.getValueFromMapfileLine(line));
                } else if (tokens[0].equalsIgnoreCase("TO")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for TO: " + line);
                        return false;
                    }
                    to = ConversionUtilities.getValueFromMapfileLine(line);
                } else if (tokens[0].equalsIgnoreCase("TYPE")) {
                    if (tokens.length < 2) {
                        MapServerObject.setErrorMessage("Join.load: Invalid syntax for TYPE: " + line);
                        return false;
                    }
                    if (tokens[1].equalsIgnoreCase("MULTIPLE")) {
                        type = this.MULTIPLE;
                    } else if (tokens[1].equalsIgnoreCase("SINGLE")) {
                        type = this.SINGLE;
                    } else {
                        MapServerObject.setErrorMessage("Join.load: Invalid value for TYPE: " + line);
                        return false;
                    }
                } else if (tokens[0].equalsIgnoreCase("END")) {
                    return true;
                } else {
                    MapServerObject.setErrorMessage("Join.load: unknown token: " + line);
                    return false;
                }
            }
        } catch (Exception e) { // Bad coding, but works...
            logger.warning("Join.load(). Exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (!isName) {
            MapServerObject.setErrorMessage("Join.load: required NAME value not set");
            return false;
        }

        return true;
    }

    /** Saves data to file
     * using Object parameters with mapFile format.
     * @param bw BufferWriter containing file data to write
     * in linked file.
     * @return true is mapping done correctly
     */
    public boolean saveAsMapFile(java.io.BufferedWriter bw) {
        boolean result = true;
        try {
            bw.write("\t join\n");
            if (connection != null) {
                bw.write("\t\t connection " + connection + "\n");
            }
            if (connectionType != null) {
                bw.write("\t\t connectiontype " + connectionType + "\n");
            }
            if (from != null) {
                bw.write("\t\t from " + from + "\n");
            }
            if (name != null) {
                bw.write("\t\t name " + name + "\n");
            }
            if (table != null) {
                bw.write("\t\t table " + table.getPath().replace('\\', '/') + "\n");
            }
            if (template != null) {
                bw.write("\t\t template " + template.getPath().replace('\\', '/') + "\n");
            }
            if (to != null) {
                bw.write("\t\t to " + to + "\n");
            }
            switch (type) {
                case MULTIPLE:
                    bw.write("\t\t type MULTIPLE\n");
                    break;
                case SINGLE:
                    bw.write("\t\t type SINGLE\n");
                    break;
            }
            bw.write("\t end\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return result;
    }

    public String toString() {
        return "Not yet implemented";
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
}

