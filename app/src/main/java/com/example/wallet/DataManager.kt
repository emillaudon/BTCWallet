package com.example.wallet

import android.util.Log
import java.text.DecimalFormat
import java.util.*
import kotlin.random.Random.Default.nextFloat

object DataManager {
    val transactions = mutableListOf<Transaction>()
    var currentBalance = 1.0034
    var walletAdress = "bc1qcz7txdgnlla3zxcxdf2panhr9uzzyyf5nr2ek7"

    init {
        println("ok")
        Log.d("!!!!", "händer")
        createMockData()
    }

    fun createMockData() {

        val dec = DecimalFormat("#.####")

        for (number in 1..20 ) {
            val transactionValue = nextFloat()
            dec.format(transactionValue)


            var mockTransaction = Transaction(transactionValue, Date(), true)

            if (number%2 == 0) {
                mockTransaction.isIncomingTransaction = false
            }

            transactions.add(mockTransaction)

            Log.d("!!!!", mockTransaction.value.toString())
        }
    }
}