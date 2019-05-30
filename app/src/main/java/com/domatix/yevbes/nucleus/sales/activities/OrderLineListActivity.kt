package com.domatix.yevbes.nucleus.sales.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.widget.ImageView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RECORD_LIMIT
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityOrderLineListBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.adapters.AddProductProductDataAdapter
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class OrderLineListActivity : AppCompatActivity() {


    private var recyclerView: RecyclerView? = null

    lateinit var compositeDisposable: CompositeDisposable private set
    private val productProductListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    private lateinit var binding: ActivityOrderLineListBinding
    private val limit = RECORD_LIMIT

    val mAdapter: AddProductProductDataAdapter by lazy {
        AddProductProductDataAdapter(binding, arrayListOf())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_order_line_list)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_line_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        filterProductProduct()

        compositeDisposable = CompositeDisposable()

        recyclerView = findViewById(R.id.rv_order_line_list)

        //mAdapter = AddProductProductDataAdapter(binding, saleOrderLineList)
        val mLayoutManager = LinearLayoutManager(applicationContext)

        recyclerView?.layoutManager = mLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.adapter = mAdapter

        findViewById<ImageView>(R.id.addSelectedProductProductImageButton).setOnClickListener {
            if (mAdapter.selectedList.isNotEmpty()) {
                val selectedListGson = Gson()
                val selectedListGsonAsAString = selectedListGson.toJson(mAdapter.selectedList)
                val returnIntent = Intent()
                returnIntent.putExtra(AddSaleFragment.SELECTED_LIST, selectedListGsonAsAString)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }

        fetchProductProduct()
    }

    private fun filterProductProduct() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.queryHint = getString(R.string.search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    mAdapter?.filter(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    mAdapter?.filter(newText)
                }
                return true
            }

        })

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false)
    }

    private fun fetchProductProduct() {
        Odoo.searchRead("product.product", ProductProduct.fields,
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
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, productProductListType)

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
                        mAdapter.addRowItems(items)
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
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

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater?.inflate(R.menu.menu_order_line_list, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_add -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }*/
}
