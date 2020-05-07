package com.example.wallet

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    lateinit var balanceInFiatTextView : TextView

    lateinit var viewPager: ViewPager
    lateinit var linearLayout: LinearLayout

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    lateinit var db : AppDataBase

    var dm = DataManager
    var walletAdress = DataManager.walletAdress
    val apiUrl = "https://blockchain.info/ticker"
    val transactionsApiUrl = "https://blockchain.info/rawaddr/${walletAdress}"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDataBase::class.java, "transactions")
            .build()

        job = Job()

        val transactionTest = Transaction(1337F, "22", false)

        getTransactionsForDatamanager()

        val balanceInBTC = findViewById<TextView>(R.id.balance_count)
        balanceInFiatTextView = findViewById(R.id.balance_fiat)

        balanceInBTC.text = "${dm.currentBalance.toString()} BTC"

        val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = TransactionsRecyclerAdapter(this, DataManager.transactions)


        getWalletBalance()

        val fabButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fabButton.setOnClickListener {
            showPopup()
        }
    }

    fun getTransactionsForDatamanager() {
        val transactions = loadTransactionsFromDatabase()

        GlobalScope.launch {
            transactions.await().forEach {transaction ->
                dm.transactions.add(transaction)
            }
        }.invokeOnCompletion {
            transactionsRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    fun loadTransactionsFromDatabase() : Deferred<List<Transaction>>{
        return GlobalScope.async(Dispatchers.IO) {
            db.transactionDao().getAll()
        }
    }

    fun saveTransaction(transaction: Transaction) {
        GlobalScope.async (Dispatchers.IO){db.transactionDao().insert(transaction)  }
    }

    fun showPopup() {

        getTransactionsFromBlockchain()

        val dialog = Dialog(this)
        var dialogWindowAttributes = dialog.window?.attributes
        dialogWindowAttributes?.gravity = Gravity.BOTTOM

        dialog.setContentView(R.layout.fab_popup)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        viewPager = dialog.findViewById(R.id.sliderviewpager)

        var sliderAdapter = SliderAdapter(this)

        viewPager.adapter = sliderAdapter
        viewPager.setPageTransformer(false, FadePageTransfomer())

        linearLayout = dialog.findViewById(R.id.dotlinearlayout)

        val backButton = dialog.findViewById<Button>(R.id.fab_inside_popupwindow)


        /*
        val imageView = dialog.findViewById<ImageView>(R.id.qr_imageview)
        val walletAdressTextView = dialog.findViewById<TextView>(R.id.textview_adress)
        val copyButton = dialog.findViewById<Button>(R.id.copy_button)

        walletAdressTextView.text = walletAdress

        try {
            val encoder = BarcodeEncoder()
            val bitmap = encoder.encodeBitmap(walletAdress, BarcodeFormat.QR_CODE, 500, 500)

            imageView.setImageBitmap(bitmap)

        } catch(e: Exception) {
            e.printStackTrace()
        }



        */


        backButton.setOnClickListener {
            dialog.dismiss()
        }


        /*copyButton.setOnClickListener {view ->
            copyToClipBoard(walletAdress)
            Snackbar.make(view, "Wallet adress copied to clipboard.", Snackbar.LENGTH_LONG)
                .show()
        } */
        dialog.show()
    }

    fun copyToClipBoard(view: View) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copy text", walletAdress)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(view, "Wallet adress copied to clipboard.", Snackbar.LENGTH_LONG)
            .show()
    }

    fun parseTransactionDate(unixDate: Long) : Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = Date(unixDate)
        sdf.format(date)

        return date
    }

    fun getTransactionsFromBlockchain() {
        AsyncTaskHandleJson().execute(transactionsApiUrl)
    }

    fun getLatestBTCPrice() {
        AsyncTaskHandleJson().execute(apiUrl)
    }

    fun calculateBTCToUSD(currentUSDValue: Double, BTC: Double) : Double {
        return currentUSDValue * BTC
    }

    fun updateValueUSD(currentUSDValue: Double) {
        val bTCInFiat = calculateBTCToUSD(currentUSDValue, dm.currentBalance)
        val roundedBalance = roundOffDecimal(bTCInFiat)
        balanceInFiatTextView.text = "${roundedBalance} USD"
    }

    fun updateBitcoinBalance(value: String) {
        val newBalance = value.toFloat() / 100000000
        DataManager.currentBalance = newBalance.toDouble()
        balance_count.text = "${newBalance} BTC"
    }

    fun getWalletBalance() {
        val urlBalance = "https://blockchain.info/q/addressbalance/${walletAdress}?confirmations=6"
        AsyncTaskHandleJson().execute(urlBalance)
    }

    fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).replace(",", ".").toDouble()
    }

    inner class AsyncTaskHandleJson : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {
            var text: String
            lateinit var connection : HttpURLConnection
            try {
                connection = URL(url[0]).openConnection() as HttpURLConnection
                connection.connect()
                text =
                    connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
            } catch (e: Exception) {
                text = "no data"
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
        val newTransactions = mutableListOf<Transaction>()

        try {
            val jsonObject = JSONObject(jsonString)
            val txs = jsonObject.getJSONArray("txs")

            for (i in 0 until txs.length()) {
                val transaction = txs.getJSONObject(i)
                val outputs = transaction.getJSONArray("out")

                for (i in 0 until outputs.length()) {
                    val output = outputs.getJSONObject(i)
                    try {
                        val adress: String? = output.getString("addr")
                        if (adress.equals(walletAdress)) {
                            println("!!!! true")
                            val value = output.getString("value").toFloat() / 100000000
                            val isIncoming = output.getString("spent")

                            val transaction = Transaction(value, Date().toString(), !isIncoming.toBoolean())
                            newTransactions.add(transaction)
                            dm.transactions.add(transaction)
                        }

                        println(adress)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
                //dm.transactions = newTransactions


                println(outputs)
            }

            println(dm.transactions.count())
            transactionsRecyclerView.adapter?.notifyDataSetChanged()
            return

        } catch (e: Exception) {
            println(e)
        }


        try {
            val jsonObject = JSONObject(jsonString)
            val JSON = jsonObject.getJSONObject("USD")
            val latestUSDValue = JSON.getDouble("last")
            println("!!!!! ${latestUSDValue}")
            updateValueUSD(latestUSDValue)
        } catch (e: Exception) {
            try {
                if (jsonString != null) {
                    updateBitcoinBalance(jsonString)
                    getLatestBTCPrice()
                }
            } catch (e: Exception)   {
                println(e)
            }
        }









    }



}
