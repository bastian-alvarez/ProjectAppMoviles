@echo off
echo ========================================
echo  LIMPIANDO BASE DE DATOS DE LA APP
echo ========================================
echo.
echo Esto eliminara los datos de la app y permitira que se recree la BD con la version correcta (v18)
echo.
pause

echo.
echo Limpiando datos de la app...
adb shell pm clear com.example.uinavegacion

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo  EXITO: Base de datos limpiada
    echo ========================================
    echo.
    echo Ahora:
    echo 1. Ejecuta la app desde Android Studio
    echo 2. Inicia sesion con: user1@demo.com / Password123!
    echo 3. Ve a "Editar Perfil"
    echo 4. Veras tus datos y podras editarlos!
    echo.
) else (
    echo.
    echo ========================================
    echo  ERROR: No se pudo limpiar la BD
    echo ========================================
    echo.
    echo Asegurate de que:
    echo 1. El emulador/dispositivo este conectado
    echo 2. Ejecuta: adb devices
    echo.
)

pause

