-- breaks contour into multilinestrings, creating gaps between segments:
-- if 2 consecutive points are closer than 200m, it is removed
-- creates segments between points
drop table smallseg;
create table smallseg as (
with points as (
	select gid, elev, st_dumppoints(geom) as d from contour
	order by gid, (st_dumppoints(geom)).path[2]
) select gid, elev, st_collect(geom) as geom
from (
	select	p1.gid, p1.elev, st_makeline((p1.d).geom, (p2.d).geom) as geom
	from points p1, points p2
	where p1.elev = p2.elev 
	and p1.gid = p2.gid
	and (p2.d).path[2] = (p1.d).path[2] + 1
	and st_length(st_makeline((p1.d).geom, (p2.d).geom)) <= 150
	) as foo
group by gid, elev
);

create index smallseg_geom_idx on smallseg using gist(geom);

-- intersects with original layer to produce gaps
drop table gap_contour;
create table gap_contour as (
	select c.gid, c.elev, st_difference(c.geom, s.geom) as geom
	from contour c, smallseg s
	where c.gid = s.gid
	and st_intersects(c.geom, s.geom)
);

with recursive tab as (
	-- begins with first segment of each contour: makes a line with it and its closest segment
	-- storing already processed segments as a stop conditition
	select s.gid, t.closest as path, array[s.path, t.closest] as paths,
	-- handle segments orientation to guarantee that closest segments will be oriented
	-- such that calling st_makeline(g1, g2) will connect the shortest distance
	st_makeShortestLine(s.geom, t.geom) as geom, 1 as rank
	from seg s, tmp t
	where s.path = 1
	and s.path = t.path
	and s.gid = t.gid
	and t.r = 1
	and s.gid = 334

	UNION ALL
w
	-- takes closest segment from current one 
	select tab.gid, tmp.closest, paths || tab.path,  
	st_makeShortestLine(tab.geom, tmp.geom) as geom, rank+1
	from tmp, tab
	where tmp.gid = tab.gid
	and tmp.path = tab.path
	and not (tmp.closest = any(paths)) 
) select * from tab 
order by gid, rank desc;
w