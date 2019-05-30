package com.domatix.yevbes.nucleus.sales.fragments

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.FragmentOrderLineBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.ProductDialogFragment
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.activities.OrderLineManagerActivity
import com.domatix.yevbes.nucleus.sales.adapters.AddProductProductDataAdapter.Companion.DIALOG_TAG
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.sales.interfaces.FragmentToActivityListener
import com.domatix.yevbes.nucleus.taxes.entities.AccountTax
import com.domatix.yevbes.nucleus.trimFalse
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 *
 */
class OrderLineFragment : Fragment() {

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment OrderLineFragment.
         */

        @JvmStatic
        fun newInstance() =
                OrderLineFragment().apply {
                    arguments = Bundle().apply {
                    }
                }

        const val DISCOUNT = "Discount"
        const val QUANTITY = "Quantity"
    }

    lateinit var binding: FragmentOrderLineBinding
    lateinit var items: ArrayList<ProductProduct>
    private var productProduct: ProductProduct? = null
    lateinit var compositeDisposable: CompositeDisposable private set
    private val productListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    private var saleOrderLine: SaleOrderLine? = null

    private var quantity: Float? = null
    private var discount: Float? = null
    private var productProductID: Int? = null
    private var description: String? = null
    private var unitPrice: Float? = null
    private var sumTaxes: Float = 0F
    private var position: Int = -1
    private var modified: Boolean = false
    private var newProduct: Boolean = false
    lateinit var activity: OrderLineManagerActivity private set

    private var listener: FragmentToActivityListener? = null

    fun isModified(): Boolean? {
        return this.modified
    }

    private fun isModified(modif: Boolean) {
        this.modified = modif
    }

    fun isNewProduct(): Boolean? {
        return this.newProduct
    }

    private fun isNewProduct(newProduct: Boolean) {
        this.newProduct = newProduct
    }

    fun setFragmentToActivityListener(listener: FragmentToActivityListener) {
        this.listener = listener
    }

    fun getSaleOrderLine(): SaleOrderLine? {
        return this.saleOrderLine
    }

    fun getProductProductObj(): ProductProduct? {
        return this.productProduct
    }

    fun getPosition(): Int? {
        return this.position
    }

    fun setPosition(pos: Int) {
        this.position = pos
    }

    fun getQuantity(): Float? {
        return this.quantity
    }

    fun getDiscount(): Float? {
        return this.discount
    }


    fun getDescription(): String? {
        return this.description
    }

    fun getUnitPrice(): Float? {
        return this.unitPrice
    }

    fun getSumTaxes(): Float? {
        return this.sumTaxes
    }

    fun getPriceTotal(): Float? {
        val tot = getUnitPrice()!! * getQuantity()!!
        val discount = tot * (getDiscount()!! / 100)

        val taxes = if (getSumTaxes() == 0f) {
            0f
        } else {
            tot * (getSumTaxes()!! / 100)
        }

        return tot - discount //+ taxes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Si no hay nada guardado en el bundle
        val bundle = arguments
        val orderLineGson = Gson()

        activity = getActivity() as OrderLineManagerActivity

        saleOrderLine = orderLineGson.fromJson(bundle?.getString(
                OrderLineManagerActivity.SELECTED_LIST), SaleOrderLine::class.java)

        quantity = saleOrderLine!!.qty
        discount = saleOrderLine!!.discount
        unitPrice = saleOrderLine!!.priceUnit
        description = saleOrderLine!!.name

        productProductID = saleOrderLine!!.productId.asJsonArray.get(0).asInt

        isModified(true)
        Timber.v("Fragmento Creado")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_line, container, false)

        compositeDisposable = CompositeDisposable()

        if (productProduct == null)
            getProductProductById(productProductID!!)
        else
            getProductProduct()

        if (!activity.getGroupDiscountPerSoLine()) {
            binding.textDiscountContainer.visibility = View.GONE
        } else {
            binding.textDiscountContainer.visibility = View.VISIBLE
        }

        binding.quantity.setText(quantity.toString(), TextView.BufferType.EDITABLE)
        binding.discount.setText(discount.toString(), TextView.BufferType.EDITABLE)

        if (description != null)
            binding.description.setText(description.toString(), TextView.BufferType.EDITABLE)

        binding.unitPrice.setText(saleOrderLine!!.priceUnit.toString(), TextView.BufferType.EDITABLE)

        binding.product.setOnClickListener {
            getProductList()
        }

        binding.quantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                quantity = if (p0!!.isNotEmpty())
                    p0.toString().toFloat()
                else {
                    1.0f
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        binding.quantity.setOnFocusChangeListener { _, p1 ->
            if (!p1 && binding.quantity.text.isEmpty()) {
                binding.quantity.setText(quantity.toString())
            }
        }

        binding.discount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                discount = if (p0!!.isNotEmpty())
                    p0.toString().toFloat()
                else {
                    0.0f
                }


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        binding.discount.setOnFocusChangeListener { _, p1 ->
            if (!p1 && binding.discount.text.isEmpty()) {
                binding.discount.setText(discount.toString())
            }
        }

        binding.description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.isNotEmpty())
                    description = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        binding.unitPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                unitPrice = if (p0!!.isNotEmpty())
                    p0.toString().toFloat()
                else {
                    0.0f
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })

        binding.unitPrice.setOnFocusChangeListener { _, p1 ->
            if (!p1 && binding.unitPrice.text.isEmpty()) {
                binding.unitPrice.setText(unitPrice.toString())
            }
        }


        return binding.root
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
                            productProduct = product
                            isNewProduct(true)
                            getProductProductById(product.id)
                        }

                    })
                    val supportFragmentManager = activity?.supportFragmentManager
                    dialogFragment.show(supportFragmentManager, DIALOG_TAG)
                } else {
                    Toast.makeText(activity, getString(R.string.no_products_find), Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun loadImageToView(imageView: ImageView, imgSmall: String, description: String) {
        if (productProduct?.imageSmall != "false") {
            ProductProduct.loadImage(imageView, imgSmall, description)
        } else {
            imageView.setImageResource(R.drawable.ic_no_camara)
        }
    }

    private fun getProductProductById(id: Int) {
        Odoo.load(id = id, model = "product.product", fields = ProductProduct.fields) {
            /*onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }*/
            onNext { response ->
                if (response.isSuccessful) {
                    val read = response.body()!!
                    if (read.isSuccessful) {
                        val result = read.result
                        val gson = Gson()
                        val jsonObject = result.value
                        productProduct = gson.fromJson(jsonObject.toString(), ProductProduct::class.java)
                        productProductID = productProduct?.id
                        binding.productProduct = productProduct
                        loadImageToView(binding.imageView11, productProduct!!.image, productProduct!!.name)

                        if (isNewProduct()!!) {
                            binding.unitPrice.setText(productProduct!!.lstPrice.toString())
                            binding.description.setText("${productProduct!!.name}\n${productProduct!!.descriptionSale.trimFalse()}", TextView.BufferType.EDITABLE)
                            binding.discount.setText(0.0f.toString())
                            binding.quantity.setText(1.0f.toString())
                        }

                        quantity = binding.quantity.editableText.toString().toFloat()
                        discount = binding.discount.editableText.toString().toFloat()
                        description = binding.description.editableText.toString()
                        unitPrice = binding.unitPrice.editableText.toString().toFloat()

                    } else {
                        // Odoo specific error
                        Timber.w("load() failed with ${read.errorMessage}")
                    }

                    //compositeDisposable.dispose()
                    //compositeDisposable = CompositeDisposable()
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                val taxIds = ArrayList<Int>()
                val jsonArray = productProduct!!.taxesId.asJsonArray

                for (i in 0 until jsonArray.size()) {
                    taxIds.add(jsonArray.get(i).asInt)
                }
                getTaxesById(taxIds)
            }
        }
    }

    private fun getProductProduct() {
        Odoo.load(id = productProductID!!, model = "product.product", fields = ProductProduct.fields) {
            /*onSubscribe { disposable ->
                compositeDisposable.add(disposable)
            }*/
            onNext { response ->
                if (response.isSuccessful) {
                    val read = response.body()!!
                    if (read.isSuccessful) {
                        val result = read.result
                        val gson = Gson()
                        val jsonObject = result.value
                        productProduct = gson.fromJson(jsonObject.toString(), ProductProduct::class.java)
                        binding.productProduct = productProduct
                    } else {
                        // Odoo specific error
                        Timber.w("load() failed with ${read.errorMessage}")
                    }

                    //compositeDisposable.dispose()
                    //compositeDisposable = CompositeDisposable()
                } else {
                    Timber.w("request failed with ${response.code()}:${response.message()}")
                }
            }

            onError { error ->
                error.printStackTrace()
            }

            onComplete {
                val taxIds = ArrayList<Int>()
                val jsonArray = productProduct!!.taxesId.asJsonArray

                for (i in 0 until jsonArray.size()) {
                    taxIds.add(jsonArray.get(i).asInt)
                }
                getTaxesById(taxIds)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        /*quantity = binding.quantity.editableText.toString().toFloat()
        discount = binding.discount.editableText.toString().toFloat()
        description = binding.description.editableText.toString()
        unitPrice = binding.unitPrice.editableText.toString().toFloat()*/
        val jsonArray = JsonArray()
        jsonArray.add(getProductProductObj()!!.id)
        jsonArray.add(getProductProductObj()!!.name)

        val saleOrderLine = SaleOrderLine(
                discount = discount!!,
                name = description!!,
                id = this.saleOrderLine!!.id,
                productId = jsonArray,
                qty = quantity!!,
                priceUnit = unitPrice!!,
                priceTotal = unitPrice!! * quantity!!,
                priceSubtotal = unitPrice!! * quantity!!,
                taxId = productProduct!!.taxesId
        )
        if (listener != null) {
            listener!!.onFragmentStop(saleOrderLine)
        }
    }
}
