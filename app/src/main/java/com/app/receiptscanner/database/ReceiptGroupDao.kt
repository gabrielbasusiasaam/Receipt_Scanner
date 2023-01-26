package com.app.receiptscanner.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ReceiptGroupDao {
    @Query("SELECT * FROM ReceiptGroup")
    fun getAllGroups(): List<ReceiptGroup>

    @Insert
    fun insertReceiptGroup(receiptGroup: ReceiptGroup): Long

    @Delete
    fun deleteReceiptGroup(receiptGroup: ReceiptGroup)
}