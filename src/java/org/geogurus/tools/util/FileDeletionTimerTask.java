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
/*
 * FileDeletionTimerTask.java
 *
 * Created on 17 d�cembre 2002, 16:55
 */
package org.geogurus.tools.util;

import java.io.File;
import java.util.Date;

/**
 * A TimerTask intendeed to delete files based on an extension, (Case sensitive) and a date, a size or a time interval.
 * You should creates several FileDeletionTimerTask to delete groups of files with != extensions
 * For the moment (17 dec 2002), only extension and date and size are supported.
 *<br> Use a java Timer to launch this task.
 * <p>
 * Example of use:
 * <br>To delete all gif files in tmp folder:
 * <code>
 * FileDeletionTimerTask fd = new FileDeletionTimerTask("/tmp/*.gif");
 *</code>
 * <br>To delete all gif files in tmp folder older than today:
 * <code>
 * FileDeletionTimerTask fd = new FileDeletionTimerTask("/tmp/*.gif",
 *                                                      new Date().getTime()),
 *                                                      FileDeletionTimerTask.DATE_BEFORE)
 *</code>
 * <br>To delete all gif files in tmp folder older than one day:
 * All files whose lastModified date il older than currentDate minus given range will be deleted
 * <code>
 * FileDeletionTimerTask fd = new FileDeletionTimerTask("/tmp/*.gif",
 *                                                      24*3600*1000),
 *                                                      FileDeletionTimerTask.INTERVAL_BEFORE)
 *</code>
 * @author  nri
 */
public class FileDeletionTimerTask extends java.util.TimerTask {

    /** constants to specify how date or size should be compared */
    public static final byte DATE_BEFORE = 0;
    public static final byte DATE_AFTER = 1;
    public static final byte DATE_SAME = 2;
    public static final byte SIZE_LESS = 3;
    public static final byte SIZE_EQUAL = 4;
    public static final byte SIZE_GREATER = 5;
    public static final byte INTERVAL_BEFORE = 6;
    public static final byte INTERVAL_EQUAL = 7;
    /**
     * The absolute path and extension to delete. Metacharacters allowed ("/tmp/*.map", "c:\\temp\\*.gif", etc.) <br>
     * Case sensitive.
     */
    protected String path;
    /**
     * A file to exclude from the list of files to remove
     */
    protected String exclusionFileName;
    /**
     * the date of file to choose files to delete
     *<br>(A long value representing the time, measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970))
     */
    protected long date;
    /**
     * the type of comparison to perform for date-based deletion
     */
    protected byte dateComparator;
    /**
     * the type of comparison to perform for size-based deletion
     */
    protected byte sizeComparator;
    /**
     * The size of files to choose files to delete. <br>
     * Size in bytes
     */
    protected long size;
    /** true to delete folders matching date criteria */
    boolean deleteFolder = false;

    /**
     * Creates a new instance of FileDeletionTimerTask with the specified files to delete
     *<br>NOTE: no size or date comparison will be made: all files matching the
     * given extension in the given folder will be deleted.
     */
    public FileDeletionTimerTask(String path) {
        this.path = path;
        // no size filter
        size = -1;
        // no date filter
        date = -1;
        // default values.
        sizeComparator = FileDeletionTimerTask.SIZE_EQUAL;
        dateComparator = FileDeletionTimerTask.DATE_SAME;
    }

    /**
     * Creates a new instance of FileDeletionTimerTask with the give files to delete,
     * For the given date matching the given comparison.
     * <br> If dateComparator is INTERVAL_*, date represents the time interval that is
     * compared to current time to perform the file deletion.<br>
     * Ex: pass 24*3600*1000 to delete all files older than 24 hours.
     *<br>NOTE: no size or date comparison will be made: all files matching the
     * given extension in the given folder will be deleted.
     */
    public FileDeletionTimerTask(String path, long date, byte dateComparator, boolean deleteFolder) {
        this(path);
        this.date = date;
        this.dateComparator = dateComparator;
        this.deleteFolder = deleteFolder;
    }

    /**
     * Creates a new instance of FileDeletionTimerTask with the give files to delete,
     * For the given date matching the given comparison
     *<br>NOTE: no size or date comparison will be made: all files matching the
     * given extension in the given folder will be deleted.
     */
    public FileDeletionTimerTask(String path, long size, byte sizeComparator, long date, byte dateComparator, boolean deleteFolder) {
        this(path, size, sizeComparator, deleteFolder);
        this.date = date;
        this.dateComparator = dateComparator;
    }

    /** When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     * <p>
     * Searches for all files denoted by path, and delete them according to comparison rules
     *
     * @see     java.lang.Thread#run()
     *
     */
    public void run() {
        if (path == null) {
            return;
        }
        // un*x path
        int idx = path.lastIndexOf("/");
        if (idx == -1) {
            // windows path
            idx = path.lastIndexOf("\\");

            if (idx == -1) {
                // not a windows or unix path
                return;
            }
        }
        String folder = path.substring(0, idx);
        String ext = "";
        if (idx < path.length() - 1) {
            ext = path.substring(idx + 1);
        } else {
            // not a valid path
            return;
        }
        idx = ext.lastIndexOf(".");

        if (idx == -1) {
            return;
        }
        ext = ext.substring(idx);

        File f = new File(folder);
        File[] files = f.listFiles();
        boolean deleteByDate = true;
        boolean deleteBySize = true;
        boolean deleteIfFolder = true;

        // can now delete files based on filter
        for (int i = 0; i < files.length; i++) {
            deleteByDate = true;
            deleteBySize = true;
            if (files[i].getName().endsWith(ext) || ext.equalsIgnoreCase(".*") || files[i].isDirectory()) {
                // look for criteria to delete.
                deleteIfFolder = (files[i].isDirectory() && deleteFolder) || (!files[i].isDirectory());
                if (size != -1) {
                    // must filter by size
                    deleteBySize = false;

                    switch (sizeComparator) {
                        case FileDeletionTimerTask.SIZE_EQUAL:
                            deleteBySize = (files[i].length() == size);
                            break;
                        case FileDeletionTimerTask.SIZE_GREATER:
                            deleteBySize = (files[i].length() > size);
                            break;
                        case FileDeletionTimerTask.SIZE_LESS:
                            deleteBySize = (files[i].length() < size);
                            break;
                        default:
                            deleteBySize = false;
                    }
                }
                if (date != -1) {
                    // must filter by date too
                    // must filter by size
                    deleteByDate = false;
                    switch (dateComparator) {
                        case FileDeletionTimerTask.DATE_SAME:
                            deleteByDate = (files[i].lastModified() == date);
                            break;
                        case FileDeletionTimerTask.DATE_AFTER:
                            deleteByDate = (files[i].lastModified() > date);
                            break;
                        case FileDeletionTimerTask.DATE_BEFORE:
                            deleteByDate = (files[i].lastModified() < date);
                            break;
                        case FileDeletionTimerTask.INTERVAL_BEFORE:
                            deleteByDate = (new Date().getTime() - files[i].lastModified() - date > 0);
                            break;
                        case FileDeletionTimerTask.INTERVAL_EQUAL:
                            deleteByDate = (new Date().getTime() - files[i].lastModified() - date == 0);
                            break;
                        default:
                            deleteByDate = false;
                    }
                }
                if (deleteByDate && deleteBySize && deleteIfFolder) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    files[i].delete();
                }
            }
        }
    }

    public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public void setSize(long s) {
        this.size = s;
    }

    public void setDate(long s) {
        this.date = s;
    }

    public void setPath(String p) {
        this.path = p;
    }

    public void setSizeComparator(byte b) {
        this.sizeComparator = b;
    }

    public void setDateComparator(byte b) {
        this.dateComparator = b;
    }
}

