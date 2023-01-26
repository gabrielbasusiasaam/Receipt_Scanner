package com.app.receiptscanner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReceiptGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateCreated: Long
)
