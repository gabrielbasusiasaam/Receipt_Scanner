package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.app.receiptscanner.databinding.FragmentReceiptGroupBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication

class ReceiptGroupFragment : Fragment() {
    private var _binding: FragmentReceiptGroupBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptGroupBinding.inflate(inflater, container, false)
        return binding.root
    }
}