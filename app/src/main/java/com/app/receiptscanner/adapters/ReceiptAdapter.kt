package com.app.receiptscanner.adapters

import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.databinding.ReceiptFieldBinding
import com.app.receiptscanner.storage.NormalizedReceipt

class ReceiptAdapter(
    private val context: Context,
    private val receipt: NormalizedReceipt,
    private val onTextChanged: (ArrayList<String>, Editable?) -> Unit
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ReceiptFieldBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ReceiptFieldBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (key, field) = receipt.fields.get(position)
        holder.binding.fieldInput.hint = field.alias
        holder.binding.fieldInput.editText?.let {
            it.setText(field.data.firstOrNull() ?: "")
            it.doAfterTextChanged { content ->
                Log.e("Receipt Fields ID", receipt.fields.hashCode().toString())
                onTextChanged.invoke(key, content)
            }
        }
    }

    override fun getItemCount() = receipt.fields.getMap().size
}