package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.sga.service.model.StockProductionLot
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener
import com.domatix.yevbes.nucleus.sga.view.ui.LotListDialogFragment
import com.domatix.yevbes.nucleus.databinding.GeneralItemRowBinding

class GeneralSearchDialogFragmentDataAdapter(val fragment: LotListDialogFragment,
                                             items: ArrayList<Any>,
                                             private val listener: OnItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.recyclerView) {
    companion object {
        const val TAG: String = "ProductDataAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockProductionLot) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private var rowItems: ArrayList<StockProductionLot> = ArrayList()
    private var rowItemsCopy: ArrayList<StockProductionLot> = ArrayList(
            items.filterIsInstance<StockProductionLot>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<StockProductionLot>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = GeneralItemRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return GeneralSearchDialogFragmentViewHolder(binding)
            }
        }

        return super.onCreateViewHolder(parent, viewType)

    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as GeneralSearchDialogFragmentViewHolder
                val item = items[position] as StockProductionLot
                val binding = holder.binding
                binding.tvName.text = item.displayName

                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as StockProductionLot

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
}