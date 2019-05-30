package com.domatix.yevbes.nucleus.sales.interfaces

import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine

interface FragmentToActivityListener {
    fun onFragmentStop(saleOrderLine: SaleOrderLine)
}