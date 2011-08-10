-- Parcours des voiries 2009 en suivant les trajets 2006:
--
-- Les etapes du processus:
--
-- 1) construire les trajets complets a partir des segments de trajets: table trajet_full
-- 2) construire le sous-ensembple des voiries dont les codes link_id sont valides pour les trajets: table voirie_work
-- 2-bis) construire le sous-ensembple des voiries a moins de 20m des trajets pour les voiries
--        non deja presentes a l'etape precedente: table voirie_work
-- 3) identifier tous les trajets 2006 dont le code NVQ link_id est valide => table trajet_2009_1
--    puis virer ces trajets et continuer le traitement sur le reste.
-- 4) construire la table des voiries de depart et de fin, des noeuds de depart pour chaque trajet: table trajet_startend_voirie)
-- 5) Eliminer les trajets n'ayant pas de start ou end voirie: table trajet_rejet
-- 6) construire la table listant, pour chaque noeud des voiries 2009, les troncons de voirie
--    les plus proches du trajet: table node_voirie. C'est cette table qui est utilisée pour le
--    parcours iteratif du graphe.
-- 7) lancer la fonction iterative pour chaque trajet valide, pour reconstruire la sequence
--    de troncons 2009 composant le trajet 2006: table trajet_2009
-- 8) calcul du sens des troncons par rapport a la direction du trajet.
-- 9) statistiques sur le resultat:
--  le nombre de trajet_rejet + trajet_2009 = trajet_full
--  rapport de longueur entre les trajets 2009 et les 2006
--
-- ce script ne cree que les fonctions.
-- l'enchainement de calage_trajet_*.sql permet de realiser le traitement complet.

-----------------------------------------------------------------
-- Fonctions utilisées dans le script
---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
CREATE AGGREGATE array_accum (anyelement)
(
sfunc = array_append,
stype = anyarray,
initcond = '{}'
);
	
-- distance moyenne:
-- fonction de distance moyenne entre tous les points d'une ligne et une geometry
create or replace function st_avgDistance(g1 in geometry, g2 in geometry) returns float as $$
	select s/np from
	(select sum(d) as s from
		(select st_distance(st_pointN($1, generate_series(1, st_numPoints($1))), $2) as d) as foo) as t1,
	(select st_numpoints($1) as np) as t2;
$$ language SQL;

-- fonction de construction des trajets
CREATE or replace function buildTrajets(p_idtraj in text = null, reverse in boolean = false)
RETURNS table(idtraj text, gid int, node_id int, ordre int, link_id int, lgtron2009 double precision, geom geometry) as $$

DECLARE
  v_param alias for $1;
	v_reverse alias for $2;
	v_idtraj text := '';
	v_startvoirie int := 0;
	v_endvoirie int := 0;
	v_startnode int := 0;
	v_temp int := 0; -- variable temp permettant d'inverser start et end voirie
  v_filter text := '%';
BEGIN
  if v_param is not null then
    v_filter := v_param;
  end if;

	FOR v_idtraj, v_startvoirie, v_endvoirie, v_startnode in
		select tsv.idtraj, tsv.startvoirie, tsv.endvoirie, tsv.startnode
		from trajet_startend_voirie tsv
    where tsv.idtraj like v_filter
    order by tsv.idtraj
	LOOP

		--raise notice 'trajet: % ', v_idtraj;
		if v_reverse then
			v_temp := v_startvoirie;
			v_startvoirie := v_endvoirie;
			v_endvoirie = v_temp;
		end if;
		return query select * from walkOnNodeVoirie(v_idtraj, v_startvoirie, v_endvoirie, v_startnode);
	END LOOP;

	RETURN;
END;
$$ language plpgsql;


CREATE or replace function buildTrajetsTest(idt in text = '', reverse in boolean = false)
RETURNS table(idtraj text, gid int, node_id int, ordre int, link_id int, lgtron2009 double precision, geom geometry) as $$

DECLARE
	v_param alias for $1;
	v_reverse alias for $2;
	v_idtraj text := '';
	v_startvoirie int := 0;
	v_endvoirie int := 0;
	v_startnode int := 0;
	v_temp int := 0; -- variable temp permettant d'inverser start et end voirie
BEGIN
	FOR v_idtraj, v_startvoirie, v_endvoirie, v_startnode in
		select tsv.idtraj, tsv.startvoirie, tsv.endvoirie, tsv.startnode
		from trajet_startend_voirie tsv
    where tsv.idtraj = v_param
    order by tsv.idtraj
	LOOP
		if v_reverse then
			v_temp := v_startvoirie;
			v_startvoirie := v_endvoirie;
			v_endvoirie = v_temp;
		end if;
		--raise notice 'trajet: % ', v_idtraj;
		return query select * from walkOnNodeVoirie(v_idtraj, v_startvoirie, v_endvoirie, v_startnode);
	END LOOP;

	RETURN;
END;
$$ language plpgsql;

CREATE OR REPLACE FUNCTION dotprod(g1 in geometry, g2 in geometry) RETURNS float AS $$
DECLARE
    ux float := 0;
    uy float := 0;
    Udx float := 0;
    Udy float := 0;
    nu float := 0;

    vx float := 0;
    vy float := 0;
    Vdx float := 0;
    Vdy float := 0;
    nv float := 0;

    dp float := 0;
BEGIN
    select into Udx st_x(st_endpoint($1)) - st_x(st_startpoint($1));
    select into Udy st_y(st_endpoint($1)) - st_y(st_startpoint($1));
    nu := (Udx * Udx) + (Udy * Udy);
    ux := Udx / nu;
    uy := Udy / nu;

    select into Vdx st_x(st_endpoint($2)) - st_x(st_startpoint($2));
    select into Vdy st_y(st_endpoint($2)) - st_y(st_startpoint($2));
    nv := (Vdx * Vdx) + (Vdy * Vdy);
    vx := Vdx / nv;
    vy := Vdy / nv;

    dp := ux * vx + uy * vy;

    RETURN dp;
END;
$$ LANGUAGE plpgsql;
---------------------------------------------------------------------------------------------
---------------------------------------------------------------------------------------------
