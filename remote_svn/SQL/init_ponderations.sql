
drop table if exists base_individus;

create table base_individus (
  poids		double precision,
  id_new	integer
);

create index base_individus_id_new on base_individus(id_new);

-- Test pour filtrer un fichier base_individus dans sa table
-- gawk '{ print $24";"$25 }' /mnt/data/2010/78302/base_individus_78302.txt > /mnt/data/2010/78302/base_individus_78302.csv
-- Import des ponderations dans la table
-- COPY base_individus FROM '/mnt/data/2010/78302/base_individus_78302.csv' WITH CSV HEADER DELIMITER ';';
