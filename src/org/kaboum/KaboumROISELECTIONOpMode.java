package org.kaboum;

/*
 *
 * Class KaboumDistanceOpMode from the Kaboum project.
 * Calculate distance between 2 or more points.
 * Kaboum is a frontend to mapserver (http://mapserver.gis.umn.edu)
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

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.kaboum.util.KaboumCoordinate;
import org.kaboum.util.KaboumExtent;
import org.kaboum.util.KaboumUtil;

/**
 * 
 * This opMode is used to select a polygonal ROI
 * 
 * @author Jerome Gasperi aka jrom
 *  
 */
public class KaboumROISELECTIONOpMode extends KaboumOpMode {

	/** Constant */
	public static final String PARAM_FOREGROUNDCOLOR = "ROISELECTION_FOREGROUND_COLOR";

	public static final String PARAM_POINTTYPE = "ROISELECTION_POINT_TYPE";

	public static final String PARAM_POINTHEIGHT = "ROISELECTION_POINT_HEIGHT";

	public static final String PARAM_POINTWIDTH = "ROISELECTION_POINT_WIDTH";

	public static final String PARAM_ROISELECTIONADDDISTANCE = "ROISELECTION_ADD_DISTANCE";

	public static final String PARAM_ROISELECTIONBOUNDINGBOX = "ROISELECTION_BOUNDING_BOX";
	
	public static final String PARAM_ROISELECTIONBOUNDINGBOXCOLOR = "ROISELECTION_BOUNDING_BOX_COLOR";
	
	public static String[] getParametersList() {
		String[] list = new String[7];
		list[0] = PARAM_FOREGROUNDCOLOR;
		list[1] = PARAM_POINTTYPE;
		list[2] = PARAM_POINTHEIGHT;
		list[3] = PARAM_POINTWIDTH;
		list[4] = PARAM_ROISELECTIONADDDISTANCE;
		list[5] = PARAM_ROISELECTIONBOUNDINGBOX;
		list[6] = PARAM_ROISELECTIONBOUNDINGBOXCOLOR;
		return list;
	}

	/** Parent reference */
	private Kaboum parent;

	/** Anchor for the first point clicked */
	private Point anchor = null;

	/** Distance */
	protected double distance = 0;

	/** Frozen state */
	private boolean freeze = false;

	/** Bounding box */
	private KaboumCoordinate LLCoord = new KaboumCoordinate();
	private KaboumCoordinate URCoord = new KaboumCoordinate();
	

	/**
	 * 
	 * Vector containing the cliqued points. Coordinates are stored in internal
	 * representation
	 * 
	 * N.B. : this vector is static, so changing opMode does not erase the
	 * current distance path
	 *  
	 */
	private static Vector path = new Vector();

	/**
	 * 
	 * Constructor
	 * 
	 * @param parent
	 *            Parent reference
	 *  
	 */
	public KaboumROISELECTIONOpMode(Kaboum parent) {

		this.parent = parent;
		this.distance = 0;
		this.mp = new Point(-100, -100);

		// Ajoute les listeners au parent
		parent.addMouseListener(this);
		parent.addMouseMotionListener(this);

		int numPointsMinusOne = KaboumROISELECTIONOpMode.path.size() - 1;

		// Check for a non-empty preexisting vector path
		if (numPointsMinusOne > -1) {
			this.anchor = this.parent.mapServerTools
					.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
							.elementAt(numPointsMinusOne));
			for (int i = 0; i < numPointsMinusOne; i++) {
				this.distance = this.distance
						+ this.parent.mapServerTools
								.getDistance(
										(KaboumCoordinate) KaboumROISELECTIONOpMode.path
												.elementAt(i),
										(KaboumCoordinate) KaboumROISELECTIONOpMode.path
												.elementAt(i + 1));
			}
		}

		// Definit la forme du curseur de la souris
		parent.setCursor("CROSSHAIR");

	}

	/*
	 * 
	 * PAINT
	 *  
	 */
	public void paint(Graphics g) {

		double tmpDistance = 0;

		g.setColor(KaboumUtil.getColorParameter(this.parent
				.getOpModeProperty(PARAM_FOREGROUNDCOLOR), Color.black));

		//
		// DRAW THE VECTOR
		//
		int numPoints = KaboumROISELECTIONOpMode.path.size();
		Point pointA;
		Point pointB;
		Point point0;

		for (int i = 0; i < numPoints - 1; i++) {
			pointA = this.parent.mapServerTools
					.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
							.elementAt(i));
			pointB = this.parent.mapServerTools
					.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
							.elementAt(i + 1));
			g.drawLine(pointA.x, pointA.y, pointB.x, pointB.y);
		}

		// Draw the vertices
		int type = KaboumCoordinate.stoi(this.parent
				.getOpModeProperty(PARAM_POINTTYPE));
		int pointHeight = KaboumUtil.stoi(this.parent
				.getOpModeProperty(PARAM_POINTHEIGHT), 5);
		int pointWidth = KaboumUtil.stoi(this.parent
				.getOpModeProperty(PARAM_POINTWIDTH), 5);

		for (int i = 0; i < numPoints; i++) {
			this
					.paintVertices(
							this.parent.mapServerTools
									.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
											.elementAt(i)), type, pointHeight,
							pointWidth, g);
		}

		// draw the distance label only if there is a first point
		// and the control list is not frozen
		if (this.anchor != null && !this.freeze) {

			this.updateBoundingBox();

			// Draw the current point position
			if (numPoints > 0) {
				pointA = this.parent.mapServerTools
						.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
								.elementAt(numPoints - 1));
				point0 = this.parent.mapServerTools
						.internalToMouseXY((KaboumCoordinate) KaboumROISELECTIONOpMode.path
								.elementAt(0));

				g.drawLine(pointA.x, pointA.y, this.mp.x, this.mp.y);
				g.drawLine(point0.x, point0.y, this.mp.x, this.mp.y);
				this.paintVertices(this.mp, type, pointHeight, pointWidth, g);
			}

			KaboumCoordinate coordA = this.parent.mapServerTools
					.mouseXYToInternal(this.anchor.x, this.anchor.y);
			KaboumCoordinate coordB = this.parent.mapServerTools
					.mouseXYToInternal(this.mp.x, this.mp.y);
			;

			tmpDistance = this.distance
					+ this.parent.mapServerTools.getDistance(coordA, coordB);

			String tmpStr = "d = "
					+ this.parent.mapServerTools.pm
							.getNumberFormated(tmpDistance) + " m";
			int tmpWidth = g.getFontMetrics().stringWidth(tmpStr);
			int tmpHeight = g.getFontMetrics().getHeight();

			// revert the distance drawing if label is outside the applet
			int shiftX = -15;
			int shiftY = -15;
			int xbox = this.mp.x + shiftX + tmpWidth;
			int ybox = this.mp.y + shiftY - tmpHeight;

			if (xbox > parent.screenSize.width) {
				// box_width negative shift
				shiftX = -tmpWidth;
			}
			if (ybox < 0) {
				// positive shift
				shiftY = 30;
			}

			if (KaboumUtil.stob(this.parent
					.getOpModeProperty(PARAM_ROISELECTIONBOUNDINGBOX), false)) {
				g.setColor(KaboumUtil.getColorParameter(
								this.parent.getOpModeProperty(PARAM_ROISELECTIONBOUNDINGBOXCOLOR), Color.lightGray));
				Point LLPoint = this.parent.mapServerTools.internalToMouseXY(this.LLCoord);
				Point URPoint = this.parent.mapServerTools.internalToMouseXY(this.URCoord);
				g.drawRect(LLPoint.x, URPoint.y, (URPoint.x - LLPoint.x), (LLPoint.y - URPoint.y));
			}

			if (KaboumUtil.stob(this.parent
					.getOpModeProperty(PARAM_ROISELECTIONADDDISTANCE), false)) {
				g.setColor(Color.black);
				g.fillRect(this.mp.x + shiftX, (int) this.mp.y + shiftY
						- tmpHeight, tmpWidth, tmpHeight);
				g.setColor(Color.white);
				g
						.drawString(tmpStr, this.mp.x + shiftX, this.mp.y
								+ shiftY - 3);
			}

		}
	}

	public void destroyEvent() {
		parent.removeMouseListener(this);
		parent.removeMouseMotionListener(this);
	}

	public void mouseDragged(MouseEvent evt) {
		this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(
				evt.getX(), evt.getY()));
	}

	public void mouseReleased(MouseEvent evt) {
	}

	public void mouseMoved(MouseEvent evt) {
		this.mp = evt.getPoint();
		this.parent.showMessage(this.parent.mapServerTools.getMapCoordString(
				evt.getX(), evt.getY()));
		this.parent.repaint();
	}

	public void mouseClicked(MouseEvent evt) {

		int mods = evt.getModifiers();

		Point tmpAnchor = evt.getPoint();

		if (this.anchor != null) {
			KaboumCoordinate coordA = this.parent.mapServerTools
					.mouseXYToInternal(this.anchor.x, this.anchor.y);
			KaboumCoordinate coordB = this.parent.mapServerTools
					.mouseXYToInternal(tmpAnchor.x, tmpAnchor.y);
			this.distance = this.distance
					+ this.parent.mapServerTools.getDistance(coordA, coordB);
		} else {
			this.LLCoord = new KaboumCoordinate(this.parent.mapServerTools.mouseXYToInternal(tmpAnchor.x, tmpAnchor.y));
			this.URCoord = new KaboumCoordinate(this.parent.mapServerTools.mouseXYToInternal(tmpAnchor.x, tmpAnchor.y));
		}

		this.anchor = tmpAnchor;

		if ((mods & InputEvent.BUTTON1_MASK) != 0) {
			// Nico: if geoObject is frozen: no more action on click: must clean
			// the opMode
			// to reinit a distance mode
			if (this.freeze) {
				return;
			}

			KaboumCoordinate internal = this.parent.mapServerTools
					.mouseXYToInternal(this.anchor.x, this.anchor.y);

			// Add this coordinate to the path vector
			KaboumROISELECTIONOpMode.path.addElement(internal);

			
		} else {
			KaboumROISELECTIONOpMode.path.removeAllElements();
			KaboumExtent tmpExtent = new KaboumExtent(LLCoord, URCoord);
			this.parent.kaboumResult("ROISELECTION|"
					+ tmpExtent.kaboumExternalString());
		}
	}

	/**
	 * 
	 * Update the bounding box coordinate
	 * 
	 * @param tmpPoint
	 *            input point
	 *  
	 */
	private void updateBoundingBox() {

		int size = KaboumROISELECTIONOpMode.path.size();
		
		KaboumCoordinate tmpCoord = null;
		KaboumCoordinate tmpmp = this.parent.mapServerTools.mouseXYToInternal(this.mp.x, this.mp.y);
		
		//LLCoord.x = Double.MAX_VALUE;
		//LLCoord.y = Double.MAX_VALUE;
		//URCoord.x = Double.MIN_VALUE;
		//URCoord.y = Double.MIN_VALUE;
		
		//if (tmpmp.x < LLCoord.x) {
			LLCoord.x = tmpmp.x;
		//}
		//if (tmpmp.y < LLCoord.y) {
			LLCoord.y = tmpmp.y;
		//}
		//if (tmpmp.x > URCoord.x) {
			URCoord.x = tmpmp.x;
		//}
		//if (tmpmp.y > URCoord.y) {
			URCoord.y = tmpmp.y;
		//}
		
		for (int i = 0; i < size; i++) {
			tmpCoord = (KaboumCoordinate) KaboumROISELECTIONOpMode.path.elementAt(i);

			if (tmpCoord.x < LLCoord.x) {
				LLCoord.x = tmpCoord.x;
			}
			if (tmpCoord.y < LLCoord.y) {
				LLCoord.y = tmpCoord.y;
			}
			if (tmpCoord.x > URCoord.x) {
				URCoord.x = tmpCoord.x;
			}
			if (tmpCoord.y > URCoord.y) {
				URCoord.y = tmpCoord.y;
			}
		}
		return;
	}

	/**
	 * 
	 * Paint one vertice
	 *  
	 */
	private void paintVertices(Point point, int type, int pointHeight,
			int pointWidth, Graphics g) {

		switch (type) {
		case KaboumCoordinate.K_TYPE_CIRCLE:
			g.drawOval((int) (point.x - pointWidth / 2),
					(int) (point.y - pointHeight / 2), pointWidth, pointHeight);
			break;
		case KaboumCoordinate.K_TYPE_BOX:
			g.fillRect((int) (point.x - pointWidth / 2),
					(int) (point.y - pointHeight / 2), pointWidth, pointHeight);
			break;
		case KaboumCoordinate.K_TYPE_POINT:
			g.drawOval(point.x, point.y, 1, 1);
			break;
		// Default is K_TYPE_POINT
		default:
			g.drawOval(point.x, point.y, 1, 1);
		}

	}

}