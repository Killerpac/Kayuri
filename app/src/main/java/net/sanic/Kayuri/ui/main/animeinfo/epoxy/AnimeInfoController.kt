package net.sanic.Kayuri.ui.main.animeinfo.epoxy

import android.content.Intent
import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import net.sanic.Kayuri.R
import net.sanic.Kayuri.ui.main.animeinfo.AnimeInfoRepository
import net.sanic.Kayuri.ui.main.player.VideoPlayerActivity
import net.sanic.Kayuri.utils.model.EpisodeModel
import net.sanic.Kayuri.utils.model.RecentlyPlayed
import timber.log.Timber


class AnimeInfoController : TypedEpoxyController<ArrayList<EpisodeModel>>() {
    var animeName: String = ""
    var imageurl:String = ""
    var categoryurl:String = ""
    private lateinit var isWatchedHelper: net.sanic.Kayuri.utils.helper.WatchedEpisode
    private val repo = AnimeInfoRepository()
    override fun buildModels(data: ArrayList<EpisodeModel>?) {
        data?.forEach{
                EpisodeModel_()
                    .id(it.episodeurl)
                    .episodeModel(it)
                    .clickListener { model, _, clickedView, _ ->
                        when(clickedView.id) {
                            R.id.cardView ->  {
                                playedEpisode(model.episodeModel)
                                startVideoActivity(model.episodeModel(), clickedView)
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

    fun setanimeimageandcategoryurl(url:String?,categoryurl:String?) {
        Timber.e(url)
        Timber.e(categoryurl)
        this.imageurl = url.toString()
        this.categoryurl = categoryurl.toString()
    }

    fun isWatchedHelperUpdated(): Boolean {
        return ::isWatchedHelper.isInitialized
    }

    fun playedEpisode(episodeModel: EpisodeModel,categoryurl: String = this.categoryurl,animeName: String = this.animeName,imageUrl:String = this.imageurl){
        val playedmodel= RecentlyPlayed()
        playedmodel.episodeUrl = episodeModel.episodeurl
        playedmodel.episodeNumber = episodeModel.episodeNumber.replace("EP","Episode")
        playedmodel.imageUrl  = imageUrl
        playedmodel.title = animeName
        playedmodel.categoryUrl = categoryurl
        playedmodel.ID = episodeModel.episodeurl.hashCode()
        repo.addrecentplayed(playedmodel)
    }

    private fun startVideoActivity(episodeModel: EpisodeModel, clickedView: View) {
        val intent = Intent(clickedView.context, VideoPlayerActivity::class.java)
        intent.putExtra("episodeUrl", episodeModel.episodeurl)
        intent.putExtra("episodeNumber", episodeModel.episodeNumber)
        intent.putExtra("animeName", animeName)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        clickedView.context.startActivity(intent)
    }
}