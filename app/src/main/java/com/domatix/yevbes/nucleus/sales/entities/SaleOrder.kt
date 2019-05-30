package com.domatix.yevbes.nucleus.sales.entities

import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class SaleOrder(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("state")
        val state: String,

        @Expose
        @SerializedName("date_order")
        val dateOrder: String,

        @Expose
        @SerializedName("note")
        val terms: String,

        @Expose
        @SerializedName("amount_untaxed")
        val amountUntaxed: Float,

        @Expose
        @SerializedName("amount_tax")
        val amountTax: Float,

        @Expose
        @SerializedName("amount_total")
        val amountTotal: Float,

        @Expose
        @SerializedName("partner_id")
        val partnerId: JsonElement,

        @Expose
        @SerializedName("partner_invoice_id")
        val partnerInvoiceId: JsonElement,

        @Expose
        @SerializedName("partner_shipping_id")
        val partnerShippingId: JsonElement,

        @Expose
        @SerializedName("pricelist_id")
        val pricelistId: JsonElement,

        @Expose
        @SerializedName("order_line")
        val orderLine: JsonElement

) {

    companion object {

        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "Name", "state" to "State", "date_order" to "Due Date",
                "partner_id" to "Partner ID", "partner_invoice_id" to "Partner Invoice ID",
                "partner_shipping_id" to "Partner Shipping ID", "order_line" to "Lines",
                "note" to "Terms", "amount_untaxed" to "Untaxed Amount", "amount_tax" to "Taxes",
                "amount_total" to "Amount Total")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

}