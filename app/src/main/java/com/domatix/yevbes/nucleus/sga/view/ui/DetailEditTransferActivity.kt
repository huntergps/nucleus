package com.domatix.yevbes.nucleus.sga.view.ui

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityDetailEditTransferBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sga.service.model.StockMove
import com.domatix.yevbes.nucleus.sga.view.adapter.DetailEditTransferAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnEditItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_detail_edit_transfer.*
import kotlinx.android.synthetic.main.alert_dialog_immediate_transfer.view.*
import timber.log.Timber

class DetailEditTransferActivity : AppCompatActivity() {
    private val moveLineListType = object : TypeToken<ArrayList<StockMove>>() {}.type
    private lateinit var binding: ActivityDetailEditTransferBinding
    lateinit var compositeDisposable: CompositeDisposable private set
    private val list: ArrayList<Int> = ArrayList()
    private val stockMoves = ArrayList<StockMove>()
    private var state: String? = null
    private var stockPickingId = 0
    private var isValidate: Boolean = false
    private var isShowLotsText: Boolean = false

    companion object {
        const val SELECTED_MOVE_LINES = "SELECTED"
        const val REQUEST_CODE = 1
        const val STOCK_PICKING_STATE = "STOCK_PICKING_STATE"
        const val STOCK_PICKING_ID = "STOCK_PICKING_ID"
        const val STOCK_PICKING_LOT_TEXT_BOOLEAN = "STOCK_PICKING_LOT_TEXT_BOOLEAN"
    }

    val mAdapter: DetailEditTransferAdapter by lazy {
        DetailEditTransferAdapter(binding, arrayListOf(), object : OnEditItemClickListener {
            override fun onItemClick(item: Any) {
                val moveStockGson = gson.toJson(item as StockMove)

                val intent = Intent(this@DetailEditTransferActivity,
                        EditProductActivity::class.java)

                intent.putExtra(EditProductActivity.MOVE_LINE, moveStockGson)
                intent.putExtra(EditProductActivity.STOCK_PICKING_STATE, state)
                intent.putExtra(EditProductActivity.STOCK_PICKING_LOT_TEXT_BOOLEAN, isShowLotsText)
                startActivityForResult(intent, REQUEST_CODE)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        mAdapter.clear()
                        fetchStockMoves(list)
                    }
                    Activity.RESULT_CANCELED -> {

                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAdapter.clear()
        stockMoves.clear()
        fetchStockMoves(list)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_edit_transfer)
        compositeDisposable = CompositeDisposable()
        val moveLines = intent.extras.getString(SELECTED_MOVE_LINES)
        val aux = moveLines.substring(1, moveLines.length - 1)
        val delimiter = ','

        state = intent.extras.getString(STOCK_PICKING_STATE)
        binding.state = state
        stockPickingId = intent.extras.getInt(STOCK_PICKING_ID)
        isShowLotsText = intent.extras.getBoolean(STOCK_PICKING_LOT_TEXT_BOOLEAN)

        val lines = aux.split(delimiter)

        for (i in lines) {
            if (!i.isBlank())
                list.add(i.toInt())
        }

        setSupportActionBar(tb)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        tb.setNavigationOnClickListener {
            super.onBackPressed()
        }

        button_add_product.setOnClickListener {
            addProduct()
        }

        if (intent.extras.getString(STOCK_PICKING_STATE).isNotBlank()
                && intent.extras.getString(STOCK_PICKING_STATE) == "done") {
            button_add_product.visibility = View.GONE
            button_add_product.setOnClickListener(null)
        }

        val mLayoutManager = LinearLayoutManager(applicationContext)

        rv.layoutManager = mLayoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = mAdapter

//        fetchStockMoves(list)
    }

    private fun alertImmediateTransfer(res_id: Int) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_immediate_transfer, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.immediate_transfer_dialog_title)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        dialogView.alertMessage.text = getString(R.string.alert_message_immediate_transfer)

        dialogView.bCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogView.b_ok.setOnClickListener {
            isValidate = false
            Odoo.callKw(model = "stock.immediate.transfer", method = "process", args = listOf(res_id)) {
                onSubscribe {
                    compositeDisposable.add(it)
                }

                onNext { response ->
                    if (response.isSuccessful) {
                        val nameGet = response.body()!!
                        if (nameGet.isSuccessful) {
                            val result = nameGet.result
                            Timber.tag("VALIDATE_ON_NEXT").e(result.toString())
                            if (result.isJsonObject) {
                                val element = result.asJsonObject.get("res_model")
                                val res_id = result.asJsonObject.get("res_id")
                                if (element != null) {
                                    isValidate = false
                                    when (element.asString) {
                                        "stock.overprocessed.transfer" -> {
                                            if (res_id != null)
                                                alertOverProcessedTransfer(res_id.asInt)
                                        }
                                        "stock.backorder.confirmation" -> {
                                            if (res_id != null)
                                                alertPartialDelivery(res_id.asInt)
                                        }
                                    }
                                }
                            } else {
                                isValidate = true
                            }

                        } else {
                            // Odoo specific error
                            isValidate = false
                            alertMessage(nameGet.errorMessage)
                            Timber.tag("VALIDATE_ON_NEXT").e("nameGet() failed with ${nameGet.errorMessage}")
//                                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
                        }
                    } else {
                        isValidate = false
                        alertMessage(response.message())
                        Timber.tag("VALIDATE_ON_NEXT").e("request failed with ${response.code()}:${response.message()}")
//                            Timber.w("request failed with ${response.code()}:${response.message()}")
                    }
                }
                onError {
                    it.printStackTrace()
                    alertMessage(it.message!!)
                    Timber.tag("VALIDATE_ON_ERROR").e(it)
                    isValidate = false
//                        Toast.makeText(this@DetailEditTransferActivity,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show()
                }
                onComplete {
                    if (isValidate) {
                        state = "done"
                        Toast.makeText(this@DetailEditTransferActivity, getString(R.string.validation_completed), Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    private fun alertPartialDelivery(res_id: Int) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_partial_delivery, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.partial_delivery_dialog_title)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        dialogView.findViewById<TextView>(R.id.alertMessage).text =
                getString(R.string.alert_message_one_partial_delivery)

        dialogView.findViewById<TextView>(R.id.alertMessageTwo).text =
                getString(R.string.alert_message_two_partial_delivery)

        dialogView.findViewById<Button>(R.id.bCancel).setOnClickListener {
            dialog.cancel()
        }

        dialogView.findViewById<Button>(R.id.bt_no_create_partial_delivery).setOnClickListener {
            Odoo.callKw(model = "stock.backorder.confirmation", method = "process_cancel_backorder", args = listOf(res_id)) {
                onSubscribe {
                    compositeDisposable.add(it)
                }

                onNext { response ->
                    if (response.isSuccessful) {
                        val nameGet = response.body()!!
                        if (nameGet.isSuccessful) {
                            val result = nameGet.result
                            Timber.tag("VALIDATE_ON_NEXT").e(result.toString())
                            if (result.isJsonObject) {
                                val element = result.asJsonObject.get("res_model")
                                val res_id = result.asJsonObject.get("res_id")
                                if (element != null) {
                                    isValidate = false
                                    when (element.asString) {
                                        "stock.immediate.transfer" -> {
                                            if (res_id != null)
                                                alertImmediateTransfer(res_id.asInt)
                                        }
                                        "stock.overprocessed.transfer" -> {
                                            if (res_id != null)
                                                alertOverProcessedTransfer(res_id.asInt)
                                        }
                                    }
                                } else {
                                    isValidate = true
                                }
                            } else {
                                isValidate = true
                            }

                        } else {
                            // Odoo specific error
                            isValidate = false
                            alertMessage(nameGet.errorMessage)
                            Timber.tag("VALIDATE_ON_NEXT").e("nameGet() failed with ${nameGet.errorMessage}")
//                                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
                        }
                    } else {
                        alertMessage(response.message())
                        isValidate = false
                        Timber.tag("VALIDATE_ON_NEXT").e("request failed with ${response.code()}:${response.message()}")
//                            Timber.w("request failed with ${response.code()}:${response.message()}")
                    }
                }
                onError {
                    isValidate = false
                    alertMessage(it.message!!)
                    it.printStackTrace()
                    Timber.tag("VALIDATE_ON_ERROR").e(it)
//                        Toast.makeText(this@DetailEditTransferActivity,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show()
                }
                onComplete {
                    if (isValidate) {
                        state = "done"
                        Toast.makeText(this@DetailEditTransferActivity, getString(R.string.validation_completed), Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    dialog.dismiss()
                }
            }
        }

        dialogView.findViewById<Button>(R.id.bt_create_partial_delivery).setOnClickListener {
            Odoo.callKw(model = "stock.backorder.confirmation", method = "process", args = listOf(res_id)) {
                onSubscribe {
                    compositeDisposable.add(it)
                }

                onNext { response ->
                    if (response.isSuccessful) {
                        val nameGet = response.body()!!
                        if (nameGet.isSuccessful) {
                            val result = nameGet.result
                            Timber.tag("VALIDATE_ON_NEXT").e(result.toString())
                            if (result.isJsonObject) {
                                val element = result.asJsonObject.get("res_model")
                                val res_id = result.asJsonObject.get("res_id")
                                if (element != null) {
                                    isValidate = false
                                    when (element.asString) {
                                        "stock.immediate.transfer" -> {
                                            if (res_id != null)
                                                alertImmediateTransfer(res_id.asInt)
                                        }
                                        "stock.overprocessed.transfer" -> {
                                            if (res_id != null)
                                                alertOverProcessedTransfer(res_id.asInt)
                                        }
                                    }
                                } else {
                                    isValidate = true
                                }
                            } else {
                                isValidate = true
                            }

                        } else {
                            // Odoo specific error
                            isValidate = false
                            Timber.tag("VALIDATE_ON_NEXT").e("nameGet() failed with ${nameGet.errorMessage}")
//                                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
                        }
                    } else {
                        isValidate = false
                        Timber.tag("VALIDATE_ON_NEXT").e("request failed with ${response.code()}:${response.message()}")
//                            Timber.w("request failed with ${response.code()}:${response.message()}")
                    }
                }
                onError {
                    isValidate = false
                    it.printStackTrace()
                    Timber.tag("VALIDATE_ON_ERROR").e(it)
//                        Toast.makeText(this@DetailEditTransferActivity,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show()
                }
                onComplete {
                    if (isValidate) {
                        state = "done"
                        Toast.makeText(this@DetailEditTransferActivity, getString(R.string.validation_completed), Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    private fun alertMessage(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_no_reserved_qty, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.attention)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        dialogView.findViewById<TextView>(R.id.alertMessage).text = message
//                getString(R.string.alert_message_no_reserved_done_qty)

        dialogView.findViewById<Button>(R.id.b_ok).setOnClickListener {
            dialog.cancel()
        }
    }

    private fun alertOverProcessedTransfer(res_id: Int) {
        val dialogView = layoutInflater.inflate(R.layout.alert_dialog_immediate_transfer, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.odoo)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        dialogView.alertMessage.text = getString(R.string.alert_message_over_processed_transfer)

        dialogView.bCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogView.b_ok.setOnClickListener {
            isValidate = false
            Odoo.callKw(model = "stock.overprocessed.transfer", method = "action_confirm", args = listOf(res_id)) {
                onSubscribe {
                    compositeDisposable.add(it)
                }

                onNext { response ->
                    if (response.isSuccessful) {
                        val nameGet = response.body()!!
                        if (nameGet.isSuccessful) {
                            val result = nameGet.result
                            val res_id = result.asJsonObject.get("res_id")
                            Timber.tag("VALIDATE_ON_NEXT").e(result.toString())
                            if (result.isJsonObject) {
                                val element = result.asJsonObject.get("res_model")
                                if (element != null) {
                                    isValidate = false
                                    when (element.asString) {
                                        "stock.immediate.transfer" -> {
                                            if (res_id != null)
                                                alertImmediateTransfer(res_id.asInt)
                                        }
                                        "stock.backorder.confirmation" -> {
                                            if (res_id != null)
                                                alertPartialDelivery(res_id.asInt)
                                        }
                                    }
                                }
                            } else {
                                isValidate = true
                            }

                        } else {
                            // Odoo specific error
                            isValidate = false
                            alertMessage(nameGet.errorMessage)
                            Timber.tag("VALIDATE_ON_NEXT").e("nameGet() failed with ${nameGet.errorMessage}")

//                                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
                        }
                    } else {
                        isValidate = false
                        Timber.tag("VALIDATE_ON_NEXT").e("request failed with ${response.code()}:${response.message()}")
                        alertMessage(response.message())
//                            Timber.w("request failed with ${response.code()}:${response.message()}")
                    }
                }
                onError {
                    it.printStackTrace()
                    alertMessage(it.message!!)
                    Timber.tag("VALIDATE_ON_ERROR").e(it)
                    isValidate = false
//                        Toast.makeText(this@DetailEditTransferActivity,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show()
                }
                onComplete {
                    if (isValidate) {
                        state = "done"
                        Toast.makeText(this@DetailEditTransferActivity, getString(R.string.validation_completed), Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
                    dialog.dismiss()
                }
            }
        }
    }

    private fun addProduct() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.add_product)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        /*  dialogView.edProduct.setOnClickListener {
              *//*val intent = Intent(this, GeneralAdapterActivity::class.java)
            this.startActivityForResult(intent, PRODUCT_REQUEST_CODE)*//*
        }

        dialogView.bCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogView.bConfirm.setOnClickListener {
            if (dialogView.edDone.text.isNotEmpty() &&
                    dialogView.edProduct.text.isNotEmpty() &&
                    dialogView.edInitialDemand.text.isNotEmpty()) {
            }
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_transfer_edit_detail, menu)
        if (state == "done") menu?.findItem(R.id.action_validate)?.isVisible = false
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_validate -> {
                isValidate = false
                Odoo.callKw(model = "stock.picking", method = "button_validate", args = listOf(stockPickingId)) {
                    onSubscribe {
                        compositeDisposable.add(it)
                    }

                    onNext { response ->
                        if (response.isSuccessful) {
                            val nameGet = response.body()!!
                            if (nameGet.isSuccessful) {
                                val result = nameGet.result
                                Timber.tag("VALIDATE_ON_NEXT").e(result.toString())
                                if (result.isJsonObject) {
                                    val element = result.asJsonObject.get("res_model")
                                    val res_id = result.asJsonObject.get("res_id")
                                    if (element != null) {
                                        isValidate = false
                                        when (element.asString) {
                                            "stock.immediate.transfer" -> {
                                                if (res_id != null)
                                                    alertImmediateTransfer(res_id.asInt)
                                            }
                                            "stock.overprocessed.transfer" -> {
                                                if (res_id != null)
                                                    alertOverProcessedTransfer(res_id.asInt)
                                            }
                                            "stock.backorder.confirmation" -> {
                                                if (res_id != null)
                                                    alertPartialDelivery(res_id.asInt)
                                            }
                                        }
                                    }
                                } else {
                                    isValidate = true
                                }

                            } else {
                                // Odoo specific error
                                isValidate = false
                                alertMessage(nameGet.errorMessage)
                                Timber.tag("VALIDATE_ON_NEXT").e("nameGet() failed with ${nameGet.errorMessage}")

//                                Timber.w("nameGet() failed with ${nameGet.errorMessage}")
                            }
                        } else {
                            alertMessage(response.message())
                            isValidate = false
                            Timber.tag("VALIDATE_ON_NEXT").e("request failed with ${response.code()}:${response.message()}")
//                            Timber.w("request failed with ${response.code()}:${response.message()}")
                        }
                    }
                    onError {
                        alertMessage(it.message!!)
                        isValidate = false
                        it.printStackTrace()
                        Timber.tag("VALIDATE_ON_ERROR").e(it)
//                        Toast.makeText(this@DetailEditTransferActivity,getString(R.string.operation_failed),Toast.LENGTH_SHORT).show()
                    }
                    onComplete {
                        if (isValidate) {
                            state = "done"
                            Toast.makeText(this@DetailEditTransferActivity, getString(R.string.validation_completed), Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                    }
                }
                /*when (getOption()) {
                    // List empty option
                    0 -> {}

                    //  Alert reservedAvailability and qty done is 0 option
                    1 -> {
                        alertNoReservedDoneQty()
                    }

                    // Done is equals to initial demand option
                    2 -> {

                    }

                    // Partial delivery option
                    3 -> {
                        alertPartialDelivery()
                    }

                    // Only reserved is available
                    4 -> {
                        alertImmediateTransfer()
                    }
                }*/
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onBackPressed() {
        if (isValidate) {
            val data = Intent()
            data.putExtra("BUTTON_VALIDATE", true)
            setResult(Activity.RESULT_OK, data)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchStockMoves(param1: ArrayList<Int>) {
        Odoo.searchRead("stock.move", StockMove.fields,
                listOf(
                        listOf(
                                "id", "in", param1
                        )
                ), mAdapter.rowItemCount, 0, "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (response.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<StockMove> = gson.fromJson(searchRead.result.records, moveLineListType)
                        stockMoves.addAll(items)
                        mAdapter.addRowItems(items)
                    } else {
                        mAdapter.showError(searchRead.errorMessage)
                    }
                } else {
                    mAdapter.showError(response.errorBodySpanned)
                }
                mAdapter.finishedMoreLoading()
            }
            onError { error ->
                error.printStackTrace()
                mAdapter.finishedMoreLoading()
            }
            onComplete {

            }
        }
    }
}


