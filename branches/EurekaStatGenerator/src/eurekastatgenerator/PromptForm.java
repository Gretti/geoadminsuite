package eurekastatgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import javax.swing.JDialog;

public class PromptForm extends JDialog {
    private EurekaStatGenerator gen = null;

    private transient char[] result;

    public char[] getResult() {
        return result;
    }

    public PromptForm(Frame parent) {
        super(parent, true);
        initComponents();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
    }

    public void prompt() {
        this.setVisible(true);
    }

    public static char[] promptForPassword(Frame parent) {
        PromptForm pf = new PromptForm(parent);
        pf.prompt();
        return pf.getResult();
    }
    
    public static void setMsg(String msg) {
        
    }

    private void jPasswordField1ActionPerformed(java.awt.event.ActionEvent evt) {                                                
        // TODO add your handling code here:
        result = jPasswordField1.getPassword();
        setVisible(false);
        dispose();
    }     

    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField1.setPreferredSize(new Dimension(400, 30));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        BorderLayout l = new BorderLayout();
        this.setLayout(l);

        jLabel1.setLabelFor(jPasswordField1);
        jLabel1.setText("Enter password for database:");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setPreferredSize(new Dimension(400, 200));
        
        this.add(jLabel1, BorderLayout.PAGE_START);
        this.add(jPasswordField1, BorderLayout.PAGE_END);

        jPasswordField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordField1ActionPerformed(evt);
            }
        });
        pack();
    }
    
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
}