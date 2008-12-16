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

package org.geogurus.data;

/**
 * Just a container of 2 elements. Good for returning 2 values.
 * 
 * @author jesse
 */
public class Pair<R, L> {
    public static <R, L> Pair<R, L> read(R one, L two) {
        return new Pair<R, L>(one, two);
    }

    public static <R, L> Pair<R, L> write(R one, L two) {
        return new Writeable<R, L>(one, two);
    }

    private R one;
    private L two;

    private Pair(R one, L two) {
        super();
        this.one = one;
        this.two = two;
    }

    public R one() {
        return one;
    }

    public L two() {
        return two;
    }

    public static class Writeable<R, L> extends Pair<R, L> {
        public Writeable(R one, L two) {
            super(one, two);
        }

        public Writeable<R, L> one(R newVal) {
            super.one = newVal;
            return this;
        }

        public Writeable<R, L> two(L newVal) {
            super.two = newVal;
            return this;
        }
    }

}
