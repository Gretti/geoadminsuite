#!/bin/sh

shp2pgsql remote_svn/data/base_tirage_ident_new.dbf ident_new | psql

