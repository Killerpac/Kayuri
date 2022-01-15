package net.sanic.Kayuri.ui.main.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.RealmList
import net.sanic.Kayuri.utils.CommonViewModel
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.Content
import net.sanic.Kayuri.utils.parser.HtmlParser
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import okhttp3.ResponseBody
import timber.log.Timber

class VideoPlayerViewModel : CommonViewModel() {

    private val episodeRepository = EpisodeRepository()
    private var compositeDisposable = CompositeDisposable()
    private var _content = MutableLiveData(Content())
    var liveContent: LiveData<Content> = _content

    init {
        episodeRepository.clearContent()
    }

    fun fetchEpisodeMediaUrl(fetchFromDb: Boolean = true) {
        liveContent.value?.episodeUrl?.let {
            updateErrorModel(show = false, e = null, isListEmpty = false)
            updateLoading(loading = true)
            val result = episodeRepository.fetchContent(it)
            val animeName = _content.value?.animeName
            if (fetchFromDb) {
                result?.let {
                    result.animeName = animeName ?: ""
                    _content.value = result
                    updateLoading(false)
                } ?: kotlin.run {
                    fetchFromInternet(it)
                }
            } else {
                fetchFromInternet(it)
            }
        }
    }

    private fun fetchFromInternet(url: String) {
        compositeDisposable.add(
            episodeRepository.fetchEpisodeMediaUrl(url = url).subscribeWith(
                getEpisodeUrlObserver(
                    C.TYPE_MEDIA_URL
                )
            )
        )
    }

    fun updateEpisodeContent(content: Content) {
        _content.value = content
    }

    private fun getEpisodeUrlObserver(type: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onComplete() {
                updateErrorModel(show = false, e = null, isListEmpty = false)
            }

            override fun onNext(response: ResponseBody) {
                when(type) {
                    C.TYPE_MEDIA_URL -> {
                        val episodeInfo = HtmlParser.parseMediaUrl(response = response.string())
                        episodeInfo.vidcdnUrl?.let {
                            if (PreferenceHelper.sharedPreference.getGoogleServer()) {
                                compositeDisposable.add(
                                    episodeRepository.fetchGoogleUrl(
                                        episodeInfo.vidcdnUrl!!
                                    )
                                        .subscribeWith(
                                            getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                        )
                                )
                            } else {
                                compositeDisposable.add(
                                    episodeRepository.fetchM3u8Url(episodeInfo.vidcdnUrl!!)
                                        .subscribeWith(
                                            getEpisodeUrlObserver(C.TYPE_M3U8_PREP)
                                        )
                                )
                            }
                        }
                        val watchedEpisode =
                            episodeRepository.fetchWatchedDuration(_content.value?.episodeUrl.hashCode())
                        _content.value?.watchedDuration = watchedEpisode?.watchedDuration ?: 0
                        _content.value?.previousEpisodeUrl = episodeInfo.previousEpisodeUrl
                        _content.value?.nextEpisodeUrl = episodeInfo.nextEpisodeUrl
                    }
                    C.TYPE_M3U8_URL -> {
                        val m3u8Url: Pair<RealmList<String>,RealmList<String>> =
                            if (PreferenceHelper.sharedPreference.getGoogleServer()) {
                                HtmlParser.parseencrypturls(response = response.string())
                            } else {
                                HtmlParser.parseencrypturls(response = response.string())
                            }
                        val content = _content.value
                        content?.url = m3u8Url.first
                        content?.quality = m3u8Url.second
                        _content.value = content
                        saveContent(content!!)
                        updateLoading(false)
                    }
                    C.TYPE_M3U8_PREP -> {
                        val m3u8Pre = HtmlParser.parseencryptajax(response = response.string())
                        compositeDisposable.add(
                            episodeRepository.m3u8preprocessor("${C.REFERER}encrypt-ajax.php?${m3u8Pre}")
                                .subscribeWith(
                                    getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                )
                        )
                    }
                }

            }

            override fun onError(e: Throwable) {
                updateLoading(false)
                updateErrorModel(true, e, false)
            }

        }
    }

    fun saveContent(content: Content) {
        if (!content.url.isNullOrEmpty()) {
            episodeRepository.saveContent(content)
        }
    }


    override fun onCleared() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
        super.onCleared()
    }
}