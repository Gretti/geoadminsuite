create or replace function st_makeShortestLine(g1 geometry, g2 geometry) returns geometry as $$

	with dist as (
		select  st_distance(st_endPoint($1), st_endPoint($2)) as dee,
	        st_distance(st_endPoint($1), st_startPoint($2)) as des,
	        st_distance(st_startPoint($1), st_endPoint($2)) as dse,
	        st_distance(st_startPoint($1), st_startPoint($2)) as dss
	) select case when st_distance(st_startPoint($1), st_startPoint($2)) = 0 then st_makeline(st_reverse($1), $2)
		      when st_distance(st_startPoint($1), st_endPoint($2)) = 0 then st_makeline($2, $1)
		      when st_distance(st_endPoint($1), st_endPoint($2)) = 0 then st_makeline($1, st_reverse($2))
		      when st_distance(st_endPoint($1), st_startPoint($2)) = 0 then st_makeline($1, $2)
		      when dee < des and dee < dse and dee < dss then st_makeline($1, st_reverse($2))
		      when des < dee and des < dse and des < dss then st_makeline($1, $2)
		      when dss < dee and dss < dse and dss < des then st_makeline(st_reverse($1), $2)
		      else st_makeline($2, $1) end
	from dist;
$$ language SQL;
-- return the line made by connecting the shortest ends of given linestrings
-- reverting them if needed
-- also deals with touching ends (using distance, as st_touches does not apply to point/point ;)

CREATE OR REPLACE FUNCTION ST_ForceClosed(geom geometry)
 RETURNS geometry AS
$BODY$BEGIN
 IF ST_IsClosed(geom) THEN
   RETURN geom;
 ELSIF GeometryType(geom) = 'LINESTRING' THEN
   SELECT ST_AddPoint(geom, ST_PointN(geom, 1)) INTO geom;
 ELSIF GeometryType(geom) ~ '(MULTI|COLLECTION)' THEN
   -- Deconstruct parts
   WITH parts AS (
     SELECT CASE
       WHEN NOT ST_IsClosed(gd.geom) AND GeometryType(gd.geom) = 'LINESTRING'
         THEN ST_AddPoint(gd.geom, ST_PointN(gd.geom, 1))
       ELSE gd.geom END AS closed_geom
     FROM ST_Dump(geom) AS gd
   ) -- Reconstitute parts
   SELECT ST_Collect(closed_geom) INTO geom
   FROM parts;
 END IF;
 IF NOT ST_IsClosed(geom) THEN
   RAISE EXCEPTION 'Could not close geometry';
 END IF;
 RETURN geom;
END;$BODY$
 LANGUAGE plpgsql IMMUTABLE COST 100;