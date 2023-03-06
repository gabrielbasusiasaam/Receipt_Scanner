package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.receiptscanner.database.UserRepository

class AccountCreationViewmodelFactory(
    private val userRepository: UserRepository,
    private val application: Application,
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AccountCreationViewmodel::class.java)) {
            return AccountCreationViewmodel(userRepository, application) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}