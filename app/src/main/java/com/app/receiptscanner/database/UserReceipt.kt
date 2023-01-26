package com.app.receiptscanner.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["receiptId", "userId"], foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserReceipt(
    val receiptId: Int,
    val userId: Int,
    val isGlobal: Boolean
)
