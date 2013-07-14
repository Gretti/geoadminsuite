-- creates the surface table from polygon layers

-- used for surface table gid pk column
drop sequence if exists surf_seq;
create sequence surf_seq;

-- The table of surfacic objects.
-- built from original navstreets layers, choosing only attributes needed
-- 
drop table if exists surface; 
create table surface as (
    select nextval('surf_seq') as gid, 3::int as polygon_class, 5::int as zlevel, polygon_id, polygon_nm, nm_langcd, st_transform(the_geom, 3857) as the_geom 
    from adminbndy2
    UNION ALL
    select nextval('surf_seq') as gid, 4::int as polygon_class, 4::int as zlevel, polygon_id, polygon_nm, nm_langcd, st_transform(the_geom, 3857) as the_geom 
    from adminbndy3
    UNION ALL
    select nextval('surf_seq') as gid, 5::int as polygon_class, 3::int as zlevel, polygon_id, polygon_nm, nm_langcd, st_transform(the_geom, 3857) as the_geom 
    from adminbndy4
    UNION ALL
    select nextval('surf_seq') as gid, 6::int as polygon_class, 2::int as zlevel, polygon_id, polygon_nm, nm_langcd, st_transform(the_geom, 3857) as the_geom 
    from adminbndy5
);

-- drop the origninal tables
drop table adminbndy2;
drop table adminbndy3;
drop table adminbndy4;
drop table adminbndy5;

-- add constraints to the table
alter table surface add primary key (gid);
ALTER TABLE surface ADD CONSTRAINT fk_surface_polygon_class FOREIGN KEY (polygon_class) REFERENCES class_code (id);

-- indexes
create index surface_the_geom_gist on surface using gist(the_geom);

-- table doc: generates comments for objects
comment on table surface is 'surfacic objects from Navstreets. Used to draw map background';
comment on column surface.gid is 'unique id, PK';
comment on column surface.zlevel is 'z-level of the object. one z-level by polygon type';
comment on column surface.polygon_class is 'Class of the polygon. FK on class_code table';
comment on column surface.polygon_id is 'ID of the polygon. From navstreet tables';
comment on column surface.polygon_nm is 'name of the polygon. From navstreet tables';
comment on column surface.nm_langcd is 'language code for the polygon. From navstreet tables';
comment on column surface.the_geom is 'Multipolygon geometry. SRID=3857';

-- generates simplified versions of the surface table

-- Table of all simplified surfacic objets, level 1:
-- --
-- drop table if exists surface_l1;
-- create table surface_l1 (
--     gid serial primary key,
--     type int not null , 
--     name text not null default '',
--     zorder int not null default 0,
--     the_geom geometry(MultiPolygon,  3857)
-- );
-- 
-- -- Table of all simplified surfacic objets, level 2:
-- --
-- drop table if exists surface_l2;
-- create table surface_l2 (
--     gid serial primary key,
--     type int not null,
--     name text not null default '',
--     zorder int not null default 0,
--     the_geom geometry(MultiPolygon,  3857)
-- );
