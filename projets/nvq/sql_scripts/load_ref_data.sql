-- Loads reference data and codes useful for data processing:
-- (comes from a CSV export of SPIL_CTRG_2012-Q4.xls)

-- table of chain names
drop table if exists li_chain_name;
create table li_chain_name (
    id serial primary key,
    chainid int not null,
    chain_name text not null,
    description text not null
);

-- table of country and chain names
drop table if exists li_country_chain_name;
create table li_country_chain_name (
    id serial primary key,
    chainid int not null,
    chain_name text not null,
    country text not null,
    country_gov_code text not null,
    facility_type int not null,
    description text not null
    --,foreign key (chainid) references li_chain_name(chainid) -- need to create a chain names tables, if really needed...
);

-- table of country and chain names
drop table if exists li_family_brand_name;
create table li_family_brand_name (
    id serial primary key,
    family_chainid int not null,
    chainid int not null,
    family_brand_name text not null,
    chain_name text not null,
    description text not null
);

-- occupation classes with codes
drop table if exists class_code;
create table class_code (
    id int primary key,
    description text,
    shapefile text
);
