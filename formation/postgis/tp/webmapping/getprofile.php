<?php

// la requete réalisant l'intersection de la ligne dessinée avec le raster
// la ligne est ensuite "remise a plat" en utilisant des fonctions de referencement
// lineaire: chaque point de la ligne initiale est placée sur une ligne imaginaire
// de longueur egale a la ligne initiale, a la bonne abscisse
$q = <<<EOD
with ln as (
    -- fabrication de la ligne a partir du format geoJson
    select st_setSRID(st_geomFromGeoJson('%line%'), 2154)
    as geom
), 
inter as (
-- intersection du raster avec la ligne
	SELECT ST_Intersection(ln.geom, mnt.rast) as gv 
    from mnt, ln where 
    ST_Intersects(ln.geom, mnt.rast)
), 
-- fabrication des points 3D avec uniquement le premier point de chaque ligne:
-- correspond a l'intersection de la ligne avec un pixel.
points as (
    SELECT st_setSRID(st_makePoint(st_X(st_startPoint((gv).geom)), st_Y(st_startPoint((gv).geom)), (gv).val), 2154) as geom, (gv).val 
    from inter

)
-- fabrication des coordonnées finales, 
select (st_line_locate_point(ln.geom, pt.geom) * st_length(ln.geom))/1000 as x, st_Z(pt.geom)  as z
from ln, points as pt
where ln.geom is not null and pt.geom is not null;
EOD;

// ATTENTION: utiliser plutot un preparedStatement pour eviter les injections SQL
$query = str_replace('%line%', $_GET["line"], $q);


$dbh = new PDO('pgsql:dbname=tp;host=localhost', 'nicolas', 'password');

// la liste des objets JSON recus depuis la base de données
$points = array();

foreach ($dbh->query($query) as $row) {
    // stockage des points du profil dans une string.
    // il serait plus rapide de remplir un vrai tableau PHP, peut etre...
    array_push($points, "[" . $row["x"] . "," . $row["z"] . "]");
}

// concatenation du tableau en string, séparé par des virgules
$pointsAsStr = implode(", ", $points);
// preparation de l'objet JSON attendu par OpenLayers.
$geojson = '[[' . $pointsAsStr. ']]';

//$geojson = "[[[1, 1],[2, 2],[3, 3],[4, 4],[5, 5]]]";
// preparation du type de retour attendu par OpenLayers
header('Content-type: application/json');
// ecriture de la reponse
echo $geojson;
?>