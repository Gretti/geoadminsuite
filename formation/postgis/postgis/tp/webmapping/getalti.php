<?php

// la requete réalisant l'intersection de la ligne dessinée avec le raster
// la ligne est ensuite "remise a plat" en utilisant des fonctions de referencement
// lineaire: chaque point de la ligne initiale est placée sur une ligne imaginaire
// de longueur egale a la ligne initiale, a la bonne abscisse
$q = "select st_value(rast, 1, st_geomFromGeoJson('%POINT%')) as alti from mnt where st_intersects(rast, st_setSRID(st_geomFromGeoJson('%POINT%'), 2154))";

// ATTENTION: utiliser plutot un preparedStatement pour eviter les injections SQL
$query = str_replace('%POINT%', $_GET["point"], $q);

$dbh = new PDO('pgsql:dbname=tp;host=localhost', 'nicolas', 'password');

// la valeur retournee par la requete
$res = "";
foreach ($dbh->query($query) as $row) {
    // ici, une seule ligne retourné: iteration inutile...
    $res = $row["alti"];
}
// preparation du message a destination du client
$pt = json_decode($_GET["point"]);

// preparation du type de retour attendu par OpenLayers
header('Content-type: text/html');
// ecriture de la reponse
echo $msg = "l'altitude du point: " 
    . number_format($pt->coordinates[0], 0) . " - " 
    . number_format($pt->coordinates[1]) . " est: " . $res . " m";
?>