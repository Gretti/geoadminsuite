-- 1) -------------------------------------------------------------------------------------------
-- eventuel renommage des champs des tables: coherence des données
alter table trajet rename iddepl to idtraj;
alter table trajet rename lgtron to lgtron2006;
-- cette colonne peut etre vide: forcage de la valeur avec un update => super plus long
update trajet set lgtron2006=st_length(geom);

create index trajet_idtraj on trajet(idtraj);
create index trajet_ordre on trajet(ordre);
create index trajet_code_gs on trajet(code_gs);

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
