-- creates the table of the new communes
-- multipg exploded into simple polygons
-- keeps a point-on-surface for each polygon, to spatially associate objects
-- at the end of the process
drop table if exists new_comm;
create table new_comm as (
	select gid, (st_dump(geom)).*, st_pointOnSurface((st_dump(geom)).geom) as pton 
	from commune
);
create index new_comm_geom_gist on new_comm using gist(geom);

-- create new empty topology structure
select CreateTopology('topo1',2154,0);

-- add all communes polygons to topology in one operation as a collection
select ST_CreateTopoGeo('topo1',ST_Collect(geom))
from commune;

-- create a new topo based on the simplification of existing one
-- should not be the right way to do it, but calling ST_ChangeEdgeGeom failed with a simplify linestring
select CreateTopology('topo2',2154,0);

select ST_CreateTopoGeo('topo2', geom)
from (
	select ST_Collect(st_simplifyPreserveTopology(geom, 10000)) as geom
	from topo1.edge_data
) as foo;

-- associates new simplified faces with points on surface, in the new_comm table
alter table new_comm add column simple_geom geometry(POLYGON, 2154);

-- retrieves polygons by comparing surfaces (pip is not enough for odd-shaped polygons)
with simple_face as (
	select st_getFaceGeometry('topo2', face_id) as geom 
	from topo2.face 
	where face_id > 0
) update new_comm d set simple_geom = sf.geom
from simple_face sf
where st_intersects(d.geom, sf.geom)
and st_area(st_intersection(sf.geom, d.geom))/st_area(sf.geom) > 0.5;

-- commune layer simplified...
-- some cleaning
--select dropTopology('topo2');
--select dropTopology('topo1');