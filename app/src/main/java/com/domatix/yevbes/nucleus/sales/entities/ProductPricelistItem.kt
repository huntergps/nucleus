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
        val productId: JsonElement,

        @Expose
        @SerializedName("applied_on")
        val appliedOn: String,

        @Expose
        @SerializedName("compute_price")
        val computePrice: String,

        @Expose
        @SerializedName("fixed_price")
        val fixedPrice: Float,

        @Expose
        @SerializedName("percent_price")
        val percentPrice: Float,

        @Expose
        @SerializedName("min_quantity")
        val minQuantity: Int




) {
    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "name", "display_name" to "Display Name",
                "pricelist_id" to "PricelistID", "product_id" to "Product Id",
                "applied_on" to "AppliedOn", "compute_price" to "ComputePrice",
                "fixed_price" to "FixedPrice", "percent_price" to "PercentPrice",
                "min_uantity" to "MinQuantity"
                )

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}