create table test (
var1 text,
var2 text);

insert into test values 
	( 'x',  'toto'),
	( 'x',  'tutu'),
	( 'y',  'titi'),
	( 'y',  'toto'),
	( 'z',  'tutu'),
	( 'z',  'tutu'),
	( 'x',  'titi'),
	( 'y',  'zzz')
	;

select * from test
where (var1 = 'x' or var1 = 'y')
and (var2 = 'toto' or var2 = 'titi');

select * from test
where var1 = 'x' or var1 = 'y'
and var2 = 'toto' or var2 = 'titi';

with pg_points as (
	select st_dumppoints(st_exteriorRing(
		'POLYGON (( 120 300, 180 340, 180 300, 200 300, 157.5 292.5, 186 250.5, 129.5 222, 100 240, 115.5 256, 140 280, 140 300, 120 300 ))'::geometry)
		) as dump
) select --(p1.dump).path[1] as p1, 
	--(p2.dump).path[1] as p2,
	min(st_distance((p1.dump).geom, (p2.dump).geom)), 
	max(st_maxDistance((p1.dump).geom, (p2.dump).geom))
from pg_points p1, pg_points p2
where (p1.dump).path[1] <> (p2.dump).path[1];

with pg as (
	select
	--'POLYGON (( 120 300, 180 340, 180 300, 200 300, 157.5 292.5, 186 250.5, 129.5 222, 100 240, 115.5 256, 140 280, 140 300, 120 300 ))'::geometry 
	'POLYGON (( 318.5 254, 254 354.5, 284 266, 243.5 347, 225 290.5, 240 240, 280 220, 300 240, 318.5 254 ))'::geometry
 as geom
), pg_points as (
	select st_dumppoints(st_exteriorRing(geom)) as dump 
	from pg
) select --(p1.dump).path[1] as p1, 
	--(p2.dump).path[1] as p2,
	st_shortestLine((p1.dump).geom, (p2.dump).geom),
	st_longestLine((p1.dump).geom, (p2.dump).geom),
	
	st_distance(st_shortestLine((p1.dump).geom, (p2.dump).geom)), 
	st_distance(st_longestLine((p1.dump).geom, (p2.dump).geom)), 
from pg_points p1, pg_points p2
where (p1.dump).path[1] <> (p2.dump).path[1]
-- start/end points skipped, to avoid 0 distance
and (p1.dump).path[1] <> array_length((p2.dump).path, 1)
and (p2.dump).path[1] <> array_length((p1.dump).path, 1)

-- to show shortest and longest lines for these dataset
with pg_points as (
	select st_dumppoints(st_exteriorRing(
	     'POLYGON (( 120 300, 180 340, 180 300, 200 300, 157.5 292.5, 186 250.5, 129.5 222, 100 240, 115.5 256, 140 280, 140 300, 120 300 ))'::geometry
	--	'POLYGON (( 318.5 254, 254 354.5, 284 266, 243.5 347, 225 290.5, 240 240, 280 220, 300 240, 318.5 254 ))'::geometry
)) as dump
) select min(st_distance((p1.dump).geom, (p2.dump).geom)), 
         max(st_maxDistance((p1.dump).geom, (p2.dump).geom))
from pg_points p1, pg_points p2
where (p1.dump).path[1] <> (p2.dump).path[1]
-- start/end points skipped, to avoid 0 distance
and (p1.dump).path[1] <> array_length((p2.dump).path, 1)
and (p2.dump).path[1] <> array_length((p1.dump).path, 1);


