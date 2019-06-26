package com.domatix.yevbes.nucleus.sales.activities

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.databinding.ActivityOrderLinesManagerBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.sales.fragments.OrderLinePageViewFragment
import com.domatix.yevbes.nucleus.sales.fragments.SELECTED_ITEM
import com.google.gson.reflect.TypeToken

class OrderLinesManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderLinesManagerBinding
    private lateinit var selectedItems: ArrayList<SaleOrderLine>

    companion object {
        const val SELECTED_LIST = "SELECTED_LIST"
        const val SELECTED_LIST_POSITION = "SELECTED_LIST_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        intent.extras?.let {
            val selectedItemsJSONString = it.getString(SELECTED_LIST)
            selectedItems = gson.fromJson(selectedItemsJSONString, object : TypeToken<ArrayList<SaleOrderLine>>() {
            }.type)

            val position = it.getInt(SELECTED_LIST_POSITION, 0)
            binding.viewPager.currentItem = position
        }

        binding.viewPager.adapter = MyPagerAdapter(supportFragmentManager, generateFragments())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sales_manager_activity, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_confirm) {

        }
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

    private fun setupUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_order_lines_manager)

        setSupportActionBar(binding.tb)
        supportActionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.tb.setNavigationOnClickListener {
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

        binding.pageIndicatorView.setViewPager(binding.viewPager)
    }

    private fun generateFragments() : ArrayList<OrderLinePageViewFragment>{
        val fragmentArray = ArrayList<OrderLinePageViewFragment>()

        for (i in 0 until selectedItems.size) {
            val bundle = Bundle()
            val item = selectedItems[i]
            val saleOrderLineAsString = gson.toJson(item)
            bundle.putString(SELECTED_ITEM, saleOrderLineAsString)

            val orderLineFragment = OrderLinePageViewFragment()
//            orderLineFragment.setFragmentToActivityListener(this)
            orderLineFragment.arguments = bundle
            fragmentArray.add(orderLineFragment)
        }
        return fragmentArray
    }

    private inner class MyPagerAdapter(fragmentManager: FragmentManager, val fragments: ArrayList<OrderLinePageViewFragment>) : FragmentStatePagerAdapter(fragmentManager) {
        override fun getItem(position: Int): Fragment {
            val frag = fragments[position]
            return frag
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//            super.destroyItem(container, position, `object`)
        }

        override fun getCount(): Int {
            return selectedItems.size
        }

    }
}
