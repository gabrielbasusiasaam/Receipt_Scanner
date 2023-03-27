package com.app.receiptscanner.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.receiptscanner.database.Receipt
import com.app.receiptscanner.database.ReceiptRepository
import com.app.receiptscanner.database.UserRepository
import com.app.receiptscanner.parser.NormalizedReceiptCallback
import com.app.receiptscanner.parser.ReceiptInsertionCallback
import com.app.receiptscanner.storage.NormalizedReceipt
import com.app.receiptscanner.storage.StorageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiptViewmodel(
    private val userRepository: UserRepository,
    private val receiptRepository: ReceiptRepository,
    application: Application
) : AndroidViewModel(application) {
    private val dispatcher = Dispatchers.IO
    private val _userReceipts: MutableLiveData<List<Receipt>> by lazy(::MutableLiveData)
    private var userId: Int = -1
    private var normalizedReceipt: NormalizedReceipt? = null
    private var receipt: Receipt? = null
    private val fields = hashMapOf<String, Any>()
    private val receiptCache = hashMapOf<Int, NormalizedReceipt>()

    val userReceipts: LiveData<List<Receipt>> = _userReceipts

    fun loadUserReceipts() = viewModelScope.launch {
        val receipts = receiptRepository.getUserReceipts(userId)
        _userReceipts.postValue(receipts)
    }

    fun loadReceiptData(endAction: NormalizedReceiptCallback) = viewModelScope.launch {
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
        dateCreated: Long,
        parserId: String,
        photoPath: String,
        endAction: ReceiptInsertionCallback
    ) = viewModelScope.launch {
        val record = Receipt(dateCreated = dateCreated, parserId = parserId, photoPath = photoPath)
        val id = receiptRepository.insertReceipt(record).toInt()
        receiptRepository.insertUserReceipt(id, userId, false)
        val finalReceipt = Receipt(id, parserId, dateCreated, photoPath)
        endAction.invoke(finalReceipt)
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

}