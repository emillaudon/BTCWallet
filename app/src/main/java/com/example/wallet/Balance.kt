package com.example.wallet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Balance(
    @ColumnInfo(name = "balanceBTC") var balanceBTC: Double,
    @ColumnInfo(name = "fiatSetting") var fiatSetting: String = "USD",
    @PrimaryKey(autoGenerate = false) var id: Int = 0) {
}