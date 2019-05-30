package com.domatix.yevbes.nucleus.products

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.products.entities.ProductProduct

//class CountryAdapter(private val countryList: ArrayList<Country>) : RecyclerView.Adapter<CountryAdapter.MyViewHolder>() {

class ProductDataAdapter(private val list: ArrayList<ProductProduct>) : RecyclerView.Adapter<ProductDataAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setClickListener(itemClickListener: OnItemClickListener) {
        this.onItemClickListener = itemClickListener
    }

    private var onItemClickListener: OnItemClickListener? = null

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView = view.findViewById<TextView>(R.id.tvProductName)!!
    }

    private var rowItemsCopy: ArrayList<ProductProduct> = ArrayList(
            list.filterIsInstance<ProductProduct>()
    )

    fun addRowItems(rowItems: ArrayList<ProductProduct>) {
        this.rowItemsCopy.addAll(rowItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_name_recyclerview_row, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]
        holder.textView.text = product.name
        holder.itemView.setOnClickListener {
            onItemClickListener?.onItemClick(holder.itemView, holder.adapterPosition)
        }
    }

    fun filter(text: String) {
        var text = text
        list.clear()
        if (text.isEmpty()) {
            list.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy)
                if (item.name.toLowerCase().contains(text)) {
                    list.add(item)
                }
        }
        notifyDataSetChanged()
    }

    fun getItem(position: Int): ProductProduct {
        return list[position]
    }


}