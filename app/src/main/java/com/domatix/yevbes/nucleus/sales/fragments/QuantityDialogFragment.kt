package com.domatix.yevbes.nucleus.sales.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.adapters.AddProductProductDataAdapter


class QuantityDialogFragment : DialogFragment() {

    private var editText: EditText? = null
    private var textView: TextView? = null
    private var button: ImageButton? = null
    private var item: ProductProduct? = null
    private var adapter: AddProductProductDataAdapter? = null

    companion object {

        @JvmStatic
        fun newInstance(textView: TextView, button: ImageButton, item: ProductProduct, addProductProductDataAdapter: AddProductProductDataAdapter) =
                QuantityDialogFragment().apply {
                    this.textView = textView
                    this.button = button
                    this.item = item
                    this.adapter = addProductProductDataAdapter
                }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = activity?.let { AlertDialog.Builder(it) }
        val inflater = activity?.layoutInflater

        val view = inflater?.inflate(R.layout.quantity_dialog, null)
        editText = view?.findViewById(R.id.quantity)


        builder?.setView(view)

        builder?.setTitle(resources.getString(R.string.quantity_dialog_title))

        builder?.setPositiveButton(R.string.quantity_dialog_positive_button)
        { _, _ ->
            val quantity = editText?.text.toString().trim()
            if (quantity.isNotEmpty()) {

                if (item!!.checked) {
                    adapter?.selectedList?.remove(item!!)
                    item?.checked = true
                }

                item?.quantity = quantity.toFloat()
                button?.visibility = View.VISIBLE
                textView?.visibility = View.VISIBLE
                textView?.text = (item?.quantity).toString()
                adapter?.selectedList?.add(item!!)
                item?.checked = true
            }
        }

        builder?.setNegativeButton(R.string.quantity_dialog_negative_button) { _, _ ->
            dialog.cancel()
        }

        return builder!!.create()
    }
}