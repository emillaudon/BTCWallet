package com.example.wallet

import android.util.Log
import java.text.DecimalFormat
import java.util.*
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextFloat
import kotlin.random.Random.Default.nextLong

object DataManager {
    var transactions = mutableListOf<Transaction>()
    var currentBalance = 1.0034
    var walletAdress = "35wgJ7i8hC2Cfx4dwqAqNobCUJPYkxMJqF"
    val transactionsApiUrl = "https://blockchain.info/rawaddr/${walletAdress}"

    init {
        println("ok")
        Log.d("!!!!", "händer")
        transactions.sortBy { it.timeStamp }
        transactions.reverse()
        //createMockData()
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