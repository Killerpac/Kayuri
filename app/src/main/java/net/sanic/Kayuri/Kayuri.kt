package net.sanic.Kayuri

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import net.sanic.Kayuri.utils.realm.InitalizeRealm
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import timber.log.Timber

class Kayuri : Application() {

    override fun onCreate() {

        super.onCreate()
        InitalizeRealm.initializeRealm(this)
        PreferenceHelper(context = this)
        RetrofitHelper(PreferenceHelper.sharedPreference.getBaseUrl())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) DynamicColors.applyToActivitiesIfAvailable(this)
    }

}