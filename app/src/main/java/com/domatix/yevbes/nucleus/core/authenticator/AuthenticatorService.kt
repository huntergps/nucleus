package com.domatix.yevbes.nucleus.core.authenticator

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticatorService : Service() {

    override fun onBind(intent: Intent): IBinder = AccountAuthenticator(this).iBinder
}
