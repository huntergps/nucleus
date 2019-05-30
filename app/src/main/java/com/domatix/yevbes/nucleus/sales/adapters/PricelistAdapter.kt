package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.PricelistRowBinding
import com.domatix.yevbes.nucleus.sales.activities.PricelistListActivity
import com.domatix.yevbes.nucleus.sales.callbacks.OnItemClickedListener
import com.domatix.yevbes.nucleus.sales.entities.ProductPricelist

class PricelistAdapter(
        val activity: PricelistListActivity,
        items: ArrayList<Any>,
        private val listener: OnItemClickedListener
) : RecyclerBaseAdapter(items, activity.binding.rv) {
    companion object {
        const val TAG: String = "PricelistAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is ProductPricelist) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private var rowItems: ArrayList<ProductPricelist> = ArrayList(
            items.filterIsInstance<ProductPricelist>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItemsAll: ArrayList<ProductPricelist>) {
        this.rowItems.addAll(rowItemsAll)
        addAll(rowItemsAll.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = PricelistRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener {
                    listener.onItemClicked(it)
                }
                return PricelistViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as PricelistViewHolder
                val item = items[position] as ProductPricelist
                val binding = holder.binding
                binding.pricelist = item

            }
        }
    }

    override fun clear() {
        rowItems.clear()
        super.clear()
    }
}