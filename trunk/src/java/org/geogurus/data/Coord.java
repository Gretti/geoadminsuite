package org.geogurus.data;

/**
 * Very basic 2-dimension coordinate object
 */
public class Coord  implements java.io.Serializable { 
	private static final long serialVersionUID = 1L;
	/** the x-coordinate of this <code>Coord</code>. (public for more efficient access)*/
    public double x;
    /** the y-coordinate of this <code>Coord</code>. (public for more efficient access)*/
    public double y;

    public Coord() {}
    public Coord(double x, double y) {
        this.x = x;
        this.y = y;
    }
    /**
     * Constructor with <code>String</code> representation of double values.
     * <br>Note:<b>Set x and y fields to Double.MIN_VALUE in case of NumberFormatException</b>
     */
     public Coord(String xs, String ys) {
        try {
            x = new Double(xs).doubleValue();
            y = new Double(ys).doubleValue();
        } catch (NumberFormatException nfe) {
            x = Double.MIN_VALUE;
            y = Double.MIN_VALUE;
        }
     }
}

