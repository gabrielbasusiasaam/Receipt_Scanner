package com.app.receiptscanner.layouts

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.receiptscanner.databinding.FragmentProviderDistributionBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.StatisticsViewmodel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement

class ProviderDistributionFragment : Fragment() {
    private var _binding: FragmentProviderDistributionBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val statisticsViewmodel: StatisticsViewmodel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentProviderDistributionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val results = statisticsViewmodel.getResults()
        val names = arrayOf("Marks and Spencers", "Morrisons", "Lidl", "Sainsburys", "Waitrose")

        // Extracts the theme's window background colour, so that the chart's colour matches
        // the theme's
        val color = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, color, true)

        // Creates a pie chart which will display the total spent per provider
        val model = AAChartModel().chartType(AAChartType.Pie).title("Total spent per provider")
            .backgroundColor(color.data).dataLabelsEnabled(true)

        // Sorts the providers by the total spent
        val comparator = compareBy<Int> { results.providerDistribution[it] }
        val sortedMap = results.providerDistribution.toSortedMap(comparator)

        // Assigns each provider an array containing the label, and the total spent
        val values = sortedMap.map {
            arrayOf(names[it.key], it.value)
        }

        // Sets the charts data to the the list of arrays, which each represent a pie slice
        val series = AASeriesElement().name("Total").data(values.toTypedArray())
        model.series = arrayOf(series)

        binding.providerPieChart.setBackgroundColor(color.data)
        binding.providerPieChart.aa_drawChartWithChartModel(model)
    }

}