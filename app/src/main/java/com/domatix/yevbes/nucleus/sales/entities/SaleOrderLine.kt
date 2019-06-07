package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class SaleOrderLine(
        @Expose
        @SerializedName("id")
        var id: Int,

        @Expose
        @SerializedName("name")
        var name: String,

        @Expose
        @SerializedName("product_id")
        var productId: JsonElement,

        @Expose
        @SerializedName("product_uom_qty")
        var qty: Float,

        @Expose
        @SerializedName("discount")
        var discount: Float,

        @Expose
        @SerializedName("price_unit")
        var priceUnit: Float,

        @Expose
        @SerializedName("price_total")
        var priceTotal: Float,

        @Expose
        @SerializedName("price_subtotal")
        var priceSubtotal: Float,

        @Expose
        @SerializedName("tax_id")
        var taxId: JsonElement
) {
    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "Description", "product_id" to "Product", "product_uom_qty" to "Quantity",
                "discount" to "Discount", "price_unit" to "Price Unit", "price_total" to "Price Total",
                "tax_id" to "Taxes", "price_subtotal" to "Price subtotal")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}