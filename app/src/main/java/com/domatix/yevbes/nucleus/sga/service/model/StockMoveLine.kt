package com.domatix.yevbes.nucleus.sga.service.model

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StockMoveLine(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("product_id")
        val productId: JsonElement,

        @Expose
        @SerializedName("qty_done")
        val qty: Float,

        @Expose
        @SerializedName("lot_name")
        val lotName: String,

        @Expose
        @SerializedName("lot_id")
        val lotId: JsonElement,

        @Expose
        @SerializedName("product_uom_qty")
        val productQty: Float,

        @Expose
        @SerializedName("location_id")
        val locationId: JsonElement,

        @Expose
        @SerializedName("location_dest_id")
        val locationDestId: JsonElement
) {

    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "ID", "qty_done" to "Quantity Done", "location_id" to "Location ID",
                "location_dest_id" to "Location Dest ID", "product_uom_qty" to "Product Quantity",
                "lot_name" to "Lot name", "lot_id" to "Lot id", "product_id" to "Product ID"
        )

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}
