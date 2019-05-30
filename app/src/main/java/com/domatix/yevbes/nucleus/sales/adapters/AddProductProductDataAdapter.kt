package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityOrderLineListBinding
import com.domatix.yevbes.nucleus.databinding.AddSaleOrderLineRowBinding
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sales.activities.OrderLineListActivity
import com.domatix.yevbes.nucleus.sales.fragments.QuantityDialogFragment
import java.util.*

class AddProductProductDataAdapter(
        val binding: ActivityOrderLineListBinding,
        items: ArrayList<Any>
) : RecyclerBaseAdapter(items, binding.rvOrderLineList) {

    companion object {
        const val TAG: String = "AddProductProductDataAdapter"
        const val DIALOG_TAG: String = "Quantity Dialog Fragment"
        private const val VIEW_TYPE_ITEM = 0
    }

    var selectedList = ArrayList<ProductProduct>()

    private var rowItems: java.util.ArrayList<ProductProduct> = java.util.ArrayList(
            items.filterIsInstance<ProductProduct>()
    )

    // for SearchView
    private var rowItemsCopy: java.util.ArrayList<ProductProduct> = java.util.ArrayList(
            items.filterIsInstance<ProductProduct>()
    )

    fun addRowItems(rowItems: java.util.ArrayList<ProductProduct>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as java.util.ArrayList<Any>)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                var binding = AddSaleOrderLineRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return AddProductProductViewHolder(binding)
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
                val holder = baseHolder as AddProductProductViewHolder
                val item = items[position] as ProductProduct
                val binding = holder.binding

                binding.buttonSubtract.setOnClickListener {
                    if (item.quantity > 1f) {
                        binding.textViewQuantity.text = (--item.quantity).toString()
                    } else if (item.quantity <= 1f) {
                        binding.textViewQuantity.visibility = View.GONE
                        binding.buttonSubtract.visibility = View.GONE
                        item.quantity = 0f
                        item.checked = false
                        binding.textViewQuantity.text = (item.quantity).toString()
                    }

                    if (!item.checked) {
                        selectedList.remove(item)
                    }
                }

                binding.buttonSubtract.setOnLongClickListener {
                    binding.textViewQuantity.visibility = View.GONE
                    binding.buttonSubtract.visibility = View.GONE
                    item.quantity = 0f
                    item.checked = false
                    binding.textViewQuantity.text = (item.quantity).toString()

                    selectedList.remove(item)

                    true
                }

                binding.root.setOnClickListener {
                    binding.textViewQuantity.visibility = View.VISIBLE
                    binding.buttonSubtract.visibility = View.VISIBLE
                    binding.textViewQuantity.text = (++item.quantity).toString()

                    if (!item.checked) {
                        selectedList.add(item)
                    }

                    item.checked = true
                }

                binding.root.setOnLongClickListener {
                    /*val tvGson = Gson()
                    val textViewQuantity = tvGson.toJson(binding.textViewQuantity)*/

                    val dialogFragment = QuantityDialogFragment.newInstance(binding.textViewQuantity, binding.buttonSubtract, item, this)
                    val context = this.binding.root.context
                    val supportFragmentManager = (context as OrderLineListActivity).supportFragmentManager

                    dialogFragment.show(supportFragmentManager, DIALOG_TAG)

                    true
                }

                binding.productName = item.name
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
            for (item in rowItemsCopy)
                if (item.name.toLowerCase().contains(text)) {
                    items.add(item)
                }
        }
        notifyDataSetChanged()
    }

}