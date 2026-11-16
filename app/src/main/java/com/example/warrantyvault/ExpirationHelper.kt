package com.example.warrantyvault

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class ExpirationStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED
}

data class ExpirationInfo(
    val status: ExpirationStatus,
    val displayText: String,
    val backgroundColor: Color,
    val textColor: Color
)

object ExpirationHelper {

    fun getExpirationInfo(purchaseDateStr: String, warrantyMonths: Int): ExpirationInfo {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val purchaseDate = try {
            LocalDate.parse(purchaseDateStr.trim(), formatter)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val expirationDate = purchaseDate.plusMonths(warrantyMonths.toLong())
        val today = LocalDate.now()
        val daysRemaining = ChronoUnit.DAYS.between(today, expirationDate)

        return when {
            daysRemaining < 0 -> ExpirationInfo(
                status = ExpirationStatus.EXPIRED,
                displayText = "Expired (${expirationDate.format(formatter)})",
                backgroundColor = Color(0xFFFFEBEE),
                textColor = Color(0xFFC62828)
            )

            daysRemaining <= 30 -> ExpirationInfo(
                status = ExpirationStatus.EXPIRING_SOON,
                displayText = "Expires in $daysRemaining Days",
                backgroundColor = Color(0xFFFFF8E1),
                textColor = Color(0xFFF57F17)
            )

            else -> {
                val monthsRemaining = ChronoUnit.MONTHS.between(today, expirationDate)
                val labelText = if (monthsRemaining > 0) "$monthsRemaining Months Left" else "$daysRemaining Days Left"
                ExpirationInfo(
                    status = ExpirationStatus.ACTIVE,
                    displayText = labelText,
                    backgroundColor = Color(0xFFE8F5E9),
                    textColor = Color(0xFF2E7D32)
                )
            }
        }
    }
}