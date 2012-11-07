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

-- nettoyage de la bd
 drop table trajet_2009_ok, trajet_2009_tmp, trajet_2010, trajet_bu, trajet_full, trajet_rejet, trajet_routing, trajet_routing_ko, trajet_startend_voirie, trajets2009_05, tratest, voirie_codeok, voirie_work;

-- Chargement de la nouvelle table des id/id_new issue du fichier:
-- /mnt/data/UU-ParisMeaux - En attente/trajets.dbf
-- shp2pgsql...
-- une entree par trajet, avec poids.

-- table de correspondance id_id_new/poids:
-- distinct on id pour avoir des id uniques: le premier id_new, poids correspondant 
-- est pris.

drop table if exists correspid;
create table correspid as
select distinct on (id) id, id_new, mode as modetr, sens, navteq as linkid, poids
from newids;

create index correspid_id_idx on correspid (id);
vacuum analyse correspid;

-- verif des correspondances entre idmenage et id
select t.idtraj, t.ordre, t.idmenage, n.id, n.id_new, n.poids
from (select * from tj_final limit 1000 offset 100000) as t,
    correspid n
where n.id = t.idmenage
order by t.idtraj, t.ordre;

-- table de test pour vrifier le match
drop table if exists test;
create table test as select * from tj_final limit 15000;
create index test_id_idx on test(id);
vacuum analyse test;

select idtraj, idmenage from test;

-- mise a jour de la table
update test t set idmenage = n.id_new,
    modetr = n.modetr::double precision
    --poids = n.poids
from correspid n
where t.idmenage = n.id;

-- test de la requete finale.
select  gid, 
    iddepl || '_' || numdepl || '_' ||Ênumtrajet as idtraj,
    poids, code_gs, sens, lgtron2009 as lgtron_2009, numdepl, numtrajet, modetr, iddepl
from (
    select t.gid, 
       n.poids,
       t.ordre, t.code_gs, t.sens, t.lgtron2009, t.numdepl, t.numtrajet, n.modetr,
       t.idtraj, t.idmenage, n.id, n.id_new,
       case when n.id_new is null then t.idmenage else n.id_new end as iddepl
       from test t left join correspid n on (t.idmenage = n.id)
as foo;

-- mise a jour de la table:


IDTRAJ (djˆ prsent dans table)
ORDRE (djˆ prsent dans table)
CODE_GS (djˆ prsent dans table)
LGTRON_2009 (djˆ prsent dans table)
SENS (djˆ prsent dans table)
NUMDEPL (djˆ prsent dans table)
MODETR (djˆ prsent dans table)
NUMTRAJET (djˆ prsent dans table)
ID (djˆ prsent dans table)
IDDEPL (ˆ crer)


idtraj     | text                  | 
 ordre      | bigint                | 
 gid        | integer               | 
 code_gs    | double precision      | 
 lgtron2009 | numeric               | 
 sens       | integer               | 
 id         | double precision      | 
 modetr     | double precision      | 
 iddepl     | character varying(9)  | 
 modeprinc  | character varying(32) | 
 numdepl    | text                  | 
 numtrajet  | text                  | 
 poste      | character varying(32) | 
 lgtron2006 | numeric               | 
 pid        | numeric               | 
 geom       | geometry              | 
 sens_bva   | smallint              | 
 idmenage   | integer               | 


-- creation de la nouvelle table
create table trajets_paris2009 as (
select 
    -- idtraj
    idmenage || '_' || numdepl || '_' ||Ênumtrajet as idtraj,
    n.poids
from tj_final t, correspid n
where t.idmenage = n.id


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

-- verification des donnÃ©es:
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
select count(*) from tj_final  where new_modetr is null;

select mode, count(mode) from trajets_dat  group by mode order by mode

create index trajets_dat_linkid_idx on trajets_dat(linkid);

-- nombre de troncons dont le linkid est < au min de la voirie 2005:
select count(*) from trajets_dat where linkid < 56164354;

-- suppression des mauvais trajets TC
delete from tj_final  where modetr = 5 or modetr = 6;

-- suppression de trajets_dat de tous les troncons qui ont un code NVQ 2005 valide
VACUUM ANALYZE trajets_dat ;

-- creation de la table des trajets non NVQ avec ordre
create table trajets_nonnvq as (
	select idtraj,    
		rank() over (partition by idtraj order by idtraj, id) as ordre,
		modetr, linkid as code_gs,
		case when sens = 2 then -1 else 1 end as sens,
		0 as lgtron_2009
	from trajets_dat t
);
create index trajets_nonnvq_linkik_idx on trajets_nonnvq (idtraj);

-- ajout des colonnes idmenage,
alter table trajets_nonnvq add column idmenage int;

-- remplissage des colonnes
update trajets_nonnvq set 
	idmenage = substring(idtraj from '(^[0-9]*)_')::int;

create index trajets_nonnvq_idmenage_idx on trajets_nonnvq (idmenage);

--numdepl = substring(iddepl from '^[0-9]*_([0-9]*)')::int
--select iddepl, substring(iddepl from '^[0-9]*_([0-9]*)')::int from trajets_;

-- mise ˆ jour des id/id_new depuis la table correspid (trajets.dbf)
-- (requete de verif)
-- drop indices for faster upate
drop index trajets_nonnvq_idmenage_idx;
drop index trajets_nonnvq_linkik_idx;

update trajets_nonnvq t set idmenage = c.id_new
from correspid c
where t.idmenage = c.id;

-- controle: 
select idtraj, idmenage, numdepl, numtrajet, ordre, modetr, code_gs, poids
from trajets_nonnvq 
order by idtraj, ordre limit 100 offset 100000;

-- ajout/mise a jour de numdepl, iddepl et numtrajet pour avoir les bonnes colonnes de sortie.
alter table trajets_nonnvq add column numdepl int;
alter table trajets_nonnvq add column iddepl int;
alter table trajets_nonnvq add column numtrajet int;

update trajets_nonnvq set
	numdepl = substring(idtraj from '^[0-9]*_([0-9]*)_[0-9]*')::int,
	numtrajet = substring(idtraj from '^[0-9]*_[0-9]*_([0-9]*)')::int;

-- ajout de la colonne poids et mise a jour depuis la table correspid/trajets_dbf issue de trajets.dbf
alter table trajets_nonnvq add column poids double precision;
create index trajets_nonnvq_idmenage_idx on trajets_nonnvq (idmenage);
create index correspid_id_new_idx on correspid (id_new);

update trajets_nonnvq t set poids = c.poids
from correspid c
where t.idmenage = c.id_new;
-- table prete a etre injecte dans le resultat final

-- injection dans tj_final des troncons de trajets qui ne sont pas navteq
-- depuis trajets.dat (trajets_dat dans la base)
-- etape non faite: il faut calculer l'ordre sur les trajets avant => on passe
-- par une nouvelle table qui a la bonne structure directement => UNION a la fin
-- insert into tj_final (id, idtraj, numdepl, modetr, sens, code_gs,
--ordre, idmenage)
--select id, idtraj, numdepl, mode, sens, linkid, ordre, idmenage
--from trajets_;
-- on attend 5509882


-- mise a jour du modetr dans la table tj_final:
-- on extrait les modetr null, on les mets a jour, on les reinjectera
-- dans la table finale ensuite

drop table modetrnull;
create table modetrnull as
select t.idtraj,
  t.ordre ,
  t.gid ,
  t.code_gs  ,
  t.lgtron2009 ,
  t.sens ,
  t.id  ,
  t.modetr  ,
  t.iddepl  ,
  t.modeprinc  ,
  t.numdepl ,
  t.numtrajet ,
  t.poste ,
  t.lgtron2006 ,
  t.pid ,
  t.geom ,
  t.sens_bva ,
  t.idmenage ,
  t.poids  ,
  t.new_modetr
from tj_final t
where t.modetr is null;

-- mise a jour des modetr
update modetrnull m set new_modetr = t.modetr
from trajets_dat  t
where t.idtraj = m.idtraj;

-- suppression des modetr null dans tj_final avant reinjection
delete from tj_final where modetr is null;

-- verif d'eventuelles valeurs a null pour les champs d'idtraj
select count(*) from tj_final where idmenage is null;
-- attention: 595 trajets dont idmenage est null => pas de
correspondance entre id/idnew
-- pour les idtraj suivants:
-- "148557_2_1"
-- "211854_1_1"
-- "211854_2_1"

select count(*) from tj_final where numdepl is null;
select count(*) from tj_final where numtrajet is null;

-- l'idtraj de sortie doit prendre l'idmenage mis a jour.

-- suppression des trajets non NVQ (ordre oubli fuck)
delete from tj_final where code_gs < 56164364;

-------------------------- Reste a faire --------------------------------------
-- trajets_dat : reconstruire l'ordre des trajets non nvq a partir de
-- l'ID present dans le fichier
-- creer une nouvelle table annexe trajets_nonnvq avec les bons champs
-- a UNION a la fin: faire le lien avec les id_new.
-- MAJ a jour idmenage, numdepl, numtrajet, ordre
-- elle est ok
-- attention au modetr, soit sous forme de string, soit sous forme d'int
-- bon type de col pour modetr.

-- idtraj tronqus ? a virer ou pas
-- sens ?

-- requete finale issue de tj_final, modetrnull, trajets_nonnvq
-- pour reconstruire tous les trajets
create table trajets_paris_2009 as (
select 
        -- nouvel idtraj
        idmenage || '_' || numdepl || '_' || numtrajet as idtraj,
        ordre,
        code_gs,
        lgtron2009,
        sens,
        numdepl,
        numtrajet,
        new_modetr as modetr,
        idmenage as id,
        idmenage || '_' || numdepl as iddepl,
        poids
from tj_final

UNION ALL

select
        -- nouvel idtraj
        idmenage || '_' || numdepl || '_' || numtrajet as idtraj,
        ordre,
        code_gs,
        lgtron2009,
        sens,
        numdepl,
        numtrajet,
        new_modetr as modetr,
        idmenage as id,
        idmenage || '_' || numdepl as iddepl,
        poids

from modetrnull

UNION ALL

select
        -- nouvel idtraj
        idmenage || '_' || numdepl || '_' || numtrajet as idtraj,
        ordre,
        code_gs,
        lgtron_2009 as lgtron2009,
        sens,
        numdepl::text,
        numtrajet::text,
        modetr::text,
        idmenage as id,
        idmenage || '_' || numdepl as iddepl,
        poids

from trajets_nonnvq
);

-- mise a jour du modetr pour avoir des caracteres sur 2 char:
update trajets_paris_2009  set modetr =  '0' || modetr 
where length(modetr) = 1; 

-- mise a jour de l'ID qui doit etre faux dans la table tj_final=
-- c'est l'idmenage= debut d'idtraj
update trajets_paris_2009  set id = substring(idtraj from '(^[0-9]*)_')::int; 

select idtraj, substring(idtraj from '(^[0-9]*)_')::int from trajets_paris_2009  limit 100 offset 8000000;

--verif du modetr sous forme de string
select distinct modetr from trajets_paris_2009;

select idtraj, id, numdepl, numtrajet, iddepl, ordre, code_gs
from (
	select * from trajets_paris_2009  limit 100 offset 0) as foo
order by idtraj, ordre;

-- stats sur la table
select count(distinct idtraj) from trajets_paris_2009;  
-- -> 223540 idtraj differents => 44 arcs de trajets par trajet.
select count(distinct id) from trajets_paris_2009;  
-- -> 36179 menage differents

select count(*) from trajets_paris_2009 where numdepl is null;
select count(*) from trajets_paris_2009 where numtrajet is null;

--------------------------------------------------------------------------------
----------------------------------- 23 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- reimport des trajets recalÃ©s initiaux pour verifier les id tronquees.
-- cf shell pour remonter les trajets.

-- nouvelle structure de table pour reprÃ©senter un individu, un deplacement, un trajet, un troncon.

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
----------------------------------- 24 octobre 2012 ----------------------------
--------------------------------------------------------------------------------
-- chargement des retours BVA:

-- shp2pgsql -iD /mnt/data/UU-ParisMeaux_attente/retour/trajets_paris_2009_retourfab.dbf trajets_retour | psql

-- comparaison des modetr entre les trajets_dat et la table de retour:
select count(distinct idtraj) from trajets_dat ;
-- -> 65170
select count(distinct idtraj) from trajets_retour ;
-- > 225000 ???
select count(distinct idtraj) from trajets_paris_2009  ;
-- -> 223540

-- retours
create index trajets_retour_compar_200_idx on trajets_retour (compar_200);

select compar_200, count(compar_200) from trajets_retour group by compar_200;
-- mise a jour de la table des trajets avec le bon mode
select idtraj, modetr 
from trajets_retour   


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

-- injection des trajets tronquÃ©s.
-- shp2pgsql -iD  aggr_2005_id_dep_trajetunique.dbf aggr_trajets | psql 

-- analyse des trajets tronques:
select t.idtraj, t.id, t.numdepl, t.numtrajet, a.id, a.numdepl, a.numtrajet, a.numtraje_a
from trajets_tronq t, aggr_trajets a
where t.id = a.id and t.numdepl::int = a.numdepl;

select count(distint idtraj, id, iddepl) from trajets_tronq;

-- mise a jour des trajets tronquÃ©s et des modetr
-- en creant une nouvelle table:
-- modetr  a prendre depuis trajets.dat pour tout le monde
-- UNION new idtraj pour les tronquÃ©s

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

-- nombre de trajets restant tronquÃ©s:
select count(*) from trajets_paris_2009 where t.idtraj like '%\\_';

-- suppression des trajets restant tronquÃ©s:
delete from trajets_paris_2009 where t.idtraj like '%\\_';

-- controle sur les modetr: doivent etre les memes que ceux de trajets_dat
select count(*) 
from trajets_paris_2009 t, trajet_dat d
where t.idtraj = d.idtraj
and t.modetr <> d.modetr;
-- doit rester 0

------------------- From local ------------------------------
-- chargement des retours BVA:

-- shp2pgsql -iD /mnt/data/UU-ParisMeaux_attente/retour/trajets_paris_2009_retourfab.dbf trajets_retour | psql

-- comparaison des modetr entre les trajets_dat et la table de retour:
select count(distinct idtraj) from trajets_dat ;
-- -> 65170
select count(distinct idtraj) from trajets_retour ;
-- > 225000 ???
select count(distinct idtraj) from trajets_paris_2009  ;
-- -> 223540

-- retours
create index trajets_retour_compar_200_idx on trajets_retour (compar_200);

select compar_200, count(compar_200) from trajets_retour group by compar_200;
-- mise a jour de la table des trajets avec le bon mode
select idtraj, modetr 
from trajets_retour   

------------------------------------------------------------------------------------------
-- Nouvelle structure de table indivudu, deplacement, trajet, troncons.
-- reimport des trajets recalÃ©s initiaux pour verifier les id tronquees.
-- cf shell pour remonter les trajets.

-- nouvelle structure de table pour reprÃ©senter un individu, un deplacement, un trajet, un troncon.

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
-- -> 20174 trajets tronquÃ©s.

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

-- creation de la vue reprÃ©sentant la table de sortie BVA/AFFI
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
----------------------------------- 27 octobre 2012 ----------------------------
--------------------------------------------------------------------------------

-- 	idtraj tronqus: 20174 dont 19119 recuperables: numtrajet = 1
select count(*) from trajet where numtrajet = 0;
-- 1001 trajets a recuperer
select distinct id, numdepl, numtrajet from aggr_trajets;

-- create column idtraj sur la table aggr_trajets
alter table aggr_trajets add column idtraj text;
alter table aggr_trajets add column iddepl text;

update aggr_trajets set idtraj  = id ||'_'||numdepl||'_'||numtrajet;
update aggr_trajets set iddepl  = id ||'_'||numdepl;

create index aggr_trajets_idtraj_idx on aggr_trajets (idtraj);
create index aggr_trajets_iddepl_idx on aggr_trajets (iddepl);

with bad as (
	select distinct idtraj, iddepl, numtrajet  from trajet where numtrajet = 0
) select b.idtraj, a.id, a.numdepl, a.numtrajet, b.numtrajet as badtrajet
from aggr_trajets a, bad b
where b.iddepl = a.iddepl;
-- -> 959 trajet sur 1001: trouver la difference de 42...

-- nettoyage de la table trajets; certains trajets a numtrajet=0 existent dans la table
select distinct iddepl from trajet where numtrajet = 1 or numtrajet = 0;

-- 4¡) requetes de mise a jour => controle a la sortie

-- mise a jour des trajets
update trajet t set numtrajet = a.numtrajet
from aggr_trajets a
where t.numtrajet = 0
and a.iddepl = t.iddepl;
-- 959 updated

-- nettoyage des idtraj tronqus:
create table tmp_trajets as select * from trajet where idtraj like E'%\\_';
delete from trajet where idtraj like E'%\\_';

with del as (
	select idtraj||numtrajet as idtraj 
	from tmp_trajets
) select t.* 
from trajet t, del d 
where t.idtraj = d.idtraj;
-- une seule: "25536_11_1": a virer avant reinsertion
delete from trajet where idtraj = '25536_11_1';

insert into trajet 
	select idtraj||numtrajet as idtraj, numtrajet, iddepl, modetr
	from tmp_trajets; 

drop table tmp_trajets;

-- controle de la table des trajets: modetr et idtraj doivent etre corrects 
with bad as (
select * from trajet where numtrajet = 0
) select * from aggr_trajets a, bad b  
where a.iddepl = b.iddepl; 

-- exclusion des trajets avec mauvais numtrajet
create table trajet_badnumtrajet as select * from trajet  where numtrajet = 0;

delete from trajet  where numtrajet = 0;
 
-- 5¡) Vue des trajets complets: penser aux trajets sans troncon (mode non nvq: voir ce qui se passe)
select trajet, count(distinct trajet) from trajets_dbf group by trajet;

-- on refait la table de controle:
drop table trajets_ctrl;
create table trajets_ctrl as (
	select distinct on (id_new||'_'||numdepl||'_'||substring(trajet from '^[0-9]*_[0-9]*_([0-9]*)'))
	id_new||'_'||numdepl||'_'||substring(trajet from '^[0-9]*_[0-9]*_([0-9]*)') as idtraj,
	id_new as id,
	mode::int as modetr, poids, numdepl, 
	substring(trajet from '^[0-9]*_[0-9]*_([0-9]*)')::int as numtrajet
	from trajets_dbf
);
create index trajets_ctrl_idtraj_idx on trajets_ctrl  (idtraj);

alter table trajets_ctrl alter column modetr set data type smallint;

create table good_modetr as 
select c.idtraj, c.numtrajet, c.modetr
from trajet t, trajets_ctrl c
where t.idtraj = c.idtraj
and t.modetr <> c.modetr;

update trajet t set modetr = c.modetr
from good_modetr c
where t.idtraj = c.idtraj;

-- YES ! tous les modetr sont mis a jour
-- les numtrajets correspondent.
--restent 42 trajet avec numtrajet = 0
select count(*) from trajet where numtrajet = 0;
select count(*) from trajet where idtraj like E'%\\_';
select count(distinct idtraj) from trajet ;

select count(*) from trajets_paris_2009_view where geom is null;

select distinct idtraj from trajets_retour where compar_200 = 0;

-- ou sont les bons modettr pour les idtraj ?
-- table trajets_dat contient les mauvais id => ne pas joindre sur idtraj !
-- table trajets_retour =
-- contient une colonne compar_200 indiquant les mauvais modetr (compar_200=0)

-- table trajets_dbf = 
-- fichier des trajets contenant une colonne id_new a prendre a la place de trajet
select count(*) from trajets_dbf;

-- table aggr_trajets =
-- Tous les trajets ayant un numtrajet = 1: sert a reconstruire les mauvais numtrajets


-- export des tables.
-- pgsql2shp -f individu.dbf bva individu
-- -> [36179 rows]
-- pgsql2shp -f deplacement.dbf bva deplacement
-- -- [147052 rows]
-- pgsql2shp -f trajet.dbf bva trajet
-- -> [223497 rows]
-- pgsql2shp -f trajets_paris_2009.shp bva trajets_paris_2009_view
-- -> 

--------------------------------------------------------------------------------
----------------------------------- 04 novembre 2012 ----------------------------
--------------------------------------------------------------------------------
-- analyse des trajets tronqus:
-- combien d'idtraj uniques dans la table de controle (aggr_trajets)
select count(distinct idtraj) from aggr_trajets;
-- 107015
select count(distinct id) from aggr_trajets;
-- 31893

-- combien d'idtraj tronques:
select count(distinct idtraj) from trajets_tronq;

-- combien d'idtraj presents dans la table de controle trouvables dans la table de sortie
select count(distinct a.iddepl) 
from aggr_trajets a, trajets_tronq t
where a.iddepl = t.iddepl;
-- 959 iddepl pouvant creer un trajet.

-- fabrication des troncons manquants a  partir de trajets_tronq
with rebuild_traj as (
	select distinct 
	t.idtraj,  t.ordre, t.sens, t.code_gs as linkid
	from aggr_trajets a, trajets_tronq t
	where a.iddepl = t.iddepl
);

insert into troncon (idtraj, ordre, sens, linkid)
select distinct t.idtraj,  t.ordre, t.sens, t.code_gs as linkid 
from aggr_trajets a, trajets_tronq t
where a.iddepl = t.iddepl;
-- 19119

-- retrouver les geometries de ces troncon.
drop table tgeom;
create table tgeom as (
with rebuild_traj as (
	select distinct 
	t.idtraj,  t.ordre, t.sens, t.code_gs as linkid
	from aggr_trajets a, trajets_tronq t
	where a.iddepl = t.iddepl
) select distinct t.code_gs as linkid,  t.geom 
from trajets_paris_2009 t, rebuild_traj r
where t.code_gs = r.linkid
);

-- mise a jour des troncon avec geom nulles
select t.idtraj, t.linkid 
from troncon t, tgeom g
where t.linkid = g.linkid
and t.geom is null;

update troncon t set geom = a.geom
from tgeom as a
where t.geom is null
and t.linkid = a.linkid;

-- combient d'idtraj reconstructibles => combien de troncons <->
-- 959 idtraj => 19119 troncons en plus.
-- combien non reconstructibles ?
-- 42 => 1055 

select count(*) from trajets_paris_2009_view;
-- 9735713 records
-- exported: 9735713
-- 9755887
-- 10648_10_ et le 10869_10_

-- trajet avec bad id
select idtraj, numtrajet from trajet where idtraj like '%\\_';
-- aucun

-- troncons avec bad id
select count(*) from troncon where idtraj like '%\\_';
-- 39293

-- => il faut corriger les bad idtraj dans les troncons:
-- match parfait entre idtraj ?
select t.idtraj, t.numtrajet, r.idtraj
from troncon r, trajet t
where r.idtraj like E'%\\_'
and r.idtraj = substring(t.idtraj from E'(^[0-9]*_[0-9]*_)[0-9]*');

update troncon r set idtraj = t.idtraj
from trajet t
where r.idtraj like E'%\\_'
and r.idtraj = substring(t.idtraj from E'(^[0-9]*_[0-9]*_)[0-9]*');

vacuum analyse troncon;

--nouveau count: 
select count(*) from trajets_paris_2009_view;
-- 9774060 --diff de 18150 trajets: bad ids ?

select t.*
from troncon t, tgeom g
where t.linkid = g.linkid 
and t.idtraj like '10648_10_%';

--------------------------------------------------------------------------------
----------------------------------- 07 novembre 2012 ----------------------------
--------------------------------------------------------------------------------
-- doublons dans trajet et/ou troncon pour les idtraj tronques ?
select count(*) from trajet where idtraj = '10648_10_1';
select count(*) from trajet where idtraj = '10869_10_1';

select * from troncon where idtraj = '10648_10_1' order by ordre;
select * from troncon where idtraj = '10869_10_1' order by ordre;

select count(*) from troncon;
-- 9775006.
-- 9735713 dans les fournitures prÃ©cÃ©dentes
-- 

-- doublons prÃ©sents dans la table des troncons, avec sens ambigus la plupart du temps.
-- d'ou viennent ces troncons, comment les virer ?
-- parmi les idtraj tronques, trouver ceux qui ont un count > 1 group by idtraj, ordre
with tronq as (
	select distinct idtraj from trajets_tronq 
)
select linkid, count(linkid) 
from troncon t, tronq q
where t.idtraj = q.idtraj || '_1'
group by linkid;

select count(*) from troncon where idtraj like E'%\\_';

with tronq as (
	select distinct idtraj from trajets_tronq 
)
select t.idtraj, ordre, count(t.idtraj)
from troncon t, tronq q
where t.idtraj = q.idtraj || '1'
group by substring(t.idtraj from '(^[0-9]*_[0-9]*_)[0-9]'), ordre;


with tronq as (
	select distinct idtraj from trajets_tronq 
)
select t.idtraj, t.ordre, t.sens, t.linkid
from troncon t, tronq q
where t.idtraj = q.idtraj || '1'
group by substring(idtraj from ''), ordre;

--
