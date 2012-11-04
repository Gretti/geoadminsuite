create table tmpgeom (id serial primary key, wkt text, geom geometry);

delete from tmpgeom;
copy tmpgeom (wkt) from '/tmp/france.wkt';

update tmpgeom set geom = st_geomFromText(wkt, 4326);

select st_isvalid(geom) from tmpgeom;

update tmpgeom set geom = st_buffer(geom, 0);

SELECT ST_simplify(intersection(way, geom),0.00005), 
height 
 from contours , tmpgeom
 where ST_Intersects(way, tmpgeom.geom);

-- nettoyage = soustraction du polygone qui contient le point d'erreur avec le
-- polygone qui touche le point d'erreur.
-- collect de tous les polygones sauf celui qui touche le point d'erreur

with pgs as 
(
	select (st_dump(geom)).path, (st_dump(geom)).geom from tmpgeom
)
select st_collect(geom) 
from 
(
	select st_difference(p1.geom, p2.geom) as geom
	from pgs p1, pgs p2, 
	(
		select st_setSRID(st_makePoint(coords[1]::numeric, coords[2]::numeric), 4326) as geom 
		from 
		(
			select regexp_matches(st_isvalidReason(geom), '(\d*\.?\d+) (\d*\.?\d+)') as coords 
			from tmpgeom
		) as foo
	) as err_point

	where p1.path <> p2.path
	and  st_contains(p1.geom, err_point.geom)
	and st_touches(p2.geom, err_point.geom)

	UNION ALL

	select p.geom
	from pgs p,
	(
		select st_setSRID(st_makePoint(coords[1]::numeric, coords[2]::numeric), 4326) as geom 
		from 
		(
			select regexp_matches(st_isvalidReason(geom), '(\d*\.?\d+) (\d*\.?\d+)') as coords 
			from tmpgeom
		) as foo
	) as err_point
	where not 
	(
		st_contains(p.geom, err_point.geom)
		or st_touches(p.geom, err_point.geom)
	)
) as bar;
