package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ProductPricelist(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("active")
        val active: Boolean,

        @Expose
        @SerializedName("company_id")
        val companyId: JsonElement,

        @Expose
        @SerializedName("discount_policy")
        val discountPolicy: String,

        @Expose
        @SerializedName("display_name")
        val displayName: String,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("item_ids")
        val itemIds: JsonElement,

        @Expose
        @SerializedName("sequence")
        val sequence: Int
) {
    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "active" to "active", "company_id" to "Company Id",
                "discount_policy" to "Discount Policy", "display_name" to "Display Name",
                "name" to "name", "item_ids" to "Item Ids", "sequence" to "sequence")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}