package net.sanic.Kayuri.ui.main.genre

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import net.sanic.Kayuri.utils.CommonViewModel2
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import net.sanic.Kayuri.utils.parser.HtmlParser
import okhttp3.ResponseBody

class GenreViewModel(genreUrl: String) : CommonViewModel2() {

    private var genreUrl: String? = null
    private val genreRepository = GenreRepository()
    private var _genreList: MutableLiveData<ArrayList<AnimeMetaModel>> = MutableLiveData()
    private var pageNumber: Int = 1
    private var _canNextPageLoaded = true
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    var genreList: LiveData<ArrayList<AnimeMetaModel>> = _genreList

    init {
        this.genreUrl = genreUrl
        fetchGenreList()
    }

    fun fetchGenreList() {
        pageNumber = 1
        val list = _genreList.value
        list?.clear()
        _genreList.value = list.let { list }
        if (!super.isLoading()) {
            genreUrl?.let {
                compositeDisposable.add(
                    genreRepository.fetchGenreList(
                        it,
                        pageNumber
                    ).subscribeWith(getGenreObserver(C.TYPE_GENRE_NEW))
                )
                updateLoadingState(loading = Loading.LOADING, e = null, isListEmpty = isListEmpty())
            }
        }
    }

    fun fetchNextPage() {
        if (_canNextPageLoaded && !super.isLoading()) {
            genreUrl?.let {
                compositeDisposable.add(
                    genreRepository.fetchGenreList(
                        it,
                        pageNumber
                    ).subscribeWith(getGenreObserver(C.TYPE_GENRE_UPDATE))
                )
                updateLoadingState(loading = Loading.LOADING, e = null, isListEmpty = isListEmpty())
            }
        }
    }

    private fun getGenreObserver(searchType: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onComplete() {
            }

            override fun onNext(response: ResponseBody) {
                val list = HtmlParser.parseMovie(response = response.string(), typeValue = C.TYPE_DEFAULT)
                if (list.isEmpty() || list.size < 20) {
                    _canNextPageLoaded = false
                }
                if (searchType == C.TYPE_GENRE_NEW) {
                    _genreList.value = list
                    updateLoadingState(loading = Loading.COMPLETED, e = null, isListEmpty = isListEmpty())
                } else if (searchType == C.TYPE_GENRE_UPDATE) {
                    val updatedList = _genreList.value
                    updatedList?.addAll(list)
                    _genreList.value = updatedList.let { updatedList }
                    updateLoadingState(loading = Loading.COMPLETED, e = null, isListEmpty = isListEmpty())
                }
                pageNumber++
            }

            override fun onError(e: Throwable) {
                updateLoadingState(loading = Loading.ERROR, e = e, isListEmpty = isListEmpty())
            }

        }
    }

    override fun onCleared() {
        if(!compositeDisposable.isDisposed){
            compositeDisposable.dispose()
        }
        super.onCleared()
    }

    private fun isListEmpty(): Boolean{
        return _genreList.value.isNullOrEmpty()
    }

}