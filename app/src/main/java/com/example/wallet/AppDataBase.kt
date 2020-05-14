package com.example.wallet

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Transaction::class), version = 2)
abstract class AppDataBase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao

}