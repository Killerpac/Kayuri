package net.sanic.Kayuri.utils.exoplayer

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class ModifyGestureDetector(context: Context?, listener: OnGestureListener) :
    GestureDetector(context,listener) {
    var myGestureListener: MyGestureListener? = null
    fun init(listener: OnGestureListener?) {
        if (listener is MyGestureListener) {
            myGestureListener = listener
        }
    }

    //u can write something more complex as long as u need
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP
            && myGestureListener != null
        ) {
            myGestureListener!!.onUp(ev)
        }
        return super.onTouchEvent(ev)
    }

    interface MyGestureListener {
        fun onUp(ev: MotionEvent?)
    }

    init {
        init(listener)
    }
}