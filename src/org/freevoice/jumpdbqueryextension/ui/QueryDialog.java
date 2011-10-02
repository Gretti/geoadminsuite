/*
 *
 *  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
 *  JUMP is Copyright (C) 2003 Vivid Solutions
 *
 *  This file is part of the JUMP DB Query Plugin.
 *
 *  The JUMP DB Query Plugin is free software; you can redistribute it and/or
 *  modify it under the terms of the Lesser GNU General Public License as
 *  published *  by the Free Software Foundation; either version 3 of the
 *  License, or  (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  Lesser GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freevoice.jumpdbqueryextension.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import org.freevoice.jumpdbqueryextension.util.DbConnectionParameters;

import javax.swing.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import jsyntaxpane.DefaultSyntaxKit;
import org.freevoice.jumpdbqueryextension.JumpDbQueryPlugin;
import org.freevoice.jumpdbqueryextension.util.Logger;
import org.freevoice.jumpdbqueryextension.util.QueryExtractor;

/**
 *
 */
public class QueryDialog extends JFrame implements ActionListener, KeyListener, MouseListener {

    public static final String CANCEL_ACTION_COMMAND = "CancelDbQUery";
    public static final String CANCEL_QUERY_ACTION_COMMAND = "CancelQueryDbQUery";
    public static final String CLEAR_ACTION_COMMAND = "ClearHistory";
    public static final String RUN_ACTION_COMMAND = "RunDbQuery";
    public static final String REFRESH_ACTION_COMMAND = "RefreshDbQuery";
    public static final String DB_CHANGED_COMMAND = "DbChanged";
    public static final String TEST_ACTION_COMMAND = "test";
    private boolean cancelled = true;
    private JPasswordField passwordField = null;
    private JTextField usernameField = null;
    public JEditorPane queryEditor = null;
    public JTextArea historyArea = null;
    private JTextField jdbcUrlField = null;
    private JTextField driverField = null;
    private JTextField queryClassField = null;
    private JPanel queryPanel = null;
    private JPanel progressPanel = null;
    private JButton runButton = null;
    private JButton cancelButton = null;
    private JButton cancelQueryButton = null;
    private JButton clearButton = null;
    private JButton refreshButton = null;
    private JComboBox dbSelectMenu = null;
    private static QueryDialog queryDialog = null;
    public JProgressBar queryProgress = null;
    public JLabel queryAreaLabel = null;
    private List dbParameterList = null;
    public JumpDbQueryPlugin plugin = null;
    // the list of queries, for history
    public List<String> historyList;
    private ImageIcon dbErrorIcon;
    private ImageIcon cancelIcon;
    public JLabel dbErrorIconLabel;
    public JLabel cancelIconLabel;

    public QueryDialog(
            JumpDbQueryPlugin plugin,
            Frame frame,
            String title,
            List<DbConnectionParameters> connectionParameters,
            boolean isModal) {

        //super(frame, title, isModal);
        super();
        dbParameterList = connectionParameters;
        this.plugin = plugin;
        historyList = new ArrayList<String>();
        //initUICode();
    }

    public static QueryDialog showDialog(
            JumpDbQueryPlugin plugin,
            Frame parentFrame,
            String title,
            List<DbConnectionParameters> connectionParameters) {

        if (queryDialog == null) {
            queryDialog = new QueryDialog(plugin, parentFrame, title, connectionParameters, false);
        }

        //queryDialog.setVisible(true);
        return queryDialog;
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public void setUsername(JTextField username) {
        this.usernameField = username;
    }

    public String getDriver() {
        return driverField.getText();
    }

    public void setDriver(JTextField driver) {
        this.driverField = driver;
    }

    public String getQueryClass() {
        return queryClassField.getText();
    }

    public void setQueryClass(JTextField queryClass) {
        this.queryClassField = queryClass;
    }

    /**
     *
     * @return the SQL query extracted from the current caret position, dealing with end of lines
     * and query separator: ";"
     */
    public String getQuery() {
        return getQueryFor(queryEditor.getText(), queryEditor.getCaretPosition());
    }

    /**
     *
     * @return the SQL query extracted from the query area
     */
    public String getHistoryAreaQuery() {
        return getQueryFor(historyArea.getText(), historyArea.getCaretPosition());
    }

    /**
     *
     * @return the SQL query extracted from the given caret position, dealing with end of lines
     * and query separator: ";"
     */
    private String getQueryFor(String text, int caret) {
        QueryExtractor queryExtractor = new QueryExtractor(text, caret);
        String res = "";
        try {
            res = queryExtractor.getQuery();
        } catch (IllegalArgumentException iae) {
            // TODO: should we log error in this case ?
        }
        return res;
    }

    public String getJdbcUrl() {
        return jdbcUrlField.getText();
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void initUICode() {

        JLabel jdbcUrlLabel = null;
        JLabel usernameLabel = null;

        this.getContentPane().setLayout(new BorderLayout());

        queryPanel = new JPanel();
        queryPanel.setLayout(new GridBagLayout());


        if (dbParameterList.size() > 0) {
            Object[] comboBoxArray = new Object[dbParameterList.size() + 1];
            comboBoxArray[0] = "Select database...";
            System.arraycopy(dbParameterList.toArray(), 0, comboBoxArray, 1, dbParameterList.size());
            dbSelectMenu = new JComboBox(comboBoxArray);
        } else {
            Object[] comboBoxArray = new Object[2];
            comboBoxArray[0] = "Select database...";
            comboBoxArray[1] = "Put dbquery.properties in you class path if you want to add default databases";
            dbSelectMenu = new JComboBox(comboBoxArray);
        }

        dbSelectMenu.setEnabled(true);
        dbSelectMenu.setActionCommand(DB_CHANGED_COMMAND);

        // changes the listener
        dbSelectMenu.addActionListener(this);
        //dbSelectMenu.addActionListener(this.plugin);


        queryPanel.add(dbSelectMenu, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        JPanel infoPanel = new JPanel(new BorderLayout());
        Dimension infoPanelDim = new Dimension(500, 25);
        infoPanel.setPreferredSize(infoPanelDim);
        infoPanel.setSize(infoPanelDim);

        queryAreaLabel = new JLabel("");
        queryAreaLabel.setForeground(new Color(183, 29, 7));
        queryAreaLabel.setPreferredSize(new Dimension(25, 300));

        infoPanel.add(queryAreaLabel, BorderLayout.CENTER);

        // the database error icon
        dbErrorIcon = createImageIcon("database_error.png", "");
        dbErrorIconLabel = new JLabel(dbErrorIcon);
        infoPanel.add(dbErrorIconLabel, BorderLayout.WEST);
        dbErrorIconLabel.setVisible(false);

        progressPanel = new JPanel(new BorderLayout());

        // the cancel query icon
        cancelIcon = createImageIcon("cancel.png", "");
        cancelIconLabel = new JLabel(cancelIcon);
        cancelQueryButton = new JButton(cancelIcon);
        cancelQueryButton.setActionCommand(CANCEL_QUERY_ACTION_COMMAND);
        cancelQueryButton.setToolTipText("cancel running query");
        cancelQueryButton.addActionListener(this);
        cancelQueryButton.setVisible(false);
        progressPanel.add(cancelQueryButton, BorderLayout.WEST);

        queryProgress = new JProgressBar();
        queryProgress.setSize(20, 20);
        queryProgress.setIndeterminate(true);
        queryProgress.setString("Click to cancel query");
        progressPanel.add(queryProgress, BorderLayout.EAST);

        infoPanel.add(progressPanel, BorderLayout.EAST);
        queryProgress.setVisible(false);

//        queryPanel.add(queryAreaLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
//                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 2, 2));
        queryPanel.add(infoPanel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 2, 2));

        //queryArea = new JTextArea("select * from \"10m_admin_0_countries\";");
        // a fixed font is set, for readability
        //queryArea.setFont(new Font("Courier", Font.PLAIN, 12));
        //queryArea.setToolTipText("Hit Ctrl+enter to run query at cursor");

        // changes the listener
        // and a keyboard event handler to execute query at cursor by typing ctrl+enter
        //queryArea.addKeyListener((KeyListener) this);
        //queryArea.addKeyListener((KeyListener) this.plugin);
        DefaultSyntaxKit.initKit();
        queryEditor = new JEditorPane();
        // changes the listener
        // and a keyboard event handler to execute query at cursor by typing ctrl+enter
        queryEditor.addKeyListener((KeyListener) this);
        //queryEditor.addKeyListener((KeyListener) this.plugin);

        JScrollPane queryScrollPane = new JScrollPane(
                queryEditor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        queryScrollPane.setPreferredSize(new Dimension(500, 200));

        queryEditor.setContentType("text/sql");
        queryEditor.setText("select * from \"10m_admin_0_countries\";");

        // the historyPanel: textarea for the moment, grid later
        this.historyArea = new JTextArea();
        historyArea.setEditable(false);
        // Nico hack: sets a nice font.
        historyArea.setFont(new Font("Courier", Font.PLAIN, 12));
        historyArea.setForeground(new Color(0, 82, 231));
        historyArea.setBackground(new Color(237, 237, 237));
        historyArea.setToolTipText("Double click a line to restore the query");
        historyArea.addMouseListener(this);

        JScrollPane histScrollPane = new JScrollPane(
                historyArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        histScrollPane.setPreferredSize(new Dimension(500, 100));

        // a panel with gridlayout to contain both queryArea and historyList
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                queryScrollPane, histScrollPane);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(200);
        //Provide minimum sizes for the two components in the split pane
        Dimension minimumSize = new Dimension(200, 100);
        //queryArea.setMinimumSize(minimumSize);
        queryEditor.setMinimumSize(minimumSize);
        historyArea.setMinimumSize(minimumSize);

        //queryPanel.add(queryScrollPane, new GridBagConstraints(0, 2, 2, 1, 0.5, 0.5, GridBagConstraints.CENTER,
        //        GridBagConstraints.BOTH, new java.awt.Insets(0, 0, 0, 0), 2, 2));
        queryPanel.add(splitPane, new GridBagConstraints(0, 2, 2, 1, 0.5, 0.5, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        // connection info input fields
        jdbcUrlLabel = new JLabel();
        jdbcUrlLabel.setText("  JDBC Url:");
        queryPanel.add(jdbcUrlLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        jdbcUrlField = new JTextField();
        jdbcUrlField.setEnabled(true);
        queryPanel.add(jdbcUrlField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        usernameField = new JTextField();
        usernameField.setEnabled(true);
        queryPanel.add(usernameField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        usernameLabel = new javax.swing.JLabel();
        usernameLabel.setText("  Username:");
        queryPanel.add(usernameLabel, new java.awt.GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        JLabel passwordLabel = null;
        passwordLabel = new javax.swing.JLabel();
        passwordLabel.setText("  Password:");
        queryPanel.add(passwordLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        passwordField = new JPasswordField();
        queryPanel.add(passwordField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        driverField = new JTextField();
        queryClassField = new JTextField();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        runButton = new javax.swing.JButton();
        runButton.setText("Run query");
        runButton.setMnemonic('R');
        runButton.setActionCommand(RUN_ACTION_COMMAND);
        runButton.setToolTipText("Submit query to database (ctrl+enter in query editor)");
        runButton.addActionListener(this);
        buttonPanel.add(runButton);


        refreshButton = new javax.swing.JButton();
        refreshButton.setText("Refreh layer");
        refreshButton.setMnemonic('F');
        refreshButton.setActionCommand(REFRESH_ACTION_COMMAND);
        refreshButton.setToolTipText("Refresh the layer created by the current query (cmd+r in query editor)");
        refreshButton.addActionListener(this);
        buttonPanel.add(refreshButton);

        clearButton = new javax.swing.JButton();
        clearButton.setText("Clear History");
        clearButton.setMnemonic('H');
        clearButton.setActionCommand(CLEAR_ACTION_COMMAND);
        clearButton.setToolTipText("Clear the query history list");
        clearButton.addActionListener(this);
        buttonPanel.add(clearButton);

        cancelButton = new javax.swing.JButton();
        cancelButton.setText("Close");
        cancelButton.setMnemonic('C');
        cancelButton.setActionCommand(CANCEL_ACTION_COMMAND);
        cancelButton.setToolTipText("Submit query to database");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        queryPanel.add(buttonPanel, new java.awt.GridBagConstraints(0, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        this.getContentPane().add(queryPanel, BorderLayout.CENTER);

        pack();
    }

    /**
     * Handle the action when the user clicks on the "OK" or "Cancel" button.
     *
     * @param e an action event
     */
    public void actionPerformed(ActionEvent e) {

        if (CANCEL_ACTION_COMMAND.equals(e.getActionCommand())) {
            setCancelled(true);
            this.setVisible(false);
        } else if (RUN_ACTION_COMMAND.equals(e.getActionCommand())) {
            setCancelled(false);
            try {
                this.plugin.runQuery();
            } catch (Exception ex) {
                // TODO: display exception in OJ, not in the out/err stream
                ex.printStackTrace();
            }
        } else if (CANCEL_QUERY_ACTION_COMMAND.equals(e.getActionCommand())) {
            setCancelled(false);
            try {
                this.plugin.cancelQuery();
            } catch (Exception ex) {
                // TODO: display exception in OJ, not in the out/err stream
                ex.printStackTrace();
            }
        } else if (REFRESH_ACTION_COMMAND.equals(e.getActionCommand())) {
            setCancelled(false);
            try {
                this.plugin.refreshQuery();
            } catch (Exception ex) {
                // TODO: display exception in OJ, not in the out/err stream
                ex.printStackTrace();
            }
        } else if (DB_CHANGED_COMMAND.equals(e.getActionCommand())) {
            JComboBox comboBox = (JComboBox) e.getSource();
            Object comboBoxItem = comboBox.getSelectedItem();

            if (comboBoxItem instanceof DbConnectionParameters) {
                DbConnectionParameters connectionParameters = (DbConnectionParameters) comboBoxItem;
                setDbFields(connectionParameters);
            }
        } else if (CLEAR_ACTION_COMMAND.equals(e.getActionCommand())) {
            clearHistoryList();
            displayHistoryList();
        }
    }

    public void setDbFields(DbConnectionParameters connectionParameters) {
        if (connectionParameters.getJdbcUrl() != null) {
            jdbcUrlField.setText(connectionParameters.getJdbcUrl());
        }

        if (connectionParameters.getUsername() != null) {
            usernameField.setText(connectionParameters.getUsername());
        }

        if (connectionParameters.getPassword() != null) {
            passwordField.setText(connectionParameters.getPassword());
        }

        if (connectionParameters.getDriverClass() != null) {
            driverField.setText(connectionParameters.getDriverClass());
        }

        if (connectionParameters.getClassName() != null) {
            queryClassField.setText(connectionParameters.getClassName());
        }

    }

    public void keyTyped(KeyEvent ke) {
        if ((ke.getKeyChar() == '\n' || ke.getKeyChar() == '\r') && ke.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK) {
            try {
                this.plugin.runQuery();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (ke.getKeyChar() == 'r' && ke.getModifiersEx() == KeyEvent.META_DOWN_MASK) {
            // CTRL+R refreshes the query
            try {
                this.plugin.refreshQuery();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void keyPressed(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }

    /**
     * Adds this query to the queries history list,
     * managing the list size to avoid exceeding historySize,
     * and also checking this query is not already in the list.
     * in which case it does not add the query.
     * @param query
     */
    public void addToHistoryList(String query) {
        for (String s : historyList) {
            if (s.equalsIgnoreCase(query.trim() + ";")) {
                Logger.logInfo("query already in history, skipping...");
                return;
            }
        }
        String val = query.trim();
        if (val.charAt(val.length() - 1) != ';') {
            val += ";";
        }
        historyList.add(0, val);
        displayHistoryList();

    }

    public void displayHistoryList() {
        StringBuilder b = new StringBuilder();

        for (String s : historyList) {
            b.append(s).append("\n");
        }
        this.historyArea.setText(b.toString());
        this.historyArea.setCaretPosition(0);
    }

    public void clearHistoryList() {
        historyList.clear();
    }

    /**
     * Restores the query found at caret position, by parsing the history text.
     * Query will be appended at the end of queryArea. Focus will be given to textarea, with
     * caret positionned at the end of the query, before trailing ";".
     */
    public void restoreQuery() {
        String query = getHistoryAreaQuery();
        if (query.length() > 0) {
            queryEditor.setText(queryEditor.getText().trim() + "\n" + query + (query.contains(";") ? "" : ";"));
            queryEditor.setCaretPosition(queryEditor.getText().length() - 2);
            queryEditor.requestFocus();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // double click => restore query at caret position
            restoreQuery();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Reinits the QueryDialog window before a query is run
     * by showing the progress bar and hiding the db query error icon,
     * also resetting elements tooltips.
     */
    public void initUiBeforeQuery() {
        queryProgress.setVisible(true);
        cancelQueryButton.setVisible(true);
        dbErrorIconLabel.setVisible(false);
        dbErrorIconLabel.setToolTipText("");
        queryAreaLabel.setToolTipText("");
        queryAreaLabel.setText("");
    }

    /**
     * refrehses the QueryDialog window elements to present the given error/warning message:
     * Shows error icon, set elements tooltips
     * @param msg the message to display
     */
    public void refreshUiForError(String msg) {
        queryAreaLabel.setText(msg);
        queryAreaLabel.setToolTipText(msg);
        dbErrorIconLabel.setVisible(true);
        dbErrorIconLabel.setToolTipText(msg);
        queryProgress.setVisible(false);
        cancelQueryButton.setVisible(false);
    }

    /**
     * refrehses the QueryDialog window elements to present the given successful query message:
     * Hides error icon, set elements tooltips, sets queryArea label text
     *
     * @param msg the message to display
     */
    public void refreshUiForResult(String msg) {
        queryAreaLabel.setText(msg);
        queryAreaLabel.setToolTipText(msg);
        dbErrorIconLabel.setVisible(false);
        dbErrorIconLabel.setToolTipText("");
        cancelQueryButton.setVisible(false);
    }
}
