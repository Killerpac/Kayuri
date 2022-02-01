package net.sanic.Kayuri.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import net.sanic.Kayuri.utils.model.HomeScreenModel
import net.sanic.Kayuri.utils.model.UpdateModel
import net.sanic.Kayuri.utils.parser.HtmlParser
import net.sanic.Kayuri.utils.realm.InitalizeRealm
import okhttp3.ResponseBody
import timber.log.Timber

class HomeViewModel : ViewModel(){

    private val  homeRepository = HomeRepository()
    private var _animeList: MutableLiveData<ArrayList<HomeScreenModel>> = MutableLiveData(makeEmptyArrayList())
    var animeList : LiveData<ArrayList<HomeScreenModel>> = _animeList
    private var _updateModel: MutableLiveData<UpdateModel> = MutableLiveData()
    var updateModel : LiveData<UpdateModel> = _updateModel
    private val compositeDisposable = CompositeDisposable()
    private val realmListenerList = ArrayList<RealmResults<AnimeMetaModel>>()

    init {
        fetchHomeList()
    }

    private fun fetchHomeList(){
        fetchRecentSub()
        fetchRecentDub()
        fetchPopular()
        fetchNewSeason()
        fetchMovies()
    }
    private fun getHomeListObserver(typeValue: Int): DisposableObserver<ResponseBody> {
        return  object : DisposableObserver<ResponseBody>(){
            override fun onComplete() {
                Timber.d("Request Completed")

            }

            override fun onNext(response: ResponseBody) {
                val list =parseList(response = response.string(), typeValue = typeValue)
                homeRepository.addDataInRealm(list)
            }

            override fun onError(e: Throwable) {
                updateError(e)
            }

        }
    }

    private fun updateError(e: Throwable){
        var isListEmpty = true
        animeList.value?.forEach {
            if(!it.animeList.isNullOrEmpty()){
                isListEmpty = false
            }
        }
//        super.updateErrorModel(true , e , isListEmpty)

    }



    private fun parseList(response: String, typeValue: Int): ArrayList<AnimeMetaModel>{
        return when(typeValue){
            C.TYPE_RECENT_DUB -> HtmlParser.parseRecentSubOrDub(response,typeValue)
            C.TYPE_RECENT_SUB -> HtmlParser.parseRecentSubOrDub(response,typeValue)
            C.TYPE_POPULAR_ANIME -> HtmlParser.parsePopular(response,typeValue)
            C.TYPE_MOVIE -> HtmlParser.parseMovie(response,typeValue)
            C.TYPE_NEW_SEASON ->HtmlParser.parseMovie(response,typeValue)
            else -> ArrayList()
        }
    }

    private fun updateList(list: ArrayList<AnimeMetaModel>, typeValue: Int){
        val homeScreenModel = HomeScreenModel(
            typeValue = typeValue,
            type = Utils.getTypeName(typeValue),
            animeList = list
        )

        val newList = animeList.value!!
            try{
//               val preHomeScreenModel = newList[getPositionByType(typeValue)]
//                if(preHomeScreenModel.typeValue == homeScreenModel.typeValue){
                    newList[getPositionByType(typeValue)] = homeScreenModel
//                }else{
//                    newList.add(getPositionByType(typeValue),homeScreenModel)
//                }

            }catch (iobe: IndexOutOfBoundsException){
//                newList.add(getPositionByType(typeValue),homeScreenModel)
            }


//        newList.sortedByDescending {
//            Utils.getPositionByType(it.typeValue)
//        }
        _animeList.value = newList
    }

    private fun addRealmListener(typeValue: Int){
        val realm = Realm.getInstance(InitalizeRealm.getConfig())
        realm.use {
            val results = it.where(AnimeMetaModel::class.java).equalTo("typeValue",typeValue).sort("insertionOrder", Sort.ASCENDING)
                .findAll()

            results.addChangeListener { newResult :RealmResults<AnimeMetaModel> , _ ->
                    val newAnimeList = (it.copyFromRealm(newResult) as ArrayList<AnimeMetaModel>)
                    updateList(newAnimeList, typeValue)
            }
            realmListenerList.add(results)
        }
    }



    private fun getPositionByType(typeValue: Int): Int{
        val size = animeList.value!!.size
        return when(typeValue){
            C.TYPE_RECENT_SUB-> if(size >= C.RECENT_SUB_POSITION) C.RECENT_SUB_POSITION else size
            C.TYPE_RECENT_DUB-> if(size >= C.RECENT_DUB_POSITION) C.RECENT_DUB_POSITION else size
            C.TYPE_POPULAR_ANIME -> if(size >= C.POPULAR_POSITION) C.POPULAR_POSITION else size
            C.TYPE_MOVIE ->if(size >= C.MOVIE_POSITION) C.MOVIE_POSITION else size
            C.TYPE_NEW_SEASON ->if(size >= C.NEWEST_SEASON_POSITION) C.NEWEST_SEASON_POSITION else size
            else->size
        }
    }


    private fun makeEmptyArrayList(): ArrayList<HomeScreenModel>{
        var i = 1
        val arrayList: ArrayList<HomeScreenModel> = ArrayList()
        while (i<=6){
            arrayList.add(
                HomeScreenModel(
                    typeValue = i
                )
            )
            i++
        }
        return arrayList
    }

    private fun fetchRecentSub(){

        val list = homeRepository.fetchFromRealm(C.TYPE_RECENT_SUB)
        if(list.size >0){
            updateList(list,C.TYPE_RECENT_SUB)
        }
        compositeDisposable.add(homeRepository.fetchRecentSubOrDub(1, C.RECENT_SUB).subscribeWith(getHomeListObserver(C.TYPE_RECENT_SUB)))
        addRealmListener(C.TYPE_RECENT_SUB)
    }

    private fun fetchRecentDub(){
        val list = homeRepository.fetchFromRealm(C.TYPE_RECENT_DUB)
        if(list.size >0){
            updateList(list,C.TYPE_RECENT_DUB)
        }
        compositeDisposable.add(homeRepository.fetchRecentSubOrDub(1, C.RECENT_DUB).subscribeWith(getHomeListObserver(C.TYPE_RECENT_DUB)))
        addRealmListener(C.TYPE_RECENT_DUB)
    }

    private fun fetchMovies(){
        val list = homeRepository.fetchFromRealm(C.TYPE_MOVIE)
        if(list.size>0){
            updateList(list,C.TYPE_MOVIE)
        }
        compositeDisposable.add(homeRepository.fetchMovies(1).subscribeWith(getHomeListObserver(C.TYPE_MOVIE)))
        addRealmListener(C.TYPE_MOVIE)
    }

    private fun fetchPopular(){
        val list = homeRepository.fetchFromRealm(C.TYPE_POPULAR_ANIME)
        if(list.size>0){
            updateList(list,C.TYPE_POPULAR_ANIME)
        }
        compositeDisposable.add(homeRepository.fetchPopularFromAjax(1).subscribeWith(getHomeListObserver(C.TYPE_POPULAR_ANIME)))
        addRealmListener(C.TYPE_POPULAR_ANIME)
    }

    private fun fetchNewSeason(){
        val resultList = homeRepository.fetchFromRealm(C.TYPE_NEW_SEASON)
        if(resultList.size>0){
            updateList(resultList,C.TYPE_NEW_SEASON)
        }
        compositeDisposable.add(homeRepository.fetchNewestAnime(1).subscribeWith(getHomeListObserver(C.TYPE_NEW_SEASON)))
        addRealmListener(C.TYPE_NEW_SEASON)
    }

    override fun onCleared() {
        homeRepository.removeFromRealm()
        if(!compositeDisposable.isDisposed){
            compositeDisposable.dispose()
        }
        super.onCleared()
    }


}