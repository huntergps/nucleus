package com.domatix.yevbes.nucleus.utils

import android.app.ProgressDialog

class MyProgressDialog(private var myProgressDialog: ProgressDialog, val title: String, val message: String) {

    init {
        with(myProgressDialog) {
            setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL)
            setTitle(title)
            setMessage(message)
            setCancelable(false)
            isIndeterminate = false
            max = 100
            progress = 0
            show()
        }
    }

    fun setProgressToProgressDialog(progress: Int) {
        myProgressDialog.progress = progress
    }

    fun getProgressDialog(): Int {
        return myProgressDialog.progress
    }

    fun dismissProgressDialog() {
        myProgressDialog.dismiss()
    }
}