package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.StoreRowBinding
import com.domatix.yevbes.nucleus.products.entities.ProductProduct
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnTransferItemClickListener
import com.domatix.yevbes.nucleus.sga.view.ui.StoreFragment
import com.google.gson.Gson

class StoreAdapter(
        val fragment: StoreFragment,
        items: ArrayList<Any>,
        val listener: OnTransferItemClickListener
) : RecyclerBaseAdapter(items, fragment.binding.rv) {

    companion object {
        const val TAG: String = "StoreAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    // for SearchView
    private var rowItems: ArrayList<ProductProduct> = ArrayList()
    private var rowItemsCopy: ArrayList<ProductProduct> = ArrayList(
            items.filterIsInstance<ProductProduct>()
    )

    private fun updateRowItems() {
        updateSearchItems()
        rowItems.clear()
        rowItems.addAll(ArrayList(
                items.filterIsInstance<ProductProduct>()))
    }

    val rowItemCount: Int get() = rowItems.size

    fun addRowItems(rowItems: ArrayList<ProductProduct>) {
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
            VIEW_TYPE_ITEM -> {
                val binding = StoreRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                binding.root.setOnClickListener { view ->
                    listener.onItemClick(view)
                }
                return StoreViewHolder(binding)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is ProductProduct) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as StoreViewHolder
                val item = items[position] as ProductProduct
                val binding = holder.binding

                binding.product = item

                if (!binding.root.hasOnClickListeners()) {
                    binding.root.setOnClickListener {
                        val clickedPosition = holder.adapterPosition
                        val clickedItem = items[clickedPosition] as ProductProduct

                        val productProductGson = Gson()
                        val productProductGsonAsString = productProductGson.toJson(clickedItem)

//                        val detailProductFragment = DetailProductFragment.newInstance(productProductGsonAsString)

/*
                        if (fragment.frameLayout != null) {
                            frameLayout = fragment.frameLayout
                        }

                        if (::frameLayout.isInitialized) {
                            fragment.fragmentManager!!.beginTransaction()
                                    .replace(R.id.frameLayout, detailProductFragment)
                                    .addToBackStack(null)
                                    .commit()
                        } else {*/
                        /*fragment.fragmentManager!!.beginTransaction()
                                .replace(R.id.clMain, detailProductFragment)
                                .addToBackStack(null)
                                .commit()*/
                        // }
                    }
                }
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
                if (item.name.toLowerCase().contains(text)) {
                    items.add(item)
                }
                if (item.defaultCode.toLowerCase().contains(text)) {
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}
