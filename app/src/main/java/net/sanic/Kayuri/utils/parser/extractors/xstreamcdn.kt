package net.sanic.Kayuri.utils.parser.extractors

import io.realm.RealmList
import net.sanic.Kayuri.utils.model.EpisodeInfo
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup

class xstreamcdn {
    companion object {
        fun parseMediaUrl(response: String): EpisodeInfo {
            val mediaUrl: String?
            val document = Jsoup.parse(response)
            val info = document?.getElementsByClass("xstreamcdn")?.first()?.select("a")
            mediaUrl = info?.attr("data-video").toString().replace("/v/", "/api/source/")
            val nextEpisodeUrl =
                document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()
                    ?.attr("href")
            val previousEpisodeUrl =
                document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()
                    ?.attr("href")

            return EpisodeInfo(
                nextEpisodeUrl = nextEpisodeUrl,
                previousEpisodeUrl = previousEpisodeUrl,
                vidcdnUrl = mediaUrl
            )
        }

        fun parseencrypturls(response: String): Pair<RealmList<String>, RealmList<String>> {
            val urls: RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            return try {
                val res = JSONObject(response).getJSONArray("data")
                for (i in 0 until res.length()) {
                    val obj = res.getJSONObject(i)
                    val url = obj.getString("file")
                    val quality = obj.getString("label")
                    urls.add(url)
                    qualities.add(quality)
                }
                Pair(urls, qualities)
            }catch (exp: JSONException) {
                Pair(urls,qualities)
            }
        }
    }
}