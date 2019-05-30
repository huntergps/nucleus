package com.domatix.yevbes.nucleus.sga.service.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StockInventory(
        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("display_name")
        val displayName: String,

        @Expose
        @SerializedName("date")
        val date: String,

        @Expose
        @SerializedName("state")
        val state: JsonElement,

        @Expose
        @SerializedName("line_ids")
        val lineIds: JsonArray
) {

    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "name" to "Name", "display_name" to "Display name", "date" to "Date",
                "state" to "State", "line_ids" to "Line IDs"
        )
        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}
