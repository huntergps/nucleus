package com.domatix.yevbes.nucleus.sga.view.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.domatix.yevbes.nucleus.*

import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentStoreBinding
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sga.view.adapter.StoreAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_store.*

/**
 * A simple [Fragment] subclass.
 *
 */
class StoreFragment : Fragment(), SearchView.OnQueryTextListener {

    lateinit var binding: FragmentStoreBinding private set
    lateinit var activity: MainActivity private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val storeListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    lateinit var compositeDisposable: CompositeDisposable private set

    private val mAdapter: StoreAdapter by lazy {
        StoreAdapter(this, arrayListOf(), object : OnTransferItemClickListener {
            override fun onItemClick(view: View) {
                val position = binding.rv.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as ProductProduct
                val stringItem = gson.toJson(item)
                val intent = Intent(binding.root.context, ProductDetailsActivity::class.java)
                intent.putExtra(ProductDetailsActivity.PRODUCT_STRING_JSON,stringItem)
                startActivity(intent)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        compositeDisposable = CompositeDisposable()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity = getActivity() as MainActivity
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE
        activity.setTitle(R.string.action_store)

        activity.setSupportActionBar(binding.tb)
        val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        drawerToggle = ActionBarDrawerToggle(activity, activity.binding.dl,
                binding.tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        activity.binding.dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        val mLayoutManager = LinearLayoutManager(context)

        binding.rv.layoutManager = mLayoutManager
        binding.rv.itemAnimator = DefaultItemAnimator()

        rv.addItemDecoration(DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL))

        mAdapter.setupScrollListener(binding.rv)

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                fetchProduct()
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                fetchProduct()
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            fetchProduct()
        }

        binding.rv.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        activity.binding.nv.menu.findItem(R.id.nav_sga).isChecked = true
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_store, menu)
        val searchItem = menu?.findItem(R.id.action_search_store)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_sga).isChecked = false
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    private fun fetchProductQuery(query: String) {
        Odoo.searchRead("product.product", ProductProduct.fields,
                listOf(
                        listOf("type", "in", listOf("consu", "product")),
                        "|",
                        "|",
                        listOf("default_code","ilike", query),
                        listOf("name","ilike", query),
                        listOf("barcode","ilike", query)
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
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, storeListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchProductQuery(query)
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
                        val items: ArrayList<ProductProduct> = gson.fromJson(searchRead.result.records, storeListType)

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


    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchProductQuery(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            fetchProductQuery(newText)
        }
        return true
    }

}
