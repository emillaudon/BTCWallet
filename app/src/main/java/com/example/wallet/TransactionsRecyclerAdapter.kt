package com.example.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionsRecyclerAdapter(private val context: Context, private val transactions: List<Transaction>) : RecyclerView.Adapter<TransactionsRecyclerAdapter.ViewHolder>() {
    private val layoutInflator = LayoutInflater.from(context)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionValueTextView = itemView.findViewById<TextView>(R.id.transaction_amount)
        val transactionDateTextView = itemView.findViewById<TextView>(R.id.date)
        val transactionTypeTextView = itemView.findViewById<TextView>(R.id.transaction_type)
        val arrowImageView = itemView.findViewById<ImageView>(R.id.arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflator.inflate(R.layout.transaction_list_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = DataManager.transactions[position]

        //holder.transactionDateTextView.text = "1${position}/04/2020"

        holder.transactionDateTextView.text = "${transaction.date}"

        holder.transactionValueTextView.text = "${transaction.value.toString()} BTC"

        println("!!!!! hash: ${transaction.hash}")


        if (transaction.isIncomingTransaction) {
            holder.transactionTypeTextView.text = "Received"
        } else {
            holder.transactionTypeTextView.text = "Sent"
            holder.arrowImageView.rotation = 180.0F
        }
    }

}