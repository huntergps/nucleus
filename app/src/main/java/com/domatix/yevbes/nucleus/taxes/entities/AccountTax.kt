package com.domatix.yevbes.nucleus.taxes.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class AccountTax(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("amount")
        val amount: Float
) {
    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "Name", "amount" to "Amount")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}