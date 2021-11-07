package net.sanic.Kayuri

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import net.sanic.Kayuri.utils.preference.PreferenceHelper


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toggleDayNight()
        setContentView(R.layout.main_activity)
    }


    fun toggleDayNight() {
            if (PreferenceHelper.sharedPreference.getNightMode()) {
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
