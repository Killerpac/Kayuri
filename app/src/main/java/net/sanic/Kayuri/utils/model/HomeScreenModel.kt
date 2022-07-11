package net.sanic.Kayuri.utils.model

data class HomeScreenModel(
    var typeValue: Int,
    var type: String = "",
    var animeList: ArrayList<AnimeMetaModel>? = null,
    var genreList: ArrayList<GenreModel>? = null,
    var recentlyPlayedList:ArrayList<RecentlyPlayed>? = null
)