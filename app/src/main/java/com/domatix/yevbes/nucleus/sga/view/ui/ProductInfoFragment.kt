package com.domatix.yevbes.nucleus.sga.view.ui


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.domatix.yevbes.nucleus.databinding.FragmentProductInfoBinding
import com.domatix.yevbes.nucleus.gson
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.google.gson.reflect.TypeToken

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ProductInfoFragment : Fragment() {
    private var param1: String? = null
    private val productType = object : TypeToken<ProductProduct>() {}.type
    lateinit var binding: FragmentProductInfoBinding private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductInfoBinding.inflate(inflater, container, false)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
        val product: ProductProduct = gson.fromJson(param1,productType)
        binding.product = product
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
                ProductInfoFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }
}
