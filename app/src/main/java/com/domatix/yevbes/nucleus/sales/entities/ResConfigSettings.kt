package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResConfigSettings(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("group_discount_per_so_line")
        val groupDiscountPerSoLine: Boolean

) {

    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id",
                "group_discount_per_so_line" to "Group discount per so line")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}