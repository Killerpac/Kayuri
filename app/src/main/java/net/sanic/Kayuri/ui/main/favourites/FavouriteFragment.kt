package net.sanic.Kayuri.ui.main.favourites

import android.content.res.Configuration
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
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import net.sanic.Kayuri.MainActivity
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.FragmentFavouriteBinding
import net.sanic.Kayuri.databinding.FragmentSearchBinding
import net.sanic.Kayuri.ui.main.favourites.epoxy.FavouriteController
import net.sanic.Kayuri.utils.ItemOffsetDecoration
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.FavouriteModel

class FavouriteFragment: Fragment(), FavouriteController.EpoxySearchAdapterCallbacks,View.OnClickListener {
    private lateinit var viewModel: FavouriteViewModel
    private lateinit var favouriteBinding: FragmentFavouriteBinding
    private lateinit var searchBinding: FragmentSearchBinding
    private lateinit var favouriteController:FavouriteController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        favouriteBinding = FragmentFavouriteBinding.inflate(inflater, container, false)
        searchBinding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(FavouriteViewModel::class.java)
        setAdapters()
        transitionListener()
        setClickListeners()
        return favouriteBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shownavbar(View.VISIBLE)
        setTransitions(view = requireView())
        setObserver()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            favouriteController.spanCount =5
            (searchBinding.searchRecyclerView.layoutManager as GridLayoutManager).spanCount = 5
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            favouriteController.spanCount = 3
            (searchBinding.searchRecyclerView.layoutManager as GridLayoutManager).spanCount = 3
        }

    }

    private fun setTransitions(view: View) {
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 300
        }
        exitTransition = MaterialFadeThrough().apply {
            duration = 300
        }
        reenterTransition = MaterialFadeThrough().apply {
            duration = 300
        }
    }

    private fun setObserver(){
        viewModel.favouriteList.observe(viewLifecycleOwner) {
            favouriteController.setData(it)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        shownavbar(View.VISIBLE)
        super.onViewStateRestored(savedInstanceState)
    }

    private fun setAdapters(){
        favouriteController = FavouriteController(this)
        favouriteController.spanCount = Utils.calculateNoOfColumns(requireContext(), 150f)
        favouriteBinding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(requireContext(), 150f))
            adapter = favouriteController.adapter
            (layoutManager as GridLayoutManager).spanSizeLookup = favouriteController.spanSizeLookup
        }
        favouriteBinding.recyclerView.addItemDecoration(ItemOffsetDecoration(context,R.dimen.episode_offset_left))

    }

//    private fun getSpanCount(): Int {
//        val orientation = resources.configuration.orientation
//        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            5
//        } else {
//            3
//        }
//    }

    private fun transitionListener() {
        favouriteBinding.motionLayout.setTransitionListener(
            object : MotionLayout.TransitionListener {
                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {

                }

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    favouriteBinding.topView.cardElevation = 0F
                }

                override fun onTransitionChange(
                    p0: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    if (startId == R.id.start) {
                        favouriteBinding.topView.cardElevation = 20F * progress
                        favouriteBinding.toolbarText.alpha = progress
                    } else {
                        favouriteBinding.topView.cardElevation = 10F * (1 - progress)
                        favouriteBinding.toolbarText.alpha = (1 - progress)
                    }
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                }

            }
        )
    }
    private fun setClickListeners(){
        favouriteBinding.back.setOnClickListener(this)
    }

    override fun animeTitleClick(model: FavouriteModel, sharedTitle: View, sharedImage: View) {
        shownavbar(View.GONE)
        val extras = FragmentNavigatorExtras(
            sharedTitle to resources.getString(R.string.shared_title),
            sharedImage to resources.getString(R.string.shared_image)
        )
        findNavController().navigate(
            FavouriteFragmentDirections.actionFavouriteFragmentToAnimeInfoFragment(
                categoryUrl = model.categoryUrl,
                animeName = model.animeName!!,
                animeImageUrl = model.imageUrl!!
            ), extras
        )
    }

//    private fun setuphidehavbaronscroll(){
//        favouriteBinding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if(recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING){
//                    if(dy > 10 )
//                    {
//                        shownavbar(View.GONE,true)
//                    }
//                    else if(dy < -15 ){
//                        shownavbar(View.VISIBLE,true)
//                    }
//                }
//            }
//        })
//    }

    private fun shownavbar(visibility:Int,transition:Boolean = false){
        (requireActivity() as MainActivity).barvisibility(visibility)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.back ->{
                findNavController().navigateUp()
            }
        }
    }

}

