package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.FragmentTransfersBinding
import com.domatix.yevbes.nucleus.databinding.TransferRowBinding
import com.domatix.yevbes.nucleus.jsonElementToString
import com.domatix.yevbes.nucleus.sga.service.model.StockPicking
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener
import com.domatix.yevbes.nucleus.trimFalse

class TransfersAdapter(
        val binding: FragmentTransfersBinding,
        items: ArrayList<Any>,
        val listener: OnTransferItemClickListener
) : RecyclerBaseAdapter(items, binding.rv) {

    companion object {
        const val TAG: String = "TransfersAdapter"
        private const val VIEW_ITEM_TYPE = 0
    }

    private var rowItems: ArrayList<StockPicking> = ArrayList(
            items.filterIsInstance<StockPicking>()
    )

    private var rowItemsCopy: ArrayList<StockPicking> = ArrayList(
            items.filterIsInstance<StockPicking>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<StockPicking>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun clear() {
        rowItems.clear()
        rowItemsCopy.clear()
        super.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_ITEM_TYPE -> {
                val binding = TransferRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener { view ->
                    listener.onItemClick(view)
                }
                return TransfersViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockPicking) {
            return VIEW_ITEM_TYPE
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_ITEM_TYPE -> {
                val holder = baseHolder as TransfersViewHolder
                val transfer = items[position] as StockPicking
                val binding = holder.binding

                binding.transfer = transfer
            }
        }
    }

    fun filter(text: String) {
        var text = text
        items.clear()
        if (text.isEmpty()) {
            items.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy) {
                if (item.name.trimFalse().toLowerCase().contains(text)
                        || item.origin.trimFalse().toLowerCase().contains(text)
                        || jsonElementToString(item.partnerId).toLowerCase().contains(text)) {
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}

