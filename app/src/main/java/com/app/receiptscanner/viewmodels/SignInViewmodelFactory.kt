package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.receiptscanner.database.UserRepository

class SignInViewmodelFactory(
    private val userRepository: UserRepository,
    private val application: Application,
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SignInViewmodel::class.java)) {
            return SignInViewmodel(userRepository, application) as T
        }
        throw IllegalArgumentException("Unknown Viewmodel class")
    }
}