package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.DashboardInventoryRowBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockPickingType
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener
import com.domatix.yevbes.nucleus.sga.view.ui.DashboardInventoryFragment

class DashboardAdapter(
        val fragment: DashboardInventoryFragment,
        items: ArrayList<Any>,
        val listener: OnItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.rv) {

    companion object {
        const val TAG: String = "DashboardInventoryAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockPickingType) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private var rowItems: ArrayList<StockPickingType> = ArrayList()
    private var rowItemsCopy: ArrayList<StockPickingType> = ArrayList(
            items.filterIsInstance<StockPickingType>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<StockPickingType>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = DashboardInventoryRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return DashboardViewHolder(binding)
            }
        }

        return super.onCreateViewHolder(parent, viewType)

    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            DashboardAdapter.VIEW_TYPE_ITEM -> {
                val holder = baseHolder as DashboardViewHolder
                val item = items[position] as StockPickingType
                val binding = holder.binding
                binding.item = item

                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as StockPickingType
                        listener.onItemClick(clickedItem)
                    }
                }
            }
        }
    }

    override fun clear() {
        rowItems.clear()
        rowItemsCopy.clear()
        super.clear()
    }

    fun filter(text: String) {
        var text = text
        items.clear()
        if (text.isEmpty()) {
            items.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy) {
                if (item.name.toLowerCase().contains(text)) {
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

}