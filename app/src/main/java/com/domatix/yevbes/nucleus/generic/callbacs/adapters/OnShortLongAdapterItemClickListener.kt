package com.domatix.yevbes.nucleus.generic.callbacs.adapters

import android.view.View

interface OnShortLongAdapterItemClickListener {
    fun onShortAdapterItemPressed(view: View)
    fun onLongAdapterItemPressed(view: View)
}