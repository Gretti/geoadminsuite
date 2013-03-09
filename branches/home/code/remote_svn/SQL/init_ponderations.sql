
drop table if exists base_individus;

-- creation de la table complete, avec toutes les colonnes pour garantir un chargement
-- OK des valeurs vides/null
create table base_individus (
  uu99 text,
  id  int,
  dc  int, 
  sexe	int,
  age	int,
  instruction	int,
  Pachats	int,
  nper	int,
  npom	int,
  ne24m	int,
  Qtypl	int,
  typl	int,
  Qstatut	int,
  statut	int,
  Qcs	int,
  Jdep	int,
  csp	int,
  cs	int,
  Qcsm	int,
  csm	int,
  csmbis	int,
  Qrevenus	int,
  voit	int,
  poids		double precision,
  id_new	integer                
);


