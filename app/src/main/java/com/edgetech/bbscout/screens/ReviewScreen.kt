package com.edgetech.bbscout.screens

import android.graphics.BitmapFactory
import android.location.Location
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ReviewScreen(
    imageFile: File,
    detectedText: String?,
    extractedBrands: List<String>,
    extractedContacts: List<String>,
    currentLocation: Location?,
    onSave: () -> Unit,
    onRetake: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display the captured image
        Image(
            bitmap = imageFile.inputStream().buffered().use { BitmapFactory.decodeStream(it) }
                .asImageBitmap(),
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display detected text
        detectedText?.let { text ->
            Text(
                text = "Detected Text:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display detected brands
        if (extractedBrands.isNotEmpty()) {
            Text(
                text = "Detected Brands:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = extractedBrands.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display detected contacts
        if (extractedContacts.isNotEmpty()) {
            Text(
                text = "Detected Contacts:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = extractedContacts.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons for Save and Retake
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onRetake) {
                Text("Retake")
            }
            Button(onClick = onSave) {
                Text("Submit")
            }
        }
    }
}

// Helper functions
public fun calculateCaptureQuality(text: String): Float {
    val words = text.split("\\s+".toRegex())
    val totalChars = text.length
    val avgWordLength = if (words.isNotEmpty()) totalChars.toFloat() / words.size else 0f

    val lengthScore = (totalChars.coerceAtMost(200) / 200f) * 0.4f
    val wordScore = (words.size.coerceAtMost(20) / 20f) * 0.4f
    val avgWordScore = (avgWordLength.coerceAtMost(8f) / 8f) * 0.2f

    return (lengthScore + wordScore + avgWordScore).coerceIn(0f, 1f)
}

public fun detectBrands(text: String): List<String> {
    val knownBrands = listOf("Coca-Cola", "Nike", "Apple", "Samsung")
    return knownBrands.filter { text.contains(it, ignoreCase = true) }
}

public fun detectContacts(text: String): List<String> {
    val phoneRegex = Regex("\\+?\\d{10,13}")
    val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    val websiteRegex = Regex("https?://[^\\s]+")

    return listOf(
        *phoneRegex.findAll(text).map { it.value }.toList().toTypedArray(),
        *emailRegex.findAll(text).map { it.value }.toList().toTypedArray(),
        *websiteRegex.findAll(text).map { it.value }.toList().toTypedArray()
    )
}