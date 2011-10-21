#!/bin/bash

UU_CODE=$1

mkdir /tmp/${UU_CODE}_Merge
cd /tmp/${UU_CODE}_Merge

unrar x -o+ /mnt/data/UU-2010/UU_100/${UU_CODE}/${UU_CODE}.rar

shp2psgsql -iISDd -W LATIN1 -s 25572 -geom trajetssimul${UU_CODE}p1.shp trajet_2010 |psql -d bva -p 5432
shp2psgsql -iISDd -W LATIN1 -s 25572 -geom Trajetssimul${UU_CODE}p1.shp trajet_2010 |psql -d bva -p 5432

shp2psgsql -iISDa -W LATIN1 -s 25572 -geom trajetssimul${UU_CODE}p2.shp trajet_2010 |psql -d bva -p 5432
shp2psgsql -iISDa -W LATIN1 -s 25572 -geom Trajetssimul${UU_CODE}p2.shp trajet_2010 |psql -d bva -p 5432

shp2psgsql -iISDa -W LATIN1 -s 25572 -geom trajetssimul${UU_CODE}p3.shp trajet_2010 |psql -d bva -p 5432
shp2psgsql -iISDa -W LATIN1 -s 25572 -geom Trajetssimul${UU_CODE}p3.shp trajet_2010 |psql -d bva -p 5432

cd $OLDPWD

