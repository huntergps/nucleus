package com.domatix.yevbes.nucleus.sales.customer

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.ActivityCustomerListBinding
import com.domatix.yevbes.nucleus.jsonElementToString
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment

class CustomerListActivity : AppCompatActivity(), CustomerListFragment.OnClickCustomerListener {
    companion object {
        const val TYPE: String = "Customer"
    }


    fun trimFalse(string: String): String {
        return if (string != "false")
            string
        else
            ""
    }

    override fun onClick(item: Any) {
        val customer = item as Customer
        val returnIntent = Intent()

        returnIntent.putExtra(AddSaleFragment.CUSTOMER_PRICELIST, customer.propertyProductPricelist.toString())
        returnIntent.putExtra(AddSaleFragment.CUSTOMER_ID, customer.id)
        returnIntent.putExtra(AddSaleFragment.CUSTOMER_NAME, customer.name)
        returnIntent.putExtra(AddSaleFragment.COMPANY_NAME, trimFalse(jsonElementToString(customer.companyId)
        ))
//        returnIntent.putExtra(AddSaleFragment.CUSTOMER_BILLING_ADDRESS, customer.fullAddress)
//        returnIntent.putExtra(AddSaleFragment.CUSTOMER_DELIVERY_ADDRESS, customer.fullAddress)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    lateinit var app: App private set
    private var type: Int = 0
    private val types = arrayOf(CustomerListFragment.Companion.CustomerType.Customer, CustomerListFragment.Companion.CustomerType.Contacts)
    lateinit var binding: ActivityCustomerListBinding private set

    private val customerListFragment: CustomerListFragment by lazy {
        CustomerListFragment.newInstance(types[type])
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(TYPE, 0)

        app = application as App
        binding = DataBindingUtil.setContentView(this, R.layout.activity_customer_list)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setTitle(R.string.app_name)

        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.clMain, customerListFragment, getString(R.string.action_contacts))
                    .commit()
        }
    }
}
