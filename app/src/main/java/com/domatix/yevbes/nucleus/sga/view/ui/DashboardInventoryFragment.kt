package com.domatix.yevbes.nucleus.sga.view.ui

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

import com.domatix.yevbes.nucleus.databinding.FragmentDashboardInventoryBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockPickingType
import com.domatix.yevbes.nucleus.sga.view.adapter.DashboardAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_dashboard_inventory.*

class DashboardInventoryFragment : Fragment(), SearchView.OnQueryTextListener {
    lateinit var binding: FragmentDashboardInventoryBinding private set
    lateinit var activity: MainActivity private set
    lateinit var compositeDisposable: CompositeDisposable private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val dashboardItemListType = object : TypeToken<ArrayList<StockPickingType>>() {}.type
    private lateinit var stockPickingType: StockPickingType

    private val mAdapter: DashboardAdapter by lazy {
        DashboardAdapter(this, arrayListOf(), object : OnItemClickListener {
            override fun onItemClick(item: Any) {
                stockPickingType = item as StockPickingType
                val query = "${jsonElementToString(stockPickingType.warehouseId)}: ${stockPickingType.name}"
                val transfersFragment = TransfersFragment.newInstance(stockPickingType.id, query)
                activity.replaceFragment(transfersFragment, MainActivity.ACTION_TRANSFERS_INVENTORY)
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
        binding = FragmentDashboardInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as MainActivity
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE
        activity.setTitle(R.string.action_dashboard_inventory)

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
                fetchDashboardInventory()
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                fetchDashboardInventory()
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            fetchDashboardInventory()
        }

        binding.rv.adapter = mAdapter
    }

    private fun fetchDashboardInventory() {
        Odoo.searchRead("stock.picking.type", StockPickingType.fields,
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
                        val items: ArrayList<StockPickingType> = gson.fromJson(searchRead.result.records, dashboardItemListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchDashboardInventory()
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

    override fun onStart() {
        super.onStart()
        activity.binding.nv.menu.findItem(R.id.nav_dashboard_inventory).isChecked = true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_dashboard_inventory, menu)
        val searchItem = menu?.findItem(R.id.action_search_dashboard_inventory)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_dashboard_inventory).isChecked = false
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.filter(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.filter(newText)
        }
        return true
    }
}
