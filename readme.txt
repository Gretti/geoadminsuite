KaboumServer Project: The Vector Features server for Kaboum Applet, allowing
to load and edit vector features.

Use ant or an IDE (Netbeans, Eclipse) to build the project.

Its organization is pretty straightforward:
web folder contains web stuff
src folder contains java sources
web/WEB-INF/lib folder contains libraries

KaboumServer depends on Geotools 2.6.x (tested on 2.6.1 and 2.6.2).
It can be downloaded at geotools.org.
Either create a library inside your IDE and add this library to the project's
libraries, or copy all the Geotools Jars in the web/WEB-INF/lib folder.