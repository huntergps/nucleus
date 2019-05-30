package com.domatix.yevbes.nucleus.sga.view.ui

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentInventorySettingsBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockInventory
import com.domatix.yevbes.nucleus.sga.view.adapter.InventorySettingsAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class InventorySettingsFragment : Fragment(), SearchView.OnQueryTextListener {
    lateinit var compositeDisposable: CompositeDisposable private set
    private val stockInventoryListType = object : TypeToken<ArrayList<StockInventory>>() {}.type
    lateinit var activity: MainActivity private set
    private lateinit var binding: FragmentInventorySettingsBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var query: String

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = query
            inventorySettingsQuery(query)
        }
        return true    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = newText
            inventorySettingsQuery(newText)
        }
        return true
    }

    companion object {
        const val REQUEST_CODE = 1

        @JvmStatic
        fun newInstance() =
                InventorySettingsFragment()
    }

    private val mAdapter: InventorySettingsAdapter by lazy {
        InventorySettingsAdapter(binding, arrayListOf(), object : OnTransferItemClickListener {
            override fun onItemClick(view: View) {
                val position = binding.rv.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as StockInventory

//                val intent = Intent(binding.root.context, StockInventoryLineActivity::class.java)
//                startActivityForResult(intent, REQUEST_CODE)
//                startActivity(intent)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_transfers, menu)
        val searchItem = menu?.findItem(R.id.action_search_transfers)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        compositeDisposable = CompositeDisposable()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentInventorySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as MainActivity
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE
        activity.setTitle(R.string.action_inventory_tools)

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
//        rv.addItemDecoration(DividerItemDecoration(getActivity(), DividerItemDecoration.HORIZONTAL))

        mAdapter.setupScrollListener(binding.rv)

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                if (!::query.isInitialized || query.isEmpty())
                    inventorySettings()
                else
                    inventorySettingsQuery(query)
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                if (!::query.isInitialized || query.isEmpty())
                    inventorySettings()
                else
                    inventorySettingsQuery(query)
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

//        mAdapter.clear()
            if (!::query.isInitialized || query.isEmpty()) {
                if (mAdapter.rowItemCount == 0) {
                    mAdapter.showMore()
                    inventorySettings()
                }
            } else {
                if (mAdapter.rowItemCount == 0) {
                    mAdapter.showMore()
                    inventorySettingsQuery(query)
                }
            }

        binding.rv.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        activity.binding.nv.menu.findItem(R.id.nav_inventory_tools).isChecked = true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_inventory_tools).isChecked = false
    }

    private fun inventorySettingsQuery(query: String?) {
        Odoo.searchRead("stock.inventory", StockInventory.fields,
                listOf(
                        listOf("name", "ilike", query)
                ), mAdapter.rowItemCount, RECORD_LIMIT, "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (response.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<StockInventory> = gson.fromJson(searchRead.result.records, stockInventoryListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    inventorySettingsQuery(query)
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
                mAdapter.finishedMoreLoading()
            }
            onComplete {

            }
        }
    }
    private fun inventorySettings() {
        Odoo.searchRead("stock.inventory", StockInventory.fields,
                listOf(
                ), mAdapter.rowItemCount, RECORD_LIMIT, "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (response.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter.hideError()
                        mAdapter.hideMore()
                        val items: ArrayList<StockInventory> = gson.fromJson(searchRead.result.records, stockInventoryListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    inventorySettings()
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
                mAdapter.finishedMoreLoading()
            }
            onComplete {

            }
        }
    }
}
