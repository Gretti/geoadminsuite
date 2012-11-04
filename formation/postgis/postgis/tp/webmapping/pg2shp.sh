#!/bin/sh

# Les tables a exporter (juste un sous ensemble de toutes les tables des données de test)
TABLES="
point_eau
poste_transformation
pylone
reservoir
reservoir_eau
route
route_nommee
route_primaire
route_secondaire
spatial_ref_sys
surface_activite
surface_eau"

for t in $TABLES; do
	echo traitement de la table $t
	pgsql2shp -f $t.shp test_ign "select * from $t where st_intersects(geometry, setSrid('BOX(915614 6454634, 917498 6456110)'::box2d, 2154))"
	echo fichier $t.shp généré...

done;

echo fin de la conversion.

