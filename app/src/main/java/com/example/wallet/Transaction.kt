package com.example.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Transaction(
    @ColumnInfo(name = "value") val value: Float,
    @ColumnInfo(name = "date") val date: String = "4994",
    @ColumnInfo(name = "isIncoming") var isIncomingTransaction: Boolean,
    @ColumnInfo(name = "timeStamp") var timeStamp: Long = Date().time / 1000,
    @PrimaryKey(autoGenerate = true) var id: Int = 0) {


}