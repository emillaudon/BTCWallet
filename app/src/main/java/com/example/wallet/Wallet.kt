package com.example.wallet

import android.os.AsyncTask
import androidx.room.Room
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.AccessController.getContext
import java.util.*
import java.util.Calendar.getInstance

class Wallet(val db: AppDataBase, var balance: Double = 0.0, val address: String = "19Wswgu8hgcc72XGSrFsRhtjuSSJJMP7B2", val keyHolder: KeyHolder = KeyHolder() ) {
    var transactions = mutableListOf<Transaction>()
    //https://blockchain.info/rawtx/$tx_hash

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