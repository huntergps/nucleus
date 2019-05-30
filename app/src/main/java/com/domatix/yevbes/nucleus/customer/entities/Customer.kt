package com.domatix.yevbes.nucleus.customer.entities

import android.databinding.BindingAdapter
import android.util.Base64
import android.widget.ImageView
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.GlideApp
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Customer(

        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("email")
        val email: String,

        @Expose
        @SerializedName("company_name")
        val companyName: String,

        @Expose
        @SerializedName("parent_id")
        val companyId: JsonElement,

        @Expose
        @SerializedName("image_small")
        val imageSmall: String,

        @Expose
        @SerializedName("website")
        val website: String,

        @Expose
        @SerializedName("phone")
        val phone: String,

        @Expose
        @SerializedName("mobile")
        val mobile: String,

        @Expose
        @SerializedName("contact_address")
        val fullAddress: String,

        @Expose
        @SerializedName("state_id")
        val stateId: JsonElement,

        @Expose
        @SerializedName("property_product_pricelist")
        val propertyProductPricelist: JsonElement,

        @Expose
        @SerializedName("country_id")
        val countryId: JsonElement,

        @Expose
        @SerializedName("comment")
        val comment: String,

        @Expose
        @SerializedName("city")
        val city: String,

        @Expose
        @SerializedName("street")
        val street: String,

        @Expose
        @SerializedName("street2")
        val streetTwo: String,

        @Expose
        @SerializedName("zip")
        val zip: String,

        @Expose
        @SerializedName("is_company")
        val isCompany: Boolean,

        @Expose
        @SerializedName("customer")
        val customer: Boolean,

        @Expose
        @SerializedName("supplier")
        val supplier: Boolean
) {
    companion object {
        @JvmStatic
        @BindingAdapter("image_small", "name")
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
                "id" to "id", "name" to "Name", "email" to "Email",
                "company_name" to "Company Name", "parent_id" to "Company", "image_small" to "Image", "website" to "Website",
                "phone" to "Phone Number", "mobile" to "Mobile Number", "contact_address" to "Full Address",
                "state_id" to "State", "country_id" to "Country", "comment" to "Internal Note", "city" to "City", "street" to "Street", "street2" to "Street2", "zip" to "Zip",
                "is_company" to "Is Company", "customer" to "Customer", "supplier" to "Supplier", "property_product_pricelist" to "property_product_pricelist")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}