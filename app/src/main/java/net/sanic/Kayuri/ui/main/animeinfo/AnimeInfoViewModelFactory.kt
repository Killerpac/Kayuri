package net.sanic.Kayuri.ui.main.animeinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnimeInfoViewModelFactory(private val categoryUrl: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AnimeInfoViewModel::class.java)){
            return AnimeInfoViewModel(categoryUrl = categoryUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

//    fun <T : ViewModel?> create(modelClass: Class<T>): T {
//        if(modelClass.isAssignableFrom(AnimeInfoViewModel::class.java)){
//            return AnimeInfoViewModel(categoryUrl = categoryUrl) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel Class")
//    }

}