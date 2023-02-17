package com.app.receiptscanner.layouts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.adapters.ReceiptAdapter
import com.app.receiptscanner.adapters.ReceiptField
import com.app.receiptscanner.databinding.FragmentReceiptBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.ReceiptViewmodelFactory

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
        val fields = receiptViewmodel.getFields()
        Log.e("FIELDS", fields.toString())
        binding.receiptRecyclerView.adapter = ReceiptAdapter(
            activity,
            arrayListOf(
                ReceiptField("Title", "Title", 0),
                ReceiptField("Pay", "Cost", 0),
                ReceiptField("MID", "MID",  0),
                ReceiptField("Items", "Items", 0),
                ReceiptField("CARD", "Card", 0)
            ),
            fields
        )
        binding.receiptRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStop() {
        super.onStop()
        receiptViewmodel.clearFields()
    }
}