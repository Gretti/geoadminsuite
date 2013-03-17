/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication4;

import java.io.File;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

/**
 *
 * @author nicolas
 */
public class HillshadeCreator {
    
    public File inputDem = null;
    public File inputRamp = null;
    public File outputFolder = null;

    public HillshadeCreator(File dem, File ramp, File out) {
        this.inputDem = dem;
        this.inputRamp = ramp;
        this.outputFolder = out;
    }
    
    
    public int createHillshade() throws Exception {
        File script = new File (getClass().getResource("/create_hillshade.sh").toURI());
        CommandLine cmdLine = new CommandLine(script);
        cmdLine.addArgument(inputDem.getAbsolutePath());
        cmdLine.addArgument(inputRamp.getAbsolutePath());
        cmdLine.addArgument(outputFolder.getAbsolutePath());
        DefaultExecutor executor = new DefaultExecutor();
        
        System.out.println("running: " + cmdLine.toString());
        
        return executor.execute(cmdLine);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        File dem = new File("/Users/nicolas/tmp/toto/N36W117.hgt");
        File ramp = new File("/Users/nicolas/tmp/toto/ramp4.txt");
        File out = new File("/tmp");
        HillshadeCreator j = new HillshadeCreator(dem, ramp, out);
        
        long t0 = System.currentTimeMillis();
        int res = j.createHillshade();
        long t1 = System.currentTimeMillis();
        System.out.println("Done: exit: " + res + " in " + (t1-t0) + " ms.");
    }
}
