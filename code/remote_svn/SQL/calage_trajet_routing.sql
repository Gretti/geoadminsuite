-- script utilisant pgRouting pour reconstruire la fin des trajets manquants
-- pour les trajets dont les stats de longueur sont mauvaises, on cherche
-- le dernier point de voirie trouvé par l'algo, puis on demande a pgrouting
-- de calculer un trajet entre ce point et le point d'arrivée du trajet.
-- on recolle ensuite les deux trajets.

-- construction de la table de stats:
drop table if exists ecart_longueur;
create table ecart_longueur as (
	select foo.idtraj, lgtron2006, lgtron2009, ((lgtron2009-lgtron2006)/lgtron2006)*100 as diff_longueur
	from
		(select idtraj, sum(lgtron2006) as lgtron2006 from trajet group by idtraj
		 UNION
		 select idtraj, sum(lgtron2006) as lgtron2006  from trajet_2009_ok group by idtraj) as foo,
		(select idtraj, sum(lgtron2009) as lgtron2009  from trajet_2009 group by idtraj) as bar
	where foo.idtraj = bar.idtraj
);

-- mise a jour de la table tsv pour identifier les trajets courts:
-- les parametres utilises ici sont importants pour le taux reconstruction des trajets.
alter table trajet_startend_voirie add column short boolean;

update trajet_startend_voirie t set short = false;

update trajet_startend_voirie t set short = true
from (select idtraj from ecart_longueur
    where lgtron2006 > 600 and abs(diff_longueur) > 10) as foo
where foo.idtraj = t.idtraj;

create index trajet_startend_voirie_short on trajet_startend_voirie(short);

-- mise a jour des endnode pour les trajets, necessaire pour le routing:
-- meme principe que pour startnode MAIS on prend l'avant dernier troncon,
-- le dernier etant un troncon en erreur
alter table trajet_startend_voirie  add column endnode numeric;

update trajet_startend_voirie tsv set endnode =
	case when dotProd(foo.traj, foo.voirie_geom) > 0 then foo.nref_in_id
	else foo.ref_in_id end
from (
	select t.idtraj, t.ref_in_id, t.nref_in_id,
		case when st_intersection(t.startgeomtrajet, t2.geom) = st_startpoint(t.startgeomtrajet)
				then st_makeline(st_endpoint(t.startgeomtrajet), st_startpoint(t.startgeomtrajet))
		       else st_makeline(st_startpoint(t.startgeomtrajet), st_endpoint(t.startgeomtrajet)) end as traj,
		t.voirie_geom
	from trajet_startend_voirie t, trajet t2
	where t.idtraj = t2.idtraj
	and  t2.ordre = (t.endordre - 1)
) as foo
where foo.idtraj = tsv.idtraj;

create index trajet_startend_voirie_endnode on trajet_startend_voirie(endnode);
create index trajet_startend_voirie_endvoirie on trajet_startend_voirie(endvoirie);

-- mise a jour de la table tsv pour ajouter source et target, les noeuds de voirie permettant
-- de faire le routing
alter table trajet_startend_voirie  add column source int4;
alter table trajet_startend_voirie  add column target int4;

-- mise a jour de la target avec l'identifiant du endnode du trajet
update trajet_startend_voirie t set target = case when v.ref_in_id = t.endnode then v.source
					     else v.target end
from voirie_work v
where v.idtraj = t.idtraj
and v.gid = t.endvoirie
and t.short;

-- mise a jour de la source avec l'identifiant du last endnode du trajet 2009: la ou s'est arrete buildTrajet
-- a voir s'il faut prendre ce point ou un point precedent sur le trajet, pour eviter un eventuel dernier mauvais troncon:
-- oui, il faut virer le dernier troncon de trajet partiel: il est souvent faux.
update trajet_startend_voirie t set source = case when v.ref_in_id = node_id then v.source
					      else v.target end
from voirie_work v,
(	select t.idtraj, t.ordre, t.node_id, t.gid
	from trajet_2009 t,
    (select idtraj, max(ordre) -1 as ordre from trajet_2009 t2 group by t2.idtraj) as foo
    where t.ordre = foo.ordre
    and foo.idtraj = t.idtraj
) as foo
where t.idtraj = v.idtraj
and v.idtraj = foo.idtraj
and foo.gid = v.gid
and t.short;

-- trouver les trajets en erreur...

-- lancement de la fonction appelant pgrouting pour chaque trajet trop court:
select * from routeTrajet();

create index trajet_routing_idtraj on trajet_routing(idtraj);
create index trajet_routing_edge_id on trajet_routing(edge_id);

-- partie geo: on supprime le noeud terminal
delete from trajet_routing where edge_id = -1;

-- mise a jour pour les infos de voiries
alter table trajet_routing add column geom geometry;
alter table trajet_routing add column link_id integer;

update trajet_routing t set geom = v.geom, link_id = v.link_id
from voirie_work v
where t.idtraj = v.idtraj
and v.gid = t.edge_id;

create index trajet_routing_ko_id on trajet_routing_ko(idtraj);

-- sauvegarde des bons trajets
drop table if exists trajet_routing_bu;
create table trajet_routing_bu as select * from trajet_routing;

-- suppression des bons trajets de tsv:
delete from trajet_startend_voirie  tsv
using trajet_routing t
where tsv.idtraj = t.idtraj;

-- mise a jour de voirie_work pour inclure les voiries a 500m, pour faire repasser
-- les trajets en erreur a l'etape precedente: surement du a une discontinuité dans
-- le graphe
insert into voirie_work (idtraj, gid, link_id, ref_in_id, nref_in_id, shape_length, geom, avgdist, source, target)
select distinct t.idtraj, v.gid, v.link_id, v.ref_in_id, v.nref_in_id, v.shape_len as shape_length, v.geom, 0, v.source, v.target
from trajet t, voirie v, trajet_routing_ko tko
where st_dwithin(v.geom, t.geom, 500)
and st_distance(v.geom, t.geom) > 30
and t.idtraj = tko.idtraj;

-- on relance sur les voiries non traitees
select * from routeTrajet();

-- partie geo: on supprime le noeud terminal
delete from trajet_routing where edge_id = -1;

-- mise a jour pour les infos de voiries
alter table trajet_routing add column geom geometry;
alter table trajet_routing add column link_id integer;

update trajet_routing t set geom = v.geom, link_id = v.link_id
from voirie_work v
where t.idtraj = v.idtraj
and v.gid = t.edge_id;

-- et on insere toutes les voiries traitees:
insert into trajet_routing select * from trajet_routing_bu;

drop table trajet_routing_bu;

-- insertion dans la table trajet_2009 des trajets
-- reconstruits par routing, avec mise a jour de l'ordre:
-- attention au calcul de l'ordre si le dernier troncon du trajet n'est pas pris en compte
-- lors du routing.
insert into trajet_2009 (idtraj, gid, node_id, ordre, link_id, lgtron2009, geom)
with tmp(idtraj, ordre) as (
	select idtraj, max(ordre) -1 as ordre
	from trajet_2009 t group by idtraj
	order by idtraj
) select tmp.idtraj, tr.edge_id as gid, tr.vertex_id, tmp.ordre + tr.ordre, tr.link_id, tr.cost as lgtron2009, tr.geom
from tmp, trajet_2009 t, trajet_routing tr
where tmp.idtraj = t.idtraj
and t.idtraj = tr.idtraj
and t.ordre = tmp.ordre;
