package com.app.receiptscanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Receipt::class, ReceiptGroup::class, UserReceipt::class, GroupEntry::class],
    version = 1
)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun receiptGroupDao(): ReceiptGroupDao
    abstract fun userReceiptDao(): UserReceiptDao
    abstract fun groupEntryDao(): GroupEntryDao

    companion object {
        @Volatile
        private var INSTANCE: ReceiptDatabase? = null

        /**
         * Returns a singleton instance of the receipt database
         */
        fun getDatabase(context: Context) = INSTANCE ?: synchronized(this) {
            val instance =
                Room.databaseBuilder(context, ReceiptDatabase::class.java, "receipt_database")
                    .build()
            INSTANCE = instance
            instance
        }
    }

}