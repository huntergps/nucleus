package com.domatix.yevbes.nucleus.general.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo

class LoadingDialogFragment : DialogFragment(){
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val app = Odoo.app
        val builder = AlertDialog.Builder(app)
        val inflater = activity?.layoutInflater

        builder.setView(inflater?.inflate(R.layout.dialog_loading,null))
        val view = builder.create()
        val tvTitle = view.findViewById<TextView>(R.id.dialog_loading_title)
        val tvMessage = view.findViewById<TextView>(R.id.dialog_loading_message)

        return view
    }
}