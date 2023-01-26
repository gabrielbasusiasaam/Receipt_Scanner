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
    private val fields = hashMapOf<String, Any>()
    val userReceipts: LiveData<List<Receipt>> = _userReceipts

    fun loadUserReceipts() = viewModelScope.launch {
        val receipts = receiptRepository.getUserReceipts(userId)
        Log.e("ID", userId.toString())
        _userReceipts.postValue(receipts)
    }

    fun setUserId(id: Int) {
        userId = id
    }

    fun clearFields() = fields.clear()

    fun setField(title: String, value: Any) {
        fields[title] = value
    }

    fun getFields() = fields
}