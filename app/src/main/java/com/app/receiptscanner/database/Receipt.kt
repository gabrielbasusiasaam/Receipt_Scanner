package com.app.receiptscanner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Receipt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoPath: String?,
    val dateCreated: Long,
    val parserId: String,
)
