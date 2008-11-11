/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geogurus.gas.print;

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
        testFromString();
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
}
