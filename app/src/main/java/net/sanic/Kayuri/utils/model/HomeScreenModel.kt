package net.sanic.Kayuri.utils.model

import kotlin.collections.ArrayList

data class HomeScreenModel(
    var typeValue: Int,
    var type: String = "",
    var animeList: ArrayList<AnimeMetaModel>? = null,
    var genreList: ArrayList<GenreModel>? = null
)