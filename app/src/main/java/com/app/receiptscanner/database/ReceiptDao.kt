package com.app.receiptscanner.database

import androidx.room.*

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM Receipt WHERE id = :id")
    fun getReceiptById(id: Int): Receipt

    @Query("SELECT * FROM Receipt WHERE parserId = :id")
    fun getReceiptByParser(id: Int): List<Receipt>

    @Insert
    fun insertReceipt(receipt: Receipt): Long

    @Delete
    fun deleteReceipt(receipt: Receipt)

    @Update
    fun updateReceipt(receipt: Receipt)
}