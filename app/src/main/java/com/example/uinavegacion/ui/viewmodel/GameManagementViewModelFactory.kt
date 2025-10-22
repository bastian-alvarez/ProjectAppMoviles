package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.repository.GameRepository

/**
 * Factory para crear GameManagementViewModel
 */
class GameManagementViewModelFactory(
    private val gameRepository: GameRepository
): ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameManagementViewModel::class.java)) {
            return GameManagementViewModel(gameRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}