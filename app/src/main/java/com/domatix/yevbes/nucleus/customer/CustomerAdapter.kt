package com.domatix.yevbes.nucleus.customer

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.customer.entities.Customer
import com.domatix.yevbes.nucleus.databinding.ItemViewCustomerBinding
import com.google.gson.Gson

class CustomerAdapter(
        val fragment: CustomerFragment,
        items: ArrayList<Any>
) : RecyclerBaseAdapter(items, fragment.binding.rv) {

    companion object {
        const val TAG: String = "CustomerAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<Customer> = ArrayList(
            items.filterIsInstance<Customer>()
    )

    // for SearchView
    private var rowItemsCopy: ArrayList<Customer> = ArrayList(
            items.filterIsInstance<Customer>()
    )

    fun getCustomerItems() : ArrayList<Customer> {
        return rowItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemViewCustomerBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return CustomerViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as CustomerViewHolder
                val item = items[position] as Customer
                val binding = holder.binding
                binding.customer = item

                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as Customer
                        val intent = Intent(baseHolder.itemView.context, CustomerProfileActivity::class.java)
                        val customerGson = Gson()
                        val customerGsonAsAString = customerGson.toJson(clickedItem)
                        intent.putExtra(CustomerProfileActivity.CUSTOMER_OBJECT, customerGsonAsAString)
                        baseHolder.itemView.context.startActivity(intent)
                        //Timber.v(String.format("%s : %s", clickedPosition, clickedItem.name))
                    }
                }
            }
        }
    }

    val rowItemCount: Int get() = rowItems.size

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is Customer) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    fun addRowItems(rowItems: ArrayList<Customer>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    fun removeRowItems(rowItems: ArrayList<Customer>) {
        this.rowItems.removeAll(rowItems)
        this.rowItemsCopy.removeAll(rowItems)
        removeAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    fun filter(text: String) {
        var text = text
        items.clear()
        if (text.isEmpty()) {
            items.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy)
                if (item.name.toLowerCase().contains(text) || item.email.toLowerCase().contains(text)) {
                    items.add(item)
                }
        }
        notifyDataSetChanged()
    }

    override fun clear() {
        rowItems.clear()
        rowItemsCopy.clear()
        super.clear()
    }


}
