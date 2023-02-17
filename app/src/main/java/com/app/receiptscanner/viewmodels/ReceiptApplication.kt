package com.app.receiptscanner.viewmodels

import android.app.Application
import com.app.receiptscanner.database.ReceiptDatabase
import com.app.receiptscanner.database.ReceiptRepository
import com.app.receiptscanner.database.UserRepository

class ReceiptApplication : Application() {
    val database by lazy { ReceiptDatabase.getDatabase(this) }

    val userRepository by lazy {
        UserRepository(database.userDao())
    }

    val receiptRepository by lazy {
        ReceiptRepository(
            database.receiptDao(),
            database.receiptGroupDao(),
            database.groupEntryDao(),
            database.userReceiptDao()
        )
    }
}