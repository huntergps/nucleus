package com.domatix.yevbes.nucleus

import android.app.Application
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.core.utils.CookiePrefs
import com.domatix.yevbes.nucleus.core.utils.LetterTileProvider
import com.domatix.yevbes.nucleus.core.utils.Retrofit2Helper
import com.domatix.yevbes.nucleus.utils.LocaleHelper
import com.domatix.yevbes.nucleus.utils.PreferencesManager
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

class App : Application() {

    companion object {
        private lateinit var prefs: SharedPreferences

        @JvmStatic
        fun getSharedPreferences() : SharedPreferences {
            return prefs
        }


        const val KEY_ACCOUNT_TYPE = "${BuildConfig.APPLICATION_ID}.auth"
    }

    private val letterTileProvider: LetterTileProvider by lazy {
        LetterTileProvider(this)
    }

    val cookiePrefs: CookiePrefs by lazy {
        CookiePrefs(this)
    }

    override fun onCreate() {
        super.onCreate()
        Odoo.app = this
        Retrofit2Helper.app = this
        JodaTimeAndroid.init(this)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val localeLanguage = PreferencesManager.getLocaleLanguage()
        when(localeLanguage){
            "en" -> {
                LocaleHelper.initialize(this, LocaleHelper.ENGLISH)
            }
            "es" -> {
                LocaleHelper.initialize(this, LocaleHelper.SPANISH)
            }
        }

    }

    fun getLetterTile(displayName: String): ByteArray =
            letterTileProvider.getLetterTile(displayName)
}