package net.sanic.Kayuri.ui.main.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.Duration
import com.github.javiersantos.appupdater.enums.UpdateFrom
import net.sanic.Kayuri.BuildConfig
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentHomeBinding
import net.sanic.Kayuri.ui.main.home.epoxy.HomeController
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import timber.log.Timber

class HomeFragment : Fragment(), View.OnClickListener, HomeController.EpoxyAdapterCallbacks {


    private val homeController by lazy {
        HomeController(this)
    }
    private var doubleClickLastTime = 0L
    private lateinit var homebind: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homebind = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        return homebind.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        setClickListeners()
        viewModelObserver()
        checkUpdate()
    }
    

    private fun setAdapter() {
       // homeController.isDebugLoggingEnabled = true
        val homeRecyclerView = homebind.recyclerView
        homeRecyclerView.layoutManager = LinearLayoutManager(context)
        homeRecyclerView.adapter = homeController.adapter
    }

    private fun viewModelObserver() {
        viewModel.animeList.observe(viewLifecycleOwner, {
            homeController.setData(it)
        })

//        viewModel.updateModel.observe(viewLifecycleOwner, {
//            Timber.e(it.whatsNew)
//            if (it.versionCode > BuildConfig.VERSION_CODE) {
//                showDialog(it.whatsNew)
//            }
//        })
    }

    private fun setTransitionListener() {

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
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
            }
            R.id.favorite -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToFavouriteFragment())
            }
            R.id.settings -> {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSettings())

            }
        }
    }

    override fun recentSubDubEpisodeClick(model: AnimeMetaModel) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToVideoPlayerActivity(
                episodeUrl = model.episodeUrl,
                animeName = model.title,
                episodeNumber = model.episodeNumber
            )
        )
    }

    override fun animeTitleClick(model: AnimeMetaModel) {
        if (!model.categoryUrl.isNullOrBlank()) {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAnimeInfoFragment(
                    categoryUrl = model.categoryUrl
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
            .setDuration(Duration.NORMAL)
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