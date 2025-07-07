@echo off
REM Run Artillery test and generate HTML report
set CONFIG=artillery-critical-endpoints.yml
set JSON=result.json
set HTML=report.html

artillery run %CONFIG% --output %JSON%
IF %ERRORLEVEL% NEQ 0 (
  echo Artillery test failed.
  exit /b %ERRORLEVEL%
)
artillery report %JSON% --output %HTML%
echo Report generated: %HTML%
