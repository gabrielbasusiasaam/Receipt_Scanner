package com.app.receiptscanner.viewmodels

import com.app.receiptscanner.storage.NormalizedReceipt
import java.math.BigDecimal

data class StatisticsResult(
    // Data
    val data: List<Pair<NormalizedReceipt, BigDecimal>>,
    val providerDistribution: HashMap<Int, BigDecimal>,
    val count: Int,
    val sum: BigDecimal,
    val max: BigDecimal,
    val min: BigDecimal,

    // Averages
    val mean: BigDecimal,
    val mode: BigDecimal,
    val median: BigDecimal,

    // Measures of spread
    val lowerQuartile: BigDecimal,
    val upperQuartile: BigDecimal,
    val standardDeviation: BigDecimal,
    val variance: BigDecimal
)
