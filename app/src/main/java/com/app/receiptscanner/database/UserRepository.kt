package com.app.receiptscanner.database

import com.app.receiptscanner.database.SecurityUtil.PASSWORD_INCORRECT
import com.app.receiptscanner.database.SecurityUtil.USERNAME_DOES_NOT_EXIST
import com.app.receiptscanner.database.SecurityUtil.USERNAME_TAKEN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    private val dispatcher = Dispatchers.IO

    /**
     * Authenticates a given username and password pair against the credentials of users
     * currently present in the 'User' table
     *
     * @param username the username of the user to be checked
     * @param password the password inputted
     * @return a UserResult object containing whether the authentication was a success,
     * as well as the user if it was a success, or the reason for failure otherwise.
     */
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

    /**
     * Updates a the record for a given user in the 'User' Table
     *
     * @param user - the updated record for the user
     */
    suspend fun updateUser(user: User) = withContext(dispatcher) { userDao.updateUser(user) }

    /**
     * Deletes the record for a given user from the 'User' Table
     *
     * @param user - the user to be deleted
     */
    suspend fun deleteUser(user: User) = withContext(dispatcher) { userDao.deleteUser(user) }

    /**
     * Fetches the user's record from the user table using their userId
     *
     * @param id the unique userId for the user
     * @return a record containing the user's details
     */
    suspend fun getUserById(id: Int) = withContext(dispatcher) {
        return@withContext userDao.getUserById(id)
    }

    /**
     * Fetches the user's record from the user table using their username
     * As a result, usernames within the table must be unique
     *
     * @param username - The user's username
     * @return a record containing the user's details
     */
    suspend fun getUserByUsername(username: String) = withContext(dispatcher) {
        return@withContext userDao.getUserByUsername(username)
    }

    /**
     * Creates a record for a user in the 'User' table with the inputted credentials,
     * whilst ensuring that the inputted username is unique
     *
     * @param username the Username for the new user. Must be unique
     * @param password the new user's password
     * @param allowsBiometrics specifies whether or not biometric authentication
     * is allowed for this user
     *
     * @return a UserResult object containing whether the creation was a success,
     * as well as the user if it was a success, or the reason for failure otherwise.
     */
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


    /**
     * Checks whether a given username has been used by another record in the 'User' table
     *
     * @param username the username to be tested
     * @return a boolean signifying whether or not the username is taken
     */
    fun checkUsernameTaken(username: String): Boolean {
        val user = userDao.getUserByUsername(username)
        return user != null
    }


}