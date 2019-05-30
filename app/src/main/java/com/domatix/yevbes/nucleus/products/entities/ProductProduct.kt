package com.domatix.yevbes.nucleus.products.entities

import android.databinding.BindingAdapter
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.GlideApp
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.core.Odoo
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class ProductProduct(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("description_sale")
        val descriptionSale: String,

        @Expose
        @SerializedName("default_code")
        val defaultCode: String,

        @Expose
        @SerializedName("barcode")
        val barcode: String,

        @Expose
        @SerializedName("lst_price")
        val lstPrice: Float,

        @Expose
        @SerializedName("standard_price")
        val standardPrice: Float,

        @Expose
        @SerializedName("type")
        val type: JsonElement,

        @Expose
        @SerializedName("image_small")
        val imageSmall: String,

        @Expose
        @SerializedName("image")
        val image: String,

        @Expose
        @SerializedName("taxes_id")
        val taxesId: JsonArray,

        @Expose
        @SerializedName("uom_id")
        val uomId: JsonArray,

        var quantity: Float = 0f,
        var checked: Boolean = false
) {

    companion object {

        @JvmStatic
        @BindingAdapter("productType")
        fun setProductypeText(view: TextView, type: JsonElement) {
            when (type.asString) {
                "product" -> view.text = Odoo.app.getString(R.string.product_type_product)

                "service" -> view.text = Odoo.app.getString(R.string.product_type_service)

                "consu" -> view.text = Odoo.app.getString(R.string.product_type_consu)
            }
        }

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
                "id" to "id", "name" to "Name", "description_sale" to "Description Sale", "default_code" to "Default Code", "barcode" to "Barcode",
                "lst_price" to "Sale Price", "standard_price" to "Cost Price", "type" to "Type",
                "image_small" to "Image Small", "image" to "Image", "taxes_id" to "Taxes", "uom_id" to "Uom ID")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}