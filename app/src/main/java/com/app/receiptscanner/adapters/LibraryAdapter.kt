package com.app.receiptscanner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.app.receiptscanner.R
import com.app.receiptscanner.adapters.LibraryItemLookup.Companion.OUT_OF_CONTEXT
import com.app.receiptscanner.databinding.ReceiptCardBinding
import com.app.receiptscanner.storage.NormalizedReceipt
import java.text.SimpleDateFormat
import java.util.*

class LibraryAdapter(
    private val context: Context,
    private var receipts: List<NormalizedReceipt>,
    private val onClick: (NormalizedReceipt) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {
    private var tracker: SelectionTracker<Long>? = null

    inner class ViewHolder(val binding: ReceiptCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // This is necessary to ensure that the receipt views are clickable
        init {
            binding.constraintLayout.isFocusable = true
            binding.constraintLayout.isClickable = true
            binding.container.isFocusable = true
            binding.container.isClickable = true
            binding.constraintLayout.setOnClickListener {
                val receipt = receipts[adapterPosition]
                onClick.invoke(receipt)
            }
        }

        // Used during recycler-view selection to get the details of each selected receipt
        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition() = adapterPosition
            override fun getSelectionKey() = itemId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ReceiptCardBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receipt = receipts[position]
        val date = Date(receipt.dateCreated)
        val format = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.UK)
        val cost = receipt.fields.getCost().firstOrNull()?.toBigDecimal()?.setScale(2)
        holder.binding.receiptName.text = receipt.name
        holder.binding.dateCreated.text = format.format(date)
        holder.binding.totalCost.text = context.getString(R.string.cost_field, cost)
        tracker?.let {
            holder.binding.constraintLayout.isActivated = it.isSelected(receipt.id.toLong())
        }
    }

    // Replaces the receipts that are loaded into the recycler view
    // Could probably replace the notifyDataSetChanged() by detecting the receipts that have been
    // added and those that have been removed, and using the dedicated notify methods
    // However, this is not done here as due to the limited scope, it adds needless complexity
    // for a slight speedup
    fun setReceipts(normalizedReceipts: List<NormalizedReceipt>) {
        receipts = normalizedReceipts
        notifyDataSetChanged()
    }

    override fun getItemCount() = receipts.size

    // Returns a stable ID for each receipt to be used during selection
    // Currently returns the time of creation since unix epoch,
    // but if possible should be replaced with a better unique id
    override fun getItemId(position: Int) = receipts[position].id.toLong()


    /**
     * Creates a recycler-view selection tracker for a given recyclerview. This tracker is stored
     * internally in the adapter for the purpose of highlighting selections, as well as to the
     * caller for whatever purpose it is needed for.
     *
     * @param recyclerView The recyclerview that the adapter is attached to
     * @return A selection tracker for the given recyclerview
     */
    fun getTracker(recyclerView: RecyclerView): SelectionTracker<Long> {
        // This selection predicate is necessary to stop the selection from being voided when the
        // user taps somewhere which is not a receipt (i.e. the space between receipts)
        val selectionPredicate = object : SelectionTracker.SelectionPredicate<Long>() {
            override fun canSetStateForKey(key: Long, nextState: Boolean) =
                key != OUT_OF_CONTEXT

            override fun canSetStateAtPosition(position: Int, nextState: Boolean) = true

            override fun canSelectMultiple() = true
        }
        tracker = SelectionTracker.Builder(
            "library-selection",
            recyclerView,
            ItemKeyProvider(recyclerView),
            LibraryItemLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(selectionPredicate).build()
        return tracker!!
    }

}