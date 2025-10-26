# 🔧 Solución: Reiniciar Base de Datos

## ❌ Problema
- No puedes iniciar sesión
- Base de datos en estado inconsistente
- Migración no aplicada correctamente

## ✅ Solución Rápida

### Opción 1: Desinstalar y Reinstalar la App (MÁS FÁCIL)

1. **En Android Studio o Emulador:**
   - Mantén presionado el icono de la app
   - Selecciona "Desinstalar" o "Uninstall"
   
2. **Ejecuta la app nuevamente desde Android Studio:**
   - Click en el botón "Run" (▶️)
   - La BD se creará limpia con la nueva versión

### Opción 2: Limpiar Datos desde Configuración

1. **En el Emulador/Dispositivo:**
   - Ve a: Configuración > Apps > [Tu App]
   - Toca "Almacenamiento"
   - Toca "Borrar datos"
   - Confirma

2. **Ejecuta la app nuevamente**

### Opción 3: Comando ADB (Terminal)

```bash
# Desde la terminal de Android Studio o PowerShell
adb shell pm clear com.example.uinavegacion

# Luego ejecuta la app nuevamente
```

## 📱 Después de Limpiar

La app creará automáticamente:

### Usuarios de Prueba:
```
Email: user1@demo.com
Password: Password123!
---
Email: test@test.com
Password: Password123!
```

### Admin:
```
Email: admin@steamish.com
Password: Admin123!
```

## 🎯 Prueba

1. Desinstala la app
2. Ejecuta desde Android Studio
3. Inicia sesión con: `user1@demo.com` / `Password123!`
4. Ve a "Editar Perfil"
5. ¡Verás tus datos y podrás editarlos!

## ✅ Resultado

- ✅ Base de datos limpia
- ✅ Versión 18 aplicada (con campo gender)
- ✅ Usuarios de prueba creados
- ✅ Login funcional
- ✅ Edición de perfil funcional

