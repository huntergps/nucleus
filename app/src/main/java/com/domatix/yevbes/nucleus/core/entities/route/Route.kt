package com.domatix.yevbes.nucleus.core.entities.route

import com.domatix.yevbes.nucleus.core.entities.odooError.OdooError
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Route(

        @Expose
        @SerializedName("result")
        val result: JsonElement = JsonObject(),

        @Expose
        @SerializedName("error")
        val odooError: OdooError = OdooError()
) {
    val isSuccessful get() = !isOdooError
    val isOdooError get() = odooError.message.isNotEmpty()
    val errorCode get() = odooError.code
    val errorMessage get() = odooError.data.message
}