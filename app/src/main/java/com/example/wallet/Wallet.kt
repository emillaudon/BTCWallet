package com.example.wallet

import android.os.AsyncTask

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL


class Wallet(val db: AppDataBase, var balance: Balance = Balance(0.0, 0.0), val address: String = "19Wswgu8hgcc72XGSrFsRhtjuSSJJMP7B2", val keyHolder: KeyHolder = KeyHolder() ) {
    private lateinit var pinCode : String
    var transactions = mutableListOf<Transaction>()
    //https://blockchain.info/rawtx/$tx_hash
    init {

    }

    fun savePinCodeToDataBase(pin: String) {
        this.keyHolder.pinCode = pin
        GlobalScope.async (Dispatchers.IO){db.keyHolderDao().insert(keyHolder)  }
            .invokeOnCompletion {
                println("!!!!! done")
            }
    }

    fun getPinCodeFromDataBase() : String?{
        val loadedKeyHolder = db.keyHolderDao().loadKeyHolder(0)
        if (loadedKeyHolder != null) {
            this.keyHolder.pinCode = loadedKeyHolder.pinCode
            return loadedKeyHolder.pinCode
        } else {
            return null
        }
    }

    fun getBalanceFromDataBase(onCompletion: (Boolean) -> Unit) {
        val loadedBalance = db.balanceDao().loadBalance(0)
        if (loadedBalance != null) {
           balance = loadedBalance
            onCompletion(true)
        }
    }

    fun updateFiatSettingAndSaveToDB(fiat: String) {
        this.balance.fiatSetting = fiat
        GlobalScope.async (Dispatchers.IO){db.balanceDao().insert(Balance(balance.balanceBTC, balance.valueInFiat, fiat))  }
    }

    fun updateAndSaveBalance(balance: Double, valueInFiat: Double) {
        val currentFiat = this.balance.fiatSetting
        if (balance == 0.0 && valueInFiat == 0.0 || balance != 0.0 && valueInFiat != 0.0) {
            this.balance = Balance(balance, valueInFiat, currentFiat)
            GlobalScope.async (Dispatchers.IO){db.balanceDao().insert(Balance(balance, valueInFiat, currentFiat))  }
        } else if (balance == 0.0 && valueInFiat != 0.0) {
            this.balance.valueInFiat = valueInFiat
            val currentBalanceBtc = this.balance.balanceBTC
            GlobalScope.async (Dispatchers.IO){db.balanceDao().insert(Balance(currentBalanceBtc, valueInFiat, currentFiat))  }
        } else if (balance != 0.0 && valueInFiat == 0.0) {
            this.balance.balanceBTC = balance
            val currentValueFiat = this.balance.valueInFiat
            GlobalScope.async (Dispatchers.IO){db.balanceDao().insert(Balance(balance, currentValueFiat, currentFiat))  }
        }
    }

    fun performTransaction(transaction: Transaction, receiver: String) {
        val valueInSats = (transaction.value * 100000000).toInt()

        AsyncTaskHandlePHP().execute("http://64.225.104.154/send.php?loginID=${this.keyHolder.loginID}&password=${this.keyHolder.password}&amount=${transaction.value * 100000000}&fee=1000&to=${receiver}")
    }

    fun getTransactionsFromDataBase(onCompletion: (Boolean) -> Unit) {
        val transactionsFromDataBase = loadTransactionsFromDatabase()

        GlobalScope.launch {
            transactionsFromDataBase.await().forEach {transaction ->
                transactions.add(transaction)
            }
        }.invokeOnCompletion {
            transactions.sortBy { it.timeStamp }
            onCompletion(true)
        }
    }

    fun loadTransactionsFromDatabase() : Deferred<List<Transaction>> {
        return GlobalScope.async(Dispatchers.IO) {
            db.transactionDao().getAll()
        }
    }

    inner class AsyncTaskHandlePHP : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?) : String? {
            var text: String
            lateinit var connection : HttpURLConnection
            try {
                connection = URL(url[0]).openConnection() as HttpURLConnection
                connection.connect()
                text = connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: Exception) {
                text = "Transaction not sent"
                println("${e} Text: ${text}")
            } finally {
                connection.disconnect()
            }
            return text
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            handleJson(result)
        }

    }

    private fun handleJson(jsonString: String?) {
        try {
            println("!!!! transaktionen skickad ${jsonString}")
            if (jsonString != null) {
                val splitArray = jsonString.split("txid\"]=>")
                val secondSplitArray = splitArray[1].split('"')

                val hash = secondSplitArray[1]
                if (hash != null) {
                    for (transaction in transactions) {
                        if (transaction.hash == "placeHolder") {
                            transaction.hash = hash
                            transaction.isConfirmed = true
                            GlobalScope.async (Dispatchers.IO){db.transactionDao().insert(transaction)  }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("!!!! didnt work ${e}")
        }
    }
}