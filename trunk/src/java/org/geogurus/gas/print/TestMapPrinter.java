/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.print;

import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONWriter;
import org.mapfish.print.MapPrinter;

/**
 *
 * @author Gretti
 */
public class TestMapPrinter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //testFromFile();
        //testFromInputStream();
        //testFromString();
        testPageSizes();
    }

    public static void testFromFile() {
        try {
            File configFile = new File("D:\\Dev\\geoadminsuite\\web\\config.yaml");
            MapPrinter printer = new MapPrinter(configFile);
            final OutputStreamWriter writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
            JSONWriter json = new JSONWriter(writer);
            json.object();
            {
                printer.printClientConfig(json);
            }
            json.endObject();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void testFromInputStream() {
        try {
            File configFile = new File("D:\\Dev\\geoadminsuite\\web\\config.yaml");
            FileInputStream fis = new FileInputStream(configFile);
            MapPrinter printer = new MapPrinter(fis, ".");
            final OutputStreamWriter writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
            JSONWriter json = new JSONWriter(writer);
            json.object();
            {
                printer.printClientConfig(json);
            }
            json.endObject();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void testFromString() {
        try {
            File configFile = new File("D:\\Dev\\geoadminsuite\\web\\config.yaml");
            FileInputStream fstream = new FileInputStream(configFile);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            StringBuilder yaml = new StringBuilder();
            while ((strLine = br.readLine()) != null) {
                yaml.append(strLine + "\n");
            }
            in.close();
            System.out.println(yaml.toString());
            MapPrinter printer = new MapPrinter(yaml.toString(), ".");
            final OutputStreamWriter writer = new OutputStreamWriter(System.out, Charset.forName("UTF-8"));
            JSONWriter json = new JSONWriter(writer);
            json.object();
            {
                printer.printClientConfig(json);
            }
            json.endObject();
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(TestMapPrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void testPageSizes() {
        String[] formats = {"A0","A1","A2","A3","A4"};
        Rectangle r;
        Dimension dimCalcContainer = new Dimension();
        int hMargins = 40;
        int vMargins = 40;
        Dimension dimLayout = new Dimension();
        //LANDSCAPE
        System.out.println("LANDSCAPE");
        dimLayout.setSize(798.0, 562.0);
        for (String format : formats) {
            r = PageSize.getRectangle(format).rotate();
            
            //margins ratio
            int hMarginSize = Math.round(hMargins/r.getWidth()* dimLayout.width);
            int vMarginSize = Math.round(vMargins/r.getHeight()* dimLayout.height);
            dimCalcContainer.setSize(dimLayout.width - hMarginSize, dimLayout.height - vMarginSize);
            System.out.println(format + " : {" + 
                    "hmargin:" + hMarginSize + 
                    ",vmargin:" + vMarginSize + 
                    ",width:" + dimCalcContainer.width + 
                    ",height:" + dimCalcContainer.height +
                    "},");
        }
        //PAGE
        System.out.println("PAGE");
        dimLayout.setSize(493.0, 696.0);
        for (String format : formats) {
            r = PageSize.getRectangle(format);
            //margins ratio
            int hMarginSize = Math.round(hMargins/r.getWidth()* dimLayout.width);
            int vMarginSize = Math.round(vMargins/r.getHeight()* dimLayout.height);
            dimCalcContainer.setSize(dimLayout.width - hMarginSize, dimLayout.height - vMarginSize);
            System.out.println(format + " : {" + 
                    "hmargin:" + hMarginSize + 
                    ",vmargin:" + vMarginSize + 
                    ",width:" + dimCalcContainer.width + 
                    ",height:" + dimCalcContainer.height +
                    "},");
        }
    }
}
