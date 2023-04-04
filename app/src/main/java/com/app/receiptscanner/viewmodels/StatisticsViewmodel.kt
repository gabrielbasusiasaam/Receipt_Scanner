package com.app.receiptscanner.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.database.ReceiptRepository
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.storage.StorageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

class StatisticsViewmodel(
    private val receiptRepository: ReceiptRepository,
    application: Application,
) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO
    private val receipts: ArrayList<Receipt> = arrayListOf()

    fun setReceipts(receiptList: List<Receipt>) {
        receipts.clear()
        receipts.addAll(receiptList)
    }

    fun calculateStatistics(endAction: (StatisticsResult) -> Unit) = viewModelScope.launch {
        val context: ReceiptApplication = getApplication()
        val storageHandler = StorageHandler(context)
        val normalizedReceipts = receipts.map {
            storageHandler.readReceipt(it, context.filesDir.path)
        }
        var sum = 0f
        var squareSum = 0f
        val providerTotal = hashMapOf<Int, Float>()
        // Stores costs for a receipt
        val costs = arrayListOf<Pair<NormalizedReceipt, Float>>()
        normalizedReceipts.forEach {
            val field = it.fields.get(it.fields.totalField) ?: return@forEach
            Log.e("DATA", field.data.toString())
            val totalCost = field.data[0].toFloat()
            sum += totalCost
            squareSum += totalCost * totalCost
            if (providerTotal.containsKey(field.type)) {
                providerTotal[field.type] = providerTotal[field.type]!! + totalCost
            } else {
                providerTotal[field.type] = totalCost
            }
            costs.add(Pair(it, totalCost))
        }
        costs.sortByDescending { it.second }
        val mean = sum / normalizedReceipts.size
        val variance = (squareSum / normalizedReceipts.size) - mean * mean
        val standardDeviation = sqrt(variance)

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

    private fun interpolate(data: List<Pair<NormalizedReceipt, Float>>, position: Double): Float {
        val lowerBound = data[floor(position).toInt()].second
        val upperBound = data[ceil(position).toInt()].second
        val fraction = position % 1
        return (lowerBound * (1.0 - fraction) + upperBound * fraction).toFloat()
    }

}