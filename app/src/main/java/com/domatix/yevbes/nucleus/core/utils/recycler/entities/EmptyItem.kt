package com.domatix.yevbes.nucleus.core.utils.recycler.entities

import android.support.annotation.DrawableRes

data class EmptyItem(
        val message: CharSequence,
        @DrawableRes
        val drawableResId: Int
)
