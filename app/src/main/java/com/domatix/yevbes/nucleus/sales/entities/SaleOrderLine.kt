package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class SaleOrderLine(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("product_id")
        val productId: JsonElement,

        @Expose
        @SerializedName("product_uom_qty")
        val qty: Float,

        @Expose
        @SerializedName("discount")
        val discount: Float,

        @Expose
        @SerializedName("price_unit")
        val priceUnit: Float,

        @Expose
        @SerializedName("price_total")
        val priceTotal: Float,

        @Expose
        @SerializedName("price_subtotal")
        val priceSubtotal: Float,

        @Expose
        @SerializedName("tax_id")
        val taxId: JsonElement
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