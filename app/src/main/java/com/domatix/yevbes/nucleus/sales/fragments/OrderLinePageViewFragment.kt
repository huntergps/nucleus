package com.domatix.yevbes.nucleus.sales.fragments


import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentOrderLinePageViewBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.ProductDialogFragment
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.adapters.AddProductProductDataAdapter
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.taxes.entities.AccountTax
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

const val SELECTED_ITEM = "SELECTED_ITEM"

class OrderLinePageViewFragment : Fragment() {
    lateinit var binding: FragmentOrderLinePageViewBinding
    private lateinit var items: ArrayList<ProductProduct>
    private val productListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    lateinit var compositeDisposable: CompositeDisposable private set
    var productProduct: ProductProduct? = null
    private var sumTaxes: Float = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_line_page_view, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        compositeDisposable = CompositeDisposable()

        arguments?.let {
            val itemString = it.getString(SELECTED_ITEM)
            val item = gson.fromJson<SaleOrderLine>(itemString, object : TypeToken<SaleOrderLine>() {}.type)
            binding.saleOrderLine = item
            getProductProductById(item.productId.asJsonArray[0].asInt)
        }

        binding.product.setOnClickListener {
            getProductList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.dispose()
    }

    private fun getTaxesById(ids: ArrayList<Int>) {
        Odoo.read(model = "account.tax", ids = ids, fields = AccountTax.fields) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }
            onNext { response ->
                if (response.isSuccessful) {
                    val read = response.body()!!
                    if (read.isSuccessful) {
                        binding.taxesLinearLayout.removeAllViews()

                        val result = read.result

                        // Generate buttons
                        val jsonArray = result.asJsonArray
                        var id: Int
                        var name: String

                        for (i in 0 until jsonArray.size()) {
                            val button = Button(activity)
                            id = jsonArray.get(i).asJsonObject.get("id").asInt
                            name = jsonArray.get(i).asJsonObject.get("name").asString
                            sumTaxes += jsonArray.get(i).asJsonObject.get("amount").asFloat

                            button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                            button.text = name
                            button.tag = id
                            button.setTextColor(Color.WHITE)
                            binding.taxesLinearLayout.addView(button)
                        }
                    } else {
                        Timber.w("read() failed with ${read.errorMessage}")
                    }
                }
            }
            onError { error ->
                error.printStackTrace()
            }

            onComplete {

            }
        }
    }

    private fun getPriceTotal(): Float? {
        val tot = binding.unitPrice.text.toString().toFloat() * binding.quantity.text.toString().toFloat()
        val discount = tot * (binding.discount.text.toString().toFloat() / 100)

        val taxes = if (this.sumTaxes == 0f) {
            0f
        } else {
            tot * (this.sumTaxes / 100)
        }

        return tot - discount //+ taxes
    }

    private fun getProductProductById(id: Int) {
        Odoo.load(id = id, model = "product.product", fields = ProductProduct.fields) {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val read = response.body()!!
                    if (read.isSuccessful) {
                        val result = read.result
                        val gson = Gson()
                        val jsonObject = result.value
                        val item = gson.fromJson<ProductProduct>(jsonObject, object : TypeToken<ProductProduct>(){}.type)
                        productProduct = item
                        binding.product.setText(item.name)
                        binding.description.setText(item.name)
                    } else {
                        Timber.w("load() failed with ${read.errorMessage}")
                    }

                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                if (productProduct != null) {
                    val taxIds = ArrayList<Int>()
                    val jsonArray = productProduct!!.taxesId.asJsonArray

                    for (i in 0 until jsonArray.size()) {
                        taxIds.add(jsonArray.get(i).asInt)
                    }
                    loadImageToView(binding.imageView11,productProduct!!.image,productProduct!!.name)
                    getTaxesById(taxIds)
                }
            }
        }
    }

    private fun loadImageToView(imageView: ImageView, imgSmall: String, description: String) {
        if (productProduct?.imageSmall != "false") {
            ProductProduct.loadImage(imageView, imgSmall, description)
        } else {
            imageView.setImageResource(R.drawable.ic_no_camara)
        }
    }

    private fun getProductList() {
        Odoo.searchRead(model = "product.product", domain = listOf(listOf("sale_ok", "=", true
        )), fields = ProductProduct.fields, sort = "name DESC") {
            onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }

            onNext { response ->
                if (response.isSuccessful) {
                    val searchRead = response.body()!!
                    if (searchRead.isSuccessful) {
                        items = gson.fromJson(searchRead.result.records, productListType)
                    }
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                if (::items.isInitialized) {
                    val dialogFragment = ProductDialogFragment.newInstance(items)
                    dialogFragment.setFragmentDetachListener(object : ProductDialogFragment.OnFragmentDetachListener {
                        override fun onFragmentDialogDetach(product: ProductProduct) {
                            if (productProduct!!.id != product.id) {
                                productProduct = product
                                getProductProductById(product.id)
                            }
                        }
                    })
                    val supportFragmentManager = activity?.supportFragmentManager
                    dialogFragment.show(supportFragmentManager, AddProductProductDataAdapter.DIALOG_TAG)
                } else {
                    Toast.makeText(activity, getString(R.string.no_products_find), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
