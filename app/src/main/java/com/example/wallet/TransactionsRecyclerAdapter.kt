package com.example.wallet

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class TransactionsRecyclerAdapter(private val context: Context, private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionsRecyclerAdapter.ViewHolder>() {
    private val layoutInflator = LayoutInflater.from(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionValueTextView = itemView.findViewById<TextView>(R.id.transaction_amount)
        val transactionDateTextView = itemView.findViewById<TextView>(R.id.date)
        val transactionTypeTextView = itemView.findViewById<TextView>(R.id.transaction_type)
        val confirmationTexView = itemView.findViewById<TextView>(R.id.confirmation_text_view)
        val arrowImageView = itemView.findViewById<ImageView>(R.id.arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflator.inflate(R.layout.transaction_list_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.transactionDateTextView.text = "${transaction.date}"

        if (transaction.isConfirmed) {
            holder.confirmationTexView.text = "Confirmed"
            holder.confirmationTexView.setTextColor(Color.parseColor("#16bd00"))
        } else {
            holder.confirmationTexView.text = "Unconfirmed"
            holder.confirmationTexView.setTextColor(Color.parseColor("#900C3F"))
        }



        val df = DecimalFormat("#.#")
        df.maximumFractionDigits = 10
        val transactionValue = df.format(transaction.value).toString().replace(',', '.')


        holder.transactionValueTextView.text = "${transactionValue} BTC"

        println("!!!!! hash: ${transaction.hash}")


        if (transaction.isIncomingTransaction) {
            holder.transactionTypeTextView.text = "Received"
            holder.arrowImageView.rotation = 0.0F
        } else {
            holder.transactionTypeTextView.text = "Sent"
            holder.arrowImageView.rotation = 180.0F
        }
    }
}