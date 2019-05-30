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
import com.domatix.yevbes.nucleus.databinding.FragmentDialogProductsSgaBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sga.view.adapter.ProductDataAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.FromDialogToFragmentListener
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable


class ProductDialogFragment : DialogFragment(), SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            queryFetchProduct(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            queryFetchProduct(newText)
        }
        return true
    }

    lateinit var binding: FragmentDialogProductsSgaBinding
    lateinit var items: ArrayList<ProductProduct>
    lateinit var compositeDisposable: CompositeDisposable private set
    private val productListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    private lateinit var listener: FromDialogToFragmentListener


    private val mAdapter: ProductDataAdapter by lazy {
        ProductDataAdapter(this, arrayListOf(),
                object : OnItemClickListener {
                    override fun onItemClick(item: Any) {
                        listener.fromDialog(item)
                        this@ProductDialogFragment.dismiss()
                    }
                })
    }

    fun setListener(listener: FromDialogToFragmentListener) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = activity?.let { AlertDialog.Builder(it) }!!
        val inflater = activity?.layoutInflater!!
        compositeDisposable = CompositeDisposable()

        binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.fragment_dialog_products_sga, null, false)
        val view = binding.root
//        val view = inflater.inflate(R.layout.fragment_dialog_products_sga, null)

        builder.setView(view)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProductList)
        view.findViewById<SearchView>(R.id.searchBar).setOnQueryTextListener(this)

        val layoutManager = LinearLayoutManager(activity)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = mAdapter
        fetchProduct()

        return builder.create()
    }


    private fun fetchProduct() {
        Odoo.searchRead("product.product", ProductProduct.fields,
                listOf(), mAdapter.rowItemCount, RECORD_LIMIT, "name ASC") {

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
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, productListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchProduct()
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

    private fun queryFetchProduct(query: String) {
        Odoo.searchRead("product.product", ProductProduct.fields,
                listOf(
                        listOf("name", "ilike", query)
                ), mAdapter.rowItemCount, RECORD_LIMIT, "name ASC") {

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
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, productListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    queryFetchProduct(query)
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

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

}