package org.kaboum;

/*
 *
 * Main class for the reference map into the client browser
 *
 * Copyright (C) 2000 Nicolas Ribot, directly and mainly inspired from
 * Jrom Gasperi's work on Kaboum Applet
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import netscape.javascript.*;

import org.kaboum.util.KaboumUtil;
import org.kaboum.util.KaboumExtent;
import org.kaboum.util.KaboumMapServerTools;
import org.kaboum.util.KaboumPrecisionModel;
import org.kaboum.util.KaboumCoordinate;


/**
 *
 * Kaboumette: kaboum little sister to display a navigable
 * reference map
 *
 * @author Nicol@s Ribot
 *
 */
public class Kaboumette extends Applet implements MouseListener, MouseMotionListener {
    /** Current version */
    protected final String version = "Kaboumette v1.2";
    
    /** Window applet size */
    public Dimension screenSize;
    
    /** Double buffering (Image) */
    public Image offScreenImage;
    
    /** Double buffering (Graphic) */
    public Graphics offScreenGraphics;
    
    /** Applet graphical context */
    private Graphics g;
    
    /** Foreground color (i.e. color of all opModes) */
    private Color fgColor = Color.red;
    
    /** Liveconnect activated  */
    public boolean javascriptEnable = false;
    
    /** reference Image image */
    public Image ref;
    
    /** Reference image URL*/
    public String refURL;
    
    /** Cursor type */
    public String cursorValue = "MOVE";
    
    /** Browser window reference */
    public JSObject window;
    
    /** The extent box*/
    public Rectangle box;
    public boolean nodrag = false;
    
    /** mouse position during user interaction*/
    protected int shiftX, shiftY;
    
    /** to transform map to pix coordinates*/
    public KaboumMapServerTools tool = null;
    
    /** busy image*/
    protected boolean busy = false;

    private KaboumPrecisionModel pm;
    
    
    /**
     *
     * Applet initialisation.
     * Get the paramater from input html.
     *
     */
    public void init() {
        
        int red = 0;
        int green = 0;
        int blue = 0;
        String inputAppletParameter = null;
        StringTokenizer st;
        
        screenSize = this.getSize();
        box = new Rectangle(0, 0, 0, 0);
        // Double buffering: evite le flickering
        offScreenImage = createImage(screenSize.width, screenSize.height);
        offScreenGraphics = offScreenImage.getGraphics();
        
        // relative or absolute path to the background image:
        // reference image on which the box will be drawn
        refURL = getParameter("REF_URL");
        
        // Foreground color used by opMode
        inputAppletParameter = getParameter("KABOUM_FOREGROUND_COLOR");
        if (inputAppletParameter != null) {
            st = new StringTokenizer(inputAppletParameter, ",");
            if (st.countTokens() == 3) {
                red = atoi(st.nextToken());
                green = atoi(st.nextToken());
                blue = atoi(st.nextToken());
                fgColor = new Color(red, green, blue);
            }
        }
        
        KaboumExtent referenceExtent = new KaboumExtent();
        
        inputAppletParameter = getParameter("MAPFILE_EXTENT");
        if (inputAppletParameter != null) {
            st = new StringTokenizer(inputAppletParameter, ",");
            if (st.countTokens() != 4) { KaboumUtil.debug("Wrong Extent!!! Not enough parameter in Extent"); }
            KaboumCoordinate coordLL = new KaboumCoordinate(KaboumUtil.stod(st.nextToken()), KaboumUtil.stod(st.nextToken()));
            KaboumCoordinate coordUR = new KaboumCoordinate(KaboumUtil.stod(st.nextToken()), KaboumUtil.stod(st.nextToken()));
            
            this.pm = new KaboumPrecisionModel(
                    KaboumPrecisionModel.sToUnit(getParameter("MAPFILE_UNITS")));

            referenceExtent = new KaboumExtent(this.pm.toInternal(coordLL), this.pm.toInternal(coordUR));
            
            // Most of the parameters are bollocks since KaboumMapServerTools is
            // used only for the coordinates transformation between map and image
            tool = new KaboumMapServerTools(getParameter("KABOUM_MAPSERVER_CGI_URL"),

            getParameter("MAPFILE_PATH"),
            0,
            this.pm,
            "",
            "",
            null,
            -1,
            false,
            referenceExtent,
            this.screenSize,
            null
            );
            
        }
        
        // Vrai : LiveConnect actif (communication JAVA ----> JAVASCRIPT))
        inputAppletParameter = getParameter("KABOUM_USE_LIVECONNECT");
        if (inputAppletParameter != null) {
            if (inputAppletParameter.equals("TRUE")) { javascriptEnable = true; }
        }
        
        // Get current graphical context
        g = this.getGraphics();
        
        // LiveConnect actif : recupere la fenetre du navigateur
        if (javascriptEnable) {
            try {
                window = JSObject.getWindow(this);
            } catch (Exception je) {je.printStackTrace();}
        }
        
        // Initialisation
        setLayout(null);
        
        // Premiere carte a afficher
        swapImage(toURL(refURL));
        
        addMouseListener(this);
        addMouseMotionListener(this);
        
        // Version
        System.out.println(version);
    }
    
    
    //******************* JAVA ----> HTML ******************************
    
    /**
     *
     * This method sends result to the HTML page via
     * LiveConnect (cf:javascript).
     * It's called by opMode with an extra-applet
     * effect (like QUERY for example).
     * It assumed that the javascript method "kaboumResult"
     * exist in the parent html page code.
     *
     * @param str Submitted command string
     *
     */
    public boolean kaboumResult(String str) {
        if (javascriptEnable) {
            try {
                KaboumUtil.debug("kaboumette.kaboumResult, sending command to JS: " + str);
                window.eval("kaboumResult('"+str+"')");
            }
            catch ( Exception jse) { jse.printStackTrace(); }
        }
        else { return false; }
        
        return true;
    }
    
    
    //******************* HTML ----> JAVA ******************************
    
    /**
     * The only supported command is currently: REFERENCE|x1,y1;x2,y2
     *
     * @param str Submited command string
     *
     */
    
    public boolean kaboumCommand(String str) {
        if (busy) return false;
        
        StringTokenizer st = new StringTokenizer(str, "|");
        // command control:
        if (st.countTokens() != 2) {
            // invalid command
            KaboumUtil.debug("invalid kaboumette command: " + str);
            return false;
        }
        String key = st.nextToken();
        String value = st.nextToken();
        StringTokenizer stCouple = new StringTokenizer(value, ";");
        
        // command control:
        if (stCouple.countTokens() != 2) {
            // invalid command: bad coordinates
            KaboumUtil.debug("invalid kaboumette command: " + str);
            return false;
        }

        StringTokenizer stCoord;
        stCoord = new StringTokenizer(stCouple.nextToken(), ",");
        double x1 = atod(stCoord.nextToken());
        double y1 = atod(stCoord.nextToken());
        stCoord = new StringTokenizer(stCouple.nextToken(), ",");
        double x2 = atod(stCoord.nextToken());
        double y2 = atod(stCoord.nextToken());
        
        // can instantiate the mapserverTool to transform coordinates
        //can instantiate the new box and paint it. Force mini size for the box
        int w = tool.internalToMouseX(x2) - tool.internalToMouseX(x1) < 10 ? 10 :
            tool.internalToMouseX(x2) - tool.internalToMouseX(x1);
        int h = tool.internalToMouseY(y1) - tool.internalToMouseY(y2) < 10 ? 10 :
                tool.internalToMouseY(y1) - tool.internalToMouseY(y2);
                
        box.setBounds(tool.internalToMouseX(x1), tool.internalToMouseY(y2), w, h);
        update(g);
        return true;
    }
    
    /**
     *
     * Convert numerical string into integer
     *
     * @param s Input string
     *
     */
    public static int atoi(String s) {
        
        int n = 0;
        
        try {
            n = new Integer(s.trim()).intValue();
        } catch(NumberFormatException e) {}
        return(n);
        
    }
    
    
    /**
     *
     * Convert numerical string into double
     *
     * @param s Input string
     *
     */
    public static double atod(String s) {
        
        double n = 0;
        
        try {
            n = new Double(s.trim()).doubleValue();
        } catch(NumberFormatException e) {}
        
        return(n);
    }
    
    
    /**
     *
     * Convert string into URL
     *
     * @param strName Input string
     *
     */
    public static URL toURL(String strName) {
        URL strURL = null;
        
        try {
            strURL = new URL(strName);
        } catch(MalformedURLException e) {
            KaboumUtil.debug(e.getMessage());
        }
        
        return strURL;
    }
    
    /**
     *
     * Change the map image.
     *
     * @param imageURL Image URL
     *
     */
    public void swapImage(URL imgURL) {
        // Get the current pointer shape
        String tmpCursor = cursorValue;
        
        // Set the wait shape for the mouse pointer
        setCursor("WAIT");
        this.busy = true;
        
        // REPAINT
        update(g);
        
        MediaTracker tracker = new MediaTracker(this);
        this.ref = Toolkit.getDefaultToolkit().getImage(imgURL);
        tracker.addImage(ref, 0, 300, 300);
        
        try {
            tracker.waitForID(0);
        }
        catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            return;
        }
        if (ref == null)
            System.err.println("Error in reading "+imgURL);
        
        busy = false;
        // ...and set the old mouse pointer back
        setCursor(tmpCursor);
        repaint();
    }
    
    /**
     *
     * paint
     *
     */
    public void paint(Graphics g) {
        
        int tmpX = 0;
        int tmpY = 0;
        
        // draw the image at the middle of the applet, not at the upper-left corner
        if(ref != null) {
            tmpX = screenSize.width/2 - ref.getWidth(this)/2;
            tmpY = screenSize.height/2 - ref.getHeight(this)/2;
            offScreenGraphics.drawImage(ref,
            tmpX,
            tmpY,
            this);
        }
        
        // draw the box
        if (box != null) {
            offScreenGraphics.setColor(fgColor);
            offScreenGraphics.drawRect(box.x, box.y, box.width, box.height);
        }
        // Draw the image onto screen
        g.drawImage(offScreenImage, tmpX, tmpY, this);
        
        // Clear the buffer
        offScreenGraphics.clearRect(tmpX,tmpY,screenSize.width,screenSize.height);
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    /**
     *
     * Set the mouse pointer shape.
     * Possible values are: DEFAULT, CROSSHAIR, WAIT, MOVE.
     *
     * @param s Value
     *
     */
    public void setCursor(String s) {
        this.cursorValue = s;
        if (s.equals("DEFAULT"))
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        else if (s.equals("MOVE"))
            this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
        else if (s.equals("WAIT"))
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        else
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    //************************** MOUSE EVENTS *******************************
    //
    // Mouse event handlers
    //
    public void mouseClicked(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }
    
    public void mouseMoved(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
        nodrag = !box.contains(e.getX(), e.getY());
        
        if (!nodrag) {
            shiftX = e.getX() - box.x;
            shiftY = e.getY() - box.y;
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (!nodrag) {
            box.setLocation(e.getX()-shiftX, e.getY()-shiftY);
            update(g);
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        try {
            KaboumUtil.debug("kaboumette.mouseReleased called");
            update(g);
            // send the command to the client
            String s = "HISTORY|MAP|";
            if (tool != null && !nodrag) {
                double tmpX = (tool.mouseXToInternal(box.x + box.width) + tool.mouseXToInternal(box.x)) / 2;
                double tmpY = (tool.mouseYToInternal(box.y + box.height) + tool.mouseYToInternal(box.y)) / 2;
                KaboumCoordinate external = this.pm.toExternal(tmpX, tmpY);
                s += external.x + "," + external.y;
                this.kaboumResult(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
} // End Kaboumette

