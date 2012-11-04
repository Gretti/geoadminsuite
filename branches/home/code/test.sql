create or replace function test() returns void as $$
DECLARE
    m text := '';
BEGIN
    m := 'ohoh' || chr(10) || 'hihi';
    raise notice 'coucou: %', 1 USING HINT = 'ohoh' || chr(10) || 'hihi'; 
END;
$$ language plpgsql;
