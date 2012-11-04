with points as (
	select 'POINT (0 0)'::geometry as geom
	UNION
	select 'POINT (1 1)'::geometry as geom
	UNION
	select 'POINT (1 1)'::geometry as geom
) select st_convexHull(st_collect(geom))
from points;

-- tells if points pt lies at left (1), right (-1) or on (0) the line
-- formed by vector [A,B].
-- -1 if point lies at left of (A,B) vector, 
--  1 if point lies at right,
--  0 if point is on the (A,B) line.
-- an error:
-- ERROR: Argument to X() or Y() must be a point
-- SQL state: XX000
-- if arguments are not points
-- 
-- usage: select point_side(pointA, pointB, pointC);
-- example:
with points as (
) select point_side(a, b, pt);
-- From: [postgis-users] Looking for function to analyze linestring and	points
-- Eric Randall eprand at earthlink.net 
-- 
--  If your polyline goes from A to B and your point is P,
-- v = (Ay-By)*Px+(Bx-Ax)*Py+(Ax*By-Bx*Ay)
-- v>0: "Left"
--  v<0: "Right"
--  v=0: "On the line" (or in front of B / behind A)
create or replace function point_side(A geometry, B geometry, P geometry) returns int as $$
	with cpt as (
		select (st_y($1) - st_y($2)) * st_x($3) + (st_x($2) - st_x($1)) * st_y($3) 
		+ (st_x($1)*st_y($2) - st_x($2)*st_y($1)) as cp
	) select case when cp < 0 then -1
	when cp > 0 then 1
	else 0 end
	from cpt;
$$ language SQL immutable strict;

with points as (
	select 'POINT (0 0)'::geometry as A, 
	'POINT (1 1)'::geometry as B,
	'POINT (0 1)'::geometry as pt
) select point_side(A, B, pt)
from points;
