package com.domatix.yevbes.nucleus.generic.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class IrModel(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("model")
        val model: String
) {
    companion object {
        @JvmField
        val fieldsMap: Map<String, String> = mapOf(
                "id" to "id", "name" to "name", "model" to "model")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }
}