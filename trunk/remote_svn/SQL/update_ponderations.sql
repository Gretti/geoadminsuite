
-- creation de la colonne poids dans trajet
alter table trajet drop column poids;
alter table trajet add column poids double precision;

-- mise a jour du poids dans trajet suivant les utilisateurs
update trajet set poids=base_individus.poids
from base_individus
where base_individus.id_new=trajet.id::int;
