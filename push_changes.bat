@echo off
echo ========================================
echo    SCRIPT PARA SUBIR CAMBIOS A GITHUB
echo ========================================
echo.

echo [1/4] Verificando estado del repositorio...
git status
echo.

echo [2/4] Agregando todos los cambios...
git add .
echo.

echo [3/4] Haciendo commit con mensaje personalizado...
set /p commit_msg="Ingresa el mensaje del commit: "
git commit -m "%commit_msg%"
echo.

echo [4/4] Subiendo cambios a GitHub...
git push origin main
echo.

echo ========================================
echo    CAMBIOS SUBIDOS EXITOSAMENTE
echo ========================================
pause
