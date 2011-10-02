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
import org.freevoice.jumpdbqueryextension.util.DbConnectionParameters;

import javax.swing.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 *
 */
public class QueryFrame extends JFrame implements ActionListener, KeyListener
{

    private static final String CANCEL_ACTION_COMMAND = "CancelDbQUery";
    private static final String RUN_ACTION_COMMAND = "RunDbQuery";
    private static final String DB_CHANGED_COMMAND = "DbChanged";

    private boolean cancelled = true;

    private JPasswordField passwordField = null;
    private JTextField usernameField = null;
    private JTextArea queryArea = null;
    private JTextField jdbcUrlField = null;
    private JTextField driverField = null;
    private JTextField queryClassField = null;


    private JPanel queryPanel = null;

    private JButton runButton = null;
    private JButton cancelButton = null;
    private JComboBox dbSelectMenu = null;


    private static QueryFrame queryFrame = null;

    private List dbParameterList = null;

    public QueryFrame(Frame frame, String title, List<DbConnectionParameters> connectionParameters, boolean isModal)
    {
        super(title);
        dbParameterList = connectionParameters;
        //NIco: forcing a non cancelled state
        cancelled = false;
        initUICode();
    }

   public static QueryFrame showDialog(Frame parentFrame, String title, List<DbConnectionParameters> connectionParameters)
   {

    if(queryFrame == null)
    {
      queryFrame = new QueryFrame(parentFrame, title, connectionParameters, true);
    }

    queryFrame.setVisible(true);
    return queryFrame;
  }


    public String getPassword()
    {
        return new String(passwordField.getPassword());
    }

    public String getUsername()
    {
        return usernameField.getText();
    }

    public void setUsername(JTextField username)
    {
        this.usernameField = username;
    }

    public String getDriver()
    {
        return driverField.getText();
    }

    public void setDriver(JTextField driver)
    {
        this.driverField = driver;
    }

    public String getQueryClass()
    {
        return queryClassField.getText();
    }

    public void setQueryClass(JTextField queryClass)
    {
        this.queryClassField = queryClass;
    }

    public String getQuery()
    {
        return queryArea.getText();
    }


    public String getJdbcUrl()
    {
        return jdbcUrlField.getText();
    }

    private void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }



    private void initUICode() {

       JLabel jdbcUrlLabel = null;
       JLabel usernameLabel = null;

        this.getContentPane().setLayout(new BorderLayout());

        queryPanel = new JPanel();
        queryPanel.setLayout(new GridBagLayout());


        if(dbParameterList.size() > 0) {
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
        dbSelectMenu.addActionListener(this);
        queryPanel.add(dbSelectMenu, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));



        JLabel queryAreaLabel = new JLabel("Enter query:");

        queryPanel.add(queryAreaLabel, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 2, 2));

        queryArea = new JTextArea();
        // Nico hack: sets a nice font.
        queryArea.setFont(new Font("Courier", Font.PLAIN, 12));
        // and a keyboard event handler to execute query by typing ctrl+enter
        queryArea.addKeyListener((KeyListener) this);

        JScrollPane queryScrollPane = new JScrollPane(queryArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        queryScrollPane.setPreferredSize(new Dimension(500, 400));
        queryPanel.add(queryScrollPane, new GridBagConstraints(0, 2, 2, 1, 0.5, 0.5 ,GridBagConstraints.CENTER,
													 GridBagConstraints.BOTH, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        jdbcUrlLabel = new JLabel();
        jdbcUrlLabel.setText("JDBC Url:");
        queryPanel.add(jdbcUrlLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
													 GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        jdbcUrlField = new JTextField();
        jdbcUrlField.setEnabled(true);
        queryPanel.add(jdbcUrlField, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        usernameField = new JTextField();
        usernameField.setEnabled(true);
        queryPanel.add(usernameField, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
													 GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        usernameLabel = new javax.swing.JLabel();
        usernameLabel.setText("Username:");
        queryPanel.add(usernameLabel, new java.awt.GridBagConstraints(0, 4, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
                GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));

        JLabel passwordLabel = null;
        passwordLabel = new javax.swing.JLabel();
        passwordLabel.setText("Password:");
        queryPanel.add(passwordLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
													 GridBagConstraints.NONE, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        passwordField = new JPasswordField();
        queryPanel.add(passwordField, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0 ,GridBagConstraints.WEST,
													 GridBagConstraints.HORIZONTAL, new java.awt.Insets(0, 0, 0, 0), 2, 2));


        driverField = new JTextField();
        queryClassField = new JTextField();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        runButton = new javax.swing.JButton();
        runButton.setText("OK");
        runButton.setMnemonic('O');
        runButton.setActionCommand(RUN_ACTION_COMMAND);
        runButton.setToolTipText("Submit query to database");
        runButton.addActionListener(this);
        buttonPanel.add(runButton, BorderLayout.WEST);


        cancelButton = new javax.swing.JButton();
        cancelButton.setText("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.setActionCommand(CANCEL_ACTION_COMMAND);
        cancelButton.setToolTipText("Submit query to database");
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton, BorderLayout.EAST);


        queryPanel.add(buttonPanel, new java.awt.GridBagConstraints(0, 6, 2, 1, 0.0, 0.0 ,GridBagConstraints.CENTER,
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
      // Nico: no more hidden now window is no more modal
     //this.setVisible(false);
    } else if (DB_CHANGED_COMMAND.equals(e.getActionCommand())) {
      JComboBox comboBox = (JComboBox) e.getSource();
      Object comboBoxItem = comboBox.getSelectedItem();

      if(comboBoxItem instanceof DbConnectionParameters)
      {
          DbConnectionParameters connectionParameters = (DbConnectionParameters) comboBoxItem;
          setDbFields(connectionParameters);
      }

    }

   }

   private void setDbFields(DbConnectionParameters connectionParameters)
   {
       if(connectionParameters.getJdbcUrl() != null)
       {
          jdbcUrlField.setText(connectionParameters.getJdbcUrl());
       }

       if(connectionParameters.getUsername() != null)
       {
         usernameField.setText(connectionParameters.getUsername());
       }

      if(connectionParameters.getPassword() != null)
      {
         passwordField.setText(connectionParameters.getPassword());
      }

       if(connectionParameters.getDriverClass() != null)
       {
           driverField.setText(connectionParameters.getDriverClass());
       }

       if(connectionParameters.getClassName() != null)
       {
           queryClassField.setText(connectionParameters.getClassName());
       }

   }

    public void keyTyped(KeyEvent ke) {
        if ( (ke.getKeyChar() == '\n' || ke.getKeyChar() == '\r') && ke.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK ) {
            ActionEvent e = new ActionEvent(queryArea, 0,QueryFrame.RUN_ACTION_COMMAND);
            System.out.println("sending event: run action...");
            this.actionPerformed(e);
        }
    }

    public void keyPressed(KeyEvent ke) {
    }

    public void keyReleased(KeyEvent ke) {
    }


}
