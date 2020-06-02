package com.example.wallet

import android.content.Context
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class CustomSwipeRefreshLayout(context: Context) : SwipeRefreshLayout(context) {

    @Override
    fun canChildScrollup(): Boolean {
        return true
    }

}