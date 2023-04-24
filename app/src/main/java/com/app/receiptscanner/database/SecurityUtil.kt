package com.app.receiptscanner.database

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtil {
    /**
     * Generates a random 256 byte long value for use as a salt
     *
     * @return the salt as a hexadecimal string
     */
    fun generateSalt(): String {
        val random = SecureRandom.getInstance("SHA1PRNG")
        val bytes = ByteArray(256)
        random.nextBytes(bytes)

        // Formats the bytes as a hexadecimal string
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a SHA-256 hash for use during authentication
     *
     * @param password the user's password
     * @param salt the salt generated when first storing the password
     * @return the hash
     */
    fun hash(password: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = "$password:$salt"
        return messageDigest
            .digest(saltedPassword.toByteArray())
            .joinToString("") { "%02x".format(it) }

    }

    /**
     * Checks whether a given flag is enabled in a bit field
     *
     * @param inputFlag the bitfield to test
     * @param testFlag the flag to test against
     * @return a boolean value signifying whether or not the flag was enabled
     */
    fun checkFlag(inputFlag: Int, testFlag: Int): Boolean {
        return inputFlag and testFlag == testFlag
    }

    // A set of flags specifying the specific issues with the user's input
    const val VALID = 0
    const val PASSWORD_TOO_SHORT = 1 shl 0
    const val PASSWORD_INCORRECT =  1 shl 1
    const val PASSWORDS_DO_NOT_MATCH = 1 shl 2
    const val USERNAME_TAKEN = 1 shl 3
    const val USERNAME_DOES_NOT_EXIST = 1 shl 4
    const val USERNAME_EMPTY = 1 shl 5
    const val ILLEGAL_CHARACTER = 1 shl 6
}