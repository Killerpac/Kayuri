package net.sanic.Kayuri.ui.main.genre

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_animeinfo.*
import kotlinx.android.synthetic.main.fragment_animeinfo.view.*
import kotlinx.android.synthetic.main.fragment_animeinfo.view.back
import kotlinx.android.synthetic.main.fragment_genre.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.loading.view.*
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentGenreBinding
import net.sanic.Kayuri.databinding.FragmentSearchBinding
import net.sanic.Kayuri.databinding.LoadingBinding
import net.sanic.Kayuri.ui.main.animeinfo.AnimeInfoFragmentArgs
import net.sanic.Kayuri.ui.main.animeinfo.AnimeInfoViewModel
import net.sanic.Kayuri.ui.main.animeinfo.AnimeInfoViewModelFactory
import net.sanic.Kayuri.ui.main.animeinfo.epoxy.AnimeInfoController
import net.sanic.Kayuri.ui.main.genre.epoxy.GenreController
import net.sanic.Kayuri.ui.main.search.SearchFragmentDirections
import net.sanic.Kayuri.utils.CommonViewModel2
import net.sanic.Kayuri.utils.ItemOffsetDecoration
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.AnimeMetaModel

class GenreFragment : Fragment(), View.OnClickListener,
    GenreController.EpoxySearchAdapterCallbacks {

    private lateinit var genreBinding: FragmentGenreBinding
    private lateinit var loadingBinding: LoadingBinding
    private lateinit var viewModelFactory: GenreViewModelFactory
    private lateinit var viewModel: GenreViewModel
    private val genreController by lazy {
        GenreController(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        genreBinding = FragmentGenreBinding.inflate(inflater, container, false)
        loadingBinding = LoadingBinding.inflate(inflater, genreBinding.root)
        return genreBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var genreUrl = GenreFragmentArgs.fromBundle(requireArguments()).genreUrl!!
        viewModelFactory = GenreViewModelFactory(genreUrl)
        viewModel = ViewModelProvider(this, viewModelFactory).get(GenreViewModel::class.java)
        setObserver(genreUrl.substring(genreUrl.lastIndexOf('/') + 1))
        setOnClickListeners()
        setAdapters()
        setRecyclerViewScroll()
    }

    private fun setOnClickListeners(){
        genreBinding.back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setAdapters() {
        genreController.spanCount = Utils.calculateNoOfColumns(requireContext(), 150f)
        genreBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(requireContext(), 150f))
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

    private fun getSpanCount(): Int {
        val orientation = resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            5
        } else {
            3
        }
    }

    private fun setObserver(genreName: String) {
        viewModel.loadingModel.observe(viewLifecycleOwner, {
            if (it.isListEmpty) {
                if (it.loading == CommonViewModel2.Loading.LOADING) loadingBinding.loading?.visibility = View.VISIBLE
                else if (it.loading == CommonViewModel2.Loading.ERROR) loadingBinding.loading?.visibility = View.GONE
            } else {
                genreBinding.header.text = getString(R.string.genre, genreName.capitalize())
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
                else if (it.loading == CommonViewModel2.Loading.COMPLETED) loadingBinding.loading?.visibility = View.GONE
            }
        })
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back ->{
                findNavController().popBackStack()
            }
        }
    }

    override fun animeTitleClick(model: AnimeMetaModel) {
        findNavController().navigate(
            GenreFragmentDirections.actionGenreFragmentToAnimeInfoFragment(
                categoryUrl = model.categoryUrl
            )
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