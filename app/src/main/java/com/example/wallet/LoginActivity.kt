package com.example.wallet

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class LoginActivity : AppCompatActivity() {
    lateinit var editText1 : EditText
    lateinit var editText2 : EditText
    lateinit var editText3 : EditText
    lateinit var editText4 : EditText

    lateinit var passwordEditTexts : List<EditText>
    lateinit var pinCode : String

    private var passwordInput = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        passwordEditTexts = arrayListOf(findViewById(R.id.editText), findViewById(R.id.editText2), findViewById(R.id.editText3), findViewById(R.id.editText4))

        setupLogin()

        var layout = findViewById<TableLayout>(R.id.passwordtablelayout)

        setUpClickListeners(layout)

    }

    override fun onResume() {
        super.onResume()
        setupLogin()

    }

    fun setupLogin() {
        getPinCode()
        clearPassword()
        passwordInput = "null"
    }

    fun getPinCode() {
        val db = Room.databaseBuilder(applicationContext, AppDataBase::class.java, "transactions")
            .fallbackToDestructiveMigration()
            .build()
        val wallet = Wallet(db)

        GlobalScope.async (Dispatchers.IO){
            pinCode = wallet.getPinCodeFromDataBase().toString()
        }.invokeOnCompletion {
            clearPassword()
        }
    }

    fun login() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun setUpClickListeners(layout: TableLayout) {
        var buttons = mutableListOf<Button>()
        for (i in 0..3) {
            val row = layout.getChildAt(i) as TableRow

            if (i <= 2) {
                for (j in 0..2) {
                    val button = row.getChildAt(j) as Button
                    buttons.add(button)
                }
            } else {
                val button = row.getChildAt(0) as Button
                buttons.add(button)
            }
        }
        for (button in buttons) {
            button.setOnClickListener {view ->
                numButtonPressed(button, view)
            }
        }
    }

    fun clearPassword() {
        for (editText in passwordEditTexts) {
            editText.setText("")
            editText.setTextColor(Color.parseColor("#FFFFFF"))
        }
        passwordInput = ""
    }

    fun addToPassword(number: String, view: View) {
        passwordInput += number
        passwordEditTexts[passwordInput.length - 1].setText(number)
        if(passwordInput.length == 4) {
            if (passwordInput.equals(pinCode)) {
                for (editText in passwordEditTexts) {
                    val colorFrom = Color.parseColor("#FFFFFF")
                    val colorTo = Color.parseColor("#16bd00")
                    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                    colorAnimation.addUpdateListener { animator ->
                        editText.setTextColor(animator.animatedValue as Int)
                        if (colorAnimation.animatedValue == -15287040) {
                            login()
                            clearPassword()
                        }
                    }
                    colorAnimation.start()
                }

            } else {
                Snackbar.make(view, "Wrong pin, try again.", Snackbar.LENGTH_SHORT)
                    .show()
                for (editText in passwordEditTexts) {
                    val colorFrom = Color.parseColor("#FFFFFF")
                    val colorTo = Color.parseColor("#ca3e47")
                    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
                    colorAnimation.setDuration(1000)
                    colorAnimation.addUpdateListener { animator ->
                        editText.setTextColor(animator.animatedValue as Int)
                        if (colorAnimation.animatedValue == -3523001) {
                            clearPassword()
                        }

                    }
                    colorAnimation.start()
                }

            }
        } else {

        }
    }

    fun numButtonPressed(button: Button, view: View) {
        if (passwordInput.length != 4) {
            val value = button.text

            addToPassword(value.toString(), view)

            button.animate()
                .setDuration(10)
                .scaleX(1.1f)
                .scaleY(1.1f)
                .withEndAction {
                    button.animate()
                        .setStartDelay(10)
                        .setDuration(1000)
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                }
                .start()
        }

    }
}