package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val user_id: String? = null,
    val amount: Double,
    val category: String,
    val paymentMode: String,
    val date: String,
    val time: String,
    val timestamp: Long,
    val notes: String = "",
    val is_synced: Boolean = false,
    val is_deleted: Boolean = false,
    val updated_at: Long = System.currentTimeMillis()
)
