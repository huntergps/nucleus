package com.domatix.yevbes.nucleus.sales.activities

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.View
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RECORD_LIMIT
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityPricelistListBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sales.adapters.PricelistAdapter
import com.domatix.yevbes.nucleus.sales.callbacks.OnItemClickedListener
import com.domatix.yevbes.nucleus.sales.entities.ProductPricelist
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_pricelist_list.*

class PricelistListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var compositeDisposable: CompositeDisposable
    lateinit var binding: ActivityPricelistListBinding
    private val productPricelististType = object : TypeToken<ArrayList<ProductPricelist>>() {}.type

    private val mAdapter: PricelistAdapter by lazy {
        PricelistAdapter(this, arrayListOf(), object : OnItemClickedListener {
            override fun onItemClicked(view: View) {
                val position = rv.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as ProductPricelist
                val returnIntent = Intent()
                returnIntent.putExtra(AddSaleFragment.PRICELIST_ID, gson.toJson(item))
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_pricelist_list)
//        setContentView(R.layout.activity_pricelist_list)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        setSupportActionBar(tb)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        tb.setNavigationOnClickListener {
            onBackPressed()
        }

        val layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false
        )

        rv.layoutManager = layoutManager
        rv.itemAnimator = DefaultItemAnimator()

        mAdapter.setupScrollListener(rv)

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                fetchPricelist()
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                fetchPricelist()
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            fetchPricelist()
        }

        rv.adapter = mAdapter
    }

    private fun fetchPricelist(query: String?) {
        Odoo.searchRead("product.pricelist", ProductPricelist.fields,
                        listOf(
                                "|",
                                listOf("name", "ilike", query),
                                listOf("display_name", "ilike", query)
                        )

                , mAdapter.rowItemCount, RECORD_LIMIT) {
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
                        val items: ArrayList<ProductPricelist> = gson.fromJson(searchRead.result.records, productPricelististType)

//                        activitiesList.addAll(items)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchPricelist(query)
                                }
                            }
                        }
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
                mAdapter.showError(error.message ?: getString(R.string.generic_error))
                mAdapter.finishedMoreLoading()
            }
        }
    }

    private fun fetchPricelist() {
        Odoo.searchRead("product.pricelist", ProductPricelist.fields,
                listOf(), mAdapter.rowItemCount, RECORD_LIMIT) {
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
                        val items: ArrayList<ProductPricelist> = gson.fromJson(searchRead.result.records, productPricelististType)

//                        activitiesList.addAll(items)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchPricelist()
                                }
                            }
                        }
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
                mAdapter.showError(error.message ?: getString(R.string.generic_error))
                mAdapter.finishedMoreLoading()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pricelist, menu)
        val item = menu?.findItem(R.id.action_search)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchPricelist(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchPricelist(newText)
        }
        return true
    }
}
