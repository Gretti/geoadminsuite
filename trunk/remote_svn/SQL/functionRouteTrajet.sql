create or replace function routeTrajet() returns text as $$
DECLARE
    v_source int := 0; -- source node pour le trajet a reconstruire
    v_target int := 0; -- target node pour le trajet a reconstruire
    v_idtraj text := '';
    v_res text := ''; -- return value
    i int := 0; -- cpteur
    v_query text := '';
    numIns int4 := 0;
    t0 timestamp;
BEGIN

    drop table if exists trajet_routing;
    create table trajet_routing(idtraj text, ordre int4, vertex_id int4, edge_id int4, cost float8);

    drop table if exists trajet_routing_ko;
    create table trajet_routing_ko(idtraj text);

    create temp sequence tmp_seq;

    select timeofday()::timestamp into t0;
    -- query de lien pour avoir la geom des voiries
    -- tester avec wrapper => rename columns
    FOR v_idtraj, v_source, v_target in
        select tsv.idtraj, tsv.source, tsv.target
        from trajet_startend_voirie tsv
        where tsv.short      
    LOOP

        i := i + 1;
        v_query := 'select gid as id, source::int4, target::int4, shape_length::float8 as cost from voirie_work where idtraj = '''
                 || v_idtraj || '''';
        
        insert into trajet_routing(edge_id, vertex_id, cost, idtraj, ordre)
            select edge_id, vertex_id, cost, null, nextval('tmp_seq') from shortest_path(
            v_query,
            v_source,
            v_target,
            false,
            false);
        GET DIAGNOSTICS numIns = ROW_COUNT;

           perform * from shortest_path(
            v_query,
            v_source,
            v_target,
            false,
            false);
        GET DIAGNOSTICS numIns = ROW_COUNT;

        v_res := '';
        if numIns = 0 THEN
            --v_res := '0 insertion => check: ';
            insert into trajet_routing_ko (idtraj) values (v_idtraj);
            raise notice '0 insertion => check idtraj: %, source: %, target: %', v_idtraj, v_source, v_target;
        end if;

        -- raise notice '% %. idtraj: % - source: % - target: % - numIns: %', v_res, i, v_idtraj, v_source, v_target, numIns;
        if i % 100 = 0 then
            raise notice '% trajets traites', i;
        end if;

        update trajet_routing set idtraj = v_idtraj where idtraj is null;
        alter sequence tmp_seq restart;

    END LOOP;

    select age(timeofday()::timestamp, t0) into v_res;
    raise notice 'routing took: % (% iterations)', v_res, i;

    drop sequence tmp_seq;

    v_res := ' trajets traites ... ok';

    return v_res;

END;
$$ language plpgsql;