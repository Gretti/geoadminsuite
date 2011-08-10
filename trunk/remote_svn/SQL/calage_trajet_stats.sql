-- table des trajets full, pour controle visuel
drop  table if exists trajet_2009_full;
create table trajet_2009_full as
select idtraj, sum(lgtron2009) as lgtron2009, st_linemerge(st_collect(geom)) as geom
from trajet_2009
group by idtraj;

-- 8) -------------------------------------------------------------------------------------------
-- stats:
select sum(count) as "nbre trajets initiaux:"
from (select count(*) from trajet_full
	UNION
     select count(distinct idtraj) from trajet_2009_ok) as t2;

select count(distinct idtraj) as "nbre trajets 2009:"     from trajet_2009;
select count(*) as "nbre trajets rejetes:"                from trajet_rejet;

select sum(count) as "rejet + trajet_2009:" from (
	select count(distinct idtraj)  from trajet_2009
	UNION
	select count(*) from trajet_rejet
) as foo;

select sum(somm) as "longueur trajets 2006 en km" from (
  select sum(lgtron2006)/1000 as somm from trajet
  UNION
  select sum(lgtron2009)/1000 as somm from trajet_2009_ok
) as t;

select sum(lgtron2009)/1000 as "longueur trajets 2009 en km" from trajet_2009;
select sum(lgtron2006)/1000 as "longueur trajets en rejet en km" from trajet_rejet;

select count(*) as "Nbre de trajets trop courts:"
from trajet_startend_voirie where short;

select count(distinct idtraj) as "nombre de trajets pgrouting"
from trajet_routing;

-- somme des longueurs 2009 apres routing:
--select sum(suum)::int4 as "longueur trajets 2009 apres routing en km" from (
--select sum(lgtron2009)/1000 as suum from trajet_2009
--UNION
--select sum(st_length(geom))/1000 from trajet_routing where geom is not null) as foo;

-- pourcentage de longueur
select (l9 / l6) * 100 as "pourcentage de longueur de trajets 2009:"
from
(select sum(somm) as l6 from (
  select sum(lgtron2006)/1000 as somm from trajet
  UNION
  select sum(lgtron2009)/1000 as somm from trajet_2009_ok
) as t) as foo,
(select sum(lgtron2009)/1000 as l9 from trajet_2009) as bar;

-- nombre de trajets avec routing ko:
select count(*) as "trajets non reconstruits par pgRouting:" from trajet_routing_ko;

-- comparaison des longueurs de troncon entre trajet 2006 et 2009
-- il manque les trajets dont les codes voirie sont bons, ils sont sortis de la table des trajets et 
-- stockés dans la table trajet_2009_ok
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

