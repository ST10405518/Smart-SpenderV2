package com.example.SmartSpender.models

import java.util.*

data class Notification(
    val id: Int,
    val userId: Int,
    val message: String,
    val type: String,
    val amount: Double,
    val date: Date
)
