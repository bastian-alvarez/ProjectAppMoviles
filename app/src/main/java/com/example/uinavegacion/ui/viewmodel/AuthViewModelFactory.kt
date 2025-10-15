package com.example.uinavegacion.ui.viewmodel
import androidx.lifecycle.ViewModel
import com.example.uinavegacion.data.repository.UserRepository
import androidx.lifecycle.ViewModelProvider

//factory del view model para que reciba el repositorio
import android.app.Application
import com.example.uinavegacion.data.repository.AdminRepository

class AuthViewModelFactory(
    private val application: Application,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(application, userRepository, adminRepository) as T
        }
        throw IllegalArgumentException("Error desconocido class: ${modelClass.name}")
    }
}