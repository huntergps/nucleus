package com.domatix.yevbes.nucleus.sga.view.ui

import android.app.AlertDialog
import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RECORD_LIMIT
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentDialogLotListBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sga.service.model.StockProductionLot
import com.domatix.yevbes.nucleus.sga.view.adapter.GeneralSearchDialogFragmentDataAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.FromDialogToFragmentListener
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class LotListDialogFragment : DialogFragment(), SearchView.OnQueryTextListener {
    lateinit var compositeDisposable: CompositeDisposable private set
    lateinit var binding: FragmentDialogLotListBinding
    private lateinit var listener: FromDialogToFragmentListener
    private val stockProductionLotType = object : TypeToken<ArrayList<StockProductionLot>>() {}.type
    private var productId = 0

    companion object {
        fun newInstance(id: Int) =
                    LotListDialogFragment().apply {
                    arguments = Bundle().apply {
                        putInt("PRODUCT_ID", id)
                    }
                }
    }

    fun setListener(listener: FromDialogToFragmentListener) {
        this.listener = listener
    }

    private val mAdapter: GeneralSearchDialogFragmentDataAdapter by lazy {
        GeneralSearchDialogFragmentDataAdapter(this, arrayListOf(),
                object : OnItemClickListener {
                    override fun onItemClick(item: Any) {
                        listener.fromDialog(item)
                        this@LotListDialogFragment.dismiss()
                    }
                })
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            getQueryListOfLots(productId,query)
        }
        return true    }


    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            getQueryListOfLots(productId,newText)
        }
        return true    
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = activity?.let { AlertDialog.Builder(it) }!!
        compositeDisposable = CompositeDisposable()

        arguments?.let {
            productId = it.getInt("PRODUCT_ID")
        }

        binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.fragment_dialog_lot_list, null, false)
        val view = binding.root
//        val view = inflater.inflate(R.layout.fragment_dialog_products_sga, null)

        builder.setView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        view.findViewById<SearchView>(R.id.searchBar).setOnQueryTextListener(this)

        val layoutManager = LinearLayoutManager(activity)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mAdapter
        getQueryListOfLots(productId,"")
        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    private fun getQueryListOfLots(id: Int, query: String) {
        Odoo.searchRead(
                model = "stock.production.lot", fields = StockProductionLot.fields,
                domain = listOf(listOf("product_id", "=", id), listOf("name", "ilike", query)), offset = 0, limit = RECORD_LIMIT,
                sort = "name ASC"
        ) {
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
                        val items: ArrayList<StockProductionLot> = gson.fromJson(searchRead.result.records, stockProductionLotType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    getQueryListOfLots(id, query)
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
}