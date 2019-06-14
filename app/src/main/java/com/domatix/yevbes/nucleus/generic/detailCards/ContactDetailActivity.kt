package com.domatix.yevbes.nucleus.generic.detailCards

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.CustomerProfileActivityBinding
import com.domatix.yevbes.nucleus.generic.ui.dialogs.CustomDialogFragment
import com.domatix.yevbes.nucleus.gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

const val MODEL_ITEM_ID = "MODEL_ITEM_ID"
class ContactDetailActivity : AppCompatActivity() {
    lateinit var binding: CustomerProfileActivityBinding
    lateinit var compositeDisposable: CompositeDisposable private set
    lateinit var customDialogFragment: CustomDialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compositeDisposable = CompositeDisposable()
        customDialogFragment = CustomDialogFragment.newInstance(this,supportFragmentManager,"TAG",
            getString(R.string.loading),getString(R.string.loading_data),cancelable = false,showInstantly = false)
        val customerId = intent.extras.getInt(MODEL_ITEM_ID)
        loadCustomerById(customerId)
    }

    private fun loadCustomerById(id: Int) {
        customDialogFragment.showDialog()

        var customer: Customer? = null
        Odoo.load(id = id, model = "res.partner", fields = Customer.fields) {
            onSubscribe {
                compositeDisposable.add(it)
            }
            onNext {
                if (it.isSuccessful) {
                    val load = it.body()!!
                    if (load.isSuccessful) {
                        val result = load.result
                        customer = gson.fromJson<Customer>(result.value, object : TypeToken<Customer>(){
                        }.type)
                    } else {
                        Timber.w("load() failed with ${load.errorMessage}")
                    }
                } else {
                    Timber.w("request failed with ${it.code()}:${it.message()}")
                }
            }
            onError {
                it.printStackTrace()
            }
            onComplete {
                customDialogFragment.dismissDialog()
                customer?.let { configureUI(it) }
                if (customer == null){
                    onBackPressed()
                }
            }
        }
    }

    private fun configureUI(customer: Customer) {
        binding = DataBindingUtil.setContentView(this, R.layout.customer_profile_activity)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.customer = customer
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

}
