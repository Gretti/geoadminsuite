-- extract polygons out of MultiPolygons, keeping code_dept attribute
drop table poly;
create table poly as (
select gid, code_dept, (st_dump(geom)).* from departement
);

-- extract rings out of polygons
drop table rings;
create table rings as select st_exteriorRing((st_dumpRings(geom)).geom) as g from poly;

-- Simplify the rings. Here, no points further than 10km:
drop table simplerings;
create table simplerings as select st_simplifyPreserveTopology(st_linemerge(st_union(g)), 10000) as g from rings;

-- extract lines as individual objects, in order to rebuild polygons from these
-- simplified lines
drop table simplelines;
create table simplelines as select (st_dump(g)).geom as g from simplerings;

-- Rebuild the polygons, first by polygonizing the lines, with a 
-- distinct clause to eliminate overlaping segments that may prevent polygon to be created,
-- then dump the collection of polygons into individual parts, in order to rebuild our layer. 
drop table simplepolys;
create table simplepolys as 
select (st_dump(st_polygonize(distinct g))).geom as g
from simplelines;

-- Add an id column to help us identify objects
alter table simplepolys  add column gid serial primary key;

-- some spatial indexes
--create index departement_geom_gist on departement using gist(geom);
create index simplepolys_geom_gist on simplepolys  using gist(g);

-- Attribute association between input layer and simplified polygons:
-- First method to retrieve attribute is based on containment of a point of the surface of simplified polygons
-- It does not work: code_dept=92 is a curved polygon and fails with st_contains() test.
drop table simpledep;
create table simpledep as 
select code_dept, g
from departement d, simplepolys s
where st_contains(d.geom, st_pointOnSurface(s.g));

-- table of depts exploded into individual polygons
drop table expl_dep;
create table expl_dep as (
	select code_dep, 
	case when array_upper((d).path, 1) is null then 0
	else (d).path[1] end as path, 
	(d).geom
	from (select code_dep, st_dump(geom) as d from departement) as foo
);

select code_dep,path from expl_dep  order by code_dep, path;

-- works better: comparing percentage of overlaping area gives better results.
-- as input set is multipolygon, we first explode departements into their polygons, to
-- be able to find islands and set them the right departement code.
drop table simpledep;
create table simpledep as (
	select distinct on (d.code_dept, d.gid) d.code_dept, d.gid, s.g as geom
	from departement d, simplepolys s
	where st_intersects(d.geom, s.g)
	order by d.code_dept, gid, st_area(st_intersection(s.g, d.geom))/st_area(s.g) desc
);

-- code_dept association is now correct:

-- rebuild departements by grouping them by code_dept
create table simple_departement as (
	select code_dept, st_collect(geom) as geom
	from simpledep
	group by code_dept
);

select distinct on (d.code_dept) d.code_dept, s.g as geom
from departement d, simplepolys s
where st_intersects(d.geom, s.g)
order by d.code_dept, st_area(st_intersection(s.g, d.geom))/st_area(s.g) desc

-- in one query
with poly as (
	select gid, code_dept, (st_dump(geom)).* 
	from departement
) select distinct on (d.code_dept) d.code_dept, baz.geom 
 from ( 
	select (st_dump(st_polygonize(distinct geom))).geom as geom
	from (
		select (st_dump(st_simplifyPreserveTopology(st_linemerge(st_union(geom)), 10000))).geom as geom
		from (
			select st_exteriorRing((st_dumpRings(geom)).geom) as geom
			from poly
		) as foo
	) as bar
) as baz,
poly d
where st_intersects(d.geom, baz.geom)
order by d.code_dept, st_area(st_intersection(baz.geom, d.geom))/st_area(baz.geom) desc;

select (st_dump(st_simplifyPreserveTopology(st_linemerge(st_union(st_exteriorRing((st_dumpRings(geom)).geom))), 10000))).geom as geom
			from poly

-- on communes
with poly as (
	select gid, code_comm, (st_dump(geom)).* 
	from commune
) select distinct on (d.code_comm) d.code_comm, baz.geom 
 from ( 
	select (st_dump(st_polygonize(distinct geom))).geom as geom
	from (
		select (st_dump(st_simplifyPreserveTopology(st_linemerge(st_union(geom)), 10000))).geom as geom
		from (
			select st_exteriorRing((st_dumpRings(geom)).geom) as geom
			from poly
		) as foo
	) as bar
) as baz,
poly d
where st_intersects(d.geom, baz.geom)
order by d.code_comm, st_area(st_intersection(baz.geom, d.geom))/st_area(baz.geom) desc;


--select distinct on (d.code_dept, d.gid) d.code_dept, d.gid, s.g as geom
select d.code_dept, d.gid, st_area(st_intersection(s.g, d.geom))/st_area(s.g) as prc_area, s.g as geom
from departement d, simplepolys s
where st_intersects(d.geom, s.g)
and st_area(st_intersection(s.g, d.geom))/st_area(s.g) > 0.5
order by d.code_dept, gid, st_area(st_intersection(s.g, d.geom))/st_area(s.g) desc
