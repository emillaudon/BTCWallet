package com.example.wallet

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat


class MainActivity : AppCompatActivity() {
    lateinit var balanceInFiatTextView : TextView

    lateinit var viewPager: ViewPager
    lateinit var linearLayout: LinearLayout

    var walletAdress = "35wgJ7i8hC2Cfx4dwqAqNobCUJPYkxMJqF"
    var dm = DataManager
    val apiUrl = "https://blockchain.info/ticker"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    fun showPopup() {
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

    private fun handleJson(jsonString: String?) {
        try {
            val jsonObject = JSONObject(jsonString)
            val JSON = jsonObject.getJSONObject("USD")
            val latestUSDValue = JSON.getDouble("last")
            println("!!!!! ${latestUSDValue}")
            updateValueUSD(latestUSDValue)
        } catch (e: Exception) {
            if (jsonString != null) {
                updateBitcoinBalance(jsonString)
                getLatestBTCPrice()
            }
            println("!!!!! ${jsonString}")
        }









    }



}
