package com.domatix.yevbes.nucleus.sales.activities

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.core.entities.session.authenticate.AuthenticateResult
import com.domatix.yevbes.nucleus.databinding.ActivitySaleDetailBinding
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment
import com.domatix.yevbes.nucleus.sales.fragments.SaleOrderProfileFragment
import com.domatix.yevbes.nucleus.sales.fragments.SalesFragment
import timber.log.Timber

class SaleDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivitySaleDetailBinding
    private var groupDiscountPerSoLine: Boolean = false
    private var saleNote: String? = null
    private var useSaleNote: Boolean = false

    companion object {
        const val FRAGMENT_TYPE = "FRAGMENT_TYPE"

        enum class FragmentType {
            AddFragment,
            DetailFragment
        }
    }

    fun getGroupDiscountPerSoLine(): Boolean {
        return groupDiscountPerSoLine
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sale_detail)
//        setContentView(R.layout.activity_sale_detail)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val sharePref = getSharedPreferences(getString(R.string.preference_fle_key_res_config_settings), Context.MODE_PRIVATE)
        sharePref.edit().clear().apply()
        getSaleSettings()

        setSupportActionBar(binding.tb)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tb.setNavigationOnClickListener {
            onBackPressed()
        }

        let {
            val saleOrderGsonAsAString = intent.extras.getString(SalesFragment.SALE_ORDER)
            val type = intent.getSerializableExtra(FRAGMENT_TYPE) as FragmentType
            when (type) {
                Companion.FragmentType.DetailFragment -> {
                    val saleOrderProfileFragment = SaleOrderProfileFragment.newInstance(saleOrderGsonAsAString)
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.clMain, saleOrderProfileFragment, SaleOrderProfileFragment.SALE_ORDER_PROFILE_FRAG_TAG)
                            .commit()
                }

                Companion.FragmentType.AddFragment -> {
                    val addSaleFragment = AddSaleFragment.newInstance()

                    supportFragmentManager!!.beginTransaction()
                            .replace(R.id.clMain, addSaleFragment, AddSaleFragment.ADD_SALE_FRAG_TAG)
                            .commit()
                }
            }
        }
    }

    private fun getSaleSettings() {
        Odoo.callKw(model = "res.users", method = "has_group", args = listOf("sale.group_discount_per_so_line")) {
            onNext { response ->
                if (response.isSuccessful) {
                    val callKw = response.body()!!
                    if (callKw.isSuccessful) {
                        val result = callKw.result
                        groupDiscountPerSoLine = result.asJsonPrimitive.asBoolean
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
                val sharePref = getSharedPreferences(getString(R.string.preference_fle_key_res_config_settings), Context.MODE_PRIVATE)
                val editor = sharePref.edit()
                editor.putBoolean(getString(R.string.saved_group_discount_per_so_line), groupDiscountPerSoLine)
                editor.apply()
            }
        }

        Odoo.callKw(model = "ir.config_parameter", method = "get_param", args = listOf("sale.use_sale_note")) {
            onNext { response ->
                if (response.isSuccessful) {
                    val callKw = response.body()!!
                    if (callKw.isSuccessful) {
                        val result = callKw.result
                        useSaleNote = result.asJsonPrimitive.asBoolean
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
                val sharePref = getSharedPreferences(getString(R.string.preference_fle_key_res_config_settings), Context.MODE_PRIVATE)
                val editor = sharePref.edit()
                editor.putBoolean(getString(R.string.saved_use_sale_note), useSaleNote)
                editor.apply()

                if (useSaleNote) {
                    var result: AuthenticateResult? = null

                    Odoo.getSessionInfo {
                        onNext { response ->
                            if (response.isSuccessful) {
                                val getSessionInfo = response.body()!!
                                if (getSessionInfo.isSuccessful) {
                                    result = getSessionInfo.result
                                    // ...
                                } else {
                                    // Odoo specific error
                                    Timber.w("getSessionInfo() failed with ${getSessionInfo.errorMessage}")
                                }
                            } else {
                                Timber.w("request failed with ${response.code()}:${response.message()}")
                            }
                        }

                        onError { error ->
                            error.printStackTrace()
                        }

                        onComplete {
                            Odoo.searchRead(model = "res.company", domain = listOf(listOf("id", '=', result!!.companyId)), fields = listOf("sale_note")) {
                                onNext { response ->
                                    if (response.isSuccessful) {
                                        val callKw = response.body()!!
                                        if (callKw.isSuccessful) {
                                            val res = callKw.result.records.asJsonArray[0].asJsonObject.get("sale_note").asString
                                            saleNote = res
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
                                    val sharePref = getSharedPreferences(getString(R.string.preference_fle_key_res_config_settings), Context.MODE_PRIVATE)
                                    val editor = sharePref.edit()
                                    editor.putString(getString(R.string.saved_sale_note), saleNote)
                                    editor.apply()
                                }
                            }
                        }
                    }
                }
            }
        }

        Odoo.callKw(model = "res.company", method = "read", args = listOf("sale_note")) {
            onNext { response ->
                if (response.isSuccessful) {
                    val callKw = response.body()!!
                    if (callKw.isSuccessful) {
                        val result = callKw.result
                        val ok = result
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

            }
        }
    }
}

