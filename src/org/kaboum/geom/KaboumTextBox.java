/*
 * KaboumTextBox.java
 *
 * Copyright (C) 2000-2003 Jerome Gasperi
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *

 */

package org.kaboum.geom;

import java.util.StringTokenizer;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;
import java.io.Serializable;

import org.kaboum.Kaboum;
import org.kaboum.util.KaboumUtil;


/**
 *
 * This class is inspired from the "area" class of
 * the mapplet_xml code from the mapserver client
 * support package (cf. http://mapserver.gis.um.edu)
 *
 * @author  jrom
 *
 */
public class KaboumTextBox implements Serializable {
    
    // CONSTANTS
    private final String BREAKLINE = "\n";
    
    /** Kaboum reference */
    private Kaboum parent;
    
    /** Screensize rectangle */
    private Rectangle rect;
    
    /** Maximum number of lines allowed */
    private int maxLines = 20;
    
    /** Array of text. Each row = a line of text */
    private String text[] = new String[20];
    
    private int numlines = 0;
    private int boxLeft = 0;
    private int boxTop = 0;
    private int boxWidth = 0;
    private int boxHeight = 0;

    private int boxHorizontalMargin = 10;
    private int boxVerticalMargin = 10;
    
    
    /**
     *
     * Constructor
     *
     */
    public KaboumTextBox(Kaboum parent, Point point, String text) {
        
        this.parent = parent;
        
        boxHorizontalMargin = KaboumUtil.stoi((String) this.parent.getOpModeProperty("TOOLTIP_HORIZONTAL_MARGIN"), 5);
        boxVerticalMargin = KaboumUtil.stoi((String) this.parent.getOpModeProperty("TOOLTIP_VERTICAL_MARGIN"), 5);
        int boxOffset = KaboumUtil.stoi((String) this.parent.getOpModeProperty("TOOLTIP_OFFSET"), 5);
        FontMetrics fontMetrics = this.parent.offScreenGraphics.getFontMetrics();
        int fontHeight = fontMetrics.getHeight();
        
        int width;
        int maxwidth = 0;
        int x = 0;
        int y = 0;
        
        this.rect = new Rectangle(this.parent.screenSize);
        
        // Split the lines
        StringTokenizer st = new StringTokenizer(text, BREAKLINE);
        while (st.hasMoreTokens()) {
            this.setText(st.nextToken());
        }
        
        // Set box size
        for(int i = 0; i < this.numlines; i++) {
            width = fontMetrics.stringWidth(this.text[i]);
            if (width > maxwidth) {
                maxwidth = width;
            }
        }
        
        this.setTextBoxSize(maxwidth + 2 * boxHorizontalMargin, this.numlines * fontHeight + 2 * boxVerticalMargin);
        
        // Step through the possible positions, checking for overlap with the
        for(int i = 0; i < 6; i++) {
            if (i == 0) { // ul
                x = point.x - this.boxWidth - boxOffset;
                y = point.y - this.boxHeight - boxOffset;
            }
            else if (i == 1) { // lr
                x = point.x + boxOffset;
                y = point.y + boxOffset;
            }
            else if (i == 2) { // ll
                x = point.x - this.boxWidth - boxOffset;
                y = point.y + boxOffset;
            }
            else if (i == 3) { // ur
                x = point.x + boxOffset;
                y = point.y - this.boxHeight - boxOffset;
            }
            else if (i == 4) { // uc
                x = (int) (point.x - Math.round(this.boxWidth / 2.0));
                y = point.y - this.boxHeight - boxOffset;
            }
            else if (i == 5) { // lc
                x = (int) (point.x - Math.round(this.boxWidth / 2.0));
                y = point.y + boxOffset;
            }
            
            // check text box against the image
            if (!rect.contains(x, y)) continue;
            if(!rect.contains(x + this.boxWidth, y)) continue;
            if(!rect.contains(x + this.boxWidth, y + this.boxHeight)) continue;
            if(!rect.contains(x, y + this.boxHeight)) continue;
            
            break;
        }
        
        this.setTextBoxPosition(x, y);
        
    }
    
    
    /**
     *
     * Return true if input coordinate is inside
     * the text box container
     *
     * @param x   X coordinate
     * @param y   Y coordinate
     *
     */
    public boolean inside(int x, int y) {
        return this.rect.contains(x, y);
    }
    
    
    /**
     *
     * Set the text within the box container
     *
     * @param text  Text string
     *
     */
    public void setText(String text) {
        if (this.numlines == this.maxLines) {
            return;
        }
        this.text[this.numlines] = text;
        this.numlines++;
    }
    
    
    /**
     *
     * Set the box container position
     *
     * @param x   X coordinate
     * @param y   Y coordinate
     *
     */
    public void setTextBoxPosition(int x, int y) {
        this.boxLeft = x;
        this.boxTop = y;
    }
    
    
    /**
     *
     * Set the box size
     *
     * @param w   Width of the box
     * @param h   Height of the box
     *
     */
    public void setTextBoxSize(int w, int h) {
        this.boxWidth = w;
        this.boxHeight = h;
    }
    
    
    /**
     *
     * Paint method
     *
     */
    public void paint(Graphics g) {
        
        int boxBorderSize = KaboumUtil.stoi((String) this.parent.getOpModeProperty("TOOLTIP_BOX_BORDER_SIZE"), 1);
        Color boxBorderColor = KaboumUtil.getColorParameter((String) this.parent.getOpModeProperty("TOOLTIP_BOX_BORDER_COLOR"), Color.black);
        Color boxColor = KaboumUtil.getColorParameter((String) this.parent.getOpModeProperty("TOOLTIP_BOX_COLOR"), Color.white);
        Color textColor = KaboumUtil.getColorParameter((String) this.parent.getOpModeProperty("TOOLTIP_TEXT_COLOR"), Color.black);
        
        // the background box
        g.setColor(boxBorderColor);
        g.fillRect(this.boxLeft, this.boxTop, this.boxWidth, this.boxHeight);
        g.setColor(boxColor);
        g.fillRect(this.boxLeft + boxBorderSize, this.boxTop + boxBorderSize, this.boxWidth - 2 * boxBorderSize, this.boxHeight - 2 * boxBorderSize);
        
        // the text
        g.setColor(textColor);
	int fontAscent = g.getFontMetrics().getMaxAscent();
	int fontHeight = g.getFontMetrics().getHeight();
	    
        for(int i = 0; i < this.numlines; i++) {
            g.drawString(this.text[i], this.boxLeft + boxHorizontalMargin, this.boxTop + boxVerticalMargin + i * fontHeight + fontAscent);
        }
    }
    
}
