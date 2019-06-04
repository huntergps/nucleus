package com.domatix.yevbes.nucleus.customer

import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.FragmentCustomerBinding
import com.domatix.yevbes.nucleus.generic.ui.dialogs.LoadingDialogFragment
import com.domatix.yevbes.nucleus.utils.PreferencesManager
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable

class CustomerFragment : Fragment(), SearchView.OnQueryTextListener {

    /* My code */
    companion object {

        enum class CustomerType {
            /*Customer,
            Supplier,
            Company*/
            Contacts
        }

//        private var items: HashMap<Int, String> = HashMap()
//        private var actualItemConditions: HashMap<String?, Boolean> = HashMap()

//        private var actualItem: MenuItem? = null
//        private var actualBoolean: Boolean? = true

        //        private var menuContacts: Menu? = null
        private const val TYPE = "type"

        fun newInstance(customerType: CustomerType) =
                CustomerFragment().apply {
                    arguments = Bundle().apply {
                        putString(TYPE, customerType.name)
                    }
                }
    }

    lateinit var activity: MainActivity private set
    lateinit var binding: FragmentCustomerBinding private set
    lateinit var compositeDisposable: CompositeDisposable private set
    lateinit var generalItems: ArrayList<Customer> private set
    private lateinit var query: String

    private var firstTime: Boolean = true


    private lateinit var customerType: CustomerType
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var prefs: PreferencesManager

    // Filters
    private val items: ArrayList<Any> = ArrayList()
    private val activeFilters: ArrayList<Any> = ArrayList()
    private var filterGroup = intArrayOf(0, 0)

    private val checkedItems = booleanArrayOf(false, false, false, false)

    val adapter: CustomerAdapter by lazy {
        CustomerAdapter(this, arrayListOf())
    }

    private val customerListType = object : TypeToken<ArrayList<Customer>>() {}.type
    private val limit = RECORD_LIMIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        generalItems = ArrayList()
        prefs = PreferencesManager

        items.add(0, listOf(getString(R.string.action_filter_companies_param), '=', true))
        items.add(1, listOf(getString(R.string.action_filter_persons_param), '=', false))
        items.add(2, listOf(getString(R.string.action_filter_customers_param), '=', true))
        items.add(3, listOf(getString(R.string.action_filter_suppliers_param), '=', true))
        items.add(4, listOf("parent_id", '=', false))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        compositeDisposable = CompositeDisposable()
        // Inflate the layout for this fragment
        binding = FragmentCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        activity = getActivity() as MainActivity
        activity.setTitle(R.string.action_contacts)

        arguments?.let {
            customerType = CustomerType.valueOf(it.getString(TYPE))
        }

        // Hiding MainActivity's AppBarLayout as well as NestedScrollView first
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        activity.binding.nv.menu.findItem(R.id.nav_contacts).isChecked = true

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

        val layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.VERTICAL, false
        )

        binding.rv.layoutManager = layoutManager
        binding.rv.addItemDecoration(
                DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
        )

        adapter.setupScrollListener(binding.rv)

        if (!adapter.hasRetryListener()) {
            adapter.retryListener {
                if (!::query.isInitialized || query.isEmpty())
                    fetchCustomer(activeFilters)
                else
                    queryFetchCustomer(query)
            }
        }

        binding.srl.setOnRefreshListener {
            adapter.clear()
            generalItems.clear()
            if (!adapter.hasMoreListener()) {
                adapter.showMore()
                if (!::query.isInitialized || query.isEmpty())
                    fetchCustomer(activeFilters)
                else
                    queryFetchCustomer(query)
            }

            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        adapter.clear()
        if (!::query.isInitialized || query.isEmpty()) {
            if (adapter.rowItemCount == 0) {
                adapter.showMore()
                fetchCustomer(activeFilters)
            }
        } else {
            if (adapter.rowItemCount == 0) {
                adapter.showMore()
                queryFetchCustomer(query)
            }
        }

        binding.rv.adapter = adapter
//        fetchCustomer()
    }

    override fun onStart() {
        super.onStart()
//        prefs.clearPreferences()
//        fetchCustomer("customer",true)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        activity.binding.nv.menu.findItem(R.id.nav_contacts).isChecked = false
        super.onDestroyView()
    }

    private fun queryFetchCustomer(query: String?) {
        Odoo.searchRead("res.partner", Customer.fields,
                listOf(
                        '|',
                        '|',
                        listOf("display_name", "ilike", query),
                        listOf("ref", '=', query),
                        listOf("email", "ilike", query)
                ),
                adapter.rowItemCount, RECORD_LIMIT, "name ASC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        adapter.hideEmpty()
                        adapter.hideError()
                        adapter.hideMore()
                        val items: ArrayList<Customer> = gson.fromJson(searchRead.result.records, customerListType)

                        if (items.size < limit) {
                            adapter.removeMoreListener()
                            if (items.size == 0 && adapter.rowItemCount == 0) {
                                adapter.showEmpty()
                            }
                        } else {
                            if (!adapter.hasMoreListener()) {
                                adapter.moreListener {
                                    queryFetchCustomer(query)
                                }
                            }
                        }
                        adapter.addRowItems(items)
                        generalItems.addAll(items)
                    } else {
                        adapter.showError(searchRead.errorMessage)
                    }
                } else {
                    adapter.showError(response.errorBodySpanned)
                }
                adapter.finishedMoreLoading()
            }

            onError { error ->
                error.printStackTrace()
                adapter.showError(error.message ?: getString(R.string.generic_error))
                adapter.finishedMoreLoading()
            }

            onComplete {
                firstTime = false
            }
        }
    }

    /*private fun fetchCustomer() {
        Odoo.searchRead("res.partner", Customer.fields,
                        listOf()

                , 0, 0) {

            onSubscribe { disposable ->
    //                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        val items: ArrayList<Customer> = gson.fromJson(searchRead.result.records, customerListType)
                        generalItems.addAll(items)
                    }
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                firstTime = false
            }
        }
    }*/
    private fun addOrToFilter(param1: Any, firstElement: Any?, numOfOrs: Int): ArrayList<Any> {
        val auxArray = ArrayList<Any>()

        auxArray.add(0, '|')
        auxArray.addAll(param1 as Collection<Any>)

        // Add first element at end of the list
        if (firstElement != null) {
            auxArray.add(firstElement)
        }
        return auxArray
    }

    private fun addAndToFilter(param1: Any, firstElement: Any?, numOfOrs: Int): ArrayList<Any> {
        val auxArray = ArrayList<Any>()

        auxArray.add(0, '&')
        auxArray.addAll(param1 as ArrayList<*>)

        // Add first element at end of the list
        if (firstElement != null) {
            auxArray.add(firstElement)
        }
        return auxArray
    }

    private fun fetchCustomer(param1: Any) {
        Odoo.searchRead("res.partner", Customer.fields,
                if (!(param1 as ArrayList<*>).isEmpty()) {
                    if ((filterGroup[0] > 0 && filterGroup[1] == 0) || (filterGroup[0] == 0 && filterGroup[1] > 0)) {
                        val cond = if (filterGroup[0] != 0) filterGroup[0] else filterGroup[1]
                        when (cond) {
                            1 -> {
                                param1
                            }
                            2 -> {
                                addOrToFilter(param1, null, 1)
                            }
                            else -> {
                                // No se da ningún caso
                                listOf<Any>()
                            }
                        }
                    } else if (filterGroup[0] > 0 && filterGroup[1] > 0) {
                        when (filterGroup[0]) {
                            1 -> {
                                when (filterGroup[1]) {
                                    1 -> {
                                        param1
                                    }
                                    2 -> {
                                        addOrToFilter(param1.subList(1, param1.size), param1[0], 1)
                                    }
                                    else -> {
                                        // No se da ningún caso
                                        listOf<Any>()
                                    }
                                }
                            }
                            2 -> {
                                when (filterGroup[1]) {
                                    1 -> {
                                        addOrToFilter(param1, null, 1)
                                    }
                                    2 -> {
                                        val aux = addOrToFilter(param1, null, 2)
                                        addOrToFilter(aux.subList(3, aux.size), aux.subList(0, 3), 1)
                                    }
                                    else -> {
                                        // No se da ningún caso
                                        listOf<Any>()
                                    }
                                }
                            }
                            else -> {
                                // No se da ningún caso
                                listOf<Any>()
                            }
                        }
                    } else {
                        // No se da ningún caso
                        listOf<Any>()
                    }
                } else {
                    // No hay ningún filtro marcado, sacar todos los datos
                    listOf<Any>()
                }

                , adapter.rowItemCount, RECORD_LIMIT, "name ASC") {

            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        adapter.hideEmpty()
                        adapter.hideError()
                        adapter.hideMore()
                        val items: ArrayList<Customer> = gson.fromJson(searchRead.result.records, customerListType)

                        if (items.size < limit) {
                            adapter.removeMoreListener()
                            if (items.size == 0 && adapter.rowItemCount == 0) {
                                adapter.showEmpty()
                            }
                        } else {
                            if (!adapter.hasMoreListener()) {
                                adapter.moreListener {
                                    fetchCustomer(param1)
                                }
                            }
                        }
                        adapter.addRowItems(items)
                        generalItems.addAll(items)
                    } else {
                        adapter.showError(searchRead.errorMessage)
                    }
                } else {
                    adapter.showError(response.errorBodySpanned)
                }
                adapter.finishedMoreLoading()
            }

            onError { error ->
                error.printStackTrace()
                adapter.showError(error.message ?: getString(R.string.generic_error))
                adapter.finishedMoreLoading()
            }

            onComplete {
                firstTime = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_contact, menu)

        val searchItem = menu!!.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.isChecked = !item?.isChecked!!

        when (item.itemId) {
            R.id.action_filter_companies -> {
                adapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[0])
                    filterGroup[0]++
                } else {
                    activeFilters.remove(items[0])
                    filterGroup[0]--
                }
                fetchCustomer(activeFilters)
            }

            R.id.action_filter_persons -> {
                adapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[1])
                    filterGroup[0]++
                } else {
                    activeFilters.remove(items[1])
                    filterGroup[0]--
                }
                fetchCustomer(activeFilters)
            }

            R.id.action_filter_customers -> {
                adapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[2])
                    filterGroup[1]++
                } else {
                    activeFilters.remove(items[2])
                    filterGroup[1]--
                }
                fetchCustomer(activeFilters)
            }

            R.id.action_filter_suppliers -> {
                adapter.clear()
                if (item.isChecked) {
                    activeFilters.add(items[3])
                    filterGroup[1]++
                } else {
                    activeFilters.remove(items[3])
                    filterGroup[1]--
                }
                fetchCustomer(activeFilters)
            }

            else -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
//            adapter.filter(query)
            adapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = query
            queryFetchCustomer(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
//            adapter.filter(newText)
            adapter.clear()
            compositeDisposable.dispose()
            compositeDisposable = CompositeDisposable()
            this.query = newText
            queryFetchCustomer(newText)
        }
        return true
    }

}
