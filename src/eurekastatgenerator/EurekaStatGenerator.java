/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eurekastatgenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

public class EurekaStatGenerator {

    private Connection con = null;
    private Properties props = null;
    /**
     * the label text with placeholders
     */
    public static final String LABEL_TXT = "<html>Enter password for database: <br><b>&nbsp;&nbsp;&nbsp;&nbsp;$HOST$:$PORT$/$INSTANCE$"
            + "</b><br>Username: <br>&nbsp;&nbsp;&nbsp;&nbsp;<b>$USER$</b></html>";

    public EurekaStatGenerator() {
        // loads properties file
        props = new Properties();
        try {

            props.load(EurekaStatGenerator.class.getClassLoader()
                    .getResourceAsStream(
                    "eurekastatgenerator/statgenerator.properties"));
        } catch (IOException ex) {
            System.out.println("cannot read statgenerator.properties file ");
            System.exit(1);
        }

        // opens the JDBC connection by prompting user for passwd
        String label = EurekaStatGenerator.LABEL_TXT
                .replace("$HOST$", props.getProperty("dbhost"))
                .replace("$PORT$", props.getProperty("dbport"))
                .replace("$INSTANCE$", props.getProperty("dbname"))
                .replace("$USER$", props.getProperty("dbuser"));

        while (true) {
            char[] ret = null;
            if (props.getProperty("dbpwd") == null || props.getProperty("dbpwd").isEmpty()) {
                ret = PromptForm.promptForPassword(null, label);
            } else {
                ret = props.getProperty("dbpwd").toCharArray();
            }

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");

                final String url = "jdbc:oracle:thin:@"
                        + props.getProperty("dbhost") + ":"
                        + props.getProperty("dbport") + ":"
                        + props.getProperty("dbname");

                this.con = DriverManager.getConnection(url,
                        props.getProperty("dbuser"), new String(ret));
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "An error occured:\n" + e.getMessage()
                        + "\nfor cnx: " + props.getProperty("dbhost") + ":" + props.getProperty("dbport")
                        + "/" + props.getProperty("dbname") + " user: " + props.getProperty("dbuser"));
                if (!e.getMessage().contains("username/password")) {
                    // force an exit to avoid infinite loop
                    System.exit(1);
                }
            }
        }
    }

    private String concatQueryResult(String query) {
        final StringBuilder result = new StringBuilder();
        Statement statement1 = null;

        printQuery(query);

        try {
            final ResultSet resulSet1;

            statement1 = con.createStatement();
            resulSet1 = statement1.executeQuery(query.toString());

            while (resulSet1.next()) {
                if (result.length() > 0) {
                    result.append(",");
                }

                result.append(resulSet1.getString(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (statement1 != null) {
                try {
                    statement1.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return result.toString();
    }

    private Object getSingleQuery(String query) {
        Statement statement1 = null;

        printQuery(query);

        try {
            final ResultSet resulSet1;

            statement1 = con.createStatement();
            resulSet1 = statement1.executeQuery(query.toString());

            if (resulSet1.next()) {
                    return resulSet1.getObject(1);
            } else {
                throw new RuntimeException("No result");
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (statement1 != null) {
                try {
                    statement1.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Generates the database/schema stats, writing result in a file named after the table.
     *
     * @param out
     */
    public void generateTableStats() {
        final String[] schemaNameList = getSchemaNameList();

        for (int i = 0; i < schemaNameList.length; i++) {
            final String schemaName = schemaNameList[i];
            final List<String> statisticList = new ArrayList<String>();
            StringBuilder query = null;
            Statement statement1 = null;

            statisticList
                    .add("owner,table_name,column_name,data_type,comments,rows_count,rows_not_null_count,constraints");

            query = new StringBuilder();

            query.append("select distinct ");
            query.append("	t_1.owner, t_1.table_name, t_1.column_name, t_1.data_type || '(' || t_1.data_length || ')', t_2.comments ");
            query.append("from ");
            query.append("	all_tab_columns t_1 ");
            query.append("		left outer join user_col_comments t_2 on ");
            query.append("			(");
            query.append("				t_1.table_name = t_2.table_name and ");
            query.append("				t_1.column_name = t_2.column_name ");
            query.append("			)");
            query.append("where ");
            query.append("	t_1.owner = '" + schemaName + "' ");
            query.append("order by ");
            query.append("	t_1.owner, t_1.table_name, t_1.column_name");

            printQuery(query.toString());

            try {
                final ResultSet resulSet1;
                final ResultSetMetaData resultSetMetaData1;
                StringBuilder statistic = null;

                statement1 = con.createStatement();
                resulSet1 = statement1.executeQuery(query.toString());
                resultSetMetaData1 = resulSet1.getMetaData();

                while (resulSet1.next()) {
                    final String tableName = resulSet1.getString(2);
                    final String columnName = resulSet1.getString(3);

                    statistic = new StringBuilder();

                    for (int j = 1; j <= resultSetMetaData1.getColumnCount(); j++) {
                        if (statistic.length() > 0) {
                            statistic.append(",");
                        }

                        statistic.append(resulSet1.getObject(j));
                    }

                    statistic.append(",");
                    statistic.append(countRowList(schemaName, tableName));

                    statistic.append(",");
                    
                    try {
                        statistic.append(getSingleQuery("select count("
                                + schemaName + "." + tableName + ".\"" + columnName
                                + "\") from " + schemaName + "." + tableName
                                + " where " + schemaName + "." + tableName + ".\""
                                + columnName + "\" is not null"));
                    } catch (RuntimeException re) {
                        // blob type throws an exception when getting an object on it
                        statistic.append(0);
                    }

                    statistic.append(",");

                    query = new StringBuilder();

                    query.append("select ");
                    query.append("	t_2.search_condition ");
                    query.append("from ");
                    query.append("	all_cons_columns t_1, ");
                    query.append("	all_constraints t_2 ");
                    query.append("where ");
                    query.append("	t_1.owner = '" + schemaName + "' and ");
                    query.append("	t_1.table_name = '" + tableName + "' and ");
                    query.append("	t_1.column_name = '" + columnName + "' and ");
                    query.append("	t_1.owner = t_2.owner and ");
                    query.append("	t_1.constraint_name = t_2.constraint_name and ");
                    query.append("	t_1.table_name = t_2.table_name and ");
                    query.append("	t_2.constraint_type = 'C' and ");
                    query.append("	t_2.search_condition is not null ");

                    statistic.append(concatQueryResult(query.toString()));

                    statisticList.add(statistic.toString());
                }

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                if (statement1 != null) {
                    try {
                        statement1.close();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            writeFile(props.getProperty("dbname") + "_" + schemaName
                    + "_statistics.txt", statisticList);
        }
    }

    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printQuery(String query) {
        System.err.println("Query : "
                + query.toString().replaceAll("[ ]+", " ")
                .replaceAll("\\t", " "));
    }

    private void writeFile(String fileName, Collection<String> lineList) {
        BufferedWriter writer = null;

        try {
            final Iterator<String> iterator = lineList.iterator();

            writer = new BufferedWriter(new FileWriter(
                    props.getProperty("output") + File.separator + fileName));

            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.write(System.getProperty("line.separator"));
            }

            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    private Map<String, BigDecimal> _countRowList = new HashMap<String, BigDecimal>();

    private BigDecimal countRowList(String schema, String table) {
        final String key = schema + "." + table;

        if (!_countRowList.containsKey(key)) {
            _countRowList.put(key, (BigDecimal) getSingleQuery("select count(*) from "
                    + key));
        }

        return _countRowList.get(key);
    }
    private Map<String, List<String>> _emptyTableList = new HashMap<String, List<String>>();

    private List<String> getEmptyTableList(String schema) {
        final List<String> resultList = new ArrayList<String>();
        StringBuilder query = null;
        Statement statement1 = null;

        query = new StringBuilder();

        query.append("select distinct ");
        query.append("	t_1.table_name ");
        query.append("from ");
        query.append("	all_tables t_1 ");
        query.append("where ");
        query.append("	t_1.owner = '" + schema + "' ");
        query.append("order by ");
        query.append("	t_1.table_name ");

        printQuery(query.toString());

        try {
            final ResultSet resulSet1;

            statement1 = con.createStatement();
            resulSet1 = statement1.executeQuery(query.toString());

            while (resulSet1.next()) {
                final String tableName = resulSet1.getString(1);

                if (countRowList(schema, tableName).equals(new BigDecimal(0))) {
                    resultList.add(tableName);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (statement1 != null) {
                try {
                    statement1.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return resultList;
    }

    private String[] getSchemaNameList() {
        final String buffer = props.getProperty("schemas_or_users");

        return buffer.substring(1, buffer.length() - 1).split("','");
    }

    public void analyseEmptyTableList() {
        final String[] schemaNameList = getSchemaNameList();

        for (int i = 0; i < schemaNameList.length; i++) {
            final String schemaName = schemaNameList[i];

            writeFile(props.getProperty("dbname") + "_" + schemaName
                    + "_emptyTableList.txt", getEmptyTableList(schemaName));
        }
    }

    public void analyseFKGraph() {
        final String[] schemaNameList = getSchemaNameList();

        for (int i = 0; i < schemaNameList.length; i++) {
            final String schemaName = schemaNameList[i];

            StringBuilder query = null;
            Statement statement1 = null;
            final List<String> completeGraphList = new ArrayList<String>();
            final List<String> partialGraphList = new ArrayList<String>();
            final List<String> emptyTableList = getEmptyTableList(schemaName);
            boolean isPrimaryEmptyTable = false;

            query = new StringBuilder();

            query.append("select distinct ");
            query.append("	t_4.table_name, ");
            query.append("	t_4.column_name, ");
            query.append("	t_1.table_name, ");
            query.append("	t_1.column_name ");
            query.append("from ");
            query.append("	all_cons_columns t_1, ");
            query.append("	all_constraints t_2, ");
            query.append("	all_constraints t_3, ");
            query.append("	all_cons_columns t_4 ");
            query.append("where ");
            query.append("	t_1.owner = '" + schemaName + "' and ");
            query.append("	t_1.owner = t_2.owner and ");
            query.append("	t_1.constraint_name = t_2.constraint_name and ");
            query.append("	t_2.constraint_type = 'R' and ");
            query.append("	t_2.r_constraint_name = t_3.constraint_name and  ");
            query.append("	t_2.r_owner = t_3.owner  and ");
            query.append("	t_3.constraint_name = t_4.constraint_name and ");
            query.append("	t_3.owner = t_4.owner ");
            query.append("order by ");
            query.append("	t_4.table_name, ");
            query.append("	t_4.column_name, ");
            query.append("	t_1.table_name, ");
            query.append("	t_1.column_name ");

            printQuery(query.toString());

            try {
                final ResultSet resulSet1;
                String lastTableName = null;

                statement1 = con.createStatement();
                resulSet1 = statement1.executeQuery(query.toString());

                while (resulSet1.next()) {
                    final String pkTableName = resulSet1.getString(1);
                    final String pkColumnName = resulSet1.getString(2);
                    final String fkTableName = resulSet1.getString(3);
                    final String fkColumnName = resulSet1.getString(4);
                    final String graphLink = "     --> " + fkTableName + "  ("
                            + pkTableName + "." + pkColumnName + ","
                            + fkTableName + "." + fkColumnName + ")";

                    if (lastTableName == null
                            || !lastTableName.equals(pkTableName)) {
                        completeGraphList.add("");
                        completeGraphList.add(pkTableName);

                        if (emptyTableList.contains(pkTableName)) {
                            isPrimaryEmptyTable = true;
                        } else {
                            partialGraphList.add("");
                            partialGraphList.add(pkTableName);
                        }
    
 
                        lastTableName = pkTableName;
                    }

                    completeGraphList.add(graphLink);

                    if (!isPrimaryEmptyTable) {
                        if (!emptyTableList.contains(fkTableName)) {
                            partialGraphList.add(graphLink);
                        }
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                if (statement1 != null) {
                    try {
                        statement1.close();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }

            writeFile(props.getProperty("dbname") + "_" + schemaName
                    + "_completeGraph.txt", completeGraphList);
            writeFile(props.getProperty("dbname") + "_" + schemaName
                    + "_partialGraph.txt", partialGraphList);
        }
    }
    
    /**
     * Dumps the tables contained in the dictionary schema, set in the properties file.
     * 
     */

    public void describeSchema() {
        final String[] schemaNameList = getSchemaNameList();
		StringBuilder query = new StringBuilder();
        

        query.append("select distinct ");
        query.append("	t_1.owner, t_1.table_name, t_1.column_name, t_1.data_type || '(' || t_1.data_length || ')', t_2.comments ");
        query.append("from ");
        query.append("	all_tab_columns t_1 ");
        query.append("		left outer join user_col_comments t_2 on ");
        query.append("			(");
        query.append("				t_1.table_name = t_2.table_name and ");
        query.append("				t_1.column_name = t_2.column_name ");
        query.append("			)");
        query.append("where ");
        query.append("	t_1.owner = '" + props.getProperty("dict_schema") + "' ");
        query.append("order by ");
        query.append("	t_1.owner, t_1.table_name, t_1.column_name");
        
    }
   

    /**
     * Exports the given schemaName.tableName in CSV format, file name is
     * qualified tableName. Export parameters are read from properties
     *
     * @param schemaName
     * @param tableName
     */
    public void tableToCsv(String schemaName, String tableName) throws Exception {
        if ("true".equalsIgnoreCase(props.getProperty("exclude_dollar_table")) && tableName.contains("$")) {
            System.out.println("EXCLUDING table: " + schemaName + "." + tableName + ". Its name contains '$' char.");
            return;
        }
        System.out.println("dumping table: " + schemaName + "." + tableName);
        boolean headers = props.getProperty("csv_header").equalsIgnoreCase("true");

        Statement stmt = con.createStatement();
        String query = "select * from " + schemaName + "." + tableName;
        printQuery(query);
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        final String csvSep = props.getProperty("csv_separator");
        final String fileName = schemaName + "." + tableName + ".csv";
        
        Collection<String> lines = new ArrayList<String>();
        
        String sep = "";
        StringBuilder b = new StringBuilder();
        
        if (headers) {
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                b.append(sep).append(rsmd.getColumnName(i+1));
                sep = csvSep;
            }
            lines.add(b.toString());
        }
        
        while (rs.next()) {
            sep = "";
            b = new StringBuilder();
            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
                b.append(sep).append(rs.getString(j));
                sep = csvSep;
            }
            lines.add(b.toString());
        }
        
        writeFile(fileName, lines);
        stmt.close();
        System.out.println("\t table: " + schemaName + "." + tableName + " dumped.");
    }
    
    /**
     * Exports all the tables in the given schema to csv files.
     * Caution... :)
     * @param schemaName the schema to export 
     */
    public void schemaToCsv(String schemaName) throws Exception {
        System.out.println("dumping tables for schema: " + schemaName);
        Statement stmt = con.createStatement();
        String query = "select owner, table_name from all_tables where owner = '" + schemaName.toUpperCase() + "'"
                + " order by table_name";
        printQuery(query.toString());
        ResultSet rs = stmt.executeQuery(query);
        
        while (rs.next()) {
            tableToCsv(rs.getString(1), rs.getString(2));
        }
        
        stmt.close();
        System.out.println("Tables dumped for schema: " + schemaName);
    }
    
    /**
     * Exports the tables contained in the schema configured under dict_schema property.
     */
    public void exportDictTables() {
        try {
            schemaToCsv(props.getProperty("dict_schema"));
        } catch (Exception e) {
            System.out.println("Exporting tables from: " + props.getProperty("dict_schema") 
                    + " failed: " + e.getMessage());
        }
    }
    
    /**
     * generates stats for all views in a schema
     */
    public void generateViewsStats() throws Exception {
        boolean headers = props.getProperty("csv_header").equalsIgnoreCase("true");
        Statement stmt = con.createStatement();
        String query = "select owner, view_name from all_views where owner = " + props.getProperty("schemas_or_users") 
                + " order by view_name";
        printQuery(query);
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        final String csvSep = props.getProperty("csv_separator");
        
        Collection<String> lines = new ArrayList<String>();
        
        String sep = "";
        StringBuilder b = new StringBuilder();
        
        if (headers) {
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                //b.append(sep).append(rsmd.getColumnName(i+1));
                sep = csvSep;
            }
            lines.add(b.toString());
        }
        
        while (rs.next()) {
            sep = "";
            b = new StringBuilder();
            System.out.println(rs.getString(1) + "." + rs.getString(2));
//            for (int j = 1; j <= rsmd.getColumnCount(); j++) {
//                b.append(sep).append(rs.getString(j));
//                sep = csvSep;
//            }
            lines.add(b.toString());
        }
        
        //writeFile(fileName, lines);
        stmt.close();
        //System.out.println("\t table: " + schemaName + "." + tableName + " dumped.");
    }
    
    /**
     * generates stats for all views in a schema
     */
    public void generateViewStats(String schemaName, String viewName) throws Exception {
        boolean headers = props.getProperty("csv_header").equalsIgnoreCase("true");
        Statement stmt = con.createStatement();
        String query = "select * from " + schemaName + ". " + viewName + " where 1=0";
        printQuery(query);
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsmd = rs.getMetaData();
        final String csvSep = props.getProperty("csv_separator");
        
        Collection<String> lines = new ArrayList<String>();
        
       
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            StringBuilder b = new StringBuilder(schemaName);
            b.append(csvSep).append(viewName).append(csvSep)
                .append(rsmd.getColumnName(i)).append(csvSep)
                .append(rsmd.getColumnTypeName(i))
                .append("(").append(rsmd.getPrecision(i)).append(")")
                    .append("no comment");
            System.out.println(b.toString());
            lines.add(b.toString());
        }
        //writeFile(fileName, lines);
        stmt.close();
        //System.out.println("\t table: " + schemaName + "." + tableName + " dumped.");
    }
    
    public static void main(String[] args) throws Exception {
        EurekaStatGenerator esg = new EurekaStatGenerator();

//        esg.analyseEmptyTableList();
//        esg.analyseFKGraph();
        esg.generateTableStats();
//        esg.tableToCsv("nico", "geoname");
//        esg.schemaToCsv("nico");
//        esg.schemaToCsv("nico");
//        esg.generateViewStats("nico", "testview");
        esg.closeConnection();
    }

}
