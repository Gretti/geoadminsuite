-- 7) -------------------------------------------------------------------------------------------
-- reconstruction du sens de parcours des trajets par rapport a l'orientation des voiries

alter table trajet_2009 add column sens int2;

-- mise a jour du sens en comparant le troncon n et le n+1= en fonction
-- des points de depart et d'arrivée des deux troncons connectés.
-- met a jour le sens pour tous les troncons sauf le premier.
update trajet_2009 set sens = foo.sens from (
  select  t1.link_id, t1.idtraj, t1.ordre,
  -- condition sur le sens:
  case when st_endpoint(t.geom) = st_startpoint(t1.geom) or
            st_startpoint(t.geom) = st_startpoint(t1.geom)  then 1
       when st_endpoint(t.geom) = st_endpoint(t1.geom) or
            st_startpoint(t.geom) = st_endpoint(t1.geom)  then -1
  else 0 end as sens
  from trajet_2009 t, trajet_2009 t1
  where t.link_id <> t1.link_id
  and t1.ordre = t.ordre + 1
  and t.idtraj = t1.idtraj
  order by t.idtraj, t.ordre
) as foo
where trajet_2009.link_id = foo.link_id
and trajet_2009.idtraj = foo.idtraj;

-- mise a jour du sens pour le premier troncon
update trajet_2009 set sens = foo.sens from (
  select t.link_id, t.idtraj,
  -- condition sur le sens:
  case when st_endpoint(t.geom) = st_startpoint(t1.geom) or
            st_startpoint(t.geom) = st_startpoint(t1.geom)  then 1
       when st_endpoint(t.geom) = st_endpoint(t1.geom) or
            st_startpoint(t.geom) = st_endpoint(t1.geom)  then -1
  else 0 end as sens
  from trajet_2009 t, trajet_2009 t1
  where t.ordre = 1 and t1.ordre = 2
  and t.idtraj = t1.idtraj
  --order by t.idtraj, t.ordre
) as foo
where trajet_2009.link_id = foo.link_id
and trajet_2009.idtraj = foo.idtraj;

--forcage du sens a 0 pour les segments seuls: pas de sens pour ces segmets
update trajet_2009 set sens = 0 where sens is null;

-- modification de la table trajet_2009 pour enlever les colonnes inutiles et ajouter
-- les colonnes de trajet qui manque

alter table trajet_2009 drop column gid;
alter table trajet_2009 drop column node_id;
alter table trajet_2009 rename link_id to code_gs;

-- lancement de la fonction ajoutant les colonnes de la table trajet
-- dans la table trajet_2009, et qui met a jour la table.
select completeTrajet();

