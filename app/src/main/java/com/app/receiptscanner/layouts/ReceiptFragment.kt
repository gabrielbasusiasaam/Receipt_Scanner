package com.app.receiptscanner.layouts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        Log.e("Receipt Fields", receipt.fields.hashCode().toString())
        val adapter = ReceiptAdapter(activity, receipt) { key, content ->
            receiptViewmodel.setField(key, arrayListOf(content.toString()))
        }
        binding.receiptRecyclerView.adapter = adapter
        binding.receiptRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.createReceiptButton.setOnClickListener {
            if (!checkValidCost(receipt)) {
                val position = receipt.fields.getMap().keys.indexOf(receipt.fields.totalField)
                val costLayout: ReceiptAdapter.ViewHolder? =
                    binding.receiptRecyclerView.findViewHolderForAdapterPosition(position) as? ReceiptAdapter.ViewHolder
                costLayout?.binding?.fieldInput?.error = "This field must contain a number"
                return@setOnClickListener
            }
            when (!binding.receiptTitle.editText?.text.isNullOrBlank()) {
                true -> {
                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.timeInMillis
                    receipt.name = binding.receiptTitle.editText?.text.toString()
                    receipt.dateCreated = currentDate
                    receiptViewmodel.createReceipt(receipt, activity.filesDir.path, "") {
                        receiptViewmodel.loadUserReceipts()
                        receiptViewmodel.clearReceipt()
                    }
                    findNavController().popBackStack(R.id.userMainFragment, false)
                }
                false -> {
                    binding.receiptTitle.error =
                        "A name must be set before the receipt can be created."
                }
            }

        }
    }

    private fun checkValidCost(receipt: NormalizedReceipt): Boolean {
        val value = receipt.fields.get(receipt.fields.totalField).toString().toFloatOrNull()
        return (value != null)
    }

    override fun onStop() {
        super.onStop()
        receiptViewmodel.clearReceipt()
    }
}