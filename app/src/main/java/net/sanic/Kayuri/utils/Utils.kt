package net.sanic.Kayuri.utils

import android.content.Context
import android.util.DisplayMetrics
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.preference.PreferenceHelper


class Utils {
    companion object{

        fun getTypeName(typeValue: Int) : String{

           return when(typeValue){

                C.TYPE_RECENT_DUB -> "Recent Dub"
                C.TYPE_RECENT_SUB -> "Recent Sub"
                C.TYPE_MOVIE -> "Movies"
                C.TYPE_POPULAR_ANIME -> "Popular Anime"
                C.TYPE_GENRE -> "Genres"
                C.TYPE_NEW_SEASON-> "New Season"
                else -> "Default"
            }
        }

        fun getHeader(): Map<String, String>{
            val pref = PreferenceHelper.sharedPreference
            return mapOf("referer" to pref.getReferrer(), "origin" to pref.getOrigin(), "user-agent" to C.USER_AGENT)
        }
        fun getGoogle():Map<String,String>{
            return mapOf("user-agent" to C.USER_AGENT)
        }
        fun calculateNoOfColumns(
            context: Context,
            columnWidthDp: Float
        ): Int {
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
            return (screenWidthDp / columnWidthDp + 0.5).toInt()
        }

        fun getPositionByType(typeValue: Int): Int{
            return when (typeValue){
                C.TYPE_RECENT_SUB -> C.RECENT_SUB_POSITION
                C.TYPE_NEW_SEASON -> C.NEWEST_SEASON_POSITION
                C.TYPE_RECENT_DUB -> C.RECENT_SUB_POSITION
                C.TYPE_MOVIE -> C.MOVIE_POSITION
                C.TYPE_POPULAR_ANIME -> C.POPULAR_POSITION
                else -> 0
            }
        }

    }
}