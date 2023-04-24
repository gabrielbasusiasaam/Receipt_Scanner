package com.app.receiptscanner

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.receiptscanner.database.*
import com.app.receiptscanner.viewmodels.ReceiptViewmodel
import com.app.receiptscanner.viewmodels.StatisticsViewmodel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class StatisticsTest {
    private lateinit var database: ReceiptDatabase
    private lateinit var receiptViewmodel: ReceiptViewmodel
    private lateinit var statisticsViewmodel: StatisticsViewmodel
    private lateinit var userRepository: UserRepository
    private lateinit var receiptRepository: ReceiptRepository

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, ReceiptDatabase::class.java).build()

        userRepository = UserRepository(database.userDao())
        receiptRepository = ReceiptRepository(
            database.receiptDao(),
            database.receiptGroupDao(),
            database.groupEntryDao(),
            database.userReceiptDao()
        )

        statisticsViewmodel = StatisticsViewmodel(context)
        receiptViewmodel = ReceiptViewmodel(userRepository, receiptRepository, context)
    }

    @After
    @Throws(IOException::class)
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun testReceiptFetching() {
        val start = Date(2022, 1, 1)
        val end = Date(2022, 10, 14)

        val testUser = User(
            username = "Test",
            passwordHash = "",
            salt = "",
            allowsBiometrics = false
        )

        val userId = database.userDao().insertUser(testUser)

        val testReceipt = Receipt(
            dateCreated = Date(2022, 5, 15).time,
            parserId = "",
            photoPath = null
        )
        val receiptId = database.receiptDao().insertReceipt(testReceipt)
        val userReceipt = UserReceipt(receiptId.toInt(), userId.toInt(), false)

        database.userReceiptDao().insertUserReceipt(userReceipt)

        receiptViewmodel.setUserId(userId.toInt())
        receiptViewmodel.getReceiptsByDate(start, end) {
            assert(it.isNotEmpty())
        }
    }

    @Test
    fun testEmptyPeriodFetching() {
        val start = Date(1970, 1, 1)
        val end = Date(1970, 2, 2)


        receiptViewmodel.getReceiptsByDate(start, end) {
            assert(it.isEmpty())
        }
    }

    @Test
    fun testSingleDay() {
        val day = Date(2022, 10, 10)
        val testUser = User(
            username = "Test",
            passwordHash = "",
            salt = "",
            allowsBiometrics = false
        )

        val userId = database.userDao().insertUser(testUser)

        val validTestReceipt = Receipt(
            dateCreated = Date(2022, 10, 10).time,
            parserId = "",
            photoPath = null
        )

        val invalidTestReceipt = Receipt(
            dateCreated = Date(2012, 6, 3).time,
            parserId = "",
            photoPath = null
        )

        // Creates and inserts two receipts, one which is on the given day and should be returned
        // and one that is on another day and shouldn't be returned
        val validReceiptId = database.receiptDao().insertReceipt(validTestReceipt)
        val invalidReceiptId = database.receiptDao().insertReceipt(invalidTestReceipt)
        val validUserReceipt = UserReceipt(validReceiptId.toInt(), userId.toInt(), false)
        val invalidUserReceipt = UserReceipt(invalidReceiptId.toInt(), userId.toInt(), false)

        database.userReceiptDao().insertUserReceipt(validUserReceipt)
        database.userReceiptDao().insertUserReceipt(invalidUserReceipt)

        receiptViewmodel.setUserId(userId.toInt())
        receiptViewmodel.getReceiptsByDate(day, day) {
            // Checks that the receipt from the correct day is returned
            assert(it.any { receipt ->
                receipt.id == validReceiptId.toInt()
            })

            // Checks that the receipt from another day isn't returned
            assert(it.none { receipt ->
                receipt.id == invalidReceiptId.toInt()
            })
        }
    }

    @Test
    fun testInvalidPeriod() {
        val invalidStart = Date(2022, 10, 10)
        val invalidEnd = Date(2019, 5, 10)

        receiptViewmodel.getReceiptsByDate(invalidStart, invalidEnd) {}
    }
}