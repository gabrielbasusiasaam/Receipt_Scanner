package com.app.receiptscanner.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.databinding.ReceiptCardBinding
import com.app.receiptscanner.storage.NormalizedReceipt
import java.text.SimpleDateFormat
import java.util.*

class LibraryAdapter(
    private val context: Context,
    private var receipts: List<NormalizedReceipt>
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ReceiptCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ReceiptCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = receipts[position]
        val date = Date(receipt.dateCreated)
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.UK)

        holder.binding.receiptName.text = receipt.name
        holder.binding.dateCreated.text = format.format(date)
    }
    fun setReceipts(normalizedReceipts: List<NormalizedReceipt>) {
        Log.e("RECEIPTS", normalizedReceipts.toString())
        receipts = normalizedReceipts
        notifyDataSetChanged()
    }
    override fun getItemCount() = receipts.size
}