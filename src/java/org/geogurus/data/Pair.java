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
