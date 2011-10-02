INSERT INTO geometry_columns
(
  f_table_name,
  f_geometry_column,
  geometry_type,
  coord_dimension,
  srid,
  geometry_format
)
VALUES
(
  'country_shape_wkt',
  'GEOMETRY',
   3,
   2,
   50000,
   'WKT'
);

INSERT INTO geometry_columns
(
  f_table_name,
  f_geometry_column,
  geometry_type,
  coord_dimension,
  srid,
  geometry_format
)
VALUES
(
  'country_shape_wkb',
  'GEOMETRY',
   3,
   2,
   50000,
   'WKB'
);

INSERT INTO country_shape_wkb
(
OGC_FID,
GEOMETRY,
'iso_code',
'name'
)
VALUES
(
  1,
  AsBinary(GeomFromText('POLYGON((2 2,5 1,5 5,1 5,2 2))')),
  'USA',
  'United States'
);

INSERT INTO country_shape_wkt
(
OGC_FID,
GEOMETRY,
'iso_code',
'name'
)
VALUES
(
  1,
  'POLYGON((2 2,5 1,5 5,1 5,2 2))',
  'USA',
  'United States'
);
