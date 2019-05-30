package com.domatix.yevbes.nucleus.sga.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.domatix.yevbes.nucleus.core.utils.recycler.RecyclerBaseAdapter
import com.domatix.yevbes.nucleus.databinding.ActivityDetailTransferBinding
import com.domatix.yevbes.nucleus.databinding.MoveLineRowBinding
import com.domatix.yevbes.nucleus.sga.service.model.StockMove

class DetailTransferAdapter(
        val binding: ActivityDetailTransferBinding,
        items: ArrayList<Any>
): RecyclerBaseAdapter(items, binding.rv) {

    companion object {
        const val TAG: String = "DetailTransferAdapter"
        private const val VIEW_TYPE_ITEM = 0
    }

  /*  private val storeListType = object : TypeToken<ArrayList<ProductProduct>>() {}.type
    private lateinit var compositeDisposable: CompositeDisposable
    private var products: ArrayList<ProductProduct> = ArrayList()*/


    private var rowItems: ArrayList<StockMove> = ArrayList(
            items.filterIsInstance<StockMove>()
    )

   /* fun addProducts(products: ArrayList<ProductProduct>) {
        this.products.addAll(products)
        addAll(products.toMutableList<Any>() as ArrayList<Any>)
    }*/

    val rowItemCount: Int get() = rowItems.size
//    val prodC: Int get() = products.size

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
                val binding = MoveLineRowBinding.inflate(
                        inflater,
                        parent,
                        false
                )
                return DetailTransferViewHolder(binding)
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
//        compositeDisposable = CompositeDisposable()

        when (getItemViewType(basePosition)) {
            VIEW_TYPE_ITEM -> {
                val holder = baseHolder as DetailTransferViewHolder
                val item = items[position] as StockMove
                val binding = holder.binding
                binding.moveLine = item
                //getProduct("id", item.productId.asJsonArray.get(0))
//                binding.tvUnits.text = item.productQty.toString() + " Unit/s"
            }
        }
    }
}


