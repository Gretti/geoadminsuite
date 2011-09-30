#!/bin/sh

#shp2pgsql remote_svn/data/base_tirage_ident_new.dbf ident_new | psql -d bva

echo "create index ident_new_aresimuler on ident_new(aresimuler);" | psql -d bva
echo "create index ident_new_id_new on ident_new(id_new);" | psql -d bva
echo "create index ident_new_uu99 on ident_new(uu99);" | psql -d bva
