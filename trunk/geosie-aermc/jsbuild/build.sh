#
# Variables
#
jsbuild="$(cd $(dirname $0); pwd)"          # where the build profile is
webcontent="${jsbuild}/../WebContent"       # path to the WebContent dir
openlayers="${webcontent}/lib/Openlayers"       # path to the OpenLayers lib
build="${webcontent}/jsbuild"               # where the resulting build file is copied
venv="${jsbuild}/venv"                      # where the virtual env is created

#
# Command path definitions
#
python="/usr/bin/python"
mkdir="/bin/mkdir"
rm="/bin/rm"
sh="/bin/sh"
cp="/bin/cp"

#
# Set up build directory
#
echo "cleaning build dir..."
${rm} -rf "${build}"
${mkdir} -p "${build}"
echo "done."

#
# Create virtual env if it doesn't exist
#
if [ ! -d ${venv} ]; then
    echo "creating virtual env and installing jstools..."
    (cd ${jsbuild};
     ${python} go-jstools.py ${venv} > /dev/null)
    echo "done."
fi;

#
# Create JS build
#
echo "running jsbuild..."
(cd ${jsbuild};
 ${venv}/bin/jsbuild -o "${build}" main.cfg
)
echo "done."

#
# Copy OpenLayers resources
#

echo "copying OpenLayers resources..."
${cp} -r "${openlayers}/img" "${build}/"
${cp} -r "${openlayers}/theme" "${build}/"
echo "done."

# Cleanup SVN stuff
${rm} -rf `find "${build}" -name .svn -type d`

exit 0
