-- on vire ensuite les colonnes qui n'interessent pas depuis la table base_individus
alter table base_individus 
drop column  uu99, 
drop column  id, 
drop column  dc, 
drop column  sexe, 
drop column  age, 
drop column  instruction, 
drop column  Pachats, 
drop column  nper, 
drop column  npom, 
drop column  ne24m, 
drop column  Qtypl, 
drop column  typl, 
drop column  Qstatut, 
drop column  statut, 
drop column  Qcs, 
drop column  Jdep, 
drop column  csp, 
drop column  cs, 
drop column  Qcsm, 
drop column  csm, 
drop column  csmbis, 
drop column  Qrevenus, 
drop column  voit;

-- index sur les champs utiles
create index base_individus_id_new on base_individus(id_new);

-- creation de la colonne poids dans trajet
alter table trajet drop column poids;
alter table trajet add column poids double precision;

-- mise a jour du poids dans trajet suivant les utilisateurs
update trajet set poids=base_individus.poids
from base_individus
where base_individus.id_new=trajet.id::int;
