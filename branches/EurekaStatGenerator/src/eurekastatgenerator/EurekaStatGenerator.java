/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eurekastatgenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author nicolas
 */
public class EurekaStatGenerator {
    private Connection con = null;
    private Properties props = null;
    private static final String OUTPUT_DIRECTORY_PATH = "/Users/nicolas/tmp";
    /**
     * the label text with placeholders
     */
    public static final String LABEL_TXT = "<html>Enter password for database: <br><b>&nbsp;&nbsp;&nbsp;&nbsp;$HOST$:$PORT$/$INSTANCE$"
            + "</b><br>Username: <br>&nbsp;&nbsp;&nbsp;&nbsp;<b>$USER$</b></html>";

    public EurekaStatGenerator() {
        // loads properties file
        props = new Properties();
        try {

            props.load(EurekaStatGenerator.class.getClassLoader().getResourceAsStream("eurekastatgenerator/statgenerator.properties"));
        } catch (IOException ex) {
            System.out.println("cannot read statgenerator.properties file ");
            System.exit(1);
        }
        
        // opens the JDBC connection by prompting user for passwd
        String label = EurekaStatGenerator.LABEL_TXT.replace("$HOST$", "192.168.1.255");
        char [] ret = PromptForm.promptForPassword(null);
        System.out.println("p: " + new String(ret));
        
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            final String url = "jdbc:oracle:thin:@" + props.getProperty("dbhost") + ":"
                    + props.getProperty("dbport") + "/" + props.getProperty("dbname");

            this.con = DriverManager.getConnection(url, props.getProperty("dbuser"), new String(ret));
        } catch (SQLException e) {
            if (e.getMessage().contains("password")) {
                PromptForm.setMsg(e.getMessage());
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EurekaStatGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void openConnection(String pwd) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            final String url = "jdbc:oracle:thin:@" + props.getProperty("dbhost") + ":"
                    + props.getProperty("dbport") + "/" + props.getProperty("dbname");

            this.con = DriverManager.getConnection(url, props.getProperty("dbuser"), pwd);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(EurekaStatGenerator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void promptPwd() {
        String pwd = null;
        String label = EurekaStatGenerator.LABEL_TXT.replace("$HOST$", "192.168.1.255");
        //this.pwdFrame = new PasswdFrame(this, label);
        //this.pwdFrame.setVisible(true);
    }
    
    /**
     * Generates the database/schema stats, writing result in the given out
     * stream
     *
     * @param out
     */
    public void generateStats(PrintStream out) throws SQLException {
        if (out == null) {
            System.out.println("Null stream to write stats to, quitting...");
            return;
        }

        Statement stmt = con.createStatement();
        Statement stmt2 = null;
        String query = "SELECT DISTINCT owner, table_name, column_name, data_type, data_length \n"
                + "    FROM all_tab_columns\n"
                + "    WHERE owner IN (" + props.getProperty("schemas_or_users") + ")"
                + "order by owner, table_name, column_name";

        ResultSet rs = stmt.executeQuery(query);
        ResultSet rs2 = null;
        String subquery1 = "";
        long rowCount = 0l;
        long rowsNotNull = 0l;

        // headers
        out.println("owner,table_name,column_name,data_type,rows_count,rows_not_null");

        while (rs.next()) {
            stmt2 = con.createStatement();
            subquery1 = "select count(*) from " + rs.getString(1) + "." + rs.getString(2);
            rs2 = stmt2.executeQuery(subquery1);
            rs2.next();
            rowCount = rs2.getLong(1);
            rs2.close();
            stmt2.close();

            stmt2 = con.createStatement();
            subquery1 = "select count(" + rs.getString(3) + ") from " + rs.getString(1) + "." + rs.getString(2) + " where " + rs.getString(3) + " is not null";
            rs2 = stmt2.executeQuery(subquery1);
            rs2.next();
            rowsNotNull = rs2.getLong(1);
            stmt2.close();

            out.println(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4) + "(" + rs.getString(5) + ")," + rowCount + "," + rowsNotNull);
        }

        stmt.close();
        con.close();
    }

    private static void printQuery(StringBuilder query) {
        System.err.println("Query : "
                + query.toString().replaceAll("[ ]+", " ")
                .replaceAll("\\t", " "));
    }

    private void writeFile(String fileName, Collection<String> lineList) {
        BufferedWriter writer = null;

        try {
            final Iterator<String> iterator = lineList.iterator();

            writer = new BufferedWriter(new FileWriter(props.getProperty("output")
                    + "/" + fileName));

            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.write("\r\n");
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

    private List<String> getEmptyTableList() throws Exception {
        final List<String> resultList = new ArrayList<String>();
        StringBuilder query = null;
        Statement statement1 = null;

        query = new StringBuilder();

        query.append("select distinct ");
        query.append("	t_1.table_name ");
        query.append("from ");
        query.append("	all_tables t_1 ");
        query.append("where ");
        query.append("	t_1.owner = '" + props.getProperty("schemas_or_users") + "' ");
        query.append("order by ");
        query.append("	t_1.table_name ");

        printQuery(query);

        try {
            final ResultSet resulSet1;

            statement1 = con.createStatement();
            resulSet1 = statement1.executeQuery(query.toString());

            while (resulSet1.next()) {
                final String tableName = resulSet1.getString(1);
                Statement statement2 = null;

                query = new StringBuilder();

                query.append("select ");
                query.append("	count(*) as n ");
                query.append("from ");
                query.append("	" + props.getProperty("schemas_or_users") + "." + tableName + " ");

                printQuery(query);

                try {
                    final ResultSet resulSet2;

                    statement2 = con.createStatement();
                    resulSet2 = statement2.executeQuery(query.toString());

                    if (resulSet2.next()) {
                        if (resulSet2.getInt(1) == 0) {
                            resultList.add(tableName);
                        }
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                } finally {
                    if (statement2 != null) {
                        try {
                            statement2.close();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

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

    private void analyseFKGraph() throws Exception {
        StringBuilder query = null;
        Statement statement1 = null;
        final List<String> completeGraphList = new ArrayList<String>();
        final List<String> partialGraphList = new ArrayList<String>();
        final List<String> emptyTableList = getEmptyTableList();
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
        query.append("	t_1.owner = '" + props.getProperty("schemas_or_users") + "' and ");
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

        printQuery(query);

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
                        + pkTableName + "." + pkColumnName + "," + fkTableName
                        + "." + fkColumnName + ")";

                if (lastTableName == null || !lastTableName.equals(pkTableName)) {
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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (statement1 != null) {
                try {
                    statement1.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        writeFile("completeGraph.txt", completeGraphList);
        writeFile("partialGraph.txt", partialGraphList);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        EurekaStatGenerator esg = new EurekaStatGenerator();

        esg.writeFile("emptyTableList.txt", esg.getEmptyTableList());
        esg.analyseFKGraph();
        esg.generateStats(System.out);
    }
}
