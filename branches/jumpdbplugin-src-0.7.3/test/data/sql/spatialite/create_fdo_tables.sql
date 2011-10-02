CREATE TABLE IF NOT EXISTS geometry_columns 
(
  f_table_name VARCHAR, 
  f_geometry_column VARCHAR,
  geometry_type INTEGER, 
  coord_dimension INTEGER,  
  srid INTEGER,  
  geometry_format VARCHAR 
);


CREATE TABLE IF NOT EXISTS country_shape_wkt
(
   OGC_FID INTEGER PRIMARY KEY,
   -- according to FDO RFC 16, WKT is stored in a BLOB
   GEOMETRY BLOB, 
   'iso_code' VARCHAR,
   'name' VARCHAR
);

CREATE TABLE IF NOT EXISTS country_shape_wkb
(
   OGC_FID INTEGER PRIMARY KEY,
   GEOMETRY BLOB, 
   'iso_code' VARCHAR,
   'name' VARCHAR
);
