package com.domatix.yevbes.nucleus.activities.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.domatix.yevbes.nucleus.R
import com.domatix.yevbes.nucleus.activities.activities.DetailActivityActivity
import com.domatix.yevbes.nucleus.modelDetailsListener
import com.google.gson.JsonElement

class CustomerAdapter(
        val items: ArrayList<JsonElement>,
        val activity: DetailActivityActivity
): RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {
    inner class CustomerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_activity_customer, parent, false)
        return CustomerViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = items[position]
        holder.name.text = customer.asJsonObject["display_name"].asString
        val id = customer.asJsonObject["id"].asInt
        holder.itemView.setOnClickListener(modelDetailsListener(id, activity, holder.name, "res.partner"))
    }

    fun addItems(list: List<JsonElement>) {
        items.addAll(list)
        notifyItemRangeInserted(itemCount,list.size)
    }

}