package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.R
import com.app.receiptscanner.adapters.ReceiptAdapter
import com.app.receiptscanner.databinding.FragmentReceiptBinding
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory
import java.util.*

class ReceiptFragment : Fragment() {
    private var locked = false
    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val receiptViewmodel: ReceiptViewmodel by activityViewModels {
        ReceiptViewmodelFactory(
            application.userRepository,
            application.receiptRepository,
            application
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val receipt = receiptViewmodel.getReceipt()
        binding.receiptTitle.editText?.setText(receipt.name)
        val adapter = ReceiptAdapter(activity, receipt) { key, content ->
            receiptViewmodel.setField(key, arrayListOf(content.toString()))
        }
        binding.receiptTitle.editText?.doAfterTextChanged {
            binding.receiptTitle.error = null
        }
        binding.receiptRecyclerView.adapter = adapter
        binding.receiptRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.createReceiptButton.setOnClickListener {
            // Gets the field which stores the total price of the receipt, and checks if it is in a
            // Valid number format. If it is not, the user is shown an error
            if (!checkValidCost(receipt)) {
                val position = receipt.fields.keys.indexOf(receipt.fields.costField)
                // Gets the View that holds the cost field
                val costLayout: ReceiptAdapter.ItemViewHolder? =
                    binding.receiptRecyclerView.findViewHolderForAdapterPosition(position) as? ReceiptAdapter.ItemViewHolder
                costLayout?.binding?.fieldInput?.error = "This field must contain a number"
                return@setOnClickListener
            }
            // Checks if the receipt's name is set
            when (!binding.receiptTitle.editText?.text.isNullOrBlank()) {
                true -> {
                    var isUpdate = true
                    // Only sets the date created if it is a new receipt
                    if (receipt.id != -1) {
                        val calendar = Calendar.getInstance()
                        val currentDate = calendar.timeInMillis
                        receipt.dateCreated = currentDate
                        isUpdate = false
                    }

                    // Sets the receipt's name
                    receipt.name = binding.receiptTitle.editText?.text.toString()

                    // Creates and stores the receipt on both the file system, and in the database
                    // This is done in a background thread
                    receiptViewmodel.createReceipt(receipt, activity.filesDir.path, "", isUpdate) {
                        receiptViewmodel.loadUserReceipts()
                        receiptViewmodel.updateData(receipt)
                        receiptViewmodel.clearReceipt()

                        // Navigates back to the user's library, this is done from the background
                        // thread to ensure that the viewmodel isn't cleared before the receipt has
                        // been fully stored
                        findNavController().popBackStack(R.id.userMainFragment, false)
                    }
                }
                false -> {
                    binding.receiptTitle.error =
                        "A name must be set before the receipt can be created."
                }
            }

        }
    }

    /**
     * Checks if the cost field can be parsed into a big decimal, returning true if it can
     * and false if it cannot, or is null
     *
     * @param receipt The receipt to have it's cost field checked
     * @return a boolean specifying whether the cost could be parsed into a big Decimal
     */
    private fun checkValidCost(receipt: NormalizedReceipt): Boolean {
        val value =
            receipt.fields[receipt.fields.costField]?.data?.firstOrNull().toString()
                .toBigDecimalOrNull()
        return (value != null)
    }

    override fun onStop() {
        if (!locked) receiptViewmodel.clearReceipt()
        super.onStop()
    }
}