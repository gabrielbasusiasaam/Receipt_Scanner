package com.app.receiptscanner.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface UserReceiptDao {
    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE UserReceipt.userId = :id")
    fun getReceiptsByUser(id: Int): List<Receipt>

    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE isGlobal = 1")
    fun getGlobalReceipts(): List<Receipt>
}