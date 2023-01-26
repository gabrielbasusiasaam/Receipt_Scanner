package com.app.receiptscanner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.databinding.ReceiptFieldBinding

class ReceiptAdapter(
    private val context: Context,
    private val data: ArrayList<ReceiptField>,
    private val fields: HashMap<String, Any>
) : RecyclerView.Adapter<ReceiptAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ReceiptFieldBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ReceiptFieldBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val field = data[position]
        val value = when(fields.containsKey(field.title)) {
            true -> fields[field.title]
            else -> field.defaultValue
        }
        holder.binding.fieldTitle.text = field.alias
        holder.binding.fieldInput.editText?.let {
            it.setText(value.toString())
            it.doAfterTextChanged { content ->
                fields[field.title] = content.toString()
            }
        }
    }

    override fun getItemCount() = data.size
}