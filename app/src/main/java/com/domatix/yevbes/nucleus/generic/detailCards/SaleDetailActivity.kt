package com.domatix.yevbes.nucleus.generic.detailCards

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityDetailSaleBinding
import com.domatix.yevbes.nucleus.generic.ui.dialogs.CustomDialogFragment
import com.domatix.yevbes.nucleus.sales.adapters.SaleOrderLineDataAdapter
import com.domatix.yevbes.nucleus.sales.entities.SaleOrder
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.utils.ConstantManager
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

class SaleDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailSaleBinding
    private lateinit var compositeDisposable: CompositeDisposable
    private lateinit var customDialogFragment: CustomDialogFragment

    private val mAdapter: SaleOrderLineDataAdapter by lazy {
        SaleOrderLineDataAdapter(binding.saleOrderLineRecyclerView, arrayListOf())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saleOrderId: Int = intent.extras.getInt(ConstantManager.MODEL_ITEM_ID)
        compositeDisposable = CompositeDisposable()
        customDialogFragment = CustomDialogFragment.newInstance(this, supportFragmentManager, "TAG",
                getString(R.string.loading), getString(R.string.loading_data), cancelable = false, showInstantly = true)
        fetchSaleOrder(saleOrderId)
    }

    private fun setupUI(saleOrder: SaleOrder) {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_sale)

        setSupportActionBar(binding.tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tb.setNavigationOnClickListener {
            onBackPressed()
        }

        title = getString(R.string.sale_name_title, saleOrder.name)
        binding.saleOrder = saleOrder

        val mLayoutManager = LinearLayoutManager(this)
        binding.saleOrderLineRecyclerView.layoutManager = mLayoutManager
        binding.saleOrderLineRecyclerView.itemAnimator = DefaultItemAnimator()
        mAdapter.setupScrollListener(binding.saleOrderLineRecyclerView)
        binding.saleOrderLineRecyclerView.adapter = mAdapter

        saleOrder.partnerId.asJsonArray?.let { partnerId ->
            val id = partnerId.asJsonArray[0].asInt
            binding.partnerId.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            binding.partnerId.setOnClickListener(modelDetailsListener(id, this, binding.partnerId, "res.partner"))
        }

        fetchSaleOrderLines("order_id", saleOrder)
    }

    private fun fetchSaleOrder(id: Int) {
        var saleOrder: SaleOrder? = null
        Odoo.load(id, "sale.order", SaleOrder.fields) {
            onSubscribe {
                compositeDisposable.add(it)
            }
            onNext {
                if (it.isSuccessful) {
                    val load = it.body()!!
                    if (load.isSuccessful) {
                        val result = load.result
                        saleOrder = gson.fromJson<SaleOrder>(result.value, object : TypeToken<SaleOrder>() {
                        }.type)
                    } else {
                        Timber.w("load() failed with ${load.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${it.code()}:${it.message()}")
                }
            }
            onError {
                it.printStackTrace()
            }
            onComplete {
                customDialogFragment.dismissDialog()
                saleOrder?.let {
                    setupUI(it)
                }
                if (saleOrder == null) {
                    onBackPressed()
                }
            }
        }
    }

    private fun fetchSaleOrderLines(param1: String?, param2: SaleOrder?) {
        Odoo.searchRead("sale.order.line",
                SaleOrderLine.fields,
                listOf(
                        listOf(param1, '=', param2?.id)
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
                        val items: ArrayList<SaleOrderLine> = gson.fromJson(searchRead.result.records, object : TypeToken<ArrayList<SaleOrderLine>>() {}.type)
                        if (items.size == 0 && mAdapter.rowItemCount == 0) {
                            mAdapter.showEmpty()
                        }
                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchSaleOrderLines(param1, param2)
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
                mAdapter.showError(error.message ?: getString(R.string.generic_error))
                mAdapter.finishedMoreLoading()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}
