package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ProductPricelistItem(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("display_name")
        val displayName: String,

        @Expose
        @SerializedName("pricelist_id")
        val pricelistId: JsonElement,

        @Expose
        @SerializedName("product_id")
        val productId: JsonElement
) {
    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "name", "display_name" to "Display Name",
                "pricelist_id" to "PricelistID", "product_id" to "Product Id")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}