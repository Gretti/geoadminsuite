truncate voisins;
insert into voisins values (1, 2, 0), (2, 3, 0), (4, 3, 0), (4, 1, 0), (5, 6, 0);

-- une ligne = couple de voisins
with vois as (
	select array[ind1, ind2] as voi, dist_min_bat 
	from voisins
) select distinct voi, ind1, ind2
from vois, voisins v
where ind1 = any(voi) or ind2 = any(voi);

--trouver les autres voisins de chaque couple
select v1.ind1, v1.ind2, v2.ind1, v2.ind2 
from voisins v1, voisins v2
where v1.ind1 = v2.ind2;

with recursive vois as (
	-- non recursive part
	select array[ind1, ind2] as voi
	from voisins

	UNION ALL

	select  uniq(sort(array_cat(voi, array[ind1, ind2]))) 
	from voisins, vois
	where (ind1 = any(voi) or ind2 = any(voi))
	
) select distinct uniq(sort(voi))  from 
(select voi from vois
limit 20000
) as foo
order by uniq;

with recursive vois as (
	-- non recursive part
	select array[ind1, ind2] as voi, '{}'::int[]  as found 
	from voisins

	UNION ALL

	select 
	case when ind1 = any(voi) then uniq(sort(voi)) || ind1
	when ind2 = any(voi) then uniq(sort(voi)) || ind2
	else voi 
	end as voi,
	case when ind1 = any(voi) then uniq(sort(found)) || ind1
	when ind2 = any(voi) then uniq(sort(found)) || ind2
	else found 
	end as found
	
	from voisins, vois
	where (ind1 = any(voi) or ind2 = any(voi))
	and (not (ind1 = any(found)) and not (ind1 = any(found)))
	
) select distinct * from 
(select * from vois limit 2000
) as foo;