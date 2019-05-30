package com.domatix.yevbes.nucleus.sga.service.model

import android.databinding.BindingAdapter
import android.util.Base64
import android.widget.ImageView
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.GlideApp
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StockMove (

        @Expose
        @SerializedName("product_id")
        val productId: JsonArray,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("product_uom_qty")
        val productQty: Float,

        @Expose
        @SerializedName("reserved_availability")
        val reservedAvailability: Float,

        @Expose
        @SerializedName("move_line_ids")
        val moveLineIds: JsonArray,

        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("state")
        val state: JsonElement,

        @Expose
        @SerializedName("quantity_done")
        val qtyDone: Float

) {

    companion object {
        @JvmStatic
        @BindingAdapter("image", "name")
        fun loadImage(view: ImageView, imageSmall: String, name: String) {
            GlideApp.with(view.context)
                    .asBitmap()
                    .load(
                            if (imageSmall.isNotEmpty())
                                Base64.decode(imageSmall, Base64.DEFAULT)
                            else
                                (view.context.applicationContext as App)
                                        .getLetterTile(if (name.isNotEmpty()) name else "X"))
                    .into(view)
        }


        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "product_id" to "Product ID", "name" to "Name", "product_uom_qty" to "Product Quantity",
                "move_line_ids" to "Move Line IDs", "id" to "ID", "state" to "State",
                "quantity_done" to "Quantity Done", "reserved_availability" to "reserved_availability"
        )

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}
