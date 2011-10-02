/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freevoice.jumpdbqueryextension.ui;
import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import jsyntaxpane.DefaultSyntaxKit;
import jsyntaxpane.SyntaxTester;

/**
 *
 * @author nicolas
 */
public class TestLexer {
    public TestLexer() {
        JFrame f = new JFrame(TestLexer.class.getName());
        final Container c = f.getContentPane();
        c.setLayout(new BorderLayout());

        DefaultSyntaxKit.initKit();

        final JEditorPane codeEditor = new JEditorPane();
        JScrollPane scrPane = new JScrollPane(codeEditor);
        c.add(scrPane, BorderLayout.CENTER);
        c.doLayout();
        codeEditor.setContentType("text/sql");
        codeEditor.setText("select * from matable where id = '23' group by id, order by id DESC;");

        f.setSize(800, 600);
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
    public static void main(String[] args) {
        TestLexer t = new TestLexer();
    }
}