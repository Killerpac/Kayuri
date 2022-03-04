package net.sanic.Kayuri.ui.main.search

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.rertofit.NetworkInterface
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import okhttp3.ResponseBody

class SearchRepository {

    private val retrofit = RetrofitHelper.getRetrofitInstance()

    fun fetchSearchList(keyWord: String, pageNumber: Int): Observable<ResponseBody>{
        val searchService = retrofit.create(NetworkInterface.FetchSearchData::class.java)
        return searchService.get(Utils.getHeader(),keyWord,pageNumber).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}