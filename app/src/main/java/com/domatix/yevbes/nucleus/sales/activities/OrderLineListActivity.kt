package com.domatix.yevbes.nucleus.sales.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RECORD_LIMIT
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityOrderLineListBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.views.OnViewLongClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.views.OnViewShortClickListener
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sales.adapters.AddProductDataAdapter
import com.domatix.yevbes.nucleus.sales.entities.CustomProductQtyEntity
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_add_product.view.*

class OrderLineListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchQueryProductProduct(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchQueryProductProduct(newText)
        }
        return true
    }

    private var recyclerView: RecyclerView? = null
    lateinit var compositeDisposable: CompositeDisposable private set
    private val productProductListType = object : TypeToken<ArrayList<CustomProductQtyEntity>>() {}.type
    private lateinit var binding: ActivityOrderLineListBinding
    private lateinit var confirmItem: MenuItem
    private val limit = RECORD_LIMIT


    val mAdapter: AddProductDataAdapter by lazy {
        AddProductDataAdapter(binding.rvOrderLineList, arrayListOf(), object : OnShortLongAdapterItemClickListener {
            override fun onShortAdapterItemPressed(view: View) {
                val position = binding.rvOrderLineList.getChildAdapterPosition(view)
                val actualItem = mAdapter.items[position] as CustomProductQtyEntity
                mAdapter.updateProductRowItem(position, actualItem)
                var isFounded = false
                mAdapter.selectedProducts.forEachIndexed { index, it ->
                    if (it.idProduct == actualItem.idProduct) {
                        actualItem.quantity = ++it.quantity
                        mAdapter.selectedProducts[index] = actualItem
                        isFounded = true
                    }
                }
                if (!isFounded) {
                    actualItem.quantity++
                    mAdapter.selectedProducts.add(actualItem)
                }

                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()

            }

            override fun onLongAdapterItemPressed(view: View) {
                showAlertDialogQty(view)
                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()
            }

        }, object : OnViewShortClickListener {
            override fun onShortClick(view: View) {
                val position = binding.rvOrderLineList.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as CustomProductQtyEntity
                var index = -1
                mAdapter.selectedProducts.forEachIndexed { indx, it ->
                    if (it.idProduct == item.idProduct) {
                        item.quantity = --it.quantity
                        if (item.quantity > 0f)
                            mAdapter.selectedProducts[indx] = item
                        else {
                            index = indx
                        }
                    }

                    if (index != -1)
                        mAdapter.selectedProducts.removeAt(index)

                }
                mAdapter.updateProductRowItem(position, item)
                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()
            }
        }, object : OnViewLongClickListener {
            override fun onLongClick(view: View) {
                val position = binding.rvOrderLineList.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as CustomProductQtyEntity
                item.quantity = 0f
                mAdapter.updateProductRowItem(position, item)
                var index = -1
                mAdapter.selectedProducts.forEachIndexed { idx, it ->
                    if (it.idProduct == item.idProduct) {
                        index = idx
                    }
                }
                if (index != -1)
                    mAdapter.selectedProducts.removeAt(index)

                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()
            }
        })
    }

    private fun showAlertDialogQty(view: View) {
        val position = binding.rvOrderLineList.getChildAdapterPosition(view)
        val item = mAdapter.items[position] as CustomProductQtyEntity
        val currentQty = item.quantity

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_element_qty, null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.quantity_dialog_title)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        val etQty = dialogView.findViewById<EditText>(R.id.quantity)
        etQty.setText(currentQty.toString())

        dialogView.bCancel.setOnClickListener {
            dialog.cancel()
        }

        dialogView.b_ok.setOnClickListener { _ ->
            val qty = etQty.text.toString().toFloat()
            if (currentQty != qty && qty >= 0f) {
                item.quantity = qty
                mAdapter.updateProductRowItem(position, item)
                var index = -1
                mAdapter.selectedProducts.forEachIndexed { indx, it ->
                    if (it.idProduct == item.idProduct) {
                        if (qty == 0f) {
                            index = indx
                        }else{
                            mAdapter.selectedProducts[indx] = item
                        }
                    }
                }
                if (index != -1)
                    mAdapter.selectedProducts.removeAt(index)

                Toast.makeText(this, getString(R.string.qty_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()
            }else{
                Toast.makeText(this, getString(R.string.qty_wrong), Toast.LENGTH_SHORT).show()
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_line_list)
        compositeDisposable = CompositeDisposable()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        recyclerView = findViewById(R.id.rv_order_line_list)
        val mLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = mAdapter

        fetchProductProduct()
    }


    private fun fetchProductProduct() {
        Odoo.searchRead("product.product", CustomProductQtyEntity.fields,
                listOf(listOf("sale_ok", "=", true
                )), mAdapter.rowItemCount, limit, "name DESC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->

                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<CustomProductQtyEntity> = gson.fromJson(searchRead.result.records, productProductListType)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchProductProduct()
                                }
                            }
                        }
                        mAdapter.addProductRowItems(items)
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

    private fun fetchQueryProductProduct(query: String) {
        Odoo.searchRead("product.product", CustomProductQtyEntity.fields,
                listOf(
                        listOf("sale_ok", "=", true),
                        "|",
                        "|",
                        listOf("default_code", "ilike", query),
                        listOf("name", "ilike", query),
                        listOf("barcode", "ilike", query)
                ), mAdapter.rowItemCount, limit, "name DESC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<CustomProductQtyEntity> = gson.fromJson(searchRead.result.records, productProductListType)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchQueryProductProduct(query)
                                }
                            }
                        }
                        mAdapter.addProductRowItems(items)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_product_qty, menu)
        confirmItem = menu?.findItem(R.id.action_confirm)!!
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_search -> {

            }
            R.id.action_confirm -> {
                if (mAdapter.selectedProducts.isNotEmpty()) {

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
