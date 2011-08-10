-- 2) -------------------------------------------------------------------------------------------
-- la table des voiries dont le code link_id est le meme que celui d'un trajet
drop table if exists voirie_codeok;
create table voirie_codeok as
select distinct t.idtraj, v.gid, v.link_id, v.ref_in_id, v.nref_in_id, v.source, v.target, v.shape_len as shape_length, v.geom
from trajet t, voirie v
where t.code_gs::int = v.link_id;

--select count(distinct gid) from voirie_codeok;

create index voirie_codeok_linkid on voirie_codeok (link_id);
create index voirie_codeok_idtraj on voirie_codeok (idtraj);

-- 2bis)
-- mise a jour de la table pour y ajouter les voiries qui n'ont pas de code valide
-- voiries a moins de 30m des trajets
drop table if exists voirie_work;
create table voirie_work as
select distinct t.idtraj, v.gid, v.link_id, v.ref_in_id, v.nref_in_id, v.source, v.target, v.shape_len as shape_length, v.geom
from trajet t, voirie v
where st_dwithin(v.geom, t.geom, 30);

create index voirie_work_idtraj on voirie_work (idtraj);
create index voirie_work_linkid on voirie_work (link_id);

-- ajout de la colonne avgdist entre chaque voirie et son trajet
alter table voirie_work add column avgdist float;

-- forcage d'une distance 0 pour les troncons NVQ ayant un code valide => ils seront
-- choisis en premier par l'algo iteratif
update voirie_work v set avgdist = 0.0
from voirie_codeok vok
where v.link_id = vok.link_id
and v.idtraj = vok.idtraj;

-- mise a jour de la colonne pour les autres troncons
update voirie_work v set avgdist = st_avgDistance(v.geom, t.geom)
from trajet_full t
where t.idtraj = v.idtraj
and v.avgdist is null;

create index voirie_work_geom on voirie_work using gist (geom);
create index voirie_work_gid on voirie_work (gid);
create index voirie_work_avgdist on voirie_work (avgdist);
create index voirie_work_ref on voirie_work (ref_in_id);
create index voirie_work_nref on voirie_work (nref_in_id);

