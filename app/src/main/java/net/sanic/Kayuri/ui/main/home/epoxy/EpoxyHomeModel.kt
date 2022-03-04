package net.sanic.Kayuri.ui.main.home.epoxy

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.*
import net.sanic.Kayuri.utils.Tags.GenreTags
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import net.sanic.Kayuri.utils.model.GenreModel
import org.apmem.tools.layouts.FlowLayout


@EpoxyModelClass(layout = R.layout.recycler_anime_recent_sub_dub_2)
abstract class AnimeSubDubModel2 : EpoxyModelWithHolder<AnimeSubDubModel2.SubDubHolder>(){

    @EpoxyAttribute
    lateinit var animeMetaModel: AnimeMetaModel
    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener

    override fun bind(holder: SubDubHolder) {
        Glide.with(holder.animeImageView.context).load(animeMetaModel.imageUrl)
            .into(holder.animeImageView)
        holder.animeTitle.text = animeMetaModel.title
        holder.animeEpisode.text = animeMetaModel.episodeNumber
        holder.animeImageView.setOnClickListener(clickListener)
        holder.animeTitle.setOnClickListener(clickListener)

        //Set Shared Element for Anime Title
        var animeTitleTransition = holder.animeTitle.context.getString(R.string.shared_title)
        animeTitleTransition =
            "${animeTitleTransition}_${animeMetaModel.title}_${animeMetaModel.ID}"
        holder.animeTitle.transitionName = animeTitleTransition

        //Set Shared Element for Anime Image
        var animeImageTransition = holder.animeImageView.context.getString(R.string.shared_image)
        animeImageTransition =
            "${animeImageTransition}_${animeMetaModel.imageUrl}_${animeMetaModel.ID}"
        holder.animeImageView.transitionName = animeImageTransition
    }

    class SubDubHolder : EpoxyHolder(){
        lateinit var subDub2Binding: RecyclerAnimeRecentSubDub2Binding
        lateinit var animeImageView: AppCompatImageView
        lateinit var animeCardView: CardView
        lateinit var animeTitle: TextView
        lateinit var animeEpisode: TextView

        override fun bindView(itemView: View) {
            subDub2Binding = RecyclerAnimeRecentSubDub2Binding.bind(itemView)
            animeImageView = subDub2Binding.animeImage
            animeCardView = subDub2Binding.animeCardView
            animeTitle = subDub2Binding.animeTitle
            animeEpisode = subDub2Binding.episodeNumber
        }

    }
}

@EpoxyModelClass(layout = R.layout.recycler_anime_popular)
abstract class AnimePopularModel : EpoxyModelWithHolder<AnimePopularModel.PopularHolder>(){

    @EpoxyAttribute
    lateinit var animeMetaModel: AnimeMetaModel
    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener
    @EpoxyAttribute
    lateinit var tagClickListener: View.OnClickListener

    override fun bind(holder: PopularHolder) {
        Glide.with(holder.animeImageView.context).load(animeMetaModel.imageUrl)
            .into(holder.animeImageView)
        holder.animeTitle.text = animeMetaModel.title
        holder.animeEpisode.text = animeMetaModel.episodeNumber

        holder.flowLayout.removeAllViews()

        animeMetaModel.genreList?.forEach {
            val genreView = GenreTags(holder.flowLayout.context).getGenreTag(genreName = it.genreName, genreUrl = it.genreUrl, onClickListener = tagClickListener)
            holder.flowLayout.addView(genreView)
        }
        holder.rootView.setOnClickListener(clickListener)

        var animeTitleTransition = holder.animeTitle.context.getString(R.string.shared_title)
        animeTitleTransition =
            "${animeTitleTransition}_${animeMetaModel.title}_${animeMetaModel.ID}"
        holder.animeTitle.transitionName = animeTitleTransition

        //Set Shared Element for Anime Image
        var animeImageTransition = holder.animeImageView.context.getString(R.string.shared_image)
        animeImageTransition =
            "${animeImageTransition}_${animeMetaModel.imageUrl}_${animeMetaModel.ID}"
        holder.animeImageView.transitionName = animeImageTransition

    }
    class PopularHolder : EpoxyHolder(){
        lateinit var popularBinding: RecyclerAnimePopularBinding
        lateinit var animeImageView: AppCompatImageView
        lateinit var animeTitle: TextView
        lateinit var animeEpisode: TextView
        lateinit var flowLayout: FlowLayout
        lateinit var rootView: ConstraintLayout
        lateinit var cardView: CardView

        override fun bindView(itemView: View) {
            popularBinding = RecyclerAnimePopularBinding.bind(itemView)
            animeImageView = popularBinding.animeImage
            animeTitle = popularBinding.animeTitle
            animeEpisode = popularBinding.episodeNumber
            flowLayout = popularBinding.flowLayout
            rootView = popularBinding.rootLayout
            cardView = popularBinding.animeCardView
        }

    }
}

@EpoxyModelClass(layout = R.layout.recycler_anime_mini_header)
abstract class AnimeMiniHeaderModel : EpoxyModelWithHolder<AnimeMiniHeaderModel.AnimeMiniHeaderHolder>(){

    @EpoxyAttribute lateinit var typeName: String

    override fun bind(holder: AnimeMiniHeaderHolder) {
        super.bind(holder)
        holder.animeType.text = typeName
    }


    class AnimeMiniHeaderHolder : EpoxyHolder(){

        lateinit var animeType: TextView
        lateinit var miniHeaderBinding: RecyclerAnimeMiniHeaderBinding

        override fun bindView(itemView: View) {
            miniHeaderBinding = RecyclerAnimeMiniHeaderBinding.bind(itemView)
            animeType = miniHeaderBinding.typeName
        }

    }

}

@EpoxyModelClass(layout = R.layout.tags_genre)
abstract class HomeGenresModel : EpoxyModelWithHolder<HomeGenresModel.HomeGenresHolder>(){

    @EpoxyAttribute
    lateinit var genreModel: GenreModel
    @EpoxyAttribute
    lateinit var clickListener: View.OnClickListener


    override fun bind(holder: HomeGenresHolder) {
        super.bind(holder)
        holder.genre.text = genreModel.genreName
        holder.genre.setOnClickListener(clickListener)
    }

    class HomeGenresHolder : EpoxyHolder(){

        lateinit var genre: TextView
        lateinit var homeGenresBinding: TagsGenreBinding

        override fun bindView(itemView: View) {
            homeGenresBinding = TagsGenreBinding.bind(itemView)
            genre = homeGenresBinding.genre
        }

    }

}
