package com.domatix.yevbes.nucleus.products

import android.app.AlertDialog
import android.app.Dialog
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.View
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class ProductDialogFragment : DialogFragment(), ProductDataAdapter.OnItemClickListener {

    interface OnFragmentDetachListener {
        fun onFragmentDialogDetach(product: ProductProduct)
    }

    fun setFragmentDetachListener(onFragmentDialogDetachListener: OnFragmentDetachListener) {
        this.onFragmentDetachListener = onFragmentDialogDetachListener
    }


    private var onFragmentDetachListener: OnFragmentDetachListener? = null
    private var productProduct: ProductProduct? = null

    override fun onItemClick(view: View, position: Int) {
        productProduct = productDataAdaper.getItem(position)
        this.dismiss()
    }


    companion object {

        @JvmStatic
        fun newInstance(items: ArrayList<ProductProduct>) =
                ProductDialogFragment().apply {
                    this.items = items
                }
    }

    private fun filterCountry(view: View) {
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = view.findViewById<SearchView>(R.id.searchBar)!!

        searchView.queryHint = getString(R.string.search_product)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    productDataAdaper.filter(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    productDataAdaper.filter(newText)
                }
                return true
            }

        })

        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName))
        searchView.setIconifiedByDefault(false)
    }

    lateinit var items: ArrayList<ProductProduct>


    /* private val productDataAdaper: ProductDataAdapter by lazy {
         ProductDataAdapter(arrayListOf())
     }*/

    private val productListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type


    lateinit var compositeDisposable: CompositeDisposable private set
    lateinit var productDataAdaper: ProductDataAdapter private set

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)

        val builder = activity?.let { AlertDialog.Builder(it) }!!
        val inflater = activity?.layoutInflater!!
        compositeDisposable = CompositeDisposable()

        val view = inflater.inflate(R.layout.fragment_dialog_products, null)
        filterCountry(view)
        builder.setView(view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProductList)
        val layoutManager = LinearLayoutManager(activity)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                layoutManager.orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        //getProductList()

        productDataAdaper = ProductDataAdapter(items)
        productDataAdaper.setClickListener(this)
        recyclerView.adapter = productDataAdaper

        //productDataAdaper = ProductDataAdapter()
        return builder.create()
    }


    /*private fun getProductList() {
        Odoo.searchRead(model = "product.product", fields = ProductProduct.fields, sort = "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, productListType)
                        productDataAdaper.addRowItems(items)
                    }
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete { productDataAdaper.notifyDataSetChanged() }
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (productProduct != null)
            onFragmentDetachListener?.onFragmentDialogDetach(productProduct!!)
    }


}