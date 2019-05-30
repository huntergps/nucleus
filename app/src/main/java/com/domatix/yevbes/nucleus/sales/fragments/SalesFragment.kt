package com.domatix.yevbes.nucleus.sales.fragments


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
import com.domatix.yevbes.nucleus.core.Odoo.user
import com.domatix.yevbes.nucleus.databinding.FragmentSalesBinding
import com.domatix.yevbes.nucleus.sales.adapters.SalesDataAdapter
import com.domatix.yevbes.nucleus.sales.entities.ResConfigSettings
import com.domatix.yevbes.nucleus.sales.entities.SaleOrder
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable


/**
 * A simple [Fragment] subclass.
 *
 */
class SalesFragment : Fragment(), SearchView.OnQueryTextListener {
    companion object {
        enum class SalesType {
            SaleOrder
        }

        private var actualItem: MenuItem? = null
        private var actualBoolean: Boolean? = true

        private const val TYPE = "type"
        private var flag: Boolean = false

        fun newInstance(activityType: SalesType) =
                SalesFragment().apply {
                    arguments = Bundle().apply {
                        putString(TYPE, activityType.name)
                    }
                }
    }

    // Filters
    private val items: ArrayList<Any> = ArrayList()

    private val activeFilters: ArrayList<Any> = ArrayList()

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val saleOrderListType = object : TypeToken<ArrayList<SaleOrder>>() {}.type
    private val resConfigSettingsListType = object : TypeToken<ArrayList<ResConfigSettings>>() {}.type
    private lateinit var salesType: SalesType
    private var filterGroup: Int = 0
    private var filterGroupSales: Boolean = true

    private val limit = RECORD_LIMIT

    val mAdapter: SalesDataAdapter by lazy {
        SalesDataAdapter(this, arrayListOf())
    }

    lateinit var activity: MainActivity private set
    lateinit var binding: FragmentSalesBinding private set
    lateinit var compositeDisposable: CompositeDisposable private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        items.add(0, listOf("user_id", '=', user.id))
        items.add(1, listOf("state", '=', "draft"))
        items.add(2, listOf("state", '=', "sent"))
        items.add(3, listOf("state", "in", listOf("sale", "done")))

        // Default filter
        activeFilters.add(0, items[0])
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        compositeDisposable = CompositeDisposable()

        // Inflate the layout for this fragment
        binding = FragmentSalesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //val toolbar = view?.findViewById<Toolbar>(R.id.toolbar)
        activity = getActivity() as MainActivity
        activity.setTitle(R.string.action_sales)
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        arguments?.let {
            salesType = SalesType.valueOf(it.getString(TYPE))
        }

        //activity.setSupportActionBar(toolbar)
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
        binding.salesRecyclerView.layoutManager = mLayoutManager
        binding.salesRecyclerView.itemAnimator = DefaultItemAnimator()

        mAdapter.setupScrollListener(binding.salesRecyclerView)

        if (!mAdapter.hasRetryListener()) {
            mAdapter.retryListener {
                //fetchSales()
                fetchSales(activeFilters)
            }
        }

        binding.srl.setOnRefreshListener {
            mAdapter.clear()
            if (!mAdapter.hasMoreListener()) {
                mAdapter.showMore()
                //fetchSales()
                fetchSales(activeFilters)
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

     /*   if (mAdapter.rowItemCount == 0) {
            mAdapter.showMore()
            //fetchSales()
//            fetchSales(activeFilters)
        }*/

        binding.fabAddButton.alpha = 0.80f

        binding.fabAddButton.setOnClickListener {
            floatingActionButtonAddPressed()
        }

        binding.salesRecyclerView.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        activity.binding.nv.menu.findItem(R.id.nav_sales).isChecked = true
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_sales).isChecked = false
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_sales, menu)

        val searchItem = menu?.findItem(R.id.action_sales_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.isChecked = !item?.isChecked!!
        when (item.itemId) {
            R.id.action_filter_my_sales -> {
                mAdapter.clear()
                if (item.isChecked) {
                    activeFilters.add(0, items[0])
                    filterGroupSales = true
                } else {
                    activeFilters.removeAt(0)
                    filterGroupSales = false
                }
                fetchSales(activeFilters)
            }

            R.id.action_filter_budgets -> {
                mAdapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[1])
                    filterGroup++
                } else {
                    activeFilters.remove(items[1])
                    filterGroup--
                }
                fetchSales(activeFilters)
            }

            R.id.action_filter_budgets_sent -> {
                mAdapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[2])
                    filterGroup++
                } else {
                    activeFilters.remove(items[2])
                    filterGroup--
                }
                fetchSales(activeFilters)
            }

            R.id.action_filter_sales -> {
                mAdapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[3])
                    filterGroup++
                } else {
                    activeFilters.remove(items[3])
                    filterGroup--
                }
                fetchSales(activeFilters)
            }
        }

        return super.onOptionsItemSelected(item)
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

    private fun fetchSales() {
        Odoo.searchRead("sale.order", SaleOrder.fields,
                when (salesType) {
                    SalesType.SaleOrder -> {
                        listOf()
                    }
                }
                , mAdapter.rowItemCount, limit, "date_order DESC") {
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
                        val items: ArrayList<SaleOrder> = gson.fromJson(searchRead.result.records, saleOrderListType)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchSales()
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

    private fun fetchSales(param1: Any) {
        Odoo.searchRead("sale.order", SaleOrder.fields,
                if (!(param1 as ArrayList<*>).isEmpty()) {
                    if (filterGroup == 0 && filterGroupSales) {
                        // Solamente activado el filtro de -> mis pedidos
                        param1
                    } else if (filterGroup > 0 && filterGroupSales) {
                        when (filterGroup) {
                            1 -> {
                                param1
                            }

                            // only one OR
                            2 -> {
                                // My sales
                                val any = param1[0]

                                val auxArray = ArrayList<Any>()
                                auxArray.add(0, '|')
                                auxArray.addAll(param1.subList(1, param1.size))
                                auxArray.add(any)
                                auxArray
                            }

                            // two OR's
                            3 -> {
                                val any = param1[0]

                                val auxArray = ArrayList<Any>()
                                auxArray.add(0, '|')
                                auxArray.add(1, '|')
                                auxArray.addAll(param1.subList(2, param1.size))
                                auxArray.add(any)
                                auxArray
                            }
                            else -> {
                                listOf<Any>()
                            }
                        }
                    } else if (filterGroup > 0 && !filterGroupSales) {
                        when (filterGroup) {
                            1 -> {
                                param1
                            }

                            // only one OR
                            2 -> {
                                val auxArray = ArrayList<Any>()
                                auxArray.add(0, '|')
                                auxArray.addAll(param1)
                                auxArray

                            }

                            // two OR's
                            3 -> {
                                val auxArray = ArrayList<Any>()
                                auxArray.add(0, '|')
                                auxArray.add(1, '|')
                                auxArray.addAll(param1)
                                auxArray
                            }
                            else -> {
                                // Cualquier otro caso
                                listOf<Any>()
                            }
                        }
                    } else {
                        // Cualquier otro caso
                        listOf<Any>()
                    }
                } else {
                    // No hay ningún filtro activo -> Sacar todos los datos
                    listOf<Any>()
                }
                , mAdapter.rowItemCount, RECORD_LIMIT, "date_order DESC") {
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
                        val items: ArrayList<SaleOrder> = gson.fromJson(searchRead.result.records, saleOrderListType)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchSales(param1)
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



    private fun fetchSalesFilter(param1: String?, param2: Any?) {
        Odoo.searchRead("sale.order", SaleOrder.fields,
                listOf(
                        listOf(param1, '=', param2)
                ), mAdapter.rowItemCount, limit, "date_order DESC") {
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
                        val items: ArrayList<SaleOrder> = gson.fromJson(searchRead.result.records, saleOrderListType)

                        if (items.size < limit) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchSalesFilter(param1, param2)
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

    override fun onResume() {
        super.onResume()
        mAdapter.clear()
        fetchSales(activeFilters)
    }

    private fun floatingActionButtonAddPressed() {
        val addSaleFragment = AddSaleFragment.newInstance()

        fragmentManager!!.beginTransaction()
                .replace(R.id.clMain, addSaleFragment, AddSaleFragment.ADD_SALE_FRAG_TAG)
                .addToBackStack(null)
                .commit()
    }

}
