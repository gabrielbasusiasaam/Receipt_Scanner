package com.app.receiptscanner.database

import android.util.Log
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

    suspend fun getUserReceipts(userId: Int, receiptIds: List<Int>) = withContext(dispatcher) {
        return@withContext userReceiptDao.getReceiptsByUser(userId, receiptIds)
    }

    suspend fun getGroupReceipts(id: Int) = withContext(dispatcher) {
        return@withContext groupEntryDao.getGroupEntries(id)
    }

    suspend fun getUserReceiptsByDate(id: Int, startDate: Date, endDate: Date) = withContext(dispatcher) {
        val start = startDate.time
        val end = endDate.time

        assert(start < end) {
            "Start date must precede end date"
        }

        return@withContext userReceiptDao.getUserReceiptByDate(id, start, end)
    }

    suspend fun insertReceipt(receipt: Receipt) = withContext(dispatcher) {
        return@withContext receiptDao.insertReceipt(receipt)
    }

    suspend fun insertUserReceipt(receiptId: Int, userId: Int, isGlobal: Boolean) = withContext(dispatcher) {
        val userReceipt = UserReceipt(receiptId, userId, isGlobal)
        Log.e("UserReceipt", "userID - $userId, receiptID - $receiptId")
        userReceiptDao.insertUserReceipt(userReceipt)
    }

    suspend fun createGroup(name: String, receipts: List<Receipt> = listOf()) = withContext(dispatcher) {
        val group = ReceiptGroup(name = name, dateCreated = Calendar.getInstance().timeInMillis)
        val groupId = receiptGroupDao.insertReceiptGroup(group)
        val groupEntries = receipts.map { GroupEntry(groupId.toInt(), it.id) }
        groupEntryDao.insertGroupEntries(groupEntries)
    }

    suspend fun updateReceipt(finalRecord: Receipt) = withContext(dispatcher) {
        receiptDao.updateReceipt(finalRecord)
    }

}