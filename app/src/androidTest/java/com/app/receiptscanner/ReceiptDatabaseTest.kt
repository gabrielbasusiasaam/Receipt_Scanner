package com.app.receiptscanner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.receiptscanner.database.ReceiptDatabase
import com.app.receiptscanner.database.User
import com.app.receiptscanner.database.UserDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ReceiptDatabaseTest {
    private lateinit var database: ReceiptDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ReceiptDatabase::class.java).build()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun testSQLInjection() {
        // Context of the app under test.
        val user = User(
            id = 0,
            username = "user; DROP TABLE User;",
            passwordHash = "",
            salt = "",
            allowsBiometrics = true
        )
        val id = userDao.insertUser(user)
        val userRecord = userDao.getUserById(id.toInt())
        assertEquals(user.username, userRecord.username)
    }
}
