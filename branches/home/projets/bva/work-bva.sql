--------------------------------------------------------------------------------
----------------------------------- 17 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- Chargement de la nouvelle table des id/id_new issue du fichier:
-- /mnt/data/UU-ParisMeaux - En attente/trajets.dbf
-- shp2pgsql...
-- une entree par trajet, avec poids.

-- table de correspondance id_id_new/poids:
-- distinct on id pour avoir des id uniques: le premier id_new, poids correspondant 
-- est pris.

create table correspid as
select distinct on (id) id, id_new, poids
from newids;

-- entrees

create index correspid_id_idx on correspid (id);
vacuum analyse correspid;

-- verif des correspondances entre idmenage et id
select t.idtraj, t.ordre, t.idmenage, n.id, n.id_new, n.poids
from (select * from tj_final limit 1000 offset 100000) as t,
    correspid n
where n.id = t.idmenage;


--------------------------------------------------------------------------------
----------------------------------- 18 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- ajout de la colonne poids
alter table tj_final add column poids double precision;
alter table tj_final add column new_modetr text;

-- mise a jour de la table tj_final direct.
update tj_final t set 
    idmenage = n.id_new,
    poids = n.poids,
    new_modetr = n.modetr
from correspid n
where t.idmenage = n.id;

-- verification des données:
select idtraj, ordre, idmenage, numdepl, numtrajet, modetr, new_modetr
from (select * from tj_final limit 1000 offset 200000) as t 
order by idtraj, ordre;

select count(*) from tj_final where modetr = null;
select count(*) from tj_final where new_modetr = null;
select count(*) from tj_final where poids = null;
select count(*) from tj_final where idmenage = null;

-- controle du
-- table des sens a 0
create table sens0 as select * from tj_final where sens = 0;

-- controle trajet TC
select idtraj, ordre, idmenage, modetr, new_modetr
from tj_final 
where modetr = 5 or modetr = 6;

-- injection des trajets tc  
-- maj des id avant.
alter table trajets_ add column idmenage int;
alter table trajets_ add column poids double precision;
alter table trajets_ add column new_modetr text;

update trajets_ t set 
    idmenage = n.id_new,
    poids = n.poids,
    new_modetr = n.modetr
from correspid n
where t.idmenage = n.id;

--injection dans la table finale:
insert into tj_final (idtraj, iddepl, modetr, sens, 
    code_gs, ordre, idmenage, numdepl, numtrajet, poids)
select idtraj, iddepl, mode as modetr, 
    sens, linkid as code_gs, ordre, idmenage, numdepl, numtrajet, poids
from trajets_;


--------------------------------------------------------------------------------
----------------------------------- 19 octobre 2012 ----------------------------
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
----------------------------------- 23 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- reimport des trajets recalés initiaux pour verifier les id tronquees.
-- cf shell pour remonter les trajets.

-- nouvelle structure de table pour représenter un individu, un deplacement, un trajet, un troncon.

create table individu (
    id serial primary key,
    idmenage int unique not null
);

create table deplacement (
    id serial primary key,
    iddepl int unique not null,
    id_individu int foreign key references individu(id)
);

create table trajet (
    id serial primary key,
    idtrajet int unique not null,
    iddepl int foreign key references deplacement(id),
    modetr smallint not null
);

create table troncon (
    id serial primary key,
    idtrajet int foreign key references trajet(id),
    sens smallint not null,
    linkid bigint not null 
    geom geometry(linestring, 2143)
);

-- creation/remplissage des tables
insert into individu (idmenage) select distinct id from trajets_paris_2009;


--------------------------------------------------------------------------------
----------------------------------- 25 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- nombre d'idtraj distinct:
select count(distinct idtraj) from trajets_dat  ;
-- 223817 trajets
select count(distinct idtraj) from trajets_paris_2009   ;
-- 223540 trajets
select count(distinct iddepl) from trajets_dat  ;
-- -> 147148
select count(distinct substring(idtraj from '^[0-9]*_')) from trajets_dat  ;
-- -> 36187

-- mise a jour des trajets pour le numtrajet inexistant
update trajets_paris_2009 t set numtrajet = 0
where t.idtraj like E'%\\_';
-- -> 20174 updated: ok

-- verif des modetr:
create index trajets_retour_idtraj_
id on trajets_retour(idtraj);

drop table if exists dist_trajets;
create table dist_trajets as select distinct idtraj, modetr from trajets_dat;
create index dist_trajets_idtraj_id on dist_trajets(idtraj);

--select t.idtraj, t.modetr, d.idtraj, d.modetr
select count(t.idtraj)
from dist_trajets t, trajets_retour d
where t.idtraj = d.idtraj
and d.compar_200 = 0 limit 1000;

-- injection des trajets tronqués.
-- shp2pgsql -iD  aggr_2005_id_dep_trajetunique.dbf aggr_trajets | psql 

-- analyse des trajets tronques:
select t.idtraj, t.id, t.numdepl, t.numtrajet, a.id, a.numdepl, a.numtrajet, a.numtraje_a
from trajets_tronq t, aggr_trajets a
where t.id = a.id and t.numdepl::int = a.numdepl;

select count(distint idtraj, id, iddepl) from trajets_tronq;

-- mise a jour des trajets tronqués et des modetr
-- en creant une nouvelle table:
-- modetr  a prendre depuis trajets.dat pour tout le monde
-- UNION new idtraj pour les tronqués

create index aggr_trajets_id_idx on aggr_trajets (id);
create index aggr_trajets_numdepl_idx on aggr_trajets (numdepl);
create index trajets_paris_2009_id_idx on trajets_paris_2009(id);
create index trajets_dat_idtraj_idx on trajets_dat(idtraj);

drop table tp092;
create table tp092 as (
select distinct t.idtraj,
 t.ordre, t.code_gs , t.lgtron2009 , t.sens, t.numdepl, t. numtrajet,
 d.modetr::text, t.id, t.iddepl, t.poids, t.geom       
from trajets_paris_2009 t, trajets_dat d
where t.idtraj not like E'%\\_'
and t.idtraj = d.idtraj

UNION 
select distinct a.id||'_'||a.numdepl||'_'||a.numtrajet as idtraj,
 t.ordre, t.code_gs , t.lgtron2009 , t.sens, t.numdepl, t.numtrajet,
 a.mode_min as modetr, t.id, t.iddepl, t.poids, t.geom       
from trajets_paris_2009 t, aggr_trajets a
where t.idtraj like E'%\\_'
and t.id = a.id and t.numdepl::int = a.numdepl
);



update trajets_paris_2009 t set modetr = d.modetr
from trajets_dat d, trajets_retour r
where t.idtraj = d.idtraj
and d.idtraj = r.idtraj
and r.compar_200 = 0;

update trajets_paris_2009 t set idtraj =  a.id||'_'||a.numdepl||'_'||a.numtrajet,
	numtrajet = a.numtrajet
from aggr_trajets a
where t.idtraj like '%\\_'
and t.id = a.id and t.numdepl::int = a.numdepl;

-- nombre de trajets restant tronqués:
select count(*) from trajets_paris_2009 where t.idtraj like '%\\_';

-- suppression des trajets restant tronqués:
delete from trajets_paris_2009 where t.idtraj like '%\\_';

-- controle sur les modetr: doivent etre les memes que ceux de trajets_dat
select count(*) 
from trajets_paris_2009 t, trajet_dat d
where t.idtraj = d.idtraj
and t.modetr <> d.modetr;
-- doit rester 0

------------------------------------------------------------------------------------------
-- Nouvelle structure de table indivudu, deplacement, trajet, troncons.
-- reimport des trajets recalés initiaux pour verifier les id tronquees.
-- cf shell pour remonter les trajets.

-- nouvelle structure de table pour représenter un individu, un deplacement, un trajet, un troncon.

create table individu (
    id serial primary key,
    idmenage int unique not null,
    poids float not null
);
create index individu_idmenage_id on individu(idmenage);

-- todo: remove in prod....
alter table individu add column poids float;
update individu i set poids = n.poids
from newids n where i.idmenage = n.id_new; 

drop table deplacement;
create table deplacement (
    id serial primary key,
    numdepl int not null,
    id_individu int references individu(id)
);
create index deplacement_id_individu_idx on deplacement(id_individu);


drop table trajet;
create table trajet (
    id serial primary key,
    numtrajet int not null,
    id_deplacement int references deplacement(id),
    modetr smallint not null,
    idtraj text not null -- a virer ensuite
);
create index trajet_id_individu_idx on trajet(id_deplacement);

drop table if exists troncon;
create table troncon (
    id serial primary key,
    id_trajet int references trajet(id),
    ordre int not null,
    sens smallint not null,
    linkid bigint not null,
    geom geometry
);

-- creation/remplissage des tables
-- individu
insert into individu (idmenage) select distinct id from trajets_paris_2009;
create index individu_idmenage_idx on individu (idmenage);

-- deplacement
insert into deplacement (numdepl, id_individu)
	select distinct t.numdepl::int, i.id
	from trajets_paris_2009 t, individu i
	where t.id = i.idmenage;
	
create index deplacement_iddepl_idx on deplacement(iddepl);

select count(*) from trajets_paris_2009 where numtrajet = '' or numtrajet is null;
-- -> 20174 trajets tronqués.

-- trajet
truncate trajet;
insert into trajet (idtraj, numtrajet, id_deplacement, modetr)
	select distinct on (idtraj) idtraj, 
	case when t.numtrajet = '' then 0 else t.numtrajet::int end, 
	d.id, t.modetr::smallint
	from trajets_paris_2009 t, deplacement d, individu i
	where t.id = i.idmenage
	and t.numdepl::int = d.numdepl
	and i.id = d.id_individu;

alter table trajet drop column idtraj;
alter table trajet add constraint id_depl_fk foreign key (id_deplacement) references deplacement (id);


-- troncon
--insert into troncon (id_trajet, ordre, sens, linkid, geom)
drop table troncon;
create table troncon as 
select  tj.id as id_trajet,
	t.ordre, t.sens, t.code_gs as linkid, t.geom
from trajets_paris_2009 t, trajet tj, individu i, deplacement d
where t.id = i.idmenage and i.id = d.id_individu
and t.numdepl::int = d.numdepl
and d.id = tj.id_deplacement and t.numtrajet::int = tj.numtrajet;
-- -> 9755887 ouf :)

alter table troncon add column id serial primary key;
alter table troncon alter column id_trajet set not null;
alter table troncon alter column ordre set not null;
alter table troncon alter column sens set not null;
alter table troncon alter column linkid set not null;

-- creation de la vue représentant la table de sortie BVA/AFFI
create view trajets_paris_2009_view as (
	select i.idmenage||'_'||d.numdepl||'_'||tj.numtrajet as idtraj,
		i.idmenage||'_'||d.numdepl as iddepl,
		i.idmenage as id,
		d.numdepl, tj.numtrajet, 
		--i.poids,
		tj.modetr, tr.ordre, tr.sens, tr.linkid as code_gs, st_length(tr.geom) as lgtron2009, tr.geom
	from individu i, deplacement d, trajet tj, troncon tr
	where i.id = d.id_individu and 
	d.id = tj.id_deplacement and 
	tj.id = tr.id_trajet
);
select count(*) from trajets_paris_2009_view  ;

-- creation de la vue des trajets (avec idtraj complet)
drop view trajet_view ;
create view trajet_view as (
	select i.idmenage||'_'||d.numdepl||'_'||t.numtrajet as idtraj,
		i.idmenage, d.numdepl, t.numtrajet, t.modetr
	from individu i, deplacement d, trajet t
	where i.id = d.id_individu and d.id = t.id_deplacement
);
select count(*) from trajet_view  ;
select * from trajet_view order by idtraj limit 1000 ;

-- mise a jour des modetr:
-- fabrication de la table des bons modetr pour chaque trajet: issu de la table trajets_dat.
drop table if exists goodmodetr;
create table goodmodetr as 
	select distinct id_new::int as idmenage, numdepl::int , substring(deplacemen from '^[0-9]*_([0-9]*)')::int as numtrajet,
	mode::int as modetr, poids 
	from trajets_dbf;
	
select count(*) from goodmodetr ;

create index goodmodetr_modetr_idx on goodmodetr (modetr);

-- comparaison avec la vue des trajets
--select v.idmenage, v.numdepl, v.numtrajet, g.idmenage, v.modetr, g.modetr::int
select count(*)
from goodmodetr g, trajet_view v
where g.idmenage = v.idmenage
and g.numdepl = v.numdepl
and g.numtrajet = v.numtrajet
and v.modetr <> g.modetr;
-- -> 39852

-- mise a jour des trajets pour le bon mode
update trajet t set modetr = g.modetr
from goodmodetr g, deplacement d, individu i
where g.idmenage = i.idmenage
and d.numdepl = g.numdepl
and st.numtrajet = g.numtrajet
and i.id = d.id_individu
and d.id = t.id_deplacement
and t.modetr <> g.modetr;


-- mise a jour des numtrajet pour les trajets ayant numtrajet = 0
select count(*) from trajet where numtrajet = 0;
-- -> 1001 - 959  = 42 trajets perdus

-- les bonnes valeurs sont dans le fichier aggr_trajets:
-- nombre de match found
update trajet t set numtrajet = a.numtrajet
from aggr_trajets a, deplacement d, individu i
where a.id = i.idmenage
and a.numdepl = d.numdepl
and t.numtrajet = 0
and i.id = d.id_individu
and d.id = t.id_deplacement;

-- Controle:
-- tous les modetr des trajets doivent correspondre avec trajets_dat
select * 
from trajet_view t, trajets_dat d
where t.idtraj = d.idtraj
and t.modetr <> d.modetr;
-- -> 11638

--------------------------------------------------------------------------------
----------------------------------- 07 novembre 2012 ----------------------------
--------------------------------------------------------------------------------
-- doublons dans trajet et/ou troncon pour les idtraj tronqués ?
select count(*) from trajet where idtraj = '10648_10_1';
select count(*) from trajet where idtraj = '10869_10_1';

select * from troncon where idtraj = '10648_10_1' order by ordre;
select * from troncon where idtraj = '10869_10_1' order by ordre;
