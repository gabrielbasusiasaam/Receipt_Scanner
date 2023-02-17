package com.app.receiptscanner.database

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtil {
    fun generateSalt(): String {
        val random = SecureRandom.getInstance("SHA1PRNG")
        val bytes = ByteArray(256)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, salt: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = "$password:$salt"
        return messageDigest
            .digest(saltedPassword.toByteArray())
            .joinToString("") { "%02x".format(it) }

    }

    fun checkFlag(inputFlag: Int, testFlag: Int): Boolean {
        return inputFlag and testFlag == testFlag
    }

    const val VALID = 0
    const val PASSWORD_TOO_SHORT = 1 shl 0
    const val PASSWORD_INCORRECT =  1 shl 1
    const val PASSWORDS_DO_NOT_MATCH = 1 shl 2
    const val USERNAME_TAKEN = 1 shl 3
    const val USERNAME_DOES_NOT_EXIST = 1 shl 4
    const val USERNAME_EMPTY = 1 shl 5
    const val ILLEGAL_CHARACTER = 1 shl 6

}