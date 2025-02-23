package com.edgetech.bbscout.components.file_saver

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.edgetech.bbscout.components.di.IoDispatcher
import com.edgetech.bbscout.data.utils.SimpleResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FileSaverImpl @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FileSaver {

    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "IMG_$timestamp.jpg"
    }

    override suspend fun saveFile(bitmap: Bitmap, fileNameKey: String): SimpleResource<File> = withContext(ioDispatcher) {
        try {
            // Get the pictures directory
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File(storageDir, fileNameKey + generateFilename())

            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            SimpleResource.Success(imageFile)
        } catch (e: Exception) {
            SimpleResource.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun getBitmapFromPath(path: String): SimpleResource<Bitmap> = withContext(ioDispatcher) {
        try {
            val bitmap = BitmapFactory.decodeFile(path)
            if (bitmap != null) {
                println("Error getting file: File gotten")
                SimpleResource.Success(bitmap)
            } else {
                println("Error getting file")
                SimpleResource.Error("Failed to decode bitmap from path: $path")
            }
        } catch (e: Exception) {
            println("Error getting file ${e.message ?: e.localizedMessage}")
            SimpleResource.Error(e.message ?: "An error occurred")
        }
    }


}

