#!/bin/bash

################################################################################
# script de traitement des trajets sur les voiries
# a lancer par un utilisateur admin de la base de donnees sur laquelle le travail
# doit se faire.
# ex: ./process_trajets.sh ../data/meaux_extrait/trajet_meaux_extrait.shp ../data/meaux_extrait/voirie_meaux_extrait.shp /Users/nicolas/Projets/BVA/data/tmpout true
# historique:
# juillet 2011: enchainement de plusieurs scripts, au lieu d'un seul gros.
# codes d'erreur :
# exit 1: mauvais parametres du script
# exit 100: les trajets ne constituent pas des LINESTRING mais des MULTILINESTRING. leur nombre est trop
#            important: process impossible pour cette UU
# exit 200: shp2pgsql ne peut etre lancŽ.
# exit 201: fichier de ponderations ne peut etre charge dans la table pg.

################################################################################

#Variables a editer pour refleter la conf actuelle
DBPORT=5432
#DBNAME=bvameaux
DBNAME=bva

#AWK_CMD=gawk
AWK_CMD=awk

# C'est plus d'actualité ca
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
shp2pgsql -iISDd -W LATIN1 -s 27572 -g geom $1 trajet | psql -p $DBPORT -d $DBNAME

# if [ ${PIPESTATUS[1]} -eq 1 ] ; then
# 	echo "chargement des trajets: $1 impossible, fin du processus..."
# 	exit 200
# fi
echo "    trajets charges"

END="$(date +%s)"
DELTAT="$(expr $END - $BEGIN)"
echo "temps d'execution: $DELTAT s."


BEGIN="$(date +%s)"
echo "__________________________________________________________________________"
echo "chargement de la ponderation..."
psql -p $DBPORT -d $DBNAME -f ../SQL/init_ponderations.sql

# Filtre du fichier de ponderations avec les colonnes qui nous interesse
#$AWK_CMD '{print $24";"$25}' $3 > $3.csv
#echo "COPY base_individus FROM '$3.csv' WITH CSV HEADER DELIMITER ';'" | psql -p $DBPORT -d $DBNAME;

# NRT: 04/10/2011: Changement de programme: on charge le fichier complet en base:
# la commande awk precedente ne prend pas en compte des valeurs vides dans le fichier => decalage
# dans le parsing.
# => on charge tout, on vire les colonnes qui ne nous interessent pas.
if [[ -f $3 ]] ; then
    psql -p $DBPORT -d $DBNAME -c "COPY base_individus FROM '$3' WITH CSV HEADER DELIMITER E'\t'"
fi

if [ $? -eq 1 ] ; then
    echo "chargement de la ponderation: $3 impossible, le traitement continu sans la mise à jour des pondérations!"
else
    # Mise a jour des ponderations dans trajet
    psql -p $DBPORT -d $DBNAME -f ../SQL/update_ponderations.sql
    echo "    ponderations chargees"

    END="$(date +%s)"
    DELTAT="$(expr $END - $BEGIN)"
    echo "temps d'execution: $DELTAT s."
fi

if [[ ! -f $4 ]] ; then
    echo "Le fichier des trajets 2010 n'existe pas : $4"
    echo "Le traitement continu sans les trajets 2010"
else
    BEGIN="$(date +%s)"
    echo "__________________________________________________________________________"
    echo "chargement des trajets 2010..."
    shp2pgsql -iISDd -W LATIN1 -s 27572 -g geom $4 trajet_2010 | psql -p $DBPORT -d $DBNAME

    # if [ ${PIPESTATUS[1]} -eq 1 ] ; then
    # 	echo "chargement des trajets: $1 impossible, fin du processus..."
    # 	exit 200
    # fi
    echo "    trajets 2010 charges"

    END="$(date +%s)"
    DELTAT="$(expr $END - $BEGIN)"
    echo "temps d'execution: $DELTAT s."
fi

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


# NRT: 17/08/2011: ajout du controle de validitŽ des trajets full: 
# si plus de 10 trajets full sont des multilinestrings, erreur des donnŽes: pas de lien
# 1-1 entre trajets et idtraj.
# si 1 a 9 MULTI => warning en base et traitement continu

echo 
echo "VERIFICATION des trajets en entree: la table trajet_full ne doit contenir que
des LINESTRING, pas des MULTILINESTRING"
NUM_MULTI=`psql -p $DBPORT -d $DBNAME -At -c "select count(*) from trajet_full where geometrytype(geom)='MULTILINESTRING'"`

if [ $NUM_MULTI -ge 10 ] ; then
    echo "ERROR: DONNEES DE TRAJET INVALIDES: impossible de reconstruire des linestrings a partir des trajets"                  
    exit 100
elif [ $NUM_MULTI -gt 0 ] && [ $NUM_MULTI -lt 10 ] ; then
   echo "WARNING: Une partie des trajets en entree est INVALIDE: MULTILINESTRING reconstruites au lieu de LINESTRING"
fi

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

if [[ -f $4 ]] ; then
    BEGIN="$(date +%s)"
    echo "__________________________________________________________________________"
    echo "lancement du traitement spatial: injection des trajets 2010..."
    psql -p $DBPORT -d $DBNAME -f ../SQL/injection_trajet_2010.sql
    echo "    traitement spatial termine"
    END="$(date +%s)"
    DELTAT="$(expr $END - $BEGIN)"
    echo "temps d'execution injection trajets 2010: $DELTAT s."
fi

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


