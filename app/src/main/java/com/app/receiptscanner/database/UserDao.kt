package com.app.receiptscanner.database

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM User WHERE username = :username")
    fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM User WHERE id = :id")
    fun getUserById(id: Int): User

    @Insert
    fun insertUser(user: User): Long

    @Update
    fun updateUser(user: User)

    @Delete
    fun deleteUser(user: User)
}