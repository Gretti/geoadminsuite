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


update stats set nb_trajets_initiaux = (
	select sum(count) 
	from (select count(*) from trajet_full
		UNION
		select count(distinct idtraj) from trajet_2009_ok) as t2
) where proc_start is not null and proc_end is null;

update stats set nb_trajets_2009 = (
	select count(distinct idtraj) from trajet_2009
) where proc_start is not null and proc_end is null;

update stats set nb_trajets_rejet = (
	select count(*) from trajet_rejet
) where proc_start is not null and proc_end is null;

update stats set sum_2009_rejet = (
	select sum(count) from (
		select count(distinct idtraj)  from trajet_2009
		UNION
		select count(*) from trajet_rejet
		) as foo
) where proc_start is not null and proc_end is null;

update stats set sum_lgtron2006 =(
	select sum(somm) from (
	select sum(lgtron2006)/1000 as somm from trajet
	UNION
	select sum(lgtron2009)/1000 as somm from trajet_2009_ok
	) as t
) where proc_start is not null and proc_end is null;

update stats set sum_lgtron2009 = (
	select sum(lgtron2009)/1000 from trajet_2009
) where proc_start is not null and proc_end is null;

update stats set sum_lg_rejet = (
	select sum(lgtron2006)/1000 from trajet_rejet
) where proc_start is not null and proc_end is null;

update stats set nb_trajets_courts = (
	select count(*) 
	from trajet_startend_voirie where short
) where proc_start is not null and proc_end is null;

update stats set nb_trajets_pgrouting = (
	select count(distinct idtraj) 
	from trajet_routing
) where proc_start is not null and proc_end is null;


update stats set prc_long = (
	select (l9 / l6) * 100 
	from
	(select sum(somm) as l6 from (
	  select sum(lgtron2006)/1000 as somm from trajet
	  UNION
	  select sum(lgtron2009)/1000 as somm from trajet_2009_ok
	) as t) as foo,
	(select sum(lgtron2009)/1000 as l9 from trajet_2009) as bar
) where proc_start is not null and proc_end is null;

update stats set nb_trajets_ko_pgrouting = (
	select count(*) from trajet_routing_ko
) where proc_start is not null and proc_end is null;
	

