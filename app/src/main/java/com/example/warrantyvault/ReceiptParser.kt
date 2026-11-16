package com.example.warrantyvault

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ReceiptParser {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun parseReceipt(bitmap: Bitmap): VaultItem = withContext(Dispatchers.IO) {
        val image = InputImage.fromBitmap(bitmap, 0)

        val rawText = suspendCancellableCoroutine<String> { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (continuation.isActive) {
                        continuation.resume(visionText.text)
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Exception("OCR Processing Failed: ${exception.localizedMessage}")
                        )
                    }
                }
        }

        // Extract accurate fields from raw text
        val parsedDate = extractDate(rawText)
        val parsedMonths = extractWarrantyMonths(rawText)
        val parsedBrand = extractBrand(rawText)
        val parsedPrice = extractPrice(rawText)
        val parsedCategory = extractCategory(rawText)
        val parsedPassengers = extractPassengerCount(rawText)
        val parsedTime = extractEventTime(rawText)
        val parsedTitle = extractTitle(rawText, parsedBrand)

        return@withContext VaultItem(
            title = parsedTitle,
            brand = parsedBrand,
            modelNumber = "",
            purchaseDate = parsedDate,
            warrantyMonths = parsedMonths,
            price = parsedPrice,
            imagePath = null,
            category = parsedCategory,
            passengerCount = parsedPassengers,
            eventTime = parsedTime
        )
    }

    private val commonBrands = listOf(
        "Apple", "Samsung", "Sony", "LG", "Dell", "HP", "Lenovo", "Asus", "Acer",
        "Philips", "Canon", "Nikon", "Bose", "JBL", "Panasonic", "Whirlpool",
        "Bosch", "Dyson", "Nintendo", "Microsoft", "Logitech", "Xiaomi", "OnePlus",
        "Google", "Intel", "AMD", "Nvidia", "JVC", "Sharp", "Electrolux", "Nike", "Adidas",
        "IKEA", "Zara", "H&M", "Under Armour", "Puma", "Reebok", "Emirates", "Delta",
        "Lufthansa", "IndiGo", "IRCTC", "Amtrak", "Eurostar", "National Express"
    )

    private fun extractBrand(text: String): String {
        for (brand in commonBrands) {
            if (text.contains(brand, ignoreCase = true)) {
                return brand
            }
        }
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            val firstLineWords = lines.first().split(" ")
            if (firstLineWords.isNotEmpty() && firstLineWords[0].firstOrNull()?.isUpperCase() == true) {
                return firstLineWords[0]
            }
        }
        return ""
    }

    private fun extractCategory(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("insurance") || lower.contains("policy") || lower.contains("coverage") -> "Insurance"
            lower.contains("flight") || lower.contains("airline") || lower.contains("boarding pass") || lower.contains("pnr") || lower.contains("seat") || lower.contains("airways") -> "Flight Ticket"
            lower.contains("train") || lower.contains("rail") || lower.contains("railway") || lower.contains("station") || lower.contains("irctc") || lower.contains("amtrak") || lower.contains("eurostar") || lower.contains("exp") || lower.contains("express") || lower.contains("mail") || lower.contains("superfast") -> "Train Ticket"
            lower.contains("bus") || lower.contains("transit") || lower.contains("coach") || lower.contains("travels") -> "Bus Ticket"
            lower.contains("movie") || lower.contains("cinema") || lower.contains("theater") || lower.contains("showtime") -> "Movie Ticket"
            lower.contains("concert") || lower.contains("event") || lower.contains("show") -> "Event Ticket"
            lower.contains("ticket") || lower.contains("booking") || lower.contains("pass") -> "Travel / Event Ticket"
            else -> "Warranty Receipt"
        }
    }

    private fun extractPassengerCount(text: String): Int {
        val seatPattern = Pattern.compile("\\b[A-Z]\\d+-\\d+\\b")
        val matcher = seatPattern.matcher(text)
        var count = 0
        while (matcher.find()) {
            count++
        }
        if (count > 0) return count

        val passengerPattern = Pattern.compile("(?:passengers?|adults?|travelers?|guests?|qty)[:\\s]*(\\d+)", Pattern.CASE_INSENSITIVE)
        val pMatcher = passengerPattern.matcher(text)
        if (pMatcher.find()) {
            return pMatcher.group(1)?.toIntOrNull() ?: 1
        }
        return 1
    }

    private fun extractEventTime(text: String): String {
        val timePattern = Pattern.compile("\\b(0?[1-9]|1[0-2]):([0-5]\\d)\\s*(AM|PM)\\b|\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b", Pattern.CASE_INSENSITIVE)
        val matcher = timePattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(0) ?: ""
        }
        return ""
    }

    private fun extractPrice(text: String): Double {
        val pricePatterns = listOf(
            Pattern.compile("(?:total|amount|price|due|paid|fare)[:\\s]*[$€£R]?\\s*(\\d{1,5}[.,]\\d{2})", Pattern.CASE_INSENSITIVE),
            Pattern.compile("[$€£R]\\s*(\\d{1,5}[.,]\\d{2})")
        )
        for (pattern in pricePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val priceStr = matcher.group(1)?.replace(",", ".")
                val price = priceStr?.toDoubleOrNull() ?: 0.0
                if (price > 0.0) return price
            }
        }
        return 0.0
    }

    private fun extractDate(text: String): String {
        val datePatterns = listOf(
            Pattern.compile("\\b(20\\d{2})[-/.](0[1-9]|1[0-2])[-/.](0[1-9]|[12]\\d|3[01])\\b"),
            Pattern.compile("\\b(0[1-9]|[12]\\d|3[01])[-/.](0[1-9]|1[0-2])[-/.](20\\d{2})\\b"),
            Pattern.compile("\\b(0?[1-9]|[12]\\d|3[01])\\s+([A-Za-z]{3,9})\\s+(20\\d{2})\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Za-z]{3,9})\\s+(0?[1-9]|[12]\\d|3[01]),?\\s+(20\\d{2})\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(0?[1-9]|[12]\\d|3[01])\\s+([A-Za-z]{3,9})\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b([A-Za-z]{3,9})\\s+(0?[1-9]|[12]\\d|3[01])\\b", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in datePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val rawDate = matcher.group(0) ?: continue
                val normalized = parseAndNormalizeDate(rawDate)
                if (normalized.isNotBlank()) return normalized
            }
        }
        return ""
    }

    private fun parseAndNormalizeDate(dateStr: String): String {
        val currentYear = java.time.LocalDate.now().year
        val formats = listOf(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("MMM d yyyy", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("dd MMM", java.util.Locale.ENGLISH),
            java.time.format.DateTimeFormatter.ofPattern("MMM d", java.util.Locale.ENGLISH)
        )
        for (fmt in formats) {
            try {
                // Check if format has year
                val fmtStr = fmt.toString().lowercase()
                if (fmtStr.contains("y")) {
                    val parsed = java.time.LocalDate.parse(dateStr.trim(), fmt)
                    return parsed.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    val parsed = java.time.MonthDay.parse(dateStr.trim(), fmt)
                    return parsed.atYear(currentYear).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            } catch (e: Exception) {
                // try next
            }
        }
        return ""
    }



    private fun extractWarrantyMonths(text: String): Int {
        // Look for expressions like "12 months", "1 year", "24 mths", "2 yrs"
        val monthPattern = Pattern.compile("(\\d+)\\s*(month|mth|mo|year|yr)", Pattern.CASE_INSENSITIVE)
        val matcher = monthPattern.matcher(text)

        if (matcher.find()) {
            val value = matcher.group(1)?.toIntOrNull() ?: 0
            val unit = matcher.group(2)?.lowercase() ?: ""

            return if (unit.startsWith("y")) {
                value * 12 // Convert years to months (e.g., 1 year -> 12 months)
            } else {
                value
            }
        }
        return 0 // Return 0 if not detected
    }

    private fun extractTitle(text: String, detectedBrand: String): String {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val skipWords = listOf("receipt", "invoice", "store", "tax", "total", "cashier", "date", "thank", "welcome", "item", "qty", "price", "subtotal", "balance", "visa", "mastercard")

        for (line in lines) {
            val lower = line.lowercase()
            val isHeader = skipWords.any { lower.contains(it) } || line.length < 3 || line.all { !it.isLetter() }
            if (!isHeader) {
                // If it's just the brand name, keep looking for the model/item name after it
                if (detectedBrand.isNotBlank() && line.equals(detectedBrand, ignoreCase = true)) {
                    continue
                }
                return line.take(30)
            }
        }
        return if (detectedBrand.isNotBlank()) "$detectedBrand Product" else (if (lines.isNotEmpty()) lines.first().take(30) else "Imported Receipt")
    }
}