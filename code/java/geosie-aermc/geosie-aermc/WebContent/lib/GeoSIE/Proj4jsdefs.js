/**
 * Header: Proj4js
 * Extends projection definitions as provided by/suited for Proj4 API. 
 * 
 * See Spatial References at <http://spatialreference.org>
 * 
 * See Proj4Js at <http://www.proj4js.org>
 * 
 * See User Guide at <http://trac.osgeo.org/proj4js/wiki/UserGuide>
 * 
 * http://depot.ign.fr/geoportail/api/js/1.3/lib/proj4js/lib/defs/
 */

// TODO : a verifier car defs un peu differentes de infoterre

/**
 * Class: Proj4js
 * Constants:
 * defs - {Array({String})} array of proj4 definitions
 * cf application.proj4jsdefs.js d'InfoTerre.
 * 
 * See Spatial References at http://spatialreference.org
 * 
 * See Proj4Js at http://www.proj4js.org
 * 
 * 
 * *Example*
 * (start code)
 * // France : Lambert 2 etendu, Lambert 93
 * Proj4js.defs["EPSG:27572"] = "+proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs";
 * Proj4js.defs["EPSG:27582"] = "+proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs";
 * Proj4js.defs["EPSG:2154"] = "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
 * (end)
 */

Proj4js.defs["EPSG:27572"] = "+proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs";
Proj4js.defs["EPSG:27582"] = "+proj=lcc +lat_1=46.8 +lat_0=46.8 +lon_0=0 +k_0=0.99987742 +x_0=600000 +y_0=2200000 +a=6378249.2 +b=6356515 +towgs84=-168,-60,320,0,0,0,0 +pm=paris +units=m +no_defs";
Proj4js.defs["EPSG:32620"] = "+proj=utm +zone=20 +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32622"] = "+proj=utm +zone=22 +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32738"] = "+proj=utm +zone=38 +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32740"] = "+proj=utm +zone=40 +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32758"] = "+proj=utm +zone=58 +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32705"] = "+proj=utm +zone=5 +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:32706"] = "+proj=utm +zone=6 +south +ellps=WGS84 +datum=WGS84 +units=m +no_defs";
Proj4js.defs["EPSG:2154"] = "+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:2989"] = "+proj=utm +zone=20 +ellps=WGS84 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:2972"] = "+proj=utm +zone=22 +ellps=GRS80 +towgs84=2,2,-2,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:2975"] = "+proj=utm +zone=40 +south +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:2984"] = "+proj=lcc +lat_1=-20.66666666666667 +lat_2=-22.33333333333333 +lat_0=-21.5 +lon_0=166 +x_0=400000 +y_0=300000 +ellps=intl +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";
Proj4js.defs["EPSG:3296"] = "+proj=utm +zone=5 +south +ellps=GRS80 +units=m +no_defs";
Proj4js.defs["EPSG:3297"] = "+proj=utm +zone=6 +south +ellps=GRS80 +units=m +no_defs";
Proj4js.defs["EPSG:4471"] = "+proj=utm +zone=38 +south +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs";

Proj4js.defs["IGNF:MART38UTM20"]="+title=Martinique Fort-Desaix +proj=tmerc +towgs84=126.9260,547.9390,130.4090,-2.7867,5.1612,-0.8584,13.822650 +a=6378388.0000 +rf=297.0000000000000 +lat_0=0.000000000 +lon_0=-63.000000000 +k_0=0.99960000 +x_0=500000.000 +y_0=0.000 +units=m +no_defs";
Proj4js.defs["IGNF:GUAD48UTM20"]="+title=Guadeloupe Ste Anne +proj=tmerc +towgs84=-472.2900,-5.6300,-304.1200,0.4362,-0.8374,0.2563,1.898400 +a=6378388.0000 +rf=297.0000000000000 +lat_0=0.000000000 +lon_0=-63.000000000 +k_0=0.99960000 +x_0=500000.000 +y_0=0.000 +units=m +no_defs";
Proj4js.defs["IGNF:RGFG95"]="+title=Reseau geodesique francais de Guyane 1995 +proj=geocent +towgs84=0.0000,0.0000,0.0000 +a=6378137.0000 +rf=298.2572221010000 +units=m +no_defs";
Proj4js.defs["IGNF:RGM04"]="+title=RGM04 (Reseau Geodesique de Mayotte 2004) +proj=geocent +towgs84=0.0000,0.0000,0.0000 +a=6378137.0000 +rf=298.2572221010000 +units=m +no_defs";
Proj4js.defs["IGNF:RGR92"]="+title=Reseau geodesique Reunion 1992 +proj=geocent +towgs84=0.0000,0.0000,0.0000 +a=6378137.0000 +rf=298.2572221010000 +units=m +no_defs";
Proj4js.defs["IGNF:RGNC"]="+title=Reseau Geodesique de Nouvelle-Caledonie +proj=geocent +towgs84=0.0000,0.0000,0.0000 +a=6378137.0000 +rf=298.2572221010000 +units=m +no_defs";
Proj4js.defs["IGNF:RGPF"]="+title=RGPF (Reseau Geodesique de Polynesie Francaise) +proj=geocent +towgs84=0.0000,0.0000,0.0000 +a=6378137.0000 +rf=298.2572221010000 +units=m +no_defs";
