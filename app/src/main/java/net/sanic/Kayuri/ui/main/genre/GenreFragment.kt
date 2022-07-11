package net.sanic.Kayuri.ui.main.genre

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import net.sanic.Kayuri.MainActivity
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentGenreBinding
import net.sanic.Kayuri.databinding.LoadingBinding
import net.sanic.Kayuri.ui.main.genre.epoxy.GenreController
import net.sanic.Kayuri.utils.CommonViewModel2
import net.sanic.Kayuri.utils.ItemOffsetDecoration
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.AnimeMetaModel
import java.util.*

class GenreFragment : Fragment(), View.OnClickListener,
    GenreController.EpoxySearchAdapterCallbacks {

    private lateinit var genreBinding: FragmentGenreBinding
    private lateinit var loadingBinding: LoadingBinding
    private lateinit var viewModelFactory: GenreViewModelFactory
    private lateinit var viewModel: GenreViewModel
    private lateinit var genreController:GenreController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        genreBinding = FragmentGenreBinding.inflate(inflater, container, false)
        loadingBinding = LoadingBinding.inflate(inflater, genreBinding.root)
        return genreBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransitions(view)
        val genreUrl = GenreFragmentArgs.fromBundle(requireArguments()).genreUrl
        viewModelFactory = GenreViewModelFactory(genreUrl)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GenreViewModel::class.java)
        setObserver(genreUrl.substring(genreUrl.lastIndexOf('/') + 1))
        setAdapters()
        setOnClickListeners()
        transitionListener()
        setRecyclerViewScroll()
    }

    private fun setOnClickListeners(){
        genreBinding.back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setTransitions(view: View) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = 250
        }
    }

    private fun setAdapters() {
        genreController = GenreController(this)
        genreController.spanCount = Utils.calculateNoOfColumns(requireContext(), 143f)
        genreBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(requireContext(), 143f))
            adapter = genreController.adapter
            (layoutManager as GridLayoutManager).spanSizeLookup = genreController.spanSizeLookup
        }
        genreBinding.recyclerView.addItemDecoration(
            ItemOffsetDecoration(
                context,
                R.dimen.episode_offset_left
            )
        )
    }

    override fun onStart() {
        shownavbar(View.GONE)
        super.onStart()
    }

    private fun shownavbar(visibility:Int,transition:Boolean = false){
        (requireActivity() as MainActivity).barvisibility(visibility)
    }

    private fun getSpanCount(): Int {
        val orientation = resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            5
        } else {
            3
        }
    }
    @SuppressLint("StringFormatInvalid")
    private fun setObserver(genreName: String) {

        viewModel.loadingModel.observe(viewLifecycleOwner) { it ->
            if (it.isListEmpty) {
                if (it.loading == CommonViewModel2.Loading.LOADING) loadingBinding.loading.visibility =
                    View.VISIBLE
                else if (it.loading == CommonViewModel2.Loading.ERROR) loadingBinding.loading.visibility =
                    View.GONE
            } else {
                genreBinding.header.text = getString(R.string.genre,
                    genreName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                genreBinding.toolbarText.text = getString(R.string.genre,
                    genreName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                genreController.setData(
                    viewModel.genreList.value,
                    it.loading == CommonViewModel2.Loading.LOADING
                )
                if (it.loading == CommonViewModel2.Loading.ERROR) view?.let { it1 ->
                    Snackbar.make(
                        it1,
                        getString(it.errorMsg),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                else if (it.loading == CommonViewModel2.Loading.COMPLETED) loadingBinding.loading.visibility =
                    View.GONE
            }
        }
    }

    private fun setRecyclerViewScroll() {
        genreBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManger = genreBinding.recyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManger.childCount
                val totalItemCount = layoutManger.itemCount
                val firstVisibleItemPosition = layoutManger.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    if (isNetworkAvailable()) {
                        viewModel.fetchNextPage()
                    } else {
                        Snackbar.make(
                            view!!,
                            getString(R.string.no_internet),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun transitionListener() {
        genreBinding.motionLayout.setTransitionListener(
            object : MotionLayout.TransitionListener {
                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {

                }

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    genreBinding.topView.cardElevation = 0F
                }

                override fun onTransitionChange(
                    p0: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    if (startId == R.id.start) {
                        genreBinding.topView.cardElevation = 20F * progress
                        genreBinding.toolbarText.alpha = progress
                    } else {
                        genreBinding.topView.cardElevation = 10F * (1 - progress)
                        genreBinding.toolbarText.alpha = (1 - progress)
                    }
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                    if(p1 == R.id.start){
                        genreBinding.toolbarText.visibility = View.GONE
                    }else{
                        genreBinding.toolbarText.visibility = View.VISIBLE
                    }
                }
            }
        )
    }
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back ->{
                findNavController().popBackStack()
            }
        }
    }

    override fun animeTitleClick(model: AnimeMetaModel, sharedTitle: View, sharedImage: View) {
        val extras = FragmentNavigatorExtras(
            sharedTitle to resources.getString(R.string.shared_title),
            sharedImage to resources.getString(R.string.shared_image)
        )
        findNavController().navigate(
            GenreFragmentDirections.actionGenreFragmentToAnimeInfoFragment(
                categoryUrl = model.categoryUrl,
                animeName = model.title,
                animeImageUrl = model.imageUrl
            ), extras
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

}