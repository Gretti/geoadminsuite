-- fonction iterative de parcours de node_voirie
create or replace function walkOnNodeVoirie(idtraj in text, startVoirieGid in int, endVoirieGid in int, nodeId in int)
	returns table (idtraj text, gid int, node_id int, ordre int, link_id int, lgtron2009 double precision, geom geometry) AS $$

	WITH RECURSIVE walk_voirie(idtraj, gid, node_id, link_id, lgtron2009, geom, path, cycle, depth) AS (
		  --terme non iteratif
		  SELECT idtraj, n.gid,	$4 as node_id, n.link_id,
		         n.shape_length as lgtron2009, n.geom,
		         ARRAY[n.gid] as path,
				 false as cycle, 1 as depth
		  FROM voirie_work n -- depart se fait avec voirie_work
		  WHERE n.gid = $2 and idtraj = $1

		  UNION ALL

		  --terme iteratif
		  SELECT distinct on (n.idtraj) n.idtraj, n.gid, n.nextnodeid as nodeid, n.link_id,
		  		 n.shape_length as lgtron2009, n.geom,
				 path || n.gid,
				 (n.gid = $3 or n.gid = -1), w.depth + 1
		  FROM walk_voirie w, node_voirie n
		  WHERE n.gid <> w.gid
		  -- condition sur le trajet
		  and n.idtraj = $1
		  -- condition sur le noeud courant
		  and n.nodeid = w.node_id
		  and not (n.gid = ANY(path))
		  and not cycle
    ) SELECT idtraj, gid, node_id, depth, link_id, lgtron2009, geom
	  FROM walk_voirie wv;

$$ language sql;

-- 6) -------------------------------------------------------------------------------------------
drop  table if exists trajet_2009;
create table trajet_2009 as
select * from buildTrajets();

-- insertion des entrees de la table trajet_2009_tmp
insert into trajet_2009
select * from trajet_2009_tmp;

-- insertion des entrees de la table trajet_2009_ok: les trajets 2006 avec code NVQ 2009 valide
insert into trajet_2009
select idtraj, gid, node_id, ordre, link_id, lgtron2009, geom
from trajet_2009_ok;

create index trajet_2009_ordre on trajet_2009 (ordre);
create index trajet_2009_idtraj on trajet_2009 (idtraj);
create index trajet_2009_geom on trajet_2009 using gist(geom);
