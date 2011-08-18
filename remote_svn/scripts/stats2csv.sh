#!/bin/sh

# exports stats into CSV

CURD=` date +'%Y%m%d_%H_%M'`
psql bva -c "copy (select code_uu, proc_start, proc_end, nb_trajets_initiaux, sum_lgtron2006, prc_long, extract('epoch' from (proc_end - proc_start)) as duree_en_sec from stats where status_ok)  to '/mnt/data/results/latest_stats_s2_$CURD.csv' with (format CSV, header true)"

if [ $? -eq 0 ]; then
    echo "statistiques a la date du ${date} exportées dans le fichiers: /mnt/data/results/latest_stats_s2_$CURD.csv"
else 
    echo "Export CSV en erreur"
fi
