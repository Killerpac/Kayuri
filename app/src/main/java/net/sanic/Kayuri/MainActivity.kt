package net.sanic.Kayuri

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import net.sanic.Kayuri.utils.preference.PreferenceHelper


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toggleDayNight()
        setContentView(R.layout.main_activity)
        if (!PreferenceHelper.sharedPreference.getGoogleServer())
        {
            PreferenceHelper.sharedPreference.setGoogleServer(true)
        }
    }

//    fun toggleDayNight() {
//        when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
//            Configuration.UI_MODE_NIGHT_YES -> {
//                Timber.e("Night Mode")
//            }
//            Configuration.UI_MODE_NIGHT_NO -> {
//                if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
//                    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
//                    } else {
//                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                    }
//                    window.decorView.systemUiVisibility = flags
//                }
//                Timber.e("Day Mode")
//            }
//        }
//    }

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
