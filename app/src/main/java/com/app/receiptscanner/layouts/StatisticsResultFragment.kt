package com.app.receiptscanner.layouts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.receiptscanner.R
import com.app.receiptscanner.adapters.StatisticsAdapter
import com.app.receiptscanner.databinding.FragmentStatisticsResultBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.StatisticsViewmodel

class StatisticsResultFragment : Fragment() {
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
        statisticsViewmodel.calculateStatistics { results ->
            statisticsViewmodel.setResults(results)
            val items = listOf(
                Pair("Count", results.count),
                Pair("Total", results.sum),
                Pair("Max", results.max),
                Pair("Min", results.min),
                Pair("Mean", results.mean),
                Pair("Mode", results.mode),
                Pair("Lower Quartile", results.lowerQuartile),
                Pair("Median", results.median),
                Pair("Upper Quartile", results.upperQuartile),
                Pair("Standard Deviation", results.standardDeviation),
                Pair("Variance", results.variance)
            )
            val adapter = StatisticsAdapter(activity, items)
            binding.resultList.adapter = adapter
            binding.resultList.layoutManager = LinearLayoutManager(activity)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewGraphButton.setOnClickListener {
            findNavController().navigate(R.id.action_statisticsResultFragment_to_fragmentCostTimeGraph)
        }

        binding.viewDistributionButton.setOnClickListener {
            findNavController().navigate(R.id.action_statisticsResultFragment_to_providerDistributionFragment)
        }
    }
}