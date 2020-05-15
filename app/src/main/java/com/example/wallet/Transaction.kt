package com.example.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Transaction(
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "date") val date: String = "4994",
    @ColumnInfo(name = "isIncoming") var isIncomingTransaction: Boolean,
    @ColumnInfo(name = "timeStamp") var timeStamp: Long = Date().time / 1000,
    @ColumnInfo(name = "hash") var hash: String,
    @PrimaryKey(autoGenerate = true) var id: Int = 0)  {

    override fun equals(transaction: Any?) = transaction is Transaction && this.hash == transaction.hash

}