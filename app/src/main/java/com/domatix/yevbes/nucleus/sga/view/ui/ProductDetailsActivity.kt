package com.domatix.yevbes.nucleus.sga.view.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.domatix.yevbes.nucleus.databinding.ActivityProductDetailsBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.trimFalse
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailsBinding
    private val productType = object : TypeToken<ProductProduct>() {}.type
    private lateinit var productProduct: ProductProduct

    companion object {
        const val PRODUCT_STRING_JSON = "PRODUCT_STRING_JSON"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_product_details)
        setSupportActionBar(tb)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        tb.setNavigationOnClickListener {
            onBackPressed()
        }

        val productString = intent.extras.getString(PRODUCT_STRING_JSON)
        val product: ProductProduct = gson.fromJson(productString, productType)

        productProduct = product


        binding.tvName.text = product.name
        binding.tvPrice.text = product.lstPrice.toString() + " €"
        if (product.image.trimFalse().isNotBlank())
            ProductProduct.loadImage(binding.ivProduct, product.image, product.name)
        else
            binding.ivProduct.setImageResource(R.drawable.ic_no_camara)

        tl.addTab(tl.newTab().setText(R.string.product_info))
        tl.addTab(tl.newTab().setText(R.string.product_pucharse))

        viewPager.adapter = TabsAdapter(supportFragmentManager, tl.tabCount, productString)
        tl.setupWithViewPager(viewPager)
    }

    inner class TabsAdapter(fm: FragmentManager,
                            private val tabNum: Int,
                            private val productString: String) : FragmentStatePagerAdapter(fm) {

        private lateinit var infoFragment: ProductInfoFragment
        private lateinit var purchaseFragment: PurchaseProductFragment

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    infoFragment = ProductInfoFragment.newInstance(productString)
                    return infoFragment
                }

                1 -> {
                    purchaseFragment = PurchaseProductFragment.newInstance(productString)
                    return purchaseFragment
                }
            }
            return this.infoFragment
        }

        override fun getCount(): Int {
            return tabNum
        }

        override fun getPageTitle(position: Int): CharSequence? {
            var title: String? = null

            when (position) {
                0 -> title = Odoo.app.getString(R.string.product_info)
                1 -> title = Odoo.app.getString(R.string.product_pucharse)
            }

            return title
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_product_details, menu)
        if (productProduct.type.asString == "product") {
            menu?.findItem(R.id.action_edit)?.isVisible = true
            menu?.findItem(R.id.action_edit)?.isEnabled = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_edit -> {
                // Show wizard

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeProductQty(){

    }
}


