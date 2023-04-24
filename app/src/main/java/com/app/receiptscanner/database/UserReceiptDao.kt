package com.app.receiptscanner.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserReceiptDao {
    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE UserReceipt.userId = :id")
    fun getReceiptsByUser(id: Int): List<Receipt>

    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE UserReceipt.userId = :userId and Receipt.id in (:receiptIds)")
    fun getReceiptsByUser(userId: Int, receiptIds: List<Int>): List<Receipt>

    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE isGlobal = 1")
    fun getGlobalReceipts(): List<Receipt>

    @Query("SELECT * FROM Receipt INNER JOIN UserReceipt ON Receipt.id = UserReceipt.receiptId WHERE UserReceipt.userId = :id AND Receipt.dateCreated BETWEEN :startDate AND :endDate")
    fun getUserReceiptByDate(id: Int, startDate: Long, endDate: Long): List<Receipt>

    @Insert
    fun insertUserReceipt(userReceipt: UserReceipt)
}