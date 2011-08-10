#!/bin/sh

################################################################################
# script de traitement des trajets sur les voiries
# a lancer par un utilisateur admin de la base de donnees sur laquelle le travail
# doit se faire.
# ex: ./process_trajets.sh ../data/meaux_extrait/trajet_meaux_extrait.shp ../data/meaux_extrait/voirie_meaux_extrait.shp /Users/nicolas/Projets/BVA/data/tmpout true
# historique:
# juillet 2011: enchainement de plusieurs scripts, au lieu d'un seul gros.
################################################################################

#Variables a editer pour refleter la conf actuelle
DBPORT=5432
#DBNAME=bvameaux
DBNAME=bva

if [ $# -lt 2 ] ; then
	echo usage: "$0 <shape des trajets> <shape des voiries> <repertoire de sortie> <true|false>"
	echo "    <shape des trajets>: le chemin vers le shapefile des trajets"
	echo "    <repertoire de sortie>: le chemin ABSOLU vers le repertoire ou ecrire les resultats"
	exit 1
fi

BEGIN="$(date +%s)"
T0="$(date +%s)"

#echo "__________________________________________________________________________"
#echo "traitement commence a: $(date) sur la base $DBNAME"

#echo "__________________________________________________________________________"
#echo "recration de la base..."
#dropdb -p $DBPORT $DBNAME
#createdb -p $DBPORT -T template_postgis $DBNAME

#echo "__________________________________________________________________________"
#echo "chargement des fonctions custom..."
#psql -p $DBPORT -d $DBNAME -f functionAvgDistance.sql
#psql -p $DBPORT -d $DBNAME -f functionCleanVoirieGraph.sql
#psql -p $DBPORT -d $DBNAME -f functionProcessTrajet.sql

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "chargement des trajets..."
shp2pgsql -iISDd -W LATIN1 -s 27572 -g geom $1  trajet | psql -p $DBPORT -d $DBNAME

if [[ $PIPESTATUS -eq 1 ]] ; then
	echo "chargement des trajets: $1 impossible, fin du processus..."
	exit 1
fi
echo "    trajets charges"

END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution: $DELTAT s."


#BEGIN="$(date +%s)"
#echo "__________________________________________________________________________"
#echo "chargement des voiries..."
#shp2pgsql -iISD -W LATIN1 -g geom $2 voirie | psql -p $DBPORT -d $DBNAME
#echo "    voiries chargees"

#END="$(date +%s)"
#DELTAT="$(expr $END - $BEGIN)"
#echo "temps d'execution: $DELTAT s."


BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: fonctions..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_fonctions.sql
psql -p $DBPORT -d $DBNAME -f ../SQL/functionRouteTrajet.sql
psql -p $DBPORT -d $DBNAME -f ../SQL/functionAddTrajet2009Cols.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution fonctions: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: prepa data..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_prepa_data.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution prepa data: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: Voirie..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_voirie.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution voirie: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: NVQ..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_nvq_ok.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution NVQ ok: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: startend voirie..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_startend_voirie.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution startend voirie: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: node voirie..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_node_voirie.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution node voirie: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: process buildTrajet..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_process.sql
echo "    traitement spatial termine buildTrajet"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution process: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: process pgRouting..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_routing.sql
echo "    traitement spatial termine buildTrajet"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution process: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: calcul sens..."
psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_sens.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution calcul sens: $DELTAT s."

BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "lancement du traitement spatial: stats finales..."
#stockage des stats en base, pour le traitement en masse
#psql -p $DBPORT -d $DBNAME -f ../SQL/calage_trajet_stats.sql
psql -p $DBPORT -d $DBNAME -v "code_uu=$3" -f ../SQL/calage_trajet_stats_in_table.sql
echo "    traitement spatial termine"
END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution stats finales: $DELTAT s."


BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "export des resultats en shapefile"
sh export_shp.sh $2
echo "    resultats exportes en shapefile"

END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution: $DELTAT s."

echo "__________________________________________________________________________"
echo "temps total: $(expr $END - $T0) s."
echo "traitement termine a:  $(date)"


