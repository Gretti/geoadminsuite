-- script de creation de la table de stats utilisée pour le recalage des trajets
drop table if exists stats;
create table stats (
id serial primary key,
code_uu text unique,
proc_start timestamp,
proc_end timestamp,
description text,
status_ok boolean,
nb_trajets_initiaux int,
nb_trajets_2009 int,
nb_trajets_rejet int,
sum_2009_rejet int,
sum_lgtron2006 float,
sum_lgtron2009 float,
sum_lg_rejet float,
nb_trajets_courts int,
nb_trajets_pgrouting int,
prc_long float,
nb_trajets_ko_pgrouting int
);
