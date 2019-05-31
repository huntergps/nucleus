package com.domatix.yevbes.nucleus.sales.fragments


import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentSaleOrderProfileBinding
import com.domatix.yevbes.nucleus.sales.activities.SaleDetailActivity
import com.domatix.yevbes.nucleus.sales.adapters.SaleOrderLineDataAdapter
import com.domatix.yevbes.nucleus.sales.entities.SaleOrder
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 *
 */
class SaleOrderProfileFragment : Fragment() {

    companion object {
        const val SALE_ORDER_PROFILE_FRAG_TAG: String = "SaleOrderProfileFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SaleOrderProfileFragment.
         */

        @JvmStatic
        fun newInstance(param1: String) =
                SaleOrderProfileFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    private val mAdapter: SaleOrderLineDataAdapter by lazy {
        SaleOrderLineDataAdapter(this, arrayListOf())
    }

    private val saleOrderLineListType = object : TypeToken<ArrayList<SaleOrderLine>>() {}.type
    private val saleOrderType = object : TypeToken<SaleOrder>() {}.type
    lateinit var binding: FragmentSaleOrderProfileBinding
    private lateinit var saleOrderGsonAsAString: String
    private lateinit var saleOrder: SaleOrder
    private lateinit var mOptionMenu: Menu
    private var saleOrderId: Int? = null

    lateinit var compositeDisposable: CompositeDisposable private set

    private lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var activity: SaleDetailActivity private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        saleOrderGsonAsAString = arguments!!.getString(ARG_PARAM1)

        val saleOrderGson = Gson()
        saleOrder = saleOrderGson.fromJson(saleOrderGsonAsAString, SaleOrder::class.java)
        saleOrderId = saleOrder.id
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_sale_order_profile, container, false)
        // Inflate the layout for this fragment
        compositeDisposable = CompositeDisposable()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sale_order_profile, container, false)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        representSaleOrderData()
        activity = getActivity() as SaleDetailActivity

        val mLayoutManager = LinearLayoutManager(context)
        binding.saleOrderLineRecyclerView.layoutManager = mLayoutManager
        binding.saleOrderLineRecyclerView.itemAnimator = DefaultItemAnimator()
        mAdapter.setupScrollListener(binding.saleOrderLineRecyclerView)


//        activity.setTitle(R.string.action_sales)
        activity.title = getString(R.string.sale_name_title,saleOrder.name)

        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        activity.setSupportActionBar(binding.tb)
        val actionBar = activity.supportActionBar

        actionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tb.setNavigationOnClickListener {
            activity.onBackPressed()
        }
      /*  val actionBar = activity.supportActionBar
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }*/

        /*activity.binding.nv.menu.findItem(R.id.nav_sales).isChecked = true

        drawerToggle = ActionBarDrawerToggle(activity, activity.binding.dl,
                binding.tb, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        activity.binding.dl.addDrawerListener(drawerToggle)
        drawerToggle.syncState()*/
    }

    private fun representSaleOrderData() {
        val date = fromStringToDate(saleOrder.dateOrder, "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateOrder = getDateToFriendlyFormat(date, "dd MMM", Locale.getDefault(), TimeZone.getTimeZone("GMT+01:00")).toLowerCase()
        val amountTotal = "%.2f".format(saleOrder.amountTotal).replace('.', '%').replace(',', '.').replace('%', ',')
        val amountUntaxed = "%.2f".format(saleOrder.amountUntaxed).replace('.', '%').replace(',', '.').replace('%', ',')
        val amountTax = "%.2f".format(saleOrder.amountTax).replace('.', '%').replace(',', '.').replace('%', ',')
        val state = saleStates(saleOrder.state, this)


        binding.saleOrderObj = saleOrder
        binding.stateString = state
        binding.dateOrderString = dateOrder
        binding.amountUntaxedString = amountUntaxed
        binding.amountTaxString = amountTax
        binding.amountTotalString = amountTotal
        binding.termsString = saleOrder.terms
        binding.saleOrderLineRecyclerView.adapter = mAdapter
    }


    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (::drawerToggle.isInitialized) {
            drawerToggle.onConfigurationChanged(newConfig)
        }
    }

    override fun onStart() {
        updateSaleOrder()
//        activity.binding.nv.menu.findItem(R.id.nav_sales).isChecked = true
        super.onStart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
//        activity.binding.nv.menu.findItem(R.id.nav_sales).isChecked = false
        mAdapter.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_sale_order, menu)
        mOptionMenu = menu!!
        if (saleOrder.state == "done" || saleOrder.state == "sale" || saleOrder.state == "cancel") {
            menu?.findItem(R.id.action_edit)?.isVisible = false
            menu?.findItem(R.id.action_edit)?.isEnabled = false
            menu?.findItem(R.id.action_edit)?.isCheckable = false
        }

        if (saleOrder.state == "draft" || saleOrder.state == "sent") {
            menu?.findItem(R.id.action_confirm)?.isVisible = true
            menu?.findItem(R.id.action_confirm)?.isEnabled = true
            menu?.findItem(R.id.action_confirm)?.isCheckable = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {
                R.id.action_edit -> {
                    val orderEditFragment = OrderEditFragment.newInstance(saleOrderGsonAsAString)
                    fragmentManager!!.beginTransaction()
                            .replace(R.id.clMain, orderEditFragment, OrderEditFragment.ORDER_EDIT_FRAG_TAG)
                            .addToBackStack(SALE_ORDER_PROFILE_FRAG_TAG)
                            .commit()
                }

                R.id.action_confirm -> {
                    Odoo.callKw("sale.order", "action_confirm", listOf(saleOrderId!!)) {
                        onSubscribe { disposable ->
                            compositeDisposable.add(disposable)
                        }

                        onError { error ->
                            error.printStackTrace()
                        }

                        onComplete {
                            fetchSaleOrderLines("order_id", saleOrderId!!)
                            Toast.makeText(activity, getString(R.string.sale_order_confirmed), Toast.LENGTH_SHORT).show()

                            mOptionMenu.findItem(R.id.action_edit)?.isVisible = false
                            mOptionMenu.findItem(R.id.action_edit)?.isEnabled = false
                            mOptionMenu.findItem(R.id.action_edit)?.isCheckable = false

                            mOptionMenu.findItem(R.id.action_confirm)?.isVisible = false
                            mOptionMenu.findItem(R.id.action_confirm)?.isEnabled = false
                            mOptionMenu.findItem(R.id.action_confirm)?.isCheckable = false

                            binding.state.text = saleStates("done", this@SaleOrderProfileFragment)

                        }

                    }
                }

                else -> {

                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchSaleOrderLines(param1: String?, param2: Any?) {
        Odoo.searchRead("sale.order.line",
                SaleOrderLine.fields,
                listOf(
                        listOf(param1, '=', param2)
                )
                , mAdapter.rowItemCount, 0) {
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
                        val items: ArrayList<SaleOrderLine> = gson.fromJson(searchRead.result.records, saleOrderLineListType)
                        if (items.size == 0 && mAdapter.rowItemCount == 0) {
                            mAdapter.showEmpty()
                        }
                        /* if (items.size < limit) {
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
                         }*/
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

    private fun updateSaleOrder() {
        representSaleOrderData()
        Odoo.load(id = saleOrderId!!, model = "sale.order", fields = SaleOrder.fields) {
            onSubscribe {
                disposable -> compositeDisposable.add(disposable)
            }
            onNext { response ->
                if (response.isSuccessful) {
                    val load = response.body()!!
                    if (load.isSuccessful) {
                        val item: SaleOrder = gson.fromJson(load.result.value, saleOrderType)
                        saleOrder = item
                        representSaleOrderData()
                        fetchSaleOrderLines("order_id", saleOrderId)
                    } else {
                        // Odoo specific error
                        Timber.w("load() failed with ${load.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {

            }

        }
    }

}
