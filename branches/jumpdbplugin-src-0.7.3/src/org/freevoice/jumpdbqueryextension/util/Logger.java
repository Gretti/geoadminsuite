/*
* 
*  The JUMP DB Query Plugin is Copyright (C) 2007  Larry Reeder
*  JUMP is Copyright (C) 2003 Vivid Solutions
* 
*  This file is part of the JUMP DB Query Plugin.
*  
*  The JUMP DB Query Plugin is free software; you can redistribute it and/or 
*  modify it under the terms of the Lesser GNU General Public License as 
*  published *  by the Free Software Foundation; either version 3 of the 
*  License, or  (at your option) any later version.
*  
*  This software is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  Lesser GNU General Public License for more details.
*  
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.freevoice.jumpdbqueryextension.util;

/**
 * Basic logger class
 */
public class Logger
{

    public static void logError(String error)
    {
        System.err.println(error);
    }

    public static void logError(Throwable t)
    {
        t.printStackTrace();
    }

    public static void logDebug(String message)
    {
        System.err.println(message);
    }

    public static void logDebug(Throwable t)
    {
        t.printStackTrace();
    }

   public static void logInfo(String message)
   {
      System.err.println(message);
   }

   public static void logInfo(Throwable t)
   {
      t.printStackTrace();
   }
}
