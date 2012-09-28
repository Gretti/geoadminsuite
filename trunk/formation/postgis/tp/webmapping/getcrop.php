<?php

// la requete réalisant l'intersection de la ligne dessinée avec le raster
// la ligne est ensuite "remise a plat" en utilisant des fonctions de referencement
// lineaire: chaque point de la ligne initiale est placée sur une ligne imaginaire
// de longueur egale a la ligne initiale, a la bonne abscisse
$q = "select encode(st_asPNG(ST_MapAlgebraExpr(st_union(st_clip(rast, st_setSRID(st_geomFromGeoJson(?), 2154))), 1, '8BUI', '[rast]')), 'base64') as clip  from mnt"
    . " where st_intersects(rast, st_setSRID(st_geomFromGeoJson(?), 2154))";

$dbh = new PDO('pgsql:dbname=tp;host=localhost', 'nicolas', 'password');
$stmt = $dbh->prepare($q);
$stmt->execute(array($_GET['polygon'], $_GET['polygon']));
$stmt->bindColumn(1, $lob, PDO::PARAM_LOB);
$stmt->fetch(PDO::FETCH_BOUND);

$lob = base64_decode($lob);

// preparation du type de retour attendu par le client
header('Content-type: image/png');

if (is_string($lob)) {
    echo $lob;
} else {
    fpassthru($lob);
}
?>