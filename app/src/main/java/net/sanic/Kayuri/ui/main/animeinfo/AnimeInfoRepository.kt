package net.sanic.Kayuri.ui.main.animeinfo

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.FavouriteModel
import net.sanic.Kayuri.utils.realm.InitalizeRealm
import net.sanic.Kayuri.utils.rertofit.NetworkInterface
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody

class AnimeInfoRepository {

    private val retrofit = RetrofitHelper.getRetrofitInstance()
    private val realm = Realm.getInstance(InitalizeRealm.getConfig())

    fun fetchAnimeInfo(categoryUrl: String): Observable<ResponseBody> {
        val animeInfoService = retrofit.create(NetworkInterface.FetchAnimeInfo::class.java)
        return animeInfoService.get(Utils.getHeader(), categoryUrl).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchEpisodeList(id: String, endEpisode: String, alias: String): Observable<ResponseBody> {
        val animeEpisodeService = retrofit.create(NetworkInterface.FetchEpisodeList::class.java)
        return animeEpisodeService.get(id = id, endEpisode = endEpisode, alias = alias, header = Utils.getHeader())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    fun isFavourite(id: String): Boolean {
        val result = realm.where(FavouriteModel::class.java).equalTo("ID", id).findFirst()
        result?.let {
            return true
        } ?: return false
    }

    fun addToFavourite(favouriteModel: FavouriteModel) {
        realm.executeTransaction {
            it.insertOrUpdate(favouriteModel)
        }
    }

    fun removeFromFavourite(id: String) {
        realm.executeTransaction {
            it.where(FavouriteModel::class.java).equalTo("ID", id).findAll().deleteAllFromRealm()
        }

    }

}