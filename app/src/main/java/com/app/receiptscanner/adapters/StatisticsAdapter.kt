package com.app.receiptscanner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.R
import com.app.receiptscanner.databinding.StatisticsItemBinding

class StatisticsAdapter(
    private val context: Context,
    private val items: List<Pair<String, Any>>
) : RecyclerView.Adapter<StatisticsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: StatisticsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = StatisticsItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        // Formats the item's text based on the format specified in the strings.xml file
        holder.binding.itemTitle.text =
            context.getString(R.string.result_format, item.first, item.second.toString())
    }

    override fun getItemCount() = items.size

}