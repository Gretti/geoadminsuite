-- segments are ordered in the collection of segments:
-- makeLine will connect them:
with lines as (
	select elev, (st_dump(geom)).geom as geom
	from oc
	order by elev, (st_dump(geom)).path
) select elev, st_forceClosed(st_makeLine(geom)) as geom
from lines
group by elev;

-- segments are not ordered into the collection: (each object is a multilinestring representing a contour)
-- if gaps between segments are short compare to segments length:
-- for each segment, find the 2 closest segments, stores them in a table
-- and rebuild lines with a recursive function to join them based on distance

-- first we prepare a table of single segments, keeping individual path
--drop table seg;
create table seg as (
	select gid, (st_dump(geom)).path[1] as path, (st_dump(geom)).geom as geom
	from gap_contour where not st_isClosed(geom)
);

update seg set path = p from (select gid, row_number() over (partition by elev) as p from seg) as foo where foo.gid = seg.gid;
-- creates the table giving, for each contour segment, the 2 closest contour with the same elevation
-- (closest based on segment ends)
drop table tmp;
create table tmp as (
	with closest as (
		select s1.elev as elev, s1.path as path, s2.path as closest, 
		s2.geom,
		-- if segments are touching: row_number
		row_number() over (
		-- else if there are gaps, rank will order closest segments first
		--rank() over (
			partition by s1.elev, s1.path 
			order by s1.elev, s1.path, 
				st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom)))) as r
		from seg s1, seg s2
		where s1.elev = s2.elev
		and s1.path <> s2.path
	) select distinct * from closest
	where r < 3
);

select * from tmp;

-- iteration among each contour's set to connect the closed segment to the current one:
-- uses tmp table to find which segment to connect, storing already processed segments
-- as the stop condition for the iteration
drop table merged_contour;
create table merged_contour as (
with recursive tab as (
	-- begins with first segment of each contour: makes a line with it and its closest segment
	-- storing already processed segments as a stop conditition
	select s.elev, t.closest as path, array[s.path, t.closest] as paths,
	-- handle segments orientation to guarantee that closest segments will be oriented
	-- such that calling st_makeline(g1, g2) will connect the shortest distance
	st_makeShortestLine(s.geom, t.geom) as geom, 1 as rank
	from seg s, tmp t
	where s.path = 1
	and s.path = t.path
	and s.elev = t.elev
	and t.r = 1

	UNION ALL

	-- takes closest segment from current one 
	select tab.elev, tmp.closest, paths || tab.path,  
	st_makeShortestLine(tab.geom, tmp.geom) as geom, rank+1
	from tmp, tab
	where tmp.elev = tab.elev
	and tmp.path = tab.path
	and not (tmp.closest = any(paths)) 
) select distinct on (elev) elev, rank, st_forceClosed(geom) as geom from (
	select * from tab 
	order by elev, rank desc
) as foo
);
-- takes only top results for each elevation: the complete line.
select  st_distance(st_endPoint(g1), st_endPoint(g2)) as dee,
	        st_distance(st_endPoint(g1), st_startPoint(g2)) as des,
	        st_distance(st_startPoint(g1), st_endPoint(g2)) as dse,
	        st_distance(st_startPoint(g1), st_startPoint(g2)) as dss
from (
	select t1.geom as g1, t2.geom as g2
	from seg t1, merged_contour t2
	where t1.elev = 200 and t1.path = 2
	and t2.elev = 200 and t2.path = 3
 ) as foo;

select st_distance(st_startPoint(g2), st_startPoint(g1))
from (
	select t1.geom as g1, t2.geom as g2
	from seg t1, merged_contour t2
	where t1.elev = 200 and t1.path = 2
	and t2.elev = 200 and t2.path = 3
 ) as foo;











