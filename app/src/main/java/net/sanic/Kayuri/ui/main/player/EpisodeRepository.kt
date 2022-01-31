package net.sanic.Kayuri.ui.main.player

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.Content
import net.sanic.Kayuri.utils.model.WatchedEpisode
import net.sanic.Kayuri.utils.realm.InitalizeRealm
import net.sanic.Kayuri.utils.rertofit.NetworkInterface
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody
import retrofit2.Retrofit
import timber.log.Timber


class EpisodeRepository {
    private var retrofit: Retrofit = RetrofitHelper.getRetrofitInstance()!!
    private var realm = Realm.getInstance(InitalizeRealm.getConfig())


    fun fetchEpisodeMediaUrl(url: String): Observable<ResponseBody> {
        val mediaUrlService = retrofit.create(NetworkInterface.FetchEpisodeMediaUrl::class.java)
        return mediaUrlService.get(Utils.getHeader(),url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchGoogleUrl(url: String): Observable<ResponseBody> {
        val m3u8urlService = retrofit.create(NetworkInterface.FetchGoogleUrl::class.java)
        return m3u8urlService.get(Utils.getGoogle(),url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchM3u8Url(url: String): Observable<ResponseBody> {
        val m3u8urlService = retrofit.create(NetworkInterface.FetchM3u8Url::class.java)
        return m3u8urlService.get(Utils.getHeader(),url).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun m3u8preprocessor(url: String):Observable<ResponseBody> {
        val m3u8Url = retrofit.create(NetworkInterface.Fetch3u8preprocessor::class.java)
        return m3u8Url.get(Utils.getHeader(),url)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun fetchWatchedDuration(id: Int): WatchedEpisode?{
        return realm.where(WatchedEpisode::class.java).equalTo("id", id).findFirst()
    }

    fun fetchContent(episodeUrl: String): Content? {
        try {
            var content: Content? = null
            val result =  realm.where(Content::class.java).equalTo("episodeUrl", episodeUrl).findFirst()
            result?.let {
                content = realm.copyFromRealm(it)
            }
           // Timber.e("ID : %s", content?.episodeUrl.hashCode())
            val watchedEpisode = fetchWatchedDuration(content?.episodeUrl.hashCode())
            content?.watchedDuration = watchedEpisode?.watchedDuration ?: 0
            return content


        } catch (ignored: Exception) {
        }
        return null
    }


    fun saveContent(content: Content){
        try {
            content.insertionTime = System.currentTimeMillis()
            realm.executeTransactionAsync { realm1: Realm ->
                realm1.insertOrUpdate(content)
            }

            val progressPercentage: Long = ((content.watchedDuration.toDouble()/(content.duration).toDouble()) * 100).toLong()
            val watchedEpisode = WatchedEpisode(
                id = content.episodeUrl.hashCode(),
                watchedDuration = content.watchedDuration,
                watchedPercentage = progressPercentage,
                animeName = content.animeName

            )
            realm.executeTransactionAsync {
                it.insertOrUpdate(watchedEpisode)
            }
        } catch (ignored: Exception) {
        }
    }


    fun clearContent(){
            realm.executeTransactionAsync {
                val results = it.where(Content::class.java).lessThanOrEqualTo("insertionTime", System.currentTimeMillis() - C.MAX_TIME_M3U8_URL).findAll()
                results.deleteAllFromRealm()
            }
    }
}