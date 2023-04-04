package com.app.receiptscanner.layouts

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

// Courtesy of https://developer.android.com/develop/ui/views/components/pickers
/**
 * A dialog prompting the user to select a date whilst also enabling a callback function
 * to be executed once the date has been selected.
 */
class DateSelectionDialog : DialogFragment() {
    private val activity by lazy { requireActivity() as MainActivity }
    private var onDateSetListener: DatePickerDialog.OnDateSetListener? = null
    private var year: Int = -1
    private var month: Int = -1
    private var day: Int = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(activity, onDateSetListener, year, month, day)
    }

    companion object {
        /**
         * Creates an instance of a Date Selection dialog with an initial date set, defaulting
         * to the current date if null is passed for any of the three date parameters
         *
         * @param year The initial year to display
         * @param month The initial month to display
         * @param day The initial day to display
         * @param dateSetListener The callback that should execute once the user has selected a date
         *
         * @return An instance of a DateSelectionDialog
         * @see DateSelectionDialog
         */
        fun newInstance(
            year: Int?,
            month: Int?,
            day: Int?,
            dateSetListener: DatePickerDialog.OnDateSetListener
        ): DateSelectionDialog {
            val dialog = DateSelectionDialog()
            if (year != null && month != null && day != null) {
                dialog.year = year
                dialog.month = month
                dialog.day = day
            } else {
                val calendar = Calendar.getInstance()
                dialog.year = calendar.get(Calendar.YEAR)
                dialog.month = calendar.get(Calendar.MONTH)
                dialog.day = calendar.get(Calendar.DAY_OF_MONTH)
            }
            dialog.onDateSetListener = dateSetListener
            return dialog
        }
    }
}