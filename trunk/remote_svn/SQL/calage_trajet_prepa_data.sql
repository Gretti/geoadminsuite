-- 1) -------------------------------------------------------------------------------------------
-- eventuel renommage des champs des tables: coherence des données
alter table trajet rename iddepl to idtraj;
alter table trajet rename lgtron to lgtron2006;
-- cette colonne peut etre vide: forcage de la valeur avec un update => super plus long
update trajet set lgtron2006=st_length(geom);

-- change le type de la colonne ID et le force en int: plus pratique et plus rapide a traiter.
alter table trajet alter column id type int using id::int;

create index trajet_idtraj on trajet(idtraj);
create index trajet_ordre on trajet(ordre);
create index trajet_code_gs on trajet(code_gs);
create index trajet_id on trajet(id);

-- creation d'une table contenant uniquement le code de l'uu courante
drop table if exists current_uu;
create table current_uu as select code_uu from stats where proc_start is not null and proc_end is null order by proc_start desc limit 1;
create index current_uu_code_uu on current_uu(code_uu);

-- creation d'une table ident_new pour chaque uu a des vocation optimisation
drop table if exists ident_new_uu;
create table ident_new_uu as select idnew.uu99, idnew.id, idnew.id_new, idnew.aresimuler from ident_new idnew, current_uu cur_uu where idnew.uu99=cur_uu.code_uu;
create index ident_new_uu_id_new on ident_new_uu(id_new);
-- create index ident_new_uu_uu99 on ident_new_uu(uu99);
-- create index ident_new_uu_aresimuler on ident_new_uu(aresimuler);

-- mise a jour de la colonne id avec le bon identifiant
update trajet t set id = n.id_new 
from ident_new_uu n 
where t.id = n.id and n.aresimuler=0;

vacuum analyse trajet;

-- trajet_full
drop table if exists trajet_full;
create table trajet_full as
select idtraj, sum(lgtron2006) as lgtron2006, count(ordre) as numtroncon, st_linemerge(st_collect(geom)) as geom
from (select * from trajet order by idtraj, ordre) as foo
group by idtraj;

create index trajet_full_geom on trajet_full using gist(geom);
create index trajet_full_idtraj on trajet_full (idtraj);
create index trajet_full_numtroncon on trajet_full (numtroncon);

-- ici, optimisation possible en virant les trajets identiques (cf  calage_trajet_final_10.sql)
