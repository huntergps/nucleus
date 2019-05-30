package com.domatix.yevbes.nucleus.utils

import com.domatix.yevbes.nucleus.App

object PreferencesManager {
    private val mSharedPreferences = App.getSharedPreferences()

    fun isContactProfileEdited(isEdited: Boolean) {
        mSharedPreferences.edit()
                .putBoolean(ConstantManager.IS_EDITED, isEdited)
                .apply()
    }


    fun clearPreferences() {
        mSharedPreferences.edit()
                .clear()
                .apply()
    }

    fun saveTimeTrackingValues(startTime: String, endTime: String) {
        mSharedPreferences.edit()
                .putString(ConstantManager.START_TIME, startTime)
                .putString(ConstantManager.END_TIME, endTime)
                .apply()
    }

    fun saveLocaleLanguage(language: String){
        mSharedPreferences.edit()
                .putString(ConstantManager.LOCALE_LANGUAGE, language)
                .apply()
    }

    fun getLocaleLanguage(): String {
        return mSharedPreferences.getString(ConstantManager.LOCALE_LANGUAGE, "en")
    }

    fun getStartTimeTrackingValue(): String {
        return mSharedPreferences.getString(ConstantManager.START_TIME, "null")
    }

    fun getEndTimeTrackingValue(): String {
        return mSharedPreferences.getString(ConstantManager.END_TIME, "null")
    }


}