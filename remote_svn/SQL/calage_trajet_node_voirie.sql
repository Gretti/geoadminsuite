-- 5) -------------------------------------------------------------------------------------------
-- table des troncons de voirie connectes a chaque noeud du graphe:
-- on ne garde que les 2 voiries les plus proches du trajet pour les voiries connectées a chaque noeud:
-- et on doit se servir des codes NVQ valides: si plus de deux voiries connectees a un noeud,
-- ne garder
drop table if exists node_voirie;
create table node_voirie as
with nodes(nodeid, nextnodeid, idtraj, gid, avgdist, link_id, shape_length, geom, row_number) as (
	select nodeid, nextnodeid, idtraj, gid, avgdist, link_id, shape_length, geom,
	       row_number() OVER (partition by nodeid, idtraj order by avgdist) from (

		select ref_in_id as nodeid, nref_in_id as nextnodeid, idtraj, gid, avgdist, link_id, shape_length, geom
		from voirie_work

		UNION ALL

		select nref_in_id as nodeid, ref_in_id as nextnodeid, idtraj , gid, avgdist, link_id, shape_length, geom
		from voirie_work
	) as foo
)
select *
from nodes
where nodes.row_number < 3;
--and idtraj = '14075_3'
--and nodeid = 77032082;

create index node_voirie_nodeid on node_voirie(nodeid);
create index node_voirie_idtraj on node_voirie(idtraj);
create index node_voirie_gid on node_voirie(gid);

-- mise a jour de cette table pour que les eventuels startedge qui ne seraient pas
-- identifies comme segment les plus pres des trajets (donc absents de node_voirie) apparaissent
-- dans cette table, pour le cas ou le noeud de depart connecte plusieurs voiries.
update node_voirie nv set gid = t.startvoirie
from (
	with starts (idtraj, nodeid, gid, avgdist, startvoirie, row_number, array_accum) as (
		select n.idtraj, n.nodeid, n.gid, n.avgdist, v.startvoirie,
		row_number() over (partition by n.idtraj order by avgdist desc),
		array_accum(gid) over (partition by n.idtraj)
		from node_voirie n, trajet_startend_voirie v
		where n.nodeid = v.startnode
		and n.idtraj = v.idtraj
	)
	select distinct s.gid, s.array_accum, s.nodeid, s.idtraj, s.startvoirie
	from starts s, node_voirie n
	where s.idtraj = n.idtraj
	and s.gid = n.gid
	and s.row_number < 2
	and s.gid <> startvoirie
	order by s.idtraj
) as t
where nv.idtraj = t.idtraj
and nv.nodeid = t.nodeid
and nv.gid = t.gid
and not (t.startvoirie = any(t.array_accum));

--------------------------------------------------------------------------------
-- creation de la fonction utilisant cette table:
--create or replace function find_voirie(p_idtraj in text, p_nodeid in int, p_gid in int)
--returns int as $$
--	select n.gid
--	from node_voirie n
--	where n.idtraj = $1
--	and n.nodeid = $2
--	and n.gid <> $3
--	order by avgdist
--	limit 1;
--
--$$ LANGUAGE SQL immutable strict;


