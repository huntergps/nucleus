package com.domatix.yevbes.nucleus.sga.view.ui

import android.app.Activity
import android.content.Intent
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
import com.domatix.yevbes.nucleus.databinding.FragmentTransfersBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockPicking
import com.domatix.yevbes.nucleus.sga.view.adapter.TransfersAdapter
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class TransfersFragment : Fragment(), SearchView.OnQueryTextListener {
    lateinit var compositeDisposable: CompositeDisposable private set
    private val stockTransferListType = object : TypeToken<ArrayList<StockPicking>>() {}.type
    private lateinit var binding: FragmentTransfersBinding
    lateinit var activity: MainActivity private set
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var query: String
    private var queryDashboard: String? = null
    private var stockPickingTypeId = 0

    companion object {
        const val REQUEST_CODE = 1
        const val STOCK_PICKING_KEY = "STOCK_PICKING_KEY"
        const val STOCK_PICKING_QUERY_KEY = "STOCK_PICKING_QUERY_KEY"

        fun newInstance(stockPickingTypeId: Int, query: String) =
                TransfersFragment().apply {
                    arguments = Bundle().apply {
                        putInt(STOCK_PICKING_KEY, stockPickingTypeId)
                        putString(STOCK_PICKING_QUERY_KEY, query)
                    }
                }
    }

    private val mAdapter: TransfersAdapter by lazy {
        TransfersAdapter(binding, arrayListOf(), object : OnTransferItemClickListener {
            override fun onItemClick(view: View) {
                val position = binding.rv.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as StockPicking
                val lines = item.moveLines
                val linesString = lines.toString()

                val intent = Intent(binding.root.context, DetailEditTransferActivity::class.java)
                val bundle = Bundle()
                bundle.putString(DetailEditTransferActivity.SELECTED_MOVE_LINES, linesString)
                bundle.putString(DetailEditTransferActivity.STOCK_PICKING_STATE, item.state.asString.trimFalse())
                bundle.putBoolean(DetailEditTransferActivity.STOCK_PICKING_LOT_TEXT_BOOLEAN, item.showLotsText)
                bundle.putInt(DetailEditTransferActivity.STOCK_PICKING_ID, item.id)
                intent.putExtras(bundle)
                startActivityForResult(intent, REQUEST_CODE)
//                startActivity(intent)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val flag = data!!.getBooleanExtra("BUTTON_VALIDATE", false)
                        if (flag) {
                            mAdapter.clear()

                            if (stockPickingTypeId == 0) {
                                if (!::query.isInitialized || query.isEmpty())
                                    fetchStockTransfers()
                                else
                                    queryFetchStockTransfers(query)
                            } else {
                                fetchFilteredStockTransfers(stockPickingTypeId)
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_transfers, menu)
        val searchItem = menu?.findItem(R.id.action_search_transfers)
        val searchView = searchItem?.actionView as SearchView
        if (stockPickingTypeId != 0 && !queryDashboard.isNullOrBlank()) {
            searchItem.expandActionView()
            searchView.setQuery(queryDashboard, false)
        }
        searchView.setOnQueryTextListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        compositeDisposable = CompositeDisposable()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTransfersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as MainActivity
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE
        activity.setTitle(R.string.action_inventory_transference)

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

        arguments?.let {
            this.stockPickingTypeId = it.getInt(STOCK_PICKING_KEY)
            this.queryDashboard = it.getString(STOCK_PICKING_QUERY_KEY)
        }

        if (stockPickingTypeId != 0) {
            fetchFilteredStockTransfers(stockPickingTypeId)
        }

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                if (stockPickingTypeId == 0) {
                    if (!::query.isInitialized || query.isEmpty())
                        fetchStockTransfers()
                    else
                        queryFetchStockTransfers(query)
                } else {
                    fetchFilteredStockTransfers(stockPickingTypeId)
                }
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                if (stockPickingTypeId == 0) {
                    if (!::query.isInitialized || query.isEmpty())
                        fetchStockTransfers()
                    else
                        queryFetchStockTransfers(query)
                } else {
                    fetchFilteredStockTransfers(stockPickingTypeId)
                }
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

//        mAdapter.clear()
        if (stockPickingTypeId == 0) {
            if (!::query.isInitialized || query.isEmpty()) {
                if (mAdapter.rowItemCount == 0) {
                    mAdapter.showMore()
                    fetchStockTransfers()
                }
            } else {
                if (mAdapter.rowItemCount == 0) {
                    mAdapter.showMore()
                    queryFetchStockTransfers(query)
                }
            }
        }

        binding.rv.adapter = mAdapter
    }

    private fun queryFetchStockTransfers(query: String?) {
        Odoo.searchRead("stock.picking", StockPicking.fields,
                listOf(
                        '|',
                        listOf("name", "ilike", query),
                        listOf("origin", "ilike", query)
                ), mAdapter.rowItemCount, RECORD_LIMIT, "scheduled_date DESC") {
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
                        val items: ArrayList<StockPicking> = gson.fromJson(searchRead.result.records, stockTransferListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    queryFetchStockTransfers(query)
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

    private fun fetchFilteredStockTransfers(pickingTypeId: Int) {
        Odoo.searchRead("stock.picking", StockPicking.fields, listOf(
                listOf("picking_type_id", '=', pickingTypeId),
                listOf("state", "in", listOf("assigned", "partially_available"))
        ), mAdapter.rowItemCount, RECORD_LIMIT) {
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
                        val items: ArrayList<StockPicking> = gson.fromJson(searchRead.result.records, stockTransferListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchFilteredStockTransfers(pickingTypeId)
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

    private fun fetchStockTransfers() {
        Odoo.searchRead("stock.picking", StockPicking.fields,
                listOf(
                ), mAdapter.rowItemCount, RECORD_LIMIT, "scheduled_date DESC") {
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
                        val items: ArrayList<StockPicking> = gson.fromJson(searchRead.result.records, stockTransferListType)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchStockTransfers()
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

    override fun onStart() {
        super.onStart()
        activity.binding.nv.menu.findItem(R.id.nav_inventory_transference).isChecked = true
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
        activity.binding.nv.menu.findItem(R.id.nav_inventory_transference).isChecked = false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            mAdapter.clear()
            stockPickingTypeId = 0
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = query
            queryDashboard = null
            queryFetchStockTransfers(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mAdapter.clear()
            stockPickingTypeId = 0
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = newText
            queryDashboard = null
            queryFetchStockTransfers(newText)
        }
        return true
    }
}
