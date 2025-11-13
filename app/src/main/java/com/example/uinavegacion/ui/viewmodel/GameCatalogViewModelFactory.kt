package com.example.uinavegacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uinavegacion.data.local.categoria.CategoriaDao
import com.example.uinavegacion.data.local.genero.GeneroDao
import com.example.uinavegacion.data.repository.GameRepository

class GameCatalogViewModelFactory(
    private val gameRepository: GameRepository,
    private val categoriaDao: CategoriaDao,
    private val generoDao: GeneroDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameCatalogViewModel::class.java)) {
            return GameCatalogViewModel(gameRepository, categoriaDao, generoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}




