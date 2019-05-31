package com.domatix.yevbes.nucleus.sales.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.*
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.SaleOrderRowBinding
import com.domatix.yevbes.nucleus.sales.entities.SaleOrder
import com.domatix.yevbes.nucleus.sales.fragments.SalesFragment
import com.domatix.yevbes.nucleus.sales.interfaces.ItemClickListener
import com.google.gson.Gson
import timber.log.Timber
import java.util.*

class SalesDataAdapter(
        val fragment: SalesFragment,
        items: ArrayList<Any>,
        val listener: ItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.salesRecyclerView) {


    companion object {
        const val TAG: String = "SaleOrderAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    private var rowItems: ArrayList<SaleOrder> = ArrayList(
            items.filterIsInstance<SaleOrder>()
    )

    // for SearchView
    private var rowItemsCopy: ArrayList<SaleOrder> = ArrayList(
            items.filterIsInstance<SaleOrder>()
    )


    fun addRowItems(rowItems: ArrayList<SaleOrder>) {
        this.rowItems.addAll(rowItems)
        this.rowItemsCopy.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is SaleOrder) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<SaleOrder>()))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                var binding = SaleOrderRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener {
                    listener.onItemClick(it)
                }
                return SalesViewHolder(binding)
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
                val holder = baseHolder as SalesViewHolder
                val item = items[position] as SaleOrder
                val binding = holder.binding

                val date = fromStringToDate(item.dateOrder, "yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateOrder = getDateToFriendlyFormat(date, "dd MMM", Locale.getDefault(), TimeZone.getTimeZone("GMT+01:00")).toLowerCase()
                val amountTotal = "%.2f".format(item.amountTotal).replace('.', '%').replace(',', '.').replace('%', ',')
                val state = saleStates(item.state, fragment)
                val name = item.name + " (${jsonElementToString(item.partnerId)})"

                binding.dateOrderString = dateOrder
                binding.amountTotalString = amountTotal
                binding.stateString = state
                binding.nameString = name
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

                if (item.name.toLowerCase().contains(text) ||
                        item.partnerId.asJsonArray.get(1).asString.toLowerCase().contains(text)) {
                    items.add(item)
                }
        }
        notifyDataSetChanged()
    }


  /*  fun filter(text: String) {
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
                if (item.partnerId.toString().toLowerCase().contains(text)) {
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }*/
}