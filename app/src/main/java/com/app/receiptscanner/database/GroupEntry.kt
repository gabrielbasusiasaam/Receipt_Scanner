package com.app.receiptscanner.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["groupId", "receiptId"],
    foreignKeys = [
        ForeignKey(
            entity = ReceiptGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GroupEntry(
    val groupId: Int,
    val receiptId: Int
)
