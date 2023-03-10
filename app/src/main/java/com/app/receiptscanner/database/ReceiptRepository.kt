package com.app.receiptscanner.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ReceiptRepository(
    private val receiptDao: ReceiptDao,
    private val receiptGroupDao: ReceiptGroupDao,
    private val groupEntryDao: GroupEntryDao,
    private val userReceiptDao: UserReceiptDao,
) {
    private val dispatcher = Dispatchers.IO

    suspend fun getUserReceipts(id: Int) = withContext(dispatcher) {
        return@withContext userReceiptDao.getReceiptsByUser(id)
    }

    suspend fun getGroupReceipts(id: Int) = withContext(dispatcher) {
        return@withContext groupEntryDao.getGroupEntries(id)
    }

    suspend fun createGroup(name: String, receipts: List<Receipt> = listOf()) = withContext(dispatcher) {
        val group = ReceiptGroup(name = name, dateCreated = Calendar.getInstance().timeInMillis)
        val groupId = receiptGroupDao.insertReceiptGroup(group)
        val groupEntries = receipts.map { GroupEntry(groupId.toInt(), it.id) }
        groupEntryDao.insertGroupEntries(groupEntries)
    }

}