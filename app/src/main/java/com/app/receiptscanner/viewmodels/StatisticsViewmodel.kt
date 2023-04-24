package com.app.receiptscanner.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.storage.StorageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

class StatisticsViewmodel(application: Application) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO
    private val receipts: ArrayList<Receipt> = arrayListOf()
    private var results: StatisticsResult? = null

    fun setReceipts(receiptList: List<Receipt>) {
        receipts.clear()
        receipts.addAll(receiptList)
    }

    fun setResults(result: StatisticsResult) {
        results = result
    }

    fun getResults(): StatisticsResult {
        return results!!
    }

    fun calculateStatistics(endAction: (StatisticsResult) -> Unit) = viewModelScope.launch {
        val context: ReceiptApplication = getApplication()
        val storageHandler = StorageHandler(context)
        val normalizedReceipts = receipts.map {
            storageHandler.readReceipt(it, context.filesDir.path)
        }
        var sum = BigDecimal(0)
        var squareSum = BigDecimal(0)
        val providerTotal = hashMapOf<Int, BigDecimal>()
        // Stores costs for a receipt
        val costs = arrayListOf<Pair<NormalizedReceipt, BigDecimal>>()
        normalizedReceipts.forEach {
            val field = it.fields[it.fields.costField] ?: return@forEach
            Log.e("DATA", field.data.toString())
            val totalCost = field.data[0].toBigDecimal()
            sum += totalCost
            squareSum += totalCost * totalCost
            if (providerTotal.containsKey(it.type)) {
                providerTotal[it.type] = providerTotal[it.type]!! + totalCost
            } else {
                providerTotal[it.type] = totalCost
            }
            costs.add(Pair(it, totalCost))
        }
        costs.sortByDescending { it.second }
        val mean = sum / BigDecimal(normalizedReceipts.size)
        val variance = (squareSum / BigDecimal(normalizedReceipts.size)) - mean * mean
        val standardDeviation = sqrt(variance.toFloat()).toBigDecimal()

        // https://stackoverflow.com/questions/65144865/use-fold-to-find-the-mode-of-any-list-in-kotlin
        val (mode, _) = costs.groupingBy { it }.eachCount().maxByOrNull { it.value }!!
        val maxCost = costs.maxBy { it.second }
        val minCost = costs.minBy { it.second }

        val lowerQuartile = interpolate(costs, normalizedReceipts.size.toDouble() / 4)
        val median = interpolate(costs, normalizedReceipts.size.toDouble() / 2)
        val upperQuartile = interpolate(costs, 3 * normalizedReceipts.size.toDouble() / 4)

        val result = StatisticsResult(
            // Data
            costs, providerTotal, normalizedReceipts.size, sum, maxCost.second, minCost.second,
            // Averages
            mean, mode.second, median,
            // Measures of spread
            lowerQuartile, upperQuartile, standardDeviation, variance
        )
        endAction.invoke(result)
    }

    private fun interpolate(
        data: List<Pair<NormalizedReceipt, BigDecimal>>,
        position: Double
    ): BigDecimal {
        val lowerBound = data[floor(position).toInt()].second
        val upperBound = data[ceil(position).toInt().coerceAtMost(data.lastIndex)].second
        val fraction = position % 1
        return (lowerBound * BigDecimal(1.0 - fraction) + upperBound * BigDecimal(fraction))
    }

}