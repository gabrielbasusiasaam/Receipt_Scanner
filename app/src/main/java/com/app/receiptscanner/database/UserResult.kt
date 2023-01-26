package com.app.receiptscanner.database

data class UserResult(
    val isSuccess: Boolean,
    val data: User? = null,
    val reason: Int = 0
)
