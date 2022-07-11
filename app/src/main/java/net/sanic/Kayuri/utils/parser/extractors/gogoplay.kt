package net.sanic.Kayuri.utils.parser.extractors

import android.os.Build
import io.realm.RealmList
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.EpisodeInfo
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.Jsoup
import timber.log.Timber
import java.net.URLDecoder
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class gogoplay {

    companion object{

        fun parseMediaUrl(response: String): EpisodeInfo {
            val mediaUrl: String?
            val document = Jsoup.parse(response)
            //GogoCdn and VidStream Server Same Logic Differrent Url
            val info = when(PreferenceHelper.sharedPreference.getserver()){
                0->{
                    document?.getElementsByClass("anime")?.first()?.select("a")
                }
                1->{
                    document?.getElementsByClass("vidcdn")?.first()?.select("a")
                }
                else->{
                    document?.getElementsByClass("anime")?.first()?.select("a")
                }
            }
            mediaUrl = info?.attr("data-video").toString()
            val nextEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()?.attr("href")
            val previousEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()?.attr("href")

            return EpisodeInfo(
                nextEpisodeUrl = nextEpisodeUrl,
                previousEpisodeUrl = previousEpisodeUrl,
                vidcdnUrl = mediaUrl
            )
        }

        fun decryptAES(encrypted: String, key: String, iv: String): String {
            val ix = IvParameterSpec(iv.toByteArray())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey,ix)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String(cipher.doFinal(Base64.getDecoder().decode(encrypted)))
            } else {
                String(cipher.doFinal(android.util.Base64.decode(encrypted,android.util.Base64.DEFAULT)))
            }
        }

        fun encryptAes(text: String, key: String,iv:String): String {
            val ix = IvParameterSpec(iv.toByteArray())
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val secretKey = SecretKeySpec(key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey,ix)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray()))
            } else {
                android.util.Base64.encodeToString(cipher.doFinal(text.toByteArray()), android.util.Base64.DEFAULT)
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

        fun parseencryptajax(response: String,id:String):String{
            return try {
                val document=Jsoup.parse(response)
                val value2 = document.select("script[data-name=\"episode\"]").attr("data-value")
                val decrypt = decryptAES(value2, C.GogoSecretkey, C.GogoSecretIV).replace("\t","").substringAfter(id)
                Timber.e(decrypt)
                val encrypted = encryptAes(id, C.GogoSecretkey, C.GogoSecretIV)
                "id=$encrypted$decrypt&alias=$id"
            }catch (e:Exception){
                e.toString()
            }
        }

        fun parseencrypturls(response: String): Pair<RealmList<String>, RealmList<String>>{
            val urls: RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            var i = 0
            return try {
                var crackit = JSONObject(response).getString("data")
                crackit = decryptAES(crackit, C.GogoSecretSecondKey, C.GogoSecretIV).replace("""o"<P{#meme":""","""e":[{"file":""")
                Timber.e(crackit)
                val res =  JSONObject(crackit).getJSONArray("source")
                while(i != res.length() && res.getJSONObject(i).getString("label") != "Auto") {
                    urls.add(res.getJSONObject(i).getString("file"))
                    qualities.add(
                        res.getJSONObject(i).getString("label").lowercase(Locale.getDefault()).filterNot { it.isWhitespace() })
                    i++
                }
                Pair(urls,qualities)
            }catch (exp: JSONException) {
                Pair(urls,qualities)
            }
        }

        fun parsegoogleurl(response: String): Pair<RealmList<String>, RealmList<String>> {
            val urls: RealmList<String> = RealmList()
            val qualities: RealmList<String> = RealmList()
            var i = 0
                return try {
                    var crackit = JSONObject(response).getString("data")
                    crackit = decryptAES(crackit, C.GogoSecretSecondKey, C.GogoSecretIV).replace("""o"<P{#meme":""","""e":[{"file":""")
                    val res = JSONObject(crackit).getJSONArray("source_bk")
                    while (i != res.length() && res.getJSONObject(i).getString("label") != "Auto") {
                        urls.add(
                            URLDecoder.decode(
                                res.getJSONObject(i).getString("file"),
                                Charsets.UTF_8.name()
                            )
                        )
                        Timber.e(res.getJSONObject(i).getString("file"))
                        qualities.add(
                            res.getJSONObject(i).getString("label").lowercase(Locale.getDefault())
                                .filterNot { it.isWhitespace() })
                        if (res.getJSONObject(i).getString("type") == "hls") break
                        i++
                    }
                    Pair(urls, qualities)
                } catch (exp: JSONException) {
                    Pair(urls,qualities)
                }

        }

    }
}