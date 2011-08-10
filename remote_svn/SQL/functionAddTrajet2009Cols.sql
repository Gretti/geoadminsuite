create or replace function completeTrajet() returns void as $$
declare
    new_col text := '';
    old_col text := '';
    old_col_type text := ''; -- type of the column.
    col_found boolean := false;
    update_query text := 'update trajet_2009 t9 set ';
    col_list_update text := '';
    col_list_inner text := '';
    update_query_end text := ' where t9.idtraj = t.idtraj;';
    update_query_sub text := ' from (select distinct tr.idtraj, ';
    update_query_sub_end text := ' from trajet tr) as t';
    sep text := '';
    
begin
    
    for old_col, old_col_type in select column_name, data_type 
                from information_schema.columns 
                where table_name = 'trajet' LOOP
                
        col_found := false;
                
        for new_col in select column_name 
                    from information_schema.columns 
                    where table_name = 'trajet_2009' LOOP
                    
            if new_col = old_col then
                col_found := true;
            END IF;
        END LOOP;
        
        -- exclusion des colonnes geo
        if not col_found and old_col <> 'gid' and old_col <> 'geom' then
            --raise notice 'new col: %', old_col;
            -- gestion du type
            if old_col_type = 'character varying' then
                old_col_type := 'text';
            end if;
            -- alter table 
            
            EXECUTE 'alter table trajet_2009 add column ' || old_col||' '||old_col_type;
            -- raise notice 'alter table trajet_2009 add column % %', old_col, old_col_type; 
            -- et preparation de l'update
            col_list_update := old_col || '=t.' || old_col||sep||col_list_update;
            -- raise notice 'col_list_update: % ', col_list_update;
            col_list_inner := 'tr.' || old_col||sep||col_list_inner;
            -- raise notice 'col_list_update: % ', col_list_inner;
            sep := ', ';
            
        end if;
    END LOOP;
    
    -- launch update clause.
    update_query := update_query || col_list_update || update_query_sub || col_list_inner || update_query_sub_end|| update_query_end; 
    execute update_query;
    --raise notice 'update q: %', update_query;

end;
$$ language PLPGSQL;
