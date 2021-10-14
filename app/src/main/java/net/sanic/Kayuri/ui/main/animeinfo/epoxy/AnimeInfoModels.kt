package net.sanic.Kayuri.ui.main.animeinfo.epoxy

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import kotlinx.android.synthetic.main.recycler_episode_item.view.*
import net.sanic.Kayuri.R

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
        holder.downloadbuton.setOnClickListener(clickListener)
        holder.progressBar.progress = if(watchedProgress >90) 100  else if(watchedProgress in 1..10) 10 else watchedProgress.toInt()
        holder.cardView.setCardBackgroundColor(ResourcesCompat.getColor(holder.cardView.resources, R.color.episode_background, null))

    }

    class HomeHeaderHolder : EpoxyHolder(){
        lateinit var episodeText: TextView
        lateinit var cardView: CardView
        lateinit var progressBar: ProgressBar
        lateinit var downloadbuton:ImageView

        override fun bindView(itemView: View) {
            episodeText = itemView.episodeNumber
            cardView = itemView.cardView
            progressBar = itemView.watchedProgress
            downloadbuton = itemView.downloadbutton
        }
    }

}