@echo off
echo ========================================
echo Limpiando proyecto Android
echo ========================================
echo.

echo [1/6] Deteniendo procesos de Gradle y Android...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM javaw.exe 2>nul
taskkill /F /IM gradle.exe 2>nul
timeout /t 2 /nobreak >nul

echo [2/6] Deteniendo daemon de Gradle...
call gradlew --stop
timeout /t 2 /nobreak >nul

echo [3/6] Eliminando carpeta .gradle...
if exist ".gradle" (
    rmdir /s /q ".gradle" 2>nul
)

echo [4/6] Eliminando carpeta build del proyecto...
if exist "build" (
    rmdir /s /q "build" 2>nul
)

echo [5/6] Eliminando carpeta build de la app...
if exist "app\build" (
    rmdir /s /q "app\build" 2>nul
    if exist "app\build" (
        echo    Algunos archivos siguen bloqueados, intentando nuevamente...
        timeout /t 3 /nobreak >nul
        rmdir /s /q "app\build" 2>nul
    )
)

echo [6/6] Limpiando con Gradle...
call gradlew clean --no-daemon
echo.

echo ========================================
echo Limpieza completada!
echo ========================================
echo.
echo Ahora puedes ejecutar la app desde Android Studio.
echo.
pause

