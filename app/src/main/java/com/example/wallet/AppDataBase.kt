package com.example.wallet

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 27, entities = [Transaction::class, Balance::class, KeyHolder::class] )
abstract class AppDataBase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao
    abstract fun balanceDao() : BalanceDao
    abstract fun keyHolderDao() : KeyHolderDao

}
