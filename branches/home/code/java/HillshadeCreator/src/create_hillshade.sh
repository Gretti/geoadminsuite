#!/bin/bash

################################################################################
#
# Creates a colored hillshade jpeg based on given WGS84 DEM and a fixed color ramp
# to represent altitude ranges.
# Will output a jpeg file name quicklook.jpg with a size reduced by a 80% ratio into 
# the given folder.
# uses the VRT format to speed things up
#
# Usage:
# create_hillshade <input dem file> <input color_ramp> <output folder
#
################################################################################

# Some variables
# cmd lines²
GDALTRANSLATE_CMD=/Library/Frameworks/GDAL.framework/Versions/Current/Programs/gdal_translate
GDALDEM_CMD=/Library/Frameworks/GDAL.framework/Versions/Current/Programs/gdaldem
MERGE_CMD=/Users/nicolas/bin/gdal-1.9.2/swig/python/samples/hsv_merge.py

# parameters
ALTI_RATIO=5
USE_VRT=
IMG_REDUCTION=20%

# reduces the input dem size
$GDALTRANSLATE_CMD -q -outsize $IMG_REDUCTION $IMG_REDUCTION -of VRT $1 $3/small_dem.vrt

# Creates the hillshade
$GDALDEM_CMD hillshade -q $3/small_dem.vrt $3/hillshade.vrt -z $ALTI_RATIO -s 111120

# creates the color relief image from input dem, based on configured color ramp
$GDALDEM_CMD color-relief -q $3/small_dem.vrt $2 $3/relief.vrt

# merges hillshade and color relief to produce colored hillshade
$MERGE_CMD -q $3/relief.vrt $3/hillshade.vrt $3/color_hill.vrt

# converts quicklook hillshade to jpeg file
$GDALTRANSLATE_CMD -q -of JPEG $3/color_hill.vrt $3/quicklook.jpg

# cleans up intermediate files
rm -rf $3/relief.vrt $3/hillshade.vrt $3/small_dem.vrt $3/color_hill.vrt $3/quicklook.jpg.aux.xml

exit 0
