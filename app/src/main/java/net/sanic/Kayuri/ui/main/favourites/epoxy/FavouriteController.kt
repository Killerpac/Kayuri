package net.sanic.Kayuri.ui.main.favourites.epoxy

import android.view.View
import com.airbnb.epoxy.TypedEpoxyController
import net.sanic.Kayuri.utils.model.FavouriteModel

class FavouriteController(private var adapterCallbacks: EpoxySearchAdapterCallbacks) :
    TypedEpoxyController<ArrayList<FavouriteModel>>() {
    override fun buildModels(data: ArrayList<FavouriteModel>?) {
        data?.let { arrayList ->
            arrayList.forEach {

                FavouriteModel_()
                    .id(it.animeName)
                    .favouriteModel(it)
                    .spanSizeOverride { totalSpanCount, _, _ -> totalSpanCount / totalSpanCount }
                    .clickListener { model, holder, _, _ ->
                        adapterCallbacks.animeTitleClick(
                            model = model.favouriteModel(),
                            sharedTitle = holder.animeTitle,
                            sharedImage = holder.animeImageView
                        )
                    }
                    .addTo(this)
            }
        }
    }

    interface EpoxySearchAdapterCallbacks {
        fun animeTitleClick(model: FavouriteModel, sharedTitle: View, sharedImage: View)
    }

}