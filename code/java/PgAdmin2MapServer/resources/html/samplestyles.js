/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Defines a polygon, linestring and point styles
 * 
 */

var vecStyle0 = new ol.style.Style({rules: [
        new ol.style.Rule({
            filter: 'geometryType("polygon")',
            symbolizers: [
                new ol.style.Fill({
                    color: '#ffff00',
                    opacity: 0.6
                }),
                new ol.style.Stroke({
                    color: '#319FD3',
                    opacity: 1
                })
            ]
        }), new ol.style.Rule({
            filter: 'geometryType("linestring")',
            symbolizers: [
                new ol.style.Line({
                    strokeColor: '#013',
                    strokeWidth: 1,
                    opacity: 1
                })
            ]
        }),new ol.style.Rule({
            filter: 'geometryType("point")',
            symbolizers: [
                new ol.style.Shape({
                    size: 40,
                    fillColor: '#013'
                }),
                new ol.style.Text({
                    color: '#bada55',
                    text: ol.expr.parse('label'),
                    fontFamily: 'Calibri,sans-serif',
                    fontSize: 14
                })
            ]
        })
    ]}
);


var vecStyle2 = new ol.style.Style({rules: [
        new ol.style.Rule({
            filter: 'where == "outer"',
            symbolizers: [
                new ol.style.Line({
                    strokeColor: ol.expr.parse('color'),
                    strokeWidth: 4,
                    opacity: 1
                })
            ]
        }),
        new ol.style.Rule({
            filter: 'where == "inner"',
            symbolizers: [
                new ol.style.Line({
                    strokeColor: '#013',
                    strokeWidth: 4,
                    opacity: 1
                }),
                new ol.style.Line({
                    strokeColor: ol.expr.parse('color'),
                    strokeWidth: 2,
                    opacity: 1
                })
            ]
        }),
        new ol.style.Rule({
            filter: 'geometryType("point")',
            symbolizers: [
                new ol.style.Shape({
                    size: 40,
                    fillColor: '#013'
                }),
                new ol.style.Text({
                    color: '#bada55',
                    text: ol.expr.parse('label'),
                    fontFamily: 'Calibri,sans-serif',
                    fontSize: 14
                })
            ]
        })
    ]});

var vecStyle1 = new ol.style.Style({rules: [
        new ol.style.Rule({
            symbolizers: [
                new ol.style.Fill({
                    color: '#ffff00',
                    opacity: 0.6
                }),
                new ol.style.Stroke({
                    color: '#319FD3',
                    opacity: 1
                })
            ]
        }),
        new ol.style.Rule({
            filter: 'resolution() < 5000',
            symbolizers: [
                new ol.style.Text({
                    color: '#000000',
                    text: ol.expr.parse('name'),
                    fontFamily: 'Calibri,sans-serif',
                    fontSize: 12
                })
            ]
        })
    ]});
