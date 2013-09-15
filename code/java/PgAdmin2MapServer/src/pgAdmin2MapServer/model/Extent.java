package pgAdmin2MapServer.model;

import java.io.Serializable;

/**
 *
 * This class define a spatial extent.
 *
 * @author Nico, with code from Kaboum (Jerome Gasperi aka jrom)
 *
 */
public class Extent {

    /**
     * Lower left X coordinate
     */
    public double xMin;
    /**
     * Lower left Y coordinate
     */
    public double yMin;
    /**
     * Upper right X coordinate
     */
    public double xMax;
    /**
     * Upper right Y coordinate
     */
    public double yMax;

    /**
     *
     * Default Constructor
     *
     *
     */
    public Extent() {
        this.init();
    }

    /**
     *
     * Constructor
     *
     * @param xMin Lower left X coordinate
     * @param yMin Lower left Y coordinate
     * @param xMax Upper right X coordinate
     * @param yMax Upper right Y coordinate
     *
     */
    public Extent(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = (xMin);
        this.yMin = (yMin);
        this.xMax = (xMax);
        this.yMax = (yMax);
    }

    /**
     *
     * Checks if extent e overlap currentExtent
     *
     * @param e Extent to check overlapping with the current one
     *
     */
    public boolean overlap(Extent e) {
        return !((e.xMin) > this.xMax
                || (e.xMax) < this.xMin
                || (e.yMin) > this.yMax
                || (e.yMax) < this.yMin);
    }

    /**
     *
     * Checks if currentExtent contains another extent e
     *
     * @param e Extent
     *
     */
    public boolean contains(Extent e) {
        return (this.xMax >= (e.xMax)
                && this.xMin <= (e.xMin)
                && this.yMax >= (e.yMax)
                && this.yMin <= (e.yMin));
    }

    /**
     *
     * Checks if extent truly contains coordinate c. Point that lie under the
     * limit of the extent are not inside the extent.
     *
     * @param c coordinate
     *
     */
    public boolean trulyContains(double x, double y) {
        return (this.xMax > x
                && this.xMin < x
                && this.yMax > y
                && this.yMin < y);
    }

    /**
     *
     * Return the deltaX i.e. (xMax - xMin)
     *
     */
    public double dx() {
        return this.xMax - this.xMin;
    }

    /**
     *
     * Return the deltaY i.e. (yMax - yMin)
     *
     */
    public double dy() {
        return this.yMax - this.yMin;
    }

    /**
     *
     * Return the equivalent size in pixel of a distance in map unit within a
     * given size
     *
     */
    public int getSizeInPixels(double d, int width) {
        return (int) ((d * width) / this.dx());
    }

    /**
     *
     * Enlarges the boundary of the
     * <code>Envelope</code> so that it contains (x,y). Does nothing if (x,y) is
     * already on or within the boundaries.
     *
     * @param p input coordinate
     *
     */
    public void expandToInclude(double x, double y) {
        if (isNull()) {
            xMin = x;
            xMax = x;
            yMin = y;
            yMax = y;
        } else {
            if (x < xMin) {
                xMin = x;
            }
            if (x > xMax) {
                xMax = x;
            }
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
        }
    }

    /**
     *
     * Enlarges the boundary of the
     * <code>Extent</code> so that it contains
     * <code>other</code>. Does nothing if
     * <code>other</code> is wholly on or within the boundaries.
     *
     * @param other the <code>Extent</code> to merge with
     */
    public void expandToInclude(Extent other) {
        if (other.isNull()) {
            return;
        }

        double tmpXMin = (other.xMin);
        double tmpXMax = (other.xMax);
        double tmpYMin = (other.yMin);
        double tmpYMax = (other.yMax);

        if (isNull()) {
            xMin = tmpXMin;
            xMax = tmpXMax;
            yMin = tmpYMin;
            yMax = tmpYMax;
        } else {
            if (tmpXMin < xMin) {
                xMin = tmpXMin;
            }
            if (tmpXMax > xMax) {
                xMax = tmpXMax;
            }
            if (tmpYMin < yMin) {
                yMin = tmpYMin;
            }
            if (tmpYMax > yMax) {
                yMax = tmpYMax;
            }
        }
    }

    /**
     *
     * Returns
     * <code>true</code> if this
     * <code>Envelope</code> is a "null" extent.
     *
     * @return    <code>true</code> if this <code>Extent</code> is uninitialized
     *
     */
    public boolean isNull() {
        return xMax < xMin;
    }

    /**
     *
     * Initialize this extent
     *
     */
    public void init() {
        this.xMin = 0.0;
        this.yMin = 0.0;
        this.xMax = -1.0;
        this.yMax = -1.0;
    }

    /**
     *
     * Return a string version of this extent in a mapserver cgi readable form
     */
    public String msCgiString() {
        return this.externalString("+", "+");
    }

    /**
     *
     * Return a string version of this extent in a mapserver mapfile readable form
     *
     */
    public String msString() {
        return this.externalString(" ", " ");
    }

    /**
     *
     * Return a string version of this extent in a mapserver cgi readable form
     *
     * @param pm Precision Model
     *
     */
    public String externalString(String xySeparator, String coordSeparator) {
        return Double.toString(this.xMin) + xySeparator + Double.toString(this.yMin)
                + coordSeparator + Double.toString(this.xMax) + xySeparator + Double.toString(this.yMax);

    }
    
    /**
     * 
     * @return coordinates as an array of 4 doubles (lllx, lly, urx, ury)
     */
    public double[] toArray() {
        double[] ret = {this.xMin, this.yMin, this.xMax, this.yMax};
        return ret;
    }
}
