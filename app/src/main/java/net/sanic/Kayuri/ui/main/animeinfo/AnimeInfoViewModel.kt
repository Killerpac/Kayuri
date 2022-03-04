package net.sanic.Kayuri.ui.main.animeinfo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import net.sanic.Kayuri.utils.CommonViewModel
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.AnimeInfoModel
import net.sanic.Kayuri.utils.model.EpisodeModel
import net.sanic.Kayuri.utils.model.FavouriteModel
import net.sanic.Kayuri.utils.parser.HtmlParser
import okhttp3.ResponseBody
import org.jetbrains.annotations.Async

class AnimeInfoViewModel(categoryUrl: String) : CommonViewModel() {

    private var categoryUrl: String? = null
    private var _animeInfoModel: MutableLiveData<AnimeInfoModel> = MutableLiveData()
    private var _episodeList: MutableLiveData<ArrayList<EpisodeModel>> = MutableLiveData()
    var episodeList: LiveData<ArrayList<EpisodeModel>> = _episodeList
    var animeInfoModel: LiveData<AnimeInfoModel> = _animeInfoModel
    private val animeInfoRepository = AnimeInfoRepository()
    private var compositeDisposable = CompositeDisposable()
    private var _isFavourite: MutableLiveData<Boolean> = MutableLiveData(false)
    var isFavourite: LiveData<Boolean> = _isFavourite

    init {
        this.categoryUrl = categoryUrl
        fetchAnimeInfo()
    }

    fun fetchAnimeInfo() {
        updateLoading(loading = true)
        updateErrorModel(false, null, false)
        categoryUrl?.let {
            compositeDisposable.add(
                animeInfoRepository.fetchAnimeInfo(it)
                    .subscribeWith(getAnimeInfoObserver(C.TYPE_ANIME_INFO))
            )
        }
    }

    private fun getAnimeInfoObserver(typeValue: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onNext(response: ResponseBody) {
                if (typeValue == C.TYPE_ANIME_INFO) {
                    val animeInfoModel = HtmlParser.parseAnimeInfo(response = response.string())
                    _animeInfoModel.value = animeInfoModel
                    compositeDisposable.add(
                        animeInfoRepository.fetchEpisodeList(
                            id = animeInfoModel.id,
                            endEpisode = animeInfoModel.endEpisode,
                            alias = animeInfoModel.alias
                        )
                            .subscribeWith(getAnimeInfoObserver(C.TYPE_EPISODE_LIST))
                    )
                    _isFavourite.value = animeInfoRepository.isFavourite(animeInfoModel.id)


                } else if (typeValue == C.TYPE_EPISODE_LIST) {
                    _episodeList.value = HtmlParser.fetchEpisodeList(response = response.string())
                    updateLoading(loading = false)

                }
            }

            override fun onComplete() {
              //  animeInfoModel.value?.let { animeInfoRepository.searchMalForAnime(it.animeTitle) }
            }

            override fun onError(e: Throwable) {
                updateLoading(loading = false)
                if (typeValue == C.TYPE_ANIME_INFO) {
                    updateErrorModel(show = true, e = e, isListEmpty = false)
                } else {
                    updateErrorModel(show = true, e = e, isListEmpty = true)
                }

            }

        }
    }


    fun toggleFavourite() {
        if (_isFavourite.value!!) {
            animeInfoModel.value?.id?.let { animeInfoRepository.removeFromFavourite(it) }
            _isFavourite.value = false
        } else {
            saveFavourite()

        }
    }

    private fun saveFavourite() {
        val model = animeInfoModel.value
        animeInfoRepository.addToFavourite(
            FavouriteModel(
                ID = model?.id,
                categoryUrl = categoryUrl,
                animeName = model?.animeTitle,
                releasedDate = model?.releasedTime,
                imageUrl = model?.imageUrl
            )
        )
        _isFavourite.value = true
    }

//    fun setUrl(url: String) {
//        this.categoryUrl = url
//    }

    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        if (isFavourite.value!!) {
            saveFavourite()
        }
        super.onCleared()
    }
}