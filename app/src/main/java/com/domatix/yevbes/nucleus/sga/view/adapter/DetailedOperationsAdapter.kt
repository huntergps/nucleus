package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityEditProductBinding
import com.domatix.yevbes.nucleus.databinding.DetailedOperationsRowBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockMoveLine
import com.domatix.yevbes.nucleus.sga.view.callbacks.OnItemClickUpdateListener


class DetailedOperationsAdapter(val binding: ActivityEditProductBinding,
                                items: ArrayList<Any>,
                                val listener: OnItemClickUpdateListener
) : RecyclerBaseAdapter(items, binding.rv) {
    companion object {
        const val TAG: String = "DetailedOperationsAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

    /*  private val storeListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
      private lateinit var compositeDisposable: CompositeDisposable
      private var products: ArrayList<ProductProduct> = ArrayList()*/


    private var rowItems: ArrayList<StockMoveLine> = ArrayList(
            items.filterIsInstance<StockMoveLine>()
    )

    private var mapItems = HashMap<Int, Boolean>()

    /* fun addProducts(products: ArrayList<ProductProduct>) {
         this.products.addAll(products)
         addAll(products.toMutableList<Any>() as ArrayList<Any>)
     }*/

    val rowItemCount: Int get() = rowItems.size
//    val prodC: Int get() = products.size

    fun addRowItems(rowItems: ArrayList<StockMoveLine>) {
        this.rowItems.addAll(rowItems)
        addAll(rowItems.toMutableList<Any>() as ArrayList<Any>)
    }

    fun addMapItems(mapItems: HashMap<Int, Boolean>) {
        this.mapItems.putAll(mapItems)
    }

    fun addMapItem(key: Int, value: Boolean) {
        this.mapItems[key] = value
        notifyDataSetChanged()
    }

    override fun clear() {
        rowItems.clear()
        mapItems.clear()
        super.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = DetailedOperationsRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                val holder = DetailedOperationsViewHolder(binding)
                binding.root.setOnClickListener {

                    listener.onItemClick(items[holder.adapterPosition], holder.adapterPosition)
                }
                return holder
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun getItemViewType(position: Int): Int {
        val o = items[position]
        if (o is StockMoveLine) {
            return VIEW_TYPE_ITEM
        }
        return super.getItemViewType(position)
    }

    fun updateItem(item: StockMoveLine, position: Int) {
        items[position] = item
        notifyItemChanged(position)
    }

    fun removeListItem(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(baseHolder: RecyclerView.ViewHolder, basePosition: Int) {
        super.onBindViewHolder(baseHolder, basePosition)
        val position = baseHolder.adapterPosition
//        compositeDisposable = CompositeDisposable()

        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as DetailedOperationsViewHolder
                val item = items[position] as StockMoveLine
                val binding = holder.binding
                binding.stockMoveLine = item
                binding.isShowLotsText = this.binding.isShowLotsText
                val productId = item.productId.asJsonArray[0].asInt
                binding.isLotOrLotId = mapItems[productId]
                binding.executePendingBindings()
                //getProduct("id", item.productId.asJsonArray.get(0))
//                binding.tvUnits.text = item.productQty.toString() + " Unit/s"
            }
        }
    }
}