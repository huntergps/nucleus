package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.SelectedOrderLineRowBinding
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.fragments.AddSaleFragment
import com.domatix.yevbes.nucleus.sales.interfaces.LongShortOrderItemClick
import io.reactivex.disposables.CompositeDisposable

class SelectedListOrderLineDataAdapter(
        val fragment: AddSaleFragment,
        items: ArrayList<Any>,
        val listener: LongShortOrderItemClick
) : RecyclerBaseAdapter(items, fragment.binding.rvSelectedOrderLines) {

    lateinit var compositeDisposable: CompositeDisposable private set

    companion object {
        const val TAG: String = "SelectedListOrderAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<ProductProduct> = ArrayList(
            items.filterIsInstance<ProductProduct>()
    )


    fun addRowItems(rowItemsAll: ArrayList<ProductProduct>) {
        this.rowItems.addAll(rowItemsAll)
        addAll(rowItemsAll.toMutableList<Any>() as ArrayList<Any>)
    }


    override fun getItemCount(): Int {
        return items.size
    }


    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is ProductProduct) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<ProductProduct>()))
    }

    fun removeItemAdapter(position: Int): ProductProduct {
        val activity = this.rowItems.removeAt(position)
        removeItem(position)
        return activity
    }

    fun restoreItem(item: ProductProduct, position: Int) {
        add(item, position)
        this.rowItems.add(position, item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = SelectedOrderLineRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnLongClickListener {
                    view -> listener.onLongItemClick(view)
                    return@setOnLongClickListener true
                }
                return SelectedListOrderLineViewHolder(binding)
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
                val holder = baseHolder as SelectedListOrderLineViewHolder
                val item = items[position] as ProductProduct
                val binding = holder.binding

                binding.productIdString = item.name
                binding.productDescString = item.name
                binding.qtyString = item.quantity.toString()
//                binding.priceUnitString = item.priceUnit.toString()
//                binding.subtotalString = (item.priceUnit * item.quantity).toString()
                binding.priceUnitString = item.lstPrice.toString()
                binding.subtotalString = (item.lstPrice * item.quantity).toString()
            }
        }
    }
}