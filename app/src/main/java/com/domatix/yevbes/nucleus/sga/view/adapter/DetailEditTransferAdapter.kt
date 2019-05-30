package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityDetailEditTransferBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockMove
import com.domatix.yevbes.nucleus.databinding.TransferProductRowBinding
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnEditItemClickListener

class DetailEditTransferAdapter(
        val binding: ActivityDetailEditTransferBinding,
        items: ArrayList<Any>,
        private val listener: OnEditItemClickListener
): RecyclerBaseAdapter(items, binding.rv) {

    companion object {
        const val TAG: String = "DetailTransferAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<StockMove> = ArrayList(
            items.filterIsInstance<StockMove>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<StockMove>) {
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
            VIEW_TYPE_ITEM -> {
                val bindingRow = TransferProductRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                val holder = DetailEditTransferViewHolder(bindingRow)
                bindingRow.bEdit.setOnClickListener {
                    val position = holder.adapterPosition
                    listener.onItemClick(items[position])
                }
                if (binding.state == "done"){
                    bindingRow.bEdit.visibility = View.GONE
                    bindingRow.bEdit.setOnClickListener(null)
                }

                return holder
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockMove) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition

        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as DetailEditTransferViewHolder
                val item = items[position] as StockMove
                val binding = holder.binding
                binding.transfer = item
            }
        }
    }
}