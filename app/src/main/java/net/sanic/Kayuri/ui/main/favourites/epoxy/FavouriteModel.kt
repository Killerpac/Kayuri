package net.sanic.Kayuri.ui.main.favourites.epoxy

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

@EpoxyModelClass(layout = R.layout.recycler_anime_common)
abstract class FavouriteModel : EpoxyModelWithHolder<FavouriteModel.MovieHolder>(){

    @EpoxyAttribute
    lateinit var favouriteModel: net.sanic.Kayuri.utils.model.FavouriteModel
    @EpoxyAttribute
    var clickListener: View.OnClickListener? = null

    override fun bind(holder: MovieHolder) {
        Glide.with(holder.animeImageView.context).load(favouriteModel.imageUrl).transition(
            DrawableTransitionOptions.withCrossFade()).into(holder.animeImageView)
        holder.animeTitle.text = favouriteModel.animeName
        favouriteModel.releasedDate?.let {
            val text = "Released: $it"
            holder.releasedDate.text = text
        }
        holder.root.setOnClickListener(clickListener)

        var animeTitleTransition = holder.root.context.getString(R.string.shared_title)
        animeTitleTransition =
            "${animeTitleTransition}_${favouriteModel.ID}"
        holder.animeTitle.transitionName = animeTitleTransition

        //Set Shared Element for Anime Image
        var animeImageTransition = holder.root.context.getString(R.string.shared_image)
        animeImageTransition =
            "${animeImageTransition}_${favouriteModel.imageUrl}"
        holder.animeImageView.transitionName = animeImageTransition

    }
    class MovieHolder : EpoxyHolder(){
        lateinit var recyclerAnimeCommonBinding: RecyclerAnimeCommonBinding
        lateinit var animeImageView: AppCompatImageView
        lateinit var animeTitle: TextView
        lateinit var releasedDate: TextView
        lateinit var root: ConstraintLayout

        override fun bindView(itemView: View) {
            recyclerAnimeCommonBinding = RecyclerAnimeCommonBinding.bind(itemView)
            animeImageView = recyclerAnimeCommonBinding.animeImage
            animeTitle = recyclerAnimeCommonBinding.animeTitle
            releasedDate = recyclerAnimeCommonBinding.releasedDate
            root = recyclerAnimeCommonBinding.getRoot()
        }

    }
}