package net.sanic.Kayuri.utils.constants

class C {
    companion object{

        const val GIT_DOWNLOAD_URL = "https://github.com/Killerpac/Kayuri"

        //Error Codes
        const val RESPONSE_UNKNOWN: Int = 1000
        const val ERROR_CODE_DEFAULT: Int = -1000
        const val NO_INTERNET_CONNECTION = 1001

        //Base URLS
        const val BASE_URL = "https://gogoanime.gg/"
        const val EPISODE_LOAD_URL = "https://ajax.gogocdn.net/ajax/load-list-episode"
        const val SEARCH_URL = "/search.html"

        //Gogoanime Secrets
        var GogoSecretkey = "93106165734640459728346589106791"
        var GogoSecretIV = "8244002440089157"
        var GogoSecretSecondKey = "97952160493714852094564712118349"
        val GogoPadding= byteArrayOf(0x8,0xe,0x3,0x8,0x9,0x3,0x4,0x9)

        //Model Type
        const val TYPE_RECENT_SUB = 1
        const val TYPE_POPULAR_ANIME =2
        const val TYPE_RECENT_DUB = 3
        const val TYPE_GENRE = 4
        const val TYPE_MOVIE = 5
        const val TYPE_NEW_SEASON = 6
        const val TYPE_DEFAULT= -1
        const val TYPE_KEYS = 7

        // Retrofit Request TYPE

        const val RECENT_SUB = 1
        const val RECENT_DUB = 2

        const val MAX_LIMIT_FOR_SUB_DUB = 10


        const val GENRE_POSITION = 0
        const val NEWEST_SEASON_POSITION = 3
        const val RECENT_SUB_POSITION = 1
        const val RECENT_DUB_POSITION = 2
        const val POPULAR_POSITION = 5
        const val MOVIE_POSITION = 4

        //Episode URL Type
        const val TYPE_MEDIA_URL = 100
        const val TYPE_M3U8_URL = 101
        const val TYPE_M3U8_PREP = 102

        //Anime Info URL Type
        const val TYPE_ANIME_INFO = 1000
        const val TYPE_EPISODE_LIST = 1001
        const val M3U8_REGEX_PATTERN = "(http|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@|?;^=%&:/~+#-]*[\\w;@?|^=%&/~+#-])?"
        //Anime Search Types
        const val TYPE_SEARCH_NEW = 2000
        const val TYPE_SEARCH_UPDATE = 2001
        //Anime Genre Types
        const val TYPE_GENRE_NEW = 3000
        const val TYPE_GENRE_UPDATE = 3001

        //Network Requests Header
        const val USER_AGENT = "Mozilla/5.0 (Linux; Android 8.0.0) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Focus/7.0.13 Chrome/70.0.3538.110 Mobile Safari/537.36"
        const val ORIGIN = "https://gogoanime.gg/"
        const val  REFERER = "https://goload.pro/"

        //Preferred Quality
        const val QUALITY = "Auto"

        //Realm
        const val MAX_TIME_M3U8_URL =  25 * 60 *1000
        const val MAX_TIME_FOR_ANIME = 2 * 24 * 60 *60 * 1000
    }
}