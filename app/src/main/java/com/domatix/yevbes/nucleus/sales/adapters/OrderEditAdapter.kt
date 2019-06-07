package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.SaleOrderLineRowBinding
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine

class OrderEditAdapter(
        val recyclerView: RecyclerView,
        items: ArrayList<Any>,
        private val listener: OnShortLongAdapterItemClickListener
) : RecyclerBaseAdapter(items, recyclerView) {
    private val saleOrderLineRowItems: ArrayList<SaleOrderLine> = ArrayList()

    val rowItemCount: Int get() = saleOrderLineRowItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var binding = SaleOrderLineRowBinding.inflate(
                inflater,
                parent,
                false
        )
        binding.root.setOnClickListener { view ->
            listener.onShortAdapterItemPressed(view)
        }
        binding.root.setOnLongClickListener { view ->
            listener.onLongAdapterItemPressed(view)
            return@setOnLongClickListener true
        }

        return OrderEditViewHolder(binding)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        val holder = baseHolder as OrderEditViewHolder
        val item = items[position] as SaleOrderLine
        val binding = holder.binding
        binding.saleOrderLine = item
    }

    fun addSaleOrderLineRowItems(saleOrderLineRowItems: ArrayList<SaleOrderLine>) {
        this.saleOrderLineRowItems.addAll(saleOrderLineRowItems)
        addAll(saleOrderLineRowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    fun restoreItem(item: SaleOrderLine, position: Int) {
        add(item, position)
        this.saleOrderLineRowItems.add(position, item)
    }

    fun updateSaleOrderLineRowItem(position: Int, saleOrderLine: SaleOrderLine) {
        saleOrderLineRowItems[position] = saleOrderLine
        items[position] = saleOrderLine
        notifyItemChanged(position)
    }

    fun removeSaleOrderLineRowItem(position: Int) : SaleOrderLine {
        removeItem(position)
        notifyItemRemoved(position)
        return saleOrderLineRowItems.removeAt(position)
    }

    override fun clear() {
        saleOrderLineRowItems.clear()
        super.clear()
    }
}