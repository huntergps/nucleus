package com.domatix.yevbes.nucleus.sales.customer

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.RECORD_LIMIT
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.FragmentCustomerListBinding
import com.domatix.yevbes.nucleus.errorBodySpanned
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sales.interfaces.LongShortItemClick
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable


class CustomerListFragment : Fragment(), SearchView.OnQueryTextListener {
    interface OnClickCustomerListener {
        fun onClick(item: Any)
    }

    /* My code */
    companion object {

        enum class CustomerType {
            /*Customer,
            Supplier,
            Company
            Contacts*/
            Customer,
            Contacts
        }

        private var listener: OnClickCustomerListener? = null

        private const val TYPE = "type"

        fun newInstance(customerType: CustomerType) =
                CustomerListFragment().apply {
                    arguments = Bundle().apply {
                        putString(TYPE, customerType.name)
                    }
                }

    }

    lateinit var activity: CustomerListActivity private set
    lateinit var binding: FragmentCustomerListBinding private set
    lateinit var compositeDisposable: CompositeDisposable private set

    private lateinit var customerType: CustomerType

    val adapter: CustomerAdapter by lazy {
        CustomerAdapter(this, arrayListOf(), object : LongShortItemClick {
            override fun onItemLongPress() {
            }

            override fun onItemClick(item: Any) {
                listener?.onClick(item)
            }

        })
    }

    private val customerListType = object : TypeToken<ArrayList<Customer>>() {}.type
    private val limit = RECORD_LIMIT * 2

    override fun onAttach(context: Context?) {
        if (context is OnClickCustomerListener) {
            listener = context
        }
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        compositeDisposable = CompositeDisposable()

        // Inflate the layout for this fragment
        binding = FragmentCustomerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as CustomerListActivity
        arguments?.let {
            customerType = CustomerType.valueOf(it.getString(TYPE))
        }

        // Hiding MainActivity's AppBarLayout as well as NestedScrollView first
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        when (customerType) {
            Companion.CustomerType.Customer -> {
                activity.setTitle(R.string.action_customer)
            }
            Companion.CustomerType.Contacts -> {
                activity.setTitle(R.string.action_contacts)
            }
        }
        activity.setSupportActionBar(binding.tb)
        val actionBar = activity.supportActionBar

        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        binding.tb.setNavigationOnClickListener {
            activity.onBackPressed()
        }


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
                when (customerType) {
                    Companion.CustomerType.Customer -> {
                        fetchCustomer("customer", true)
                    }
                    Companion.CustomerType.Contacts -> {
                        fetchCustomer()
                    }
                }
            }
        }

        binding.srl.setOnRefreshListener {
            adapter.clear()
            if (!adapter.hasMoreListener()) {
                adapter.showMore()
                when (customerType) {
                    Companion.CustomerType.Customer -> {
                        fetchCustomer("customer", true)
                    }
                    Companion.CustomerType.Contacts -> {
                        fetchCustomer()
                    }
                }
            }
            binding.srl.post {
                binding.srl.isRefreshing = false
            }
        }

        if (adapter.rowItemCount == 0) {
            adapter.showMore()
            when (customerType) {
                Companion.CustomerType.Customer -> {
                    fetchCustomer("customer", true)
                }
                Companion.CustomerType.Contacts -> {
                    fetchCustomer()
                }
            }
        }

        binding.rv.adapter = adapter
    }


    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }


    private fun fetchCustomer(param1: String?, param2: Boolean?) {
        Odoo.searchRead("res.partner", Customer.fields,
                listOf(
                        listOf(param1, "=", param2)
                )
                , adapter.rowItemCount, 0, "name ASC") {

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
                                    fetchCustomer(param1, param2)
                                }
                            }
                        }

                        adapter.addRowItems(items)
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
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
        }
    }

    private fun fetchCustomer() {
        Odoo.searchRead("res.partner", Customer.fields,

                listOf()

                , adapter.rowItemCount, 0, "name ASC") {

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
                                    fetchCustomer()
                                }
                            }
                        }
                        adapter.addRowItems(items)
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
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
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_sale_suppliers, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            adapter.filter(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            adapter.filter(newText)
        }
        return true
    }


}

