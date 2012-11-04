-- sde hack:
create schema sde;

create table sde.sde_layers(
	id serial primary key
);

drop table sde.sde_version;
create table sde.sde_version (
	id serial primary key,
	major int, 
	minor int, 
	bugfix int, 
	description text, 
	release text, 
	sdesvr_rel_low text
);


insert into sde.sde_version(major, minor, bugfix, description, release, sdesvr_rel_low)
values (10, 1, 0, 'arcsde', '10.1', '10.1');

drop table if exists sde.sde_server_config;
create table sde.sde_server_config(
	id serial primary key,
	prop_name text,
	char_prop_value text, 
	num_prop_value int
);

insert into sde.sde_server_config (prop_name, char_prop_value, num_prop_value)
values ('STATUS', '1', 1);

create table sde.sde_geometry_columns table

create table sde.sde_raster_columns table

create table sde.sde_spatial_references table

create table sde.sde_table_registry table

create table sde.sde_states table 