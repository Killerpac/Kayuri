package net.sanic.Kayuri.ui.main.search

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentSearchBinding
import net.sanic.Kayuri.databinding.LoadingBinding
import net.sanic.Kayuri.ui.main.search.epoxy.SearchController
import net.sanic.Kayuri.utils.CommonViewModel2
import net.sanic.Kayuri.utils.ItemOffsetDecoration
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.AnimeMetaModel


class SearchFragment : Fragment(), View.OnClickListener,
    SearchController.EpoxySearchAdapterCallbacks {

    private lateinit var searchBinding: FragmentSearchBinding
    private lateinit var loadingBinding: LoadingBinding
    private lateinit var viewModel: SearchViewModel
    private val searchController by lazy {
        SearchController(this)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        searchBinding = FragmentSearchBinding.inflate(inflater, container, false)
        loadingBinding = LoadingBinding.inflate(inflater, searchBinding.root)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        setOnClickListeners()
        setAdapters()
        setRecyclerViewScroll()
        setEditTextListener()
        showKeyBoard()
        return searchBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObserver()
    }

    private fun setEditTextListener() {
        searchBinding.searchEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.action == KeyEvent.ACTION_DOWN) {
                hideKeyBoard()
                searchBinding.searchEditText.clearFocus()
                viewModel.fetchSearchList(v.text.toString().trim())
                return@OnEditorActionListener true
            }
            false
        })
    }


    private fun setOnClickListeners() {
        searchBinding.backButton.setOnClickListener(this)
    }

    private fun setAdapters() {
        searchController.spanCount = Utils.calculateNoOfColumns(requireContext(), 150f)
        searchBinding.searchRecyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(requireContext(), 150f))
            adapter = searchController.adapter
            (layoutManager as GridLayoutManager).spanSizeLookup = searchController.spanSizeLookup
        }
        searchBinding.searchRecyclerView.addItemDecoration(
            ItemOffsetDecoration(
                context,
                R.dimen.episode_offset_left
            )
        )

    }

//    private fun getSpanCount(): Int {
//        val orientation = resources.configuration.orientation
//        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            5
//        } else {
//            3
//        }
//    }

    private fun setObserver() {


        viewModel.loadingModel.observe(viewLifecycleOwner, {
            if (it.isListEmpty) {
                if (it.loading == CommonViewModel2.Loading.LOADING) loadingBinding.loading.visibility =
                    View.VISIBLE
                //TODO Error Visibiity GONE

                else if (it.loading == CommonViewModel2.Loading.ERROR
                //Todo Error visisblity visible
                ) loadingBinding.loading.visibility = View.GONE
            } else {
                searchController.setData(
                    viewModel.searchList.value,
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

        })

//        viewModel.searchList.observe(viewLifecycleOwner, Observer {
//            searchController.setData(it ,viewModel.isLoading.value?.isLoading ?: false)
//            if(!it.isNullOrEmpty()){
//                hideKeyBoard()
//            }
//        })
//
//
//        viewModel.isLoading.observe( viewLifecycleOwner, Observer {
//            if(it.isLoading){
//                if(it.isListEmpty){
//                    rootView.loading.visibility =  View.VISIBLE
//                }else{
//                    rootView.loading.visibility = View.GONE
//                }
//            }else{
//               rootView.loading.visibility = View.GONE
//            }
//            searchController.setData(viewModel.searchList.value, it.isLoading)
//        })
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> {
                hideKeyBoard()
                findNavController().popBackStack()

            }
        }
    }

    private fun setRecyclerViewScroll() {
        searchBinding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManger = searchBinding.searchRecyclerView.layoutManager as GridLayoutManager
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

    private fun hideKeyBoard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    private fun showKeyBoard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchBinding.root, 0)
    }

    override fun animeTitleClick(model: AnimeMetaModel) {
        hideKeyBoard()
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToAnimeInfoFragment(
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