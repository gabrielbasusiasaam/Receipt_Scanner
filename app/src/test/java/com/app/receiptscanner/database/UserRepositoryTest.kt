package com.app.receiptscanner.database

import com.app.receiptscanner.database.SecurityUtil.generateSalt
import com.app.receiptscanner.database.SecurityUtil.hash
import org.junit.Assert.assertNotEquals
import org.junit.Test

internal class UserRepositoryTest {
    @Test
    fun testHash() {
        val input = "password"
        val salt = generateSalt()
        val unexpected = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertNotEquals(unexpected, hash(input, salt))
    }
}