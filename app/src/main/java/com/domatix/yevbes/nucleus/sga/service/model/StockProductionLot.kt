package com.domatix.yevbes.nucleus.sga.service.model

import com.google.gson.JsonArray
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StockProductionLot(
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
        @SerializedName("product_id")
        val productId: JsonArray
) {
    companion object {
        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "ID", "name" to "Name", "display_name" to "Display name", "product_id" to "Product ID"
        )

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}