package com.app.receiptscanner.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.database.ReceiptRepository
import com.app.receiptscanner.database.UserRepository
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.storage.StorageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ReceiptViewmodel(
    private val userRepository: UserRepository,
    private val receiptRepository: ReceiptRepository,
    application: Application
) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO
    private val _userReceipts: MutableLiveData<List<Receipt>> by lazy(::MutableLiveData)
    private var userId: Int = -1
    private var normalizedReceipt: NormalizedReceipt? = null
    private val receiptCache = hashMapOf<Int, NormalizedReceipt>()

    val userReceipts: LiveData<List<Receipt>> = _userReceipts

    fun loadUserReceipts() = viewModelScope.launch {
        val receipts = receiptRepository.getUserReceipts(userId)
        _userReceipts.postValue(receipts)
    }

    fun loadReceiptData(endAction: (receipts: List<NormalizedReceipt>) -> Unit) =
        viewModelScope.launch {
            val context: ReceiptApplication = getApplication()
            val storageHandler = StorageHandler(context)
            val normalizedReceipts = userReceipts.value!!.map {
                if (receiptCache.containsKey(it.id)) return@map receiptCache[it.id]!!
                val normalized = storageHandler.readReceipt(it, context.filesDir.path)
                receiptCache[it.id] = normalized
                normalized
            }
            endAction.invoke(normalizedReceipts)
        }

    fun createReceipt(
        receipt: NormalizedReceipt,
        storageDirectory: String,
        parserId: String,
        endAction: () -> Unit
    ) = viewModelScope.launch {
        val record = Receipt(
            dateCreated = receipt.dateCreated,
            parserId = parserId,
            photoPath = receipt.photoPath
        )
        val id = receiptRepository.insertReceipt(record).toInt()
        receiptRepository.insertUserReceipt(id, userId, false)
        val finalRecord = Receipt(
            id = id,
            parserId = parserId,
            dateCreated = receipt.dateCreated,
            photoPath = receipt.photoPath
        )
        val storageHandler = StorageHandler(getApplication())
        storageHandler.storeReceipt(finalRecord, receipt, storageDirectory)
        endAction.invoke()
    }

    fun setUserId(id: Int) {
        userId = id
    }

    fun setNormalizedReceipt(normalized: NormalizedReceipt) {
        normalizedReceipt = normalized
    }

    fun getReceipt(): NormalizedReceipt {
        return normalizedReceipt!!
    }

    fun clearReceipt() {
        normalizedReceipt = null
    }

    fun setField(key: ArrayList<String>, value: ArrayList<String>) {
        normalizedReceipt?.fields?.set(key, value)
    }

    fun getReceiptsByDate(
        start: Date,
        end: Date,
        endAction: (List<Receipt>) -> Unit
    ) = viewModelScope.launch {
        if (start > end) {
            Log.e("Date", "Invalid date")
            return@launch
        }
        val receipts = receiptRepository.getUserReceiptsByDate(userId, start, end)
        endAction.invoke(receipts)
    }

}