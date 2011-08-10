#!/bin/sh

DBPORT=5432
DBNAME=bva

pgsql2shp -p $DBPORT -f $1/trajet_2009.shp $DBNAME trajet_2009

pgsql2shp -p $DBPORT -f $1/trajet_rejet.shp $DBNAME trajet_rejet

# export de la table des stats de longueur en csv
psql -p $DBPORT -c "copy ecart_longueur to '$1/ecart_longueur.csv' WITH CSV HEADER" $DBNAME

#pgsql2shp -p $DBPORT -f $1/trajet_voirie_link.dbf $DBNAME trajet_voirie_link

#pgsql2shp -p $DBPORT -f $1/voirie_trajet_2009.shp $DBNAME voirie_trajet_2009



