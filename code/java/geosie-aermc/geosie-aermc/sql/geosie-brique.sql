-- Creer une base geosie_brique avec le modele template_postgis 

-- Creation d'un role geosie_brique_web (mot de passe : 6briques)
CREATE ROLE geosie_brique_web;
ALTER ROLE geosie_brique_web WITH SUPERUSER INHERIT CREATEROLE CREATEDB LOGIN PASSWORD 'md5e4495b506cae849b5fafba006a243b99' VALID UNTIL 'infinity';

-- Create un schema geosie
CREATE SCHEMA geosie;
GRANT USAGE ON SCHEMA geosie TO geosie_brique_web;
GRANT USAGE ON SCHEMA geosie TO geosie_brique_web;

CREATE SEQUENCE id_seq; 

-- Table: geosie.brique_point
-- DROP TABLE geosie.brique_point;

CREATE TABLE geosie.brique_point
(
  id_brique INT4 DEFAULT nextval('id_seq'),
  classification character varying(40),
  attribut_2 character varying(40),
  attribut_3 character varying(40),
  the_geom geometry NOT NULL,
  CONSTRAINT brique_point_pkey PRIMARY KEY (id_brique),
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE geosie.brique_point OWNER TO geosie_brique_web;

-- Table: geosie.brique_ligne
-- DROP TABLE geosie.brique_ligne;

CREATE TABLE geosie.brique_ligne
(
  id_brique INT4 DEFAULT nextval('id_seq'),
  classification character varying(40),
  attribut_2 character varying(40),
  attribut_3 character varying(40),
  the_geom geometry NOT NULL,
  CONSTRAINT brique_ligne_pkey PRIMARY KEY (id_brique),
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE geosie.brique_ligne OWNER TO geosie_brique_web;

-- Table: geosie.brique_polygone
-- DROP TABLE geosie.brique_polygone;

CREATE TABLE geosie.brique_polygone
(
  id_brique INT4 DEFAULT nextval('id_seq'),
  classification character varying(40),
  attribut_2 character varying(40),
  attribut_3 character varying(40),
  the_geom geometry NOT NULL,
  CONSTRAINT brique_polygone_pkey PRIMARY KEY (id_brique),
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE geosie.brique_polygone OWNER TO geosie_brique_web;

SELECT AddGeometryColumn( 'geosie', 'brique_point', 'the_geom', 4326, 'GEOMETRY', 2);
SELECT AddGeometryColumn( 'geosie', 'brique_ligne', 'the_geom', 4326, 'GEOMETRY', 2);
SELECT AddGeometryColumn( 'geosie', 'brique_polygone', 'the_geom', 4326, 'GEOMETRY', 2);
