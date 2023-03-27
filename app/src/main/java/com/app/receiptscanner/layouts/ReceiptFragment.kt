package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.R
import com.app.receiptscanner.adapters.ReceiptAdapter
import com.app.receiptscanner.databinding.FragmentReceiptBinding
import com.app.receiptscanner.storage.StorageHandler
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory
import kotlinx.coroutines.launch

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
        val storageHandler = StorageHandler(activity)
        binding.receiptRecyclerView.adapter = ReceiptAdapter(
            activity,
            receipt
        )
        binding.receiptRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.createReceiptButton.setOnClickListener {
            when (!binding.receiptTitle.editText?.text.isNullOrBlank()) {
                true -> {
                    receiptViewmodel.createReceipt(System.currentTimeMillis(), "", "") {
                        lifecycleScope.launch {
                            receipt.name = binding.receiptTitle.editText?.text.toString()
                            storageHandler.storeReceipt(it, receipt, activity.filesDir.path)
                            receiptViewmodel.loadUserReceipts()
                        }
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

    override fun onStop() {
        super.onStop()
        receiptViewmodel.getReceipt().fields.clearAll()
    }
}