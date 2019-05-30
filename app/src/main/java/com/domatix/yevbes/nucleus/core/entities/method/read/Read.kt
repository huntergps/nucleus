package com.domatix.yevbes.nucleus.core.entities.method.read

import com.domatix.yevbes.nucleus.core.entities.odooError.OdooError
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Read(

        @field:Expose
        @field:SerializedName("result")
        val result: JsonElement = JsonArray(),

        @field:Expose
        @field:SerializedName("error")
        val odooError: OdooError = OdooError()
) {
    val isSuccessful get() = !isOdooError
    val isOdooError get() = odooError.message.isNotEmpty()
    val errorCode get() = odooError.code
    val errorMessage get() = odooError.data.message
}