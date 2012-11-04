#!/bin/sh

#chemin vers les fichiers MIF/MID
$FILES=../data/38_BDTOPO_mif/

echo conversion de données mif/mid en postgis...

#encodage du client postgres pour la lecture des données
echo "forcage de l'encodage du client postgres..."
export PGCLIENTENCODING=LATIN1


for f in `find ../data/38_BDTOPO_mif/ -type f -iname "*.mid"`; do
	echo traitement du fichier $f
	ogr2ogr -a_srs EPSG:2154 -f PostgreSQL PG:"dbname='test_ign'" $f -lco FORMAT=MIF -lco GEOMETRY_NAME=geometry -overwrite
done;

echo fin de la conversion.

# les améliorations possibles: 
#	Charger les couches similaires dans une meme table, avec un nouvel attribut sur le type, par exemple
#	pour les routes.
