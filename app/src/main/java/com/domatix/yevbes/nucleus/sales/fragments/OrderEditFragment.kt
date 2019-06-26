package com.domatix.yevbes.nucleus.sales.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.airbnb.lottie.LottieDrawable
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentOrderEditBinding
import com.domatix.yevbes.nucleus.generic.callbacs.OnThreadFinishedListener
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.dialogs.OnDialogButtonsClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.dialogs.OnDialogStartListener
import com.domatix.yevbes.nucleus.generic.ui.dialogs.CustomDialogFragment
import com.domatix.yevbes.nucleus.sales.activities.*
import com.domatix.yevbes.nucleus.sales.adapters.OrderEditAdapter
import com.domatix.yevbes.nucleus.sales.customer.CustomerListActivity
import com.domatix.yevbes.nucleus.sales.entities.CustomProductQtyEntity
import com.domatix.yevbes.nucleus.sales.entities.ProductPricelist
import com.domatix.yevbes.nucleus.sales.entities.SaleOrder
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.utils.MyProgressDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.DateTimeZone
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 *
 */
class OrderEditFragment : Fragment() {
    companion object {
        const val ORDER_EDIT_FRAG_TAG = "OrderEditFragment"
        const val NEW_LIST_ELEMENTS = "NEW_LIST_ELEMENTS"
        const val SELECTED_LIST = "SaleOrderLineSelectedList"
        const val CUSTOMER_ID = "CustomerId"
        const val CUSTOMER_NAME = "CustomerName"
        const val COMPANY_NAME = "CompanyName"
        const val REQUEST_CODE = 1
        const val SALES_MANAGER_REQUEST_CODE = 3
        const val CUSTOMER_REQUEST_CODE = 2
        const val INVOICE_REQUEST_CODE = 4
        const val SHIPPING_REQUEST_CODE = 5
        const val PRICELIST_REQUEST_CODE = 6

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment OrderEditFragment.
         */

        @JvmStatic
        fun newInstance(param1: String) =
                OrderEditFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }

    private var partnerInvoiceId: Int? = null
    private var partnerShippingId: Int? = null
    private var idPriceList: Int? = null
    private var isPricelistWithDiscount: Boolean? = null

    private lateinit var selectedItemsJSONString: String
    private lateinit var addedItemsJSONString: String
    private val saleOrderLineListType = object : TypeToken<ArrayList<SaleOrderLine>>() {}.type
    private val productPricelistType = object : TypeToken<ProductPricelist>() {}.type

    lateinit var removedItemsIdList: HashSet<Int> private set
    lateinit var addedItems: ArrayList<SaleOrderLine> private set
    lateinit var selectedSaleOrderLineItems: ArrayList<SaleOrderLine> private set
    lateinit var myProgressDialog: MyProgressDialog private set
    lateinit var progressDialog: ProgressDialog private set

    private lateinit var dialogFragment: CustomDialogFragment
    private var idCustomer: Int? = null
    lateinit var binding: FragmentOrderEditBinding
    private lateinit var saleOrderGsonAsAString: String

    lateinit var compositeDisposable: CompositeDisposable private set

    private lateinit var drawerToggle: ActionBarDrawerToggle
    lateinit var activity: SaleDetailActivity private set
    private var saleOrder: SaleOrder? = null

    private val mAdapter: OrderEditAdapter by lazy {
        OrderEditAdapter(binding.saleOrderLineRecyclerView, arrayListOf(), object : OnShortLongAdapterItemClickListener {
            override fun onShortAdapterItemPressed(view: View) {
               /* val position = binding.saleOrderLineRecyclerView.getChildAdapterPosition(view)
                val intent = Intent(activity, OrderLineManagerActivity::class.java)
                val bundle = Bundle()

                val aux = ArrayList<SaleOrderLine>()
                aux.addAll(selectedSaleOrderLineItems)
                aux.addAll(addedItems)

                bundle.putString(OrderLineManagerActivity.SELECTED_LIST, gson.toJson(aux))
                bundle.putInt(OrderLineManagerActivity.SELECTED_LIST_POSITION, position)
                intent.putExtras(bundle)

                startActivityForResult(intent, SALES_MANAGER_REQUEST_CODE)
                Timber.v("ITEM_CLICKED_$position")*/

                val position = binding.saleOrderLineRecyclerView.getChildAdapterPosition(view)
                val intent = Intent(activity, OrderLinesManagerActivity::class.java)
                val bundle = Bundle()

                val aux = ArrayList<SaleOrderLine>()
                aux.addAll(mAdapter.items as ArrayList<SaleOrderLine>)
//                aux.addAll(addedItems)

                bundle.putString(OrderLinesManagerActivity.SELECTED_LIST, gson.toJson(aux))
                bundle.putInt(OrderLinesManagerActivity.SELECTED_LIST_POSITION, position)
                intent.putExtras(bundle)

                startActivityForResult(intent, SALES_MANAGER_REQUEST_CODE)
                Timber.v("ITEM_CLICKED_$position")
            }

            override fun onLongAdapterItemPressed(view: View) {
                val position = binding.saleOrderLineRecyclerView.getChildAdapterPosition(view)
                val item = mAdapter.items[position] as SaleOrderLine

                dialogFragment.showDialog()
                dialogFragment.setOnDialogStartListener(object : OnDialogStartListener {
                    override fun onDialogStarted() {
                        dialogFragment.setTilte(getString(R.string.remove_element))
                        dialogFragment.setMessage(String.format(resources.getString(R.string.remove_element_message), item.name))

                        dialogFragment.setAnimation("removing.json", true, maxFrame = 57, loop = false)
                        dialogFragment.isVisibleDialogButtons(true)
                        dialogFragment.setCancelableDialog(true)

                        dialogFragment.setOnDialogButtonsClickListener(object : OnDialogButtonsClickListener {
                            override fun onPositiveButtonPressed() {
                                val removedItem = mAdapter.removeSaleOrderLineRowItem(position)
                                val id = removedItem.id
                                if (id != 0) {
                                    removedItemsIdList.add(id)
                                }

                                dialogFragment.dismissDialog()

                                Snackbar.make(binding.root, String.format(resources.getString(R.string.removed_element_message), removedItem.name), Snackbar.LENGTH_LONG)
                                        .setActionTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
                                        .setAction(R.string.undo) {
                                            mAdapter.restoreItem(removedItem, position)
                                            if (id != 0) {
                                                removedItemsIdList.remove(id)
                                            }
                                        }
                                        .show()
                            }

                            override fun onNegativeButtonPressed() {
                                dialogFragment.dismissDialog()
                            }
                        })
                    }
                })
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        if (!arguments!!.isEmpty) {
            saleOrderGsonAsAString = arguments!!.getString(ARG_PARAM1)
            val saleOrderGson = Gson()
            saleOrder = saleOrderGson.fromJson(saleOrderGsonAsAString, SaleOrder::class.java)
        }

        addedItems = ArrayList()
        removedItemsIdList = HashSet()
        progressDialog = ProgressDialog(context)
        //aux = ArrayList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        compositeDisposable = CompositeDisposable()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_edit, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity = getActivity() as SaleDetailActivity
        activity.title = getString(R.string.sale_name_title, saleOrder!!.name)

        dialogFragment = CustomDialogFragment.newInstance(
                activity,
                fragmentManager!!,
                showInstantly = false,
                playAnimation = true,
                loopAnimation = true,
                repeatCount = LottieDrawable.INFINITE,
                cancelable = false)

        checkForDiscountPolicy(saleOrder!!.pricelistId.asJsonArray[0].asInt)

        val mLayoutManager = LinearLayoutManager(context)
        binding.saleOrderLineRecyclerView.layoutManager = mLayoutManager
        binding.saleOrderLineRecyclerView.itemAnimator = DefaultItemAnimator()
        mAdapter.setupScrollListener(binding.saleOrderLineRecyclerView)

        partnerInvoiceId = saleOrder!!.partnerInvoiceId.asJsonArray.get(0).asInt
        partnerShippingId = saleOrder!!.partnerShippingId.asJsonArray.get(0).asInt
        idCustomer = saleOrder!!.partnerId.asJsonArray.get(0).asInt

        binding.saleOrder = saleOrder

        idPriceList = saleOrder!!.pricelistId.asJsonArray[0].asInt
        binding.saleOrderLineRecyclerView.adapter = mAdapter

        binding.buttonAddOrderSalesLine.setOnClickListener {
            if (!::isPricelistWithDiscount.isLateinit) {
                val intent = Intent(activity, OrderLineListActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE)
            }else{
                Toast.makeText(activity,"Price List not defined",Toast.LENGTH_SHORT).show()
            }
        }

        binding.priceList.setOnClickListener {
            val intent = Intent(activity, PricelistListActivity::class.java)
            startActivityForResult(intent, AddSaleFragment.PRICELIST_REQUEST_CODE)
        }

        activity = getActivity() as SaleDetailActivity
//        activity.setTitle(R.string.action_sales)
        activity.title = getString(R.string.sale_name_title, saleOrder!!.name)
        activity.binding.abl.visibility = View.GONE
        activity.binding.nsv.visibility = View.GONE

        activity.setSupportActionBar(binding.tb)

        setOnClickListeners(binding)
        fetchSaleOrderLines("order_id", saleOrder!!.id)
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
    }

    private fun getUnitPrice(tarifId: Int, productId: Int, quantity: Float, listener: OnThreadFinishedListener) {
        var priceWithTarifa: Float? = 0f
        Odoo.callKw(model = "product.pricelist", method = "price_get", args = listOf(tarifId,
                productId, quantity))
        {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val callKw = response.body()!!
                    if (callKw.isSuccessful) {
                        val result = callKw.result
                        priceWithTarifa = result.asJsonObject[tarifId.toString()].asFloat
                    } else {
                        // Odoo specific error
                        Timber.w("callkw() failed with ${callKw.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                listener.onThreadFinished(priceWithTarifa as Any)
            }
        }
    }

    private fun setOnClickListeners(binding: FragmentOrderEditBinding) {
        binding.dateOrder.setOnClickListener {
            getDate()
        }

        binding.partnerId.setOnClickListener {
            /* val intent = Intent(activity, CustomerListActivity::class.java)
             startActivityForResult(intent, OrderEditFragment.CUSTOMER_REQUEST_CODE)*/
            val intent = Intent(activity, CustomerListActivity::class.java)
            intent.putExtra(CustomerListActivity.TYPE, 0)
            startActivityForResult(intent, CUSTOMER_REQUEST_CODE)
        }

        binding.partnerInvoiceAddress.setOnClickListener {
            val intent = Intent(activity, CustomerListActivity::class.java)
            intent.putExtra(CustomerListActivity.TYPE, 1)
            startActivityForResult(intent, OrderEditFragment.INVOICE_REQUEST_CODE)
        }

        binding.partnerShippingAddress.setOnClickListener {
            val intent = Intent(activity, CustomerListActivity::class.java)
            intent.putExtra(CustomerListActivity.TYPE, 1)
            startActivityForResult(intent, OrderEditFragment.SHIPPING_REQUEST_CODE)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {
            PRICELIST_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val item = gson.fromJson<ProductPricelist>(data?.getStringExtra(AddSaleFragment.PRICELIST_ID), productPricelistType)

                        if (idPriceList != item.id) {
                            idPriceList = item.id
                            binding.priceList.text = item.displayName
                            checkForDiscountPolicy(item.id)
                        }

                    }
                }
            }

            REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val addedItemJSONString = data?.getStringExtra(NEW_LIST_ELEMENTS)
                        val addedList = gson.fromJson<List<CustomProductQtyEntity>>(addedItemJSONString, object : TypeToken<java.util.ArrayList<CustomProductQtyEntity>>() {
                        }.type)

                        val selectedItemsGson = Gson()
                        val auxSaleOrderLineList = ArrayList<SaleOrderLine>()
                        for (index in 0 until addedList.size) {
                            if (isPricelistWithDiscount!!) {
                                getUnitPrice(idPriceList!!, addedList[index].idProduct, addedList[index].quantity, object : OnThreadFinishedListener {
                                    override fun onThreadFinished(value: Any) {
                                        val price = value as Float
                                        val jsonArray = JsonArray()
                                        jsonArray.add(addedList[index].idProduct)
                                        jsonArray.add(addedList[index].name)

                                        auxSaleOrderLineList.add(SaleOrderLine(
                                                0,
                                                addedList[index].name,
                                                jsonArray,
                                                addedList[index].quantity,
                                                0f,
                                                addedList[index].lstPrice,
                                                price,
                                                addedList[index].lstPrice * addedList[index].quantity,
                                                addedList[index].taxesId
                                        ))
                                        addedItems.addAll(auxSaleOrderLineList)
                                        mAdapter.addSaleOrderLineRowItems(auxSaleOrderLineList)
                                        auxSaleOrderLineList.clear()
                                        addedItemsJSONString = selectedItemsGson.toJson(auxSaleOrderLineList)
                                    }
                                })
                            } else {
                                // TODO: implement get price without discount
                            }
                        }
                    }

                    Activity.RESULT_CANCELED -> {

                    }
                }
            }

            CUSTOMER_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        idCustomer = data?.getIntExtra(CUSTOMER_ID, 0)
                        partnerInvoiceId = idCustomer
                        partnerShippingId = idCustomer
//                        binding.billing.text = data?.getStringExtra(CUSTOMER_BILLING_ADDRESS)
//                        binding.delivery.text = data?.getStringExtra(CUSTOMER_DELIVERY_ADDRESS)
                        if (data?.getStringExtra(COMPANY_NAME) != "") {
                            binding.partnerId.text = "${data?.getStringExtra(COMPANY_NAME)}, ${data?.getStringExtra(CUSTOMER_NAME)}"
                            binding.partnerInvoiceAddress.text = "${data?.getStringExtra(COMPANY_NAME)}, ${data?.getStringExtra(CUSTOMER_NAME)}"
                            binding.partnerShippingAddress.text = "${data?.getStringExtra(COMPANY_NAME)}, ${data?.getStringExtra(CUSTOMER_NAME)}"
                        } else {
                            binding.partnerId.text = data?.getStringExtra(CUSTOMER_NAME)
                            binding.partnerInvoiceAddress.text = data?.getStringExtra(CUSTOMER_NAME)
                            binding.partnerShippingAddress.text = data?.getStringExtra(CUSTOMER_NAME)
                        }
                    }
                }
            }

            INVOICE_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        partnerInvoiceId = data?.getIntExtra(CUSTOMER_ID, 0)
//                        binding.billing.text = data?.getStringExtra(CUSTOMER_BILLING_ADDRESS)
//                        binding.delivery.text = data?.getStringExtra(CUSTOMER_DELIVERY_ADDRESS)
                        if (data?.getStringExtra(COMPANY_NAME) != "") {
                            binding.partnerInvoiceAddress.text = "${data?.getStringExtra(COMPANY_NAME)}, ${data?.getStringExtra(CUSTOMER_NAME)}"
                        } else {
                            binding.partnerInvoiceAddress.text = data?.getStringExtra(CUSTOMER_NAME)
                        }
                    }
                }
            }

            SHIPPING_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        partnerShippingId = data?.getIntExtra(CUSTOMER_ID, 0)
//                        binding.billing.text = data?.getStringExtra(CUSTOMER_BILLING_ADDRESS)
//                        binding.delivery.text = data?.getStringExtra(CUSTOMER_DELIVERY_ADDRESS)
                        if (data?.getStringExtra(COMPANY_NAME) != "") {
                            binding.partnerShippingAddress.text = "${data?.getStringExtra(COMPANY_NAME)}, ${data?.getStringExtra(CUSTOMER_NAME)}"
                        } else {
                            binding.partnerShippingAddress.text = data?.getStringExtra(CUSTOMER_NAME)
                        }
                    }
                }
            }

            OrderLineManagerActivity.SALES_MANAGER_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val selectedJSONString = data?.getStringExtra(OrderEditFragment.SELECTED_LIST)!!
                        val map: HashMap<Int, SaleOrderLine>

                        val type = object : TypeToken<Map<Int, SaleOrderLine>>() {

                        }.type
                        map = Gson().fromJson(selectedJSONString, type)

                        for (i in map.keys) {
                            if (i <= selectedSaleOrderLineItems.size - 1) {
                                selectedSaleOrderLineItems[i] = map[i]!!
                            } else {
                                val aux = i - selectedSaleOrderLineItems.size
                                addedItems[aux] = map[i]!!
                            }
                        }

                        /*selectedSaleOrderLineItems.addAll(selectedItemsGson.fromJson(selectedItemsJSONString, object : TypeToken<ArrayList<SaleOrderLine>>() {
                        }.type))*/
                        mAdapter.clear()
                        mAdapter.addSaleOrderLineRowItems(selectedSaleOrderLineItems)
                        mAdapter.addSaleOrderLineRowItems(addedItems)

                        //selectedItemsJSONString = selectedItemsGson.toJson(selectedSaleOrderLineItems)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkForDiscountPolicy(pricelistId: Int) {
        // Lock main thread
        dialogFragment.showDialog()
        dialogFragment.setOnDialogStartListener(object : OnDialogStartListener {
            override fun onDialogStarted() {
                dialogFragment.setTilte(getString(R.string.loading_data))
                dialogFragment.setMessage(getString(R.string.loading_discount_policy))
                dialogFragment.setAnimation("loading.json", true, loop = true, repeatCount = LottieDrawable.INFINITE)
            }
        })

        Odoo.read(model = "product.pricelist", ids = listOf(pricelistId), fields = listOf("discount_policy")) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val read = response.body()!!
                    if (read.isSuccessful) {
                        val result = read.result
                        val discountPolicy = result.asJsonArray[0].asJsonObject["discount_policy"].asString
                        when (discountPolicy) {
                            "with_discount" -> {
                                isPricelistWithDiscount = true
                            }
                            "without_discount" -> {
                                isPricelistWithDiscount = false
                            }
                        }
                    } else {
                        // Odoo specific error
                        Timber.w("read() failed with ${read.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
                dialogFragment.dismissDialog()
            }

            onComplete {
                dialogFragment.dismissDialog()
            }
        }
    }


    private fun getDate() {
        val zero = "0"
        val slash = "/"
        val c = Calendar.getInstance()

        val monthCal = c.get(Calendar.MONTH)
        val dayCal = c.get(Calendar.DAY_OF_MONTH)
        val yearCal = c.get(Calendar.YEAR)

        var actualMonth: Int
        var formattedDay: String
        var formattedMonth: String
        var formattedYear: Int

        val getDate = DatePickerDialog(context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            /*          actualMonth = month + 1
                     formattedYear = year
                      formattedDay = if (dayOfMonth < 10) zero + dayOfMonth.toString() else dayOfMonth.toString()
                      formattedMonth = if (actualMonth < 10) zero + actualMonth.toString() else actualMonth.toString()*/

//            binding.saleDate.text = formattedDay + slash + formattedMonth + slash + year
        },
                yearCal, monthCal, dayCal)
        getDate.setButton(DialogInterface.BUTTON_POSITIVE, "OK") { _, p1 ->
            if (p1 == DialogInterface.BUTTON_POSITIVE) {
                val datePicker = getDate.datePicker

                actualMonth = datePicker.month + 1
                formattedYear = datePicker.year
                formattedDay = if (datePicker.dayOfMonth < 10) zero + datePicker.dayOfMonth.toString() else datePicker.dayOfMonth.toString()
                formattedMonth = if (actualMonth < 10) zero + actualMonth.toString() else actualMonth.toString()
                binding.dateOrder.text = formattedDay + slash + formattedMonth + slash + formattedYear
                getTime()
            }
        }
        getDate.show()
    }

    private fun getTime() {
        val mCurrentTime = Calendar.getInstance()
        val hour = mCurrentTime.get(Calendar.HOUR_OF_DAY)
        val minute = mCurrentTime.get(Calendar.MINUTE)
        val second = mCurrentTime.get(Calendar.SECOND)

        val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { p0, p1, p2 ->
            binding.dateOrder.text = "${binding.dateOrder.text} ${checkDigit(p1)}:${checkDigit(p2)}:${checkDigit(second)}"
        }, hour, minute, true)

        timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { _, p1 ->
            if (p1 == DialogInterface.BUTTON_NEGATIVE) {
                showActualTime()
            }
        }

        timePickerDialog.setTitle(getString(R.string.select_time))
        timePickerDialog.show()
    }

    private fun checkDigit(number: Int): String {
        return if (number <= 9) {
            "0$number"
        } else {
            number.toString()
        }
    }

    private fun showActualTime() {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("HH:mm:ss", Locale.FRANCE)
        val formattedTime = df.format(c)
        binding.dateOrder.text = "${binding.dateOrder.text} $formattedTime"
    }

    private lateinit var menu: Menu

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_order_edit, menu)
        this.menu = menu!!
        menu.findItem(R.id.action_confirm).isEnabled = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_confirm -> {
                if (removedItemsIdList.isNotEmpty()) {
                    myProgressDialog = MyProgressDialog(progressDialog, getString(R.string.applying_changes_sale_order_dialog_title), getString(R.string.please_wait_dialog_message))
                    unlinkSaleOrderLines(removedItemsIdList)
                } else {
                    myProgressDialog = MyProgressDialog(progressDialog, getString(R.string.applying_changes_sale_order_dialog_title), getString(R.string.please_wait_dialog_message))
                    modifyOrder(saleOrder!!.id, partnerInvoiceId!!, partnerShippingId!!, idCustomer!!, binding.terms.text.toString())
                }
                activity.isSaleOrderModified(true)
            }
            R.id.action_cancel -> {
                showDialogOK(getString(R.string.dialog_message),
                        DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    activity.onBackPressed()
                                }
                                DialogInterface.BUTTON_NEGATIVE -> {

                                }
                            }
                        })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun unlinkSaleOrderLines(ids: HashSet<Int>) {
        if (::myProgressDialog.isInitialized)
            myProgressDialog.setProgressToProgressDialog(10)

        Odoo.unlink(model = "sale.order.line", ids = ids.toList()) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onComplete {
                modifyOrder(saleOrder!!.id, partnerInvoiceId!!, partnerShippingId!!, idCustomer!!, binding.terms.text.toString())
            }
        }
    }

    private fun modifyOrder(saleOrderId: Int, partnerInvoiceId: Int, partnerShippingId: Int, idCustomer: Int, conditions: String) {
        var date: Date
        val convertedDate: Date

        date = try {
            fromStringToDate(binding.dateOrder.text.toString(), "dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        } catch (e: java.text.ParseException) {
            fromStringToDate(saleOrder!!.dateOrder, "dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        }

        convertedDate = convertDateToSpecificTimeZone(date, TimeZone.getDefault(), DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00")))
        if (::myProgressDialog.isInitialized)
            myProgressDialog.setProgressToProgressDialog(20)

        Odoo.write(model = "sale.order", values = mapOf(
                "partner_id" to idCustomer,
                "partner_invoice_id" to partnerInvoiceId,
                "partner_shipping_id" to partnerShippingId,
                "note" to conditions,
                "date_order" to convertedDate
        ), ids = listOf(saleOrderId)) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val create = response.body()!!
                    if (create.isSuccessful) {
                        // Devuelve ID del pedido creado
                        val result = create.result
                        //res = result
                        /*   if (::selectedSaleOrderLineItems.isInitialized && selectedSaleOrderLineItems.isNotEmpty())
                               addOrderLines(result, context, selectedSaleOrderLineItems)*/
                    } else {
                        // Odoo specific error
                        Timber.w("create() failed with ${create.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                if (addedItems.size != 0)
                    addOrderLinesToDB(saleOrder!!.id, addedItems, 0)
                else {
                    if (::myProgressDialog.isInitialized)
                        myProgressDialog.setProgressToProgressDialog(60)

                    if (!selectedSaleOrderLineItems.isEmpty())
                        modifyOrderLinesInDB(selectedSaleOrderLineItems, 0)
                }

            }
        }
        /*Toast.makeText(context, context.getString(R.string.order_added), Toast.LENGTH_SHORT).show()
        fragmentManager?.popBackStack()*/
    }

    private fun addOrderLinesToDB(orderId: Int, addedItems: ArrayList<SaleOrderLine>, index: Int) {
        val item = addedItems[index]
        Odoo.create(model = "sale.order.line", values = mapOf(
                "order_id" to orderId,
                "product_id" to item.productId.asJsonArray.get(0).asLong,
                "product_uom_qty" to item.qty,
                "price_unit" to item.priceUnit,
                "name" to item.name,
                "discount" to item.discount
        )) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val create = response.body()!!
                    if (create.isSuccessful) {
                        val result = create.result
                    } else {
                        Timber.w("create() failed with ${create.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                val count = index + 1
                if (count <= addedItems.size - 1) {

                    if (::myProgressDialog.isInitialized)
                        myProgressDialog.setProgressToProgressDialog(myProgressDialog.getProgressDialog() + 40 / addedItems.size)
                    addOrderLinesToDB(orderId, addedItems, count)
                } else if (count == addedItems.size) {
                    if (selectedSaleOrderLineItems.isNotEmpty())
                        modifyOrderLinesInDB(selectedSaleOrderLineItems, 0)
                    else {
                        myProgressDialog.setProgressToProgressDialog(100)
                        myProgressDialog.dismissProgressDialog()
                        Toast.makeText(activity, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()
                        fragmentManager!!.popBackStack()
                    }
                }
            }
        }
    }

    // The last task in the process flow
    private fun modifyOrderLinesInDB(selectedItems: ArrayList<SaleOrderLine>, index: Int) {
        val item = selectedItems[index]
        Odoo.write("sale.order.line", listOf(item.id), mapOf(
                "product_id" to item.productId.asJsonArray.get(0).asLong,
                "product_uom_qty" to item.qty,
                "price_unit" to item.priceUnit,
                "name" to item.name,
                "discount" to item.discount
        )) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val write = response.body()!!
                    if (write.isSuccessful) {
                        val result = write.result
                        //Toast.makeText(activity, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()
                        //fragmentManager!!.popBackStack()
                    } else {
                        // Odoo specific error
                        Timber.w("write() failed with ${write.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                val count = index + 1
                if (count <= selectedItems.size - 1) {
                    modifyOrderLinesInDB(selectedItems, count)
                    myProgressDialog.setProgressToProgressDialog(myProgressDialog.getProgressDialog() + 40 / selectedItems.size)
                } else if (count == selectedItems.size) {
                    myProgressDialog.setProgressToProgressDialog(100)
                    myProgressDialog.dismissProgressDialog()
                    Toast.makeText(activity, getString(R.string.toast_changes_saved), Toast.LENGTH_SHORT).show()

                    /* val fragmentTag = fragmentManager!!.getBackStackEntryAt(fragmentManager!!.backStackEntryCount - 1).name
                     val currentFragment  = fragmentManager!!.findFragmentByTag(fragmentTag) as SaleOrderProfileFragment
                     currentFragment.updateSaleOrder()*/
                    fragmentManager!!.popBackStack()
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        android.support.v7.app.AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok_positive_button), okListener)
                .setNegativeButton(getString(R.string.cancel_negative_button), okListener)
                .create()
                .show()
    }

    private fun fetchSaleOrderLines(param1: String?, param2: Any?) {
        Odoo.searchRead("sale.order.line",
                SaleOrderLine.fields,
                listOf(
                        listOf(param1, '=', param2)
                )
                , mAdapter!!.rowItemCount, RECORD_LIMIT) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        mAdapter.hideEmpty()
                        mAdapter!!.hideError()
                        mAdapter!!.hideMore()

                        val items: ArrayList<SaleOrderLine> = gson.fromJson(searchRead.result.records, saleOrderLineListType)
                        selectedSaleOrderLineItems = items
                        val selectedItemsGson = Gson()
                        selectedItemsJSONString = selectedItemsGson.toJson(items)

                        menu.findItem(R.id.action_confirm).isEnabled = !(items.size == 0 && mAdapter!!.rowItemCount == 0)

                        if (items.size < RECORD_LIMIT) {
                            mAdapter.removeMoreListener()
                            if (items.size == 0 && mAdapter.rowItemCount == 0) {
                                mAdapter.showEmpty()
                            }
                        } else {
                            if (!mAdapter.hasMoreListener()) {
                                mAdapter.moreListener {
                                    fetchSaleOrderLines(param1, param2)
                                }
                            }
                        }
                        mAdapter!!.addSaleOrderLineRowItems(items)
                        compositeDisposable.dispose()
                        compositeDisposable = CompositeDisposable()
                    } else {
                        mAdapter!!.showError(searchRead.errorMessage)
                    }
                } else {
                    mAdapter!!.showError(response.errorBodySpanned)
                }
                mAdapter!!.finishedMoreLoading()
            }

            onError { error ->
                error.printStackTrace()
                mAdapter!!.showError(error.message ?: getString(R.string.generic_error))
                mAdapter!!.finishedMoreLoading()
            }
        }
    }
}

