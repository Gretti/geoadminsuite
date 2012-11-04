/*
 * @requires OpenLayers/Control/Snapping.js
 */


/**
 * Class: GeoSIE.Control.SnappingSIE
 * The SnappingSIE control is based on Snapping Control, it returns the feature snapped.
 * Using : add a point on a lineString (for exemple a section of BD Topo with a attribute nom : Loire) 
 * with the snapping button, your point is snapped on a lineString and the point has the information
 * of the lineString snapped (point.feature.attributes = lineString.feature.attributes). 
 * For the exemple your point is a feature with the attribute nom : Loire.
 * 
 * Two methods of OL Snapping are override (considerSnapping and testTarget) to return
 * the feature snapped.
 * 
 * TO UPDATE WHEN YOU UPDATE OL (2.10)
 * 
 * Inherits from:
 * - <OpenLayers.Control.Snapping>
 */
GeoSIE.Control.SnappingSIE = OpenLayers.Class( OpenLayers.Control.Snapping, {

    /**
     * Constructor: GeoSIE.Control.SnappingSIE
     * 
     * Parameters:
     * options - {Object}
     * 
     * Returns:
     * {<GeoSIE.Control.SnappingSIE>}
     */
    initialize: function(options) {
    	OpenLayers.Control.Snapping.prototype.initialize.apply(this, arguments);
    },
	
    /**
     * Method: considerSnapping
     *
     * Parameters:
     * point - {<OpenLayers.Geometry.Point}} The vertex to be snapped (or
     *     unsnapped).
     * loc - {<OpenLayers.Geometry.Point>} The location of the mouse in map
     *     coords.
     */
    considerSnapping: function(point, loc) {
        //TODO : a mettre a jour si changement de version de OL (actuel 2.10)
    	var best = {
            rank: Number.POSITIVE_INFINITY,
            dist: Number.POSITIVE_INFINITY,
            feature: null,
            x: null, y: null
        };
        var snapped = false;
        var result, target;
        for(var i=0, len=this.targets.length; i<len; ++i) {
            target = this.targets[i];
            result = this.testTarget(target, loc);
            if(result) {
                if(this.greedy) {
                    best = result;
                    best.target = target; 
                    snapped = true;
                    break;
                } else {
                    if((result.rank < best.rank) ||
                       (result.rank === best.rank && result.dist < best.dist)) {
                        best = result;
                        best.target = target;
                        snapped = true;
                    }
                }
            }
        }
        if(snapped) {
            var proceed = this.events.triggerEvent("beforesnap", {
                point: point, x: best.x, y: best.y, distance: best.dist, feature: best.feature,
                layer: best.target.layer, snapType: this.precedence[best.rank]
            });
            if(proceed !== false) {
                point.x = best.x;
                point.y = best.y;
                this.point = point;
                this.events.triggerEvent("snap", {
                    point: point,
                    snapType: this.precedence[best.rank],
                    layer: best.target.layer,
                    distance: best.dist,
                    feature: best.feature
                });
            } else {
                snapped = false;
            }
        }
        if(this.point && !snapped) {
            point.x = loc.x;
            point.y = loc.y;
            this.point = null;
            this.events.triggerEvent("unsnap", {point: point});
        }
    },
    
    /**
     * Method: testTarget
     * 
     * Parameters:
     * target - {Object} Object with target layer configuration.
     * loc - {<OpenLayers.Geometry.Point>} The location of the mouse in map
     *     coords.
     *
     * Returns:
     * {Object} A result object with rank, dist, the feature snapped, x, and y properties.
     *     Returns null if candidate is not eligible for snapping.
     */
    testTarget: function(target, loc) {
        //TODO : a mettre a jour si changement de version de OL (actuel 2.10)
        var tolerance = {
            node: this.getGeoTolerance(target.nodeTolerance),
            vertex: this.getGeoTolerance(target.vertexTolerance),
            edge: this.getGeoTolerance(target.edgeTolerance)
        };
        // this could be cached if we don't support setting tolerance values directly
        var maxTolerance = Math.max(
            tolerance.node, tolerance.vertex, tolerance.edge
        );
        var result = {
            rank: Number.POSITIVE_INFINITY, dist: Number.POSITIVE_INFINITY
        };
        var eligible = false;
        var features = target.layer.features;
        var feature, type, vertices, vertex, closest, dist, found;
        var numTypes = this.precedence.length;
        var ll = new OpenLayers.LonLat(loc.x, loc.y);
        for(var i=0, len=features.length; i<len; ++i) {
            feature = features[i];
            if(feature !== this.feature && !feature._sketch &&
               feature.state !== OpenLayers.State.DELETE &&
               (!target.filter || target.filter.evaluate(feature.attributes))) {
                if(feature.atPoint(ll, maxTolerance, maxTolerance)) {
                    for(var j=0, stop=Math.min(result.rank+1, numTypes); j<stop; ++j) {
                        type = this.precedence[j];
                        if(target[type]) {
                            if(type === "edge") {
                                closest = feature.geometry.distanceTo(loc, {details: true});
                                dist = closest.distance;
                                if(dist <= tolerance[type] && dist < result.dist) {
                                    result = {
                                        rank: j, dist: dist, feature: feature,
                                        x: closest.x0, y: closest.y0 // closest coords on feature
                                    };
                                    eligible = true;
                                    // don't look for lower precedence types for this feature
                                    break;
                                }
                            } else {
                                // look for nodes or vertices
                                vertices = feature.geometry.getVertices(type === "node");
                                found = false;
                                for(var k=0, klen=vertices.length; k<klen; ++k) {
                                    vertex = vertices[k];
                                    dist = vertex.distanceTo(loc);
                                    if(dist <= tolerance[type] &&
                                       (j < result.rank || (j === result.rank && dist < result.dist))) {
                                        result = {
                                            rank: j, dist: dist, feature: feature,
                                            x: vertex.x, y: vertex.y
                                        };
                                        eligible = true;
                                        found = true;
                                    }
                                }
                                if(found) {
                                    // don't look for lower precedence types for this feature
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return eligible ? result : null;
    },
    
    CLASS_NAME: 'GeoSIE.Control.SnappingSIE'
});