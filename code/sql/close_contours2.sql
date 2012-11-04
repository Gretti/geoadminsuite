update seg set path = p 
from (
      select sid, row_number() over (partition by elev) as p from seg
) as foo where foo.sid = seg.sid; 


drop table tmp;
create table tmp as (
	with closest as (
		select s1.elev as elev, s1.path as path, s2.path as closest, 
		s2.geom,
		row_number() over (
			partition by s1.elev, s1.path 
			order by s1.elev, s1.path, 
				st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom)))) as r
		from seggap s1, seggap s2
		where s1.elev = s2.elev
		and s1.path <> s2.path
	) select distinct * from closest
	where r < 3
);

drop table merged_contour ;
create table merged_contour as (
	with recursive tab as (
		-- begins with first segment of each contour: makes a line with it and its closest segment
		-- storing already processed segments as a stop conditition
		select s.elev, t.closest as path, array[s.path, t.closest] as paths,
		-- handle segments orientation to guarantee that closest segments will be oriented
		-- such that calling st_makeline(g1, g2) will connect the shortest distance
		st_makeShortestLine(s.geom, t.geom) as geom, 1 as rank
		from seggap s, tmp t
		where s.path = 1
		and s.path = t.path
		and s.elev = t.elev
		and t.r = 1
		--and s.elev = 100

		UNION ALL

		-- takes closest segment from current one 
		select tab.elev, tmp.closest, paths || tmp.closest,  
		st_makeShortestLine(tab.geom, tmp.geom) as geom, rank+1
		from tmp, tab
		where tmp.elev = tab.elev
		and tmp.path = tab.path
		and not (tmp.closest = any(paths)) 
		--and tmp.elev = 100		
	) select distinct on (elev) elev, rank, st_forceClosed(geom) from (
		select * from tab 
		order by elev, rank desc
	) as foo
);

select st_touches(st_endPoint(t.geom), st_endPoint(s.geom)) 
from seg s, merged_contour t
where t.elev = 200 and t.path = 3
and s.elev = 200 and s.path=2;