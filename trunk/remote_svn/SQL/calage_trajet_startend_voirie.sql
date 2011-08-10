-- 3) -------------------------------------------------------------------------------------------
-- but du script: trouver les voirie de depart et de fin pour chaque trajet
-- entree: table des trajet_full, trajet, voirie_work
-- sortie: une table donnant, pour chaque trajet, sa voirie de depart et de fin
-- puis trouver le noeud de depart = un des deux noeuds du troncon de depart
-- la table contient les troncons de trajet de depart et de fin,
-- les troncons de voirie de depart et de fin
-- les ref_in_id et nref_in_id des troncons de depart.

-- utilisation des troncons NVQ valides pour startvoirie et endvoirie
drop table if exists trajet_startend_voirie;
create table trajet_startend_voirie as
select distinct on (foo.idtraj) foo.idtraj, foo.ordre as startordre, foo.geom as startgeomtrajet,
	v.gid as startvoirie, v.ref_in_id, v.nref_in_id, v.geom as voirie_geom
from voirie_work v,
(	select distinct on(idtraj) idtraj, code_gs, ordre, geom
	from trajet t
	order by idtraj, ordre
) as foo
where v.idtraj = foo.idtraj
and foo.code_gs::int = v.link_id;

-- puis mise a jour pour les trajets pour lesquels des codes valides ne sont pas trouves.
insert into trajet_startend_voirie
select distinct on (foo.idtraj) foo.idtraj, foo.ordre as startordre, foo.geom as startgeomtrajet,
	v.gid as startvoirie, v.ref_in_id, v.nref_in_id, v.geom as voirie_geom
from voirie_work v,
    (
    	select distinct on(t.idtraj) t.idtraj, ordre, geom
    	from trajet t
    	order by idtraj, ordre
    ) as foo
where st_dwithin(foo.geom, v.geom, 30)
and v.idtraj = foo.idtraj
and not exists (select idtraj from trajet_startend_voirie tsv where foo.idtraj = tsv.idtraj)
order by idtraj, st_avgDistance(foo.geom, v.geom);

select count(*) as "nombre trajets avec startvoirie: " from trajet_startend_voirie;

-- 4) -------------------------------------------------------------------------------------------
-- l'echantillon de test cree de fausses erreurs en decoupant les trajets/voirie sur une bbox.
drop table if exists trajet_rejet;
create table trajet_rejet as
select * from trajet_full tf
where not exists (select idtraj from trajet_startend_voirie tsv where tf.idtraj = tsv.idtraj);

-- mise a jour avec les endvoiries
alter table trajet_startend_voirie add column endvoirie int;
alter table trajet_startend_voirie add column endordre int;
alter table trajet_startend_voirie add column endgeomtrajet geometry;

-- mise a jour avec les troncons NVQ dont le link_id est valide
update trajet_startend_voirie tsv  set endvoirie = bar.endvoirie, endordre = bar.endordre, endgeomtrajet = bar.endgeomtrajet
from (
	select distinct on (foo.idtraj) foo.idtraj, foo.ordre as endordre, foo.geom as endgeomtrajet,
		v.gid as endvoirie, v.ref_in_id, v.nref_in_id, v.geom as voirie_geom
	from voirie_work v,
	(	select distinct on(idtraj) idtraj, code_gs, ordre, geom
		from trajet t
		order by idtraj, ordre desc
	) as foo
	where v.idtraj = foo.idtraj
	and foo.code_gs::int = v.link_id
) as bar
where tsv.idtraj = bar.idtraj;

-- puis mise a jour pour les autres troncons en se servant de la distance moyenne trajet-voirie
update trajet_startend_voirie tsv  set endvoirie = bar.endvoirie, endordre = bar.endordre, endgeomtrajet = bar.endgeomtrajet
from (
	select distinct on (foo.idtraj) foo.idtraj, foo.ordre as endordre, foo.geom as endgeomtrajet,
		v.gid as endvoirie, v.ref_in_id, v.nref_in_id, v.geom as voirie_geom
	from voirie_work v,
	    (
		select distinct on(t.idtraj) t.idtraj, ordre, geom
		from trajet t
		order by idtraj, ordre desc
	    ) as foo
	where st_dwithin(foo.geom, v.geom, 30)
	and v.idtraj = foo.idtraj
	order by idtraj, st_avgDistance(foo.geom, v.geom)
) as bar
where tsv.idtraj = bar.idtraj
and tsv.endvoirie is null;

-- creation des index
create index trajet_startend_voirie_idtraj on trajet_startend_voirie(idtraj);
create index trajet_startend_voirie_startordre on trajet_startend_voirie(startordre);
create index trajet_startend_voirie_endordre on trajet_startend_voirie(endordre);

-- ne pas rejeter les trajets sans endvoirie: mettre la valeur a -1 et laisser l'iteratif
-- les traiter
-- MAJ des trajet_rejet tout de suite
update trajet_startend_voirie tsv set endvoirie = -1
where endvoirie is null;

select count(distinct idtraj) "nombre trajets rejetes:"
from trajet_rejet;

--nettoyage des voiries de travail
delete from voirie_work v
using trajet_rejet tr
where v.idtraj = tr.idtraj;

-- et des startend pour chaque trajet
delete from trajet_startend_voirie  tsv
using trajet_rejet t
where t.idtraj = tsv.idtraj;

-- identification des trajets mono troncon: si une voirie est trouvee, on s'en sert comme trajet 2009
-- startvoirie = endvoirie
drop table if exists trajet_2009_tmp;
create table trajet_2009_tmp as (
select t.idtraj, v.gid, -1 as node_id, 1 as depth, v.link_id, v.shape_length as lgtron2009, v.geom
from trajet t, voirie_work v, trajet_startend_voirie tsv
where t.idtraj = tsv.idtraj
and v.idtraj = tsv.idtraj
and tsv.startvoirie = tsv.endvoirie
);

select count(*) as "Nombre de trajets mono troncon" from trajet_2009_tmp;

-- suppression de ces trajets des tables de travail
delete from voirie_work v
using trajet_2009_tmp t
where v.idtraj = t.idtraj;

delete from trajet_startend_voirie tsv
using trajet_2009_tmp t
where tsv.idtraj = t.idtraj;

-- identification des trajets mono troncons avec plusieurs voiries

with tmt(idtraj) as (
  select idtraj
  from (select idtraj, count(ordre) as cnt from trajet group by idtraj) as t
  where t.cnt = 1
)
select tmt.idtraj as "trajet mono troncon tjs dans TSV:"
from tmt, trajet_startend_voirie tsv
where tmt.idtraj = tsv.idtraj;

-- Maintenant, identifier les noeuds de depart de chaque startvoirie
-- identification du noeud de depart sur le startvoirie:
-- le noeud de voirie le plus pres du noeud de trajet connecté au troncon de trajet suivant
alter table trajet_startend_voirie  add column startnode numeric;

-- nouvelle methode pour identifier le startnode en se servant du sens relatif des vecteurs
-- formes par le troncon de trajet de depart et du trocon de voirie correspondant
update trajet_startend_voirie tsv set startnode =
	case when dotProd(foo.traj, foo.voirie_geom) > 0 then foo.nref_in_id
	else foo.ref_in_id end
from (
	select t.idtraj, t.ref_in_id, t.nref_in_id, startnode,
		case when st_intersection(t.startgeomtrajet, t2.geom) = st_startpoint(t.startgeomtrajet)
				then st_makeline(st_endpoint(t.startgeomtrajet), st_startpoint(t.startgeomtrajet))
		       else st_makeline(st_startpoint(t.startgeomtrajet), st_endpoint(t.startgeomtrajet)) end as traj,
		t.voirie_geom
	from trajet_startend_voirie t, trajet t2
	where t.idtraj = t2.idtraj
	and  t2.ordre = (t.startordre + 1)
) as foo
where foo.idtraj = tsv.idtraj;

-- creation des index
create index trajet_startend_voirie_startnode on trajet_startend_voirie(startnode);

-- identification des trajets n'ayant pas de startnode.
select count(idtraj) as "trajets sans startnode: "
from trajet_startend_voirie
where startnode is null;

-- rejet de ces trajets
insert into trajet_rejet
select t.*
from trajet_startend_voirie tsv, trajet_full t
where startnode is null
and tsv.idtraj = t.idtraj;

-- effacement des trajets sans startnodes
delete from voirie_work v
using trajet_rejet t
where t.idtraj = v.idtraj;

delete from trajet_startend_voirie  tsv
where startnode is null;
