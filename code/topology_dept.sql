-- creates the table of the new departements
-- multipg exploded into simple polygons
-- keeps original polygons, to spatially associate objects
-- at the end of the process
drop table if exists new_dept;
create table new_dept as (
	select gid, code_dept, (st_dump(geom)).path[1] as path, (st_dump(geom)).geom as geom  
	from departement
);
create index new_dept_geom_gist on new_dept using gist(geom);

-- create new empty topology structure
select CreateTopology('topo1',2154,0);

-- add all departements polygons to topology in one operation as a collection
select ST_CreateTopoGeo('topo1',ST_Collect(geom))
from departement;

-- create a new topo based on the simplification of existing one
-- should not be the right way to do it, but calling ST_ChangeEdgeGeom failed with a simplify linestring
select CreateTopology('topo2',2154,0);

select ST_CreateTopoGeo('topo2', geom)
from (
	select ST_Collect(st_simplifyPreserveTopology(geom, 10000)) as geom
	from topo1.edge_data
) as foo;

-- associates new simplified faces with points on surface, in the new_dept table
alter table new_dept add column simple_geom geometry(POLYGON, 2154);

-- retrieves polygons by comparing surfaces (pip is not enough for odd-shaped polygons)
with simple_face as (
	select st_getFaceGeometry('topo2', face_id) as geom 
	from topo2.face 
	where face_id > 0
) update new_dept d set simple_geom = sf.geom
from simple_face sf
where st_intersects(d.geom, sf.geom)
and st_area(st_intersection(sf.geom, d.geom))/st_area(sf.geom) > 0.5;

-- other method: area sorting, distinct on gid, path, keeps only bigger
--update new_dept n set simple_geom = foo.geom
--from (
--	select distinct on (d.gid, d.path) d.gid, d.path, st_area(st_intersection(d.geom, sf.geom)), sf.geom, d.geom as inigeom
--	from new_dept d, -
--	(
--		select st_getFaceGeometry('topo2', face_id) as geom 
--		from topo2.face 
--		where face_id > 0
--	) as sf
--	where st_intersects(d.geom, sf.geom)
--	and not st_contains(sf.geom, d.geom)
--	and code_dept = '65'
--	order by d.gid, d.path, st_area desc
--) as foo
--where n.gid = foo.gid and n.path = foo.path; 

-- departement layer simplified...
-- some cleaning

select dropTopology('topo2');
select dropTopology('topo1');

-- create new empty topology structure
select CreateTopology('topo1',2154,0);

drop table topo_dep;
create table topo_dep as (
	select gid as ori_gid, code_dept
	from departement
);

select addTopoGeometryColumn('topo1', 'public', 'topo_dep', 'topogeom', 'MultiPolygon');

INSERT INTO topo_dep(ori_gid, code_dept, topogeom)
SELECT gid, code_dept, topology.toTopoGeom(geom, 'topo1', 1)
FROM departement;

SELECT * FROM 
    topology.TopologySummary('topo1'); 

-- how to simplify/update edges ?
-- new edges, update ? direct simplify

-- updates edge:
select ST_ChangeEdgeGeom('topo1', edge_id, st_simplify(geom, 10000))
from topo1.edge_data
where st_isvalid(st_simplify(geom, 10000))
and st_issimple(st_simplify(geom, 10000));

-- create table of simplified geomtries


select edge_id, st_issimple(st_simplify(geom, 1000))
from topo1.edge_data; 

select edge_id, st_simplify(geom, 1000)
from topo1.edge_data; 

update topo1.edge_data  set geom = st_simplify(geom, 10000);

-- still valid ?
SELECT * FROM  ValidateTopology('topo1');

select st_isvalidreason(geom) from topo1.edge_data  ;

-- copy topology


-- update where st_isvalid

-- rebuild polygons for faces containing new valid edges

-- this is the extraction of the faces where simplification worked.

