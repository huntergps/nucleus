package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.AddProductAdapterRowBinding
import com.domatix.yevbes.nucleus.generic.callbacs.adapters.OnShortLongAdapterItemClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.views.OnViewLongClickListener
import com.domatix.yevbes.nucleus.generic.callbacs.views.OnViewShortClickListener
import com.domatix.yevbes.nucleus.sales.entities.CustomProductQtyEntity

class AddProductDataAdapter(
        val recyclerView: RecyclerView,
        items: ArrayList<Any>,
        val listener: OnShortLongAdapterItemClickListener,
        val listenerShort: OnViewShortClickListener,
        val listenerLong: OnViewLongClickListener
) : RecyclerBaseAdapter(items, recyclerView) {

    private val selectedProductList = ArrayList<CustomProductQtyEntity>()
    val selectedProducts = ArrayList<CustomProductQtyEntity>()


    companion object {
        const val TAG: String = "AddProductDataAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }


    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is CustomProductQtyEntity) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    fun removeProductItem(position: Int): CustomProductQtyEntity {
        removeItem(position)
        notifyItemRemoved(position)
        return selectedProductList.removeAt(position)
    }

    fun addProductRowItems(selectedProductList: ArrayList<CustomProductQtyEntity>) {
        this.selectedProductList.addAll(selectedProductList)
        addAll(selectedProductList.toMutableList<Any>() as ArrayList<Any>)
    }

    fun updateProductRowItem(position: Int, product: CustomProductQtyEntity) {
        selectedProductList[position] = product
        items[position] = product
        notifyItemChanged(position)
    }

    override fun clear() {
        selectedProductList.clear()
        super.clear()
    }

    val rowItemCount: Int get() = selectedProductList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                var binding = AddProductAdapterRowBinding.inflate(
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

                return AddProductViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as AddProductViewHolder
                val item = items[position] as CustomProductQtyEntity
                val binding = holder.binding

                val itemFounded = selectedProducts.find {
                    it.idProduct == item.idProduct
                }

                if (itemFounded == null)
                    binding.product = item
                else {
                    items[position] = itemFounded
                    binding.product = itemFounded
                }

                binding.buttonSubtract.setOnClickListener {
                    listenerShort.onShortClick(holder.itemView)
                }

                binding.buttonSubtract.setOnLongClickListener {
                    listenerLong.onLongClick(holder.itemView)
                    true
                }
            }
        }
    }
}
