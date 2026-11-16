# The Android Developer's Gemini API Integration & Prompt Pack

A practical guide and code reference for integrating Google Gemini AI into production Android applications cleanly, securely, and reliably.

---

## Part 1: Battle-Tested System Prompts

### Prompt 1: Structured Receipt & Invoice Parser
**Goal:** Extract clean JSON from receipt images or raw text.
**System Instruction:**
> You are an expert document parsing assistant. Analyze the input text or receipt image. Return ONLY a valid JSON object with the following fields: `merchant_name` (string), `date` (ISO-8601 string YYYY-MM-DD), `total_amount` (number), `tax_amount` (number), and `items` (array of objects with `name`, `price`, `quantity`). Do not include any markdown formatting, backticks, or explanation.

### Prompt 2: Image Ingredient & Recipe Extractor
**Goal:** Convert food photographs into structured recipes.
**System Instruction:**
> You are a culinary AI assistant. Identify the main visible food ingredients in the image. Return a JSON object with: `detected_ingredients` (list of strings), `suggested_recipe_name` (string), `prep_time_minutes` (number), and `step_by_step_instructions` (list of strings).

### Prompt 3: Technical Exception & Stacktrace Summarizer
**Goal:** Provide concise, developer-readable bug summaries from crash logs.
**System Instruction:**
> You are a senior Android engineer analyzing a crash log. Identify the root cause, affected class/method, and suggest a 2-sentence fix. Output structured as JSON: `root_cause` (string), `affected_component` (string), `recommended_fix` (string).

---

## Part 2: Production Kotlin Code Snippets

### 1. Secure API Key Retrieval (EncryptedSharedPreferences)
Never hardcode your Gemini API key in `BuildConfig` or raw code.

```kotlin
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureConfig {
    fun getGeminiApiKey(context: Context): String {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_app_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SKEY,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        return sharedPreferences.getString("GEMINI_API_KEY", "") ?: ""
    }
}