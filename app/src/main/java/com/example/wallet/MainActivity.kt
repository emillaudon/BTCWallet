package com.example.wallet

import android.app.Dialog
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.PixelFormat.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    lateinit var balanceInFiatTextView : TextView

    var dm = DataManager
    val apiUrl = "https://blockchain.info/ticker"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceInFiatTextView = findViewById(R.id.balance_fiat)

        val recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.adapter = TransactionsRecyclerAdapter(this, DataManager.transactions)

        getLatestBTCPrice()

        val fabButton = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fabButton.setOnClickListener {
            showPopup()
        }
    }

    fun showPopup() {
        val dialog = Dialog(this)
        var dialogWindowAttributes = dialog.window?.attributes
        dialogWindowAttributes?.gravity = Gravity.BOTTOM

        dialog.setContentView(R.layout.fab_popup)
        dialog.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var button = dialog.findViewById<Button>(R.id.fab_inside_popupwindow)

        button.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

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

    fun roundOffDecimal(number: Double): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).replace(",", ".").toDouble()
    }

    inner class AsyncTaskHandleJson : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg url: String?): String {

            var text: String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try{
                connection.connect()
                text = connection.inputStream.use { it.reader().use{reader -> reader.readText()} }
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

    private fun handleJson(jsonString: String?)  {
        val jsonObject = JSONObject(jsonString)
        val USDJSON = jsonObject.getJSONObject("USD")
        val latestUSDValue = USDJSON.getDouble("last")

        updateValueUSD(latestUSDValue)
    }



}
