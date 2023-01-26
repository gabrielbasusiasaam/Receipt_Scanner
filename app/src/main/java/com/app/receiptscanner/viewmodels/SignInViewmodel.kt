package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.receiptscanner.database.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignInViewmodel(
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO

    suspend fun verify(username: String, password: String) = withContext(dispatcher) {
        return@withContext userRepository.verifyUser(username, password)
    }

    suspend fun checkUsernameExists(username: String) = withContext(dispatcher) {
        return@withContext userRepository.checkUsernameTaken(username)
    }

    suspend fun getUser(username: String) = withContext(dispatcher) {
        return@withContext userRepository.getUserByUsername(username)!!
    }
}