package com.app.receiptscanner.layouts

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.app.receiptscanner.databinding.FragmentCostTimeBinding
import com.app.receiptscanner.viewmodels.ReceiptApplication
import com.app.receiptscanner.viewmodels.StatisticsViewmodel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SourceLockedOrientationActivity")
class CostTimeGraphFragment : Fragment() {
    private var _binding: FragmentCostTimeBinding? = null
    private val binding get() = _binding!!
    private val activity by lazy { requireActivity() as MainActivity }
    private val application by lazy { activity.application as ReceiptApplication }
    private val statisticsViewmodel: StatisticsViewmodel by activityViewModels()

    // Orientates the screen to landscape when the fragment is created, as it is extremely difficult
    // to view when in portrait
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    // Reorientates the screen to portrait when the user leaves the screen. Portrait is used here
    // instead of Unspecified, as the layouts for the rest of the application haven't been designed
    // with landscape in mind
    override fun onStop() {
        super.onStop()
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCostTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fetches the results calculated in the viewmodel. These are guaranteed to exist by the
        // time this screen opens, so there is no need to check if is null
        val results = statisticsViewmodel.getResults()

        // Extracts the theme's window background colour, so that the chart's colour matches
        // the theme's
        val colour = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, colour, true)

        // Creates a line chart which will display the total spent per day
        val model = AAChartModel().chartType(AAChartType.Line).title("Cost over time")
            .backgroundColor(colour.data).dataLabelsEnabled(true)

        val calendar = Calendar.getInstance()
        val sortedData = results.data.sortedBy { it.first.dateCreated }
        val dateMap = hashMapOf<Date, BigDecimal>()
        var lastDay = Date(0)

        sortedData.forEach {
            // Gets the exact time the receipt was created, and then gets 00:00:00 am
            // of the same day using that time
            calendar.time = Date(it.first.dateCreated)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val date = calendar.time

            // Checks if this is the chronologically last day found,
            // setting lastDays value to it if it is
            lastDay = if(lastDay.time < date.time) date else lastDay

            // Checks if a value day has already been stored, adding to the current value if it has
            // and setting the day's value to the price otherwise
            if(dateMap.containsKey(date)) {
                dateMap[date] = dateMap[date]!! + it.second
            } else {
                dateMap[date] = it.second
            }
        }

        val startDate = Date(sortedData.first().first.dateCreated)
        calendar.time = startDate

        // Iterates between the start date and the last date, adding in zeroes for the days that
        // have not already been added
        while (calendar.time.time < lastDay.time) {
            // Based on this stackoverflow answer https://stackoverflow.com/a/428966, this should
            // automatically account for months and years rolling over, however this is difficult to
            // test, so it is not certain
            calendar.add(Calendar.DATE, 1)

            // Sets the hour to 00:00:00 am every time, instead of once at the start,
            // as sometimes the time will be incorrect when incrementing the date
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val date = calendar.time
            if(!dateMap.containsKey(date)) dateMap[date] = BigDecimal(0)

        }
        // Sorts the data based on it's date, so that it shows up in the correct order on the chart
        val comparator = compareBy<Date> { it.time }
        val sortedMap = dateMap.toSortedMap(comparator)
        val finalData = sortedMap.values

        // Formats the date from epoch time to a readable format
        val formatter = SimpleDateFormat("dd/MM/yy", Locale.UK)
        val labels = sortedMap.keys.map {
            formatter.format(it)
        }

        // Creates the Y axis, and stores the price values for the data
        val series = AASeriesElement().name("Total Spent").data(finalData.toTypedArray())
        model.series = arrayOf(series)

        // Creates the X axis, and stores the formatted dates
        model.categories = labels.toTypedArray()

        binding.costTimeGraph.setBackgroundColor(colour.data)
        binding.costTimeGraph.aa_drawChartWithChartModel(model)

    }
}