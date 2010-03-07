# creates the postgis test database for KaboumServer project
# works only with postgresql > 8.0 and postgis 1.x
# for other versions, adapt table constraint names
# this script assumes that postgresql tools are in the path

#dropdb kaboum_test

#createdb -E "LATIN1" kaboum_test

#createlang -d kaboum_test plpgsql

#psql -d kaboum_test -f "/usr/share/postgresql-8.1-postgis/lwpostgis.sql"
#psql -d kaboum_test -f "/usr/share/postgresql-8.1-postgis/spatial_ref_sys.sql"

# adds geographic data
shp2pgsql -s 27582 ../files/departements.shp departements | psql -U postgres -d kaboum_test 


# Create GiST indices on geographic tables
psql -d kaboum_test -c "CREATE INDEX departements_gist ON departements USING GIST ( the_geom GIST_GEOMETRY_OPS )"

vacuumdb -d kaboum_test -f -z
