#!/bin/sh
################################################################################
# Script de traitement des données en erreur:

# Decompression du zip des trajets 2009, du zip des trajets initiaux, 
# chargement dans une base de travail
# analyse des messages d'erreur: soit les stats, soit l'echec pgrouting
# rafraichissement dans OpenJump et affichage
################################################################################

if [[ $# -lt 2 ]] ; then
	echo usage: "$0 <repertoire contenant le zip des trajets a traiter> <code UU a traiter>"
	exit 1
fi


# analyse des erreurs pour identifier les trajets
# si ecart_longueur.csv

# si process log file:

# decompression du zip des trajets 2009
cd $1
echo "decompression et mise en BD des trajets 2009 pour l'UU: $2"
unzip -q uu_${2}.zip -d toto
cd toto 
shp2pgsql -IidDS -g geom -s 27572 trajet_2009.shp tko | psql bva

cd ../$2
echo "mise en BD des trajets initiaux pour l'UU: $2"
shp2pgsql -IidDS -g geom -s 27572 trajetssimul.shp tinit | psql bva
cd ..

# chargement des stats ecart_longueur dans une nouvelle table pour sortir les trajets
# en erreur
psql -d bva -c "truncate el"
psql -d bva -c "copy el from '/Users/nicolas/Projets/BVA/toto/ecart_longueur.csv' WITH (FORMAT CSV, HEADER TRUE)"
TRAJ_LIST=`psql -At -d bva -c "select sum(''''||idtraj || '''' || ',') from el where lgtron2009 > 600 and abs(diff_longueur) > 10"`

# nettoyage
rm -rf toto

#echo "conservation des trajets KO: $TRAJ_LIST"

# suppression des trajets OK: la chaine vide '' a la fin est la car traj_list contient une virgule terminale
# TODO: SQL substring pour supprimer cette virgule terminale.
psql -d bva -c "delete from tko where idtraj not in ($TRAJ_LIST '')"

# filtrage des trajets initiaux pour ne garder que ceux en erreur
psql -d bva -c "delete from tinit where iddepl not in ($TRAJ_LIST '')"

#table des voiries associées a ces trajets
psql -d bva -c "drop table if exists vw"
psql -d bva -c "create table vw as
                select distinct t.iddepl, v.gid, v.link_id, v.ref_in_id, 
                       v.nref_in_id, v.source, v.target, v.shape_len as shape_length, v.geom
               from tinit t, voirie v
               where st_dwithin(v.geom, t.geom, 500)"
 
# table des trajets KO full
psql -d bva -c "drop table if exists tko_full"
psql -d bva -c "create table tko_full as 
                select idtraj, sum(lgtron2006) as lgtron2006, sum(lgtron2009) as lgtron2009, count(ordre) as numtroncon, 
                st_linemerge(st_collect(geom)) as geom
                from (select * from tko order by idtraj, ordre) as foo
                group by idtraj"

psql -d bva -c "drop table if exists tinit_full"
psql -d bva -c "create table tinit_full as 
                select iddepl, sum(lgtron) as lgtron, count(ordre) as numtroncon, 
                st_linemerge(st_collect(geom)) as geom
                from (select * from tinit order by iddepl, ordre) as foo
                group by iddepl"

echo "______________________________"          
echo "trajets KO prets pour analyse"
echo "requetes OJ pour affichage:"
echo "select * from tko;"
echo "select * from tko_full;"
echo "select * from tinit;"
echo "select * from tinit_full;"
echo "select * from vw;"
echo "requete pgRouting pour analyse:
select edge_id, vertex_id, cost, null, nextval('tmp_seq') from shortest_path(
'select gid as id, source::int4, target::int4, shape_length::float8 as cost from vw where idtraj = ''$2'' ',
source,
target,
false,
false);
ou source et target sont les noeuds de depart et d'arrivée.
"

echo
exit 0

