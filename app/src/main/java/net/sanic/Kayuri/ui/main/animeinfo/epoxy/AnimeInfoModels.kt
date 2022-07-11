package net.sanic.Kayuri.ui.main.animeinfo.epoxy

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.RecyclerEpisodeItemBinding

@EpoxyModelClass(layout = R.layout.recycler_episode_item)
abstract class EpisodeModel : EpoxyModelWithHolder<EpisodeModel.HomeHeaderHolder>(){

    @EpoxyAttribute
    lateinit var episodeModel: net.sanic.Kayuri.utils.model.EpisodeModel
    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener
    @EpoxyAttribute
    var watchedProgress: Long = 0


    override fun bind(holder: HomeHeaderHolder) {
        super.bind(holder)
        holder.episodeText.text = episodeModel.episodeNumber
        holder.cardView.setOnClickListener(clickListener)
        holder.progressBar.progress = if(watchedProgress >90) 100  else if(watchedProgress in 1..10) 10 else watchedProgress.toInt()
        holder.cardView.setCardBackgroundColor(ResourcesCompat.getColor(holder.cardView.resources, R.color.episode_background, null))

    }

    class HomeHeaderHolder : EpoxyHolder(){
        private lateinit var episodeItemBinding: RecyclerEpisodeItemBinding
        lateinit var episodeText: TextView
        lateinit var cardView: CardView
        lateinit var progressBar: ProgressBar

        override fun bindView(itemView: View) {
            episodeItemBinding = RecyclerEpisodeItemBinding.bind(itemView)
            episodeText =   episodeItemBinding.episodeNumber
            cardView = episodeItemBinding.cardView
            progressBar = episodeItemBinding.watchedProgress
        }
    }

}