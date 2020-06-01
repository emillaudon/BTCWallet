package com.example.wallet

import androidx.room.*
import java.util.*


@Entity
class Transaction(
    @ColumnInfo(name = "value") val value: Double,
    @ColumnInfo(name = "date") var date: String = "4994",
    @ColumnInfo(name = "isIncoming") var isIncomingTransaction: Boolean,
    @ColumnInfo(name = "timeStamp") var timeStamp: Long = Date().time / 1000,
    @ColumnInfo(name = "hash") var hash: String,
    @ColumnInfo(name = "isConfirmed") var isConfirmed: Boolean = true,
    @PrimaryKey(autoGenerate = true) var id: Int = 0)  {

    override fun equals(transaction: Any?) = transaction is Transaction && this.hash == transaction.hash

}