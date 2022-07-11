package net.sanic.Kayuri.ui.main.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.RealmList
import net.sanic.Kayuri.utils.CommonViewModel
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.Content
import net.sanic.Kayuri.utils.parser.extractors.gogoplay
import net.sanic.Kayuri.utils.parser.extractors.streamsb
import net.sanic.Kayuri.utils.parser.extractors.xstreamcdn
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import okhttp3.ResponseBody
import timber.log.Timber

class VideoPlayerViewModel : CommonViewModel() {

    private val episodeRepository = EpisodeRepository()
    private var compositeDisposable = CompositeDisposable()
    private var _content = MutableLiveData(Content())
    private var id:String = ""
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
                when (PreferenceHelper.sharedPreference.getserver()) {
                    0, 1 -> {
                        when (type) {
                            C.TYPE_MEDIA_URL -> {
                                val episodeInfo =
                                    gogoplay.parseMediaUrl(response = response.string())
                                id =
                                    Regex("id=([^&]+)").find(episodeInfo.vidcdnUrl!!)!!.value.removePrefix(
                                        "id="
                                    )
                                episodeInfo.vidcdnUrl?.let {
                                    if (PreferenceHelper.sharedPreference.getserver() == 1) {
                                        compositeDisposable.add(
                                            episodeRepository.fetchGogoPlayUrl(
                                                episodeInfo.vidcdnUrl!!
                                            )
                                                .subscribeWith(
                                                    getEpisodeUrlObserver(C.TYPE_M3U8_PREP)
                                                )
                                        )
                                    } else {
                                        compositeDisposable.add(
                                            episodeRepository.fetchM3u8Url(
                                                episodeInfo.vidcdnUrl!!,
                                                episodeInfo.vidcdnUrl!!
                                            )
                                                .subscribeWith(
                                                    getEpisodeUrlObserver(C.TYPE_M3U8_PREP)
                                                )
                                        )
                                    }
                                }

                                val watchedEpisode =
                                    episodeRepository.fetchWatchedDuration(_content.value?.episodeUrl.hashCode())
                                _content.value?.watchedDuration =
                                    watchedEpisode?.watchedDuration ?: 0
                                _content.value?.previousEpisodeUrl = episodeInfo.previousEpisodeUrl
                                _content.value?.nextEpisodeUrl = episodeInfo.nextEpisodeUrl
                            }
                            C.TYPE_M3U8_URL -> {
                                val m3u8Url: Pair<RealmList<String>, RealmList<String>> =
                                    if (PreferenceHelper.sharedPreference.getserver() == 1) {
                                        gogoplay.parsegoogleurl(response = response.string())
                                    } else {
                                        gogoplay.parseencrypturls(response = response.string())
                                    }
                                Timber.e(m3u8Url.toString())
                                val content = _content.value
                                content?.url = m3u8Url.first
                                content?.quality = m3u8Url.second
                                _content.value = content
                                saveContent(content!!)
                                updateLoading(false)
                            }
                            C.TYPE_M3U8_PREP -> {
                                val m3u8Pre =
                                    gogoplay.parseencryptajax(response = response.string(), id)
                                compositeDisposable.add(
                                    episodeRepository.m3u8preprocessor("${PreferenceHelper.sharedPreference.getReferrer()}encrypt-ajax.php?${m3u8Pre}")
                                        .subscribeWith(
                                            getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                        )
                                )
                            }
                        }

                    }
                    2 -> {
                        when (type) {
                            C.TYPE_MEDIA_URL -> {
                                val episodeInfo =
                                    streamsb.parseMediaUrl(response = response.string())
                                val pp = streamsb.parseurl(episodeInfo.vidcdnUrl!!)
                                episodeInfo.vidcdnUrl?.let {
                                    compositeDisposable.add(
                                        episodeRepository.fetchM3u8Url(
                                            pp, episodeInfo.vidcdnUrl!!
                                        )
                                            .subscribeWith(
                                                getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                            )
                                    )
                                }
                                val watchedEpisode =
                                    episodeRepository.fetchWatchedDuration(_content.value?.episodeUrl.hashCode())
                                _content.value?.watchedDuration =
                                    watchedEpisode?.watchedDuration ?: 0
                                _content.value?.previousEpisodeUrl = episodeInfo.previousEpisodeUrl
                                _content.value?.nextEpisodeUrl = episodeInfo.nextEpisodeUrl
                            }
                            C.TYPE_M3U8_URL -> {
                                val m3u8Url: Pair<RealmList<String>, RealmList<String>> =
                                    streamsb.parseencrypturls(response = response.string())
                                val content = _content.value
                                content?.url = m3u8Url.first
                                content?.quality = m3u8Url.second
                                _content.value = content
                                saveContent(content!!)
                                updateLoading(false)
                            }
                        }
                    }
                    3 -> {
                        when (type) {
                            C.TYPE_MEDIA_URL -> {
                                val episodeInfo =
                                    xstreamcdn.parseMediaUrl(response = response.string())
                                episodeInfo.vidcdnUrl?.let {
                                    compositeDisposable.add(
                                        episodeRepository.fetchXstreamCdn(
                                            episodeInfo.vidcdnUrl!!
                                        )
                                            .subscribeWith(
                                                getEpisodeUrlObserver(C.TYPE_M3U8_URL)
                                            )
                                    )
                                }
                                val watchedEpisode =
                                    episodeRepository.fetchWatchedDuration(_content.value?.episodeUrl.hashCode())
                                _content.value?.watchedDuration =
                                    watchedEpisode?.watchedDuration ?: 0
                                _content.value?.previousEpisodeUrl = episodeInfo.previousEpisodeUrl
                                _content.value?.nextEpisodeUrl = episodeInfo.nextEpisodeUrl
                            }
                            C.TYPE_M3U8_URL -> {
                                val m3u8Url: Pair<RealmList<String>, RealmList<String>> =
                                    xstreamcdn.parseencrypturls(response = response.string())
                                val content = _content.value
                                content?.url = m3u8Url.first
                                content?.quality = m3u8Url.second
                                _content.value = content
                                saveContent(content!!)
                                updateLoading(false)
                            }
                        }
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
        if (!content.url.isEmpty()) {
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