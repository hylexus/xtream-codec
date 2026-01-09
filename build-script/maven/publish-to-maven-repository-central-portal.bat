@echo off
setlocal enabledelayedexpansion

set SCRIPT_DIR=%~dp0
if "%SCRIPT_DIR:~-1%"=="\" set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

pushd "%SCRIPT_DIR%\..\.."
set XTREAM_PROJECT_ROOT_DIR=%CD%
popd

echo XTREAM_PROJECT_ROOT_DIR  : "%XTREAM_PROJECT_ROOT_DIR%"

cd /d "%XTREAM_PROJECT_ROOT_DIR%"
echo Working-Directory        : "%CD%"

REM @see xtream.maven.repo.central-portal.artifacts.temp-dir
REM @see xtream.maven.repo.central-portal.bom.temp-dir
rmdir /s /q "\tmp\xtream-codec\temp-artifacts" 2>nul
rmdir /s /q "\tmp\xtream-codec\temp-bom" 2>nul

REM org.gradle.parallel=false @see https://stackoverflow.com/questions/72664149/gradle-maven-publish-sonatype-creates-multiple-repositories-that-cant-be-clos
REM https://central.sonatype.org/publish/publish-portal-api/

call .\gradlew.bat clean build publishToMavenCentralPortal ^
 -Pxtream.maven.repo.central-portal.enabled=true ^
 -Pxtream.maven.publications.signing=on ^
 -Pxtream.frontend.build.jt-808-server-dashboard-ui.enabled=true ^
 -Pxtream.backend.build.checkstyle.enabled=true ^
 -Pxtream.backend.build.errorprone.enabled=true ^
 -Pxtream.backend.build.license-checker.enabled=true ^
 -Pxtream.backend.build.debug-module-fatjar.enabled=false

endlocal
