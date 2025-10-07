package com.example.SmartSpender.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.SmartSpender.R
import com.example.SmartSpender.models.Transaction
import java.text.NumberFormat
import java.util.*

class TransactionAdapter(
    private val context: Context,
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(transaction: Transaction)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCategoryIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)
        val tvTransactionName: TextView = view.findViewById(R.id.tvTransactionName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvTransactionName.text = transaction.name
        holder.tvCategory.text = transaction.category

        // Format amount
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        currencyFormat.currency = Currency.getInstance("ZAR")
        val formattedAmount = currencyFormat.format(kotlin.math.abs(transaction.amount)).replace("ZAR", "R")
        holder.tvAmount.text = if (transaction.amount < 0) "-$formattedAmount" else "+$formattedAmount"
        holder.tvAmount.setTextColor(
            ContextCompat.getColor(
                context,
                if (transaction.amount < 0) R.color.colorExpense else R.color.colorIncome
            )
        )

        // Set category icon based on category
        val iconResId = when (transaction.category.lowercase(Locale.ROOT)) {
            "food" -> R.drawable.food
            "entertainment" -> R.drawable.entertainment
            "groceries" -> R.drawable.groceries
            "movies" -> R.drawable.movies
            "transport" -> R.drawable.transport
            "savings" -> R.drawable.savings
            "utilities" -> R.drawable.utilities
            else -> R.drawable.other
        }
        holder.ivCategoryIcon.setImageResource(iconResId)

        // Set click listener
        holder.itemView.setOnClickListener {
            listener?.onItemClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
