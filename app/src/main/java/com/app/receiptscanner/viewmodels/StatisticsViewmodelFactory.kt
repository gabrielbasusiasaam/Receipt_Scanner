package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.receiptscanner.database.ReceiptRepository

class StatisticsViewmodelFactory(
    private val receiptRepository: ReceiptRepository,
    private val application: Application,
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StatisticsViewmodel::class.java)) {
            return StatisticsViewmodel(receiptRepository, application) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}