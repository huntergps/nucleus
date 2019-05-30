package com.domatix.yevbes.nucleus.company

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.company.entities.Company


class CompanyAdapter(private val companyList: ArrayList<Company>) : RecyclerView.Adapter<CompanyAdapter.MyViewHolder>() {

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView
        var name: TextView

        init {
            image = view.findViewById(R.id.listItemImage) as ImageView
            name = view.findViewById(R.id.listItemRow) as TextView
        }
    }

    private var rowItemsCopy: ArrayList<Company> = ArrayList(
            companyList.filterIsInstance<Company>()
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompanyAdapter.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val company = companyList[position]
        /*if (company.imageSmall != "false") {
            Company.loadImage(holder.image, company.imageSmall, company.name)
        }*/
        holder.image.visibility = View.GONE
        holder.name.text = company.name
    }

    override fun getItemCount(): Int {
        return companyList.size
    }

    fun addRowItems(rowItems: ArrayList<Company>) {
        this.rowItemsCopy.addAll(rowItems)
    }

    fun filter(text: String) {
        var text = text
        companyList.clear()
        if (text.isEmpty()) {
            companyList.addAll(rowItemsCopy)
        } else {
            text = text.toLowerCase()
            for (item in rowItemsCopy)
                if (item.name.toLowerCase().contains(text)) {
                    companyList.add(item)
                }
        }
        notifyDataSetChanged()
    }
}