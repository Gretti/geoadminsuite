#!/bin/bash

################################################################################
#
# Shell script to create the postgis database that will receive Navstreets data
#
################################################################################

################################################################################
#  Configuration variable:
# Edit the following variable to match target environment
################################################################################

# *DBCON = -U $DBUSER -h $DBHOST -p $DBPORT


echo "Dropping the database $DBNAME if exists..."
$PGBIN/dropdb --if-exists  $DBCON  $DBNAME
echo "Database $DBANE dropped."
echo

"Creating database $DBNAME..."
$PGBIN/createdb  $DBCON -E UTF-8 -l c $DBNAME
echo "Database $DBANE created."
echo

"installing Postgis through Extension..."
$PGBIN/psql  $DBCON - c "create extension postgis" -d $DBNAME
OUT=$?

if [ $OUT -eq 0 ];then
    echo "Postgis extension created"
else
    echo "Unable to create postgis extension, aborting."
    exit 1
fi

echo "Done with database creation"
echo

