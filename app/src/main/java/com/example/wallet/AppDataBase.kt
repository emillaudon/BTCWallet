package com.example.wallet

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(version = 25, entities = [Transaction::class, Balance::class] )
abstract class AppDataBase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao
    abstract fun balanceDao() : BalanceDao

}
