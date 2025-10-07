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
import com.example.SmartSpender.models.Notification
import java.text.NumberFormat
import java.util.*

class NotificationAdapter(
    private val context: Context,
    private var notifications: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivNotificationIcon: ImageView = view.findViewById(R.id.ivNotificationIcon)
        val tvNotificationText: TextView = view.findViewById(R.id.tvNotificationText)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.tvNotificationText.text = notification.message

        // Format amount
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        currencyFormat.currency = Currency.getInstance("ZAR")
        val formattedAmount = currencyFormat.format(kotlin.math.abs(notification.amount)).replace("ZAR", "R")
        holder.tvAmount.text = if (notification.amount < 0) "-$formattedAmount" else "+$formattedAmount"
        holder.tvAmount.setTextColor(
            ContextCompat.getColor(
                context,
                if (notification.amount < 0) R.color.colorExpense else R.color.colorIncome
            )
        )

        // Set notification icon based on type
        val iconResId = when (notification.type.lowercase(Locale.ROOT)) {
            "warning" -> R.drawable.ic_notification
            "income" -> R.drawable.ic_add_circle
            "expense" -> R.drawable.ic_category
            else -> R.drawable.ic_notification
        }
        holder.ivNotificationIcon.setImageResource(iconResId)
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
