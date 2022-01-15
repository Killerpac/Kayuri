package net.sanic.Kayuri.ui.main.animeinfo.epoxy

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import androidx.core.app.ActivityCompat
import com.airbnb.epoxy.TypedEpoxyController
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.realm.RealmList
import net.sanic.Kayuri.R
import net.sanic.Kayuri.ui.main.player.EpisodeRepository
import net.sanic.Kayuri.ui.main.player.VideoPlayerActivity
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.EpisodeModel
import net.sanic.Kayuri.utils.parser.HtmlParser
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import okhttp3.ResponseBody
import timber.log.Timber


class AnimeInfoController : TypedEpoxyController<ArrayList<EpisodeModel>>() {
    var animeName: String = ""
    var quality:RealmList<String> = RealmList()
    private var compositeDisposable = CompositeDisposable()
    private val episodeRepository = EpisodeRepository()
    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 41
    private lateinit var isWatchedHelper: net.sanic.Kayuri.utils.helper.WatchedEpisode
    private lateinit var load:AlertDialog
    override fun buildModels(data: ArrayList<EpisodeModel>?) {
        data?.forEach {
            EpisodeModel_()
                .id(it.episodeurl)
                .episodeModel(it)
                .clickListener { model, _, clickedView, _ ->
                    when(clickedView.id) {
                        R.id.cardView ->  startVideoActivity(model.episodeModel(), clickedView)
                        R.id.downloadbutton -> {
                            if(!checkPermissionForReadExtertalStorage(clickedView))
                            {
                                requestPermissionForReadExtertalStorage(clickedView)
                            }
                            else {
                                downloadshit(model.episodeModel(), clickedView)
                                   val shit = AlertDialog.Builder(clickedView.context,R.style.RoundedCornersDialog).apply {
                                    setView(R.layout.load)
                                    setCancelable(false)
                                }
                                load = shit.create()
                                load.show()
                            }
                        }
                    }

                }
                .spanSizeOverride { totalSpanCount, _, _ ->
                    totalSpanCount / totalSpanCount
                }
                .watchedProgress(isWatchedHelper.getWatchedDuration(it.episodeurl.hashCode()))
                .addTo(this)
        }
    }

    fun setAnime(animeName: String) {
        this.animeName = animeName
        isWatchedHelper = net.sanic.Kayuri.utils.helper.WatchedEpisode(animeName)
    }

    fun isWatchedHelperUpdated(): Boolean {
        return ::isWatchedHelper.isInitialized
    }

    private fun startVideoActivity(episodeModel: EpisodeModel, clickedView: View) {
        val intent = Intent(clickedView.context, VideoPlayerActivity::class.java)
        intent.putExtra("episodeUrl", episodeModel.episodeurl)
        intent.putExtra("episodeNumber", episodeModel.episodeNumber)
        intent.putExtra("animeName", animeName)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        clickedView.context.startActivity(intent)
    }

    private fun downloadshit(episodeModel: EpisodeModel, clickedView: View) {

        fun getEpisodeUrlObserver(type: Int): DisposableObserver<ResponseBody> {
            return object : DisposableObserver<ResponseBody>() {
                var urls:RealmList<String> = RealmList()
                override fun onComplete() {
                    var num = 0
                    if (!urls.isNullOrEmpty()) {
                        load.dismiss()
                       val dialog = AlertDialog.Builder(clickedView.context,R.style.RoundedCornersDialog)
                        dialog.apply {
                            setTitle("Choose Quality")
                            setSingleChoiceItems(quality.toTypedArray(), 0) { _, which ->
                                num = which
                            }
                            setPositiveButton("OK") { dialog, _ ->
                                urls[num]?.let { downloadmanager(it, episodeModel, clickedView) }
                                dialog.dismiss()
                            }
                            setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }

                        }
                        dialog.show()
                    }
                }
                override fun onNext(response: ResponseBody) {
                    when(type) {
                        C.TYPE_MEDIA_URL -> {
                            val episodeInfo = HtmlParser.parseMediaUrl(response = response.string())
                            episodeInfo.vidcdnUrl?.let {
                                    compositeDisposable.add(
                                        episodeRepository.fetchM3u8Url(episodeInfo.vidcdnUrl!!)
                                            .subscribeWith(
                                                getEpisodeUrlObserver(C.TYPE_M3U8_PREP)
                                            )
                                    )
                            }
                        }
                        C.TYPE_M3U8_URL -> {
                            val m3u8Url: Pair<RealmList<String>,RealmList<String>> = HtmlParser.parseencrypturls(response = response.string())
                            urls = m3u8Url.first
                            quality = m3u8Url.second

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
                    Snackbar.make(clickedView.rootView,"An Unexpected Error Occurred.Please Try Again Later",3000).show()
                }

            }
        }
        fun fetchdownloadlink(url: String) {
            compositeDisposable.add(
                episodeRepository.fetchEpisodeMediaUrl(url = url).subscribeWith(
                    getEpisodeUrlObserver(
                        C.TYPE_MEDIA_URL
                    )
                )
            )
        }
        fetchdownloadlink("${C.BASE_URL}${episodeModel.episodeurl}")
    }

    fun downloadmanager(link:String,episodeModel: EpisodeModel,clickedView: View){
        Timber.e(link)
        val download:DownloadManager.Request = DownloadManager.Request(Uri.parse(link))
            .setTitle("$animeName:${episodeModel.episodeNumber}")
            .setDescription("Downloading..")
            .addRequestHeader("Referer",C.REFERER)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES,"$animeName/${episodeModel.episodeNumber}.mp4")
        val manager = clickedView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(download)
        Snackbar.make(clickedView.rootView,"Started Downloading ${episodeModel.episodeNumber} of $animeName",4000).show()

    }
    private fun checkPermissionForReadExtertalStorage(clickedView: View): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result: Int =clickedView.context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    @Throws(Exception::class)
    fun requestPermissionForReadExtertalStorage(clickedView: View) {
        try {
            ActivityCompat.requestPermissions(
                (clickedView.context as Activity?)!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}