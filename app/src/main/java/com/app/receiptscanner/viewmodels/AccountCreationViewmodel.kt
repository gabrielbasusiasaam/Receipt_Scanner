package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.receiptscanner.database.SecurityUtil.ILLEGAL_CHARACTER
import com.app.receiptscanner.database.SecurityUtil.PASSWORDS_DO_NOT_MATCH
import com.app.receiptscanner.database.SecurityUtil.PASSWORD_TOO_SHORT
import com.app.receiptscanner.database.SecurityUtil.USERNAME_EMPTY
import com.app.receiptscanner.database.SecurityUtil.USERNAME_TAKEN
import com.app.receiptscanner.database.SecurityUtil.VALID
import com.app.receiptscanner.database.UserRepository
import com.app.receiptscanner.database.UserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountCreationViewmodel(
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO

    suspend fun createUser(username: String, password: String, allowBiometrics: Boolean) =
        withContext(dispatcher) {
            return@withContext userRepository.createUser(username, password, allowBiometrics).data!!
        }

    suspend fun validate(username: String, password: String, passwordRetry: String): UserResult =
        withContext(dispatcher) {
            var reason = 0
            if (userRepository.checkUsernameTaken(username)) reason = reason xor USERNAME_TAKEN
            if (username.isEmpty()) reason = reason xor USERNAME_EMPTY
            if (username.any { !it.isLetterOrDigit() }) reason = reason xor ILLEGAL_CHARACTER
            if (password.length < 8) reason = reason xor PASSWORD_TOO_SHORT
            if (password != passwordRetry) reason = reason xor PASSWORDS_DO_NOT_MATCH
            val result = reason == VALID
            return@withContext UserResult(result, null, reason)
        }
}