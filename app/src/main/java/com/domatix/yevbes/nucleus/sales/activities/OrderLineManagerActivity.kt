package com.domatix.yevbes.nucleus.sales.activities

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.sales.fragments.OrderEditFragment
import com.domatix.yevbes.nucleus.sales.fragments.OrderLineFragment
import com.domatix.yevbes.nucleus.sales.interfaces.FragmentToActivityListener
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import com.rd.PageIndicatorView


class OrderLineManagerActivity : AppCompatActivity(), FragmentToActivityListener {


    companion object {
        const val SELECTED_LIST = "SalesManagerSelectedList"
        const val SELECTED_LIST_POSITION = "SalesManagerSelectedListPosition"
        const val SALES_MANAGER_REQUEST_CODE = 3
    }

    private lateinit var viewPager: ViewPager
    private lateinit var selectedItems: ArrayList<SaleOrderLine>
    private var maxSize: Int = 0
    private lateinit var adapterViewPager: MyPagerAdapter
    private var groupDiscountPerSoLine: Boolean = false

    override fun onFragmentStop(saleOrderLine: SaleOrderLine) {
        selectedItems[viewPager.currentItem] = saleOrderLine
    }

    private fun getPreferences() {
        val sharedPref = getSharedPreferences(getString(R.string.preference_fle_key_res_config_settings),Context.MODE_PRIVATE) ?: return
        groupDiscountPerSoLine = sharedPref.getBoolean(getString(R.string.saved_group_discount_per_so_line), false)
    }

    fun getGroupDiscountPerSoLine(): Boolean{
        return groupDiscountPerSoLine
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_manager)
        setSupportActionBar(findViewById(R.id.tb))

        getPreferences()

        supportActionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        findViewById<Toolbar>(R.id.tb).setNavigationOnClickListener {
            showDialogOK(getString(R.string.dialog_message),
                    DialogInterface.OnClickListener { _, which ->
                        when (which) {
                            DialogInterface.BUTTON_POSITIVE -> {
                                onBackPressed()
                            }
                            DialogInterface.BUTTON_NEGATIVE -> {

                            }
                        }
                    })
        }

        setTitle(R.string.order_line_manager_title)

        var position = 0

        if (!intent.extras.isEmpty) {
            val selectedItemsJSONString = intent.extras.getString(SELECTED_LIST)
            val selectedItemsGson = Gson()
            selectedItems = selectedItemsGson.fromJson(selectedItemsJSONString, object : TypeToken<ArrayList<SaleOrderLine>>() {
            }.type)
            position = intent.extras.getInt(SELECTED_LIST_POSITION, 0)
        }

        val fragmentArray = ArrayList<OrderLineFragment>()

        for (i in 0 until selectedItems.size) {
            val saleOrderLineItemGson = Gson()
            val saleOrderLineAsString = saleOrderLineItemGson.toJson(selectedItems[i])
            val bundle = Bundle()
            bundle.putString(SELECTED_LIST, saleOrderLineAsString)
            val orderLineFragment = OrderLineFragment.newInstance()
            orderLineFragment.setFragmentToActivityListener(this)
            orderLineFragment.arguments = bundle
            fragmentArray.add(orderLineFragment)
        }

        maxSize = selectedItems.size
        viewPager = findViewById(R.id.viewPager)
        adapterViewPager = MyPagerAdapter(supportFragmentManager, fragmentArray)
        viewPager.adapter = adapterViewPager
        viewPager.currentItem = position

        val pageIndicatorView = findViewById<PageIndicatorView>(R.id.pageIndicatorView)
        pageIndicatorView.setViewPager(viewPager)
        //viewPager.currentItem = position
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sales_manager_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        /*val fr = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.currentItem)*/

        if (item!!.itemId == R.id.action_confirm) {
            var fr: OrderLineFragment
            val hashMap = HashMap<Int, SaleOrderLine>()
            val count = selectedItems.size - 1
            //selectedSaleOrderLineItems.clear()


            for (i in 0.rangeTo(count)) {
                val fm = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + i)
                if (fm != null && (fm as OrderLineFragment).isModified()!!) {
                    fr = fm

                    val jsonArray = JsonArray()
                    jsonArray.add(fr.getProductProductObj()!!.id)
                    jsonArray.add(fr.getProductProductObj()!!.name)

                    hashMap[fr.getPosition()!!] = SaleOrderLine(
                            fr.getSaleOrderLine()!!.id,
                            fr.getDescription()!!,
                            jsonArray,
                            fr.getQuantity()!!,
                            fr.getDiscount()!!,
                            fr.getUnitPrice()!!,
                            fr.getPriceTotal()!!,
                            fr.getPriceTotal()!!,
                            fr.getProductProductObj()!!.taxesId)
                }
            }

            val returnIntent = Intent()
            val selectedItemsGson = Gson()

            val selectedItemsAsString = selectedItemsGson.toJson(hashMap)

            returnIntent.putExtra(OrderEditFragment.SELECTED_LIST, selectedItemsAsString)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        /*if (item!!.itemId == R.id.action_confirm) {
            val handler = Handler()
            handler.post {
                var flag = true
                while (flag) {
                    flag = fr.isVisible && !fr.isDetached
                }
                val returnIntent = Intent()
                val selectedItemsGson = Gson()
                val selectedItemsAsString = selectedItemsGson.toJson(selectedSaleOrderLineItems, object : TypeToken<ArrayList<SaleOrderLine>>() {
                }.type)
                returnIntent.putExtra(OrderEditFragment.SELECTED_LIST,selectedItemsAsString)
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }*/
        return super.onOptionsItemSelected(item)
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.ok_positive_button), okListener)
                .setNegativeButton(getString(R.string.cancel_negative_button), okListener)
                .create()
                .show()
    }

    private class MyPagerAdapter(fragmentManager: FragmentManager, var fragments: ArrayList<OrderLineFragment> /*, val listener: FragmentToActivityListener*/) : FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return fragments.size
        }


        override fun getItem(position: Int): Fragment? {
            /*return when(position){
                0 -> {
                    OrderLineFragment()
                }
                else -> {
                    null
                }
            }*/
            /*val saleOrderLineItemGson = Gson()
            val saleOrderLineAsString = saleOrderLineItemGson.toJson(selectedSaleOrderLineItems[position])
            val bundle = Bundle()
            bundle.putString(SELECTED_LIST, saleOrderLineAsString)
            val orderLineFragment = OrderLineFragment()
            orderLineFragment.setFragmentToActivityListener(listener)
            orderLineFragment.arguments = bundle
            return orderLineFragment*/
            val frag = fragments[position]
            frag.setPosition(position)
            return frag
        }
        /*val saleOrderLineItemGson = Gson()
        val saleOrderLineAsString = saleOrderLineItemGson.toJson(selectedSaleOrderLineItems[position])*/
        /*return OrderLineFragment.newInstance("")
    }*/

    }

}
