package com.domatix.yevbes.nucleus.country

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.country.entities.Country

class CountryAdapter(private val countryList: ArrayList<Country>) : RecyclerView.Adapter<CountryAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView
        var name: TextView

        init {
            image = view.findViewById(R.id.listItemImage) as ImageView
            name = view.findViewById(R.id.listItemRow) as TextView
        }
    }

    private var rowItemsCopy: ArrayList<Country> = ArrayList(
            countryList.filterIsInstance<Country>()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val country = countryList[position]

        if (country.image != "false") {
            Country.loadImage(holder.image, country.image, country.name)
        }
        holder.name.text = country.name
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    fun addRowItems(rowItems: ArrayList<Country>) {
        this.rowItemsCopy.addAll(rowItems)
    }

    fun filter(text: String) {
        var text = text
        countryList.clear()
        if (text.isEmpty()) {
            countryList.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy)
                if (item.name.toLowerCase().contains(text)) {
                    countryList.add(item)
                }
        }
        notifyDataSetChanged()
    }

}