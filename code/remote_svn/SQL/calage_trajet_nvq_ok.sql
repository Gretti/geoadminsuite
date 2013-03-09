-- 3) -------------------------------------------------------------------------------------------
-- Extraction des trajets pour lesquels TOUS les codes NVQ sont valides
drop table if exists trajet_2009_ok;
create table trajet_2009_ok as (
	with tmp(tgid, numdepl, sens, modetr, modeprinc, numtrajet, poste, id, pid, poids,
	         idtraj, voirie_gid, link_id,  shape_length, voirie_geom, count) as (
		select distinct t.gid, numdepl, sens, modetr, modeprinc, numtrajet, poste, id, pid, poids,
		t.idtraj, v.gid,  v.link_id, v.shape_length, v.geom, count(ordre) OVER (partition by t.idtraj)
		from trajet t, voirie_work v
		where t.code_gs::int = v.link_id
		and v.idtraj = t.idtraj
	)
	select t.idtraj, 
	        t.numdepl, t.sens, t.modetr, t.modeprinc, t.numtrajet, t.poste, t.id, t.pid, t.poids,
	       tmp.voirie_gid as gid, 
	       -1 as node_id, 
	       t.ordre, 
	       tmp.link_id, 
           tmp.shape_length as lgtron2009, 
           t.lgtron2006, 
           tmp.voirie_geom as geom
	from tmp, trajet t, trajet_full tf
	where tmp.idtraj = tf.idtraj
	and t.gid = tmp.tgid
	and t.idtraj = tmp.idtraj
	and tmp.count = tf.numtroncon
);

select count(distinct idtraj) as "trajets avec code NVQ valide" from trajet_2009_ok  ;

-- elimination de ces trajets des tables de travail.
delete from voirie_work v
using trajet_2009_ok t
where t.idtraj = v.idtraj;

delete from trajet t
using trajet_2009_ok t1
where t.idtraj = t1.idtraj;

delete from trajet_full t
using trajet_2009_ok t1
where t.idtraj = t1.idtraj;
