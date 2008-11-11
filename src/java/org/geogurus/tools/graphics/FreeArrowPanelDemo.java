package org.geogurus.tools.graphics;

/*
 * ImagePanel.java
 *
 * Created on 11 mars 2002, 10:12
 */
import java.awt.Color;
import java.awt.Point;
/**
 * This class displays an rotrans an arrow
 * @author Bastien VIALADE  
 */
public class FreeArrowPanelDemo extends java.awt.Panel {
        
    private FreeArrowCanvas freeArrowCanvas ;
    private double mult=1;

    public FreeArrowPanelDemo() {
        initComponents();
        freeArrowCanvas = new FreeArrowCanvas(new Point(200,200),mult,Color.blue,true,10,Color.red,500,500);
        add(freeArrowCanvas);
    } 

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        panel1 = new java.awt.Panel();
        panel2 = new java.awt.Panel();
        panel3 = new java.awt.Panel();
        button1 = new java.awt.Button();
        button2 = new java.awt.Button();
        button3 = new java.awt.Button();
        button4 = new java.awt.Button();
        button5 = new java.awt.Button();
        panel4 = new java.awt.Panel();
        textField1 = new java.awt.TextField();

        setLayout(new java.awt.BorderLayout());

        setBackground(java.awt.Color.cyan);
        add(panel1, java.awt.BorderLayout.CENTER);

        panel2.setLayout(new java.awt.BorderLayout());

        button1.setLabel("Display/Hide Grid");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                display(evt);
            }
        });

        panel3.add(button1);

        button2.setLabel("Angle");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getAngle(evt);
            }
        });

        panel3.add(button2);

        button3.setLabel("Position");
        button3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getPosition(evt);
            }
        });

        panel3.add(button3);

        button4.setLabel("+");
        button4.setName("null");
        button4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                increase(evt);
            }
        });

        panel3.add(button4);

        button5.setLabel("-");
        button5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decrease(evt);
            }
        });

        panel3.add(button5);

        panel2.add(panel3, java.awt.BorderLayout.CENTER);

        textField1.setColumns(10);
        panel4.add(textField1);

        panel2.add(panel4, java.awt.BorderLayout.SOUTH);

        add(panel2, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void increase(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_increase
        freeArrowCanvas.setMultiplicityFactor(++mult);
    }//GEN-LAST:event_increase

    private void decrease(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decrease
        if (mult<=2) mult=(mult/2); else mult--;
        freeArrowCanvas.setMultiplicityFactor(mult);
    }//GEN-LAST:event_decrease

    private void getPosition(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getPosition
      Point p = freeArrowCanvas.getPosition();
        textField1.setText("("+p.x+","+p.y+")");
    }//GEN-LAST:event_getPosition

    private void getAngle(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getAngle
      textField1.setText(freeArrowCanvas.getCurrentAngle()+" degrees");
      }//GEN-LAST:event_getAngle

    private void display(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_display
      boolean isVisible = freeArrowCanvas.isGridDisplayed();
        freeArrowCanvas.setGridVisible(!isVisible);
      }//GEN-LAST:event_display
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Button button1;
    private java.awt.Panel panel4;
    private java.awt.Panel panel3;
    private java.awt.Panel panel2;
    private java.awt.Panel panel1;
    private java.awt.TextField textField1;
    private java.awt.Button button5;
    private java.awt.Button button4;
    private java.awt.Button button3;
    private java.awt.Button button2;
    // End of variables declaration//GEN-END:variables
    
}

