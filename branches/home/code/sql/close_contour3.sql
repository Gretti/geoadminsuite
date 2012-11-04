-- map extent as a linestring: defines the data frame
create table frame as (
	select st_exteriorRing(st_setSRID(st_extent(geom)::geometry, 2193)) as geom 
	from contour_100
);

with ext as (
	select st_exteriorRing(st_setSRID(st_extent(geom)::geometry, 2193)) as geom 
	from contour_100
)
select s.* 
from seg s, ext e 
where st_dwithin(st_startpoint(s.geom), e.geom, 0.001)
and st_dwithin(st_endpoint(s.geom), e.geom, 0.001);

-- First try to close contours at map edge:
-- identify segments that should be closed:

--tables of segments composing linestrings, keeping path as identifier
drop table seg;
create table seg as (
	select elevint as elev, (st_dump(geom)).path[1] as path, (st_dump(geom)).geom as geom
	from contour_100
);
-- adds gid column to seg
alter table seg add column gid serial primary key; 
-- spatial index on geom and index on evel
create index seg_geom_gist on seg using gist(geom);
create index seg_elev_idx on seg (elev);

-- keep closed lines and removes them from seg table
drop table seg_closed ;
create table seg_closed as (
	select * from seg where st_isclosed(geom)
);
delete from seg where st_isclosed(geom);

-- also removes all lines touching the map extent at start AND end points: These lines should be closed with
-- map extent boundary, or left opened for later processing
drop table seg_border;
create table seg_border as (
	select s.*
	from seg s, frame f 
	where st_dwithin(st_startpoint(s.geom), f.geom, 0.001)
	and st_dwithin(st_endpoint(s.geom), f.geom, 0.001)
);
delete from seg s using seg_border sb
where s.gid = sb.gid;

-- assoc table: for each segment, gives the 2 closest with same elev
drop table tmp;
create table tmp as (
	with closest as (
		select s1.elev as elev, s1.gid, s1.path as path, s2.gid as closest,
		s2.geom,
		st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom))) as dist,
		row_number() over (
			partition by s1.gid 
			order by s1.gid,
				st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom)))) as r
		from seg s1, seg s2
		where s1.elev = s2.elev
		and s1.gid <> s2.gid
	) select distinct * from closest 
	where r < 3 and dist < 100
	order by elev, gid, r
);

-- then first iteration to treat all lines touching map frame: 
-- it will be our "seed" to start iteration: each segment touching the frame will be completed
drop table merged_contour1;
create table merged_contour1 as (
	with recursive tab as (
		-- begins with first segment of each contour: makes a line with it and its closest segment
		-- storing already processed segments as a stop conditition
		select s.gid, s.elev, t.closest, array[s.gid, t.closest] as gids,
		-- handle segments orientation to guarantee that closest segments will be oriented
		-- such that calling st_makeline(g1, g2) will connect the shortest distance
		st_makeShortestLine(s.geom, t.geom) as geom, 1 as rank
		from seg s, tmp t, frame f
		where s.gid = t.gid
		and s.elev = t.elev
		-- takes lines touching the map frame at one end point
		and (st_dwithin(st_startpoint(s.geom), f.geom, 0.001)
		     or st_dwithin(st_endpoint(s.geom), f.geom, 0.001))
		--and s.gid = 52

		UNION ALL

		-- takes closest segment from current one 
		select tab.gid, tab.elev, tmp.closest, gids || tmp.closest,  
		st_makeShortestLine(tab.geom, tmp.geom) as geom, rank+1
		from tmp, tab
		where tmp.elev = tab.elev
		and tmp.gid = tab.closest
		and not (tmp.closest = any(gids)) 
		--and tmp.elev = 100		
	) select distinct on (gid) gid, elev, rank, gids, geom from (
		select * from tab 
		order by gid, rank desc
	) as foo
);


-- these segments are then removed from original table, to keep only those segments entirely contained 
-- in the map frame, to close them later
-- merged_contour1.gids is the array of all gid used to form the contour.
-- use it to remove segments treated
delete from seg s using merged_contour1 m
where s.gid = any(gids);

-- If there are still rows in seg, one should rebuild a new association table
-- based on these rows, and relaunch an iteration to merge these segments.
-- it will be hard to identify segments if several contours
-- should try to build them all and use a distinct clause on geometries at the end to remove duplicate contours

-- recreate assication with remaining lines
drop table tmp;
create table tmp as (
	with closest as (
		select s1.elev as elev, s1.gid, s1.path as path, s2.gid as closest,
		s2.geom,
		st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom))) as dist,
		row_number() over (
			partition by s1.gid 
			order by s1.gid,
				st_distance(st_collect(st_startpoint(s1.geom), st_endpoint(s1.geom)), st_collect(st_startpoint(s2.geom), st_endpoint(s2.geom)))) as r
		from seg s1, seg s2
		where s1.elev = s2.elev
		and s1.gid <> s2.gid
	) select distinct * from closest 
	where r < 3 and dist < 100
	order by elev, gid, r
);

-- re-iterate to close remaining contours: brute force is used:
-- each remaining segment will be merged with its neighboors, leading to several duplicate lines:
-- as many duplicate as there are lines in a contour.
-- final distinct will remove duplicates.
-- caution: distinct uses geom bbox => should check if 2 contours can have the same bbox. guess not.
-- here, we force close the contours, as remaining segments should form closed contour.
drop table merged_contour2;
create table merged_contour2 as (
	with recursive tab as (
		-- begins with first segment of each contour: makes a line with it and its closest segment
		-- storing already processed segments as a stop conditition
		select s.gid, s.elev, t.closest, array[s.gid, t.closest] as gids,
		-- handle segments orientation to guarantee that closest segments will be oriented
		-- such that calling st_makeline(g1, g2) will connect the shortest distance
		st_makeShortestLine(s.geom, t.geom) as geom, 1 as rank
		from seg s, tmp t, frame f
		where s.gid = t.gid
		and s.elev = t.elev

		UNION ALL

		-- takes closest segment from current one 
		select tab.gid, tab.elev, tmp.closest, gids || tmp.closest,  
		st_makeShortestLine(tab.geom, tmp.geom) as geom, rank+1
		from tmp, tab
		where tmp.elev = tab.elev
		and tmp.gid = tab.closest
		and not (tmp.closest = any(gids)) 
		--and tmp.elev = 100		
	) --select distinct on (geom) geom, gid, elev, rank, gids from (
		select distinct on (gid) gid, elev, rank, gids, st_forceClosed(geom) as geom from (
			select * from tab 
			order by gid, rank desc
		) as foo
	) as bar
);


-- the final association will group all contours based on same elevation, from our working tables
-- seg_closed, seg_border, merged_contour1 and merged_contour2
drop table if exists all_contour;
create table all_contour as (
	with all_c as (
		select elev, geom from merged_contour1
		UNION
		select elev, geom from merged_contour2
		UNION
		SELECT elev, geom from seg_border
		UNION
		select elev, geom from seg_closed
	) select elev, st_union(geom) as geom
	from all_c
	group by elev
);

select elev, st_numgeometries(st_union) from all_contour order by elev;

create table toto as (
select elev, (st_dump(st_union)).* 
from all_contour);
