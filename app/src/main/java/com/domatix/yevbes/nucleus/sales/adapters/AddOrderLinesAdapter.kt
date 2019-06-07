package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.SaleOrderLineRowBinding
import com.domatix.yevbes.nucleus.jsonElementToString
import com.domatix.yevbes.nucleus.sales.entities.SaleOrderLine
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment
import com.domatix.yevbes.nucleus.sales.interfaces.LongShortOrderItemClick
import kotlinx.android.synthetic.main.fragment_add_sale.view.*

class AddOrderLinesAdapter(
        val fragment: AddSaleFragment,
        items: ArrayList<Any>,
        private val listener: LongShortOrderItemClick
) : RecyclerBaseAdapter(items, fragment.binding.root.rvSelectedOrderLines) {
    companion object {
        const val TAG: String = "SaleOrderLineAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: java.util.ArrayList<SaleOrderLine> = java.util.ArrayList(
            items.filterIsInstance<SaleOrderLine>()
    )

    // for SearchView
    private var rowItemsCopy: java.util.ArrayList<SaleOrderLine> = java.util.ArrayList(
            items.filterIsInstance<SaleOrderLine>()
    )

    fun addRowItems(rowItems: java.util.ArrayList<SaleOrderLine>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as java.util.ArrayList<Any>)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is SaleOrderLine) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<SaleOrderLine>()))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                var binding = SaleOrderLineRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener { view ->
                    listener.onItemClick(view)
                }
                binding.root.setOnLongClickListener { view ->
                    listener.onLongItemClick(view)
                    return@setOnLongClickListener true
                }
                return SaleOrderLineViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun clear() {
        rowItems.clear()
        rowItemsCopy.clear()
        super.clear()
    }

    val rowItemCount: Int get() = rowItems.size

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition

        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as SaleOrderLineViewHolder
                val item = items[position] as SaleOrderLine
                val binding = holder.binding
                binding.saleOrderLine = item

            }
        }
    }

    fun addRowItem(saleOrderLine: SaleOrderLine) {
        this.rowItems.add(saleOrderLine)
        this.rowItemsCopy.add(saleOrderLine)
        add(saleOrderLine,0)
//        addAll(rowItems.toMutableList<Any>() as java.util.ArrayList<Any>)
    }
}