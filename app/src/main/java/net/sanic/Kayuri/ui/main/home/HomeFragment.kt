package net.sanic.Kayuri.ui.main.home

import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.material.transition.MaterialFadeThrough
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentHomeBinding
import net.sanic.Kayuri.ui.main.home.epoxy.HomeController
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import net.sanic.Kayuri.utils.model.GenreModel

class HomeFragment : Fragment(), View.OnClickListener, HomeController.EpoxyAdapterCallbacks {


    private lateinit var homeController:HomeController
    private var doubleClickLastTime = 0L
    private lateinit var homebind: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        homebind = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return homebind.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTransitions(view)
        setAdapter()
        setClickListeners()
        viewModelObserver()
        checkUpdate()
    }

    private fun setAdapter() {
       // homeController.isDebugLoggingEnabled = true
        homeController = HomeController(this)
        val homeRecyclerView = homebind.recyclerView
        homeRecyclerView.layoutManager = LinearLayoutManager(context)
        homeRecyclerView.adapter = homeController.adapter
    }

    private fun viewModelObserver() {
        viewModel.animeList.observe(viewLifecycleOwner) {
            homeController.setData(it)
        }
//        viewModel.updateModel.observe(viewLifecycleOwner, {
//            Timber.e(it.whatsNew)
//            if (it.versionCode > BuildConfig.VERSION_CODE) {
//                showDialog(it.whatsNew)
//            }
//        })
    }


    private fun setupTransitions(view: View) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        exitTransition = MaterialFadeThrough().apply {
            duration = 300
        }
        reenterTransition = MaterialFadeThrough().apply {
            duration = 300
        }
    }

    private fun setClickListeners() {
        homebind.header.setOnClickListener(this)
        homebind.search.setOnClickListener(this)
        homebind.favorite.setOnClickListener(this)
        homebind.settings.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.header -> {
                doubleClickLastTime = if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                    homebind.recyclerView.smoothScrollToPosition(0)
                    0L
                } else {
                    System.currentTimeMillis()
                }

            }
            R.id.search -> {
                val extras =
                    FragmentNavigatorExtras(homebind.search to resources.getString(R.string.search_transition))
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToSearchFragment(),
                    extras
                )
            }
            R.id.favorite -> {
                val extras = FragmentNavigatorExtras(
                    homebind.favorite to resources.getString(R.string.favourite_transition)

                )
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToFavouriteFragment(),
                    extras
                )
            }
            R.id.settings -> {
                val extras = FragmentNavigatorExtras(
                    homebind.settings to resources.getString(R.string.settings_transition)

                )
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToSettings(),
                    extras
                )
            }
        }
    }

    override fun recentSubDubEpisodeClick(model: AnimeMetaModel) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToVideoPlayerActivity(
                episodeUrl = model.episodeUrl,
                animeName = model.title,
                episodeNumber = model.episodeNumber?.replace("Episode","EP")
            )
        )
    }

    override fun animeTitleClick(model: AnimeMetaModel,sharedTitle: View, sharedImage: View) {
        if (!model.categoryUrl.isNullOrBlank()) {

            val extras = FragmentNavigatorExtras(
                sharedTitle to resources.getString(R.string.shared_title),
                sharedImage to resources.getString(R.string.shared_image)
            )
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAnimeInfoFragment(
                    categoryUrl = model.categoryUrl,
                    animeImageUrl = model.imageUrl,
                    animeName = model.title
                ),
                extras
            )
        }
    }

    override fun tagClick(model: AnimeMetaModel, genreName: String) {
        if (!model.genreList.isNullOrEmpty()) {
            val genre = model.genreList!!.find { it.genreName == genreName }!!
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToGenreFragment(
                    genreUrl = genre.genreUrl, genreName = genre.genreName
                )
            )
        }
    }

    override fun genreClick(model: GenreModel) {
        if (model.genreUrl.isNotEmpty()) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToGenreFragment(
                    genreUrl = model.genreUrl, genreName = model.genreName
                )
            )
        }
    }

    private fun checkUpdate() {
        AppUpdater(context)
            .setDisplay(Display.DIALOG)
            .setGitHubUserAndRepo("Killerpac","Kayuri")
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setDisplay(Display.DIALOG)
            .showAppUpdated(false)
            .setCancelable(false)
            .setButtonDoNotShowAgain("")
            .start()
    }
//    private fun showDialog(whatsNew: String) {
//        AlertDialog.Builder(requireContext()).setTitle("New Update Available")
//            .setMessage("What's New ! \n$whatsNew")
//            .setCancelable(false)
//            .setPositiveButton("Update") { _, _ ->
//                val i = Intent(Intent.ACTION_VIEW)
//                i.data = Uri.parse(C.GIT_DOWNLOAD_URL)
//                startActivity(i)
//            }
//            .setNegativeButton("Not now") { dialog, _ ->
//                dialog.cancel()
//            }.show()
//    }

}