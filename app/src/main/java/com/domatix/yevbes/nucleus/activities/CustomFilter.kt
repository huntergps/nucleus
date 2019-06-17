package com.domatix.yevbes.nucleus.activities

import android.widget.Filter
import com.domatix.yevbes.nucleus.activities.adapters.ActivityDataAdapter
import com.domatix.yevbes.nucleus.activities.entities.Activity


class CustomFilter(private var adapter: ActivityDataAdapter, private var filterList: java.util.ArrayList<Any>, private var items: ArrayList<Any>) : Filter() {

    /*override fun performFiltering(p0: CharSequence?): FilterResults {
        var p0 = p0
        val results = FilterResults()
        if (p0 != null && p0.isNotEmpty()) {
            p0 = p0.toString().toUpperCase()
            val filteredActivities: ArrayList<Any> = ArrayList()
            for (i in 0 until filterList.size) {
                if ((filterList[i] as Activity).summary.toUpperCase().contains(p0)) {
                    filteredActivities.add(filterList[i])
                }
            }

            results.count = filteredActivities.size
            results.values = filteredActivities

        } else {
            results.count = filterList.size
            results.values = filterList
        }

        return results

    }

    override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
        adapter.items = p1?.values as ArrayList<Any>
        adapter.notifyDataSetChanged()
    }*/

    override fun performFiltering(charSequence: CharSequence): FilterResults {
        val charString = charSequence.toString()

        if (charString.isEmpty()) {
            filterList = items
        } else {
            val filteredList = ArrayList<Any>()

            for (row in items) {
                if ((row as Activity).summary.toLowerCase().contains(charString.toLowerCase())) {
                    filteredList.add(row)
                }
            }
            filterList = filteredList
        }

        val filterResults = Filter.FilterResults()
        filterResults.values = filterList
        return filterResults
    }

    override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
        //filterList = filterResults.values as ArrayList<Any>
        adapter.items = filterResults.values as ArrayList<Any>
        adapter.notifyDataSetChanged()
    }
}