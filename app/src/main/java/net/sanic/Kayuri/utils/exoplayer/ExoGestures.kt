package net.sanic.Kayuri.utils.exoplayer

import android.content.Context
import android.graphics.Insets
import android.media.AudioManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import kotlinx.android.synthetic.main.exo_advance_controls.*
import kotlinx.android.synthetic.main.fragment_video_player.*
import net.sanic.Kayuri.ui.main.player.VideoPlayerActivity
import kotlin.math.abs


open class OnSwipeTouchListener(c: Context?) : View.OnTouchListener {
    private var context: Context? = null
    init {
        context = c
        /** you can do multi if else statements for multi activity classes  */
    }

    private val gestureDetector = GestureDetector(GestureListener())

    private  inner class GestureListener : SimpleOnGestureListener(),
        ModifyGestureDetector.MyGestureListener {


        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        fun getdisplayheight(): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics =
                    (context as VideoPlayerActivity).windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                return windowMetrics.bounds.height() - insets.left - insets.right

            } else {
                val metrics = DisplayMetrics()
                (context as VideoPlayerActivity).windowManager
                    .defaultDisplay
                    .getMetrics(metrics)
                return metrics.heightPixels
            }
        }

        fun getdisplaywidth(): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics: WindowMetrics =
                    (context as VideoPlayerActivity).windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                return windowMetrics.bounds.width() - insets.left - insets.right

            } else {
                val metrics = DisplayMetrics()
                (context as VideoPlayerActivity).windowManager
                    .defaultDisplay
                    .getMetrics(metrics)
                return metrics.widthPixels
            }
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            showhidecontrols()
            return super.onSingleTapUp(e)
        }

        override fun onUp(ev: MotionEvent?) {
            (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
            (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if ((context as VideoPlayerActivity).exoPlayerView.isControllerVisible) (context as VideoPlayerActivity).exoPlayerView.hideController()
            val height = getdisplayheight()
            val width = getdisplaywidth()
            val mOldX = e1.x
            val mOldY = e1.y
            var mode = -1
            val y = e2.rawY.toInt()
            if (abs(distanceX) >= abs(distanceY)) {
                (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                mode = 0
            } else {
                if (mOldX > width * 3.0 / 5) { //Volume
                    (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.VISIBLE
                    (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                    mode = 1
                } else if (mOldX < width * 2.0 / 5) { // Brightness
                    (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.VISIBLE
                    (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                    mode = 2
                }
            }
            // If the first scroll is to adjust the volume after each touch of the screen, then the subsequent scroll events will handle the volume adjustment until you leave the screen to perform the next operation
            if (mode == 1) {
                val audiomanager =
                    (context as VideoPlayerActivity).getSystemService(Context.AUDIO_SERVICE) as AudioManager
                var currentVolume: Int =
                    audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) // Get the current value
                    if (distanceY >= DensityUtil.dip2px((context as VideoPlayerActivity), 2f)) {// Turn up the volume, pay attention to the coordinate system when the screen is horizontal, although the upper left corner is the origin, distanceY is positive when sliding up horizontally
                        currentVolume++
                        //   gesture_iv_player_volume.setImageResource(R.drawable.player_volume);
                    } else if (distanceY <= -DensityUtil.dip2px(
                            (context as VideoPlayerActivity),
                            2f
                        )
                    ) {// Turn down the volume
                        currentVolume--
                    }
                    audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                    adjustvolume()
            }
            // If the first scroll is to adjust the brightness every time you touch the screen, then the subsequent scroll events will handle the brightness adjustment until you leave the screen to perform the next operation
            else if (mode == 2) {
                var mBrightness = (context as VideoPlayerActivity).window.attributes.screenBrightness
                if (mBrightness <= 0.00f) mBrightness = 0.50f
                if (mBrightness < 0.01f) mBrightness = 0.01f
                val lpa = (context as VideoPlayerActivity).window.attributes
                if (distanceY >= DensityUtil.dip2px((context as VideoPlayerActivity), 2f)) {// Turn up the Brightness,
                    lpa.screenBrightness= mBrightness + 0.045f
                } else if (distanceY <= -DensityUtil.dip2px((context as VideoPlayerActivity), 2f)
                ) {// Turn down the Brightness
                    lpa.screenBrightness= mBrightness - 0.045f
                }
                if (lpa.screenBrightness > 1.0f)
                    lpa.screenBrightness = 1.0f
                else if (lpa.screenBrightness < 0.01f)
                    lpa.screenBrightness = 0.01f
                (context as VideoPlayerActivity).window.attributes = lpa
                adjustbrightness((lpa.screenBrightness * 100).toInt())
            }

                //firstScroll = false;// The first scroll execution is complete, modify the flag
                return true

            }
            //    return result
        }


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_UP){
            (context as VideoPlayerActivity).gesture_volume_layout.visibility  = View.GONE
            (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
        }
        return gestureDetector.onTouchEvent(event)
    }

    private fun showhidecontrols(){
        if ((context as VideoPlayerActivity).exoPlayerView.isControllerVisible) (context as VideoPlayerActivity).exoPlayerView.hideController()
        else ((context as VideoPlayerActivity).exoPlayerView.showController())
    }

    private fun adjustvolume(){
        val seekBar = (context as VideoPlayerActivity).volumeseekbar
        val audiomanager = (context as VideoPlayerActivity).getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) // Get the maximum volume of the system
        val currentVolume: Int = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) // Get the current value // Get the current value
        seekBar.maxValue = maxVolume
        seekBar.progress = currentVolume
    }
    private fun adjustbrightness(lpa:Int){
        val seekBar = (context as VideoPlayerActivity).brightnessseekbar
        seekBar.maxValue = 100
        seekBar.progress = lpa
    }

    fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val CLICK_ACTION_THRESHOLD = 300
        val differenceX = abs(startX - endX)
        val differenceY = abs(startY - endY)
        return !(differenceX > CLICK_ACTION_THRESHOLD /* =5 */ || differenceY > CLICK_ACTION_THRESHOLD)
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}


}
internal object DensityUtil {
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 1f).toInt()
    }

}