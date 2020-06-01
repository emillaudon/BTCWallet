package com.example.wallet

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Transaction::class), version = 14)
abstract class AppDataBase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao

}