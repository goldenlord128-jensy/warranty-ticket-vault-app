package com.example.warrantyvault

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BackupExporter(private val context: Context) {

    fun exportBackupJson(items: List<VaultItem>) {
        try {
            val jsonArray = JSONArray()
            items.forEach { item ->
                val jsonObject = JSONObject().apply {
                    put("title", item.title)
                    put("brand", item.brand)
                    put("modelNumber", item.modelNumber)
                    put("purchaseDate", item.purchaseDate)
                    put("warrantyMonths", item.warrantyMonths)
                    put("price", item.price)
                }
                jsonArray.put(jsonObject)
            }

            val backupFile = File(context.cacheDir, "warranty_vault_backup.json")
            backupFile.writeText(jsonArray.toString(4))

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export Vault Backup"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}