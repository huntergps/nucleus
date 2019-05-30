package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sga.view.ui.ProductDialogFragment
import com.domatix.yevbes.nucleus.databinding.ProductProductRowBinding
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickListener

class ProductDataAdapter(
        val fragment: ProductDialogFragment,
        items: ArrayList<Any>,
        private val listener: OnItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.recyclerViewProductList) {

    companion object {
        const val TAG: String = "ProductDataAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is ProductProduct) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private var rowItems: ArrayList<ProductProduct> = ArrayList()
    private var rowItemsCopy: ArrayList<ProductProduct> = ArrayList(
            items.filterIsInstance<ProductProduct>()
    )

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<ProductProduct>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ProductProductRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return ProductDataViewHolder(binding)
            }
        }

        return super.onCreateViewHolder(parent, viewType)

    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as ProductDataViewHolder
                val item = items[position] as ProductProduct
                val binding = holder.binding
                binding.product = item

                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as ProductProduct

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