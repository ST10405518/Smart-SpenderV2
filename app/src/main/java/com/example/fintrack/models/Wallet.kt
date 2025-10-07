package com.example.SmartSpender.models

data class Wallet(
    val id: Int,
    val userId: Int,
    var nameOnCard: String,
    var cardNumber: String,
    var cvc: String,
    var expirationDate: String,
    var zip: String,
    var balance: Double
)
