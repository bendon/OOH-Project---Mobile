package com.edgetech.bbscout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.edgetech.bbscout.screens.BillboardCapture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class BillboardRepository(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "billboards")
    private val gson = Gson()

    // Key for storing capture metadata
    private val CAPTURES_KEY = stringPreferencesKey("captures")

    // Save a new capture
    suspend fun saveCapture(capture: BillboardCapture) {
        val captures = getAllCaptures().toMutableList()
        captures.add(capture)
        saveCapturesList(captures)
    }

    // Get all captures
    fun getAllCaptures(): List<BillboardCapture> {
        val capturesJson = context.dataStore.data
            .map { preferences ->
                preferences[CAPTURES_KEY] ?: "[]"
            }

        val type = object : TypeToken<List<BillboardCapture>>() {}.type
        return gson.fromJson(capturesJson.toString(), type) ?: emptyList()
    }

    // Delete a capture
    suspend fun deleteCapture(capture: BillboardCapture) {
        val captures = getAllCaptures().toMutableList()
        captures.removeAll { it.id == capture.id }
        capture.imageFile.delete() // Delete the actual image file
        saveCapturesList(captures)
    }

    private suspend fun saveCapturesList(captures: List<BillboardCapture>) {
        val capturesJson = gson.toJson(captures)
        context.dataStore.edit { preferences ->
            preferences[CAPTURES_KEY] = capturesJson
        }
    }
}

// Extension function to convert Location to a BillboardCapture
fun File.toBillboardCapture(
    latitude: Double,
    longitude: Double,
    detectedText: String?,
    brands: List<String>,
    contacts: List<String>
) = BillboardCapture(
    imageFile = this,
    latitude = latitude,
    longitude = longitude,
    detectedText = detectedText,
    brands = brands,
    contacts = contacts
)