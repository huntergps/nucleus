package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.FragmentInventorySettingsBinding
import com.domatix.yevbes.nucleus.databinding.InventoryRowBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockInventory
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener

class InventorySettingsAdapter (
        val binding: FragmentInventorySettingsBinding,
        items: ArrayList<Any>,
        val listener: OnTransferItemClickListener
) : RecyclerBaseAdapter(items, binding.rv) {

    companion object {
        const val TAG: String = "TransfersAdapter"
        private const val VIEW_ITEM_TYPE = 0
    }

    private var rowItems: ArrayList<StockInventory> = ArrayList(
            items.filterIsInstance<StockInventory>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<StockInventory>) {
        this.rowItems.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_ITEM_TYPE -> {
                val binding = InventoryRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener { view ->
                    listener.onItemClick(view)
                }
                return InventorySettingsViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockInventory) {
            return VIEW_ITEM_TYPE
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_ITEM_TYPE -> {
                val holder = baseHolder as InventorySettingsViewHolder
                val inventory = items[position] as StockInventory
                val binding = holder.binding

                binding.inventory = inventory
            }
        }
    }

}