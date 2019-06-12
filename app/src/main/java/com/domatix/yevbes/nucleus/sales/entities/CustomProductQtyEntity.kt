package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonArray
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class CustomProductQtyEntity(
        @Expose
        @SerializedName("id")
        val idProduct: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("lst_price")
        val lstPrice: Float,

        @Expose
        @SerializedName("taxes_id")
        val taxesId: JsonArray,

        @Expose
        var quantity: Float = 0f


) {
    companion object {
        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "Name", "lst_price" to "Last price", "taxes_id" to "Taxes ID")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}