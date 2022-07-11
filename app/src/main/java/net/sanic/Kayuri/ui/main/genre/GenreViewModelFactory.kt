package net.sanic.Kayuri.ui.main.genre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenreViewModelFactory(private val genreUrl: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(GenreViewModel::class.java)){
            return GenreViewModel(genreUrl = genreUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}