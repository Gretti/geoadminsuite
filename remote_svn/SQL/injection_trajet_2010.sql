
-- TEST
-- -- Copie des trajets 2010 dans la table trajet
-- insert into trajet select nextval('trajet_gid_seq'::regclass), ordre, numdepl, sens, mode, 'NONE', iddepl, link_id, numtrajet, 'NONE', idinsee, null, null, geom, poids from trajet_2010;
-- -- Recherche des trajects 2010 a inserer
-- select * from ident_new id_new, current_uu uu where id_new.aresimuler=1 and uu.code_uu=id_new.uu99;
-- -- Indique le nombre d'utilisateur a mettre a jour dans la table trajet
-- select count(distinct t.idinsee) from trajet_2010 as t, ident_new as i where t.idinsee::int=i.id_new and i.aresimuler=1 and i.uu99=(select * from current_uu);
-- -- Compte les trajets 2010 a injecter dans la table trajet
-- select count(*) from (select nextval('trajet_gid_seq'::regclass), t.ordre, t.numdepl, t.sens, t.mode, 'NONE', t.iddepl, t.link_id, t.numtrajet, 'NONE', t.idinsee, null, null, t.geom, t.poids from trajet_2010 t, ident_new i, current_uu c where i.aresimuler=1 and c.code_uu=i.uu99 and t.idinsee::int=i.id_new) as request1;

-- Creation d'un index sur le code idinsee des trajets 2010
create index trajet_2010_idinsee on trajet_2010(idinsee);

-- Insere les trajets 2010 a injecter dans la table trajet
insert into trajet_2009 (idtraj  ,ordre,code_gs ,lgtron2009, geom, 
                         sens, numdepl, modetr ,modeprinc,numtrajet, 
                         poste, id , lgtron2006,pid,poids)
                         
    select t.iddepl, t.ordre, t.link_id, st_length(geom), t.geom, 
           t.sens, t.numdepl, t.mode, 'NONE', t.numtrajet, 
           'NONE', t.idinsee::int, -999.999,      -999.999, t.poids
    from trajet_2010 t, ident_new_uu i
    where i.aresimuler=1 and t.idinsee::int=i.id_new;
