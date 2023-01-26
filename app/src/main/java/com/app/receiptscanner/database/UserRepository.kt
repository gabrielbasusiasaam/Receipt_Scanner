package com.app.receiptscanner.database

import com.app.receiptscanner.database.SecurityUtil.PASSWORD_INCORRECT
import com.app.receiptscanner.database.SecurityUtil.USERNAME_DOES_NOT_EXIST
import com.app.receiptscanner.database.SecurityUtil.USERNAME_TAKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    private val dispatcher = Dispatchers.IO

    suspend fun verifyUser(username: String, password: String) = withContext(dispatcher) {
        val user = userDao.getUserByUsername(username) ?: return@withContext UserResult(
            false,
            null,
            USERNAME_DOES_NOT_EXIST
        )
        val hash = SecurityUtil.hash(password, user.salt)
        return@withContext when (hash == user.passwordHash) {
            true -> UserResult(true, user)
            false -> UserResult(false, null, PASSWORD_INCORRECT)
        }
    }

    suspend fun updateUser(user: User) = withContext(dispatcher) { userDao.updateUser(user) }

    suspend fun deleteUser(user: User) = withContext(dispatcher) { userDao.deleteUser(user) }

    suspend fun getUserById(id: Int) = withContext(dispatcher) {
        return@withContext userDao.getUserById(id)
    }

    suspend fun getUserByUsername(username: String) = withContext(dispatcher) {
        return@withContext userDao.getUserByUsername(username)
    }

    suspend fun createUser(username: String, password: String, allowsBiometrics: Boolean) =
        withContext(dispatcher) {
            val isTaken = checkUsernameTaken(username)
            if (isTaken)
                return@withContext UserResult(false, null, USERNAME_TAKEN)

            val salt = SecurityUtil.generateSalt()
            val saltedPassword = SecurityUtil.hash(password, salt)
            val user = User(
                username = username,
                passwordHash = saltedPassword,
                salt = salt,
                allowsBiometrics = allowsBiometrics
            )
            userDao.insertUser(user)
            return@withContext UserResult(true, user)
        }

    fun checkUsernameTaken(username: String): Boolean {
        val user = userDao.getUserByUsername(username)
        return user != null
    }


}