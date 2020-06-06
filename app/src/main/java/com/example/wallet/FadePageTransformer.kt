package com.example.wallet


import android.view.View
import androidx.core.view.ViewCompat.animate
import androidx.viewpager.widget.ViewPager


/**
 * Created by realandylawton on 11/22/13.
 */
class FadePageTransfomer : ViewPager.PageTransformer {
    override fun transformPage(
        view: View,
        position: Float
    ) {

        //Checks position of page and changes alpha depending on where it is.
        if (position < -0.6 || position > 0.6) {
            view.alpha = 0f
        } else if (position <= 0 || position <= 1) {

            // Calculate alpha.  Position is decimal in [-1,0] or [0,1]
            val alpha = if (position < 0) position + 0.5f else 0.5f - position
            view.alpha = alpha
        }
        if (position == 0f) {
            view.animate()
                .alpha(1f)
                .setDuration(0.5.toLong())

        }
    }
}