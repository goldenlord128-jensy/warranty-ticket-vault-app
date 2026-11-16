package com.example.warrantyvault

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class KeyManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences by lazy {
        val prefName = "secure_vault_prefs"
        try {
            createEncryptedPrefs(prefName)
        } catch (t: Throwable) {
            Log.e("KeyManager", "EncryptedSharedPreferences failed, clearing and retrying", t)
            try {
                context.deleteSharedPreferences(prefName)
                createEncryptedPrefs(prefName)
            } catch (t2: Throwable) {
                Log.e("KeyManager", "Critical failure in EncryptedSharedPreferences, using plain fallback", t2)
                context.getSharedPreferences("${prefName}_plain", Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPrefs(name: String): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getDeviceId(): String {
        var id = sharedPreferences.getString("device_hardware_id", null)
        if (id == null) {
            id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "anon_${System.currentTimeMillis()}"
            sharedPreferences.edit().putString("device_hardware_id", id).apply()
        }
        return id
    }

    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString("user_gemini_api_key", apiKey).apply()
    }

    fun getUserApiKey(): String? {
        return try {
            sharedPreferences.getString("user_gemini_api_key", null)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteApiKey() {
        sharedPreferences.edit().remove("user_gemini_api_key").apply()
    }
}