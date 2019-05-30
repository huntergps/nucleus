package com.domatix.yevbes.nucleus.utils

import android.content.Context
import android.support.annotation.StringDef
import java.util.*


class LocaleHelper {

    companion object {
        const val ENGLISH = "en"
        const val SPANISH = "es"

        @Retention(AnnotationRetention.SOURCE)
        @StringDef(ENGLISH, SPANISH)
        annotation class LocaleDef {
            companion object {
                val SUPPORTED_LOCALES = arrayOf(ENGLISH, SPANISH)
            }
        }

        fun initialize(context: Context) {
            //        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
            setLocale(context, ENGLISH)
        }

        fun initialize(context: Context, @LocaleDef defaultLanguage: String) {
            //        String lang = getPersistedData(context, defaultLanguage);
            setLocale(context, defaultLanguage)
        }

//    public static String getLanguage(Context context) {
//        return getPersistedData(context, Locale.getDefault().getLanguage());
//    }

        fun setLocale(context: Context, @LocaleDef language: String): Boolean {
            //        persist(context, language);
            return updateResources(context, language)
        }

        fun setLocale(context: Context, languageIndex: Int): Boolean {
            //        persist(context, language);
            return if (languageIndex >= LocaleDef.SUPPORTED_LOCALES.size) {
                false
            } else updateResources(context, LocaleDef.SUPPORTED_LOCALES[languageIndex])

        }

//    private static String getPersistedData(Context context, String defaultLanguage) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
//    }

//    private static void persist(Context context, String language) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = preferences.edit();
//
//        editor.putString(SELECTED_LANGUAGE, language);
//        editor.apply();
//    }

        private fun updateResources(context: Context, language: String): Boolean {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val resources = context.resources

            val configuration = resources.configuration
            configuration.locale = locale

            resources.updateConfiguration(configuration, resources.displayMetrics)

            return true
        }
    }
}