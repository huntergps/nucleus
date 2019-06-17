package com.domatix.yevbes.nucleus.activities.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import com.domatix.yevbes.nucleus.databinding.ActivityRowBinding


class ActivityViewHolder(
        val binding: ActivityRowBinding,
        val viewBackground: View,
        var viewForeground: View
) : RecyclerView.ViewHolder(binding.root)