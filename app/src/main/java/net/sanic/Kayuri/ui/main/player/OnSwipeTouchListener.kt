package net.sanic.Kayuri.ui.main.player

import android.content.Context
import android.graphics.Insets
import android.media.AudioManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import kotlinx.android.synthetic.main.fragment_video_player.*
import kotlin.math.abs


open class OnSwipeTouchListener(c: Context?) : View.OnTouchListener {
    private var context: Context? = null
    init {
        context = c
        /** you can do multi if else statements for multi activity classes  */
    }

    private val gestureDetector = GestureDetector(GestureListener())

    fun onTouch(event: MotionEvent): Boolean {

        return gestureDetector.onTouchEvent(event)
    }

    private  inner class GestureListener : SimpleOnGestureListener(),ModifyGestureDetector.MyGestureListener {


        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        fun getdisplayheight():Int
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                val windowMetrics: WindowMetrics =
                    (context as VideoPlayerActivity).windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                return windowMetrics.bounds.height() - insets.left - insets.right

            }
            else
            {
                val metrics = DisplayMetrics()
                (context as VideoPlayerActivity).windowManager
                    .defaultDisplay
                    .getMetrics(metrics)
                return metrics.heightPixels
            }
        }

        fun getdisplaywidth():Int
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                val windowMetrics: WindowMetrics =
                    (context as VideoPlayerActivity).windowManager.currentWindowMetrics
                val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
                return windowMetrics.bounds.width() - insets.left - insets.right

            }
            else
            {
                val metrics = DisplayMetrics()
                (context as VideoPlayerActivity).windowManager
                    .defaultDisplay
                    .getMetrics(metrics)
                return metrics.widthPixels
            }
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            //   gestureDetector.setIsLongpressEnabled(true)
            //  onTouch(e)
            if((context as VideoPlayerActivity).exoPlayerView.isControllerVisible) (context as VideoPlayerActivity).exoPlayerView.hideController()
            else ((context as VideoPlayerActivity).exoPlayerView.showController())
            return true}

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            return super.onDoubleTap(e)
        }


        override fun onUp(ev: MotionEvent?) {
            (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
            (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
            (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if((context as VideoPlayerActivity).exoPlayerView.isControllerVisible) (context as VideoPlayerActivity).exoPlayerView.hideController()
            val height = getdisplayheight()
            val width = getdisplaywidth()
            val mOldX = e1.x
            val mOldY = e1.y
            var mode = -1
            val y = e2.rawY.toInt()
            if (abs(distanceX) >= abs(distanceY)) {
                //      (context as VideoPlayerActivity).gesture_progress_layout.setVisibility(View.VISIBLE)
                (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                mode = 0
            } else {
                if (mOldX > width * 3.0 / 5) { //Volume
                    (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.VISIBLE
                    (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
                    (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE
                    mode = 1
                } else if (mOldX < width * 2.0 / 5) { // Brightness
                    (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.VISIBLE
                    (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
                    (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE
                    mode = 2
                }
            }
            if (mode == 0) {
            }

            // If the first scroll is to adjust the volume after each touch of the screen, then the subsequent scroll events will handle the volume adjustment until you leave the screen to perform the next operation
            else if (mode == 1) {

                val audiomanager = (context as VideoPlayerActivity).getSystemService(Context.AUDIO_SERVICE)  as AudioManager

                val maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) // Get the maximum volume of the system
                var currentVolume: Int =
                    audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC) // Get the current value // Get the current value
                if (abs(distanceY)> abs(distanceX)) {// Vertical movement is greater than horizontal movement
                    if (distanceY >= DensityUtil.dip2px((context as VideoPlayerActivity), 2.0f)) {// Turn up the volume, pay attention to the coordinate system when the screen is horizontal, although the upper left corner is the origin, distanceY is positive when sliding up horizontally
                        if (currentVolume <maxVolume) {// To avoid too fast adjustment, distanceY should be greater than a set value
                            currentVolume++

                        }
                        //   gesture_iv_player_volume.setImageResource(R.drawable.player_volume);
                    } else if (distanceY <= -DensityUtil.dip2px((context as VideoPlayerActivity), 2.0f)) {// Turn down the volume
                        if (currentVolume > 0) {
                            currentVolume--
//                                                if (currentVolume == 0) {// Mute, set mute unique picture
//                                                        gesture_iv_player_volume.setImageResource(R.drawable.player_silence);
//                                                }
                        }
                    }
                    val percentage = (currentVolume * 100) / maxVolume
                    (context as VideoPlayerActivity).gesture_tv_volume_percentage.text = percentage.toString() + "%"
                    audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
                }
            }

            // If the first scroll is to adjust the brightness every time you touch the screen, then the subsequent scroll events will handle the brightness adjustment until you leave the screen to perform the next operation
            else if (mode == 2) {
//                        gesture_iv_player_bright.setImageResource(R.drawable.player_bright);

                var mBrightness = (context as VideoPlayerActivity).window.attributes.screenBrightness
                if (mBrightness <= 0.00f)
                    mBrightness = 0.50f
                if (mBrightness < 0.01f)
                    mBrightness = 0.01f

                val lpa = (context as VideoPlayerActivity).window.attributes
                lpa.screenBrightness = mBrightness + (mOldY - y) / height * 0.05f
                if (lpa.screenBrightness > 1.0f)
                    lpa.screenBrightness = 1.0f
                else if (lpa.screenBrightness < 0.01f)
                    lpa.screenBrightness = 0.01f
                (context as VideoPlayerActivity).window.attributes = lpa
                (context as VideoPlayerActivity).gesture_tv_bright_percentage.text =
                    (lpa.screenBrightness * 100).toInt().toString() + "%"
            }

            //firstScroll = false;// The first scroll execution is complete, modify the flag
            return true

        }

//        try {
//        val diffY = e2.y - e1.y
//        val diffX = e2.x - e1.x
//        if (Math.abs(diffX) > Math.abs(diffY)) {
//        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
//        if (diffX > 0) {
//        onSwipeRight()
//        } else {
//        onSwipeLeft()
//        }
//        }
//        } else {
//        // onTouch(e);
//        }
//        } catch (exception: Exception) {
//        exception.printStackTrace()
//        }

        //    return result
    }


    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_UP){
            (context as VideoPlayerActivity).gesture_volume_layout.visibility = View.GONE
            (context as VideoPlayerActivity).gesture_bright_layout.visibility = View.GONE
            (context as VideoPlayerActivity).gesture_progress_layout.visibility = View.GONE
        }
        return gestureDetector.onTouchEvent(event)
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}


}
internal object DensityUtil {
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 1f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 1f).toInt()
    }
}

class MyGestureListener : SimpleOnGestureListener(), ModifyGestureDetector.MyGestureListener {
    override fun onUp(ev: MotionEvent) {
        //do what u want
    }
}
