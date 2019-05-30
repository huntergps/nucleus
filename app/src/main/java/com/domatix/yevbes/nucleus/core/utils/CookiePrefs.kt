package com.domatix.yevbes.nucleus.core.utils

import android.content.Context
import com.domatix.yevbes.nucleus.getActiveOdooUser
import com.domatix.yevbes.nucleus.gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie

class CookiePrefs(context: Context) : Prefs(CookiePrefs.TAG, context) {

    companion object {
        const val TAG = "CookiePrefs"
    }

    private val type = object : TypeToken<ArrayList<ClonedCookie>>() {}.type

    fun getCookies(): ArrayList<Cookie> {
        val activeUser = context.getActiveOdooUser()
        if (activeUser != null) {
            val cookiesStr = getString(activeUser.androidName)
            if (cookiesStr.isNotEmpty()) {
                val clonedCookies: ArrayList<ClonedCookie> = gson.fromJson(cookiesStr, type)
                val cookies = arrayListOf<Cookie>()
                for (clonedCookie in clonedCookies) {
                    cookies += clonedCookie.toCookie()
                }
                return cookies
            }
        }
        return arrayListOf()
    }

    fun setCookies(cookies: ArrayList<Cookie>) {
        val clonedCookies = arrayListOf<ClonedCookie>()
        for (cookie in cookies) {
            clonedCookies += ClonedCookie.fromCookie(cookie)
        }
        val cookiesStr = gson.toJson(clonedCookies, type)
        val activeUser = context.getActiveOdooUser()
        if (activeUser != null) {
            putString(activeUser.androidName, cookiesStr)
        }
    }
}
