package com.app.receiptscanner.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GroupEntryDao {
    @Query("SELECT * FROM Receipt INNER JOIN GroupEntry ON Receipt.id = GroupEntry.receiptId WHERE receiptId = :receiptId")
    fun getReceiptEntries(receiptId: Int): List<Receipt>

    @Query("SELECT * FROM ReceiptGroup INNER JOIN GroupEntry ON ReceiptGroup.id = GroupEntry.groupId WHERE groupId = :groupId")
    fun getGroupEntries(groupId: Int): List<ReceiptGroup>

    @Insert
    fun insertGroupEntry(groupEntry: GroupEntry): Long

    @Insert
    fun insertGroupEntries(groupEntries: List<GroupEntry>)
}