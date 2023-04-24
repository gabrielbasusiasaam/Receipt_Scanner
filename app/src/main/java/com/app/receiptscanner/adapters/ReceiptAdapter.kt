package com.app.receiptscanner.adapters

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.databinding.ReceiptFieldBinding
import com.app.receiptscanner.databinding.ReceiptItemBinding
import com.app.receiptscanner.databinding.ReceiptItemListBinding
import com.app.receiptscanner.parser.FieldMap.Companion.FIELD_LIST
import com.app.receiptscanner.storage.NormalizedReceipt

class ReceiptAdapter(
    private val context: Context,
    private val receipt: NormalizedReceipt,
    private val onTextChanged: (List<String>, Editable?) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class ItemViewHolder(val binding: ReceiptFieldBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ListViewHolder(val binding: ReceiptItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            // This makes it so that the error message shown is removed when the user types
            binding.addItemField.editText?.doAfterTextChanged {
                binding.addItemField.error = null
            }
        }

        /**
         * This method is used to add an item to the item list. It does not, however, add it to the
         * actual receipt, so that must be handled elsewhere
         *
         * @param title The name of the item to add
         */
        fun addItem(title: String) {
            val layoutInflater = LayoutInflater.from(context)
            val listItem = ReceiptItemBinding
                .inflate(layoutInflater, binding.itemList, false)
            listItem.root.text = title
            binding.itemList.addView(listItem.root)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        // Returns a different type of view based on the type of field it is
        return when (viewType) {
            FIELD_LIST -> {
                val binding = ReceiptItemListBinding.inflate(layoutInflater, parent, false)
                ListViewHolder(binding)
            }
            else -> {
                val binding = ReceiptFieldBinding.inflate(layoutInflater, parent, false)
                ItemViewHolder(binding)
            }
        }
    }

    // Returns the type of the receipt, for use in deciding the type of view for each field
    // These types are stored in the original templates for the fields
    override fun getItemViewType(position: Int) = receipt.fields.get(position).second.type

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val (key, field) = receipt.fields.get(position)
        when (field.type) {
            FIELD_LIST -> {
                val listHolder = holder as ListViewHolder

                // Replaces the currently shown items with the items for this field
                listHolder.binding.itemList.removeAllViews()
                field.data.map {
                    listHolder.addItem(it)
                }

                // Adds an item to the item list when the user presses the end icon
                listHolder.binding.addItemField.setEndIconOnClickListener {
                    val text = listHolder.binding.addItemField.editText?.text.toString()
                    if (text.isNotEmpty()) {
                        // Adds the item to the UI and actual receipt separately
                        listHolder.addItem(text)
                        field.data.add(text)

                        // Clears the input once done to avoid the user accidentally adding the same
                        // item multiple times
                        listHolder.binding.addItemField.editText?.text?.clear()
                    } else {
                        listHolder.binding.addItemField.error = "Item must have a name"
                    }
                }
            }
            else -> {
                val itemHolder = holder as ItemViewHolder
                itemHolder.binding.fieldInput.hint = field.alias
                itemHolder.binding.fieldInput.editText?.let {
                    // Sets the text in the input to the initial data or, if the view was loaded and
                    // unloaded, the previously entered text
                    it.setText(field.data.firstOrNull() ?: "")

                    // Removes the shown error when the user types
                    it.doAfterTextChanged { content ->
                        itemHolder.binding.fieldInput.error = null
                        onTextChanged.invoke(key, content)
                    }
                }
            }
        }
    }

    override fun getItemCount() = receipt.fields.size
}