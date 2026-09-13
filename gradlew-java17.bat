@echo off
REM Script pour lancer Gradle avec Java 17 spécifiquement

REM Chemins possibles de Java 17 (ajustez selon votre installation)
set JAVA17_PATHS[0]=C:\Program Files\Eclipse Adoptium\jdk-17.0.10.7-hotspot
set JAVA17_PATHS[1]=C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot
set JAVA17_PATHS[2]=C:\Program Files\Java\jdk-17
set JAVA17_PATHS[3]=C:\Program Files\Java\jdk-17.0.10
set JAVA17_PATHS[4]=C:\Program Files\Microsoft\jdk-17
set JAVA17_PATHS[5]=C:\Program Files\Azul\zulu-17

REM Chercher Java 17
set JAVA17_HOME=
for /L %%i in (0,1,5) do (
    if exist "!JAVA17_PATHS[%%i]!\bin\java.exe" (
        set JAVA17_HOME=!JAVA17_PATHS[%%i]!
        goto :found
    )
)

REM Si pas trouvé, chercher dans tous les dossiers Program Files
for /D %%d in ("C:\Program Files\*jdk*17*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA17_HOME=%%d
        goto :found
    )
)

for /D %%d in ("C:\Program Files\Eclipse Adoptium\*") do (
    if exist "%%d\bin\java.exe" (
        set JAVA17_HOME=%%d
        goto :found
    )
)

echo ERREUR: Java 17 non trouve!
echo.
echo Veuillez installer Java 17 depuis: https://adoptium.net/
echo Ou executez find-java17.bat pour trouver ou il est installe
pause
exit /b 1

:found
echo Java 17 trouve: %JAVA17_HOME%
echo.

REM Définir JAVA_HOME temporairement pour cette commande
set JAVA_HOME=%JAVA17_HOME%
set PATH=%JAVA17_HOME%\bin;%PATH%

REM Vérifier la version
"%JAVA17_HOME%\bin\java.exe" -version
echo.

REM Lancer Gradle avec les arguments passés
if exist gradlew.bat (
    call gradlew.bat %*
) else (
    echo Erreur: gradlew.bat n'existe pas
    echo Lancez d'abord: setup-gradle.bat
    pause
    exit /b 1
)
