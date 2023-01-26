package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.receiptscanner.database.ReceiptRepository
import com.app.receiptscanner.database.UserRepository

class ReceiptViewmodelFactory(
    private val userRepository: UserRepository,
    private val receiptRepository: ReceiptRepository,
    private val application: Application,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ReceiptViewmodel::class.java)) {
            return ReceiptViewmodel(userRepository, receiptRepository, application) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}