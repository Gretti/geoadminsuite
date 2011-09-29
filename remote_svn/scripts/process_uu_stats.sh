#!/bin/bash
# Script d'affichage des stats sur le process de recalage des trajets.
# 
# Affichage les stats suivantes:
# 	- Nombre d'UU traitées et OK
#   - stats sur les UU traitées.
# 	- Nombre d'UU traitées et KO
# 	- liste des UU KO et raison de l'echec
# 	- UU en cours de traitement: date de debut, duree du processus
# 	- Nbre d'UU restant a traiter
# 
# historique
# 	24/07/2011: creation du script

# nom de la base de donnée de travail
DBCONN="-d bva -p 5432"

echo 
echo "____________________________________________________________________________"
echo "statistiques sur la machine courante:"
echo

RES_OK=`psql -At $DBCONN -c "select count(*) from stats where status_ok"`
echo  "Nombre d'UU traitees et OK: $RES_OK"
echo

RES_KO=`psql -At $DBCONN -c "select count(*) from stats where not status_ok"`
echo  "Nombre d'UU traitees et KO: $RES_KO"
echo

if [[ $RES_OK -gt 0 ]]; then
	echo "____________________________________________________________________________"
	echo  "Liste des UU traitees et ok"
	psql   --pset=pager=off $DBCONN -x -c "select * from stats where status_ok"
	echo "____________________________________________________________________________"
	echo
fi

if [[ $RES_KO -gt 0 ]]; then
	echo "____________________________________________________________________________"
	echo  "Liste UU KO:"
	psql  $DBCONN -c "select code_uu, description from stats where not status_ok"
	echo "____________________________________________________________________________"
	echo
fi

RES=`psql -x $DBCONN -c "select 'code uu: ' || code_uu, ' demarre a: ' || proc_start, ' duree: ' || (now() - proc_start) from stats where proc_start is not null and proc_end is null"`
if [[ $RES ]]; then
	echo  "UU en cours de traitement:"
	# on est fou, on refait la requete ...
	psql -x $DBCONN -c "select code_uu, proc_start, (now() - proc_start) as duree from stats where proc_start is not null and proc_end is null"
	echo
fi
