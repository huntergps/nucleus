package com.domatix.yevbes.nucleus.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkStatusChecker {

    companion object {

        /**
         * Function that check internet connection
         *
         * @param context Context
         * @return Internet state connection
         */
        fun isInternetConnected(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo

            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
}