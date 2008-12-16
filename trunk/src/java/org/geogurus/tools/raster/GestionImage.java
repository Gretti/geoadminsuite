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

package org.geogurus.tools.raster;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import org.geogurus.tools.util.TabByte;

public class GestionImage
{
	/**
	 *    transforme une image en tableau de byte
	 */
	public static byte [] imageToByte ( Image img )
			 throws Exception
	{
		int lg = img.getWidth(null);
		int ht = img.getHeight(null);
		int tab [] = new int [lg*ht];
		byte ret [];
		PixelGrabber grab = new PixelGrabber ( img, 0,0,lg, ht, tab, 0, lg);
		try
		{
			grab.grabPixels();
		}
		catch ( Exception e )
		{
			return null;
		}
		ret = TabByte.tabIntToByte(tab);

		return ret ;
	}

	/**
	 *    transforme un tableau de byte en Image
	 */
	public static Image byteToImage ( byte tab [], int lg, int ht )
			 throws Exception
	{
		int tabImg [] = TabByte.tabByteToInt(tab);
		MemoryImageSource src = new MemoryImageSource (lg, ht, tabImg, 0, lg );
		Image ret = Toolkit.getDefaultToolkit().createImage( src );

		return ret;
	}
}
