package com.domatix.yevbes.nucleus.sga.view.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityEditProductBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sga.service.model.StockMove
import com.domatix.yevbes.nucleus.sga.service.model.StockMoveLine
import com.domatix.yevbes.nucleus.sga.service.model.StockProductionLot
import com.domatix.yevbes.nucleus.sga.view.adapter.DetailedOperationsAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.FromDialogToFragmentListener
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickUpdateListener
import com.domatix.yevbes.nucleus.trimFalse
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_edit_product.*
import kotlinx.android.synthetic.main.dialog_add_product.view.*
import timber.log.Timber

class EditProductActivity : AppCompatActivity() {
    private lateinit var stockMove: StockMove
    lateinit var binding: ActivityEditProductBinding
    private val moveStockType = object : TypeToken<StockMove>() {}.type
    private val stockMoveLinesListType = object : TypeToken<ArrayList<StockMoveLine>>() {}.type
    private var isChanged = false
    private val changedItmems = HashMap<Int, StockMoveLine>()
    private var isShowLotsText = false
    private lateinit var lotId: JsonElement

    private var productId = 0
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var mapList: HashMap<Int, Boolean>

    companion object {
        const val MOVE_LINE = "MOVE_LINE"
        const val STOCK_PICKING_STATE = "STOCK_PICKING_STATE"
        const val STOCK_PICKING_LOT_TEXT_BOOLEAN = "STOCK_PICKING_LOT_TEXT_BOOLEAN"
    }

    private val mAdapter: DetailedOperationsAdapter by lazy {
        DetailedOperationsAdapter(binding, arrayListOf(), object : OnItemClickUpdateListener {
            override fun onItemClick(item: Any, positon: Int) {
                changeQTY(item, positon)
            }
        })
    }

    private fun changeQTY(item: Any, positon: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.quantity_dialog_title)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()
        dialogView.bCancel.setOnClickListener {
            dialog.cancel()
        }

        val tilLotSerial = dialogView.findViewById<TextInputLayout>(R.id.tilLotSerial)
        val edLotSerial = dialogView.findViewById<EditText>(R.id.edLotSerial)

        val productId = (item as StockMoveLine).productId.asJsonArray[0].asInt
        val flag = mapList[productId]

        if (flag!!) {
            tilLotSerial.visibility = View.VISIBLE
        }

        val qtyET = dialogView.findViewById<EditText>(R.id.edInitialDemand)
        qtyET.text = SpannableStringBuilder(item.qty.toString())
        lotId = item.lotId

        if (!isShowLotsText) {
            tilLotSerial.isClickable = false
            tilLotSerial.isFocusable = false

            edLotSerial.isClickable = false
            edLotSerial.isFocusable = false
            edLotSerial.inputType = InputType.TYPE_NULL
            edLotSerial.setTextIsSelectable(false)
            edLotSerial.isFocusableInTouchMode = false

            if (!item.lotId.isJsonPrimitive)
                edLotSerial.text = SpannableStringBuilder(item.lotId.asJsonArray[1].asString)

            edLotSerial.setOnClickListener {
                val frag = LotListDialogFragment.newInstance(productId)
                frag.show(supportFragmentManager, "dialog")
                frag.setListener(object : FromDialogToFragmentListener {
                    override fun fromDialog(item: Any) {
                        val it = item as StockProductionLot
                        val jsonArray = JsonArray()
                        jsonArray.add(it.id)
                        jsonArray.add(it.name)
                        lotId = jsonArray
                        edLotSerial.text = SpannableStringBuilder(it.displayName)
                    }
                })
            }
        } else {
            edLotSerial.text = SpannableStringBuilder(item.lotName.trimFalse())
        }

        dialogView.b_ok.setOnClickListener { _ ->
            if (qtyET.text.toString().toFloat() >= 0f) {
                val it = item
                if (it.qty != qtyET.text.toString().toFloat()) {
                    val updateVal = if (it.qty < qtyET.text.toString().toFloat()) {
                        qtyET.text.toString().toFloat() - it.qty
                    } else {
                        (it.qty - qtyET.text.toString().toFloat()) * -1
                    }

                    var txtLotName = edLotSerial.text.toString()
                    if (flag) {
                        if (isShowLotsText) {
                            // Lot name
                            if (txtLotName == it.lotName || txtLotName.isBlank()) {
                                txtLotName = it.lotName
                                edLotSerial.text = SpannableStringBuilder(it.lotName)
                            }
                        } else {
                            // Lot id

                        }
                    }

                    val updated = StockMoveLine(it.id, it.productId, qtyET.text.toString().toFloat(), txtLotName, lotId, it.productQty, it.locationId, it.locationDestId)
                    binding.edDone.text = SpannableStringBuilder((binding.edDone.text.toString().toFloat() + updateVal).toString())
                    mAdapter.updateItem(updated, positon)
                    changedItmems[updated.id] = updated
                }
                isChanged = true
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Quantity must be positive number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_product)
        setSupportActionBar(toolbar)
        compositeDisposable = CompositeDisposable()
        mapList = HashMap()

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val moveStockGson = intent.extras.getString(MOVE_LINE)
        stockMove = gson.fromJson(moveStockGson, moveStockType)
        binding.item = stockMove

        productId = stockMove.productId.get(0).asInt

        val state = intent.extras.getString(STOCK_PICKING_STATE)
        isShowLotsText = intent.extras.getBoolean(STOCK_PICKING_LOT_TEXT_BOOLEAN)
        binding.isShowLotsText = isShowLotsText
        /*if (!state.isNullOrBlank() && state == "draft") {
            edProduct.setOnClickListener {
                val dialogFragment = ProductDialogFragment()
                dialogFragment.setListener(
                        object : FromDialogToFragmentListener {
                            override fun fromDialog(item: Any) {
                                val product = item as ProductProduct
                                productId = product.id
                                edProduct.text = SpannableStringBuilder(product.name)
                            }
                        }
                )
                dialogFragment.show(supportFragmentManager, "ProductProductList")
            }
        }*/

        val mLayoutManager = LinearLayoutManager(this)
        binding.rv.layoutManager = mLayoutManager
        binding.rv.itemAnimator = DefaultItemAnimator()

        mAdapter.setupScrollListener(binding.rv)
        binding.rv.adapter = mAdapter
        fetchStockMoveLines(stockMove.moveLineIds)
    }

    private val idsListType = object : TypeToken<List<Int>>() {}.type

    private fun fetchStockMoveLines(moveLineIds: JsonArray) {
        val list: List<Int> = gson.fromJson(moveLineIds, idsListType)
        Odoo.read("stock.move.line", list, StockMoveLine.fields) {
            onSubscribe {
                compositeDisposable.add(it)
            }
            onError { error ->
                error.printStackTrace()
                mAdapter.finishedMoreLoading()
            }
            onComplete {

            }
            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (response.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<StockMoveLine> = gson.fromJson(searchRead.result, stockMoveLinesListType)

                        if (items.isEmpty()) {
                            mAdapter.showEmpty()
                        } else {
                            fetchProductProductTracking(items)
                            mAdapter.addRowItems(items)
                        }
                    } else {
                        mAdapter.showError(searchRead.errorMessage)
                    }
                } else {
                    mAdapter.showError(response.errorBodySpanned)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit_product, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_confirm -> {
                if (isChanged) {
                    for ((k, v) in changedItmems) {
                        Odoo.write(model = "stock.move.line", ids = listOf(k),
                                values = mapOf("qty_done" to v.qty,
                                        "lot_name" to v.lotName,
                                        if (v.lotId.isJsonArray) {
                                            "lot_id" to v.lotId.asJsonArray[0].asInt
                                        } else {
                                            "lot_id" to v.lotId
                                        })
                        ) {
                            onSubscribe { disposable ->
                                compositeDisposable.add(disposable)
                            }

                            onNext { response ->
                                if (response.isSuccessful) {
                                    val write = response.body()!!
                                    if (write.isSuccessful) {
                                    } else {
                                        Timber.w("write() failed with ${write.errorMessage}")
                                    }
                                } else {
                                    Timber.w("request failed with ${response.code()}:${response.message()}")
                                }
                            }

                            onError { error ->
                                error.printStackTrace()
                            }

                            onComplete {

                            }

                        }
                    }

                    if (changedItmems.isNotEmpty()) {
                        Toast.makeText(this, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()
                    }
                }

                /*    Odoo.write(model = "stock.move", ids = listOf(stockMove.id),
                            values = mapOf("product_id" to productId,
        //                                    "product_uom_qty" to edInitialDemand.text.toString().toFloat(),
        //                                    "reserved_availability" to edReserved.text.toString().toFloat(),
                                    "quantity_done" to edDone.text.toString().toFloat())
                    ) {
                        onSubscribe { disposable ->
                            compositeDisposable.add(disposable)
                        }

                        onNext { response ->
                            if (response.isSuccessful) {
                                val write = response.body()!!
                                if (write.isSuccessful) {
                                    val result = write.result

                                } else {
                                    // Odoo specific error
                                    Timber.w("write() failed with ${write.errorMessage}")
                                }
                            } else {
                                Timber.w("request failed with ${response.code()}:${response.message()}")
                            }
                        }

                        onError { error ->
                            error.printStackTrace()
                        }

                        onComplete {
                            Toast.makeText(this@EditProductActivity, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }*/
            }
            R.id.action_cancel -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchProductProductTracking(list: List<StockMoveLine>) {
        list.forEach {
            val id = it.productId.asJsonArray[0].asInt
            Odoo.read(
                    model = "product.product", ids = listOf(id), fields = listOf("tracking")
            ) {
                onSubscribe { disposable ->
                    compositeDisposable.add(disposable)
                }
                onNext { response ->
                    if (response.isSuccessful) {
                        val read = response.body()!!
                        if (read.isSuccessful) {
                            val result = read.result
                            val aux = result.asJsonArray[0].asJsonObject["tracking"].asString
                            val value = aux != "none"
                            mAdapter.addMapItem(id, value)
                            mapList[id] = value
                        } else {
                            // Odoo specific error
                            Timber.w("read() failed with ${read.errorMessage}")
                        }
                    } else {
                        Timber.w("request failed with ${response.code()}:${response.message()}")
                    }
                }

                onError { error ->
                    error.printStackTrace()
                }

                onComplete {

                }
            }
        }
    }
}
