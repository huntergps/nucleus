package com.domatix.yevbes.nucleus.sga.service.model

import android.databinding.BindingAdapter
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StockPickingType(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("warehouse_id")
        val warehouseId: JsonElement,

        @Expose
        @SerializedName("color")
        val color: Int,

        @Expose
        @SerializedName("count_picking_ready")
        val countPickingReady: Int,

        @Expose
        @SerializedName("code")
        val code: String
) {
    companion object {

        @JvmStatic
        @BindingAdapter("android:background")
        fun setColor(view: View, color: Int) {
            when (color) {
                0 -> {view.setBackgroundColor(Color.TRANSPARENT)}
                1 -> {view.setBackgroundColor(Color.parseColor("#F06050"))}
                2 -> {view.setBackgroundColor(Color.parseColor("#F4A460"))}
                3 -> {view.setBackgroundColor(Color.parseColor("#F7CD1F"))}
                4 -> {view.setBackgroundColor(Color.parseColor("#6CC1ED"))}
                5 -> {view.setBackgroundColor(Color.parseColor("#814968"))}
                6 -> {view.setBackgroundColor(Color.parseColor("#EB7E7F"))}
                7 -> {view.setBackgroundColor(Color.parseColor("#2C8397"))}
                8 -> {view.setBackgroundColor(Color.parseColor("#475577"))}
                9 -> {view.setBackgroundColor(Color.parseColor("#D6145F"))}
                10 -> {view.setBackgroundColor(Color.parseColor("#30C381"))}
                11 -> {view.setBackgroundColor(Color.parseColor("#9365B8"))}
                else -> {view.setBackgroundColor(Color.TRANSPARENT)}
            }
        }

        @JvmStatic
        @BindingAdapter("customText")
        fun setText(view: TextView, code: String) {
            val stringArray = Odoo.app.resources.getStringArray(R.array.record_code)
            when(code){
                "incoming" -> {view.text = stringArray[0]}
                "outgoing" -> {view.text = stringArray[1]}
                "internal" -> {view.text = stringArray[2]}
                "mrp_operation" -> {view.text = stringArray[1]}
                else -> {}
            }
        }

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "name", "warehouse_id" to "warehouse_id", "color" to "color",
                "count_picking_ready" to "count_picking_ready", "code" to "code")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}