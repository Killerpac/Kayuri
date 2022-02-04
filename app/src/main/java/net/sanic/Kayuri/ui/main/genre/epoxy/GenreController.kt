package net.sanic.Kayuri.ui.main.genre.epoxy

import com.airbnb.epoxy.Typed2EpoxyController
import net.sanic.Kayuri.ui.main.genre.GenreFragment
import net.sanic.Kayuri.utils.epoxy.AnimeCommonModel_
import net.sanic.Kayuri.utils.epoxy.LoadingModel_
import net.sanic.Kayuri.utils.model.AnimeMetaModel

class GenreController(var adapterCallbacks: GenreFragment) : Typed2EpoxyController<ArrayList<AnimeMetaModel>, Boolean>() {

    override fun buildModels(data: ArrayList<AnimeMetaModel>?, isLoading: Boolean) {
        data?.forEach { animeMetaModel ->
            AnimeCommonModel_()
                .id(animeMetaModel.ID)
                .animeMetaModel(animeMetaModel)
                .spanSizeOverride { totalSpanCount, _, _ -> totalSpanCount / totalSpanCount }
                .clickListener { model, _, _, _ ->
                    adapterCallbacks.animeTitleClick(model = model.animeMetaModel())
                }
                .addTo(this)
        }
        LoadingModel_()
            .id("loading")
            .spanSizeOverride { totalSpanCount, _, _ ->
                totalSpanCount
            }
            .addIf(isLoading,this)
    }


    interface EpoxySearchAdapterCallbacks {
        fun animeTitleClick(model: AnimeMetaModel)

    }
}