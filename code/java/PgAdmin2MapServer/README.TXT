pgAdmin2MapServer

Nicolas Ribot, January 2013

_____
What:

A pgAdmin III java-web viewer for PostGIS layers 

______________
How to install:

? Unzip in the pgAdmin plugins.d directory:

    On Mac OS X: /Applications/pgAdmin3.app/Contents/SharedSupport/plugins.d

    On Linux Ubuntu: /usr/share/pgadmin3/plugins.d
             Centos: /usr/pgsql-9.2/share/pgadmin3/plugins.d

    On Windows: C:\Program Files\pgAdmin III\1.16\plugins.d

? Launch pgAdmin: on a database, schema or table node (left tree), select the plugin "View PostGIS layers in MapServer":
A browser should opened with selected layer(s)

_________
Features:

Loads Postgis Layers in a GeoExt webpage.
Supports database, schema and table level
Generates WGS84 maps if layers have different SRID's
Layer reorder 

________
To come:

External files drag'n'drop (shp, img, etc.)

Debug URL to refresh the map:
http://localhost:9472/newParams?bindir=/Applications/Dev/pgAdmin3.app/Contents/MacOS&host=&port=5432&database=nicolas&user=nicolas&passwd=&schema=public&table=