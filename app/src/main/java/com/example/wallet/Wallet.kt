package com.example.wallet

import android.os.AsyncTask
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.AccessController.getContext
import java.util.*
import java.util.Calendar.getInstance

class Wallet(val db: AppDataBase, var balance: Double = 0.0, val address: String = "19Wswgu8hgcc72XGSrFsRhtjuSSJJMP7B2", val keyHolder: KeyHolder = KeyHolder() ) {

    //https://blockchain.info/rawtx/$tx_hash

    fun performTransaction(transaction: Transaction, receiver: String) {
        val valueInSats = (transaction.value * 100000000).toInt()
        println("!!!!! value: ${valueInSats}")

        AsyncTaskHandlePHP().execute("http://64.225.104.154/send.php?loginID=${this.keyHolder.loginID}&password=${this.keyHolder.password}&amount=${transaction.value * 100000000}&fee=1000&to=${receiver}")

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
                    println("!!!! hash ej null")
                    //val newTransaction = Transaction(value, Date().toString(), false, Date().time/1000, hash)

                    for (transaction in DataManager.transactions) {
                        if (transaction.hash == "placeHolder") {
                            transaction.hash = hash
                            transaction.isConfirmed = true
                        }
                    }

                    /*
                    DataManager.transactions.add(newTransaction)
                    this.balance -= newTransaction.value
                    */
                }
            }

            /*
            val jsonObject = JSONObject(jsonString)
            val hash = jsonObject.getString("txid")
            val valueArray = jsonObject.getJSONArray("amounts")
            val value = valueArray.getString(0).toDouble()
            */


        } catch (e: Exception) {
            println("!!!! didnt work ${e}")

        }
    }
}