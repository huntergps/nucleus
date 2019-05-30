package com.domatix.yevbes.nucleus.activities.entities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Model(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("name")
        val name: String,

        @Expose
        @SerializedName("model")
        val description: String) {

}