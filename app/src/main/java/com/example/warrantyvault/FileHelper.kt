package com.example.warrantyvault

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object FileHelper {

    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap, fileNamePrefix: String): String? {
        return try {
            val directory = File(context.filesDir, "receipt_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}