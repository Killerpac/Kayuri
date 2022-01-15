package net.sanic.Kayuri.ui.main.player

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.fragment_video_player.*
import net.sanic.Kayuri.R
import net.sanic.Kayuri.utils.model.Content
import net.sanic.Kayuri.utils.preference.Preference
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import timber.log.Timber

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerListener {

    private lateinit var viewModel: VideoPlayerViewModel
    private var episodeNumber: String? = ""
    private var animeName: String? = ""
    private lateinit var content: Content
    private val sharedPreference: Preference = PreferenceHelper.sharedPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        viewModel = ViewModelProvider(this).get(VideoPlayerViewModel::class.java)
        getExtra(intent)
        setObserver()
        goFullScreen()
    }



    override fun onNewIntent(intent: Intent?) {
        (playerFragment as VideoPlayerFragment).playOrPausePlayer(
            playWhenReady = false,
            loseAudioFocus = false
        )
        (playerFragment as VideoPlayerFragment).saveWatchedDuration()
        getExtra(intent)
        super.onNewIntent(intent)

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            goFullScreen()
        }
    }

    private fun getExtra(intent: Intent?) {
        val url = intent?.extras?.getString("episodeUrl")
        episodeNumber = intent?.extras?.getString("episodeNumber")
        animeName = intent?.extras?.getString("animeName")
        viewModel.updateEpisodeContent(
            Content(
                animeName = animeName ?: "",
                episodeUrl = url,
                episodeName = animeName!! + " (" + episodeNumber!! + ")",
                url = RealmList()
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }


    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && hasPipPermission()
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
            && sharedPreference.getPIPMode()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())
            } else {
                this.enterPictureInPictureMode()
            }
        }
    }

    override fun onStop() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && hasPipPermission()
            && sharedPreference.getPIPMode()
        ) {
            finishAndRemoveTask()
        }
        super.onStop()
    }

    override fun finish() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && sharedPreference.getPIPMode()
        ) {
            finishAndRemoveTask()
        }
        super.finish()
    }

    fun enterPipModeOrExit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
            && hasPipPermission()
            && sharedPreference.getPIPMode()

        ) {
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val params = PictureInPictureParams.Builder()
                    this.enterPictureInPictureMode(params.build())
                } else {
                    this.enterPictureInPictureMode()
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }

        } else {
            finish()

        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        exoPlayerView.useController = !isInPictureInPictureMode
    }

    private fun hasPipPermission(): Boolean {
        val appsOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                appsOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                appsOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            else -> {
                false
            }
        }
    }

    private fun setObserver() {
        viewModel.liveContent.observe(this, {
            this.content = it
            it?.let {
                if (!it.url.isNullOrEmpty()) {
                    (playerFragment as VideoPlayerFragment).updateContent(it)
                }
            }
        })
        viewModel.isLoading.observe(this, {
            (playerFragment as VideoPlayerFragment).showLoading(it.isLoading)
        })
        viewModel.errorModel.observe(this, {
            (playerFragment as VideoPlayerFragment).showErrorLayout(
                it.show,
                it.errorMsgId,
                it.errorCode
            )
        })
    }

    override fun onBackPressed() {
        enterPipModeOrExit()
    }

    private fun goFullScreen() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        else {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
    }

    override fun updateWatchedValue(content: Content) {
        viewModel.saveContent(content)
    }

    override fun playNextEpisode() {
        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.nextEpisodeUrl,
                episodeName = "$animeName (EP ${incrimentEpisodeNumber(content.episodeName!!)})",
                url = content.url,
                animeName = content.animeName,
                index = content.index,
                quality = content.quality
            )
        )
        viewModel.fetchEpisodeMediaUrl()

    }

    override fun playPreviousEpisode() {

        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.previousEpisodeUrl,
                episodeName = "$animeName (EP ${decrimentEpisodeNumber(content.episodeName!!)})",
                url = content.url,
                animeName = content.animeName,
                index = content.index,
                quality = content.quality
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }

    private fun incrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber++
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    private fun decrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber--
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    fun refreshM3u8Url() {
        viewModel.fetchEpisodeMediaUrl(fetchFromDb = false)
    }

}