REM Script to generate the "js" doc for the project GeoSIE-Brique
REM with the documentation generator tool Natural Docs http://www.naturaldocs.org/
REM Requires Perl 5.05 http://www.activestate.com/Products/activeperl/
@echo on

Set root=%CD%
Set naturaldocs=%root%\GeoSIE-Brique\NaturalDocs-1.5
Set source=%root%\GeoSIE-Brique\WebContent
REM doc directory
Set target=%root%\GeoSIE-Build\doc
Set doc_config=%naturaldocs%\doc_config

cd "%naturaldocs%"
REM Execute Natural Docs 
call NaturalDocs.bat -i %source% -o FramedHTML %target% -p %doc_config% -cs UTF-8 Default

REM echo done
REM exit 0