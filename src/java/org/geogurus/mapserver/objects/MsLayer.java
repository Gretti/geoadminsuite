package org.geogurus.mapserver.objects;

/**
 * Enumeration of MapServer Layer types.
 * 
 * @author jesse
 */
public enum MsLayer {
    LOCAL("Label"), SDE("sde"), OGR("ogr"), POSTGIS("Postgis"), ORACLESPATIAL(
            "Oracle Spatial"), WMS("Web Map Server"), WFS("Web Feature Server");

    private final String label;

    /**
     * Creates a new instance of type MsLayer
     */
    private MsLayer(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
