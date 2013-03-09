#!/bin/bash

#####################Variables a adapter #######################################
# parametres de connexion de la base de donnée de travail
DBCONN="-d bva -p 5432"

UU_CODE=$1


run_shp2pgsql() {
	if [ -f $3 ] ; then
		shp2pgsql -iISD$1 -W LATIN1 -s 27572 -geom $3 $2 |psql ${DBCONN}
	fi
}


mkdir /tmp/${UU_CODE}_Merge 2> /dev/null
cd /tmp/${UU_CODE}_Merge

unrar x -o+ /mnt/data/UU-2010/UU_100/${UU_CODE}/${UU_CODE}.rar

MODE=d

TABLE="trajet_2010p1"
FILE="trajetssimul${UU_CODE}p1.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}

FILE="Trajetssimul${UU_CODE}p1.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}


MODE=a

TABLE="trajet_2010p2"
FILE="trajetssimul${UU_CODE}p2.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}

FILE="Trajetssimul${UU_CODE}p2.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}


TABLE="trajet_2010p3"
FILE="trajetssimul${UU_CODE}p3.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}

FILE="Trajetssimul${UU_CODE}p3.shp"
run_shp2pgsql ${MODE} ${TABLE} ${FILE}

cd $OLDPWD

