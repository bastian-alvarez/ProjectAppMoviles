package com.example.uinavegacion.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.uinavegacion.data.local.database.AppDatabase
import com.example.uinavegacion.data.repository.UserRepository
import com.example.uinavegacion.data.SessionManager
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(nav: NavHostController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var photoSavedMessage by remember { mutableStateOf<String?>(null) }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val scope = rememberCoroutineScope()
    

    // Crear archivo temporal para foto
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = File(context.getExternalFilesDir(null), "Pictures")
        storageDir.mkdirs()
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    // Crear URI para la cámara
    val photoFile = remember { createImageFile() }
    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            photoFile
        )
    }

    // ID del usuario para mantener la referencia correcta
    var userId by remember { mutableStateOf<Long?>(null) }

    // Launcher para tomar foto con cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profilePhotoUri = photoUri.toString()
            scope.launch {
                if (userId != null) {
                    userRepository.updateProfilePhoto(userId!!, profilePhotoUri)
                    // Actualizar SessionManager con la nueva foto
                    val updatedUser = db.userDao().getById(userId!!)
                    if (updatedUser != null) {
                        SessionManager.loginUser(updatedUser)
                    }
                    photoSavedMessage = "Foto tomada y guardada"
                } else {
                    photoSavedMessage = "Error: Usuario no identificado"
                }
            }
        }
    }

    // Launcher para seleccionar imagen desde galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            profilePhotoUri = uri.toString()
            scope.launch {
                if (userId != null) {
                    userRepository.updateProfilePhoto(userId!!, profilePhotoUri)
                    // Actualizar SessionManager con la nueva foto
                    val updatedUser = db.userDao().getById(userId!!)
                    if (updatedUser != null) {
                        SessionManager.loginUser(updatedUser)
                    }
                    photoSavedMessage = "Foto de galería guardada"
                } else {
                    photoSavedMessage = "Error: Usuario no identificado"
                }
            }
        }
    }

    // Launcher para pedir permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoUri)
        } else {
            photoSavedMessage = "Permiso de cámara denegado"
        }
    }

    // Cargar datos del usuario desde SessionManager
    LaunchedEffect(Unit) {
        val currentUserEmail = SessionManager.getCurrentUserEmail()
        if (currentUserEmail != null) {
            val userByEmail = db.userDao().getByEmail(currentUserEmail)
            if (userByEmail != null) {
                userId = userByEmail.id
                name = userByEmail.name
                email = userByEmail.email
                // Formatear el teléfono al cargarlo desde la BD
                phone = formatChileanPhoneNumber(userByEmail.phone)
                profilePhotoUri = userByEmail.profilePhotoUri
            }
        }
    }

    val genderOptions = listOf("Masculino", "Femenino", "Otro", "Prefiero no decir")

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            ) 
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de foto de perfil mejorada
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Foto de Perfil",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Avatar circular grande con previsualización
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(
                                3.dp, 
                                MaterialTheme.colorScheme.primary, 
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profilePhotoUri != null) {
                            AsyncImage(
                                model = profilePhotoUri,
                                contentDescription = "Foto de perfil",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Sin foto",
                                modifier = Modifier.size(60.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Mensaje de estado
                    if (photoSavedMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (photoSavedMessage!!.contains("Error")) 
                                    MaterialTheme.colorScheme.errorContainer 
                                else 
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = photoSavedMessage!!,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (photoSavedMessage!!.contains("Error")) 
                                    MaterialTheme.colorScheme.onErrorContainer 
                                else 
                                    MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    
                    // Botones para tomar foto o seleccionar desde galería
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { 
                                permissionLauncher.launch(android.Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cámara",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Cámara")
                        }
                        
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Galería",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Galería")
                        }
                    }
                    
                    // Botón para eliminar foto
                    if (profilePhotoUri != null) {
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                profilePhotoUri = null
                                scope.launch {
                                    if (userId != null) {
                                        userRepository.updateProfilePhoto(userId!!, null)
                                        val updatedUser = db.userDao().getById(userId!!)
                                        if (updatedUser != null) {
                                            SessionManager.loginUser(updatedUser)
                                        }
                                        photoSavedMessage = "Foto eliminada"
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Eliminar Foto")
                        }
                    }
                }
            }

            // Información personal
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información Personal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Nombre completo
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Nombre", modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Teléfono
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { newValue ->
                            // Formatear automáticamente mientras el usuario escribe
                            phone = formatChileanPhoneNumber(newValue)
                        },
                        label = { Text("Teléfono") },
                        placeholder = { Text("+56 9 1234 5678") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Teléfono", modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(20.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Género
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Género") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Género", modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Mensaje de éxito
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Perfil actualizado exitosamente",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { nav.popBackStack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                
                Button(
                    onClick = {
                        if (userId == null) {
                            photoSavedMessage = "Error: Usuario no identificado"
                            return@Button
                        }
                        
                        // Validar teléfono si no está vacío (acepta +56 9 XXXX XXXX o +569XXXXXXXX)
                        if (phone.isNotBlank()) {
                            if (!phone.startsWith("+56")) {
                                photoSavedMessage = "Formato incorrecto"
                                return@Button
                            }
                            
                            // Extraer todos los dígitos después de +56
                            val digitsAfterCountryCode = phone.substring(3).replace(" ", "").trim()
                            
                            if (digitsAfterCountryCode.isEmpty() || 
                                digitsAfterCountryCode.length != 9 || 
                                !digitsAfterCountryCode.all { it.isDigit() } ||
                                !digitsAfterCountryCode.startsWith("9")) {
                                photoSavedMessage = "Formato incorrecto"
                                return@Button
                            }
                        }
                        
                        isLoading = true
                        scope.launch {
                            try {
                                // Obtener el usuario actual por ID para mantener la contraseña
                                val currentUser = db.userDao().getById(userId!!)
                                if (currentUser != null) {
                                    // Actualizar todos los campos en la base de datos
                                    db.userDao().update(
                                        id = userId!!,
                                        name = name.trim(),
                                        email = email.trim(),
                                        phone = phone.trim(),
                                        password = currentUser.password // Mantener la contraseña actual
                                    )
                                    
                                    // Actualizar SessionManager con los nuevos datos
                                    val updatedUser = db.userDao().getById(userId!!)
                                    if (updatedUser != null) {
                                        SessionManager.loginUser(updatedUser)
                                        showSuccessMessage = true
                                        photoSavedMessage = null // Limpiar mensaje anterior
                                    }
                                } else {
                                    photoSavedMessage = "Error: Usuario no encontrado"
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                photoSavedMessage = "Error al guardar: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && name.isNotBlank() && email.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Guardar Cambios")
                }
            }
        }
    }
}

// Función para formatear el teléfono chileno a +56 9 XXXX XXXX
private fun formatChileanPhoneNumber(input: String): String {
    // Extraer solo los dígitos
    val digits = input.filter { it.isDigit() }
    
    // Si está vacío, retornar vacío
    if (digits.isEmpty()) return ""
    
    // Construir el formato según la cantidad de dígitos
    return when {
        digits.length <= 2 -> "+$digits"
        digits.length == 3 -> "+${digits.substring(0, 2)} ${digits[2]}"
        digits.length <= 11 -> {
            val countryCode = digits.substring(0, 2) // 56
            val mobilePrefix = digits.getOrNull(2) ?: "" // 9
            val remaining = digits.substring(3.coerceAtMost(digits.length))
            
            when {
                remaining.isEmpty() -> "+$countryCode $mobilePrefix"
                remaining.length <= 4 -> "+$countryCode $mobilePrefix $remaining"
                else -> {
                    val first4 = remaining.substring(0, 4)
                    val last4 = remaining.substring(4).take(4)
                    "+$countryCode $mobilePrefix $first4 $last4"
                }
            }
        }
        else -> {
            // Si tiene más de 11 dígitos, tomar solo los primeros 11
            val trimmed = digits.take(11)
            val countryCode = trimmed.substring(0, 2)
            val mobilePrefix = trimmed[2]
            val first4 = trimmed.substring(3, 7)
            val last4 = trimmed.substring(7)
            "+$countryCode $mobilePrefix $first4 $last4"
        }
    }
}
