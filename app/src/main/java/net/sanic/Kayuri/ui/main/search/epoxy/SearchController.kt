package net.sanic.Kayuri.ui.main.search.epoxy

import android.view.View
import com.airbnb.epoxy.Typed2EpoxyController
import net.sanic.Kayuri.utils.epoxy.AnimeCommonModel_
import net.sanic.Kayuri.utils.epoxy.LoadingModel_
import net.sanic.Kayuri.utils.model.AnimeMetaModel

class SearchController(var adapterCallbacks: EpoxySearchAdapterCallbacks) : Typed2EpoxyController<ArrayList<AnimeMetaModel>, Boolean>() {


    override fun buildModels(data: ArrayList<AnimeMetaModel>?, isLoading: Boolean) {
        data?.forEach { animeMetaModel ->
            AnimeCommonModel_()
                .id(animeMetaModel.ID)
                .animeMetaModel(animeMetaModel)
                .spanSizeOverride { totalSpanCount, _, _ -> totalSpanCount / totalSpanCount }
                .clickListener { model, holder, _, _ ->
                    adapterCallbacks.animeTitleClick(model = model.animeMetaModel(),
                        sharedTitle = holder.animeTitle,
                        sharedImage = holder.animeImageView)
                }
                .addTo(this)
        }
        if(!data.isNullOrEmpty()){
            LoadingModel_()
                .id("loading")
                .spanSizeOverride { totalSpanCount, _, _ ->
                    totalSpanCount
                }
                .addIf(isLoading,this)
        }
    }


    interface EpoxySearchAdapterCallbacks{
        fun animeTitleClick(model: AnimeMetaModel, sharedTitle: View, sharedImage: View)
    }

}