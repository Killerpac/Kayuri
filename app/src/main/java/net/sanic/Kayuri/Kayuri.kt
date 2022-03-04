package net.sanic.Kayuri

import android.app.Application
import net.sanic.Kayuri.utils.constants.C
import net.sanic.Kayuri.utils.preference.PreferenceHelper
import net.sanic.Kayuri.utils.realm.InitalizeRealm
import net.sanic.Kayuri.utils.rertofit.RetrofitHelper
import org.jsoup.Jsoup
import timber.log.Timber
import java.lang.Exception

class Kayuri : Application() {

    override fun onCreate() {

        super.onCreate()
        InitalizeRealm.initializeRealm(this)
        PreferenceHelper(context = this)
        RetrofitHelper(PreferenceHelper.sharedPreference.getBaseUrl())
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}