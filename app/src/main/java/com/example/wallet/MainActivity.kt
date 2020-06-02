package com.example.wallet

import android.app.AlertDialog
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    lateinit var balanceInFiatTextView : TextView

    lateinit var viewPager: ViewPager
    lateinit var linearLayout: LinearLayout
    lateinit var dialog : Dialog

    lateinit var db : AppDataBase
    lateinit var wallet: Wallet
    lateinit var transactionsApiUrl : String
    lateinit var walletAdress : String

    private lateinit var job : Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    val apiUrl = "https://blockchain.info/ticker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDataBase::class.java, "transactions")
            .fallbackToDestructiveMigration()
            .build()
        job = Job()
        wallet = Wallet(db)
        walletAdress = wallet.address
        transactionsApiUrl = "https://blockchain.info/rawaddr/${walletAdress}"

        val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TransactionsRecyclerAdapter(this, wallet.transactions)
        val pullToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)

        recyclerView.canScrollVertically(-1)

        setupUI()

        val chooseFiatButton = findViewById<Button>(R.id.choose_fiat_button)


        val fabButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fabButton.setOnClickListener {
            showFabPopup()
        }
        pullToRefresh.setOnRefreshListener {
            getTransactionsFromBlockchain()
            getLatestBTCPrice()
            getWalletBalance()
            get24hPriceChange()
            pullToRefresh.setRefreshing(false)
        }
    }



    fun setupUI() {
        wallet.getTransactionsFromDataBase {
            transactionsRecyclerView.scheduleLayoutAnimation()
            updateRecyclerView()
        }

        GlobalScope.async (Dispatchers.IO){wallet.getBalanceFromDataBase {
            val balanceInBTC = findViewById<TextView>(R.id.balance_count)
            balanceInBTC.text = "${wallet.balance.balanceBTC.toFloat().toString()} BTC"
            balanceInFiatTextView.text = "${wallet.balance.valueInFiat.toString()} USD"
            updateRecyclerView() } }

        getWalletBalance()
        get24hPriceChange()

        balanceInFiatTextView = findViewById(R.id.balance_fiat)
    }

    fun saveTransaction(transaction: Transaction) {
        GlobalScope.async (Dispatchers.IO){db.transactionDao().insert(transaction)  }
    }

    fun copyToClipBoard(view: View) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("copy text", walletAdress)
        clipboard.setPrimaryClip(clip)
        Snackbar.make(view, "Wallet adress copied to clipboard.", Snackbar.LENGTH_LONG)
            .show()
    }

    fun parseUnixTransactionDate(unixDate: Long) : String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = Date(unixDate * 1000)

        //TODO: Local time

        return sdf.format(date)
    }

    fun get24hPriceChange() {
        val apiAdress = "https://api.coinpaprika.com/v1/tickers/btc-bitcoin/historical?start=1591005600"
        AsyncTaskHandleJson().execute(apiAdress)
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
        val bTCInFiat = calculateBTCToUSD(currentUSDValue, wallet.balance.balanceBTC)
        val roundedBalance = roundOffDecimal(bTCInFiat)
        wallet.updateAndSaveBalance(wallet.balance.balanceBTC, roundedBalance)
        balanceInFiatTextView.text = "${roundedBalance} USD"
    }

    fun update24hPriceChange(oldPrice: Int, latestPrice: Int) {
        val percentChange = ((latestPrice - oldPrice) * 100) / oldPrice
        val changeTextView = findViewById<TextView>(R.id.change)

        if(percentChange > 0) {
            changeTextView.text = "+${percentChange}%"
            changeTextView.setTextColor(Color.parseColor("#16bd00"))
        } else if (percentChange < 0){
            changeTextView.text = "${percentChange}%"
            changeTextView.setTextColor(Color.parseColor("#bd0000"))
        } else {
            changeTextView.setTextColor(Color.parseColor("#8a8888"))
            changeTextView.text = "${percentChange}%"
        }


    }

    fun updateBitcoinBalance(value: String) {
        val newBalance = value.toFloat() / 100000000

        getLatestBTCPrice()
        wallet.updateAndSaveBalance(newBalance.toDouble(), wallet.balance.valueInFiat)
        balance_count.text = "${newBalance} BTC"
    }

    fun getWalletBalance() {
        val urlBalance = "http://64.225.104.154/balance.php"
        AsyncTaskHandleJson().execute(urlBalance)
    }

    fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).replace(",", ".").toDouble()
    }

    fun updateRecyclerView() {
        wallet.transactions.sortBy { it.timeStamp }
        wallet.transactions.reverse()
        transactionsRecyclerView.adapter?.notifyDataSetChanged()
    }

    fun sendTransaction(view: View) {
        val transactionAdressEditText = view.rootView.findViewById<EditText>(R.id.editText_adress)
        val transactionValueEditText = view.rootView.findViewById<EditText>(R.id.editText_amount)

        if (transactionAdressEditText.text.toString() != "" && transactionValueEditText.text.toString() != "") {

            try {
                var transactionValue = transactionValueEditText.text.toString().replace(',', '.')
                    if (wallet.balance.balanceBTC >= transactionValue.toDouble()) {

                        var newTransaction = Transaction(
                            transactionValue.toDouble(),
                            parseUnixTransactionDate(Date().time / 1000),
                            false,
                            Date().time / 1000,
                            "placeHolder",
                            false
                        )
                        wallet.transactions.add(newTransaction)

                        wallet.performTransaction(newTransaction, transactionAdressEditText.text.toString())

                        dialog.dismiss()

                        updateRecyclerView()
                } else {
                        Snackbar.make(view, "Input value higher than balance of wallet.", Snackbar.LENGTH_SHORT)
                            .show()
                    }
            } catch (e: Exception) {
                Snackbar.make(view, "Please use a correct value.", Snackbar.LENGTH_SHORT)
                    .show()
            }
        } else {
            Snackbar.make(view, "Receiver adress or value not input.", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    fun showChooseFiatPopup(view: View) {
        val singleItems = arrayOf("Item 1", "Item 2", "Item 3")
        val checkedItem = 1
        val context = view.context

        val checkboxView = View.inflate(this, R.layout.checkboxview, null)
        val checkBox = checkboxView.findViewById<CheckBox>(R.id.checkbox)

            AlertDialog.Builder(context)
            .setTitle("Hej")
            .setMessage("hejhej")
                .setView(checkboxView)
            .setPositiveButton("ok") {dialog, which ->

            }
            .setNegativeButton("nej") {dialog, which ->

            }
            .show()
    }

    fun showFabPopup() {
        dialog = Dialog(this)
        var dialogWindowAttributes = dialog.window?.attributes
        dialogWindowAttributes?.gravity = Gravity.BOTTOM

        dialog.setContentView(R.layout.fab_popup)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        viewPager = dialog.findViewById(R.id.sliderviewpager)

        var sliderAdapter = SliderAdapter(this, wallet.address)

        viewPager.adapter = sliderAdapter
        viewPager.setPageTransformer(false, FadePageTransfomer())

        linearLayout = dialog.findViewById(R.id.dotlinearlayout)

        val backButton = dialog.findViewById<Button>(R.id.fab_inside_popupwindow)

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
                println("!!!! EXCEPTION${e} Text: ${text}")
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
        println(jsonString)

        //Transactions
        try {
            val newTransactions = mutableListOf<Transaction>()

            val jsonObject = JSONObject(jsonString)
            val txs = jsonObject.getJSONArray("txs")

            for (i in 0 until txs.length()) {
                val transaction = txs.getJSONObject(i)
                val blockDate = transaction.getString("time")
                val transactionHash = transaction.getString("hash")
                val outputs = transaction.getJSONArray("out")

                for (i in 0 until outputs.length()) {
                    val output = outputs.getJSONObject(i)
                    try {
                        val adress: String? = output.getString("addr")
                        if (adress.equals(walletAdress)) {

                            val value = output.getString("value").toDouble() / 100000000.toDouble()

                            val isIncoming = output.getString("spent")

                            val transaction = Transaction(value, parseUnixTransactionDate(blockDate.toLong()), !isIncoming.toBoolean(), blockDate.toLong(), transactionHash)
                            newTransactions.add(transaction)
                        }

                        println(adress)
                    } catch (e: Exception) {
                        println(e)
                    }
                }
            }
            //Compare transactions with saved
            var listChanged = false
            for (transaction in newTransactions) {
                if (wallet.transactions.contains(transaction)) {
                    return
                } else {
                    listChanged = true
                    wallet.transactions.add(transaction)
                    saveTransaction(transaction)
                }
            }
            if (listChanged) {
                updateRecyclerView()
            }
            return
        } catch (e: Exception) {
            println(e)
        }

        //24 hour price movement
        try {
            val jsonArray = JSONArray(jsonString)
            val jsonObjectForOldPrice : JSONObject = jsonArray[0] as JSONObject
            val oldPrice = jsonObjectForOldPrice.getInt("price")

            val jsonObjectForLatestPrice = jsonArray[jsonArray.length() - 1] as JSONObject
            val newPrice = jsonObjectForLatestPrice.getInt("price")

            update24hPriceChange(oldPrice, newPrice)
            return


        } catch (e: Exception) {


        }

        // USD Price
        try {
            val jsonObject = JSONObject(jsonString)
            val JSON = jsonObject.getJSONObject("USD")
            val latestUSDValue = JSON.getDouble("last")
            updateValueUSD(latestUSDValue)
        } catch (e: Exception) {
            
            //Balance
            try {
                if (jsonString != null) {
                    val stringWithoutPrefix = jsonString.removePrefix("<pre>array(1) {\n" +
                            "  [\"balance\"]=>\n" +
                            "  int(")
                    var fixedString = stringWithoutPrefix.removeSuffix(")" + "\\n\" +\n" +
                            "                        \"}")
                    fixedString = fixedString.filter { it.isDigit() }

                    updateBitcoinBalance(fixedString)
                    getLatestBTCPrice()
                }
            } catch (e: Exception)   {
                println("!!!!! ${e}")
            }


        }

    }
}
