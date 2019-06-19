package com.domatix.yevbes.nucleus.sales.activities

import android.app.Activity
import android.content.Intent
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
import com.domatix.yevbes.nucleus.sales.fragments.OrderEditFragment
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_add_product.view.*

val limit = RECORD_LIMIT * 2

class OrderLineListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = query
            fetchQueryProductProduct(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = newText
            fetchQueryProductProduct(newText)
        }
        return true
    }

    private var recyclerView: RecyclerView? = null
    lateinit var compositeDisposable: CompositeDisposable private set
    private val productProductListType = object : TypeToken<ArrayList<CustomProductQtyEntity>>() {}.type
    private lateinit var binding: ActivityOrderLineListBinding
    private lateinit var confirmItem: MenuItem
    private var query: String? = null

    val mAdapter: AddProductDataAdapter by lazy {
        AddProductDataAdapter(binding.rvOrderLineList, arrayListOf(), object : OnShortLongAdapterItemClickListener {
            override fun onShortAdapterItemPressed(view: View) {
                val position = binding.rvOrderLineList.getChildAdapterPosition(view)
                val actualItem = mAdapter.items[position] as CustomProductQtyEntity
                mAdapter.updateProductRowItem(position, actualItem)
                var isFounded = false
                var index = -1
                mAdapter.selectedProducts.forEachIndexed { indx, it ->
                    if (it.idProduct == actualItem.idProduct) {
                        actualItem.quantity = ++it.quantity
                        isFounded = true
                        index = indx
                    }
                }

                if (!isFounded) {
                    actualItem.quantity++
                    mAdapter.selectedProducts.add(actualItem)
                } else {
                    if (index != -1)
                        mAdapter.selectedProducts[index] = actualItem
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
                var isFinded = false

                mAdapter.selectedProducts.forEachIndexed { indx, it ->
                    if (it.idProduct == item.idProduct) {
                        isFinded = true
                        item.quantity = --it.quantity
                        index = indx
                    }
                }

                if (isFinded) {
                    if (item.quantity > 0f)
                        mAdapter.selectedProducts[index] = item
                    else {
                        mAdapter.selectedProducts.removeAt(index)
                    }
                }

                /*if (index != -1)
                    mAdapter.selectedProducts.removeAt(index)*/

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
                var index = -1
                var isFinded = false
                mAdapter.updateProductRowItem(position, item)

                if (mAdapter.selectedProducts.isEmpty())
                    mAdapter.selectedProducts.add(item)
                else
                    mAdapter.selectedProducts.forEachIndexed { indx, it ->
                        if (it.idProduct == item.idProduct) {
                            isFinded = !isFinded
                            index = indx
                        }
                    }

                if (isFinded) {
                    mAdapter.selectedProducts[index] = item
                } else {
                    mAdapter.selectedProducts.add(item)
                }

                Toast.makeText(this, getString(R.string.qty_updated), Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                confirmItem.isEnabled = mAdapter.selectedProducts.isNotEmpty()
            } else {
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

        mAdapter.setupScrollListener(binding.rvOrderLineList)


        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                if (query != null) {
                    fetchQueryProductProduct(query!!)
                } else {
                    fetchProductProduct()
                }
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                if (query != null) {
                    fetchQueryProductProduct(query!!)
                } else {
                    fetchProductProduct()
                }
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            if (query != null) {
                fetchQueryProductProduct(query!!)
            } else {
                fetchProductProduct()
            }
        }

        recyclerView?.adapter = mAdapter

//        fetchProductProduct()
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        confirmItem = menu?.findItem(R.id.action_confirm)!!
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_product_qty, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_confirm -> {
                if (mAdapter.selectedProducts.isNotEmpty()) {
                    val gsonString = gson.toJson(mAdapter.selectedProducts)
                    val data = Intent()
                    data.putExtra(OrderEditFragment.NEW_LIST_ELEMENTS, gsonString)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
