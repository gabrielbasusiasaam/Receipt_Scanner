package com.app.receiptscanner.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class LibraryItemLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {
    /**
     * This represents the user pressing somewhere which is not a receipt
     */
    private val outOfContextSelection = object : ItemDetails<Long>() {
        override fun getPosition() = OUT_OF_CONTEXT.toInt()
        override fun getSelectionKey() = OUT_OF_CONTEXT
    }

    override fun getItemDetails(e: MotionEvent): ItemDetails<Long> {
        val view = recyclerView.findChildViewUnder(e.x, e.y)

        // Checks if there was a view underneath where the user pressed, returning the details of
        // the view if there is, and otherwise outOfContextSelection.
        return when (view != null) {
            true -> (recyclerView.getChildViewHolder(view) as LibraryAdapter.ViewHolder).getItemDetails()
            false -> outOfContextSelection
        }
    }

    companion object {
        const val OUT_OF_CONTEXT = 1000000L
    }
}