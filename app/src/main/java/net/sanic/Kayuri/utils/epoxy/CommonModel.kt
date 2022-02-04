package net.sanic.Kayuri.utils.epoxy

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.RecyclerAnimeCommonBinding
import net.sanic.Kayuri.utils.model.AnimeMetaModel

@EpoxyModelClass(layout = R.layout.recycler_anime_common)
abstract class AnimeCommonModel : EpoxyModelWithHolder<AnimeCommonModel.MovieHolder>(){

    @EpoxyAttribute
    lateinit var animeMetaModel: AnimeMetaModel
    @EpoxyAttribute
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: MovieHolder) {
        Glide.with(holder.animeImageView.context).load(animeMetaModel.imageUrl).transition(
            DrawableTransitionOptions.withCrossFade(100)).into(holder.animeImageView)
        holder.animeTitle.text = animeMetaModel.title
        animeMetaModel.releasedDate?.let {
            holder.releasedDate.text = it
        }
        holder.root.setOnClickListener(clickListener)

    }
    class MovieHolder : EpoxyHolder(){
        lateinit var commonBinding: RecyclerAnimeCommonBinding
        lateinit var animeImageView: AppCompatImageView
        lateinit var animeTitle: TextView
        lateinit var releasedDate: TextView
        lateinit var root: ConstraintLayout

        override fun bindView(itemView: View) {
            commonBinding = RecyclerAnimeCommonBinding.bind(itemView)
            animeImageView = commonBinding.animeImage
            animeTitle = commonBinding.animeTitle
            releasedDate = commonBinding.releasedDate
            root = commonBinding.root
        }

    }
}

@EpoxyModelClass(layout = R.layout.recycler_loading)
abstract class LoadingModel : EpoxyModelWithHolder<LoadingHolder>()

class LoadingHolder : EpoxyHolder() {
    override fun bindView(itemView: View) {
    }
}

