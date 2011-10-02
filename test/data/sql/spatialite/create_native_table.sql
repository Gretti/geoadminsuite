CREATE TABLE province
(
 country_id BLOB NOT NULL,
 geometry BLOB NOT NULL,
 name CHAR(50) NOT NULL,
 population INTEGER NOT NULL,
 FOREIGN KEY (country_id) REFERENCES country(id)
);

CREATE TABLE country
(
   id INTEGER NOT NULL,
   iso_code CHAR(3) NOT NULL,
   name CHAR(80) NOT NULL
);
