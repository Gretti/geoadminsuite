@echo on

Set root=%CD%
Set jsbuild=%root%\GeoSIE-Brique\jsbuild
Set venv=%jsbuild%\venv
Set webcontent=%root%\GeoSIE-Brique\WebContent
Set build=%webcontent%\jsbuild
Set openlayers=%webcontent%\lib\Openlayers

REM
REM Set up build directory
REM

echo cleaning build dir...
rmdir /s /q "%build%"
mkdir "%build%"
echo done

REM
REM Create virtual env if it doesn't exist
REM

if exist "%venv%" goto venv_created

echo creating virtual env and installing jstools...
cd "%jsbuild%"
REM Python25
C:\Python24\python.exe go-jstools.py "%venv%" > NUL
cd "%root%"
echo done

:venv_created

REM
REM Create JS build
REM

echo running jsbuild...
cd "%jsbuild%"
"%venv%\Scripts\jsbuild.exe" -o "%build%" main.cfg > NUL
cd "%root%"
echo done

REM
REM Copy OpenLayers resources
REM

echo copying OpenLayers resources...
xcopy "%openlayers%\img" "%build%\img" /E /Q /Y /I
xcopy "%openlayers%\theme" "%build%\theme" /E /Q /Y /I
echo done

REM exit 0
