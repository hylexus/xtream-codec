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

call .\gradlew.bat publishMavenPublicationToGitHubPackagesRepository publishMavenBomPublicationToGitHubPackagesRepository ^
 -Dorg.gradle.parallel=false ^
 -Pxtream.maven.repo.central-portal.enabled=true ^
 -Pxtream.maven.publications.signing=on ^
 -Pxtream.frontend.build.jt-808-server-dashboard-ui.enabled=true ^
 -Pxtream.backend.build.checkstyle.enabled=true ^
 -Pxtream.backend.build.errorprone.enabled=true ^
 -Pxtream.backend.build.license-checker.enabled=true ^
 -Pxtream.backend.build.debug-module-fatjar.enabled=false

endlocal
