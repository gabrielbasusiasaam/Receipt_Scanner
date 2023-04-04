package com.app.receiptscanner.layouts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.receiptscanner.databinding.FragmentStatisticsResultBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.StatisticsViewmodel

class StatisticsResultFragment: Fragment() {
    private var _binding: FragmentStatisticsResultBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val statisticsViewmodel: StatisticsViewmodel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsResultBinding.inflate(inflater, container, false)
        statisticsViewmodel.calculateStatistics {
            Log.e("RESULT", it.toString())
        }
        return binding.root
    }
}