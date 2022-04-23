package net.sanic.Kayuri.utils.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.core.location.LocationRequestCompat
import net.sanic.Kayuri.utils.constants.C

class Preference(private val context: Context) {
    private val PREF_NAME = "Kayuri"
    private val BASE_URL = "BASE_URL"
    private val QUALITY = "AUTO"
    private val ORIGIN = "ORIGIN"
    private val REFERER = "REFERER"
    private val NIGHT_MODE = "NIGHT_MODE"
    private val PIP = "PIP"
    private val PRIVATE_MODE = 0
    private val GOOGLESERVER = "GOOGLESERVER"
    private val ADVANCECONTROLS = "ADVANCECONTROLS"
    private val DNS = "DNS"
    private val SBSTREAM = "SBSTREAM"
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)

    fun setNightMode(nightMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(NIGHT_MODE, nightMode)
        editor.apply()
    }

    fun getNightMode(): Boolean {
        return sharedPreferences.getBoolean(NIGHT_MODE, false)
    }

    fun setPIPMode(isPip: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(PIP, isPip)
        editor.apply()
    }

    fun getPIPMode(): Boolean {
        return sharedPreferences.getBoolean(PIP, true)
    }

    fun getGoogleServer():Boolean {
        return  sharedPreferences.getBoolean(GOOGLESERVER, false)
    }

    fun getdns():Boolean {
        return  sharedPreferences.getBoolean(DNS,false)
    }

    fun setdns(dns:Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(DNS, dns)
        editor.apply()
    }

    fun setSB(sb:Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(SBSTREAM, sb)
        editor.apply()
    }

    fun getSB():Boolean{
        return  sharedPreferences.getBoolean(SBSTREAM,false)
    }


    fun setGoogleServer(gogo: Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(GOOGLESERVER, gogo)
        editor.apply()
    }

    fun getadvancecontrols():Boolean {
        return  sharedPreferences.getBoolean(ADVANCECONTROLS,false)
    }

    fun getpreferredquality():String {
        return sharedPreferences.getString(QUALITY, C.QUALITY) ?: C.QUALITY
    }

    fun setpreferredquality(quality:String) {
        val editor = sharedPreferences.edit()
        editor.putString(QUALITY, quality)
        editor.apply()
    }

    fun setadvancecontrols(controls:Boolean){
        val editor = sharedPreferences.edit()
        editor.putBoolean(ADVANCECONTROLS,controls)
        editor.apply()
    }

    fun getBaseUrl(): String {
        return sharedPreferences.getString(BASE_URL, C.BASE_URL) ?: C.BASE_URL
    }

    fun setBaseUrl(baseUrl: String) {
        val editor = sharedPreferences.edit()
        editor.putString(BASE_URL, baseUrl)
        editor.apply()
    }


    fun getOrigin(): String {
        return sharedPreferences.getString(ORIGIN, C.ORIGIN) ?: C.ORIGIN
    }

    fun setOrigin(origin: String) {
        val editor = sharedPreferences.edit()
        editor.putString(ORIGIN, origin)
        editor.apply()
    }

    fun getReferrer(): String {
        return sharedPreferences.getString(REFERER, C.REFERER) ?: C.REFERER
    }

    fun setReferrer(ref: String) {
        val editor = sharedPreferences.edit()
        editor.putString(REFERER, ref)
        editor.apply()
    }

}