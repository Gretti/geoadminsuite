<?php

// la requete recuperant les objets en GeoJson: le contenu de l'objet "geometry" est renvoyé
$query = "select st_asGeoJson(st_transform(geom, 4326)) as geojson from departement";
$dbh = new PDO('pgsql:dbname=tp;host=localhost', 'nicolas', 'password');
// la liste des objets JSON recus depuis la base de données
$geoObj = array();

foreach ($dbh->query($query) as $row) {
    // stockage des strings JSON dans un tableau
    array_push($geoObj, '{"geometry": ' . $row["geojson"] . "}");
}

// preparation de l'objet JSON attendu par OpenLayers.
$geojson = '{"type": "FeatureCollection", "features": [' . implode(", ", $geoObj) . ']}';

// preparation du type de retour attendu par OpenLayers
header('Content-type: application/json');
// ecriture de la reponse
echo $geojson;
?>