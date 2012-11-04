-- colonne idtraj officielle
alter table test add column idtrajoff text;
update test set idtrajoff = idm || '_' || nd || '_' || nt;
create index test_idtrajoff_idx on test (idtrajoff);
create index test_ordre_idx on test (ordre);


update test set idm = (case length(arr[1]) when 0 then 0::int else arr[1]::int end), 
	nd = (case length(arr[2]) when 0 then 0::int else arr[2]::int end), 
	nt = (case length(arr[3]) when 0 then 0::int else arr[3]::int end), 
	st = (case  array_length(arr, 1) when 3 then 0::int else arr[4]::int end);

select count(*) from test where st = 0;

-- la nouvelle colonne idtraj devient alors
-- ... set newidtraj = idm || '_' || nd || '_' || nt;

-- nombre max de sous-trajet
select max(st) from test;

-- nombre de trajets comportant n sous trajets
with c as (
	select (count(idtraj)::float/20000.0)*100.0 as "% of st" , st from test group by st
) select * from c order by st;

