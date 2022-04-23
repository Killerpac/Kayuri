package net.sanic.Kayuri.utils.parser.extractors

import io.realm.RealmList
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.EpisodeInfo
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import timber.log.Timber
import java.lang.NullPointerException
import java.util.*

class streamsb {

    companion object{

        private fun bytesToHex(bytes: ByteArray): String {
            val hexArray = "0123456789ABCDEF".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v = bytes[j].toInt() and 0xFF

                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }

        fun parseMediaUrl(response: String): EpisodeInfo {
            val mediaUrl: String?
            val document = Jsoup.parse(response)
            val info = document?.getElementsByClass("streamsb")?.first()?.select("a")
            mediaUrl = info?.attr("data-video").toString()
            val nextEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()?.attr("href")
            val previousEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()?.attr("href")

            return EpisodeInfo(
                nextEpisodeUrl = nextEpisodeUrl,
                previousEpisodeUrl = previousEpisodeUrl,
                vidcdnUrl = mediaUrl
            )
        }

        fun parseurl(url: String): String {
            Timber.e(url.substringAfter("/e/"))
            return "https://sbplay2.xyz/sources43/7361696b6f757c7c${
                bytesToHex(url.substringAfter("/e/").encodeToByteArray())
            }7c7c616e696d646c616e696d646c7c7c73747265616d7362/616e696d646c616e696d646c7c7c363136653639366436343663363136653639366436343663376337633631366536393664363436633631366536393664363436633763376336313665363936643634366336313665363936643634366337633763373337343732363536313664373336327c7c616e696d646c616e696d646c7c7c73747265616d7362"
        }
        fun parseencrypturls(response: String): Pair<RealmList<String>, RealmList<String>>{
            Timber.e(response)
            val urls: RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            var i = 0
            return try {
                val res =  JSONObject(response).getJSONObject("stream_data").getString("file")
                Timber.e(res.toString())
                urls.add(res)
                qualities.add("hlsp")
                Pair(urls,qualities)
            }catch (exp: JSONException) {
                Pair(urls,qualities)
            }
        }
    }
}