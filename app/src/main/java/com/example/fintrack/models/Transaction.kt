package com.example.SmartSpender.models

import java.util.*

data class Transaction(
    val id: Int,
    val userId: Int,
    val name: String,
    val company: String,
    val category: String,
    val amount: Double,
    val date: Date,
    val photo: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Transaction

        if (id != other.id) return false
        if (userId != other.userId) return false
        if (name != other.name) return false
        if (company != other.company) return false
        if (category != other.category) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        if (photo != null) {
            if (other.photo == null) return false
            if (!photo.contentEquals(other.photo)) return false
        } else if (other.photo != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + userId
        result = 31 * result + name.hashCode()
        result = 31 * result + company.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (photo?.contentHashCode() ?: 0)
        return result
    }
}
