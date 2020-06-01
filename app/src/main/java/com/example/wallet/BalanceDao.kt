package com.example.wallet

import androidx.room.*

@Dao
interface BalanceDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(balance: Balance)

    @Delete
    fun delete(balance: Balance)

    @Update
    fun update(balance: Balance)

    @Query("SELECT * FROM `balance` WHERE id =:id")
    fun loadBalance(id: Int): Balance?

}