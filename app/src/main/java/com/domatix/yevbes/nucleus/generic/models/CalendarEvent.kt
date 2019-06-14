package com.domatix.yevbes.nucleus.generic.models

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class CalendarEvent(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("partner_ids")
        val partnerIds: JsonElement
) {
    companion object {
        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "partner_ids" to "partner_ids")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}