package com.domatix.yevbes.nucleus.activities

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.activities.callbacks.OnCheckClicked
import com.domatix.yevbes.nucleus.activities.entities.Activity
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityRowBinding
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import kotlinx.android.synthetic.main.activity_row.view.*
import kotlin.collections.ArrayList


class ActivityDataAdapter(
        val fragment: ActivitiesFragment,
        items: ArrayList<Any>,
        private val listener: OnCheckClicked,
        private val clickListener: OnShortLongAdapterItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.activitiesRecyclerView) {

    companion object {
        const val TAG: String = "ActivityAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<Activity> = ArrayList(
            items.filterIsInstance<Activity>()
    )


    fun addRowItems(rowItemsAll: ArrayList<Activity>) {
        this.rowItems.addAll(rowItemsAll)
        addAll(rowItemsAll.toMutableList<Any>() as ArrayList<Any>)
    }


    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is Activity) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<Activity>()))
    }

    fun removeItemAdapter(position: Int): Activity {
        val activity = this.rowItems.removeAt(position)
        removeItem(position)
        return activity
    }

    fun restoreItem(item: Activity, position: Int) {
        add(item, position)
        this.rowItems.add(position, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ActivityDataAdapter.VIEW_TYPE_ITEM -> {
                val binding = ActivityRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )

                binding.root.setOnClickListener {
                    clickListener.onShortAdapterItemPressed(it)
                }

                binding.root.imageView10.setOnClickListener {
                    listener.onCheckClicked( binding.root)
                }

                return ActivityViewHolder(binding,
                        binding.root.findViewById(R.id.view_background) as View,
                        binding.root.findViewById(R.id.view_foreground) as View
                )
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }

    val rowItemCount: Int get() = rowItems.size

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as ActivityViewHolder
                val item = items[position] as Activity
                val binding = holder.binding
                binding.activityObj = item
            }
        }
    }
}