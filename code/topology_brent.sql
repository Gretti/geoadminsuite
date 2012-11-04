-- SUMMARY:
--  Uses geometry linestrings
--  Adds these to topology to create polygons
--  Uses table of geometry points & attributes 
--  Extracts topo polygons to match points & attributes
--  Checks for points with no polygons & points sharing a polygon

create table lines
                 ( id       serial primary key, 
                   name     varchar(6),
                   geom     geometry(LINESTRING,4326));

insert into lines values
                 (default,
                  'top',
                  ST_Setsrid(ST_Makeline(ST_Makepoint(175,-44),ST_Makepoint(179,-44)),4326)
                 );

insert into lines values
                 (default,
                  'bottom',
                  ST_Setsrid(ST_Makeline(ST_Makepoint(175,-45),ST_Makepoint(179,-45)),4326)
                 );

insert into lines values
                 (default,
                  'left',
                  ST_Setsrid(ST_Makeline(ST_Makepoint(176,-43),ST_Makepoint(176,-46)),4326)
                 );

insert into lines values
                 (default,
                  'middle',
                  ST_Setsrid(ST_Makeline(ST_Makepoint(177,-43),ST_Makepoint(177,-46)),4326)
                 );

insert into lines values
                 (default,
                  'right',
                  ST_Setsrid(ST_Makeline(ST_Makepoint(178,-43),ST_Makepoint(178,-46)),4326)
                 );


-- create new empty topology structure
select CreateTopology('topo1',4326,0);

-- add all linestrings to topology in one operation as a collection
select ST_CreateTopoGeo('topo1',ST_Collect(geom))
                 from lines;

-- creates topology like this (X = polygons - faces, + = nodes)
--   +   +   +
--   |   |   |
-- +-+---+---+--+
--   | X | X |
-- +-+---+---+--+
--   |   |   |
--   +   +   +

-- validate topology - identify errors
select * FROM  ValidateTopology('topo1');

-- list topology contents
select TopologySummary('topo1');

-- list polygons in ExtWKT format
select ST_AsEWKT(ST_GetFaceGeometry('topo1', 1));
select ST_AsEWKT(ST_GetFaceGeometry('topo1', 2));

-- create stratum polygon table for geometries as polygons
create table poly
                  ( id      serial primary key,
                    topo_id integer,
                    name    varchar(10),
                    point   geometry(point,4326),
                    geom    geometry(polygon,4326));

-- populate polygon table with non-polygon data - 
--   use points to geolocate the created strata, & associate the faces as geometries 
insert into poly (id, name, point) values
                 (default,
                  'stra1',
                  ST_Setsrid(ST_Makepoint(176.5,-44.5),4326));  
insert into poly (id, name, point) values
                 (default,
                  'stra2',
                   ST_Setsrid(ST_Makepoint(177.5,-44.5),4326));

-- insert a record with a point outside of any generated polygon to test error case
insert into poly (id, name, point) values
                 (default,
                  'stra3',
                   ST_Setsrid(ST_Makepoint(177,-43.9),4326));

-- insert a record with a point inside the same stratum polygon to test error case
insert into poly (id, name, point) values
                 (default,
                  'stra4',
                   ST_Setsrid(ST_Makepoint(177.5,-44.4),4326));

-- check for any points without generated polygons
select 'ERROR: no enclosing polygon for point' as error,
                         name,
                         id,
                         ST_Astext(point) as point
                 from poly
                 where GetFaceByPoint('topo1',point,0) = 0;

-- add polygons from topology matching the points
update poly set geom=topology.ST_GetFaceGeometry('topo1',GetFaceByPoint('topo1',point,0))
where GetFaceByPoint('topo1',point,0) != 0;

-- check for any points with the same stratum
select 'ERROR: points in same polygon' as error,
                 name,
                 id,
                 ST_Astext(point) as point
from poly where geom in 
                   ( select geom from poly
                     group by geom
                     having count(geom) >1);

select id, name, geom from poly where geom is not null;
