package net.sanic.Kayuri.utils.parser

import io.realm.RealmList
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.AnimeInfoModel
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import net.sanic.Kayuri.utils.model.EpisodeModel
import net.sanic.Kayuri.utils.model.GenreModel
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import timber.log.Timber

class HtmlParser {


    companion object {

        fun parseRecentSubOrDub(response: String, typeValue: Int) :ArrayList<AnimeMetaModel> {
            val animeMetaModelList: ArrayList<AnimeMetaModel> = ArrayList()
            val document = Jsoup.parse(response)
            val lists = document?.getElementsByClass("items")?.first()?.select("li")
            var i = 0
            lists?.forEach { anime ->
                val animeInfo = anime.getElementsByClass("name").first().select("a")
                val title = animeInfo.attr("title")
                val episodeUrl = animeInfo.attr("href")
                val episodeNumber = anime.getElementsByClass("episode").first().text()
                val animeImageInfo = anime.selectFirst("a")
                val imageUrl = animeImageInfo.select("img").first().absUrl("src")
                animeMetaModelList.add(
                    AnimeMetaModel(
                        ID = "$title$typeValue".hashCode(),
                        title = title,
                        episodeNumber = episodeNumber,
                        episodeUrl = episodeUrl,
                        categoryUrl = getCategoryUrl(imageUrl),
                        imageUrl = imageUrl,
                        typeValue = typeValue,
                        insertionOrder = i

                    )
                )
                i++
            }
            return animeMetaModelList
        }

        fun parsePopular(response: String, typeValue: Int) : ArrayList<AnimeMetaModel>{
            val animeMetaModelList: ArrayList<AnimeMetaModel> = ArrayList()
            val document = Jsoup.parse(response)
            val lists = document?.getElementsByClass("added_series_body popular")?.first()?.select("ul")?.first()?.select("li")
            Timber.e("POPULAR\n\n\n")
            var i=0

            lists?.forEach {anime->

                val animeInfoFirst = anime.select("a").first()
                val imageDiv = animeInfoFirst.getElementsByClass("thumbnail-popular").first().attr("style").toString()
                val imageUrl = imageDiv.substring(imageDiv.indexOf('\'')+1, imageDiv.lastIndexOf('\''))
                val categoryUrl = animeInfoFirst.attr("href")
                val animeTitle = animeInfoFirst.attr("title")
                val animeInfoSecond = anime.select("p").last().select("a")
                val episodeUrl = animeInfoSecond.attr("href")
                val episodeNumber = animeInfoSecond.text()
                val genreHtmlList = anime.getElementsByClass("genres").first().select("a")
//                Timber.e(genreHtmlList.toString())
                val genreList = RealmList<GenreModel>()
                genreList.addAll(getGenreList(genreHtmlList))



                animeMetaModelList.add(
                    AnimeMetaModel(
                        ID ="$animeTitle$typeValue".hashCode(),
                        title = animeTitle,
                        episodeNumber = episodeNumber,
                        episodeUrl = episodeUrl,
                        categoryUrl = categoryUrl,
                        imageUrl = imageUrl,
                        typeValue = typeValue,
                        genreList = genreList,
                        insertionOrder = i
                    )
                )
                i++
            }
            return animeMetaModelList
        }

        fun parseMovie(response: String, typeValue: Int) : ArrayList<AnimeMetaModel>{
            val animeMetaModelList: ArrayList<AnimeMetaModel> = ArrayList()
            val document = Jsoup.parse(response)
            val lists = document?.getElementsByClass("items")?.first()?.select("li")
            var i = 0
            lists?.forEach {
                val movieInfo = it.select("a").first()
                val movieUrl = movieInfo.attr("href")
                val movieName = movieInfo.attr("title")
                val imageUrl = movieInfo.select("img").first().absUrl("src")
                val releasedDate = it.getElementsByClass("released")?.first()?.text()
                animeMetaModelList.add(
                    AnimeMetaModel(
                        ID = "$movieName$typeValue".hashCode().hashCode(),
                        title = movieName,
                        imageUrl = imageUrl,
                        categoryUrl = movieUrl,
                        episodeUrl = null,
                        episodeNumber = null,
                        typeValue = typeValue,
                        insertionOrder = i,
                        releasedDate = releasedDate
                    )
                )
                i++
            }
            return animeMetaModelList
        }

        fun parseGenres(response: String) : ArrayList<GenreModel>{
            val genreList: ArrayList<GenreModel> = ArrayList()
            val document = Jsoup.parse(response)
            val genres = document.getElementsByClass("menu_series genre right").first()
            val genreHtmlList = genres.select("a")
            genreList.addAll(getGenreList(genreHtmlList))
            return genreList
        }

        fun parseAnimeInfo(response: String): AnimeInfoModel{
            val document = Jsoup.parse(response)
            val animeInfo = document.getElementsByClass("anime_info_body_bg")
            val animeUrl = animeInfo.select("img").first().absUrl("src")
            val animeTitle = animeInfo.select("h1").first().text()
            val lists = document?.getElementsByClass("type")
            lateinit var type: String
            lateinit var releaseTime: String
            lateinit var status: String
            lateinit var plotSummary: String
            val genre: ArrayList<GenreModel> = ArrayList()
            lists?.forEachIndexed { index, element ->
                when(index){
                    0-> type = element.text()
                    1-> plotSummary = element.text()
                    2-> genre.addAll(getGenreList(element.select("a")))
                    3-> releaseTime = element.text()
                    4-> status = element.text()
                }
            }
            val episodeInfo = document.getElementById("episode_page")
            val episodeList = episodeInfo.select("a").last()
            val endEpisode = episodeList.attr("ep_end")
            val alias = document.getElementById("alias_anime").attr("value")
            val id = document.getElementById("movie_id").attr("value")
            return AnimeInfoModel(
                id= id,
                animeTitle = animeTitle,
                imageUrl = animeUrl,
                type = formatInfoValues(type),
                releasedTime = formatInfoValues(releaseTime),
                status = formatInfoValues(status),
                genre = genre,
                plotSummary = formatInfoValues(plotSummary).trim(),
                alias = alias,
                endEpisode = endEpisode
            )

        }



        fun updatekeys(response: String,flag:Boolean){
            if (flag){
                try {
                    val obj = JSONObject(response)
                    val key = obj.getString("key")
                    val iv = obj.getString("iv")
                    val secondkey = obj.getString("second_key")
                    if(C.GogoSecretIV != iv || C.GogoSecretkey != key  || C.GogoSecretSecondKey != secondkey){
                        C.GogoSecretIV = iv
                        C.GogoSecretkey = key
                        C.GogoSecretSecondKey = secondkey
                    }
                }catch (exp:NullPointerException){
                    Timber.e(exp)
                }
            }else{
                val ref = PreferenceHelper.sharedPreference
                try {
                    val obj = JSONObject(response)
                    val domain = obj.getString("domain")
                    val origin = obj.getString("origin")
                    val referer = obj.getString("referer")
                    if( ref.getBaseUrl() != domain || ref.getOrigin() != origin  || ref.getReferrer() != referer){
                        ref.setBaseUrl(domain)
                        ref.setOrigin(origin)
                        ref.setReferrer(referer)
                    }
                }catch (exp:NullPointerException){
                    Timber.e(exp)
                }
            }


        }


        fun fetchEpisodeList(response: String): ArrayList<EpisodeModel>{
            val episodeList = ArrayList<EpisodeModel>()
            val document = Jsoup.parse(response)
            val lists = document?.select("li")
            lists?.forEach {
                val episodeUrl = it.select("a").first().attr("href").trim()
                val episodeNumber = it.getElementsByClass("name").first().text()
                val episodeType = it.getElementsByClass("cate").first().text()
                episodeList.add(
                    EpisodeModel(
                        episodeNumber = episodeNumber,
                        episodeType = episodeType,
                        episodeurl = episodeUrl
                    )
                )
            }
            return episodeList
        }

        private fun filterGenreName(genreName: String): String{
            return if(genreName.contains(',')){
                genreName.substring(genreName.indexOf(',')+1)
            }else{
                genreName
            }
        }

        private fun getGenreList(genreHtmlList: Elements): ArrayList<GenreModel>{
            val genreList = ArrayList<GenreModel>()
            genreHtmlList.forEach {
                val genreUrl = it.attr("href")
                val genreName = it.text().trim()

                genreList.add(
                    GenreModel(
                        genreUrl = genreUrl,
                        genreName = filterGenreName(genreName)
                    )
                )

            }

            return genreList
        }

        private fun formatInfoValues(infoValue: String): String{
            return infoValue.substring(infoValue.indexOf(':')+1, infoValue.length)
        }

        private fun getCategoryUrl(url: String): String {
            return try{
                var categoryUrl =  url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))
                categoryUrl = "/category/$categoryUrl"
                categoryUrl
            }catch (exception: StringIndexOutOfBoundsException){
                Timber.e("Image URL: $url")
                ""
            }
        }

    }
}