package net.sanic.Kayuri.utils.parser

import android.net.Uri.decode
import android.os.Build
import androidx.appcompat.app.WindowDecorActionBar
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.realm.RealmList
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.*
import okhttp3.internal.http2.Http2Reader
import org.apache.commons.lang3.RandomStringUtils
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import timber.log.Timber
import java.lang.Byte.decode
import java.lang.NullPointerException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

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

        fun parseMediaUrl(response: String): EpisodeInfo{
            val mediaUrl: String?
            val document = Jsoup.parse(response)
            val info = document?.getElementsByClass("anime")?.first()?.select("a")
            mediaUrl = info?.attr("data-video").toString()
            val nextEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()?.attr("href")
            val previousEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()?.attr("href")

            return EpisodeInfo(
                nextEpisodeUrl = nextEpisodeUrl,
                previousEpisodeUrl = previousEpisodeUrl,
                vidcdnUrl = mediaUrl
            )
        }

        private fun decryptAES(encrypted: String, key: String, iv: String): String {
            val ix = IvParameterSpec(iv.toByteArray())
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey,ix)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(cipher.doFinal(Base64.getDecoder().decode(encrypted)))
            } else {
                String(cipher.doFinal(android.util.Base64.decode(encrypted,android.util.Base64.DEFAULT)))
            }
        }

        private fun encryptAes(text: String, key: String,iv:String): String {
            val ix = IvParameterSpec(iv.toByteArray())
            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,ix)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray()+C.GogoPadding))
            } else {
                android.util.Base64.encodeToString(cipher.doFinal(text.toByteArray()+C.GogoPadding), android.util.Base64.DEFAULT)
            }
        }

//        fun parseencryptajax(response: String):String{
//            val document=Jsoup.parse(response)
//            val value6 = document.getElementsByAttributeValue("data-name","ts").attr("data-value")
//            val value5 = document.getElementsByAttributeValue("name","crypto").attr("content")
//            val value1 = decryptAES(document.getElementsByAttributeValue("data-name","crypto").attr("data-value"),URLDecoder.decode(value6+value6,Charsets.UTF_8.name()),URLDecoder.decode(value6,Charsets.UTF_8.name()))
//            val value4 = decryptAES(value5,URLDecoder.decode(value1,Charsets.UTF_8.name()),URLDecoder.decode(value6,Charsets.UTF_8.name()))
//            val value2 = RandomStringUtils.randomAlphanumeric(16)
//            val value3 = URLDecoder.decode(value4,Charsets.UTF_8.name()).toString()
//            val encrypted = encryptAes(value4.removeRange(value4.indexOf("&"),value4.length),URLDecoder.decode(value1,Charsets.UTF_8.name()),URLDecoder.decode(value2,Charsets.UTF_8.name()))
//            return "id="+encrypted+"&time="+"00"+value2+"00"+value3.substring(value3.indexOf("&"))
//        }

        //should be faster
        fun parseencryptajax(response: String):String{
            val document=Jsoup.parse(response)
            val value2 = document.select("script[data-name='crypto']").attr("data-value")
            val decryptkey = decryptAES(value2,C.GogoSecretkey,C.GogoSecretIV).replaceAfter("&","").removeSuffix("&")
            val encrypted = encryptAes(decryptkey, C.GogoSecretkey, C.GogoSecretIV)
            return "id=$encrypted"
        }

        fun parseencrypturls(response: String): Pair<RealmList<String>,RealmList<String>>{
            Timber.e(response)
            var crackit = JSONObject(response).getString("data")
            crackit = decryptAES(crackit,C.GogoSecretkey,C.GogoSecretIV).replace("""o"<P{#meme":""","""e":[{"file":""")
            val urls:RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            var i = 0
            val res =  JSONObject(crackit).getJSONArray("source")
            return try {
                while(i != res.length() && res.getJSONObject(i).getString("label") != "Auto") {
                    urls.add(res.getJSONObject(i).getString("file"))
                    qualities.add(
                        res.getJSONObject(i).getString("label").lowercase(Locale.getDefault()).filterNot { it.isWhitespace() })
                    i++
                }
                Pair(urls,qualities)
            }catch (exp: NullPointerException) {
                Pair(urls,qualities)
            }
        }

        fun parsegoogleurl(response: String): Pair<RealmList<String>,RealmList<String>>{
            var crackit = JSONObject(response).getString("data")
            crackit = decryptAES(crackit,C.GogoSecretkey,C.GogoSecretIV).replace("""o"<P{#meme":""","""e":[{"file":""")
            val urls:RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            var i = 0
            Timber.e(Jsoup.parse(crackit).toString())
            val res =  JSONObject(crackit).getJSONArray("source_bk")
            return try {
                while(i != res.length() && res.getJSONObject(i).getString("label") != "Auto") {
                    urls.add(URLDecoder.decode(res.getJSONObject(i).getString("file"),Charsets.UTF_8.name()))
                    Timber.e(res.getJSONObject(i).getString("file"))
                    qualities.add(
                        res.getJSONObject(i).getString("label").lowercase(Locale.getDefault()).filterNot { it.isWhitespace() })
                    if(res.getJSONObject(i).getString("type") == "hls") break
                    i++
                }
                Pair(urls,qualities)
            }catch (exp: NullPointerException) {
                Pair(urls,qualities)
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