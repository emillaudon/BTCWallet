package com.example.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.viewpager.widget.PagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class SliderAdapter(context: Context, walletAdress: String) : PagerAdapter() {
    val walletAdress = walletAdress
    private val layoutInflator = LayoutInflater.from(context)

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return 2
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var view: View

        if (position == 0) {
            //receive page
            view = layoutInflator.inflate(R.layout.slide_layout1, container, false)

            val imageView = view.findViewById<ImageView>(R.id.qr_imageview)
            val walletAdressTextView = view.findViewById<TextView>(R.id.textview_adress)

            walletAdressTextView.text = walletAdress

            try {
                val encoder = BarcodeEncoder()
                val bitmap = encoder.encodeBitmap(walletAdress, BarcodeFormat.QR_CODE, 500, 500)

                imageView.setImageBitmap(bitmap)

            } catch(e: Exception) {
                e.printStackTrace()
            }
        } else {
            view = layoutInflator.inflate(R.layout.slide_layout2, container, false)
        }



        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
        container.removeView(view as View?)
    }

    fun copyToClipBoard(text: CharSequence) {

    }
}
