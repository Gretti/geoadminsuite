-- admin toolbox

-- list size of all databases, sorting biggest first
create or replace function listDbSize(biggest boolean) returns setof text as $$
    DECLARE
     ord text := '';
     q text := '';
    BEGIN
        select case 
            when biggest then 'DESC'
            ELSE 'ASC' END into ord;
        raise notice 'ord: %', ord;
        q := E'with db as ( '
            ||E'select datname ' 
            ||E'from pg_database '
        ||E') select datname || '': '' || pg_size_pretty(size) ' 
        ||E'from ( '
            ||E'select datname, pg_database_size(datname) as size ' 
            ||E'from db '
            ||E'order by size '||ord
        ||E') as foo';
        return query execute q;
    END;
$$ language PLPGSQL STRICT;


--------------------------------------------- GIS ----------------------------------
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

-- list table size (total, index) for all non catalog tables
with list as (
    select table_name, table_schema
    from information_schema.tables
    where table_name not like 'pg_%' and table_name not like '_pg%'
), size_ as ( 
    select table_schema, table_name, 
        pg_relation_size(table_schema||'.'||table_name) as table_size, 
        pg_total_relation_size(table_schema||'.'||table_name) as total_table_size
    from list
) select table_schema, table_name, pg_size_pretty(table_size) as pts,
    pg_size_pretty(total_table_size) as ptts,
    pg_size_pretty(total_table_size-table_size) as pis,
    table_size as ts,
    total_table_size as tts,
    total_table_size-table_size as tis
from size_
order by 7 desc;