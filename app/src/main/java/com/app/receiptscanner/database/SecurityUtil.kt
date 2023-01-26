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

    const val VALID = 0b00000000
    const val PASSWORD_TOO_SHORT = 0b00000001
    const val PASSWORD_INCORRECT =  0b00000010
    const val PASSWORDS_DO_NOT_MATCH = 0b00000100
    const val USERNAME_TAKEN = 0b00001000
    const val USERNAME_DOES_NOT_EXIST = 0b00010000
    const val USERNAME_EMPTY = 0b00100000
    const val ILLEGAL_CHARACTER = 0b01000000

}