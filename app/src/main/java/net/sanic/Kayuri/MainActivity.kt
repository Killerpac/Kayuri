package net.sanic.Kayuri

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import net.sanic.Kayuri.utils.preference.PreferenceHelper


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setupTransitions()
        super.onCreate(savedInstanceState)
        toggleDayNight()
        setContentView(R.layout.main_activity)
    }

    private fun setupTransitions(){
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        // Attach a callback used to capture the shared elements from this Activity to be used
        // by the container transform transition
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        // Keep system bars (status bar, navigation bar) persistent throughout the transition.
        window.sharedElementsUseOverlay = true
    }


    fun toggleDayNight() {
            if (PreferenceHelper.sharedPreference.getNightMode() ) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//                } else {
//                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                }
//                window.decorView.systemUiVisibility = flags
//            }
            }
    }

}
