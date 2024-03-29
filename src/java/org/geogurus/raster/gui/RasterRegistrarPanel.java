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


 * RasterRegistrarPanel.java


 *


 * Created on 6 mars 2002, 16:57


 */





package org.geogurus.raster.gui;


import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.geogurus.raster.Attribute;
import org.geogurus.raster.RasterRegistrar;





/**


 * Displays and modifies a RasterRegistrar Object


 * @author Bastien VIALADE


 */





public class RasterRegistrarPanel extends javax.swing.JPanel {


    private JFileChooser fileChooser ;


    private ResourceBundle res ;


    private ArrayList attributes;


    private ArrayList textFields;


    


    /** Creates new form RasterRegistrarPanel.


     * Inits all needed component


     */


    public RasterRegistrarPanel() {


        res = ResourceBundle.getBundle("resources/RasterRegistrarPanel") ;


        initComponents();


        fileChooser = new JFileChooser() ;


        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        textFields = new ArrayList();


    }


    


    /** This method is called from within the constructor to


     * initialize the form.


     * WARNING: Do NOT modify this code. The content of this method is


     * always regenerated by the Form Editor.


     */


    private void initComponents() {//GEN-BEGIN:initComponents


        jPanel3 = new javax.swing.JPanel();


        jPanel2 = new javax.swing.JPanel();


        jLabel2 = new javax.swing.JLabel();


        jLabel3 = new javax.swing.JLabel();


        jLabel4 = new javax.swing.JLabel();


        jLabel5 = new javax.swing.JLabel();


        jLabel6 = new javax.swing.JLabel();


        jPanel5 = new javax.swing.JPanel();


        jTextField1 = new javax.swing.JTextField();


        jTextField2 = new javax.swing.JTextField();


        jTextField3 = new javax.swing.JTextField();


        jTextField5 = new javax.swing.JTextField();


        jPasswordField1 = new javax.swing.JPasswordField();


        jPanel4 = new javax.swing.JPanel();


        jPanel6 = new javax.swing.JPanel();


        jPanel7 = new javax.swing.JPanel();


        jPanel1 = new javax.swing.JPanel();


        jPanel9 = new javax.swing.JPanel();


        jLabel1 = new javax.swing.JLabel();


        jPanel8 = new javax.swing.JPanel();


        jTextField4 = new javax.swing.JTextField();


        jButton1 = new javax.swing.JButton();





        setLayout(new java.awt.BorderLayout(10, 10));





        jPanel3.setLayout(new java.awt.BorderLayout(5, 5));





        jPanel3.setBorder(new javax.swing.border.TitledBorder("DataBase"));


        jPanel2.setLayout(new java.awt.GridLayout(5, 2, 5, 5));





        jLabel2.setText(res.getString("Host_:"));


        jPanel2.add(jLabel2);





        jLabel3.setText(res.getString("Port_:"));


        jPanel2.add(jLabel3);





        jLabel4.setText(res.getString("DB_Name_:"));


        jPanel2.add(jLabel4);





        jLabel5.setText("Login:");


        jPanel2.add(jLabel5);





        jLabel6.setText("Password:");


        jPanel2.add(jLabel6);





        jPanel3.add(jPanel2, java.awt.BorderLayout.WEST);





        jPanel5.setLayout(new java.awt.GridLayout(5, 1, 5, 5));





        jTextField1.setColumns(10);


        jTextField1.setToolTipText(res.getString("hostToolTip"));


        jPanel5.add(jTextField1);





        jTextField2.setToolTipText(res.getString("portToolTip"));


        jPanel5.add(jTextField2);





        jTextField3.setToolTipText(res.getString("nameDBToolTip"));


        jPanel5.add(jTextField3);





        jPanel5.add(jTextField5);





        jPanel5.add(jPasswordField1);





        jPanel3.add(jPanel5, java.awt.BorderLayout.CENTER);





        add(jPanel3, java.awt.BorderLayout.NORTH);





        jPanel4.setLayout(new java.awt.BorderLayout(5, 5));





        jPanel4.setBorder(new javax.swing.border.TitledBorder("Rasters"));


        jPanel6.setLayout(new java.awt.GridLayout(1, 2, 5, 5));





        jPanel4.add(jPanel6, java.awt.BorderLayout.CENTER);





        jPanel7.setLayout(new java.awt.GridLayout(3, 1, 2, 2));





        jPanel4.add(jPanel7, java.awt.BorderLayout.EAST);





        jPanel1.setLayout(new java.awt.GridLayout(3, 1, 5, 5));





        jPanel4.add(jPanel1, java.awt.BorderLayout.WEST);





        jPanel9.setLayout(new java.awt.GridLayout());





        jLabel1.setText("Path : ");


        jPanel9.add(jLabel1);





        jTextField4.setColumns(15);


        jTextField4.setToolTipText(res.getString("pathToolTip"));


        jPanel8.add(jTextField4);





        jButton1.setText("...");


        jButton1.addActionListener(new java.awt.event.ActionListener() {


            public void actionPerformed(java.awt.event.ActionEvent evt) {


                chooseFolder(evt);


            }


        });





        jPanel8.add(jButton1);





        jPanel9.add(jPanel8);





        jPanel4.add(jPanel9, java.awt.BorderLayout.NORTH);





        add(jPanel4, java.awt.BorderLayout.CENTER);





    }//GEN-END:initComponents


    


    private void chooseFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseFolder


        int res = fileChooser.showOpenDialog(this) ;


        if (res == JFileChooser.APPROVE_OPTION) {


            File f = fileChooser.getSelectedFile() ;


            jTextField4.setText(f.getPath()) ;


        }


    }//GEN-LAST:event_chooseFolder


    


    /** Displays the given RasterRegistrar


     * @param rg RasterRegistrar to display


     */


    public void setData(RasterRegistrar rg) {


        jTextField1.setText(rg.getDbhost()) ;


        jTextField2.setText(Integer.toString(rg.getDbport())) ;


        jTextField3.setText(rg.getDbname()) ;


        jTextField4.setText(rg.getPath()) ;


        jTextField5.setText(rg.getLogin());


        jPasswordField1.setText(rg.getPassword());


        fileChooser.setCurrentDirectory(new File(rg.getPath())) ;


        attributes = rg.getAttributes();


        // Inits attributes textFields


        JTextField tf;


        JLabel label;


        jPanel6.setLayout(new java.awt.GridLayout(attributes.size(), 2, 5, 5));


        for (int i=0; i<attributes.size(); i++) {


            Attribute att = (Attribute)attributes.get(i);


            if (!(att.type.equalsIgnoreCase("WIDTH"))&&!(att.type.equalsIgnoreCase("WIDTH"))) {


                label = new JLabel(att.label);


                jPanel6.add(label);


                tf = new JTextField(att.value);


                jPanel6.add(tf);


                textFields.add(tf);


            }


        }


    }


    


    /** Gets the RasterRegistra Object with displayed parameters values


     * @param rg RasterRegistra to update


     * @return RasterRegistra updated with displayed values


     */


    public RasterRegistrar getData(RasterRegistrar rg) {


        rg.setDbhost(jTextField1.getText()) ;


        rg.setDbport(Integer.parseInt(jTextField2.getText())) ;


        rg.setDbname(jTextField3.getText()) ;


        rg.setPath(jTextField4.getText()) ;


        rg.setLogin(jTextField5.getText());


        rg.setPassword(jPasswordField1.getPassword().toString());


        int j=0;


        for (int i=0; i<attributes.size(); i++) {


            Attribute att = (Attribute)attributes.get(i);


            if (!(att.type.equalsIgnoreCase("WIDTH"))&&!(att.type.equalsIgnoreCase("WIDTH"))) {


                JTextField jtf = (JTextField)textFields.get(j++);


                att.value = jtf.getText();


            }


        }


        rg.setAttributes(attributes);


        return rg ;


        


    }


    





    // Variables declaration - do not modify//GEN-BEGIN:variables


    private javax.swing.JPanel jPanel9;


    private javax.swing.JPanel jPanel8;


    private javax.swing.JPanel jPanel7;


    private javax.swing.JPasswordField jPasswordField1;


    private javax.swing.JPanel jPanel6;


    private javax.swing.JPanel jPanel5;


    private javax.swing.JPanel jPanel4;


    private javax.swing.JPanel jPanel3;


    private javax.swing.JPanel jPanel2;


    private javax.swing.JPanel jPanel1;


    private javax.swing.JButton jButton1;


    private javax.swing.JTextField jTextField5;


    private javax.swing.JTextField jTextField4;


    private javax.swing.JTextField jTextField3;


    private javax.swing.JTextField jTextField2;


    private javax.swing.JTextField jTextField1;


    private javax.swing.JLabel jLabel6;


    private javax.swing.JLabel jLabel5;


    private javax.swing.JLabel jLabel4;


    private javax.swing.JLabel jLabel3;


    private javax.swing.JLabel jLabel2;


    private javax.swing.JLabel jLabel1;


    // End of variables declaration//GEN-END:variables


   


}


