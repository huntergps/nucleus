package com.domatix.yevbes.nucleus.company.entities

import android.databinding.BindingAdapter
import android.util.Base64
import android.widget.ImageView
import com.domatix.yevbes.nucleus.App
import com.domatix.yevbes.nucleus.GlideApp
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Company(
        @Expose
        @SerializedName("id")
        val id: Int,

        @Expose
        @SerializedName("image_small")
        val imageSmall: String,

        @Expose
        @SerializedName("name")
        val name: String) : Serializable {
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
                "id" to "id", "name" to "Name", "image_small" to "Image small")

        @JvmField
        val fields: ArrayList<String> = fieldsMap.keys.toMutableList() as ArrayList<String>
    }

    override fun toString(): String {
        return name
    }
}