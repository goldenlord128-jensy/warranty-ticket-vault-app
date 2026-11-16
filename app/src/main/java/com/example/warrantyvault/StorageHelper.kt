package com.example.warrantyvault

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object StorageHelper {

    /**
     * Copies the picked image Uri into the app's internal private storage directory
     * and returns the persistent local file path.
     */
    fun saveImageLocally(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, "receipt_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            file.absolutePath // Returns local path (e.g., /data/user/0/.../receipt_xxx.jpg)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}