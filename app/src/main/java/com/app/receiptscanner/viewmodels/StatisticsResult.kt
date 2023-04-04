package com.app.receiptscanner.viewmodels

import com.app.receiptscanner.storage.NormalizedReceipt

data class StatisticsResult(
    // Data
    val data: List<Pair<NormalizedReceipt, Float>>,
    val providerDistribution: HashMap<Int, Float>,
    val count: Int,
    val sum: Float,
    val max: Float,
    val min: Float,

    // Averages
    val mean: Float,
    val mode: Float,
    val median: Float,

    // Measures of spread
    val lowerQuartile: Float,
    val upperQuartile: Float,
    val standardDeviation: Float,
    val variance: Float
)
