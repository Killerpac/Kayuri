package net.sanic.Kayuri.ui.main.genre

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.rertofit.NetworkInterface
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import okhttp3.HttpUrl
import okhttp3.ResponseBody

class GenreRepository {

    private val retrofit = RetrofitHelper.getRetrofitInstance()

    fun fetchGenreList(genreUrl: String, pageNumber: Int): Observable<ResponseBody> {
        val genreService = retrofit.create(NetworkInterface.FetchGenre::class.java)
        return genreService.get(Utils.getHeader(),genreUrl,pageNumber).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread())
    }

}