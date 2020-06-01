package com.example.wallet

import android.content.Context
import android.util.Log
import androidx.room.Room
import java.text.DecimalFormat
import java.util.*
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextFloat
import kotlin.random.Random.Default.nextLong

object DataManager(val context: Context) {
    lateinit var db : AppDataBase
    var wallet = Wallet(db)

    var transactions = mutableListOf<Transaction>()
    var currentBalance = 1.0034
    var walletAdress = "19Wswgu8hgcc72XGSrFsRhtjuSSJJMP7B2"
    val transactionsApiUrl = "https://blockchain.info/rawaddr/${walletAdress}"

    init {
        transactions.sortBy { it.timeStamp }
        transactions.reverse()
    }

    fun createMockData() {

        val dec = DecimalFormat("#.####")

        for (number in 1..20 ) {
            val transactionValue = nextDouble()
            dec.format(transactionValue)


            var mockTransaction = Transaction(transactionValue, Date().toString(), true, 43434, "mock${number}")

            if (number%2 == 0) {
                mockTransaction.isIncomingTransaction = false
            }

            transactions.add(mockTransaction)

            Log.d("!!!!", mockTransaction.value.toString())
        }
    }
}