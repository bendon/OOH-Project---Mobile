package com.edgetech.bbscout.components.file_saver

import android.graphics.Bitmap
import com.edgetech.bbscout.data.utils.SimpleResource
import java.io.File

interface FileSaver {
    suspend fun saveFile(bitmap: Bitmap, fileNameKey: String = ""): SimpleResource<File>

    suspend fun getBitmapFromPath(path: String): SimpleResource<Bitmap>
}