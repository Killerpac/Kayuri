package net.sanic.Kayuri.ui.main.animeinfo

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import net.sanic.Kayuri.databinding.FragmentAnimeinfoBinding
import net.sanic.Kayuri.databinding.LoadingBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.tags_genre.view.*
import net.sanic.Kayuri.R
import net.sanic.Kayuri.ui.main.animeinfo.epoxy.AnimeInfoController
import net.sanic.Kayuri.utils.ItemOffsetDecoration
import net.sanic.Kayuri.utils.Tags.GenreTags
import net.sanic.Kayuri.utils.Utils
import net.sanic.Kayuri.utils.model.AnimeInfoModel

class AnimeInfoFragment : Fragment() {

    private lateinit var viewModelFactory: AnimeInfoViewModelFactory
    private lateinit var viewModel: AnimeInfoViewModel
    private val episodeController by lazy {
        AnimeInfoController()
    }

    private lateinit var animeInfoBinding: FragmentAnimeinfoBinding
    private lateinit var loadingBinding: LoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        animeInfoBinding = FragmentAnimeinfoBinding.inflate(inflater, container, false)
        loadingBinding = LoadingBinding.inflate(inflater, animeInfoBinding.root)
        return animeInfoBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModelFactory = AnimeInfoViewModelFactory(AnimeInfoFragmentArgs.fromBundle(requireArguments()).categoryUrl!!)
        viewModel = ViewModelProvider(this, viewModelFactory).get(AnimeInfoViewModel::class.java)
        setupRecyclerView()
        setObserver()
        transitionListener()
        setOnClickListeners()
    }



    private fun setObserver() {
        viewModel.animeInfoModel.observe(viewLifecycleOwner, {
            it?.let {
                updateViews(it)
            }
        })

        viewModel.episodeList.observe(viewLifecycleOwner, {
            it?.let {
                animeInfoBinding.animeInfoRoot.visibility = View.VISIBLE
                episodeController.setData(it)
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, {

            if(it.isLoading){
                loadingBinding.loading.visibility = View.VISIBLE
            }else{
                loadingBinding.loading.visibility = View.GONE
            }
        })

        viewModel.isFavourite.observe(viewLifecycleOwner, {
            if(it){
                animeInfoBinding.favourite.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_favorite , null))
            }else{
                animeInfoBinding.favourite.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_unfavorite , null))
            }
        })
    }

    private fun updateViews(animeInfoModel: AnimeInfoModel) {

        Glide.with(this).load(animeInfoModel.imageUrl).into(animeInfoBinding.animeInfoImage)
        animeInfoBinding.animeInfoReleased.text = animeInfoModel.releasedTime
        animeInfoBinding.animeInfoStatus.text = animeInfoModel.status
        animeInfoBinding.animeInfoType.text = animeInfoModel.type
        animeInfoBinding.animeInfoTitle.text = animeInfoModel.animeTitle
        animeInfoBinding.toolbarText.text = animeInfoModel.animeTitle

        animeInfoBinding.flowLayout.removeAllViews()
        animeInfoModel.genre.forEach {
            var genreUrl = it.genreUrl
            var genreView = GenreTags(requireContext()).getGenreTag(genreName = it.genreName, genreUrl = genreUrl)
            genreView.genre.setOnClickListener {
                findNavController().navigate(
                    AnimeInfoFragmentDirections.actionAnimeInfoFragmentToGenreFragment(
                        genreUrl = genreUrl
                    )
                )
            }
            animeInfoBinding.flowLayout.addView(genreView)
        }
        episodeController.setAnime(animeInfoModel.animeTitle)
        animeInfoBinding.animeInfoSummary.text = animeInfoModel.plotSummary
        animeInfoBinding.favourite.visibility = View.VISIBLE
        animeInfoBinding.animeInfoRoot.visibility = View.VISIBLE
    }

    private fun setupRecyclerView(){
        episodeController.spanCount = Utils.calculateNoOfColumns(requireContext(), 165f)
        animeInfoBinding.animeInfoRecyclerView.adapter = episodeController.adapter
        val itemOffsetDecoration = ItemOffsetDecoration(context, R.dimen.episode_offset_left)
        animeInfoBinding.animeInfoRecyclerView.addItemDecoration(itemOffsetDecoration)
        animeInfoBinding.animeInfoRecyclerView.apply {
            layoutManager = GridLayoutManager(context,Utils.calculateNoOfColumns(requireContext(), 160f))
            (layoutManager as GridLayoutManager).spanSizeLookup = episodeController.spanSizeLookup

        }
    }

    private fun transitionListener(){
        animeInfoBinding.motionLayout.setTransitionListener(
            object: MotionLayout.TransitionListener{
                override fun onTransitionTrigger(
                    p0: MotionLayout?,
                    p1: Int,
                    p2: Boolean,
                    p3: Float
                ) {}

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    animeInfoBinding.topView.cardElevation = 0F
                    animeInfoBinding.animeInfoSummary.elevation = 0F
                }

                override fun onTransitionChange(p0: MotionLayout?, startId: Int, endId: Int, progress: Float) {
                    if(startId == R.id.start){
                        animeInfoBinding.topView.cardElevation = 20F * progress
                        animeInfoBinding.animeInfoSummary.elevation = 20F * progress
                        animeInfoBinding.toolbarText.alpha = progress
                    }
                    else{
                        animeInfoBinding.topView.cardElevation = 10F * (1 - progress)
                        animeInfoBinding.animeInfoSummary.elevation = 10F * (1 - progress)
                        animeInfoBinding.toolbarText.alpha = (1-progress)
                    }
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                }

            }
        )
    }

    private fun setOnClickListeners(){
        animeInfoBinding.favourite.setOnClickListener {
            onFavouriteClick()
        }

        animeInfoBinding.animeInfoSummary.setOnClickListener{
                animeInfoBinding.animeInfoSummary.maxLines = 10
                animeInfoBinding.animeInfoSummary.movementMethod = ScrollingMovementMethod()
        }


        animeInfoBinding.back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun onFavouriteClick(){
        if(viewModel.isFavourite.value!!){
            Snackbar.make(animeInfoBinding.root, getText(R.string.removed_from_favourites), Snackbar.LENGTH_SHORT).show()
        }else{
            Snackbar.make(animeInfoBinding.root, getText(R.string.added_to_favourites), Snackbar.LENGTH_SHORT).show()
        }
        viewModel.toggleFavourite()
    }

    override fun onResume() {
        super.onResume()
        if(episodeController.isWatchedHelperUpdated()){
            episodeController.setData(viewModel.episodeList.value)
        }
    }

}