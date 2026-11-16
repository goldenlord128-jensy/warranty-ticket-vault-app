package com.example.warrantyvault

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "items")
data class VaultItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val brand: String = "",
    val modelNumber: String = "",
    val purchaseDate: String = "",
    val warrantyMonths: Int = 0,
    val price: Double = 0.0,
    val imagePath: String? = null,
    val category: String = "Warranty Receipt",
    val passengerCount: Int = 1,
    val eventTime: String = ""
) {
    // No-argument constructor required by Firebase Firestore for data deserialization
    constructor() : this(
        id = UUID.randomUUID().toString(),
        title = "",
        brand = "",
        modelNumber = "",
        purchaseDate = "",
        warrantyMonths = 0,
        price = 0.0,
        imagePath = null,
        category = "Warranty Receipt",
        passengerCount = 1,
        eventTime = ""
    )
}