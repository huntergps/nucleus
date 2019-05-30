package com.domatix.yevbes.nucleus.sga.service.model

import android.databinding.BindingAdapter
import android.widget.TextView
import com.domatix.yevbes.nucleus.fromStringToDate
import com.domatix.yevbes.nucleus.getDateToFriendlyFormat
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class StockPicking(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("origin")
        val origin: String,

        @Expose
        @SerializedName("state")
        val state: JsonElement,

        @Expose
        @SerializedName("show_lots_text")
        val showLotsText: Boolean,

        @Expose
        @SerializedName("partner_id")
        val partnerId: JsonElement,

        @Expose
        @SerializedName("picking_type_code")
        val pickingType: JsonElement,

        @Expose
        @SerializedName("scheduled_date")
        val scheduledDate: String,

        @Expose
        @SerializedName("move_line_ids")
        val moveLinesID: JsonElement,

        @Expose
        @SerializedName("move_lines")
        val moveLines: JsonElement,

        @Expose
        @SerializedName("location_id")
        val locationId: JsonElement,

        @Expose
        @SerializedName("location_dest_id")
        val locationDestId: JsonElement,

        @Expose
        @SerializedName("barcode")
        val barcode: String

) {
    companion object {

        @JvmStatic
        @BindingAdapter("setDate")
        fun setDateFrendlyFormat(tv: TextView, date: String){
            val date = fromStringToDate(date, "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateTransfer = getDateToFriendlyFormat(date, "dd MMM - HH:mm", Locale.getDefault(), TimeZone.getTimeZone("GMT+04:00")).toLowerCase()
            tv.text = dateTransfer
        }

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "name" to "Name", "origin" to "Origin", "state" to "State", "partner_id" to "Partner ID", "picking_type_code" to "Picking Type Code",
                "scheduled_date" to "Scheduled Date", "move_line_ids" to "Move Line IDs", "move_lines" to "Move Line", "location_id" to "Location ID",
                "location_dest_id" to "Location Dest ID", "barcode" to "Barcode", "show_lots_text" to "Show lots text")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}
